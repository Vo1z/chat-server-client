package Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.time.LocalDateTime;
import java.util.*;

public final class ChatServer extends Thread
{
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private boolean isWorking = false;
    private final Map<User, String> activeSessions = new HashMap<>();
    private final List<String> serverLogs = new ArrayList<>();

    public final InetSocketAddress inetServerAddress;

    public ChatServer(String host, int port)
    {
        inetServerAddress = new InetSocketAddress(host, port);
    }

    public void startServer()
    {
        //fixme debug
        System.out.println("Server is listening on " + inetServerAddress.getHostString() + ":" + inetServerAddress.getPort());

        try
        {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(inetServerAddress);
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        }
        catch (IOException ioException)
        {
            ioException.printStackTrace();
        }

        isWorking = true;
        start();
    }

    @Override
    public void run()
    {
        while (isWorking)
        {
            try
            {
                if(selector.selectNow() == 0)
                    continue;

                var keyIter = selector.selectedKeys().iterator();

                while (keyIter.hasNext())
                {
                    var event = keyIter.next();
                    keyIter.remove();

                    if (event.isAcceptable())
                    {
                        var socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);

                        socketChannel.register(selector, SelectionKey.OP_READ);
                        continue;
                    }
                    if (event.isReadable())
                    {
                        var socketChannel = (SocketChannel) event.channel();
                        var buffer = ByteBuffer.allocate(10000);

                        socketChannel.read(buffer);

                        var inputMessage = new String(buffer.array(), 0, buffer.limit());

                        processRequest(inputMessage, socketChannel);

                        socketChannel.finishConnect();
                        socketChannel.close();
                    }
                }
            }
            catch (IOException | NoSuchElementException ignored) {ignored.printStackTrace(); }
        }
    }

    private void processRequest(String inputMessage, SocketChannel socketChannel) throws IOException
    {
        if(inputMessage == null || inputMessage.length() == 0)
            return;

        for(var req : Request.values())
        {
            if(inputMessage.contains(req.toString()))
            {
                var userName = inputMessage.replaceAll(req.toString().trim()+ "|\n", "");

                if(req == Request.LOGIN)
                {
                    loginUser(userName, socketChannel.getRemoteAddress());
                    return;
                }
                if(req == Request.LOGOUT)
                {
                    logoutUser(userName);
                    return;
                }
                if(req == Request.SEND_MESSAGE)
                {
                    confirmMessage(inputMessage, socketChannel.getRemoteAddress().toString().replaceAll(":\\d+|\\\\", ""));
                    return;
                }
                if(req == Request.GET_MESSAGES)
                {
                    var user = getUserByHost(socketChannel.getRemoteAddress().toString().replaceAll(":\\d+|\\\\", ""));

                    if (user == null)
                        throw new NullPointerException();

                    //fixme debug
                    System.out.println("GET_MESSAGE request for user " + user.loginName);
                    var message = user.getMessagesForUser().stream()
                            .reduce("", (res, userMessage) -> res + userMessage);
                    var messageBuffer = ByteBuffer.wrap(message.getBytes());

                    socketChannel.write(messageBuffer);

                    return;
                }
            }
        }
    }

    private void confirmMessage(String message, String host)
    {
        var user = getUserByHost(host);
        if (user == null)
            throw new NullPointerException();

        var processedMessage = String.format("%s: %s", user.loginName, message);
        //fixme debug
        System.out.println(processedMessage);
        serverLogs.add(processedMessage + "\n");
        for (var activeUsers : activeSessions.keySet())
            activeUsers.addMessage(processedMessage + "\n");
    }

    private void loginUser(String name, SocketAddress host)
    {
        name = name.replaceAll("\n|\s", "");
        var user = new User(name, LocalDateTime.now());
        var messageToAdd = name + " joined the conversation";

        //fixme debug
        System.out.println("User " + user.loginName + " was connected: " + host.toString());

        activeSessions.put(user, host.toString().replaceAll(":\\d+|\\\\", ""));
        serverLogs.add(messageToAdd);

        for (var activeUser : activeSessions.keySet())
            activeUser.addMessage(messageToAdd);
    }

    private void logoutUser(String name)
    {
        if (!isUserLoggedIn(name))
            return;

        var messageToAdd = name + " left the conversation";
        var suchUserInSession = activeSessions.keySet().stream().filter(user -> user.loginName.equals(name))
                .findFirst().get();

        activeSessions.remove(suchUserInSession);
        serverLogs.add(messageToAdd);

        //fixme debug
        System.out.println("User " + suchUserInSession.loginName + " was disconnected: " + suchUserInSession.toString());


        for (var activeUser : activeSessions.keySet())
            activeUser.addMessage(messageToAdd);
    }

    private User getUserByHost(String host)
    {
        for(var activeUser : activeSessions.keySet())
        {
            if (activeSessions.get(activeUser).equals(host))
                return activeUser;
        }

        throw new NullPointerException();
        //return null;
    }

    private boolean isUserLoggedIn(String name)
    {
        if (name == null || name.length() == 0)
            throw new NullPointerException();

        return activeSessions.keySet().stream()
                .map(user -> user.loginName)
                .anyMatch(loginName -> loginName.equals(name));
    }

    public void stopServer()
    {
        isWorking = false;
        System.out.println("Server: Server stopped");
    }

    public String getServerLog()
    {
        return serverLogs.stream().reduce("", (res, message) -> res + message);
    }
}