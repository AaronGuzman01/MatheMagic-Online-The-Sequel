package mathemagic.online.the.sequel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author Aaron Guzman
 */
public class MatheMagicClient {
    
    private static final int SERVER_PORT = 9862;
    
    private static final String HOST = "localhost";
    
    private static Socket socket;
            
    //creates output and input stream and message variables
    private static DataOutputStream toServer;
    private static DataInputStream fromServer;
    private static String message;
        
    public static void main(String[] args) {
        
        //creates and assigns scanner variable to receive user input
        Scanner input = new Scanner(System.in);
        
        try {
            //establishes connection with server
            socket = new Socket(HOST, SERVER_PORT);
            
            System.out.println("MathMagic connection established");
            
            //assigns input and output streams
            fromServer = new DataInputStream(socket.getInputStream());
            
            toServer = new DataOutputStream(socket.getOutputStream());
            
            //console message to receie user input
            System.out.println("Start entering a command:\n");
            
            new Thread(() -> {
                while (true && !socket.isClosed()) {
                    HandleMessage();
                }
            }).start();
            
            while (true && !socket.isClosed()) {
                //gets user input
                message = input.nextLine();
                
                //sends input to server if server is not closed
                if (!socket.isClosed()) {
                    toServer.writeUTF(message);
                }
            }
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    private static void HandleMessage() {
        try {
            //receives input from server if server is not closed
            if (!socket.isClosed()) {
                message = fromServer.readUTF();

                //prints server message to console
                System.out.println(message);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
