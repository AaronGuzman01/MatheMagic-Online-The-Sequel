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
    
    public static void main(String[] args) {
        //creates output and input stream and message variables
        DataOutputStream toServer;
        DataInputStream fromServer;
        String message;
        
        //creates and assigns scanner variable to receive user input
        Scanner input = new Scanner(System.in);
        
        try {
            //establishes connection with server
            Socket socket = new Socket(HOST, SERVER_PORT);
            
            System.out.println("MathMagic connection established");
            
            //assigns input and output streams
            fromServer = new DataInputStream(socket.getInputStream());
            
            toServer = new DataOutputStream(socket.getOutputStream());
            
            //console message to receie user input
            System.out.println("Start entering a command:\n");
            
            while (true) {
                //gets user input
                message = input.nextLine();
                
                //sends input to server
                toServer.writeUTF(message);
                
                //receives input from user
                message = fromServer.readUTF();
                
                //prints server message to console
                System.out.println(message);
            }
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
}
