package shared;

public class InputProcessor {
	public static String getValue(String command) {
		int spacei = command.indexOf(" ");
		return command.substring(spacei+1);
	}
	

	public static String quote(String input) {
		return "'" + input + "'";
	}
	
	// return a command for printing a horizontal rule numChar long
	public static String getHR(int numChar) {
		String hr = "print ";
		for (int i=0; i < numChar; i++) {
			hr += "-";
		}
		return hr + ";";
	}
}
