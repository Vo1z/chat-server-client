package Client;

import java.util.List;

public class Client
{
    public void login()
    {
        throw new NullPointerException("Method is not implemented yet");
    }

    public void logout()
    {
        throw new NullPointerException("Method is not implemented yet");
    }

    public void send(String req)
    {
        throw new NullPointerException("Method is not implemented yet");
    }

    public String getChatView()
    {
        throw new NullPointerException("Method is not implemented yet");
    }

    public static ChatClientTask create(Client c, List<String> msgs, int wait)
    {
        return new ChatClientTask(c, msgs, wait);
    }
}
