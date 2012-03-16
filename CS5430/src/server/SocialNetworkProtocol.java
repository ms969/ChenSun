package server;

import java.net.*;
import java.io.*;

import java.security.*;
import crypto.PublicKeyCryptoServer;

public class SocialNetworkProtocol implements Runnable {
	
	private ServerInputProcessor iprocessor;
	
	private Socket clientSocket;
	
	private PrivateKey pk;

	public SocialNetworkProtocol(Socket clientSocket, PrivateKey pk) {
		this.clientSocket = clientSocket;
		this.pk = pk;
	}

	public void main() throws Exception {
		PublicKeyCryptoServer.serverSideAuth(clientSocket.getInputStream(), clientSocket.getOutputStream(), pk);
		// Getting client socket's input and output streams
		BufferedReader in = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		
		iprocessor = new ServerInputProcessor(out, in);
		
		// request input
		
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			iprocessor.processCommand(inputLine);
		}

		// closing the connection
		in.close();
		clientSocket.close();
	}

	public void run(){
		try {
			main();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
