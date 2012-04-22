package server;

import java.math.BigInteger;
import java.net.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.util.Arrays;
import java.io.*;

import javax.crypto.KeyGenerator;

import shared.ProjectConfig;

import crypto.PublicKeyCryptoServer;
import crypto.SharedKeyCrypto;
import database.DBManager;
import database.DatabaseDBA;

/* Setup code and Connection code courtesy of 
 * tutorials in docs.oracle.com
 */

//TODO should we merely just exit if the server finds an error?

public class SocialNetworkServer {
	private static final int LISTEN_PORT_NUM = ProjectConfig.SERVER_CLIENT_CONN_PORT;
	private static PrivateKey privk = null;
	private static PublicKey pubk = null;
	private static final boolean DEBUG = ProjectConfig.DEBUG;

	public static void main(String[] args) throws IOException {

		//Initialize the public key
		pubk = PublicKeyCryptoServer.serverPublicKeyRSA();
		if (pubk == null) {
			//public key is bad!
			System.exit( 1 );
		}
		
		if (DEBUG && args.length == 2) {
			try {
				privk = PublicKeyCryptoServer.serverPrivateKeyRSA(new BigInteger(args[0]));
			} catch (Exception e) { /* Can't happen */ }
			SharedKeyCrypto.initSharedKeyCrypto(args[1]);
		} else {
			getSecrets();
		}
		
		System.out.println("Server successfully started");
		
		ServerSocket serverSocket = initializeSocket();
		
		/*Starts a thread for the DBA to input commands*/
		SocialNetworkDBAThread thread = new SocialNetworkDBAThread();
		Thread t = new Thread(thread);
		t.start();
		
		
		while (true) {
			acceptClient(serverSocket);
		}
		
		
	}
	
	private static void getPassword() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		char[] input = new char[21]; //password is at most 20 characters.
		boolean valid = false;
		int numRetries = 5;
		
		Connection conn = DBManager.getConnection();
		String keystring = DatabaseDBA.fetchKeys(conn);
		
		if (keystring == null) {
			System.err.println("Could not fetch initialization string from Database");
			System.err.println("Make sure the database is configured correctly.");
			System.exit(1);
		}
		String[] keys = keystring.split(";");
		
		System.out.println("Input passphrase");
		
		while(!valid && numRetries > 0) {
			System.out.print(">> ");
			int length = br.read(input);

			if (br.ready()) { //there is leftover data.
				//TODO leftover \n triggers ready to be true?
				System.out.println("Invalid passphrase. Try again");
				numRetries--;
				continue;
			}
			
			char[] pwd = Arrays.copyOf(input, length - 1); //get rid of the \n
			
			//get the salt and iteration count from the first key.
			String[] firstKey = keys[0].split(" ");
			
			
			/*Create the PBEKey from this pwd*/
		}
	}

	private static void getSecrets() throws IOException {
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
			if (privk == null && numRetries != 1) {
				System.out.println("Incorrect. Please try again");
				System.out.print(">> ");
			}
			numRetries--;
		}
		if (numRetries == 0) {
			System.out.println("Too many incorrect tries. Exiting.");
			System.exit( 1 );
		}
		
		System.out.println("Input the second secret");
		System.out.print(">> ");
		numRetries = 5;
		boolean valid = false;
		while (!valid && numRetries > 0) {
			valid = SharedKeyCrypto.initSharedKeyCrypto(br.readLine());
			if (!valid && numRetries != 1) {
				System.out.println("Incorrect. Please try again");
				System.out.print(">> ");
			}
			numRetries--;
		}
		if (numRetries == 0) {
			System.out.println("Too many incorrect tries. Exiting.");
			System.exit( 1 );
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
			System.exit(1);
		}
		return serverSocket;
	}

	private static void acceptClient(ServerSocket serverSocket) {
		Socket clientSocket = null;
		try {
			// connecting to the client
			clientSocket = serverSocket.accept();
			clientSocket.setSoTimeout(ProjectConfig.TIMEOUT);
			SocialNetworkProtocol snp = new SocialNetworkProtocol(clientSocket, privk);
			Thread client = new Thread(snp);
			client.start();
		} catch (IOException e) {
			System.err.println("Error: Client connection failed.");
		}
	}

}
