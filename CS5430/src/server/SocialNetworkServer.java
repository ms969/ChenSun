package server;

import java.math.BigInteger;
import java.net.*;
import java.security.*;
import java.io.*;

import javax.crypto.KeyGenerator;

import crypto.PublicKeyCryptoServer;
import crypto.SharedKeyCrypto;

/* Setup code and Connection code courtesy of 
 * tutorials in docs.oracle.com
 */

//TODO should we merely just exit if the server finds an error?

public class SocialNetworkServer {
	private static final int LISTEN_PORT_NUM = 5329;
	private static PrivateKey privk = null;
	private static PublicKey pubk = null;
	public static final boolean DEBUG = true;

	public static void main(String[] args) throws IOException {

		//Initialize the public key
		pubk = PublicKeyCryptoServer.serverPublicKeyRSA();
		if (pubk == null) {
			//public key is bad!
			System.exit( 1 );
		}
		
		//Get the private key from the operator
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Input the first secret");
		System.out.print(">> ");
		BigInteger bi;
		int numRetries = 5;
		while (privk == null && numRetries > 0) {
			try {
				bi = new BigInteger(br.readLine());
				privk = PublicKeyCryptoServer.serverPrivateKeyRSA(bi);
				if (!PublicKeyCryptoServer.authenticatePrivateKeyRSA(pubk, privk)) {
					privk = null;
				}
			}
			catch (Exception e) {
				
			}
			if (privk == null) {
				System.out.println("Incorrect. Please try again");
				System.out.print(">> ");
				numRetries--;
			}
		}
		if (numRetries == 0) {
			System.out.println("Too many incorrect tries. Exiting.");
			System.exit( 1 );
		}
		
		System.out.println("Server successfully started");
		
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
			// TODO exit here?
			System.exit(1);
		}
		SocialNetworkProtocol snp = new SocialNetworkProtocol(clientSocket, privk);
		Thread client = new Thread(snp);
		client.start();
	}

}
