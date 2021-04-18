package Client;

import Server.Request;

public class ClientMain
{
    public static void main(String[] args)
    {
        var client = new ChatClient("192.168.0.106", 167, "Viktor");

        client.login();
        var message = new Message(Request.SEND_MESSAGE, client.id, "Hello");
        client.send(message.toString());
        client.logout();
    }
}
