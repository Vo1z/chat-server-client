package Client;

import Server.Request;

import java.util.List;

public final class ChatClientTask implements Runnable
{
    private final Client client;
    private final List<String> messages;
    private final int waitTime;

    public ChatClientTask(Client client, List<String> messages, int waitTime)
    {
        this.client = client;
        this.messages = messages;
        this.waitTime = waitTime;
    }

    @Override
    public void run()
    {
        client.login();

        for(var msg : messages)
        {
            client.send(Request.SEND_MESSAGE + msg);
            try { Thread.sleep(waitTime); }
            catch (InterruptedException ignored) { }
        }

        client.logout();
    }
}
