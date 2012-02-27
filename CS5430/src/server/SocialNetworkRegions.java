package server;

import java.sql.Connection;

import database.SocialNetworkDatabaseRegions;
import database.DBManager;

public class SocialNetworkRegions {
	
	public static String createRegion(String username, String boardName, String regionName) {
		if (boardName == null || regionName == null) {
			return "Invalid Call to Function";
		}
		if (boardName.equals("freeforall")) {
			return "Invalid Call to Function";
		}
		//TODO any more region names that should be discouraged?
		if (regionName.trim().equals("") || regionName.contains("..") || 
				regionName.contains(";")) {
			return "print Cannot create a region with the name \"" + regionName 
			  +"\". Please use a different name (Case Insensitive).";
		}
		Connection dbconn = DBManager.getConnection();
		String msg = SocialNetworkDatabaseRegions.createRegion(dbconn, username, boardName, regionName);
		DBManager.closeConnection(dbconn);
		return msg;
	}

	public static String viewRegions(String username, String boardName){
		if (boardName == null) {
			return "Invalid Call to Function";
		}
		if (boardName.equals("freeforall")) {
			return "Invalid Call to Function";
		}
		Connection dbconn = DBManager.getConnection();
		String msg = SocialNetworkDatabaseRegions.getRegionListRecentPosts(dbconn, username, boardName.trim().toLowerCase());
		DBManager.closeConnection(dbconn);
		return msg;
	}
}
