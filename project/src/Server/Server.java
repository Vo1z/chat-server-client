package Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.LinkedList;
import java.util.List;

public class Server extends Thread
{
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private boolean isWorking = false;
    private List<String> messages = new LinkedList<>();

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
        try
        {
            while (isWorking)
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
                        //request

                        continue;
                    }

                    if (event.isWritable())
                    {
                        var socketChannel = (SocketChannel) event.channel();
                        //response

                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
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

    }

    String getServerLog()
    {
        throw new NullPointerException("Method is not implemented");
    }
}