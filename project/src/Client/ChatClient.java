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
        //fixme debug
        System.out.println("Login");
        var message = new Message(Request.LOGIN, id, "");
        //fixme debug
        System.out.println(message);
        send(message.toString());
    }

    public void logout()
    {
        //fixme debug
        System.out.println("Logout");
        var message = new Message(Request.LOGOUT, id, "");
        send(message.toString());
    }

    public String getChatView()
    {
        //fixme debug
        System.out.println("Chat View");
        var message = new Message(Request.GET_MESSAGES, id, "");
        send(message.toString());

        return clientView;
    }

    public void send(String req)
    {
        //fixme debug
        System.out.println("Send");
        try
        {
            var socketChannel = SocketChannel.open();
            socketChannel.connect(inetSocketAddress);
            var buffer = ByteBuffer.wrap(req.getBytes());

            socketChannel.write(buffer);

            if(req.contains(Request.GET_MESSAGES.toString()))
            {
                buffer.clear();
                socketChannel.read(buffer);

                clientView = Objects.requireNonNullElse(buffer.asCharBuffer().toString(), "");
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
