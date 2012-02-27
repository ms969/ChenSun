package server;

import java.sql.Connection;

import database.SocialNetworkDatabasePosts;
import database.DBManager;

public class SocialNetworkPosts {
	public String createPost(String username, Integer boardNum, 
			Integer regionNum, String content) {
		return "";
	}
	public String getPostListFreeForAll(String username) {
		Connection dbconn = DBManager.getConnection();
		String msg = SocialNetworkDatabasePosts.getPostListFreeForAll(dbconn, username);
		DBManager.closeConnection(dbconn);
		return msg;
	}
	public String getPostList(String username, String boardName, String regionName) {
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
