package server;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import crypto.CryptoUtil;
import crypto.Hash;

public class generateEncryptedStuffs {

	private static SecureRandom createSecureRandom() {
		SecureRandom sr = null;
		try {
			sr = SecureRandom.getInstance("SHA1PRNG");
		}
		catch (NoSuchAlgorithmException e) {/*Valid alg*/}
		return sr;
	}
	
	/** Creates an IV with the given size**/
	private static byte[] createRandomBits(SecureRandom sr, int size) {
		byte[] iv = new byte[size];
		sr.nextBytes(iv);
		return iv;
	}
	
	public static void main(String[] args) throws Exception{
		int index;
		String password = "Cs5430!";
		byte[] privateKey = (new BigInteger("35945105707051436463265304985738551991613891713175525" +
				"1555642053453407248168284392665741392290994678901854876959302641751351486930931" +
				"5319779891888363939131910793794159827285654790441055281056962297542468442548591" +
				"9920471686858218767521363784780358713936281232600468990679159303006096606946533" +
				"721779530214714753")).toByteArray();
		byte[] sharedKey = CryptoUtil.decode("92LlYoVU1hU=");
		
		for (index = 0; index < 2; index++) {
			byte[] indexArr = ByteBuffer.allocate(4).putInt(index).array();
			byte[] salt = createRandomBits(createSecureRandom(), 8);
			int iter = 1305;
			if (index == 1) {
				iter = 1530;
			}
			byte[] iterArr = ByteBuffer.allocate(4).putInt(iter).array();
			
			byte[] checksum = null;
			if (index == 0) {
				checksum = new byte[indexArr.length + salt.length + iterArr.length + privateKey.length];
				System.arraycopy(privateKey, 0, checksum, indexArr.length + salt.length + iterArr.length, privateKey.length);
			}
			else {
				checksum = new byte[indexArr.length + salt.length + iterArr.length + sharedKey.length];
				System.arraycopy(sharedKey, 0, checksum, indexArr.length + salt.length + iterArr.length, sharedKey.length);
			}
			System.arraycopy(indexArr, 0, checksum, 0, indexArr.length);
			System.arraycopy(salt, 0, checksum, indexArr.length, salt.length);
			System.arraycopy(iterArr, 0, checksum, indexArr.length + salt.length, iterArr.length);
			
			//generate the encryped key.
			
			SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
			PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iter);
			SecretKey sk = kf.generateSecret(keySpec);
			
			Cipher c = Cipher.getInstance(sk.getAlgorithm());
			PBEParameterSpec params = new PBEParameterSpec(salt, iter);
			c.init(Cipher.ENCRYPT_MODE, sk, params);
			byte[] enckey = (index == 0 ? c.doFinal(privateKey) : c.doFinal(sharedKey));
			
			System.out.println(index + ", \"" + CryptoUtil.encode(salt) + "\", " + iter + ", \"" 
			+ CryptoUtil.encode(enckey) + "\", \"" + CryptoUtil.encode(Hash.generateChecksum(checksum)) + "\"");
			
		}
		
		System.out.println(CryptoUtil.encode(privateKey));
		System.out.println(CryptoUtil.encode(sharedKey));
		
		
	}

}
