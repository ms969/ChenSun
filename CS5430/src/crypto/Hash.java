package crypto;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import server.SocialNetworkServer;

public class Hash {
	private static final boolean DEBUG = SocialNetworkServer.DEBUG;
	
	public static String generateHash(String pwd) {
		byte[] plainText;
		try {
			plainText = pwd.getBytes("UTF8");
			
		    // get a message digest object using the MD5 algorithm
		    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		    // calculate the digest and print it out
		    messageDigest.update(plainText);
		    return encode(messageDigest.digest());
		} catch (UnsupportedEncodingException e) {
			if (DEBUG) e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			if (DEBUG) e.printStackTrace();
		}
	    return null;
	}
	
	private static String encode(byte[] bytes) {
		return CryptoUtil.encode(bytes);
	}
	
//	private static byte[] decode(String str) {
//		return CryptoUtil.decode(str);
//	}
}
