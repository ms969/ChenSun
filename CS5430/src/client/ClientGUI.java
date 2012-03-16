package client;

import java.io.*;
import java.net.*;
import java.security.*;

import crypto.PublicKeyCryptoClient;
import crypto.PublicKeyCryptoServer;

public class ClientGUI {
	private static final int SERVER_PORT = 5329;
	private static final String SERVER_HOST_NAME = "localhost";


	public static void main(String[] args) throws Exception {
		// Connecting to server
		Socket kkSocket = null;
		PrintWriter serverOut = null;
		BufferedReader serverIn = null;

		try {
			kkSocket = new Socket(InetAddress.getByName(SERVER_HOST_NAME),
					SERVER_PORT);
			PublicKey pubk = PublicKeyCryptoClient.serverPublicKeyRSA();
			if (pubk == null) {
				System.exit( 1 );
			}
			PublicKeyCryptoClient.clientSideAuth(kkSocket.getInputStream(), kkSocket.getOutputStream(), pubk);
			serverOut = new PrintWriter(kkSocket.getOutputStream(), true);
			serverIn = new BufferedReader(new InputStreamReader(
					kkSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: " + SERVER_HOST_NAME);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: "
					+ SERVER_HOST_NAME);
			System.exit(1);
		}
		System.out.println("Connected to " + SERVER_HOST_NAME + ":"
				+ SERVER_PORT);
		
		BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
		ClientProcessor processor = new ClientProcessor(serverOut, serverIn, keyboard);
		
		// System welcome message
		System.out.println("Welcome to the system.");
		
		/** LOGGING IN **/
		while (true) {
			while (!processor.isLoggedIn() && !processor.isExit()) {
				processor.processLogin();
			}
			
			while(processor.isLoggedIn() && !processor.isExit()) {
				processor.askForInput();
			}
	
			/* TODO Get Information from Server if successfully logged in */
	
//			boolean inSession = true;
//			String input;
//			String socketMsg;
//	
//			while (inSession) {
//				System.out.print(">> ");
//				input = keyboard.readLine();
//				serverOut.println(input);
//				socketMsg = serverIn.readLine();
//				System.out.println(socketMsg);
//			}
		}

//		serverOut.close();
//		serverIn.close();
//		kkSocket.close();
	}

}
