package crypto;

import org.apache.commons.codec.binary.Base64;

public class CryptoUtil {
	
	public static String encode(byte[] bytes) {
		return Base64.encodeBase64String(bytes);
	}
	
	public static byte[] decode(String str) {
		return Base64.decodeBase64(str);
	}

	public static boolean validPassword(char[] pwd) {
		// between 6 and 20 character
		if (pwd.length < 6 || pwd.length > 20) {
			return false;
		}
		
		boolean lowerCase = false, upperCase = false, number = false;
		for (char c: pwd) {
			String s = c + "";
			if (s.matches("[a-z]")) { // contains 1 lower case
				lowerCase = true;
			} else if (s.matches("[A-Z]")) { // contains 1 upper case
				upperCase = true;
			} else if (s.matches("[0-9]")) { // contains a number
				number = true;
			} else if (s.matches("[!@#$%^&*?:.,~`+=\\-_|]")) {
				// contains other allowed chars:
				// ! @ # $ % ^ & * ? : . , ~ ` + = - _ |
			} else {
				return false;
			}
		}
		return lowerCase && upperCase && number;
	}
}
