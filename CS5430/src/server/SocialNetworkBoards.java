package server;

import java.io.IOException;
import java.sql.Connection;

import database.DBManager;
import database.SocialNetworkDatabaseBoards;

/**
 * Look at corresponding SQL.java file for specs.
 * These functions are merely wrappers, containing
 * some server logic.
 * @author kchen
 *
 */
public class SocialNetworkBoards {

	public static Boolean boardExists(String boardName) {
		Connection dbconn = DBManager.getConnection();
		Boolean exists = SocialNetworkDatabaseBoards.boardExists(dbconn, boardName.trim().toLowerCase());
		DBManager.closeConnection(dbconn);
		return exists;
	}
	
	//TODO assumptions: user is admin.
	public static String createBoard(String username, String boardName) 
	throws IOException {
		if (boardName.trim().contains("..") || boardName.trim().equals("") 
				|| boardName.trim().toLowerCase().equals("freefreeall")
				|| boardName.trim().toLowerCase().equals("main")
				|| boardName.trim().toLowerCase().equals("home")
				|| boardName.contains(" ")
				|| boardName.trim().contains(";")
				|| boardName.trim().contains("/")) {
			return "print Error: Cannot create a board with the name \"" + boardName 
			  +"\". Please use a different name (One word, Case Insensitive).";
		}
		Connection dbconn = DBManager.getConnection();
		String msg = SocialNetworkDatabaseBoards.createBoard(dbconn, username, boardName.trim().toLowerCase());
		DBManager.closeConnection(dbconn);
		return msg;
	}

	public static String viewBoards(String username) {
		Connection dbconn = DBManager.getConnection();
		String msg = SocialNetworkDatabaseBoards.getBoardList(dbconn, username);
		DBManager.closeConnection(dbconn);
		return msg;
	}

}
