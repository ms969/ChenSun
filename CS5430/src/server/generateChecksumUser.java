package server;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import crypto.CryptoUtil;
import crypto.Hash;

public class generateChecksumUser {

	public static String username = "fbs";
	public static String number = "5";
	public static String role = "";
	public static String hashPw = null;
	public static String hashSecA = null;
	
	public static String encrypt(String txt, SecretKey key) throws Exception{
		Cipher cipher = null;
		cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		String iv = CryptoUtil.encode(cipher.getIV());
		byte[] encrypted = cipher.doFinal(txt.getBytes("UTF8"));
		return iv+CryptoUtil.encode(encrypted);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{

		/*make decoder*/
		DESKeySpec keySpec = new DESKeySpec(CryptoUtil.decode("92LlYoVU1hU="));
		SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
		SecretKey key = factory.generateSecret(keySpec);
		
		char[] pwd = "Spring1234".toCharArray();
		hashPw = Hash.createPwdHashStore(pwd);
		System.out.println("new password hash: " + hashPw);
		
		char[] sec = "ithaca".toCharArray();
		hashSecA = Hash.createPwdHashStore(sec);
		System.out.println("new sec hash: " + hashSecA);
		
		String encHashPw = encrypt(hashPw, key);
		String encHashSecA = encrypt(hashSecA, key);
		
		byte[] userBytes = username.getBytes("UTF8");
		byte[] pwBytes = hashPw.getBytes("UTF8");
		byte[] ansBytes = hashSecA.getBytes("UTF8");
		
		byte[] toChecksum = new byte[userBytes.length + pwBytes.length + ansBytes.length];
		System.arraycopy(userBytes, 0, toChecksum, 0, userBytes.length);
		System.arraycopy(pwBytes, 0, toChecksum, userBytes.length, pwBytes.length);
		System.arraycopy(ansBytes, 0, toChecksum, pwBytes.length + userBytes.length, ansBytes.length);
		
		String checksum = CryptoUtil.encode(Hash.generateChecksum(toChecksum));
		
		System.out.println();
		System.out.printf("\"%s\", %s, '%s', \"%s\", \"%s\"", encHashPw, number, role, encHashSecA, checksum);
		
	}
	
	
}
