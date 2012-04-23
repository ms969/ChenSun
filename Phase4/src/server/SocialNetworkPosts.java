package server;

import java.sql.Connection;

import database.SocialNetworkDatabasePosts;
import database.DBManager;
import database.SocialNetworkDatabaseRegions;

/**
 * Look at corresponding SQL.java file for specs.
 * These functions are merely wrappers, containing
 * some server logic.
 * @author kchen
 *
 */
public class SocialNetworkPosts {
	
	public static Boolean postExists(String boardName, String regionName, int postNum) {
		Connection dbconn = DBManager.getConnection();
		Boolean exists = SocialNetworkDatabasePosts.postExists(dbconn, boardName, regionName, postNum);
		DBManager.closeConnection(dbconn);
		return exists;
	}
	
	//Returns "true" when everything is good, or a msg when something is wrong.
	public static String authorizedToReply(String username, String boardName, String regionName, int postNum) {
		Connection dbconn = DBManager.getConnection();
		String bname = boardName.trim().toLowerCase();
		if (bname.equals("freeforall")) { //regionName might be null
			Boolean postExists = postExists("freeforall", null, postNum);
			if (postExists == null) {
				DBManager.closeConnection(dbconn);
				return "print Error: Database error while verifying existence of post. " +
				"If the problem persists, contact an admin.";
			}
			else if (postExists.booleanValue()) {
				//AUTHORIZATION FUNCTION
				Boolean authReply = SocialNetworkDatabasePosts.authorizedFFAPost(dbconn, username, postNum, "reply");
				if (authReply == null) {
					DBManager.closeConnection(dbconn);
					return "print Error: Database error while creating the reply.";
				}
				else if (!authReply.booleanValue()) {
					DBManager.closeConnection(dbconn);
					return "print Error: Not authorized to reply to this post.";
				}
				DBManager.closeConnection(dbconn);
				return "true";
			}
			else {
				DBManager.closeConnection(dbconn);
				return "print Error: Post does not exist. Refresh. " +
						"If the problem persists, contact an admin.";
			}
		}
		//regionname not null
		String rname = regionName.trim().toLowerCase();
		Boolean boardExists = SocialNetworkBoards.boardExists(bname);
		if (boardExists == null) {
			DBManager.closeConnection(dbconn);
			return "print Error: Database error while verifying existence of board. " +
					"If the problem persists, contact an admin.";
		}
		else if (boardExists.booleanValue()) {
			Boolean regionExists = SocialNetworkRegions.regionExists(bname, rname);
			if (regionExists == null) {
				DBManager.closeConnection(dbconn);
				return "print Error: Database error while verifying existence of region. " +
						"If the problem persists, contact an admin.";
			}
			else if (regionExists.booleanValue()) {
				Boolean postExists = postExists(bname, rname, postNum);
				if (postExists == null) {
					DBManager.closeConnection(dbconn);
					return "print Error: Database error while verifying existence of post. " +
					"If the problem persists, contact an admin.";
				}
				else if (postExists.booleanValue()) {
					//AUTHORIZATION FUNCTION
					Boolean authReply = SocialNetworkDatabasePosts.authorizedToPostNotFFA(dbconn, username, boardName, regionName);
					if (authReply == null) {
						DBManager.closeConnection(dbconn);
						return "print Error: Database error while creating the reply.";
					}
					else if (!authReply.booleanValue()) {
						DBManager.closeConnection(dbconn);
						return "print Error: Not authorized to reply to this post.";
					}
					DBManager.closeConnection(dbconn);
					return "true";
				}
				else {
					DBManager.closeConnection(dbconn);
					return "print Error: Post does not exist. Refresh. " +
					"If the problem persists, contact an admin.";
				}
			}
			else {
				DBManager.closeConnection(dbconn);
				return "print Error: Region does not exist. Refresh. " +
				"If the problem persists, contact an admin.";
			}
		}
		else {
			DBManager.closeConnection(dbconn);
			return "print Error: Board does not exist. Refresh. " +
			"If the problem persists, contact an admin.";
		}
	}
	
	//ASSUMES authorizedToReply HAS BEEN CALLED
	//SO THAT ALL PARAMETERS ARE VALID, AUTHS ARE VALID.
	public static String createReply(String username, String content, 
			String boardName, String regionName, int postNum) {
		Connection dbconn = DBManager.getConnection();
		String bname = boardName.trim().toLowerCase();
		if (bname.equals("freeforall")) { //regionName might be null
			String msg = SocialNetworkDatabasePosts.createReplyFreeForAll(dbconn, username, content, postNum);
			DBManager.closeConnection(dbconn);
			return msg;
		}
		//regionname not null
		String rname = regionName.trim().toLowerCase();
		String msg = SocialNetworkDatabasePosts.createReply(dbconn, username, content,
				bname, rname, postNum);
		DBManager.closeConnection(dbconn);
		return msg;
	}
	
	public static String viewPost(String username, String boardName, 
			String regionName, int postNum, boolean isGoTo) {
		Connection dbconn = DBManager.getConnection();
		String bname = boardName.trim().toLowerCase();
		if (bname.equals("freeforall")) {
			Boolean postExists = postExists("freeforall", null, postNum);
			if (postExists == null) {
				DBManager.closeConnection(dbconn);
				return "print Error: Database error while verifying existence of post. " +
				"If the problem persists, contact an admin.";
			}
			else if (postExists.booleanValue()) {
				//only need to authorize if it is the first time going to this post
				if (isGoTo) {
					//AUTHORIZATION FUNCTION
					Boolean canView = SocialNetworkDatabasePosts.authorizedFFAPost(dbconn, username, postNum, "view");
					if (canView == null) {
						DBManager.closeConnection(dbconn);
						return "print Error: Database error while checking authorization. If the problem persists, contact an admin.";
					}
					else if (!canView.booleanValue()) {
						DBManager.closeConnection(dbconn);
						return "print Error: Not authorized to view this post.";
					}
				}
				
				String msg = SocialNetworkDatabasePosts.getPostFreeForAll(dbconn, username, postNum);
				DBManager.closeConnection(dbconn);
				return msg;
			}
			else {
				DBManager.closeConnection(dbconn);
				return "print Error: Post does not exist. Refresh. " +
				"If the problem persists, contact an admin.";
			}
		}
		String rname = regionName.trim().toLowerCase();
		Boolean boardExists = SocialNetworkBoards.boardExists(bname);
		if (boardExists == null) {
			DBManager.closeConnection(dbconn);
			return "print Error: Database error while verifying existence of board. " +
					"If the problem persists, contact an admin.";
		}
		else if (boardExists.booleanValue()) {
			Boolean regionExists = SocialNetworkRegions.regionExists(bname, rname);
			if (regionExists == null) {
				DBManager.closeConnection(dbconn);
				return "print Error: Database error while verifying existence of region. " +
						"If the problem persists, contact an admin.";
			}
			else if (regionExists.booleanValue()) {
				Boolean postExists = postExists(bname, rname, postNum);
				if (postExists == null) {
					DBManager.closeConnection(dbconn);
					return "print Error: Database error while verifying existence of post. " +
					"If the problem persists, contact an admin.";
				}
				else if (postExists.booleanValue()) {
					//dont have to check here; view privileges in region => view privs for each post.
					String msg = SocialNetworkDatabasePosts.getPost(dbconn, username,
							bname, rname, postNum);
					DBManager.closeConnection(dbconn);
					return msg;
				}
				else {
					DBManager.closeConnection(dbconn);
					return "print Error: Post does not exist. Refresh. " +
					"If the problem persists, contact an admin.";
				}
			}
			else {
				DBManager.closeConnection(dbconn);
				return "print Error: Region does not exist. Refresh. " +
				"If the problem persists, contact an admin.";
			}
		}
		else {
			DBManager.closeConnection(dbconn);
			return "print Error: Board does not exist. Refresh. " +
			"If the problem persists, contact an admin.";
		}
	}
	
	public static String authorizedToPost (String username,
			String boardName, String regionName) {
		String bname = boardName.trim().toLowerCase();
		Connection dbconn = DBManager.getConnection();
		if (bname.equals("freeforall")) { //regionName might be null
			//everyone can post in the freeforall board
			return "true";
		}
		//regionName should not be null
		String rname = regionName.trim().toLowerCase();
		Boolean boardExists = SocialNetworkBoards.boardExists(bname);
		if (boardExists == null) {
			return "print Error: Database error while verifying existence of board. " +
					"If the problem persists, contact an admin.";
		}
		else if (boardExists.booleanValue()) {
			Boolean regionExists = SocialNetworkRegions.regionExists(bname, rname);
			if (regionExists == null) {
				return "print Error: Database error while verifying existence of region. " +
						"If the problem persists, contact an admin.";
			}
			else if (regionExists.booleanValue()) {
				//AUTHORIZATION FUNCTION
				Boolean authPost = SocialNetworkDatabasePosts.authorizedToPostNotFFA(dbconn, username, boardName, regionName);
				if (authPost == null) {
					DBManager.closeConnection(dbconn);
					return "print Error: Database error while creating the post.";
				}
				else if (!authPost.booleanValue()) {
					DBManager.closeConnection(dbconn);
					return "print Error: Not authorized to post in this region";
				}
				DBManager.closeConnection(dbconn);
				return "true";
			}
			else {
				return "print Error: Region does not exist. Refresh. " +
				"If the problem persists, contact an admin.";
			}
		}
		else {
			return "print Error: Board does not exist. Refresh. " +
			"If the problem persists, contact an admin.";
		}
	}
	
	//THIS FUNCTION ASSUMES EVERYTHING IS VALID/EXISTS
	//(assumed AUTHORIZED TO POST has already been called!)
	public static String createPost(String username, String content, 
			String boardName, String regionName) {
		String bname = boardName.trim().toLowerCase();
		Connection dbconn = DBManager.getConnection();
		if (bname.equals("freeforall")) { //regionName might be null
			//everyone can post in the freeforall board
			String msg = SocialNetworkDatabasePosts.createPostFreeForAll(dbconn, username, content);
			DBManager.closeConnection(dbconn);
			return msg;
		}
		//regionName should not be null
		String rname = regionName.trim().toLowerCase();
		String msg = SocialNetworkDatabasePosts.createPost(dbconn, username, content,
				bname, rname);
		DBManager.closeConnection(dbconn);
		return msg;
	}
	
	public static String viewPostList(String username, String boardName, String regionName, boolean isGoTo) {
		if (boardName == null || (!("freeforall").equals(boardName) && regionName == null)) {
			return "Invalid Call to Function";
		}
		String bname = boardName.trim().toLowerCase();
		Connection dbconn = DBManager.getConnection();
		if (bname.equals("freeforall")) { //regionName might be null
			String msg = SocialNetworkDatabasePosts.getPostListFreeForAll(dbconn, username);
			DBManager.closeConnection(dbconn);
			return msg;
		}
		//regionName is NOT null
		String rname = regionName.trim().toLowerCase();
		Boolean boardExists = SocialNetworkBoards.boardExists(bname);
		if (boardExists == null) {
			return "print Error: Database error while verifying existence of board. " +
					"If the problem persists, contact an admin.";
		}
		else if (boardExists.booleanValue()) {
			Boolean regionExists = SocialNetworkRegions.regionExists(bname, rname);
			if (regionExists == null) {
				return "print Error: Database error while verifying existence of region. " +
						"If the problem persists, contact an admin.";
			}
			else if (regionExists.booleanValue()) {
				//AUTHORIZATION CHECK
				if (isGoTo) {
					Boolean authorized = SocialNetworkDatabaseRegions.authorizedGoToRegion(dbconn, username, boardName, regionName);
					if (authorized == null) {
						return "print Error: Database error while checking authorization. If the problem persists, contact an admin.";
					}
					else if (!authorized.booleanValue()) {
						return "print Error: Not authorized to view this region.";
					}
				}
				String msg = SocialNetworkDatabasePosts.getPostList(dbconn, username,
						bname, rname);
				DBManager.closeConnection(dbconn);
				return msg;
			}
			else {
				return "print Error: Region does not exist. Refresh. " +
				"If the problem persists, contact an admin.";
			}
		}
		else {
			return "print Error: Board does not exist. Refresh. " +
			"If the problem persists, contact an admin.";
		}
	}
}
