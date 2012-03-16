package server;

import java.net.*;
import java.io.*;

import java.security.*;
import javax.crypto.*;
import crypto.PublicKeyCryptoServer;
import crypto.SharedKeyCryptoComm;

public class SocialNetworkProtocol implements Runnable {
	
	private ServerInputProcessor iprocessor;
	
	private Socket clientSocket;
	
	private PrivateKey pk;

	public SocialNetworkProtocol(Socket clientSocket, PrivateKey pk) {
		this.clientSocket = clientSocket;
		this.pk = pk;
	}

	public void main() throws Exception {
		SecretKey sk = PublicKeyCryptoServer.serverSideAuth(clientSocket.getInputStream(), clientSocket.getOutputStream(), pk);
		Cipher c = SharedKeyCryptoComm.createCipher(SharedKeyCryptoComm.ALG);
		// Getting client socket's input and output streams
		BufferedReader in = new BufferedReader(new InputStreamReader(new CipherInputStream(
				clientSocket.getInputStream(), c)));
		
		iprocessor = new ServerInputProcessor(clientSocket.getOutputStream(), in, 
				clientSocket.getInputStream(), c, sk);
		
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
