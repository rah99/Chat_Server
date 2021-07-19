package chat_client;

import javax.swing.*;

import chat_server.Server;
import chat_server.ChatMessage;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JLabel label; 	// Holds "Username:", and "Enter message" / "Enter username" at login
	private JTextField tf; 	// Hold the Username and later on the messages
	private JTextField tfServer, tfPort; // Holds the server address an the port number
	private JButton login, logout, whoIsIn; // Login/logout and user lists
	private JTextArea ta; // Chat room text
	private boolean connected; // Check connection
	private Client client; // Client object
	private int defaultPort; // Default port number
	private String defaultHost; // Default host
	

	// Start the Client GUI
	
	public static void main(String[] args) {
		new ClientGUI("localhost", 4444);
	}

	// Constructor connection and GUI build
	
	ClientGUI(String host, int port) {
		super("Really Basic Chat Client");
		defaultPort = port;
		defaultHost = host;
		
		// North Panel
		JPanel northPanel = new JPanel(new GridLayout(3,1)); 
		JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
		tfServer = new JTextField(host); // Server name and port number - both defaulted
		tfPort = new JTextField("" + port);
		tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

		serverAndPort.add(new JLabel("Server Address:  "));
		serverAndPort.add(tfServer);
		serverAndPort.add(new JLabel("Port Number:  "));
		serverAndPort.add(tfPort);
		serverAndPort.add(new JLabel(""));
		northPanel.add(serverAndPort); // Adds the Server an port field to the GUI

		// Label and TextField
		label = new JLabel("Enter your username below", SwingConstants.CENTER);
		northPanel.add(label);
		tf = new JTextField("Guest");
		
		tf.setBackground(Color.WHITE);
		northPanel.add(tf);
		add(northPanel, BorderLayout.NORTH);

		// CenterPanel - Chat Room
		ta = new JTextArea("Welcome to the Really Basic Chat room\n", 80, 80);
		JPanel centerPanel = new JPanel(new GridLayout(1,1));
		centerPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);

		// The buttons
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this); // Listens for button click - this meaning on the specific client GUI
		logout.setEnabled(false); // Must be logged-in before logging-out
		whoIsIn = new JButton("Who is in");
		whoIsIn.addActionListener(this);
		whoIsIn.setEnabled(false); // Disabled unless logged-in

		// South Panel
		JPanel southPanel = new JPanel();
		southPanel.add(login);
		southPanel.add(logout);
		southPanel.add(whoIsIn);
		add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
		tf.requestFocus();
	}

	// Append text in the TextArea 
	
	void append(String str) {
//		System.out.println(str);
//		Integer identify = getWhoIsIn();
//		System.out.println(identify);
//		if (str.matches(".*\\d.*") && client.equals(this.client)) {
//			
//			ta.append("You: "+ str);
//		} else {
//			ta.append(str);
//		}
		if (str.contains(this.username)) {
//		if(str == this.toString()) {
			String OwnStr = str.replace(username + ":", "You:");
			ta.append(OwnStr);
		} else {
			ta.append(str);
		}
		System.out.println(str);
		ta.setCaretPosition(ta.getText().length() - 1);
	}
	
	public String replace(String str) {
		
		return str;
	}
	
	// If the connection failed - reset buttons, labels and textfields
	
	void connectionFailed() {
		login.setEnabled(true);
		logout.setEnabled(false);
		whoIsIn.setEnabled(false);
		label.setText("Enter your username below");
		tf.setText("Guest");
		tfPort.setText("" + defaultPort); // Reset port number and host name
		tfServer.setText(defaultHost);
		tfServer.setEditable(false);
		tfPort.setEditable(false);
		tf.removeActionListener(this);
		connected = false;
	}
		
	// Button or JTextField clicked
	
	public void actionPerformed(ActionEvent e) {
		Object clientObject = e.getSource();
		
		// Logout button
		
		if(clientObject == logout) {
			client.sendMessage(new ChatMessage(ChatMessage.getLOGOUT(), ""));
			super.setTitle("Really Basic Chat Client");
			return;
		}
		
		// Who is in button
		
		if(clientObject == whoIsIn) {
			client.sendMessage(new ChatMessage(ChatMessage.getWHOISIN(), ""));				
			return;
		}

		// JTextField
		
		if(connected) {
			client.sendMessage(new ChatMessage(ChatMessage.getMESSAGE(), tf.getText()));				
			tf.setText("");
			return;
		}
		

		if(clientObject == login) {
			String username = tf.getText().trim(); // Connection request
			if(username.equalsIgnoreCase("Guest") || username.equalsIgnoreCase("")) { // If username empty ignore it
				infoBox("Please enter a username / handle to continue.", "MESSAGE");
				return;
			}
			String server = tfServer.getText().trim();
			if(server.length() == 0)  // If empty serverAddress ignore it
				return;
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() == 0) // If empty or invalid port number, ignore it
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				System.err.println("Invalid Port");
				return;   // Nothing can be done if port is invalid
			}

			
			client = new Client(server, port, username, this); // Create a new Client instance from Client.class
			
			setUsername(username);
			
			// Client start test
			
			if(!client.start()) 
				return;
			tf.setText("");
			super.setTitle("Really Basic Chat Client - user: " + username); // Changes the super title to include the username when logged-in
			label.setText(username + ", please enter your message below"); // Personalises the chat entry
			connected = true;
			
			// Disable
			
			login.setEnabled(false);
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			
			// Enable
			
			logout.setEnabled(true);
			whoIsIn.setEnabled(true);
			
			tf.addActionListener(this); // Listener for when the user sends a message 
		}
	}
	
	String username = " ";
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
	public int getWhoIsIn() {
		return ChatMessage.getWHOISIN();
	}
	
	// Popup notifying if the username has already been taken
	public static void infoBox(String infoMsg, String titleBar) {
		JOptionPane.showMessageDialog(null, infoMsg, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
	}
}

