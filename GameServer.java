package Assignment;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Alvin on 27/10/2017.
 */
public class GameServer {

    private static final int PORT = 8888;
    public static final Game game = new Game();

    public static void main(String[] args) throws IOException{

        //The Game object is created. This object is shared between all threads.
        // The server socket is also created which allows the threads to communicate with the Game object.
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Started GameServer at port " + PORT);
        System.out.println("Waiting for players to join...");

        TomcatServer tc = new TomcatServer();
        new Thread(tc).start();

        while(true){
            Socket socket = server.accept();
            System.out.println("Player connected");
            GameService service = new GameService(game, socket, false);
            new Thread(service).start();
        }
    }

}
