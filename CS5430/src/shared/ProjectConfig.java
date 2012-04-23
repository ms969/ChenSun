package shared;

public class ProjectConfig {
	
	public enum SendingType {
		SHARED_KEY,
		NO_ENCRYPTION
	}
	
	public static final SendingType SENDING_METHOD = SendingType.SHARED_KEY;
	public static final boolean DEBUG = false;
	public static final int SERVER_CLIENT_CONN_PORT = 5430;
	
	public static final String COMMAND_INVALID = "print Invalid command.;";
	public static final String COMMAND_CANCEL = "print Cancelled.;";
	public static final String COMMAND_HELP = "print To see a list of commands type 'help'.;";
	public static final String COMMAND_EMPTY_LIST = "print     (Empty);";
	public static final String SECURITY_QUESTION = "Where were you born?";
	public static final String COMMAND_SECURITY_QUESTION = "print " + SECURITY_QUESTION + ";";
	public static final int SERVER_TIMEOUT = 15*60*1000; // unit: milliseconds
	public static final int CLIENT_TIMEOUT = 30*1000; // units: milliseconds
}
