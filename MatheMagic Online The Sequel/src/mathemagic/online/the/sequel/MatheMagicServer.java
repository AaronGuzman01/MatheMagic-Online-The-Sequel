package mathemagic.online.the.sequel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Aaron Guzman
 */
public class MatheMagicServer {

    private static ServerSocket serverSocket;

    private static final int SERVER_PORT = 9862;

    private static final String FILES_PATH = "Solution Files/";

    private static List<String> userList;
    
    private static ArrayList<Socket> clients;

    private static boolean[] usersLoggedIn;

    private static DataOutputStream[] clientOutputs;

    private static File solutionFile;

    private static int clientNums = 0;
    
    private static boolean shutdown = false;

    public static void main(String[] args) {
        try {
            //creates server socket
            serverSocket = new ServerSocket(SERVER_PORT);

            //gets all valid users
            userList = Files.readAllLines(Path.of("logins.txt"));
            
            //initializes login flags, and output streams and client sockets variables
            usersLoggedIn = new boolean[userList.size()];
            clientOutputs = new DataOutputStream[userList.size()];
            clients = new ArrayList<Socket>();

            //removes unnecessary empty user
            userList.remove(" ");

            //prints server start message to console
            System.out.println("Server started at " + new Date());

            while (true && !shutdown) {
                //receives client connection and prints message to console
                Socket socket = serverSocket.accept();
                clientNums++;

                printMessage("Client connection established");

                new Thread(() -> {
                    HandleClient(socket, clientNums);

                    Thread.yield();
                }).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * The HandleClient method is used to handle a new client connecting to the
     * server
     */
    private static void HandleClient(Socket client, int num) {
        try {
            DataInputStream inputFromClient;
            DataOutputStream outputToClient;
            
            //adds client to list of clients
            clients.add(client);

            //assigns input and output streams
            inputFromClient = new DataInputStream(client.getInputStream());

            outputToClient = new DataOutputStream(client.getOutputStream());

            //runs GetMessage to receive requests from client
            GetMessage(inputFromClient, outputToClient, client, num);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * The GetMessage method is used to process a client's message to the server
     */
    public static void GetMessage(DataInputStream inputFromClient, DataOutputStream outputToClient, Socket client, int clientNum) {
        String request;
        String command;
        String userLogged = null;
        boolean rootLogin = false;
        boolean loggedIn = false;

        while (true && !shutdown) {
            try {
                request = inputFromClient.readUTF();

                printMessage("Client " + String.valueOf(clientNum) + " Request: " + request);

                //checks if client request contains a whitespace (indicates potential flag or data)
                if (request.contains(" ")) {
                    //gets command portion of client request
                    command = request.substring(0, request.indexOf(" "));
                } else {
                    //sets command to empty
                    command = "";
                }

                //checks if command received is LOGIN
                if (command.equals("LOGIN")) {
                    //checks if user is already logged in
                    if (!loggedIn) {
                        //gets login information from LOGIN request
                        String loginInfo = request.substring(request.indexOf(" ") + 1, request.length());

                        //checks login information follows correct format (at least one character for username and password)
                        if (loginInfo.matches(".+ .+")) {
                            //creates user found flag and sets it to false
                            boolean found = false;
                            boolean alreadyLogged = false;
                            int index = 0;

                            //for loop to check each valid user
                            for (String user : userList) {
                                //checks if login information matches current valid user
                                if (user.equals(loginInfo)) {
                                    if (!usersLoggedIn[index]) {
                                        //assigns current logged username
                                        userLogged = user.substring(0, request.indexOf(" "));

                                        //removes unnecessary whitespaces
                                        userLogged = userLogged.trim();

                                        //sets logged in flag
                                        loggedIn = true;

                                        usersLoggedIn[index] = true;
                                        clientOutputs[index] = outputToClient;

                                        //sets found flag
                                        found = true;

                                        //checks if user logged in is root user and sets root login flag
                                        if (userLogged.equals("root")) {
                                            rootLogin = true;
                                        }

                                        //prepares current logged user's solution file
                                        handleUserFile(userLogged);

                                        //sends success message to client
                                        outputToClient.writeUTF("SUCCESS \n");

                                        //breaks from user loop
                                        break;
                                    } else {
                                        alreadyLogged = true;

                                        break;
                                    }
                                }

                                index++;
                            }

                            //check if the user is already logged in and sends message to client
                            if (alreadyLogged) {
                                outputToClient.writeUTF("FAILURE: User is already logged in. \n");
                            } else {
                                //check if the user was not found and sends failure message to client
                                if (!found) {
                                    outputToClient.writeUTF("FAILURE: Please provide a correct username and password. Try again. \n");
                                }
                            }
                        } else {
                            //sends invalid format emessage to client
                            outputToClient.writeUTF("301 message format error \n");
                        }
                    } else {
                        //sends invalid format emessage to client
                        outputToClient.writeUTF("Error: You are already logged in\n");
                    }
                } else if (command.equals("SOLVE")) { //checks if command received is SOLVE
                    //checks if user is logged in
                    if (loggedIn) {
                        //gets problem information from SOLVE request
                        String problem = request.substring(request.indexOf(" ") + 1, request.length());

                        //checks if beinning flag in problem matches the circle flag
                        if (problem.matches("^-c.*")) {
                            //gets problem data from problem information
                            problem = problem.substring(problem.indexOf("c") + 1, problem.length());

                            //checks if problem data matches the correct data format
                            if (problem.matches(" \\d+\\s*")) {
                                //gets radius value form problem data and calculates circumference and area using this value
                                double val = Double.parseDouble(problem);
                                double circumference = 2.0 * Math.PI * val;
                                double area = Math.PI * (val * val);

                                //creates a solution string from calulations
                                String solution = "Circle’s circumference is " + String.format("%.2f", circumference)
                                        + " and area is " + String.format("%.2f", area);

                                //sends solution to client
                                outputToClient.writeUTF(solution + "\n");

                                //writes solution to client file
                                writeToFile("radius " + (int) val + ": " + solution, userLogged);

                            } else if (problem.isEmpty() || problem.isBlank()) { //checks if problem data is empty or blank
                                //sends radius error message to client and writes message to client file
                                outputToClient.writeUTF("Error: No radius found \n");

                                writeToFile("Error: No radius found \n", userLogged);
                            } else {
                                //sends invalid format emessage to client
                                outputToClient.writeUTF("301 message format error \n");
                            }
                        } else if (problem.matches("^-r.*")) { //checks if beinning flag in problem matches the rectangle flag
                            //gets problem data from problem information
                            problem = problem.substring(problem.indexOf("r") + 1, problem.length());

                            //checks if problem data matches the first correct data format
                            if (problem.matches(" \\d+ \\d+\\s*")) {
                                //removes beginning whitespace from problem data
                                problem = problem.substring(1, problem.length());

                                //gets length and width values from problem data and calculates perimeter and area using these values
                                double val1 = Double.parseDouble(problem.substring(0, problem.indexOf(" ")));
                                double val2 = Double.parseDouble(problem.substring(problem.indexOf(" "), problem.length()));
                                double perimeter = 2.0 * (val1 + val2);
                                double area = val1 * val2;

                                //creates a solution string from calculations
                                String solution = "Rectangle’s perimeter is " + String.format("%.2f", perimeter)
                                        + " and area is " + String.format("%.2f", area);

                                //sends solution to client
                                outputToClient.writeUTF(solution + "\n");

                                //writes solution to client file
                                writeToFile("sides " + (int) val1 + " " + (int) val2 + ": " + solution, userLogged);

                            } else if (problem.matches(" \\d+\\s*")) { //checks if problem data matches the second correct data format
                                //gets side value from problem data and calulates permeter and area using this value
                                double val = Double.parseDouble(problem);
                                double perimeter = 4.0 * val;
                                double area = val * val;

                                //creates a solution string from calculations
                                String solution = "Rectangle’s perimeter is " + String.format("%.2f", perimeter)
                                        + " and area is " + String.format("%.2f", area);

                                //sends solution to client
                                outputToClient.writeUTF(solution + "\n");

                                //writes solution to client file
                                writeToFile("sides " + (int) val + " " + (int) val + ": " + solution, userLogged);

                            } else if (problem.isEmpty() || problem.isBlank()) { //checks if problem data is empty or blank
                                //sends sides error message to client and writes message to client file
                                outputToClient.writeUTF("Error: No sides found \n");

                                writeToFile("Error: No sides found \n", userLogged);
                            } else {
                                //sends invalid format message to client
                                outputToClient.writeUTF("301 message format error \n");
                            }
                        } else {
                            //sends invalid format message to client
                            outputToClient.writeUTF("301 message format error \n");
                        }
                    } else {
                        //sends login error message to client
                        outputToClient.writeUTF("Error: You must login to use this command \n");
                    }
                } else if (request.matches("^LIST *")) { //checks if request received is LIST
                    //checks if user is logged in
                    if (loggedIn) {
                        //creates list of strings to store individual interactions and a string to store all interactions as 
                        //a single string
                        List<String> interactions;
                        String list;

                        //gets interactions from logged in user's file
                        interactions = getFileLines(userLogged);

                        //converts user interactions into a single string
                        list = listToString(interactions, userLogged);

                        //appends a newline character at the end of the list string
                        list += "\n";

                        //sends list string to client
                        outputToClient.writeUTF(list);
                    } else {
                        //sends login error message to client
                        outputToClient.writeUTF("Error: You must login to use this command \n");
                    }
                } else if (command.equals("LIST")) { //checks if command received is LIST
                    //gets flag from client request
                    String flag = request.substring(request.indexOf(" "), request.length());

                    //checks if request is in valid format
                    if (flag.charAt(1) != '-') {
                        //sends invalid format message to client
                        outputToClient.writeUTF("301 message format error \n");
                    } else if (rootLogin) { //checks if user logged in is root user
                        //checks if flag is in correct format
                        if (flag.matches("^ -all *")) {
                            //creates list of strings to store individual interactions, a string for the users' name, and a string to
                            //store all interactions as a single string
                            List<String> interactions;
                            String username, list = "";

                            //for loop to process each valid user
                            for (String user : userList) {
                                //gets user's username
                                username = user.substring(0, request.indexOf(" ") + 1);

                                //removes unnecessary whitespaces
                                username = username.trim();

                                //prepares user's solution file
                                handleUserFile(username);

                                //gets interactions from user's file
                                interactions = getFileLines(username);

                                //converts user interactions into a single string
                                list += listToString(interactions, username);

                                //appends a newline character at the end of the list string
                                list += "\n";
                            }

                            //sends list string to client
                            outputToClient.writeUTF(list);
                        } else {
                            //sends invalid format message to client
                            outputToClient.writeUTF("301 message format error \n");
                        }
                    } else {
                        //sends root user error message to client
                        outputToClient.writeUTF("Error: You must be the root user to use this command \n");
                    }
                } else if (command.matches("^MESSAGE *")) { //checks if command received is MESSAGE
                    //checks if user is logged in
                    if (loggedIn) {
                        //removes command portion from request
                        String user = request.substring(request.indexOf((' ')));
                        user = user.trim();

                        //checks if string contains correct format
                        if (user.contains(" ")) {
                            //obtains user's name and message
                            String message = user.substring(user.indexOf(" "));
                            user = user.substring(0, user.indexOf(" "));
                            user = user.trim();
                            message = message.trim();
                            
                            //checks if user is valid or if command flag is valid
                            if (hasUser(user) || user.matches("-all")) {
                                //checks for command flag
                                if (user.matches("-all")) {
                                    //checks if user logged in is root user
                                    if (rootLogin) {
                                        //calls sendMessageAll to write message to all clients
                                        sendMessageAll(message, userLogged, clientNum);
                                        
                                        outputToClient.writeUTF(" ");
                                    } else {
                                        //sends root user error message to client
                                        outputToClient.writeUTF("Error: You must be the root user to use this command \n");
                                    }
                                } else {
                                    //prints sending message to concole
                                    printMessage("Message from client " + clientNum + ": \n" + message);
                                    printMessage("Sending to " + user);
                                    
                                    int userIndex = getClient(user);

                                    if (usersLoggedIn[userIndex]) {
                                        clientOutputs[userIndex].writeUTF("Message from " + userLogged + ": \n" + message + '\n');
                                    } else {
                                        //sends user is not logged in message to client and console
                                        outputToClient.writeUTF("User " + user + " is not logged in \n");
                                        printMessage("User " + user + " is not logged in");
                                        printMessage("Informing client");
                                    }
                                }
                            } else {
                                //sends user does not exist message to client and console
                                outputToClient.writeUTF("User " + user + " does not exist \n");
                                printMessage("User " + user + " does not exist");
                                printMessage("Informing client");
                            }
                        } else {
                            //sends invalid format message to client
                            outputToClient.writeUTF("301 message format error \n");
                        }
                    } else {
                        //sends login error message to client
                        outputToClient.writeUTF("Error: You must login to use this command \n");
                    }
                } else if (request.matches("^SHUTDOWN *")) { //checks if request received is SHUTDOWN
                    //checks if user is logged in
                    if (loggedIn) {
                        //sends 200 OK message to client
                        outputToClient.writeUTF("200 OK \n");

                        //closes client connections
                        for (Socket currClient : clients) {
                            currClient.close();
                        }

                        //closes server
                        serverSocket.close();

                        //sets the shutdown flag
                        shutdown = true;
                        
                        //breaks from while loop
                        break;
                    } else {
                        //sends login error message to client
                        outputToClient.writeUTF("Error: You must login to use this command \n");
                    }
                } else if (request.matches("^LOGOUT *")) { //checks if request received is LOGOUT
                    if (loggedIn) {
                        //gets client index
                        int index = getClient(userLogged);
                        
                        //resets client login flag and output stream
                        usersLoggedIn[index] = false;
                        clientOutputs[index] = null;
                        
                        //resets logged in and root login flags and clears user logged in
                        loggedIn = false;
                        rootLogin = false;
                        userLogged = null;

                        //sends 200 OK message to client
                        outputToClient.writeUTF("200 OK \n");
                    } else {
                        //sends login error message to client
                        outputToClient.writeUTF("Error: You must login to use this command \n");
                    }
                } else {
                    //sends invalid format message to client
                    outputToClient.writeUTF("300 invalid command \n");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * The hasUser method find if a specified user is valid
     */
    private static boolean hasUser(String user) {
        String name;
        
        //checks every valid user
        for (String userName : userList) {
            //obtains name from current user
            name = userName.substring(0, userName.indexOf(" "));
            name = name.trim();
            
            //checks if specified user is equal to name
            if (user.equals(name)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * The getClient method is used to get the index of a client corresponding
     * to the client's login flag and output stream
     */
    private static int getClient(String user) {
        int index = 0;
        String name;

        //checks every valid user
        for (String client : userList) {
            //obtains name from current user
            name = client.substring(0, client.indexOf(" "));
            name = name.trim();
            
            //checks if specified user is equal to name
            if (user.equals(name)) {
                return index;
            }

            index++;
        }

        return -1;
    }
    
    /**
     * The sendMessageAll method is used to print a message from root to every 
     * client logged in to the server 
     */
    private static void sendMessageAll(String message, String sender, int num) {
        int index = 0;
        String client;
        String serverMsg = "Sending to ";
        
        //checks every valid user
        for (String user : userList) {
            //checks if current index is not root user
            if (index > 0) {
                //checks if user at index is logged in
                if (usersLoggedIn[index]) {
                    try {
                        //obtains client name 
                        client = user.substring(0, user.indexOf(" "));
                        client = client.trim();
                        
                        //adds name to server message
                        serverMsg += client + ", ";
                        
                        //displays root message to current client
                        clientOutputs[index].writeUTF("Message from " + sender + ": \n" + message + '\n');
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            
            index++;
        }
        
        //prints server message to console
        printMessage("Message from client " + num + ": \n" + message);
        printMessage(serverMsg);
    }

    /**
     * The printMessage method is used to print a message to the server's
     * console
     */
    private static void printMessage(String message) {
        //prints message received with current date and time to console
        System.out.println(new Date() + ":  " + message);
    }

    /**
     * The handleUserFile method will check and create the solution files folder
     * and will create a solution file for the user received
     */
    private static void handleUserFile(String user) throws IOException {
        //creates a file instance of a solution file using the user received
        solutionFile = new File(FILES_PATH + user + "_solutions.txt");

        //checks if solution files folder does not exist and creates the folder
        if (!Files.exists(Path.of(FILES_PATH))) {
            Files.createDirectory(Path.of(FILES_PATH));
        }

        //creates user solution file if the file does not exist 
        solutionFile.createNewFile();
    }

    /**
     * The getFileLines method will return the contents of a specified user's
     * solution file as a list of strings
     */
    private static List<String> getFileLines(String user) throws IOException {
        //reads all lines from solution file of received user
        List<String> lines = Files.readAllLines(Path.of(FILES_PATH + user + "_solutions.txt"));

        //returns read lines
        return lines;
    }

    /**
     * The listToString method will convert a list of strings into a single
     * customized string message using the user's username
     */
    private static String listToString(List<String> interactions, String user) {
        //string for customized message
        String message;

        //assigns username to message
        message = user + ":\n";

        //checks if the list of string interactions is empty
        if (interactions.isEmpty()) {
            //appends a no interactions message to customized message
            message += "\t" + "No interactions yet";
        } else {
            //for loop to process each interaction
            for (String interaction : interactions) {
                //appends string interaction message to customized message
                message += "\t" + interaction + "\n";
            }

            //removes the last newline character in customized message
            message = message.substring(0, message.length() - 2);
        }

        //returns customized message
        return message;
    }

    /**
     * The writeToFile method will receive a string and write that string to the
     * current logged in user's solution file
     */
    private static void writeToFile(String str, String user) throws IOException {
        //try-with-resource statment to open user's solution file and ensure it is closed
        //also, file is opened in append mode (denoted by the true argument in FileWriter)
        try (FileWriter solutinFile = new FileWriter(FILES_PATH + user + "_solutions.txt", true)) {
            //writes string with newline character to solution file
            solutinFile.write(str + "\n");
        }
    }
}
