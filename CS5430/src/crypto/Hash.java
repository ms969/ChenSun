package crypto;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import server.SocialNetworkServer;
import shared.ProjectConfig;

public class Hash {
	private static final boolean DEBUG = ProjectConfig.DEBUG;
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
			if (DEBUG)
				e.printStackTrace();
		}
		return null;
	}

	public static String createPwdHashStore(char[] pwd) {
		byte[] pwdBytes = new byte[pwd.length*2];
		ByteBuffer.wrap(pwdBytes).asCharBuffer().put(pwd);
		
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[SALT_BYTE_LENGTH];
		random.nextBytes(salt);
		byte[] saltAndPwd = new byte[salt.length + pwdBytes.length];
		System.arraycopy(salt, 0, saltAndPwd, 0, salt.length);
		System.arraycopy(pwdBytes, 0, saltAndPwd, salt.length, pwdBytes.length);
		String pwdh = generateHash(saltAndPwd);
		return encode(salt) + pwdh;
	}

	public static boolean comparePwd(String storedPwd, char[] enteredPwd) {
		String pwdAndSaltStr = hashExistingPwd(storedPwd.substring(0, SALT_STRING_LENGTH), enteredPwd);
		boolean ret = storedPwd.substring(SALT_STRING_LENGTH).equals(
				pwdAndSaltStr);
		Arrays.fill(enteredPwd, ' ');
		return ret;
	}
	
	public static boolean comparePwd(String storedPwd, String enteredPwd) {
		return storedPwd.substring(SALT_STRING_LENGTH).equals(enteredPwd);
	}
	
	public static String hashExistingPwd(String salt, char[] enteredPwd) {
		// convert char[] to byte[]
		byte[] enteredPwdByte = new byte[enteredPwd.length*2];
		ByteBuffer.wrap(enteredPwdByte).asCharBuffer().put(enteredPwd);
		// getting the salt in byte[]
		byte[] saltBytes = decode(salt);
		// concatenating into one
		byte[] pwdAndSalt = new byte[saltBytes.length + enteredPwdByte.length];
		System.arraycopy(saltBytes, 0, pwdAndSalt, 0, saltBytes.length);
		System.arraycopy(enteredPwdByte, 0, pwdAndSalt, saltBytes.length,
				enteredPwdByte.length);
		// hashing it
		String hash = generateHash(pwdAndSalt);
		Arrays.fill(enteredPwdByte, (byte)0x00);
		Arrays.fill(pwdAndSalt, (byte)0x00);
		return hash;
	}

	private static String encode(byte[] bytes) {
		return CryptoUtil.encode(bytes);
	}

	private static byte[] decode(String str) {
		return CryptoUtil.decode(str);
	}
}
