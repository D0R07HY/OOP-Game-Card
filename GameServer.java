
import java.io.*;
import java.net.*;

public class GameServer {
    GameServer(){
        try{
            
            ServerSocket ss = new ServerSocket(6767);
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
