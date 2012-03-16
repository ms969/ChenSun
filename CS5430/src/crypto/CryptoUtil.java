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
}
