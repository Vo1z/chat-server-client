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
        inetSocketAddress = new InetSocketAddress(host, port);
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
        var message = new Message(Request.GET_MESSAGES, id, "");
        send(message.toString());

        return clientView;
    }

    public void send(String req)
    {
        try
        {
            var socketChannel = SocketChannel.open();
            socketChannel.connect(inetSocketAddress);
            var buffer = ByteBuffer.wrap(req.getBytes());

            socketChannel.write(buffer);

            if(req.contains(Request.GET_MESSAGES.toString()))
            {
                //fixme debug
                System.out.println("Waiting for respond...");

                buffer = ByteBuffer.allocate(10000);
                socketChannel.read(buffer);

                clientView += Objects.requireNonNullElse(new String(buffer.array(), 0, buffer.limit()), "");
            }

            socketChannel.finishConnect();
            socketChannel.close();
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait)
    {
        return new ChatClientTask(c, msgs, wait);
    }
}