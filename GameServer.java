
import java.io.BufferedReader;
import java.net.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class GameServer {
    GameServer(){
        try{
            InetAddress id = InetAddress.getByName("localhost");
            ServerSocket ss = new ServerSocket(6767,10,id);
            Socket sv = ss.accept();
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(sv.getOutputStream()),true);
            BufferedReader bf = new BufferedReader(new InputStreamReader(sv.getInputStream()));
            pw.println("hello");
            String a = bf.readLine();
            IO.println(a);
            
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new GameServer();
    }
}
