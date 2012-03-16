package crypto;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;

public class PublicKeyCryptoClient {
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
	
	private static boolean debug = false;
	private final static int NONCE_LENGTH = (64/8);
	private final static int BLOWFISH_KEY_LENGTH = 128;
	private final static int BLOWFISH_KEY_LENGTH_BYTES = (BLOWFISH_KEY_LENGTH/8);

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
			System.out.println("Error recreating the public key from static information! Contact an admin.");
			e.printStackTrace();
			System.exit( 1 );
		}
		return pk;
	}
	
	/**
	 * Implements the authentication protocol on the client side
	 * is and os are the inputstream and output stream of the Socket
	 * used to connect to the server
	 */
	public static SecretKey clientSideAuth(InputStream is, OutputStream os, PublicKey serverPubK) {
		
		/*Generate a symmetric session key. 
		 * For Blowfish, the default is 128 bit length*/
		KeyGenerator kg = null;
		try {
			kg = KeyGenerator.getInstance("Blowfish");
		}
		catch (Exception e) {/*algorithm is valid*/} 
		kg.init(BLOWFISH_KEY_LENGTH);
		SecretKey key = kg.generateKey();
		byte[] sendkey = key.getEncoded();
		if (debug) {
			try {
				System.out.println("Encoded Key: " + new String(sendkey, "UTF8"));
			}
			catch (Exception e) {
				//cannot happen, correct enc.
			}
		}
		
		/*Prepare to encrypt a message for the server 
		 * using the server's public key */
		Cipher c = null;
		try {
			c = Cipher.getInstance("RSA");
			c.init(Cipher.ENCRYPT_MODE, serverPubK);
		}
		catch (Exception e) {
			System.out.println("Server's Public Key fed is incorrect");
			return null;
		}
		
		/*Initialize a nonce.
		 *The sec random num gen. will also be used for ivp*/
		
		SecureRandom sr = null; 
		try {
			sr= SecureRandom.getInstance("SHA1PRNG");
		}
		catch (Exception e) {/*Valid algorithm*/}
		byte[] nonce = new byte[NONCE_LENGTH];
		sr.nextBytes(nonce);
		if (debug) {
			System.out.println("First Nonce created on client side: " + new BigInteger(nonce));
		}
		
		/*Construct the first message*/
		byte[] firstmsg = new byte[NONCE_LENGTH + BLOWFISH_KEY_LENGTH_BYTES];
		System.arraycopy(nonce, 0, firstmsg, 0, NONCE_LENGTH);
		System.arraycopy(sendkey, 0, firstmsg, NONCE_LENGTH, BLOWFISH_KEY_LENGTH_BYTES);
		
		byte[] encfirstmsg = null;
		/*Encrypt */
		try {
			encfirstmsg = c.doFinal(firstmsg);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			bos.write(encfirstmsg);
			bos.flush();
		}
		catch (Exception e) {
			System.out.println("Error sending the first message");
			return null;
		}
		if (debug) {
			try {
				System.out.println("First Message: " + new String(encfirstmsg, "UTF8"));//firstmsgbase64);
			}
			catch (Exception e) {/*valid enc*/}
		}
		//Zero out arrays that contained RAW keys
		CryptoUtil.zeroArray(sendkey);
		CryptoUtil.zeroArray(firstmsg);
		
		/*Receive the second message*/
		
		try {
			c = Cipher.getInstance("Blowfish/CFB8/PKCS5Padding");
		}
		catch (Exception e) {/*Everything is valid*/}
		
		//get ivp from prepend of second msg.
		byte[] iv = new byte[8];
			try {
			int bytesRead = is.read(iv);
			while (bytesRead != 8) {
				bytesRead += is.read(iv, bytesRead, 8 - bytesRead);
			}
		}
		catch (Exception e) {
			System.out.println("Error/Timeout while receiving the second message.");
			return null;
		}
		IvParameterSpec ivp = new IvParameterSpec(iv);
		
		//fetch the actual second message
		byte[] secondmsg = new byte[NONCE_LENGTH + NONCE_LENGTH];
		try {
			c.init(Cipher.DECRYPT_MODE, key, ivp);
			CipherInputStream cis = new CipherInputStream(is, c);
			int bytesRead = cis.read(secondmsg);
			while (bytesRead != NONCE_LENGTH + NONCE_LENGTH) {
				System.out.println(bytesRead);
				bytesRead += cis.read(secondmsg, bytesRead, NONCE_LENGTH + NONCE_LENGTH - bytesRead);
			}
		}
		catch (Exception e) {
			System.out.println("Error/Timeout while receiving the second message.");
			return null;
		}
		if (debug) {
			try {
				System.out.println("Second msg received: " + new String(secondmsg, "UTF8"));
			}
			catch (Exception e) {/*Valid enc*/}
		}
		/*Verify that the first nonce is nonce + 1*/
		
		byte[] recvnonce = new byte[NONCE_LENGTH];
		System.arraycopy(secondmsg, 0, recvnonce, 0, NONCE_LENGTH);
		BigInteger firstNonceNum = new BigInteger(nonce);
		firstNonceNum = firstNonceNum.add(BigInteger.ONE);
		byte[] firstNonceNumPlusOne = firstNonceNum.toByteArray();
		byte[] firstNonceNumPlusOneCorrectLen = new byte[NONCE_LENGTH];
		System.arraycopy(firstNonceNumPlusOne, 0, firstNonceNumPlusOneCorrectLen, 0, NONCE_LENGTH);
		if (!Arrays.equals(recvnonce, firstNonceNumPlusOneCorrectLen)) {
			if (debug) {
				System.out.println("Failure");
			}
			return null;
		}
		else {
			/*Calculate second nonce * 2*/
			byte[] secondNonce = new byte[NONCE_LENGTH];
			System.arraycopy(secondmsg, NONCE_LENGTH, secondNonce, 0, NONCE_LENGTH);
			BigInteger secondNonceNum = new BigInteger(secondNonce);
			if (debug) {
				System.out.println("Second nonce received: " + secondNonceNum);
			}
			secondNonceNum = secondNonceNum.shiftLeft(1);
			if (debug) {
				System.out.println("Second nonce * 2: " + secondNonceNum);
			}
			byte[] secondNonceTimesTwo = secondNonceNum.toByteArray();
			byte[] secondNonceTimesTwoCorrectLen = new byte[NONCE_LENGTH];
			System.arraycopy(secondNonceTimesTwo, 0, secondNonceTimesTwoCorrectLen, 0, NONCE_LENGTH);
			byte[] thirdmsg = new byte[NONCE_LENGTH];
			System.arraycopy(secondNonceTimesTwoCorrectLen, 0, thirdmsg, 0, NONCE_LENGTH);
			
			//Create a new ivp for this new msg
			iv = new byte[8];
			sr.nextBytes(iv);
			ivp = new IvParameterSpec(iv);
			
			//prepend iv to the msg
			try {
				os.write(iv);
				c.init(Cipher.ENCRYPT_MODE, key, ivp);
				CipherOutputStream cos = new CipherOutputStream(new NonClosingCipherOutputStream(os), c);
				cos.write(thirdmsg);
				cos.flush();
				cos.close();
			}
			catch (Exception e) {
				System.out.println("Error/Timeout while sending the third message");
				return null;
			}
			if (debug) {
				System.out.println("Success");
			}
			return key;
		}
	}
	
	
}
