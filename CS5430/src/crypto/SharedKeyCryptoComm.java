package crypto;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import java.security.*;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

/**
 * @author kchen
 * 
 * Implements shared key communications between the server and the client
 * 
 * Sent message:
 * TODO NONCE
 * TODO we need to handle when the key nonce bundle is null (aka the protocol failed)
 * checksum(nonce + ivp + encmsglen + actualmsg) + nonce + ivp + encryptedmsg's len + encmsg
 *
 */
public class SharedKeyCryptoComm {
	
	public static final String ALG = "Blowfish/CBC/PKCS5Padding";
	
	public static final int MD5CHECKSUMLEN = (128/8); //in bytes
	
	private final static int NONCE_LENGTH = (64/8);
	
	private static SecureRandom createSecureRandom() {
		SecureRandom sr = null;
		try {
			sr = SecureRandom.getInstance("SHA1PRNG");
		}
		catch (NoSuchAlgorithmException e) {/*Valid alg*/}
		return sr;
	}
	
	/** Creates an IV with the given size**/
	private static byte[] createIV(SecureRandom sr, int size) {
		byte[] iv = new byte[size];
		sr.nextBytes(iv);
		return iv;
	}

	/** Reads all possible data into buf.
	 *  Returns false when there is an IOException
	 * **/
	private static boolean readIntoBuffer(InputStream is, byte[] buf) {
		try {
			int bytesRead = is.read(buf);
			while (bytesRead != buf.length) {
				bytesRead += is.read(buf, bytesRead, buf.length - bytesRead);
			}
		}
		catch (IOException ioe){
			return false;
		}
		return true;
	}
	
	public static Cipher createCipher(String alg) {
		Cipher c = null;
		try {
			c = Cipher.getInstance(ALG);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return c;
	}
	
	public static boolean send(byte[] msgbytes, OutputStream os, Cipher c, SecretKey sk, 
			BigInteger sendNonce) {
		int blockSize = c.getBlockSize();
		
		SecureRandom sr = createSecureRandom();
		try {
			//iv and msg
			byte[] iv = createIV(sr, blockSize);
			IvParameterSpec ivp = new IvParameterSpec(iv);
			try {
				c.init(Cipher.ENCRYPT_MODE, sk, ivp);
			}
			catch (Exception e) {/*This cannot happen*/}
			
			byte[] encmsg = null;
			try {
				encmsg = c.doFinal(msgbytes);
			} catch (Exception e) { //exception should not happen...
				e.printStackTrace();
			}
			byte[] encmsglen = ByteBuffer.allocate(4).putInt(encmsg.length).array();
			
			byte[] sendNonceArray = sendNonce.toByteArray();
			byte[] sendNonceArrayMsg = Arrays.copyOf(sendNonceArray, NONCE_LENGTH);
			
			byte[] totalmsg = new byte[NONCE_LENGTH + iv.length + encmsglen.length + msgbytes.length];
			//nonce, iv, msglen, and msgbytes.
			System.arraycopy(sendNonceArrayMsg, 0, totalmsg, 0, NONCE_LENGTH);
			System.arraycopy(iv, 0, totalmsg, NONCE_LENGTH, iv.length);
			System.arraycopy(encmsglen, 0, totalmsg, NONCE_LENGTH + iv.length, encmsglen.length);
			System.arraycopy(msgbytes, 0, totalmsg, NONCE_LENGTH + iv.length + encmsglen.length, msgbytes.length);
			
			//get checksum
			byte[] checksum = Hash.generateChecksum(totalmsg);
			
			// zero out totalmsg.
			Arrays.fill(totalmsg, (byte)0x00);

			os.write(checksum); //128 bits
			os.write(sendNonceArrayMsg);
			os.write(iv);
			os.write(encmsglen);
			os.write(encmsg);
			os.flush();
		}
		catch (IOException e) {
			System.out.println("Error/Timeout sending the message (msg in bytes so it is not printed) ");
			return false;
		}
		return true;
	}
	
	public static boolean send(String msg, OutputStream os, Cipher c, SecretKey sk, 
			BigInteger sendNonce) {
		try {
			byte[] msgbytes = msg.getBytes("UTF8"); //the exception won't happen
			return send(msgbytes, os, c , sk, sendNonce);
		}
		catch (Exception e) {/*Should not happen*/}
		return false;
	}
	
	/**
	 * RETURNS NULL IF CHECKSUM CHECK FAILS!!!
	 */
	public static byte[] receiveBytes(InputStream is, Cipher c, SecretKey sk, 
			BigInteger recvNonce) {
		int blockSize = c.getBlockSize();
		byte[] checksum = new byte[MD5CHECKSUMLEN]; //MD5
		byte[] recvnonce = new byte [NONCE_LENGTH];
		byte[] expctnonce = Arrays.copyOf(recvNonce.toByteArray(), NONCE_LENGTH);
		byte[] iv = new byte[blockSize];
		byte[] size = new byte[4]; //int

		//first fetch the checksum
		if (!readIntoBuffer(is, checksum)) {
			System.out.println("Error/Timeout receiving the message. (checksum)");
			return null;
		}
		
		if (!readIntoBuffer(is, recvnonce)) {
			System.out.println("Error/Timeout receiving the message. (recvnonce)");
			return null;
		}

		//fetch iv
		if (!readIntoBuffer(is, iv)) {
			System.out.println("Error/Timeout receiving the message. (iv)");
			return null;
		}

		//fetch size of enc msg
		if (!readIntoBuffer(is, size)) {
			System.out.println("Error/Timeout receiving the message. (encmsglen)");
			return null;
		}

		int encmsglen = ByteBuffer.wrap(size).getInt();

		byte[] encmsg = new byte[encmsglen];

		//read the actual message in
		if (!readIntoBuffer(is, encmsg)) {
			System.out.println("Error/Timeout receiving the message. (encmsg)");
			return null;
		}
		
		IvParameterSpec ivp = new IvParameterSpec(iv);
		try {
			c.init(Cipher.DECRYPT_MODE, sk, ivp);
		}
		catch (Exception e) {/*cannot happen*/}

		byte[] msgbytes = null;
		String msg = null;
		try {
			msgbytes = c.doFinal(encmsg);
			//msg = new String(msgbytes, "UTF8");
		} catch (Exception e) {
			e.printStackTrace(); //this should not happen
		} 
		
		//generate checksum of received msg.
		byte[] wholeMessage = new byte[NONCE_LENGTH + iv.length + size.length + msgbytes.length];
		System.arraycopy(recvnonce, 0, wholeMessage, 0, NONCE_LENGTH);
		System.arraycopy(iv, 0, wholeMessage, NONCE_LENGTH, iv.length);
		System.arraycopy(size, 0, wholeMessage, NONCE_LENGTH + iv.length, size.length);
		System.arraycopy(msgbytes, 0, wholeMessage, NONCE_LENGTH + iv.length + size.length, msgbytes.length);
		
		//compare the checksum received to the generated checksum.
		if (Arrays.equals(checksum, Hash.generateChecksum(wholeMessage)) && 
				Arrays.equals(recvnonce, expctnonce)) {
			// zero out the wholeMessage array
			Arrays.fill(wholeMessage, (byte)0x00);
			return msgbytes;
		}
		if (!Arrays.equals(checksum, Hash.generateChecksum(wholeMessage))) {
				System.out.println("Generated checksum for message does not equal the received checksum!");
		}
		else {
			System.out.println("Received nonce for the message does not equal the expected nonce!");
		}
		return null; //returns null on checksum mismatch
	}
	
	public static String receiveString(InputStream is, Cipher c, SecretKey sk, BigInteger recvNonce) {
		try {
			return new String(receiveBytes(is, c, sk, recvNonce), "UTF8");
		} catch (Exception e) {/*Should not happen*/}
		return null;
	}
}
