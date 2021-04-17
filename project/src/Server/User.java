package Server;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User
{
    public final String loginName;
    public final LocalDateTime loginTime;
    private final List<String> messagesForUser = new ArrayList<>();

    public User(String login, LocalDateTime loginTime)
    {
        this.loginName = login;
        this.loginTime = loginTime;
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
