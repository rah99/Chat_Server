package chat_server;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.JOptionPane;

// The server can be run both as a console (IDE or Javac) application or GUI

public class Server {
	private static ClientThread ct;
	private static int uniqueId; // Unique ID for each connection
	private static ArrayList<ClientThread> al; // ArrayList of Clients
	//	private ObjectInputStream sInput;
	private ServerGUI sg; // If launched as a GUI
	private SimpleDateFormat sdf; // Time variable
	private int port; // The port number to listen for connections
	private boolean keepGoing; // Boolean if true will run and false will stop the server
	
	public static void main(String[] args) {
		int portNumber = 4444; // Start server on port 4444 unless a alternative specified
		switch(args.length) {
		case 1:
			try {
				portNumber = Integer.parseInt(args[0]);
			}
			catch(Exception e) {
				System.out.println("Invalid port number.");
				System.out.println("Usage is: > java Server [portNumber]");
				return;
			}
		case 0:
			break;
		default:
			System.out.println("Usage is: > java Server [portNumber]");
			return;	
		}
		
		// create a server object and start it
		
		Server server = new Server(portNumber);
		server.start();
	}

	// Server constructor that receives the port to listen on for connections - as a parameter in console mode
	
	public Server(int port) {
		this(port, null);
	}

	public Server(int port, ServerGUI sg) {
		this.sg = sg; // GUI or Console
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss"); // Displays timestamp hh:mm:ss
		al = new ArrayList<ClientThread>(); // ArrayList for the Client list
	}

	public void start() {
		keepGoing = true;
		
		// Create server socket and wait for connections
		
		try {
			ServerSocket serverSocket = new ServerSocket(port); // Server socket

			// Infinite loop waiting for connections
			
			while(keepGoing) {
				display("Server waiting for Clients on port " + port + ".");
				Socket socket = serverSocket.accept(); // Accept connections
				if(!keepGoing) // If the server is stopped
					break;
				ClientThread t = new ClientThread(socket);  // Make a thread of ClientThread - as clients join the chat
					al.add(t); // Save the new Client in the Array (al)
					t.start();
			}
			
			// If stopped
			
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
						tc.sInput.close();
						tc.sOutput.close();
						tc.socket.close();
					}
					catch(IOException ioE) {
						System.err.println(ioE);
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		} catch (IOException e) {
			String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}
	
	// Stop the server using the GUI

	protected void stop() {
		keepGoing = false;
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
			System.err.println(e);
		}
	}
	
	// Display an event (not a message) to the console or GUI

	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		if(sg == null)
			System.out.println(time);
		else
			sg.appendEvent(time + "\n");
	}
	
	// Broadcast a message to all Clients - synchronised to prevent thread interference and memory consistency errors i.e. message order

	private synchronized void broadcast(String message) {
		String time = sdf.format(new Date()); // Add a timestamp to the message
		String messageLf = time + " " + message + "\n";
		
		// Display the message
		
		if(sg == null)
			System.out.print(messageLf);
		else
			sg.appendRoom(messageLf); // Append in chat window

		// Loop in reverse order in case a Client has disconnected and needs to be removed
		
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			
			// Write to the Clients chat window if it fails for any one of the clients then remove client/s from the list
			
			if(!ct.writeMsg(messageLf)) {
				al.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}

	// Client using the LOGOUT message
	
	synchronized void remove(int id) {
		
		// Scan the array for the id
		
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			if(ct.id == id) { // id found
				al.remove(i);
				return;
			}
		}
	}

	// Instance of this thread will run for each client
	
	class ClientThread extends Thread {
		Socket socket; // The socket for communications
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		int id; // Unique id (disconnection procedure)
		String username;
		ChatMessage cm;
		String date;

		// Constructor
		
		ClientThread(Socket socket) {
			id = ++ uniqueId; // Setting the id value
			this.socket = socket;
			
			// Create I/O Data Streams
			
			System.out.println("Thread trying to create Object Input/Output Streams");
			try {
				
				// Create stream instances
				
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());

				username = (String) sInput.readObject(); // Read the username entered			

//				Set<Thread> threadSet = Thread.getAllStackTraces().keySet(); // Added this for thread ID but it returns all the threads running on a system
//				From reading the possible Thread method parameters I was able to show that the server is creating new threads per connection
				
				display(username + " just connected on Socket: " + socket.getPort() + " Active Threads: " + Thread.activeCount()); // Included the socket port to show connecting to a new socket
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			
			// Catch ClassNotFoundException - reading a String in this case so not sure it will work as Java Docs state it is for a class
			
			catch (ClassNotFoundException e) {
			}
			date = new Date().toString() + "\n";
		}

		public void run() {
			boolean keepGoing = true;
			while(keepGoing) { // Infinite loop until boolean "keepGoing is set to false
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				String message = cm.getMessage();

				// Switch checking message type

				switch(cm.getType()) {
					case ChatMessage.MESSAGE:
						broadcast(username + ": " + message);
						break;
					case ChatMessage.LOGOUT:
						display(username + " disconnected with a LOGOUT message.");
						keepGoing = false;
						break;
					case ChatMessage.WHOISIN:
						writeMsg("List of users connected as of " + sdf.format(new Date()) + "\n");
	
						for(int i = 0; i < al.size(); ++i) { // Scan all connected users to send message to
							ClientThread ct = al.get(i);
							writeMsg((i+1) + ") " + ct.username + " online from " + ct.date);
						}
						break;
				}
			}
			remove(id);
			close();
		}

		// Close everything
		
		private void close() {
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		// Write a message to the Client output stream
 
		private boolean writeMsg(String msg) {
			
			// if Client is connected send the message to it
			
			if(!socket.isConnected()) {
				close();
				return false;
			}
			
			// Write the message to the stream
			
			try {
				sOutput.writeObject(msg);
			}
			
			// Do not abort the whole process If exception, just inform the user
			
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}

	// Popup notifying if the username has already been taken
	public static void infoBox(String infoMsg, String titleBar) {
		JOptionPane.showMessageDialog(null, infoMsg, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
	}
	
    private void findItemInTheList(String itemToFind) {
        if (al.contains(itemToFind)) {
            System.out.println(itemToFind + " was found in the list");
        } else {
            System.out.println(itemToFind + " was not found in the list");
        }
    }
}
