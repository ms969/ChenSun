package server;

import java.math.BigInteger;
import java.net.*;
import java.io.*;

import java.security.*;
import javax.crypto.*;

import shared.ConnectionException;

import comm.CommManager;

import crypto.KeyNonceBundle;
import crypto.PublicKeyCryptoServer;
import crypto.SharedKeyCryptoComm;

public class SocialNetworkProtocol implements Runnable {
	
	private ServerInputProcessor iprocessor;
	
	private Socket clientSocket;
	
	private PrivateKey pk;
	public BigInteger sendNonce = null;
	public BigInteger recvNonce = null;

	public SocialNetworkProtocol(Socket clientSocket, PrivateKey pk) {
		this.clientSocket = clientSocket;
		this.pk = pk;
	}

	public void main() throws IOException, ConnectionException {
		KeyNonceBundle knb = PublicKeyCryptoServer.serverSideAuth(clientSocket.getInputStream(), clientSocket.getOutputStream(), pk);
		if (knb == null) {
			System.out.println("Authentication Protocol Failed - Some client was unresponsive / malicious");
			//thread must exit.
		}
		else {
			Cipher c = SharedKeyCryptoComm.createCipher(SharedKeyCryptoComm.ALG);
			// Getting client socket's input and output streams
			
			iprocessor = new ServerInputProcessor(clientSocket.getOutputStream(),
					clientSocket.getInputStream(), c, knb.getSk(), knb.getSendNonce(), knb.getRecvNonce());
			
			// request input
			
			
			String inputLine;
			while ((inputLine = CommManager.receive(clientSocket.getInputStream(), c, knb.getSk(), knb.getRecvNonce())) != null) {
				System.out.println(inputLine);
				iprocessor.processCommand(inputLine);
			}
	
			// closing the connection
			clientSocket.close();
		}
	}

	public void run(){
		try {
			main();
		} catch (IOException e) {
			if (e instanceof SocketException) {
				System.err.println("Network Error. Closing the socket connection.");
			} else {
				System.err.println("Error: Cannot find CreateDB SQL file.");
			}
			try {
				clientSocket.close();
			} catch (IOException e1) {	}
		} catch (ConnectionException e) {
			System.err.println("Error: closing connection.");
			try {
				clientSocket.close();
			} catch (IOException e1) {	}
		}
	}

}
