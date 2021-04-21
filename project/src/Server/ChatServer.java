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
    private final List<User> activeSessions = new ArrayList<>();
    private final List<String> serverLogs = new LinkedList<>();

    public final InetSocketAddress inetServerAddress;

    public ChatServer(String host, int port)
    {
        inetServerAddress = new InetSocketAddress(host, port);
    }

    public void startServer()
    {
        serverLogs.add(LocalDateTime.now() + " " + "Server was started\n");
        try
        {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(inetServerAddress);
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        }
        catch (IOException ioException) { serverLogs.add(ioException.getMessage()); }

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
                        buffer.flip();

                        var inputMessage = new String(buffer.array(), 0, buffer.limit());

                        processRequest(inputMessage, socketChannel);

                        socketChannel.finishConnect();
                        socketChannel.close();
                    }
                }
            }
            catch (IOException | NoSuchElementException ex) {serverLogs.add(ex.getMessage()); }
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
                    confirmMessage(inputMessage, (String)inputMessage.lines().toArray()[1]);
                    return;
                }
                if(req == Request.GET_MESSAGES)
                {
                    sendView(userName, socketChannel);
                    return;
                }
            }
        }
    }

    private void sendView(String name, SocketChannel socketChannel) throws IOException
    {
        var user = getUserByName(name);

        if(user == null)
            return;

        var message = user.getMessagesForUser().stream()
                .reduce("", (res, userMessage) -> res + userMessage);
        var messageBuffer = ByteBuffer.wrap(message.getBytes());

        socketChannel.write(messageBuffer);
    }

    private void confirmMessage(String message, String name)
    {
        var user = getUserByName(name);
        if (user == null)
            throw new NullPointerException();

        message = message.replaceAll(Request.SEND_MESSAGE.toString(), "").replaceAll(user.loginName, "").trim();
        var processedMessage = String.format("%s: %s\n", user.loginName, message);

        serverLogs.add(LocalDateTime.now() + " " + processedMessage);
        for (var activeUsers : activeSessions)
            activeUsers.addMessage(processedMessage);
    }

    private void loginUser(String name, SocketAddress host)
    {
        name = name.replaceAll("\n|\s", "");
        var user = new User(name);
        var messageToAdd = name + " joined the conversation\n";

        activeSessions.add(user);
        serverLogs.add(LocalDateTime.now() + " " + messageToAdd);

        for (var activeUser : activeSessions)
            activeUser.addMessage(messageToAdd);
    }

    private void logoutUser(String name)
    {
        var user = getUserByName(name);
        if(user == null)
            return;

        var messageToAdd = name + " left the conversation\n";
        for (var activeUser : activeSessions)
            activeUser.addMessage(messageToAdd);

        serverLogs.add(LocalDateTime.now() + " " + messageToAdd);
    }

    private User getUserByName(String name)
    {
        for(var activeUser : activeSessions)
            if (activeUser.loginName.equals(name))
                return activeUser;

        return null;
    }

    public void stopServer()
    {
        isWorking = false;
        serverLogs.add(LocalDateTime.now() + " " + "Server: Server stopped");
    }

    public String getServerLog()
    {
        return "=== Server log ===\n" + serverLogs.stream()
                .reduce("", (tmp, element) -> tmp + element);
    }
}