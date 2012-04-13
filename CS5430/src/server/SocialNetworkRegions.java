package server;

import java.sql.Connection;

import database.SocialNetworkDatabaseBoards;
import database.SocialNetworkDatabasePosts;
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
		String bname = boardName.trim().toLowerCase();
		if (boardName.equals("freeforall")) {
			return "Invalid Call to Function";
		}
		//TODO any more region names that should be discouraged?
		if (regionName.trim().equals("") || regionName.contains("..") 
				|| regionName.contains(";")
				|| regionName.contains(" ")
				|| regionName.trim().toLowerCase().equals("home")
				|| regionName.trim().contains("/")) {
			return "print Error: Cannot create a region with the name \"" + regionName 
			  +"\". Please use a different name (One word, Case Insensitive).";
		}
		Connection dbconn = DBManager.getConnection();
		Boolean boardExists = SocialNetworkDatabaseBoards.boardExists(dbconn, bname);
		if (boardExists == null) {
			return "print Error: Database error while verifying existence of board. " +
					"If the problem persists, contact an admin.";
		}
		if (boardExists.booleanValue()) {
			String msg = SocialNetworkDatabaseRegions.createRegion(dbconn, username, 
					bname, regionName.trim().toLowerCase());
			DBManager.closeConnection(dbconn);
			return msg;
		}
		else {
			return "print Error: Encapsulating Board does not exist. Refresh. " +
					"If the problem persists, contact an admin.";
		}
	}

	public static String viewRegions(String username, String boardName, boolean isGoTo){
		if (boardName == null) {
			return "Invalid Call to Function";
		}
		String bname = boardName.trim().toLowerCase();
		if (bname.equals("freeforall")) {
			return "Invalid Call to Function";
		}
		Connection dbconn = DBManager.getConnection();
		Boolean boardExists = SocialNetworkDatabaseBoards.boardExists(dbconn, bname);
		if (boardExists == null) {
			return "print Error: Database error while verifying existence of board. " +
					"If the problem persists, contact an admin.";
		}
		if (boardExists.booleanValue()) {
			//AUTHORIZATION CHECK
			if (isGoTo) {
				Boolean authorized = SocialNetworkDatabaseBoards.authorizedGoToBoard(dbconn, username, boardName);
				if (authorized == null) {
					return "print Error: Database error while checking authorization. If the problem persists, contact an admin.";
				}
				else if (!authorized.booleanValue()) {
					return "print Error: Not authorized to view this board.";
				}
			}
			String msg = SocialNetworkDatabaseRegions.getRegionListRecentPosts(dbconn, username, bname);
			DBManager.closeConnection(dbconn);
			return msg;
		}
		else {
			return "print Error: Encapsulating Board does not exist. Refresh. " +
					"If the problem persists, contact an admin.";
		}
	}
	
	public static String addParticipant(Connection conn, String board, String region, 
			String username, String priv, String grantedBy) {
		String success = "print "+username+" has been successfully added to the region/post " +
				"with "+priv+" privilege.;";
		String error = "print Database Error while adding "+username+" to region/post. " +
				"Please try again or contact a system admin.;";
		int status = 0;
		if (board.equals("freeforall")) {
			status = SocialNetworkDatabasePosts.addFFAParticipipant(conn, Integer.parseInt(region), username, priv);
		} else {
			status = SocialNetworkDatabaseRegions.addParticipant(
					conn, board, region, username, priv, grantedBy);
		}
		if (status == 1) {
			return success;
		} else {
			return error;
		}
	}
}
