package client;


import java.io.*;
import java.net.*;
import java.security.*;
import javax.crypto.*;

import shared.ProjectConfig;

import crypto.KeyNonceBundle;
import crypto.PublicKeyCryptoClient;
import crypto.SharedKeyCryptoComm;

public class ClientGUI {
	private static final int SERVER_PORT = ProjectConfig.SERVER_CLIENT_CONN_PORT;
	private static final String SERVER_HOST_NAME = "localhost";


	public static void main(String[] args) throws Exception {
		// Connecting to server
		Socket kkSocket = null;
		OutputStream serverOut = null;
		InputStream serverIn = null;
		Cipher c = null;
		KeyNonceBundle knb = null;

		try {
			kkSocket = new Socket(InetAddress.getByName(SERVER_HOST_NAME),
					SERVER_PORT);
			
			PublicKey pubk = PublicKeyCryptoClient.serverPublicKeyRSA();
			if (pubk == null) {
				System.exit( 1 );
			}
			knb = PublicKeyCryptoClient.clientSideAuth(kkSocket.getInputStream(), kkSocket.getOutputStream(), pubk);
			c = SharedKeyCryptoComm.createCipher(SharedKeyCryptoComm.ALG);
			
			serverOut = kkSocket.getOutputStream();
			serverIn = kkSocket.getInputStream();
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: " + SERVER_HOST_NAME);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: "
					+ SERVER_HOST_NAME);
			System.exit(1);
		}
		if (knb == null) { //the protocol failed...
			System.out.println("Authentication Protocol Failed - Server is unsafe to connect to!");
			System.exit(1);
		}
		System.out.println("Connected to " + SERVER_HOST_NAME + ":"
				+ SERVER_PORT);
		
		BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
		ClientProcessor processor = new ClientProcessor(serverOut, serverIn, keyboard, c, 
				knb.getSk(), knb.getSendNonce(), knb.getRecvNonce());
		
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

		}

	}

}
