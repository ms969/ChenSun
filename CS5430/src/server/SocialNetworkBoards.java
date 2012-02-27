package server;

import java.io.IOException;
import java.sql.Connection;

import database.DBManager;
import database.SocialNetworkDatabaseBoards;

public class SocialNetworkBoards {

	//TODO assumptions: user is admin.
	public static String createBoard(String boardName, String username) 
	throws IOException {
		if (boardName.trim().contains("..") || boardName.trim().equals("") 
				|| boardName.trim().toLowerCase().equals("freefreeall")
				|| boardName.trim().toLowerCase().equals("main")
				|| boardName.trim().contains(";")) {
			return "print Cannot create a board with the name \"" + boardName 
			  +"\". Please use a different name (Case Insensitive).";
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
