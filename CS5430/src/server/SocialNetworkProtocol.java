package server;

import java.net.*;
import java.io.*;

import java.security.*;
import javax.crypto.*;

import comm.CommManager;

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
		
		iprocessor = new ServerInputProcessor(clientSocket.getOutputStream(),
				clientSocket.getInputStream(), c, sk);
		
		// request input
		
		
		String inputLine;
		while ((inputLine = CommManager.receive(clientSocket.getInputStream(), c, sk)) != null) {
			System.out.println(inputLine);
			iprocessor.processCommand(inputLine);
		}

		// closing the connection
		clientSocket.close();
	}

	public void run(){
		try {
			main();
		} catch (Exception e) {
			// TODO What to do with the exceptions?
			e.printStackTrace();
		}
	}

}
