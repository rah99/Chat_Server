package chat_server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// The Server GUI

public class ServerGUI extends JFrame implements ActionListener, WindowListener {
	
	private static final long serialVersionUID = 1L; // Ensures the same class used for deserializing as was used serializing
	private JButton stopStart; // Stop and start buttons
	private JTextArea chat, event; // JTextArea for the chat room and the events
	private JTextField tPortNumber; // The port number
	private Server server; // The server
	
	// Main method to Start the Server
	
	public static void main(String[] arg) {
		
		// Start server - default port 4444
		
		new ServerGUI(4444);
	}
	
	// Thread to run the Server
	
	class ServerRunning extends Thread {
		public void run() {
			server.start();
			stopStart.setText("Start");
			tPortNumber.setEditable(true);
			appendEvent("Server crashed\n");
			server = null;
		}
	}
		
	// Server constructor with port parameter listening for connections
	
	ServerGUI(int port) {
		super("Chat Server");
		server = null;
		
		// NorthPanel - PortNumber - Start and Stop buttons
		
		JPanel north = new JPanel();
		north.add(new JLabel("Port number: "));
		tPortNumber = new JTextField("  " + port);
		north.add(tPortNumber);
		
		// Stop or start the server, default as "Start"
		
		stopStart = new JButton("Start");
		stopStart.addActionListener(this);
		north.add(stopStart);
		add(north, BorderLayout.NORTH);
		
		// Event and chat room panels
		
		JPanel center = new JPanel(new GridLayout(2,1));
		chat = new JTextArea(80,80);
		chat.setEditable(false);
		appendRoom("Chat room.\n");
		center.add(new JScrollPane(chat));
		event = new JTextArea(80,80);
		event.setEditable(false);
		appendEvent("Events log.\n");
		center.add(new JScrollPane(event));	
		add(center);
		addWindowListener(this); // The window "X" close button listener
		setSize(400, 600);
		setVisible(true);
	}		

	// Append message to the two JTextAreas and position as last post
	
	void appendRoom(String str) {
		chat.append(str);
		chat.setCaretPosition(chat.getText().length() - 1);
	}
	
	void appendEvent(String str) {
		event.append(str);
		event.setCaretPosition(chat.getText().length() - 1);
		
	}
	
	// Start/Stop on-click listener (all-in-one button)
	
	public void actionPerformed(ActionEvent e) {
		
		// If Server running then button push must stop it
		
		if(server != null) {
			server.stop();
			server = null;
			tPortNumber.setEditable(true);
			stopStart.setText("Start"); // Set button name
			return;
		}
		
      	// Start the Server button push
		
		int port;
		try {
			port = Integer.parseInt(tPortNumber.getText().trim());
		}
		catch(Exception er) {
			appendEvent("Invalid port number");
			return;
		}
		server = new Server(port, this); // Create new Server instance
		new ServerRunning().start(); // Start instance as a thread
		stopStart.setText("Stop"); // Set button name
		tPortNumber.setEditable(false);
	}
	


	// Handling if the user clicks the "X" button to close the application
	
	public void windowClosing(WindowEvent e) {
		if(server != null) { // Tests if a Server exist
			try {
				server.stop(); // Requests the Server to close the connection
			}
			catch(Exception eClose) {
			}
			server = null;
		}
		dispose(); // Dispose of the GUI frame
		System.exit(0);
	}
	
	// Methods needed but not used
	
	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
}
