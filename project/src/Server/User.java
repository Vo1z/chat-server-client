package Server;

import java.util.ArrayList;
import java.util.List;

public class User
{
    public final String loginName;
    private final List<String> messagesForUser = new ArrayList<>();

    public User(String login)
    {
        this.loginName = login;
    }

    public void addMessage(String message)
    {
        messagesForUser.add(message);
    }

    public List<String> getMessagesForUser()
    {
        return messagesForUser;
    }
}