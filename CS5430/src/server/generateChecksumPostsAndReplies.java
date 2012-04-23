package server;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import crypto.CryptoUtil;
import crypto.Hash;

public class generateChecksumPostsAndReplies {
	
	public static String decAndCheck = "RuK3LVd6bQA=bDFM2Quk4LU=";

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{

		/*make decoder*/
		DESKeySpec keySpec = new DESKeySpec(CryptoUtil.decode("92LlYoVU1hU="));
		SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
		SecretKey key = factory.generateSecret(keySpec);
		
		byte[] iv = CryptoUtil.decode(decAndCheck.substring(0, 12));
		Cipher c = Cipher.getInstance("DES/CBC/PKCS5Padding");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		c.init(Cipher.DECRYPT_MODE, key, ivSpec);
		
		byte[] content = c.doFinal(CryptoUtil.decode(decAndCheck.substring(12)));
		System.out.println(new String(content, "UTF8"));
		byte[] checksum = Hash.generateChecksum(content);
		System.out.println("\"" + CryptoUtil.encode(checksum) + "\"");
		
	}

}
