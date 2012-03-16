package crypto;

import java.io.*;

import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

/**
 * @author kchen
 * 
 * Implements shared key communications between the server and the client
 * 
 * Sent message:
 * ivp_1 + {msg}k_CS
 *
 */
public class SharedKeyCryptoComm {
	
	public static String ALG = "Blowfish/CBC/PKCS5Padding";
	
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
	
	/** Reads an IV with the given size
	 *  Returns null when there is an IOException
	 * **/
	private static boolean readIV(InputStream is, byte[] iv) {
		try {
			int bytesRead = is.read(iv);
			while (bytesRead != iv.length) {
				bytesRead += is.read(iv, bytesRead, iv.length - bytesRead);
			}
		}
		catch (IOException ioe){
			return false;
		}
		return true;
	}
	
	private static Cipher createCipher(String alg) {
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
	
	public static boolean send(String msg, OutputStream os, SecretKey sk) throws InvalidKeyException, InvalidAlgorithmParameterException {
		Cipher c = createCipher(ALG); //valid alg.
		int blockSize = c.getBlockSize();
		
		SecureRandom sr = createSecureRandom();
		
		CipherOutputStream cos = new CipherOutputStream(new NonClosingCipherOutputStream(os), c);
		PrintWriter pw = new PrintWriter(cos);
		try {
			//iv and msg
			byte[] iv = createIV(sr, blockSize);
			System.out.println(new String(iv, "UTF8"));
			IvParameterSpec ivp = new IvParameterSpec(iv);
			c.init(Cipher.ENCRYPT_MODE, sk, ivp);
			os.write(iv);
			pw.println(msg);
			pw.flush();
			pw.close();
			cos.close();
		}
		catch (IOException e) {
			System.out.println("Error/Timeout sending the message: " + msg);
			return false;
		}
		return true;
	}
	
	public static String receive(InputStream is, SecretKey sk) throws InvalidKeyException, InvalidAlgorithmParameterException {
		Cipher c = createCipher(ALG); //valid alg.
		int blockSize = c.getBlockSize();
		byte[] iv = new byte[blockSize];
		
		CipherInputStream cis = new CipherInputStream(is, c);
		BufferedReader br = new BufferedReader(new InputStreamReader(cis));
		try {
			//fetch iv
			if (!readIV(is, iv)) {
				System.out.println("Error/Timeout receiving the message.");
				return null;
			}
			System.out.println(new String(iv, "UTF8"));
			IvParameterSpec ivp = new IvParameterSpec(iv);
			c.init(Cipher.DECRYPT_MODE, sk, ivp);

			//get msg
			return br.readLine();
		}
		catch (IOException ioe) {
			System.out.println("Error/Timeout receiving the message.");
			return null;
		}
	}
}
