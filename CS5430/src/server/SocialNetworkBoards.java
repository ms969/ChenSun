package server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import database.DBManager;
import database.SocialNetworkDatabaseBoards;

public class SocialNetworkBoards {

	//TODO assumptions: user is admin.
	public static String createBoard(String boardName, String username) 
	throws SQLException, IOException {
		if (boardName.trim().contains("..") || boardName.trim().equals("") 
				|| boardName.trim().toLowerCase().equals("freefreeall")
				|| boardName.trim().toLowerCase().equals("main")
				|| boardName.trim().contains(";")) {
			return "print Cannot create a board with the name \"" + boardName 
			  +"\". Please use a different name (Case Insensitive).";
		}
		Connection dbconn = DBManager.getConnection();
		String msg = SocialNetworkDatabaseBoards.createBoard(dbconn, username, boardName.trim().toLowerCase());
		dbconn.close();
		return msg;
	}

	public static String viewBoards(String username) throws SQLException {
		Connection dbconn = DBManager.getConnection();
		String msg = SocialNetworkDatabaseBoards.getBoardList(dbconn, username);
		dbconn.close();
		return msg;
	}

}
