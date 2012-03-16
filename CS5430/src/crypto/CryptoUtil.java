package crypto;

import java.io.IOException;

import server.SocialNetworkServer;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class CryptoUtil {
	public static String encode(byte[] bytes) {
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(bytes);
	}
	
	public static byte[] decode(String str) {
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			return decoder.decodeBuffer(str);
		} catch (IOException e) {
			if (SocialNetworkServer.DEBUG) e.printStackTrace();
			return null;
		}
	}
	
	private static byte[] generateZeroArray(int length) {
		byte[] array = new byte[length];
		byte[] zero = {(byte)0x00};
		for (int i=0; i<length; i++) {
			System.arraycopy(zero, 0, array, i, 1);
		}
		return array;
	}
	
	public static void zeroArray(byte[] array) {
		byte[] zeros = generateZeroArray(array.length);
		System.arraycopy(zeros, 0, array, 0, array.length);
	}
}
