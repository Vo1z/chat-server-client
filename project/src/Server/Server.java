package Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.time.LocalDateTime;
import java.util.*;

public class Server extends Thread
{
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private boolean isWorking = false;
    private Map<User, SocketAddress> activeSessions = new HashMap<>();
    private List<String> serverLogs = new LinkedList<>();

    public final String host;
    public final int port;

    public Server(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run()
    {
        while (isWorking)
        {
            try
            {

                var selectorKeys = selector.keys();
                var keyIter = selectorKeys.iterator();

                while (keyIter.hasNext())
                {
                    var event = keyIter.next();

                    keyIter.remove();

                    if (event.isAcceptable())
                    {
                        var socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);

                        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        continue;
                    }
                    if (event.isReadable())
                    {
                        var socketChannel = (SocketChannel) event.channel();
                        var buffer = ByteBuffer.allocate(Integer.MAX_VALUE);

                        socketChannel.read(buffer);

                        var inputMessage = new String(buffer.asCharBuffer().array());

                        processRequest(inputMessage, socketChannel);

                        continue;
                    }
                    if (event.isWritable())
                    {
                        var socketChannel = (SocketChannel) event.channel();
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
            catch (IOException | NoSuchElementException ignored) { }
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
                    loginUser(inputMessage.replaceAll(req.toString(), ""), socketChannel.getRemoteAddress());
                    return;
                }
                if(req == Request.LOGOUT)
                {
                    logoutUser(inputMessage.replaceAll(req.toString(), ""));
                    return;
                }
                if(req == Request.SEND_MESSAGE)
                {
                    confirmMessage(inputMessage, socketChannel.getRemoteAddress());
                    return;
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
        serverLogs.add(processedMessage);
        for (var activeUsers : activeSessions.keySet())
            activeUsers.addMessage(processedMessage);
    }

    private void loginUser(String name, SocketAddress host)
    {
        var user = new User(name, LocalDateTime.now());
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

    public void startServer()
    {
        try
        {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(host, port));
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

    public void stopServer()
    {
        isWorking = false;
        System.out.println("Server: Server stopped");
    }

    String getServerLog()
    {
        throw new NullPointerException("Method is not implemented");
    }
}