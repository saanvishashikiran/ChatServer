import java.io.*;
import java.net.*;


/**
 * The ChattyChatChatClient class allows clients to connect to a specified server, and listens
 * for incoming messages while sending user inputs to the server. 
 */
public class ChattyChatChatClient {
   private Socket socket; //client socket connection
   private PrintWriter out; //output stream to send data to client
   private BufferedReader in; //input stream to take in client data
   private BufferedReader userInputReader; //reader to handle command line inputs

    /**
     * The main method for initiating the client connection to the server.
     *
     * @param args Command line arguments (need to be hostname and port)
     */
   public static void main(String[] args) {
       if (args.length != 2) { //checking to make sure necessary arguments are provided
           System.out.println("Usage: java ChattyChatChatClient <hostname> <port>");
           return;
       }

       String hostname = args[0]; //extracting hostname
       int port = Integer.parseInt(args[1]); //extracting port number (will be right after hostname)

       //creating an instance of ChattyChatChatClient and connect to server
       ChattyChatChatClient client = new ChattyChatChatClient();
       client.connectToServer(hostname, port);
   }


   /**
     * connectToServer establishes a connection to the server using the specified hostname
     * and port, initializes the necessary streams for communication, and initializes a
     * separate thread for handling incoming messages from the server!
     *
     * @param hostname The server's hostname.
     * @param port The port number the server is listening on.
     */
   public void connectToServer(String hostname, int port) {
       try {
           socket = new Socket(hostname, port); //establishing connection to server
           out = new PrintWriter(socket.getOutputStream(), true); //output stream to send messages to the server
           in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //input stream to read messages from the server
           userInputReader = new BufferedReader(new InputStreamReader(System.in)); //input stream to read command line inputs

           System.out.println("Connected to ChattyChatChat Server");

           new Thread(new IncomingMessageHandler()).start(); //new thread to handle incoming server messages

           String userInput;
           while ((userInput = userInputReader.readLine()) != null) {
               userInput = userInput.trim();
               if (userInput.equals("/quit")) { //exiting the server 
                   out.println(userInput); //sending user input to server
                   break;
               }
               out.println(userInput);
           }
       } catch (IOException e) {
           System.out.println("Error connecting to server: " + e.getMessage());
       } finally {
           closeConnection(); //closing connection when done
           System.exit(0);
       }
   }


   /**
     * closeConnection closes all client resources (socket and input/output streams).
     */
   private void closeConnection() {
       try {
           if (socket != null) socket.close(); //closing socket
           if (out != null) out.close(); //closing output stream
           if (in != null) in.close(); //closing server input stream
           if (userInputReader != null) userInputReader.close(); //closing user input stream
       } catch (IOException e) {
           System.out.println("Error closing client resources: " + e.getMessage());
       }
   }


    /**
     * IncomingMessageHandler handles incoming messages from the server by continuously
     * listening for messages and outputting them to the console.
     */
   private class IncomingMessageHandler implements Runnable {
       @Override
       public void run() {
           try {
               String message;
               while ((message = in.readLine()) != null) { //continuously reads messages from the server
                   System.out.println(message); //outputs messages
               }
           } catch (IOException e) {
               System.out.println("Disconnected from server.");
           }
       }
   }
}