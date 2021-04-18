package Client;

import Server.Request;

import java.util.List;

public final class ChatClientTask implements Runnable
{
    private final ChatClient client;
    private final List<String> messages;
    private final int waitTime;

    public ChatClientTask(ChatClient client, List<String> messages, int waitTime)
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
            var message = new Message(Request.SEND_MESSAGE, client.id, msg);

            client.send(message.toString());
            try { Thread.sleep(waitTime); }
            catch (InterruptedException ignored) { }
        }

        client.logout();
    }
}
