package database;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import crypto.CryptoUtil;
import crypto.Hash;
import crypto.SharedKeyCrypto;

public class SocialNetworkDatabasePosts {
	private static String specialStrPostable = "*";
	private static String specialStrCreatedPost = "**";
	
	/**
	 * Verifies whether a post exists in the given board (and region).
	 */
	public static Boolean postExists(Connection conn, String boardName, String regionName, int postNum) {
		PreparedStatement pstmt = null;
		ResultSet postResult = null;
		String getPost = "";
		if (boardName.equals("freeforall")) {
			getPost = "SELECT * FROM freeforall.posts " +
			"WHERE pid = ?";
		}
		else {
			getPost = "SELECT * FROM " + boardName + ".posts " +
					"WHERE pid = ? AND rname = ?";
		}
		
		Boolean postExists = null;
		try {
			pstmt = conn.prepareStatement(getPost);
			pstmt.setInt(1, postNum);
			if (!boardName.equals("freeforall")) {
				pstmt.setString(2, regionName);
			}
			postResult = pstmt.executeQuery();
			postExists = new Boolean(postResult.next());
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getSQLState());
		}
		finally {
			DBManager.closeResultSet(postResult);
			DBManager.closePreparedStatement(pstmt);
		}
		return postExists;
	}
	
	/**
	 * AUTHORIZATION FUNCTION
	 * Returns whether this user can go to/reply under the specified post # in the FFA board.
	 * The user must be the post's author or have a privilege in the post.
	 * Assumes post already exists.
	 * AuthType = "view" or "reply". For view, merely checks that a priv exists.
	 * For reply, checks that the priv is "viewpost"
	 */
	public static Boolean authorizedFFAPost(Connection conn, String username, int postNum, String authType) {
		
		Boolean isPostCreator = isFFAPostCreator(conn, username, postNum);
		if (isPostCreator == null) {
			return null;
		}
		else if (isPostCreator.booleanValue()) {
			return new Boolean(true);
		}
		else {		
			/*Retrieve the privilege for a given post and user*/
			PreparedStatement getPriv = null;
			String getPrivString = "SELECT privilege " +
					"FROM freeforall.postprivileges " +
					"WHERE pid = ? AND username = ?";
			ResultSet privResult = null;
			
			Boolean authorized = null;
			
			try {
				getPriv = conn.prepareStatement(getPrivString);
				getPriv.setInt(1, postNum);
				getPriv.setString(2, username);
				
				privResult = getPriv.executeQuery();
				
				//the privilege is at least View
				if (authType.equals("view")) {
					authorized = new Boolean(privResult.next());
				}
				else if (authType.equals("reply")) {
					if (privResult.next()) {
						authorized = new Boolean(privResult.getString("privilege").equals("viewpost"));
					}
					else {
						authorized = new Boolean(false);
					}
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
			finally {
				DBManager.closeResultSet(privResult);
				DBManager.closePreparedStatement(getPriv);
			}
			return authorized;
		}
	}
	
	/**
	 * AUTHORIZATION FUNCTION
	 * Returns whether the user can post under the specified region of this board.
	 * Assumes the board and region are valid, and board =/= freeforall
	 * For regular mem's are authorized to post if you have post privileges under this region
	 * For admins, you must be an admin of the board.
	 * 
	 * This doubles as both the POSTING AND REPLYING Authorization Function
	 */
	public static Boolean authorizedToPostNotFFA(Connection conn, String username, String boardName, String regionName) {	
		/*Retrieve the privilege for the given user and region*/
		PreparedStatement privPstmt = null;
		String getPrivMemberString = "SELECT privilege " +
		"FROM " + boardName + ".regionprivileges " +
		"WHERE rname = ? AND username = ?";
		ResultSet privResult = null;
		
		String getPrivAdminString = "SELECT * FROM main.boardadmins WHERE bname = ? AND username = ?";

		Boolean authorized = null;

		try {
			String role = DatabaseAdmin.getUserRole(conn, username);
			if (role.equals("member")) {
				privPstmt = conn.prepareStatement(getPrivMemberString);
				privPstmt.setString(1, regionName);
			}
			else if (!role.equals("")) {
				privPstmt = conn.prepareStatement(getPrivAdminString);
				privPstmt.setString(1, boardName);
			}
			privPstmt.setString(2, username);

			privResult = privPstmt.executeQuery();

			//the privilege is at least View
			if (privResult.next()) {
				if (role.equals("member")) {
					authorized = new Boolean(privResult.getString("privilege").equals("viewpost"));
				}
				else {
					authorized = new Boolean(true);
				}
			}
			else {
				authorized = new Boolean(false);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			DBManager.closeResultSet(privResult);
			DBManager.closePreparedStatement(privPstmt);
		}
		return authorized;
	}
	
	public static Boolean isFFAPostCreator(Connection conn, String username, int post) {
		Boolean isCreator = null;
		String query = "SELECT postedby FROM freeforall.posts " +
				"WHERE pid = ?";
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, post);
			result = pstmt.executeQuery();
			if (result.next()) {
				isCreator = new Boolean(username.equals(result.getString("postedby")));
			}
		} catch (SQLException e) {
			isCreator = null;
		} finally {
			DBManager.closeResultSet(result);
			DBManager.closePreparedStatement(pstmt);
		}
		return isCreator;
	}
	
	/**
	 * Returns the list of posts within the Free For All board that the
	 * specified user can see.
	 * 
	 * A User can see a post if they are:
	 *   - the creator of the post (username == postedBy)
	 *   - granted view privilege in PostsPrivileges
	 * 
	 * Different from a regular board because you must check each post
	 * one by one to ensure that the user has the privilege for it.
	 */
	public static String getPostListFreeForAll(Connection conn, String username) {
		String posts = "print Posts:;";
		
		/*Retrieves all posts, joined with their most recent reply*/
		Statement getPosts = null;
		String getPostsFreeForAll = "SELECT pid, P.postedBy, P.datePosted, P.dateLastUpdated, MAX(R.dateReplied) " +
			"FROM freeforall.posts AS P LEFT OUTER JOIN " + 
			"freeforall.replies as R USING (pid) " +
			"GROUP BY pid ORDER BY P.dateLastUpdated DESC";
		ResultSet postsResults = null;
		
		/*Retrieves the privilege for a given post and user*/
		PreparedStatement getPrivs = null;
		String getPostPrivileges = "SELECT privilege " +
				"FROM freeforall.postprivileges " +
				"WHERE pid = ? AND username = ?";
		ResultSet privsResult = null;
		
		boolean sqlex = false;
		try {
			getPrivs = conn.prepareStatement(getPostPrivileges);
			getPosts = conn.createStatement();
			postsResults = getPosts.executeQuery(getPostsFreeForAll);
			
			int pid;
			String postedBy;
			while (postsResults.next()) {
				pid = postsResults.getInt("pid");
				postedBy = postsResults.getString("P.postedBy");
				if (!postedBy.equals(username)) {
					getPrivs.setInt(1, pid);
					getPrivs.setString(2, username);
					privsResult = getPrivs.executeQuery();
					/*Only expect one result set*/
					if (privsResult.next()) { //user has view or viewpost priv
						posts += "print \t" + 
						(privsResult.getString("privilege").equals("viewpost")? specialStrPostable : "") +
						"Post#" + pid + "[" + postsResults.getString("P.postedBy") + "];";
						if (postsResults.getTimestamp("MAX(R.dateReplied)") != null) {
							posts += "print \t\t" +
							"Most Recent Reply at " +
							postsResults.getTimestamp("MAX(R.dateReplied)") + ";";
						}
					}
				}
				else { //the user is the creator of the post
					posts += "print \t" + specialStrCreatedPost +
					"Post#" + pid + "[" + postsResults.getString("P.postedBy") + "];";
					if (postsResults.getTimestamp("MAX(R.dateReplied)") != null) {
						posts += "print \t\t" +
						"Most Recent Reply at " +
						postsResults.getTimestamp("MAX(R.dateReplied)") + ";";
					}
				}
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			sqlex = true;
		}
		finally {
			DBManager.closeStatement(getPosts);
			DBManager.closeResultSet(postsResults);
			DBManager.closePreparedStatement(getPrivs);
			DBManager.closeResultSet(privsResult);
		}
		if (posts.equals("print Posts:;") && !sqlex) {
			return "print No posts for this board.";
		}
		else if (sqlex) {
			return "print Error: Database Error while querying viewable posts. Contact an admin.";
		}
		else {
			return posts;
		}
	}
	
	/** 
	 * Gets the post list for the given region.
	 * The board is assumed not to be the free for all board.
	 * Assumes all parameters are valid (boardName and regionName especially)
	 * 
	 */
	public static String getPostList(Connection conn, String username, String boardName, String regionName) {
		PreparedStatement pstmt = null;
		String posts = "print Posts:;";
		String getPosts = "SELECT rname, pid, P.postedBy, P.datePosted, P.dateLastUpdated, MAX(R.dateReplied) " +
			"FROM " + boardName +  ".posts AS P LEFT OUTER JOIN " + 
			boardName + ".replies as R USING (rname, pid) " +
			"WHERE rname = ? " +
			"GROUP BY pid ORDER BY P.dateLastUpdated DESC";
		ResultSet postsResults = null;
		boolean sqlex = false;
		try {
			pstmt = conn.prepareStatement(getPosts);
			pstmt.setString(1, regionName);
			postsResults = pstmt.executeQuery();
			while (postsResults.next()) {
				posts += "print \tPost#" + postsResults.getInt("pid") + 
				"[" + postsResults.getString("P.postedBy") + "];";
				if (postsResults.getTimestamp("MAX(R.dateReplied)") != null) {
					posts += "print \t\t" +
					"Most Recent Reply at " +
					postsResults.getTimestamp("MAX(R.dateReplied)") + ";";
				}
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			sqlex = true;
		}
		finally {
			DBManager.closePreparedStatement(pstmt);
			DBManager.closeResultSet(postsResults);
		}
		if (posts.equals("print Posts:;") && !sqlex) { //board and region assumed to be valid
			return "print No Posts for this Region";
		}
		else if (sqlex) {
			return "print Error: Database Error while querying viewable posts. Contact an admin.";
		}
		else return posts;
	}
	
	//
	public static String createPostFreeForAll(Connection conn, String username, String content) {
		return createPost(conn, username, content, "freeforall", null);
	}
	
	/**
	 * Inserts the post into the database, then tries its best to
	 * return the pid that contains the post.
	 * Assumes the board and region are correct (unless the board is freeforall)
	 * Does NOT do "Post Privileges" processing.
	 * 
	 *
	 */
	public static String createPost(Connection conn, String username, String contentRaw, 
			String boardName, String regionName) {
		String content = SharedKeyCrypto.encrypt(contentRaw);
		PreparedStatement createPstmt = null;
		String createPost = "";
		
		/*Have to retrieve the pid that is generated for the post*/
		PreparedStatement getPstmt = null;
		String getPost = "";
		ResultSet getResult = null;
		
		PreparedStatement getDatePstmt = null;
		String getMaxDate = "";
		ResultSet dateResult = null;
		
		if (boardName.equals("freeforall")) {
			createPost = "INSERT INTO freeforall.posts " +
					"VALUES (null, ?, NOW(), ?, NOW(), ?)";
			getMaxDate = "SELECT MAX(datePosted) FROM freeforall.posts " +
					"WHERE postedBy = ? AND content = ?";
			getPost = "SELECT pid, datePosted FROM freeforall.posts " +
					"WHERE postedBy = ? AND content = ? AND datePosted = ?";
		}
		else {
			createPost = "INSERT INTO " + boardName + ".posts " +
					"VALUES (?, null, ?, NOW(), ?, NOW(), ?)";
			getMaxDate = "SELECT MAX(datePosted) FROM " + boardName + ".posts " +
			"WHERE rname = ? AND postedBy = ? AND content = ?";
			getPost = "SELECT pid, datePosted FROM " + boardName + ".posts " +
					"WHERE rname = ? AND postedBy = ? AND content = ? AND datePosted = ?";
		}
		
		
		boolean sqlex = false;
		boolean success = false;
		try {
			createPstmt = conn.prepareStatement(createPost);
			byte[] contentBytes = null;
			try {
				contentBytes = contentRaw.getBytes("UTF8");
			} catch (UnsupportedEncodingException e) {
				//this should not happen
			}
			String checksum = CryptoUtil.encode(Hash.generateChecksum(contentBytes));
			Arrays.fill(contentBytes, (byte)0x00);
			
			if (boardName.equals("freeforall")) {
				createPstmt.setString(1, username);
				createPstmt.setString(2, content);
				createPstmt.setString(3, checksum);
			}
			else {
				createPstmt.setString(1, regionName);
				createPstmt.setString(2, username);
				createPstmt.setString(3, content);
				createPstmt.setString(4, checksum);
			}
			success = (createPstmt.executeUpdate() == 1);
			
		}
		catch (SQLException e) {
			e.printStackTrace();
			sqlex = true;
		}
		finally {
			DBManager.closePreparedStatement(createPstmt);
		}
		if (sqlex) {
			return "print Error: Database error while inserting the post. Contact an admin.";
		}
		else if (success) {
			/*Try to retrieve the pid for the user to reference*/
			Integer pid = null;
			try {
				getDatePstmt = conn.prepareStatement(getMaxDate);
				getPstmt = conn.prepareStatement(getPost);
				if (boardName.equals("freeforall")) {
					getPstmt.setString(1, username);
					getPstmt.setString(2, content);
					getDatePstmt.setString(1, username);
					getDatePstmt.setString(2, content);
				}
				else {
					getPstmt.setString(1, regionName);
					getPstmt.setString(2, username);
					getPstmt.setString(3, content);
					getDatePstmt.setString(1, regionName);
					getDatePstmt.setString(2, username);
					getDatePstmt.setString(3, content);
				}
				dateResult = getDatePstmt.executeQuery();
				if (dateResult.next()) {
					if (boardName.equals("freeforall")) {
						getPstmt.setTimestamp(3, dateResult.getTimestamp("MAX(datePosted)"));
					}
					else {
						
						getPstmt.setTimestamp(4, dateResult.getTimestamp("MAX(datePosted)"));
					}
					getResult = getPstmt.executeQuery();
					if (getResult.next()) { //There should be at most one result... just inserted!
						pid = new Integer(getResult.getInt("pid"));
					}
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
				sqlex = true;
			}
			finally {
				DBManager.closePreparedStatement(getPstmt);
				DBManager.closeResultSet(getResult);
			}
			if (pid == null || sqlex) {
				return "print Post successfully added (post num cannot be retrieved).;" +
						"print Don't forget to give people permission to view/reply to it!";
			}
			else {
				return "print Post#" + pid.intValue() + " successfully added.;" +
						"print Don't forget to give people permission to view/reply to it!";
			}
		}
		else { //not successful
			return "print Error: Post could not be uploaded. If this problem persists, contact an admin.";
		}
	}
	
	public static String createReplyFreeForAll(Connection conn, String username, String content, 
			int postNum) {
		return createReply(conn, username, content, "freeforall", null, postNum);
	}
	
	/**
	 * Inserts the reply for the given post.
	 * Updates the originating post's dateLastUpdated
	 * Assumes the board, the region, and the post are valid.
	 */
	public static String createReply(Connection conn, String username, String contentRaw, 
			String boardName, String regionName, int postNum) {
		String content = SharedKeyCrypto.encrypt(contentRaw);
		PreparedStatement createPstmt = null;
		String createReply = "";
		
		PreparedStatement getDatePstmt = null;
		String getDate = "";
		ResultSet dateResult = null;
		
		PreparedStatement updateDatePstmt = null;
		String updateDate = "";
		
		if (boardName.equals("freeforall")) {
			createReply = "INSERT INTO freeforall.replies " +
			"VALUES (?, null, ?, NOW(), ?, ?)";
			getDate = "SELECT MAX(dateReplied) FROM freeforall.replies " +
			"WHERE pid = ?";
			updateDate = "UPDATE freeforall.posts SET dateLastUpdated = ? " +
					"WHERE pid = ?";
		}
		else {
			createReply = "INSERT INTO " + boardName + ".replies " +
			"VALUES (?, ?, null, ?, NOW(), ?, ?)";
			getDate = "SELECT MAX(dateReplied) FROM " + boardName + ".replies " +
			"WHERE pid = ? AND rname = ?";
			updateDate = "UPDATE " + boardName + ".posts SET dateLastUpdated = ? " +
			"WHERE pid = ? AND rname = ?";
		}
		
		boolean successInsert = false;
		boolean successUpdate = false;
		boolean sqlex = false;
		try {
			conn.setAutoCommit(false);
			createPstmt = conn.prepareStatement(createReply);
			
			//calculate a checksum for the content
			byte[] contentBytes = null;
			try {
				contentBytes = contentRaw.getBytes("UTF8");
			} catch (UnsupportedEncodingException e) {//should not happen
			}
			
			String checksum = CryptoUtil.encode(Hash.generateChecksum(contentBytes));
			Arrays.fill(contentBytes, (byte)0x00);
			
			if (boardName.equals("freeforall")) {
				createPstmt.setInt(1, postNum);
				createPstmt.setString(2, username);
				createPstmt.setString(3, content);
				createPstmt.setString(4, checksum);
			}
			else {
				createPstmt.setString(1 , regionName);
				createPstmt.setInt(2, postNum);
				createPstmt.setString(3, username);
				createPstmt.setString(4, content);
				createPstmt.setString(5, checksum);
			}
			successInsert = (createPstmt.executeUpdate() == 1);
			if (successInsert) {
				/* Get the timestamp of the most recent reply!*/
				getDatePstmt = conn.prepareStatement(getDate);
				getDatePstmt.setInt(1, postNum);
				if (!boardName.equals("freeforall")) {
					getDatePstmt.setString(2, regionName);
				}
				dateResult = getDatePstmt.executeQuery();
				if (dateResult.next()) {//only expect one result, the max.
					/*Update the record with this time*/
					updateDatePstmt = conn.prepareStatement(updateDate);
					updateDatePstmt.setTimestamp(1, dateResult.getTimestamp("MAX(dateReplied)"));
					updateDatePstmt.setInt(2, postNum);
					if (!boardName.equals("freeforall")) {
						updateDatePstmt.setString(3, regionName);
					}
					successUpdate = (updateDatePstmt.executeUpdate() == 1);
					if (successUpdate) {
						conn.commit();
					}
					else {
						conn.rollback();
					}
				}
				else {
					conn.rollback();
				}
			}
			else {
				conn.rollback();
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getSQLState());
			DBManager.rollback(conn);
			sqlex = true;
		}
		finally {
			DBManager.trueAutoCommit(conn);
			DBManager.closePreparedStatement(createPstmt);
			DBManager.closePreparedStatement(getDatePstmt);
			DBManager.closePreparedStatement(updateDatePstmt);
			DBManager.closeResultSet(dateResult);
		}
		if (!successInsert || !successUpdate || sqlex) {
			return "print Error: Database error while inserting reply. Contact an admin";
		}
		else if (successInsert && successUpdate) {
			return "print Reply successfully added. Refresh the post to view";
		}
		else {
			return "print Error: Reply could not be uploaded. If this problem persists, contact an admin";
		}
	}
	
	/** Gets a post from the free for all board designated
	 * by the post number.
	 * postNum is assumed to be an accurate post number.
	 */
	public static String getPostFreeForAll(Connection conn, String username, int postNum) {
		return getPost(conn, username, "freeforall", "", postNum);
	}
	
	/** Gets a post from the designated board and region
	 *  with the given post number.
	 *  ASSUMES that the board, region, and post are all valid.
	 */
	public static String getPost(Connection conn, String username, String boardName, 
			String regionName, int postNum) {
		String getOriginalPost = "";
		String getReplies = "";
		String postAndReplies = "";
		
		/*No joining of results because of redundancy of data returned*/
		if (boardName.equals("freeforall")) {
			getOriginalPost = "SELECT * FROM freeforall.posts " +
			"WHERE pid = ?";
			getReplies = "SELECT * FROM freeforall.replies " +
			"WHERE pid = ? ORDER BY dateReplied ASC";
		}
		else {
			getOriginalPost = "SELECT * FROM " + boardName + ".posts " +
			"WHERE pid = ? AND rname = ?";
			getReplies = "SELECT * FROM " + boardName + ".replies " +
			"WHERE pid = ? AND rname = ? ORDER BY dateReplied ASC";
		}
		
		PreparedStatement originalPost = null;
		ResultSet postResult = null;
		
		PreparedStatement replies = null;
		ResultSet repliesResult = null;
		
		boolean sqlex = false;
		try {
			originalPost = conn.prepareStatement(getOriginalPost);
			replies = conn.prepareStatement(getReplies);
			originalPost.setInt(1, postNum);
			replies.setInt(1, postNum);
			if (!boardName.equals("freeforall")) {
				originalPost.setString(2, regionName);
				replies.setString(2, regionName);
			}
			
			postResult = originalPost.executeQuery();
			if (postResult.next()) { /*Only expect one post result*/
				//Make sure the checksum is correct
				if (!Arrays.equals(
						Hash.generateChecksum((SharedKeyCrypto.decrypt(postResult.getString("content"))).getBytes("UTF8")),
						CryptoUtil.decode(postResult.getString("checksum")))) {
					postAndReplies +=
							"print ----- Post# " + postNum + "[" + postResult.getString("postedBy") + "]----- " +
									postResult.getTimestamp("datePosted").toString() + ";print \t" +
									"Content could not be fetched -- Integrity Failure!" + ";";
				}
				else {
					postAndReplies += 
						"print ----- Post# " + postNum + "[" + postResult.getString("postedBy") + "]----- " +
						postResult.getTimestamp("datePosted").toString() + ";print \t" +
						SharedKeyCrypto.decrypt(postResult.getString("content")) + ";";
				}
				
				repliesResult = replies.executeQuery();
				while (repliesResult.next()) { //Print out all replies
					//for each reply, make sure the checksum is correct.
					if(!Arrays.equals(
							Hash.generateChecksum((SharedKeyCrypto.decrypt(repliesResult.getString("content"))).getBytes("UTF8")),
							CryptoUtil.decode(repliesResult.getString("checksum")))) {
						postAndReplies += "print ----- Reply[" + repliesResult.getString("repliedBy") + "] ----- " +
						repliesResult.getTimestamp("dateReplied").toString() + ";print \t" +
						"Content could not be fetched -- Integrity Failure!" + ";";
					}
					else {
						postAndReplies += "print ----- Reply[" + repliesResult.getString("repliedBy") + "] ----- " +
						repliesResult.getTimestamp("dateReplied").toString() + ";print \t" +
						SharedKeyCrypto.decrypt(repliesResult.getString("content")) + ";";
					}
				}
			}
			// if there's no postResult, the post DNE.
		}
		catch (SQLException e) {
			e.printStackTrace();
			sqlex = true;
		} catch (UnsupportedEncodingException e) {
			// This should not happen.
		} finally {
			DBManager.closeResultSet(postResult);
			DBManager.closePreparedStatement(originalPost);
			DBManager.closeResultSet(repliesResult);
			DBManager.closePreparedStatement(replies);
		}
		if (postAndReplies.equals("") && !sqlex) {
			return "print Error: Post does not exist. Refresh. If the problem persists, contact an admin.";
		}
		else if (sqlex) {
			return "print Error: Database error while querying post and replies. Contact an admin.";
		}
		else return postAndReplies;
	}
	
	public static int addFFAParticipipant(Connection conn, int post, String username, String priv) {
		int status = 0;
		String query = "INSERT INTO freeforall.postprivileges (pid, username, privilege) " +
				"VALUES (?, ?, ?)";
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, post);
			pstmt.setString(2, username);
			pstmt.setString(3, priv);
			status = pstmt.executeUpdate();
		} catch (SQLException e) {
			status = 0;
		} finally {
			DBManager.closePreparedStatement(pstmt);
		}
		return status;
	}
}
