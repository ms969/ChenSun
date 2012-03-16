package crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import server.SocialNetworkServer;

public class SharedKeyCrypto {
	
	private static final boolean DEBUG = SocialNetworkServer.DEBUG;
	
	private static final int IV_STRING_LENGTH = 12;
	
	private static final String KEY_STRING = "92LlYoVU1hU="; // TODO: need to take this out
	private Key key;
	
	public SharedKeyCrypto() {
		try {
			this.key = unwrapKey(KEY_STRING);
		} catch (InvalidKeyException e) {
			if (DEBUG) e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			if (DEBUG) e.printStackTrace();
		}
	}
	
	public String generateKey() throws InvalidKeySpecException {
		try {
			KeyGenerator generator = KeyGenerator.getInstance("DES");
			generator.init(new SecureRandom());
			SecretKey key = generator.generateKey();
			
			SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
			DESKeySpec keySpec = (DESKeySpec) factory.getKeySpec(key, DESKeySpec.class);
			byte[] wrappedKey = keySpec.getKey();
			
			return encode(wrappedKey);
		} catch (NoSuchAlgorithmException e1) {
			if (DEBUG) e1.printStackTrace();
		}
		return null;
	}
	
	public SecretKey unwrapKey(String wrappedKey) throws InvalidKeyException, InvalidKeySpecException {
		try {
			DESKeySpec keySpec = new DESKeySpec(decode(wrappedKey));
			SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
			SecretKey key = factory.generateSecret(keySpec);
			return key;
		} catch (NoSuchAlgorithmException e) {
			if (DEBUG) e.printStackTrace();
		}
		return null;
	}
	
	public String encrypt(String txt) throws InvalidKeyException, IllegalBlockSizeException {
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			String iv = encode(cipher.getIV());
			byte[] encrypted = cipher.doFinal(txt.getBytes("UTF8"));
			return iv+encode(encrypted);
		} catch (BadPaddingException e) {
			if (DEBUG) e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			if (DEBUG) e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			if (DEBUG) e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			if (DEBUG) e.printStackTrace();
		}
		return null;
	}
	
	public String decrypt(String secret) throws InvalidKeyException, IllegalBlockSizeException {
		Cipher cipher;
		try {
			byte[] iv = decode(secret.substring(0, IV_STRING_LENGTH));
			cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
			
			byte[] byteString = cipher.doFinal(decode(secret.substring(IV_STRING_LENGTH)));

			return new String(byteString, "UTF8");
		} catch (UnsupportedEncodingException e) {
			if (DEBUG) e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			if (DEBUG) e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			if (DEBUG) e.printStackTrace();
		} catch (BadPaddingException e) {
			if (DEBUG) e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			if (DEBUG) e.printStackTrace();
		}
		return null;
	}
	
	private String encode(byte[] bytes) {
		return CryptoUtil.encode(bytes);
	}
	
	private byte[] decode(String str) {
		return CryptoUtil.decode(str);
	}
}
