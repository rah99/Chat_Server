package chat_client;

import java.net.*;
import java.io.*;
import java.util.*;

import chat_server.ChatMessage;

// The Client that can be run as Console or GUI
 
public class Client  {

	// for I/O
	private ObjectInputStream sInput; // Read from the socket
	private ObjectOutputStream sOutput; // Write on the socket
	private Socket socket;
	private ClientGUI cg; // Using GUI or Console
	private String server, username;
	private int port;

	Client(String server, int port, String username) {
		
		// Calls common constructor with the GUI set to null
		
		this(server, port, username, null);
	}

	// Constructor called when used from GUI, in console mode the ClienGUI parameter is null

	Client(String server, int port, String username, ClientGUI cg) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.cg = cg; // Saved if in GUI mode or not
	}
	
	// To start the dialog
	
	public boolean start() {
		
		// Try to connect to the server
		
		try {
			socket = new Socket(server, port);
		}
		
		// If failed can only display the error
		
		catch(Exception ec) {
			display("Error connectiong to server:" + ec);
			return false;
		}
		
		String msg = "Connection accepted " + socket.getInetAddress() + " : " + socket.getPort();
		display(msg);
	
		// Creating I/O data streams
		
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// Creates Server listener Thread 
		
		new ListenFromServer().start();
		
		// Send username to the server as a string - all chat messages will be sent as objects
		
		try {
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exception during login : " + eIO);
			disconnect();
			return false;
		}
		return true; // Inform caller it worked
	}

	// Send message to the Console or the GUI
	
	private void display(String msg) {
		if(cg == null)
			System.out.println(msg); // println for Console mode
		else
			cg.append(msg + "\n"); // Append to, for example, JTextArea in the ClientGUI
	}
	
	// Send a message to the Server
	
	public void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	// If something fails close the Input/Output streams and disconnect
	
	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {}
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {}
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {}
		
		// Notify the GUI
        
		if(cg != null)
			cg.connectionFailed();
			
	}

	public static void main(String[] args) {
		
		// Default values
		
		int portNumber = 4444;
		String serverAddress = "localhost";
		String userName = "Guest";

		switch(args.length) {
			case 3:  // > javac Client username portNumber serverAddr
				serverAddress = args[2];
			case 2: // > javac Client username portNumber
				try {
					portNumber = Integer.parseInt(args[1]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
					return;
				}	
			case 1: // > javac Client username
				userName = args[0];
			case 0: // > java Client
				break;
			default: // invalid number of arguments
				System.out.println("Usage is: > java Client [username] [portNumber] {serverAddress]");
			return;
		}
		
		// create the Client object
		
		Client client = new Client(serverAddress, portNumber, userName);
		
		// Test connection to the Server, no resolution if not - retry

		if(!client.start())
			return;
		
		// Infinite loop waiting for message from the user
		
		Scanner scan = new Scanner(System.in);

		while(true) {
			System.out.print("> ");
			String msg = scan.nextLine(); // Read message
			if(msg.equalsIgnoreCase("LOGOUT")) { // Logout if message is LOGOUT (GUI = button push)
				client.sendMessage(new ChatMessage(ChatMessage.getLOGOUT(), ""));
				break; // Break disconnects
			} else if(msg.equalsIgnoreCase("WHOISIN")) { // Message checking WhoIsIn the chat (GUI = button push)
				client.sendMessage(new ChatMessage(ChatMessage.getWHOISIN(), ""));				
			} else { // Default to "typed" message
				client.sendMessage(new ChatMessage(ChatMessage.getMESSAGE(), msg));
			}
		}
		client.disconnect(); // Disconnected if LOGOUT
		scan.close();
	}

	// Waits for the message from the server and appends to the JTextArea for a GUI or System.out.println() if in console mode

	class ListenFromServer extends Thread {
		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject(); // If console mode prints the message and re-add the prompt ">"
					if(cg == null) {
						System.out.println(msg);
						System.out.print("> ");
						//					} else if(msg.contains(cg.username)) {
						//						cg.append(msg.replace(cg.username + ":", "You: "));
					} else {
						cg.append(msg);
					}
				} catch(IOException e) {
					display("Server has closed the connection: " + e);
					if(cg != null) 
						cg.connectionFailed();
					break;
				} catch(ClassNotFoundException e2) { // Same issue here as in Server.class but needs to be handled
				}
			}
		}
	}
}
