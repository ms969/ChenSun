package crypto;

import java.io.*;
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
 * checksum(ivp + encmsglen + actualmsg) + ivp + encmsglen + encmsg
 *
 */
public class SharedKeyCryptoComm {
	
	public static String ALG = "Blowfish/CBC/PKCS5Padding";
	
	public static int MD5CHECKSUMLEN = (128/8); //in bytes
	
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
	
	public static boolean send(String msg, OutputStream os, Cipher c, SecretKey sk) {
		//System.out.println(msg);
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

			byte[] msgbytes = msg.getBytes("UTF8");
			byte[] encmsg = null;
			try {
				encmsg = c.doFinal(msgbytes);
			} catch (Exception e) { //exception should not happen...
				e.printStackTrace();
			}
			byte[] encmsglen = ByteBuffer.allocate(4).putInt(encmsg.length).array();
			
			byte[] totalmsg = new byte[iv.length + encmsglen.length + msgbytes.length];
			//iv, msglen, and msgbytes.
			System.arraycopy(iv, 0, totalmsg, 0, iv.length);
			System.arraycopy(encmsglen, 0, totalmsg, iv.length, encmsglen.length);
			System.arraycopy(msgbytes, 0, totalmsg, iv.length + encmsglen.length, msgbytes.length);
			
			//get checksum
			byte[] checksum = Hash.generateChecksum(totalmsg);

			os.write(checksum); //128 bits
			os.write(iv);
			os.write(encmsglen);
			os.write(encmsg);
			os.flush();
		}
		catch (IOException e) {
			System.out.println("Error/Timeout sending the message: " + msg);
			return false;
		}
		return true;
	}
	
	/**
	 * RETURNS NULL IF CHECKSUM CHECK FAILS!!!
	 */
	public static String receive(InputStream is, Cipher c, SecretKey sk) {
		int blockSize = c.getBlockSize();
		byte[] checksum = new byte[MD5CHECKSUMLEN]; //MD5
		byte[] iv = new byte[blockSize];
		byte[] size = new byte[4]; //int

		//first fetch the checksum
		if (!readIntoBuffer(is, checksum)) {
			System.out.println("Error/Timeout receiving the message. (checksum)");
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
			msg = new String(msgbytes, "UTF8");
		} catch (Exception e) {
			e.printStackTrace(); //this should not happen
		} 
		
		//generate checksum of received msg.
		byte[] wholeMessage = new byte[iv.length + size.length + msgbytes.length];
		System.arraycopy(iv, 0, wholeMessage, 0, iv.length);
		System.arraycopy(encmsglen, 0, wholeMessage, iv.length, size.length);
		System.arraycopy(msgbytes, 0, wholeMessage, iv.length + size.length, msgbytes.length);
		
		//compare the checksum received to the generated checksum.
		if (Arrays.equals(checksum, Hash.generateChecksum(wholeMessage))) {
			System.out.println("Generated checksum for message does not equal the received checksum!");
			return msg;
		}
		return null; //returns null on checksum mismatch
	}
}
