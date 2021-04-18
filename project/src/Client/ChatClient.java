package Client;

import Server.Request;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Objects;

public class ChatClient
{
    public final String id;
    public final InetSocketAddress inetSocketAddress;

    private String clientView = "";

    public ChatClient(String host, int port, String id)
    {
        this.id = id;
        inetSocketAddress = InetSocketAddress.createUnresolved(host, port);
    }

    public void login()
    {
        var message = new Message(Request.LOGIN, id, "");
        send(message.toString());
    }

    public void logout()
    {
        var message = new Message(Request.LOGOUT, id, "");
        send(message.toString());
    }

    public String getChatView()
    {
        try
        {
            var socketChannel = SocketChannel.open(inetSocketAddress);
            var buffer = ByteBuffer.allocate(Integer.MAX_VALUE);

            socketChannel.read(buffer);
            socketChannel.close();

            clientView = Objects.requireNonNullElse(new String(buffer.asCharBuffer().array()), "");
        }
        catch (IOException e) { throw new NullPointerException("Method is not implemented yet"); }

        return clientView;
    }

    public void send(String req)
    {
        try
        {
            var socketChannel = SocketChannel.open(inetSocketAddress);
            var buffer = ByteBuffer.wrap(req.getBytes());
            socketChannel.write(buffer);

            socketChannel.close();
        }
        catch (IOException e) { throw new NullPointerException("Method is not implemented yet"); }
    }

    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait)
    {
        return new ChatClientTask(c, msgs, wait);
    }
}
