package crypto;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class PasswordBasedEncryption {
	
	public static final String ALG = "PBEWithMD5AndDES";
	
	public static byte[] decrypt(byte[] data, char[] password, 
			byte[] salt, int iterations) {
		byte[] decrypted = null;
		try {
			SecretKeyFactory kf = SecretKeyFactory.getInstance(ALG);
			PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterations);
			SecretKey sk = kf.generateSecret(keySpec);
			
			Cipher c = Cipher.getInstance(ALG);
			PBEParameterSpec params = new PBEParameterSpec(salt, iterations);
			c.init(Cipher.DECRYPT_MODE, sk, params);
			decrypted = c.doFinal(data);
		} catch (Exception e) { //this could happen if the password is wrong.
			System.out.println("Not correct");
		}
		return decrypted;
	}
}
