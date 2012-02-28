package server;

import java.sql.Connection;

import database.SocialNetworkDatabaseBoards;
import database.SocialNetworkDatabaseRegions;
import database.DBManager;

/**
 * Look at corresponding SQL.java file for specs.
 * These functions are merely wrappers, containing
 * some server logic.
 * @author kchen
 *
 */
public class SocialNetworkRegions {
	
	public static Boolean regionExists(String boardName, String regionName) {
		Connection dbconn = DBManager.getConnection();
		Boolean regionExists = SocialNetworkDatabaseRegions.regionExists(dbconn, 
				boardName.trim().toLowerCase(), regionName.trim().toLowerCase());
		DBManager.closeConnection(dbconn);
		return regionExists;
	}
	
	public static String createRegion(String username, String boardName, String regionName) {
		if (boardName == null || regionName == null) {
			return "Invalid Call to Function";
		}
		if (boardName.equals("freeforall")) {
			return "Invalid Call to Function";
		}
		//TODO any more region names that should be discouraged?
		if (regionName.trim().equals("") || regionName.contains("..") 
				|| regionName.contains(";")
				|| regionName.contains(" ")
				|| regionName.trim().toLowerCase().equals("home")
				|| regionName.trim().contains("/")) {
			return "print Cannot create a region with the name \"" + regionName 
			  +"\". Please use a different name (One word, Case Insensitive).";
		}
		Connection dbconn = DBManager.getConnection();
		Boolean boardExists = SocialNetworkDatabaseBoards.boardExists(dbconn, boardName.trim().toLowerCase());
		if (boardExists == null) {
			return "print Database error while verifying existence of board. If the problem persists, contact an admin.";
		}
		if (boardExists.booleanValue()) {
			String msg = SocialNetworkDatabaseRegions.createRegion(dbconn, username, 
					boardName.trim().toLowerCase(), regionName.trim().toLowerCase());
			DBManager.closeConnection(dbconn);
			return msg;
		}
		else {
			return "print Board DNE";
		}
	}

	public static String viewRegions(String username, String boardName){
		if (boardName == null) {
			return "Invalid Call to Function";
		}
		if (boardName.equals("freeforall")) {
			return "Invalid Call to Function";
		}
		Connection dbconn = DBManager.getConnection();
		Boolean boardExists = SocialNetworkDatabaseBoards.boardExists(dbconn, boardName.trim().toLowerCase());
		if (boardExists == null) {
			return "print Database error while verifying existence of board. If the problem persists, contact an admin.";
		}
		if (boardExists.booleanValue()) {
			String msg = SocialNetworkDatabaseRegions.getRegionListRecentPosts(dbconn, username, boardName.trim().toLowerCase());
			DBManager.closeConnection(dbconn);
			return msg;
		}
		else {
			return "print Board DNE";
		}
	}
}
