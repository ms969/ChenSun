package server;

import java.net.*;
import java.io.*;

/* Setup code and Connection code courtesy of 
 * tutorials in docs.oracle.com
 */

//TODO should we merely just exit if the server finds an error?

public class SocialNetworkServer {
	private static final int LISTEN_PORT_NUM = 5329;
	public static final boolean DEBUG = true;

	public static void main(String[] args) throws IOException {

		ServerSocket serverSocket = initializeSocket();

		while (true) {
			acceptClient(serverSocket);
		}
		
		 
	}

	private static ServerSocket initializeSocket() {
		ServerSocket serverSocket = null;
		try {
			// creates the server socket and listens on port 4443
			serverSocket = new ServerSocket(LISTEN_PORT_NUM);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + LISTEN_PORT_NUM
					+ ".");
			// TODO exit here?
			System.exit(1);
		}
		return serverSocket;
	}

	private static void acceptClient(ServerSocket serverSocket) {
		Socket clientSocket = null;
		try {
			// connecting to the client
			clientSocket = serverSocket.accept();
		} catch (IOException e) {
			System.err.println("Accept failed.");
			// TODO exit here?
			System.exit(1);
		}

		SocialNetworkProtocol snp = new SocialNetworkProtocol(clientSocket);
		Thread client = new Thread(snp);
		client.start();
	}

}
