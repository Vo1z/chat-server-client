package Client;

import Server.Request;

import java.util.Objects;

public class Message
{
    public final Request request;
    public final String id;
    public final String content;

    public Message(Request request, String id, String content)
    {
        this.request = request;
        this.id = id;
        this.content = Objects.requireNonNullElse(content, "");
    }

    @Override
    public String toString()
    {
        return String.format("%s\n%s\n%s", request.toString(), id, content).trim();
    }
}
