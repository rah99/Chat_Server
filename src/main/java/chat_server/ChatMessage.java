package chat_server;

import java.io.*;

/*
 * This class defines the different type of messages that will be exchanged between the Clients and the Server,
 * making it easier for communications as no need to count bytes or wait for a line feed at the end of the frame
 */

public class ChatMessage implements Serializable {

	protected static final long serialVersionUID = 1112122200L;

	// The types of message sent by the Client:
	// 		WHOISIN to receive the list of the users connected
	// 		MESSAGE an ordinary message
	// 		LOGOUT to disconnect from the Server

	final static int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2;
	private int type;
	private String message;
	
	// Constructor
	
	public ChatMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}
	
	// Getters
	
	public int getType() {
		return type;
	}
	
	public String getMessage() {
		return message;
	}

	public static int getWHOISIN() {
		return WHOISIN;
	}
	
	public static int getMESSAGE() {
		return MESSAGE;
	}
	
	public static int getLOGOUT() {
		return LOGOUT;
	}
}
