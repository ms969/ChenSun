package shared;

public class ProjectConfig {
	
	public enum SendingType {
		SHARED_KEY,
		NO_ENCRYPTION
	}
	
	public static final SendingType SENDING_METHOD = SendingType.SHARED_KEY;
	public static final boolean DEBUG = true;
}
