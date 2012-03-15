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

public class PublicKeyCrypto {
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
	/* TODO must remove this.*/
	private static BigInteger serverPrivKPrivExpRSA = new BigInteger(
			"35945105707" +
			"0514364632653049857385519916138917131755251" +
			"5556420534534072481682843926657413922909946" +
			"7890185487695930264175135148693093153197798" +
			"9188836393913191079379415982728565479044105" +
			"5281056962297542468442548591992047168685821" +
			"8767521363784780358713936281232600468990679" +
			"159303006096606946533721779530214714753");
	/*Nonce length is in bytes*/
	private final static int NONCE_LENGTH = (64/8);
	/*DES Key length is in bytes*/
	private final static int DES_KEY_LENGTH = 56;
	private final static int DES_KEY_LENGTH_BYTES = (64/8);
	
	

	public static boolean debug = true;
	
	private static PublicKey serverPublicKeyRSA() throws NoSuchAlgorithmException, InvalidKeySpecException {
		RSAPublicKeySpec pks = new RSAPublicKeySpec(serverPKModRSA, serverPKExpRSA);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(pks);
	}
	
	/**TODO Error handling for this function, because
	 * the user may enter an invalid key
	 */
	private static PrivateKey serverPrivateKeyRSA(BigInteger privExp) throws NoSuchAlgorithmException, InvalidKeySpecException {
		RSAPrivateKeySpec pks = new RSAPrivateKeySpec(serverPKModRSA, privExp);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(pks);
	}
	
	/**Checks that a message comes out perfect when encrypted
	 * using the given public key and decrypted with the
	 * given private key.
	 */
	public static boolean authenticatePrivateKeyRSA(PublicKey pub, PrivateKey priv) throws NoSuchAlgorithmException, NoSuchPaddingException, 
	InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		String testingString = "Hello World!";
		
		Cipher rsac = Cipher.getInstance("RSA");
		//encrypt
		rsac.init(Cipher.ENCRYPT_MODE, pub);
		byte[] ciphertxt = rsac.doFinal(testingString.getBytes("UTF8"));
		if (debug) {
			System.out.println(new String(ciphertxt, "UTF8"));
		}
		//decrypt
		rsac.init(Cipher.DECRYPT_MODE,priv);
		String txt = new String(rsac.doFinal(ciphertxt), "UTF8");
		if (debug) {
			System.out.println("\n" + txt);
		}
		
		return (txt.equals(testingString));
	}
	
	/**
	 * Implements the authentication protocol on the server side
	 * is and os are the inputstream and outputstream of the Socket
	 * used to connect to the client.
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws InterruptedException 
	 */
	public static SecretKey serverSideAuth(InputStream is, OutputStream os) throws NoSuchAlgorithmException, NoSuchPaddingException, 
	InvalidKeySpecException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
		/*Initialize a cipher to accept the incoming message first message*/
		Cipher c = null;
		//try {
			c = Cipher.getInstance("RSA");
			//TODO for now. The server initializes this on startup
			PrivateKey serverPrivK = serverPrivateKeyRSA(serverPrivKPrivExpRSA);
			c.init(Cipher.DECRYPT_MODE, serverPrivK);
		//}
		/*
		catch (Exception e) {
			System.out.println("Server's private key was not initialized correctly.");
			e.printStackTrace();
			System.exit( 1 );
		}*/
		
		CipherInputStream cis = new CipherInputStream(is, c);
		//BufferedReader br = new BufferedReader(new InputStreamReader(is));
		byte[] firstmsg = new byte[NONCE_LENGTH + DES_KEY_LENGTH_BYTES];
		System.out.println("About to read the first msg");

		int bytesRead = cis.read(firstmsg);
		while (bytesRead != NONCE_LENGTH + DES_KEY_LENGTH_BYTES) {
			bytesRead += cis.read(firstmsg, bytesRead, NONCE_LENGTH + DES_KEY_LENGTH_BYTES - bytesRead);
		}
		//String msg = br.readLine();
		//System.out.println(msg);
		
		System.out.println("Server received first message: " + 
				new String(firstmsg, "UTF8"));
		
		//get the first nonce, and add 1
		byte[] recvNonce = new byte[NONCE_LENGTH];
		System.arraycopy(firstmsg, 0, recvNonce, 0, NONCE_LENGTH);
		BigInteger firstNonceNum = new BigInteger(recvNonce);
		System.out.println("First nonce recv: " + firstNonceNum);
		firstNonceNum = firstNonceNum.add(BigInteger.ONE);
		System.out.println("First nonce recv + 1: " + firstNonceNum);
		byte[] firstNonceNumPlusOne = firstNonceNum.toByteArray();
		byte[] firstNonceNumPlusOneCorrectLen = new byte[8];
		System.arraycopy(firstNonceNumPlusOne, 0, firstNonceNumPlusOneCorrectLen, 0, NONCE_LENGTH);
		
		byte[] recvkey = new byte[DES_KEY_LENGTH_BYTES];
		System.arraycopy(firstmsg, NONCE_LENGTH, recvkey, 0, DES_KEY_LENGTH_BYTES);
		SecretKey key = (SecretKey) new SecretKeySpec(recvkey, 0, DES_KEY_LENGTH_BYTES, "DES");
		System.out.println("Key received: " + new String(key.getEncoded(), "UTF8"));
		
		//create a second nonce to authenticate the client
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] sendNonce = new byte[NONCE_LENGTH];
		sr.nextBytes(sendNonce);
		System.out.println("Second nonce created: " + new BigInteger(sendNonce));
		
		byte[] iv = new byte[8];
		sr.nextBytes(iv);
		IvParameterSpec ivp = new IvParameterSpec(iv);
		
		/*Construct the second message*/
		c = Cipher.getInstance("DES/CFB8/PKCS5Padding");
		c.init(Cipher.ENCRYPT_MODE, key, ivp);
		byte[] secondmsg = new byte[NONCE_LENGTH + NONCE_LENGTH];
		System.arraycopy(firstNonceNumPlusOneCorrectLen, 0, secondmsg, 0, NONCE_LENGTH);
		System.arraycopy(sendNonce, 0, secondmsg, NONCE_LENGTH, NONCE_LENGTH);
		CipherOutputStream cos = new CipherOutputStream(os, c);
		cos.write(secondmsg);
		//c.doFinal();
		cos.flush();
		cos.close();
		System.out.println("Server sent the second message: " + new String(secondmsg, "UTF8"));
		
		iv = new byte[8];
		sr.nextBytes(iv);
		ivp = new IvParameterSpec(iv);
		
		/*Receive the third message, check that nonce = nonce*2*/
		c.init(Cipher.DECRYPT_MODE, key, ivp);
		byte[] thirdmsg = new byte[NONCE_LENGTH];
		bytesRead = cis.read(thirdmsg);
		while (bytesRead != NONCE_LENGTH) {
			bytesRead += cis.read(thirdmsg, bytesRead, NONCE_LENGTH - bytesRead);
		}
		BigInteger secondNonceNum = new BigInteger(sendNonce);
		secondNonceNum = secondNonceNum.shiftLeft(1);
		byte[] secondNonceTimesTwo = secondNonceNum.toByteArray();
		byte[] secondNonceTimesTwoCorrectLen = new byte[NONCE_LENGTH];
		System.arraycopy(secondNonceTimesTwo, 0, secondNonceTimesTwoCorrectLen, 0, NONCE_LENGTH);
		if (Arrays.equals(thirdmsg, secondNonceTimesTwoCorrectLen)) {
			System.out.println("Success");
			return key;
		}
		else {
			System.out.println("Failure");
			return null;
		}
	}
	
	/**
	 * Implements the authentication protocol on the client side
	 * is and os are the inputstream and output stream of the Socket
	 * used to connect to the server
	 * @throws IOException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws InvalidAlgorithmParameterException 
	 */
	public static SecretKey clientSideAuth(InputStream is, OutputStream os) throws NoSuchAlgorithmException, 
	NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		/*Fetch the server's public key*/
		PublicKey serverPubK = serverPublicKeyRSA();
		
		/*Generate a symmetric session key. 
		 * For DES, the default is 56 bit length*/
		KeyGenerator kg = KeyGenerator.getInstance("DES"); //algorithm is valid
		kg.init(DES_KEY_LENGTH);
		SecretKey key = kg.generateKey();
		byte[] sendkey = key.getEncoded();
		System.out.println("Encoded Key: " + new String(sendkey, "UTF8"));
		
		/*Prepare to encrypt a message for the server 
		 * using the server's public key */
		Cipher c = null;
		c = Cipher.getInstance("RSA");
		c.init(Cipher.ENCRYPT_MODE, serverPubK);
		
		/*Initialize a nonce.
		 *The sec random num gen. will also be used for DES */
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] nonce = new byte[NONCE_LENGTH];
		sr.nextBytes(nonce);
		if (debug) {
			System.out.println("First Nonce created on client side: " + new BigInteger(nonce));
		}
		
		/*Construct the first message*/
		byte[] firstmsg = new byte[NONCE_LENGTH + DES_KEY_LENGTH_BYTES];
		System.arraycopy(nonce, 0, firstmsg, 0, NONCE_LENGTH);
		System.arraycopy(sendkey, 0, firstmsg, NONCE_LENGTH, DES_KEY_LENGTH_BYTES);
		
		/*Encrypt */
		CipherOutputStream cos = new CipherOutputStream(os, c);
		
		//PrintWriter ps = new PrintWriter(cos, true);
		//ps.println(new String(firstmsg, "UTF8"));
		cos.write(firstmsg);
		//c.doFinal();
		cos.flush();
		cos.close();
		//ps.flush();
		
		System.out.println("Sent first message from client: " + new String(firstmsg, "UTF8"));
		
		/*Receive the second message*/
		
		c = Cipher.getInstance("DES/CFB8/PKCS5Padding");
		
		byte[] iv = new byte[8];
		sr.nextBytes(iv);
		IvParameterSpec ivp = new IvParameterSpec(iv);
		
		c.init(Cipher.DECRYPT_MODE, key, ivp);
		CipherInputStream cis = new CipherInputStream(is, c);
		byte[] secondmsg = new byte[NONCE_LENGTH + NONCE_LENGTH];
		int bytesRead = cis.read(secondmsg);
		while (bytesRead != NONCE_LENGTH + NONCE_LENGTH) {
			bytesRead += cis.read(secondmsg, bytesRead, NONCE_LENGTH + NONCE_LENGTH - bytesRead);
		}
		System.out.println("Second msg received: " + new String(secondmsg, "UTF8"));
		
		/*Verify that the first nonce is nonce + 1*/
		
		byte[] recvnonce = new byte[NONCE_LENGTH];
		System.arraycopy(secondmsg, 0, recvnonce, 0, NONCE_LENGTH);
		BigInteger firstNonceNum = new BigInteger(nonce);
		firstNonceNum = firstNonceNum.add(BigInteger.ONE);
		byte[] firstNonceNumPlusOne = firstNonceNum.toByteArray();
		byte[] firstNonceNumPlusOneCorrectLen = new byte[NONCE_LENGTH];
		System.arraycopy(firstNonceNumPlusOne, 0, firstNonceNumPlusOneCorrectLen, 0, NONCE_LENGTH);
		if (!Arrays.equals(recvnonce, firstNonceNumPlusOneCorrectLen)) {
			System.out.println("Failure");
			return null;
		}
		else {
			/*Calculate second nonce * 2*/
		
			byte[] secondNonce = new byte[NONCE_LENGTH];
			System.arraycopy(secondmsg, NONCE_LENGTH, secondNonce, 0, NONCE_LENGTH);
			BigInteger secondNonceNum = new BigInteger(secondNonce);
			System.out.println("Second nonce received: " + secondNonceNum);
			secondNonceNum = secondNonceNum.shiftLeft(1);
			System.out.println("Second nonce * 2: " + secondNonceNum);
			byte[] secondNonceTimesTwo = secondNonceNum.toByteArray();
			byte[] secondNonceTimesTwoCorrectLen = new byte[NONCE_LENGTH];
			System.arraycopy(secondNonceTimesTwo, 0, secondNonceTimesTwoCorrectLen, 0, NONCE_LENGTH);
			byte[] thirdmsg = new byte[NONCE_LENGTH];
			System.arraycopy(secondNonceTimesTwoCorrectLen, 0, thirdmsg, 0, NONCE_LENGTH);
			
			c.init(Cipher.ENCRYPT_MODE, key);
			cos.write(thirdmsg);
			cos.flush();
			cos.close();
			
			return key;
		}
	}
	
	/*
	private static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator pkf = KeyPairGenerator.getInstance("RSA");
		return pkf.generateKeyPair(); 
	}*/
	
	//TODO the default for RSA uses ECB...
}
