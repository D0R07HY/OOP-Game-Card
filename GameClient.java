import java.io.*;
import java.net.Socket;


public class GameClient 
{
    private Socket sk;
    private BufferedReader in;
    private PrintWriter out;
    private GameWindow ui;

    public void setUi(GameWindow ui)
    {
        this.ui = ui;
    }

    public void SetConnect(String ip, int port) 
    {
        try
        {
            sk = new Socket(ip, port);

            in = new BufferedReader(new InputStreamReader(sk.getInputStream()));

            String text = in.readLine();
            
            IO.println(text);

            out = new PrintWriter(new OutputStreamWriter(sk.getOutputStream()), true);

            out.println("Hello Arty");

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
        if (in == null)
        {
            return;
        }

        Thread listener = new Thread(() ->
        {
            try
            {
                String line;
                while ((line = in.readLine()) != null)
                {
                    if (ui != null)
                    {
                        ui.updateBoardState(line);
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });

        listener.start();
    }


    public static void main (String[] args)
    {
        GameClient gm = new GameClient();
        
        gm.SetConnect("10.106.71.250" , 6767);
        
    }
}
