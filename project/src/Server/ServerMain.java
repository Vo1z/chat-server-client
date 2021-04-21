package Server;

public class ServerMain
{
    public static void main(String[] args) throws InterruptedException
    {
        var server = new ChatServer("192.168.0.106", 167);

        server.startServer();

        Thread.sleep(10000);

        server.stopServer();
        System.out.println(server.getServerLog());
    }
}
