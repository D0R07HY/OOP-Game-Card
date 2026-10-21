import java.io.*;
import java.net.Socket;


public class GameClient 
{
    private Socket sk;
    private BufferedReader in;
    private PrintWriter out;
    private GameWindow ui;

    public void connect(String ip, int port) 
    {
        try
        {
            sk = new Socket(ip, port);

            in = new BufferedReader(new InputStreamReader(sk.getInputStream()));

            out = new PrintWriter(new OutputStreamWriter(sk.getOutputStream()), true);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void sendAction(String actionMsg) 
    {
        if(out != null)
        {
            out.println(actionMsg);
        }
    }

    public void startListening() 
    {
        
    }

}
