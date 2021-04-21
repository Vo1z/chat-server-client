package Client;

import Server.Request;

import java.util.List;
import java.util.concurrent.FutureTask;

public final class ChatClientTask extends FutureTask<String>
{
    private final ChatClient client;
    private final List<String> messages;
    private final int waitTime;

    ChatClientTask(ChatClient client, List<String> messages, int waitTime)
    {
        super(() ->
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

            var view = client.getChatView();
            System.out.println(view);

            return view;
        });

        this.client = client;
        this.messages = messages;
        this.waitTime = waitTime;
    }

    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait)
    {
        return new ChatClientTask(c, msgs, wait);
    }

    public ChatClient getClient()
    {
        return client;
    }

}