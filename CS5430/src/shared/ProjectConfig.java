package shared;

public class ProjectConfig {
	
	public enum SendingType {
		SHARED_KEY,
		NO_ENCRYPTION
	}
	
	public static final SendingType SENDING_METHOD = SendingType.NO_ENCRYPTION;
	public static final boolean DEBUG = true;
	public static final int SERVER_CLIENT_CONN_PORT = 5430;
}
