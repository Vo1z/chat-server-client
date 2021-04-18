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
    private Map<User, SocketAddress> activeSessions = new HashMap<>();
    private List<String> serverLogs = new LinkedList<>();

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
//                var channelsReady = selector.selectNow();
                if(selector.selectNow() < 1)
                    continue;

                var keyIter = selector.selectedKeys().iterator();

                while (keyIter.hasNext())
                {
                    var event = keyIter.next();
                    keyIter.remove();

                    if (event.isAcceptable())
                    {
                        //fixme debug
                        System.out.println("Socket accept operation was invoked");
                        var socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);

                        socketChannel.register(selector, SelectionKey.OP_READ);
                        continue;
                    }
                    if (event.isReadable())
                    {
                        //fixme debug
                        System.out.println("Readable operation was invoked");
                        var socketChannel = (SocketChannel) event.channel();
                        var buffer = ByteBuffer.allocate(10000);

                        socketChannel.read(buffer);

                        //fixme debug
                        System.out.println(buffer.array().length);

                        var inputMessage = "";
                        buffer.flip();

                        while (buffer.)
                        {

                        }

                        //fixme debug
                        System.out.println("Message is: " + inputMessage);

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
                if(req == Request.LOGIN)
                {
                    //fixme debug
                    System.out.println("Login operation was invoked");
                    loginUser(inputMessage.replaceAll(req.toString(), ""), socketChannel.getRemoteAddress());
                    return;
                }
                if(req == Request.LOGOUT)
                {
                    //fixme debug
                    System.out.println("Logout operation was invoked");
                    logoutUser(inputMessage.replaceAll(req.toString(), ""));
                    return;
                }
                if(req == Request.SEND_MESSAGE)
                {
                    //fixme debug
                    System.out.println("Send operation was invoked");
                    confirmMessage(inputMessage, socketChannel.getRemoteAddress());
                    return;
                }
                if(req == Request.GET_MESSAGES)
                {
                    //fixme debug
                    System.out.println("Get operation was invoked");
                    var user = getUserByHost(socketChannel.getRemoteAddress());
                    if (user == null)
                        return;

                    var message = user.getMessagesForUser().stream()
                            .reduce((res, userMessage) -> res += userMessage)
                            .get();
                    var messageBuffer = ByteBuffer.wrap(message.getBytes());

                    socketChannel.write(messageBuffer);
                }
            }
        }
    }

    private void confirmMessage(String message, SocketAddress host)
    {
        var user = getUserByHost(host);
        if (user == null)
            return;

        var processedMessage = String.format("%s: %s%n", user.loginName, message);
        //fixme debug
        System.out.println(processedMessage);
        serverLogs.add(processedMessage + "\n");
        for (var activeUsers : activeSessions.keySet())
            activeUsers.addMessage(processedMessage + "\n");
    }

    private void loginUser(String name, SocketAddress host)
    {
        var user = new User(name, LocalDateTime.now());
        //fixme debug
        System.out.println("User " + user.loginName + " was connected");
        activeSessions.put(user, host);
        serverLogs.add(name + " joined the conversation");
    }

    private void logoutUser(String name)
    {
        if (!isUserLoggedIn(name))
            return;

        var suchUserInSession = activeSessions.keySet().stream().filter(user -> user.loginName.equals(name))
                .findFirst().get();

        activeSessions.remove(suchUserInSession);
        serverLogs.add(name + " left the conversation");
    }

    private User getUserByHost(SocketAddress host)
    {
        for(var key : activeSessions.keySet())
        {
            if(activeSessions.get(key).equals(host))
                return key;
        }

        return null;
    }

    private boolean isUserLoggedIn(String name)
    {
        if (name == null || name.length() == 0)
            return false;

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
        return serverLogs.stream().reduce((res, message) -> res += message).get();
    }
}