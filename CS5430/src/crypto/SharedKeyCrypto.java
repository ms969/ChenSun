package crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import server.SocialNetworkServer;

public class SharedKeyCrypto {
	
	public SharedKeyCrypto() {
	}
	
	public String generateKey() {
		KeyGenerator generator;
		byte[] wrappedKey = null;
		try {
			generator = KeyGenerator.getInstance("DES");
			generator.init(new SecureRandom());
			Key key = generator.generateKey();
			wrappedKey = key.getEncoded();
		} catch (NoSuchAlgorithmException e1) {
			if (SocialNetworkServer.DEBUG) {
				System.out.println("Check algorithm for SharedKeyCrypto generateKey");
			}
		}
		
		try {
			return new String(wrappedKey, "UTF8");
		} catch (UnsupportedEncodingException e) {
			if (SocialNetworkServer.DEBUG) {
				System.out.println("Check encoding code for SharedKeyCrypto generateKey");
			}
			return null;
		}
	}
	
	public Key unwrapKey(String wrappedKey) {
		byte[] keyBytes = null;
		try {
			keyBytes = wrappedKey.getBytes("UTF8");
			return (SecretKey) new SecretKeySpec(keyBytes, "DES");
		} catch (UnsupportedEncodingException e) {
			if (SocialNetworkServer.DEBUG) {
				System.out.println("Check encoding code for SharedKeyCrypto unwrapKey");
			}
			return null;
		}
	}
	
	public byte[] encrypt(String txt, Key key) throws InvalidKeyException, IllegalBlockSizeException {
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			if (SocialNetworkServer.DEBUG) {
				System.err.println("Check cipher algorithm for SharedKeyCrypto encrypt");
			}
			return null;
		} catch (NoSuchPaddingException e) {
			if (SocialNetworkServer.DEBUG) {
				System.err.println("Check cipher padding for SharedKeyCrypto encrypt");
			}
			return null;
		}
		cipher.init(Cipher.ENCRYPT_MODE, key);
		try {
			return cipher.doFinal(txt.getBytes("UTF8"));
		} catch (BadPaddingException e) {
			return null;
		} catch (UnsupportedEncodingException e) {
			if (SocialNetworkServer.DEBUG) {
				System.err.println("Check SharedKeyCrypto encrypt encoding code");
			}
			return null;
		}
	}
	
	public String decrypt(byte[] secret, Key key) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			if (SocialNetworkServer.DEBUG) {
				System.err.println("Check cipher algorithm for SharedKeyCrypto decrypt");
			}
			return null;
		} catch (NoSuchPaddingException e) {
			if (SocialNetworkServer.DEBUG) {
				System.err.println("Check cipher padding for SharedKeyCrypto decrypt");
			}
			return null;
		}
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] byteString = cipher.doFinal(secret);
		try {
			return new String(byteString, "UTF8");
		} catch (UnsupportedEncodingException e) {
			if (SocialNetworkServer.DEBUG) {
				System.err.println("Check SharedKeyCrypto decrypt encoding code");
			}
			return null;
		}
	}
}
