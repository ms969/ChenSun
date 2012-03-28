package crypto;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import shared.ProjectConfig;

public class PublicKeyCryptoServer {
	/* In the server and client code, the server's Public Key
	 * is on full display. */
	private static BigInteger serverPKModRSA = new BigInteger(
			"91862985209" +
			"9138196651465564206187600169396202311021834" +
			"3940927022763980199346768929236739052634112" +
			"5687064666476687791422782414595977459878495" +
			"8566088392914282011818638994118931178203799" +
			"7340749472669868059841641093244450667873475" +
			"2579861907700960359679733907549544647570992" +
			"483255857586126066156178747843234758357");
	private static BigInteger serverPKExpRSA = new BigInteger("65537");
	/*Nonce length is in bytes*/
	private final static int NONCE_LENGTH = (64/8);
	private final static int BLOWFISH_KEY_LENGTH = 128;
	private final static int BLOWFISH_KEY_LENGTH_BYTES = (BLOWFISH_KEY_LENGTH/8);
	private final static int RSA_KEY_LENGTH = 1024;
	private final static int RSA_BLOCK_LENGTH = (((RSA_KEY_LENGTH)/8));
	
	public static final boolean DEBUG = ProjectConfig.DEBUG;
	
	/** Returns the server's public key
	 * from static info.
	 * Null if the info is somehow corrupted.
	 */
	public static PublicKey serverPublicKeyRSA() {
		RSAPublicKeySpec pks = new RSAPublicKeySpec(serverPKModRSA, serverPKExpRSA);
		PublicKey pk = null;
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			pk = kf.generatePublic(pks);
		}
		catch (Exception e) {
			System.out.println("Error recreating the public key from static information!");
			e.printStackTrace();
			System.exit( 1 );
		}
		return pk;
	}
	
	/** Returns the server's private key
	 * from user input.
	 * Null if the param is invalid, or if
	 * the PKExp static info is somehow corrupted.
	 */
	public static PrivateKey serverPrivateKeyRSA(BigInteger privExp) throws NoSuchAlgorithmException, InvalidKeySpecException {
		RSAPrivateKeySpec pks = new RSAPrivateKeySpec(serverPKModRSA, privExp);
		PrivateKey pk = null;
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");
			pk = kf.generatePrivate(pks);
		}
		catch (NoSuchAlgorithmException e) { }
		catch (InvalidKeySpecException ikse) {
			System.out.println("Error recreating the private key from given secret.");
			System.out.println("Please try inputting again");
		}
		return pk;
	}
	
	/**Checks that a message comes out perfect when encrypted
	 * using the given public key and decrypted with the
	 * given private key.
	 */
	public static boolean authenticatePrivateKeyRSA(PublicKey pub, PrivateKey priv) {
		String testingString = "Hello World!";
		String txt = "";
		try {
			Cipher rsac = Cipher.getInstance("RSA");
			//encrypt
			rsac.init(Cipher.ENCRYPT_MODE, pub);
			byte[] ciphertxt = rsac.doFinal(testingString.getBytes("UTF8"));
			if (DEBUG) {
				System.out.println(new String(ciphertxt, "UTF8"));
			}
			//decrypt
			rsac.init(Cipher.DECRYPT_MODE,priv);
			txt = new String(rsac.doFinal(ciphertxt), "UTF8");
			if (DEBUG) {
				System.out.println("\n" + txt);
			}
		}
		catch (Exception e) {
			//Most of the errors will never happen. RSA is valid, UTF8 is valid...
			//However, an InvalidKeyError, which could happen should return false...
			//And any of the other errors could be caused by inappropriate args
			return false;
		}
		
		return (txt.equals(testingString));
	}
	
	/**
	 * Implements the authentication protocol on the server side
	 * is and os are the inputstream and outputstream of the Socket
	 * used to connect to the client.
	 */
	public static SecretKey serverSideAuth(InputStream is, OutputStream os, PrivateKey serverPrivK) {
		/*Initialize a cipher to accept the incoming message first message*/
		Cipher c = null;
		try {
			c = Cipher.getInstance("RSA");
			c.init(Cipher.DECRYPT_MODE, serverPrivK);

		}
		catch (Exception e) {
			System.out.println("Server's private key was not initialized correctly.");
			e.printStackTrace();
			System.exit( 1 );
		}
		
		/*Receive the first message*/
		/*Because RSA is DUMB WITH CIPHER STREAMS, doing it old school*/
		BufferedInputStream bis = new BufferedInputStream(is);
		byte[] encfirstmsg = new byte[RSA_BLOCK_LENGTH];
		byte[] firstmsg = null;
		if (DEBUG) {
			System.out.println("About to read the first msg");
		}
		try {
			int bytesRead = bis.read(encfirstmsg);
			while (bytesRead != RSA_BLOCK_LENGTH) {
				bytesRead += bis.read(encfirstmsg, bytesRead, RSA_BLOCK_LENGTH - bytesRead);
			}
			firstmsg = c.doFinal(encfirstmsg);
			if (DEBUG) {
				System.out.println("Server received first message: " + 
						new String(firstmsg, "UTF8"));
			}
		}
		catch (Exception e) {
			System.out.println("Error/Timeout while receiving the first message from the client.");
			return null;
		}
		
		//get the first nonce, and add 1
		byte[] recvNonce = new byte[NONCE_LENGTH];
		System.arraycopy(firstmsg, 0, recvNonce, 0, NONCE_LENGTH);
		BigInteger firstNonceNum = new BigInteger(recvNonce);
		if (DEBUG) {
			System.out.println("First nonce recv: " + firstNonceNum);
		}
		firstNonceNum = firstNonceNum.add(BigInteger.ONE);
		if (DEBUG) {
			System.out.println("First nonce recv + 1: " + firstNonceNum);
		}
		byte[] firstNonceNumPlusOne = firstNonceNum.toByteArray();
		byte[] firstNonceNumPlusOneCorrectLen = new byte[8];
		System.arraycopy(firstNonceNumPlusOne, 0, firstNonceNumPlusOneCorrectLen, 0, NONCE_LENGTH);
		
		//extract the shared key
		byte[] recvkey = new byte[BLOWFISH_KEY_LENGTH_BYTES];
		System.arraycopy(firstmsg, NONCE_LENGTH, recvkey, 0, BLOWFISH_KEY_LENGTH_BYTES);
		SecretKey key = (SecretKey) new SecretKeySpec(recvkey, 0, BLOWFISH_KEY_LENGTH_BYTES, "Blowfish");
		if (DEBUG) {
			try {
				System.out.println("Key received: " + new String(key.getEncoded(), "UTF8"));
			}
			catch (Exception e) {
				//cannot happen, encoding is real.
			}
		}
		/*Zero out arrays that contained the key in RAW*/
		Arrays.fill(recvkey, (byte) 0x00);
		Arrays.fill(firstmsg, (byte) 0x00);;
		
		//create a second nonce to authenticate the client
		SecureRandom sr = null; 
		try {
			sr = SecureRandom.getInstance("SHA1PRNG");
		}
		catch (Exception e) {
			//cannot happen, valid alg.
		}
		byte[] sendNonce = new byte[NONCE_LENGTH];
		sr.nextBytes(sendNonce);
		if (DEBUG) {
			System.out.println("Second nonce created: " + new BigInteger(sendNonce));
		}
		//create an ivp
		byte[] iv = new byte[8];
		sr.nextBytes(iv);
		IvParameterSpec ivp = new IvParameterSpec(iv);
		
		/*Construct the second message*/
		//prepend the iv for the decrypter to know.
		byte[] secondmsg = new byte[NONCE_LENGTH + NONCE_LENGTH];
		try {
			os.write(iv);
			c = Cipher.getInstance("Blowfish/CFB8/PKCS5Padding");
			c.init(Cipher.ENCRYPT_MODE, key, ivp);
			System.arraycopy(firstNonceNumPlusOneCorrectLen, 0, secondmsg, 0, NONCE_LENGTH);
			System.arraycopy(sendNonce, 0, secondmsg, NONCE_LENGTH, NONCE_LENGTH);
			CipherOutputStream cos = new CipherOutputStream(new NonClosingCipherOutputStream(os), c);
			cos.write(secondmsg);
			cos.flush();
			cos.close();
		}
		catch (Exception e) {
			//all padding, alg are correct. Only possible error is io
			System.out.println("Error sending the second message");
			return null;
		}
		if (DEBUG) {
			try {
				System.out.println("Server sent the second message: " + new String(secondmsg, "UTF8"));
			}
			catch (Exception e) {
				//cannot happen, valid enc
			}
		}
		//get the new ivp for the third message
		iv = new byte[8];
		try {
			int bytesRead = is.read(iv);
			while (bytesRead != 8) {
				bytesRead += is.read(iv, bytesRead, 8 - bytesRead);
			}
		}
		catch (IOException ioe) {
			System.out.println("Error/Timeout while getting the third message.");
			return null;
		}
		ivp = new IvParameterSpec(iv);
		
		/*Receive the third message, check that nonce = nonce*2*/
		byte[] thirdmsg = new byte[NONCE_LENGTH];
		try {
			c.init(Cipher.DECRYPT_MODE, key, ivp);
			CipherInputStream cis = new CipherInputStream(is, c);
			int bytesRead = cis.read(thirdmsg);
			while (bytesRead != NONCE_LENGTH) {
				bytesRead += cis.read(thirdmsg, bytesRead, NONCE_LENGTH - bytesRead);
			}
		}
		catch (Exception e) {
			System.out.println("Error/Timeout while getting the third message.");
			return null;
			
		}
		BigInteger secondNonceNum = new BigInteger(sendNonce);
		secondNonceNum = secondNonceNum.shiftLeft(1);
		byte[] secondNonceTimesTwo = secondNonceNum.toByteArray();
		byte[] secondNonceTimesTwoCorrectLen = new byte[NONCE_LENGTH];
		System.arraycopy(secondNonceTimesTwo, 0, secondNonceTimesTwoCorrectLen, 0, NONCE_LENGTH);
		if (Arrays.equals(thirdmsg, secondNonceTimesTwoCorrectLen)) {
			if (DEBUG) {
				System.out.println("Success");
			}
			return key;
		}
		else {
			if (DEBUG) {
				System.out.println("Failure");
			}
			return null;
		}
	}
	
	/*
	private static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator pkf = KeyPairGenerator.getInstance("RSA");
		return pkf.generateKeyPair(); 
	}*/
}
