# Chat_Server
The solution adopted for the client/server chatroom was to create 5 classes, namely, ChatMessage, Server, Client, Server GUI, and Client GUI that handle the functions of TCP connections, Threads, and a GUI for a chatroom client and server. Information sent over TCP are sent as bytes, for this chatroom ObjectInputStream and ObjectOutputStream is initialized from the Socket stream reader, this process is serialized and deserialized by Java. Chat is sent between the clients through the server where they are sent and received as string objects, other than the initial string for the user to login. Initially, the program did not differentiate between the user's chat in the chat pane and that of other users (a fix has been applied, however further work is required to either prevent another user of the same name or recognize the local chat pane associated with the user). This application works in console and as a GUI, the GUI has options to change the host and port, it also allows a user to enter a “handle” which then confirms their handle above the message entry area and displays own messages as “You”. Whilst the server is running it is constantly listening for new connections that it then passes to the Client Thread run() to create a connection. Users can see who is logged in from the GUI by checking WHOISIN, this list is created from the stored array of unique user ID’s connected to their username.

## User Guide
1. Start Server GUI
2. Select "Start" button
3. Start Chat Client GUI - can start multiple
4. Type in a user name (Guest has been blocked) - please use different names - as mentioned Guest blocked but more work needed on unique users
5. Press "Login" button
6. Whos In shows logged in users
7. Sending a message only requires typing in the text field at the top of the GUI and pressing enter when wanting to send
8. Logging out is performed by pressing the "Logout" button
