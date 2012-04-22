package shared;


public class Utils {

	// return a command for printing a horizontal rule numChar long
	public static String getHR(int numChar) {
		String hr = "print ";
		for (int i=0; i < numChar; i++) {
			hr += "-";
		}
		return hr + ";";
	}

	public static String getValue(String command) {
		int spacei = command.indexOf(" ");
		return command.substring(spacei+1);
	}

	public static String quote(String input) {
		return "'" + input + "'";
	}

	public static byte[] stringToBytesASCII(char[] buffer) {
		byte[] b = new byte[buffer.length];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) buffer[i];
		}
		return b;
	}
	
	public static byte[] charToByteArray(char[] chars) {
		byte[] bytes = new byte[chars.length*2];
		for(int i=0; i<chars.length; i++) {
		   bytes[i*2] = (byte) (chars[i] >> 8);
		   bytes[i*2+1] = (byte) chars[i];
		}
		return bytes;
	}
	
	public static char[] byteToCharArray(byte[] bytes) {
		char[] chars = new char[bytes.length/2];
		for(int i=0; i<chars.length; i+=1) {
		   chars[i] = (char) (bytes[i*2] << 8 + bytes[i*2+1]);//why & 11111111?? it's just identity
		}
		return chars;
	}

}
