package crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import server.SocialNetworkServer;

public class Hash {
	private static final boolean DEBUG = SocialNetworkServer.DEBUG;
	public static final int SALT_STRING_LENGTH = 24;
	public static final int SALT_BYTE_LENGTH = 16;
	
	public static String generateHash(byte[] pwd) {
		try {
		    // get a message digest object using the MD5 algorithm
		    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		    // calculate the digest and print it out
		    messageDigest.update(pwd);
		    return encode(messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			if (DEBUG) e.printStackTrace();
		}
	    return null;
	}
	
	public static String createPwdHashStore(byte[] pwd) {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[SALT_BYTE_LENGTH];
		random.nextBytes(salt);
		byte[] saltAndPwd = new byte[salt.length+pwd.length];
		System.arraycopy(salt, 0, saltAndPwd, 0, salt.length);
		System.arraycopy(pwd, 0, saltAndPwd, salt.length, pwd.length);
		String pwdh = generateHash(saltAndPwd);
		String salts = encode(salt);
		System.out.println("string length of salt: "+salts.length());
		return salts+pwdh;
	}
	

	
	public static boolean comparePwd(String storedPwd, byte[] enteredPwd) {
		try {
			byte[] saltBytes = decode(storedPwd.substring(0, SALT_STRING_LENGTH));
			byte[] pwdAndSalt = new byte[saltBytes.length+enteredPwd.length];
			System.arraycopy(saltBytes, 0, pwdAndSalt, 0, saltBytes.length);
			System.arraycopy(enteredPwd, 0, pwdAndSalt, saltBytes.length, enteredPwd.length);
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(pwdAndSalt);
			String pwdAndSaltStr = encode(md.digest());
			boolean ret = storedPwd.substring(SALT_STRING_LENGTH).equals(pwdAndSaltStr);
			CryptoUtil.zeroArray(enteredPwd);
			CryptoUtil.zeroArray(pwdAndSalt);
			return ret;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	private static String encode(byte[] bytes) {
		return CryptoUtil.encode(bytes);
	}
	
	private static byte[] decode(String str) {
		return CryptoUtil.decode(str);
	}
}
