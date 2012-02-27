package server;

import java.sql.Connection;

import database.SocialNetworkDatabasePosts;
import database.DBManager;

public class SocialNetworkPosts {
	public static String createPostFreeForAll(String username, String content) {
		return "";
	}
	public static String createPost(String username, String boardName, 
		 String regionName, String content) {
		return "";
	}
	public static String getPostListFreeForAll(String username) {
		Connection dbconn = DBManager.getConnection();
		String msg = SocialNetworkDatabasePosts.getPostListFreeForAll(dbconn, username);
		DBManager.closeConnection(dbconn);
		return msg;
	}
	public static String getPostList(String username, String boardName, String regionName) {
		if (boardName == null || regionName == null) {
			return "Invalid Call to Function";
		}
		if (boardName.equals("freeforall") || regionName.trim().equals("")) {
			return "Invalid Call to Function";
		}
		Connection dbconn = DBManager.getConnection();
		String msg = SocialNetworkDatabasePosts.getPostList(dbconn, username, boardName, regionName);
		DBManager.closeConnection(dbconn);
		return msg;
	}
}
