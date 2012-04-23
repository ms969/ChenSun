package server;

import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.util.Arrays;
import java.io.*;

import javax.crypto.KeyGenerator;

import shared.ProjectConfig;

import crypto.CryptoUtil;
import crypto.Hash;
import crypto.PasswordBasedEncryption;
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
	
	public static final int INDEX_SALT = 1;
	public static final int INDEX_ITERATIONS = 2;
	public static final int INDEX_ENCKEY = 3;
	public static final int INDEX_CHECKSUM = 4;

	public static void main(String[] args) throws IOException {

		//Initialize the public key
		pubk = PublicKeyCryptoServer.serverPublicKeyRSA();
		if (pubk == null) {
			//public key is bad!
			System.err.println("Error on creating the public key!");
			System.exit( 1 );
		}
		
		getPassword();
		
		ServerSocket serverSocket = initializeSocket();
		
		/*Starts a thread for the DBA to input commands*/
		SocialNetworkDBAThread thread = new SocialNetworkDBAThread();
		Thread t = new Thread(thread);
		t.start();
		
		
		while (true) {
			acceptClient(serverSocket);
		}
		
		
	}
	
	/** Gets a user inputted password and tries to get the
	 * keys from the db
	 */
	private static void getPassword() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		char[] input = new char[22]; //password is at most 20 characters.
		boolean valid = false;
		int numRetries = 5;
		
		Connection conn = DBManager.getConnection();
		String keystring = DatabaseDBA.fetchKeys(conn);
		DBManager.closeConnection(conn);
		
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
				System.out.println("Invalid passphrase. Try again");
				numRetries--;
				continue;
			}
			
			char[] pwd = Arrays.copyOf(input, length - 2); //get rid of the carriage return
			
			//There are two keys.
			for (int i = 0; i < 2; i++) {
				//get the salt and iteration count for the key
				String[] key = keys[i].split(" ");
				int index = Integer.parseInt(key[0]);
				byte[] indexArr = ByteBuffer.allocate(4).putInt(index).array();
				byte[] salt = CryptoUtil.decode(key[INDEX_SALT]);
				int iterations = Integer.parseInt(key[INDEX_ITERATIONS]);
				byte[] iterationsArr = ByteBuffer.allocate(4).putInt(iterations).array();
				byte[] encKey = CryptoUtil.decode(key[INDEX_ENCKEY]);
				byte[] checksum = CryptoUtil.decode(key[INDEX_CHECKSUM]);
				
				byte[] decryptedKey = PasswordBasedEncryption.decrypt(encKey, pwd, salt, iterations);
				if (decryptedKey == null) {
					break;
				}
				
				//checksum is made up of (index, salt, iterations, decryptedKey)
				byte[] toBeChecksummed = new byte[indexArr.length + salt.length + iterationsArr.length + decryptedKey.length];
				System.arraycopy(indexArr, 0, toBeChecksummed, 0, indexArr.length);
				System.arraycopy(salt, 0, toBeChecksummed, indexArr.length, salt.length);
				System.arraycopy(iterationsArr, 0, toBeChecksummed, indexArr.length + salt.length, iterationsArr.length);
				System.arraycopy(decryptedKey, 0, toBeChecksummed, indexArr.length + salt.length + iterationsArr.length, decryptedKey.length);
				
				if (Arrays.equals(checksum, Hash.generateChecksum(toBeChecksummed))) { //it is the correct key
					if (i == 0) { //first key is the private
						privk = PublicKeyCryptoServer.serverPrivateKeyRSA(new BigInteger(decryptedKey));
						if (privk == null) { /*THIS SHOULD NOT HAPPEN*/
							//(checksum was correct, but the key was incorrect!)
							System.out.println("Something really bad happened.");
							Arrays.fill(decryptedKey, (byte)0x00);
							Arrays.fill(toBeChecksummed, (byte)0x00);
							break;
						}
					}
					else {
						if (!SharedKeyCrypto.initSharedKeyCrypto(decryptedKey)) {
							/*THIS SHOULD NOT HAPPEN*/
							System.out.println("Something really bad happened.");
							Arrays.fill(decryptedKey, (byte)0x00);
							Arrays.fill(toBeChecksummed, (byte)0x00);
							break;
						}
						valid = true;
					}
					//these arrays contain the decrypted key.
					Arrays.fill(decryptedKey, (byte)0x00);
					Arrays.fill(toBeChecksummed, (byte)0x00);
				}
				else {
					break;
				}
			}
			numRetries--;
		}
		if (!valid) {
			System.err.println("Too many retries");
			System.err.println("Shutting down the server.");
			System.exit(1);
		}
		else {
			System.out.println("Server successfully started");
		}
	}

	/*
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
	}*/

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
			clientSocket.setSoTimeout(ProjectConfig.SERVER_TIMEOUT);
			SocialNetworkProtocol snp = new SocialNetworkProtocol(clientSocket, privk);
			Thread client = new Thread(snp);
			client.start();
		} catch (IOException e) {
			System.err.println("Error: Client connection failed.");
		}
	}

}
