import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


/**
 * ChattyChatChatServer implements a simple multi-client chat server.
 * Allows for clients to connect to the server, update their nickname, send messages to the general chat room, and send DMs to other by nickname.
 */
public class ChattyChatChatServer {
   private static final Map<String, List<ClientHandler>> clients = new ConcurrentHashMap<>(); //map to store active clients, each nickname maps to list of ClientHandler instances

    /**
    * The main method for ChattyChatChatServer, which listens for incoming client connections on the port.
    *
    * @param args Command-line arguments: The user-specified port the server should run on.
    */
   public static void main(String[] args) {
       if (args.length != 1) { //checking if server is started w/ correct number of arguments 
           System.out.println("java ChattyChatChatServer <port>");
           return;
       }


       int port = Integer.parseInt(args[0]); //parsing port number from user input on command line!


       try (ServerSocket serverSocket = new ServerSocket(port)) { //creating a ServerSocket to listen for new client connections on the port
           System.out.println("ChattyChatChat Server started on this port: " + port);

           while (true) {
               Socket clientSocket = serverSocket.accept(); //accepting new server connections
               ClientHandler clientHandler = new ClientHandler(clientSocket); //making a new clientHandler to manage the connected client
               new Thread(clientHandler).start(); //starting a new thread for client communications
           } 
       } catch (IOException e) {
           System.out.println("Error starting server: " + e.getMessage());
       }
   }


   /**
    * ClientHandler handles the communication with a client by listening for messages from the client and sending responses.
    * Clients can send messages, update their nickname, and send DMs to other clients.
    */
   static class ClientHandler implements Runnable {
       private Socket socket; //client connection socket
       private PrintWriter out; //output stream to send data to client
       private BufferedReader in; //input stream to take in client data
       private String nickname = "Anonymous"; //default nickname for a client


       /**
        * Constructor
        *
        * @param socket The client socket.
        */
       public ClientHandler(Socket socket) {
           this.socket = socket; //initializing a socket when a client has connected to the server
       }

       /**
        * The main method of ClientHandler, listens for client messages,
        * processes commands, and broadcasts messages/responds to the client.
        */
       @Override
       public void run() {
           try {
               in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //reading data from the client
               out = new PrintWriter(socket.getOutputStream(), true); //sending data to the client


               //registering the client under the initial nickname
               registerClient();


               broadcast("User " + nickname + " has joined the chat."); //notifying other clients that this new client has joined


               String message;
               while ((message = in.readLine()) != null) { //reading messages from the client
                    message = message.trim();
                    if (message.startsWith("/quit ") && message.length() > 5) {
                       handleQuit(); //client disconnection
                       break;
                   } else if (message.equals("/quit")) {
                       handleQuit(); //handling disconnection
                       break;
                   } else if (message.startsWith("/nick ")) {
                       handleNicknames(message); //updating nickname
                   } else if (message.startsWith("/dm ")) {
                       handleDms(message); //sending dms
                   } else {
                       broadcast(nickname + ": " + message); //sending the message to all connected clients
                   }
               }
           } catch (IOException e) {
               System.out.println("Error handling client: " + e.getMessage());
           } finally {
               closeConnection(); //closing socket
           }
       }


        /**
        * handleNicknames updates the client's nickname and registers the new nickname.
        *
        * @param message "/nick <new-nickname>"
        */
       private void handleNicknames(String message) {
           String[] command = message.split(" ", 2);
           if (command.length == 2) {
               String newNickname = command[1].split(" ")[0]; //only taking the first word as nickname
               unregisterClient(); //removing current nickname from clients map
               nickname = newNickname; //updating nickname
               registerClient(); //registering new nickname
               out.println("Your nickname is now " + nickname); 
           } else {
               out.println("Usage: /nick <name>"); //if missing an argument
           }
       }


        /**
        * handleDms handles the sending of direct messages to other clients.
        *
        * @param message "/dm <nickname> <message>" 
        */
       private void handleDms(String message) {
           String[] command = message.split(" ", 3); //splitting the message for extraction of nickname and message
           if (command.length == 3) {
               String recipientNickname = command[1]; //extracting recipient's nickname
               String dmMessage = command[2]; //extracting dm
               List<ClientHandler> recipientClients = clients.get(recipientNickname); //getting handlers for recipient nickname
               if (recipientClients != null) {
                   for (ClientHandler recipientClient : recipientClients) {
                       recipientClient.out.println("[DM from " + nickname + "]: " + dmMessage); //sending dm
                   }
               } else {
                   out.println("User " + recipientNickname + " not found.");
               }
           } else {
               out.println("Usage: /dm <nickname> <message>");
           }
       }


       /**
        * handleQuit handles disconnecting the client from the server.
        * Broadcasts to all other users that the client has left, and closes the client's socket.
        */
       private void handleQuit() {
           out.println("Goodbyeee!");
           broadcast(nickname + " has left the chat."); //notifying other clients that this user has exited the chat
           closeConnection(); //closing socket
           Thread.currentThread().interrupt();
       }


       /**
        * broadcast sends a message to all connected clients with the exception of the client sending the message.
        *
        * @param message The message to broadcast!
        */
       private void broadcast(String message) {
           for (List<ClientHandler> clientList : clients.values()) { //iterating through registered clients
               for (ClientHandler client : clientList) {
                   if (client != this) { //exclude the sender from recipients of the message
                       client.out.println(message);
                   }
               }
           }
       }


       /**
        * registerClient adds the client under their current nickname to the global clients map.
        */
       private void registerClient() {
           clients.computeIfAbsent(nickname, k -> new CopyOnWriteArrayList<>()).add(this); //adding this client to the list under their nickname
       }


       /**
        * unregisterClient removes the client from the global clients map.
        */
       private void unregisterClient() {
           List<ClientHandler> clientList = clients.get(nickname); //getting list of handlers
           if (clientList != null) {
               clientList.remove(this); //removing handler from list
               if (clientList.isEmpty()) {
                   clients.remove(nickname); //removing nickname from list if there are no handlers left
               }
           }
       }


        /**
        * closeConnection unregisters the client and closes the socket.
        */
       private void closeConnection() {
           unregisterClient(); //deregistering w/ helper function
           try {
            //    socket.close(); //closing client's socket
               if (in != null) in.close(); // Close the input stream
               if (out != null) out.close(); // Close the output stream
               if (socket != null && !socket.isClosed()) socket.close(); // Close the socket
           } catch (IOException e) {
               System.out.println("Error closing socket: " + e.getMessage());
           }
       }
   }
}