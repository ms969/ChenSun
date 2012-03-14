package crypto;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

public class SharedKeyCrypto {

	private Key key;
	
	public SharedKeyCrypto() {
	}
	
	public void generateKey() throws NoSuchAlgorithmException, NoSuchPaddingException {
		KeyGenerator generator;
		generator = KeyGenerator.getInstance("DES");
		generator.init(new SecureRandom());
		key = generator.generateKey();
		Cipher cipher = Cipher.getInstance("DES");
		//cipher.unwrap(wrappedKey, "DES", SECRET_KEY);
	}
}
