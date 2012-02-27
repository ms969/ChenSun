package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SocialNetworkDatabasePosts {
	private static String specialStrPostable = "*";
	
	/**
	 * Returns the list of posts within the Free For All board that the
	 * specified user can see.
	 * Different from a regular board because you must check each post
	 * one by one to ensure that the user has a privilege for it.
	 */
	public static String getPostListFreeForAll(Connection conn, String username) {
		String posts = "print Posts:;";
		
		PreparedStatement getPrivs = null;
		String getPostPrivileges = "SELECT pid, privilege " +
				"FROM freeforall.postprivileges " +
				"WHERE username = \"?\"";
		ResultSet privsResult = null;
		
		PreparedStatement getPost = null;
		String getPostFreeForAll = "SELECT pid, P.postedBy, R.repliedBy, MAX(R.dateReplied) " +
			"FROM freeforall.posts AS P INNER JOIN " + 
			"freeforall.replies as R USING (pid) " +
			"WHERE pid = ?";
		ResultSet post = null;
		
		boolean sqlex = false;
		try {
			getPrivs = conn.prepareStatement(getPostPrivileges);
			getPost = conn.prepareStatement(getPostFreeForAll);
			getPrivs.setString(1, username);
			privsResult = getPrivs.executeQuery();
			int pid;
			while (privsResult.next()) {
				pid = privsResult.getInt("pid");
				getPost.setInt(1, pid);
				post = getPost.executeQuery();
				/*Only expect one result set*/
				if (post.next()) {
					posts += "print \t" + 
						(privsResult.getString("privilege").equals("viewpost")? specialStrPostable : "") +
						"Post#" + pid + "[" + post.getString("P.postedBy") + "]; print \t" +
						"Most Recent Reply: [" + post.getString("R.repliedBy") + "]" +
						post.getTimestamp("R.dateReplied").toString() + ";";
				}
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			sqlex = true;
		}
		finally {
			
		}
		if (posts.equals("print Posts:;") && !sqlex) {
			return "print No posts for this board.";
		}
		else if (sqlex) {
			return "print Database Error while querying viewable posts. Contact an admin.";
		}
		else {
			return posts;
		}
	}
	
	/** 
	 * Gets the post list for the given region.
	 * The board is assumed not to be the free for all board.
	 * Assumes all parameters are valid (boardName and regionName especially)
	 * TODO (author) ensure that the user can view the posts for this region
	 */
	public static String getPostList(Connection conn, String username, String boardName, String regionName) {
		PreparedStatement pstmt = null;
		String posts = "print Posts:;";
		String getPosts = "SELECT rname, pid, P.postedBy, R.repliedBy, MAX(R.dateReplied) " +
			"FROM " + boardName +  ".posts AS P INNER JOIN " + 
			boardName + ".replies as R USING (rname, pid) " +
			"WHERE rname = \"?\" " +
			"GROUP BY pid ORDER BY R.dateReplied DESC";
		ResultSet postsResults = null;
		boolean sqlex = false;
		try {
			pstmt = conn.prepareStatement(getPosts);
			pstmt.setString(1, regionName);
			postsResults = pstmt.executeQuery();
			while (postsResults.next()) {
				posts += "print \tPost#" + postsResults.getInt("pid") + 
				"[" + postsResults.getString("P.postedBy") + "]; print \t" +
				"Most Recent Reply: [" + postsResults.getString("R.repliedBy") + "] " +
				postsResults.getTimestamp("R.dateReplied").toString() + ";";
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
			return "print Database Error while querying viewable posts. Contact an admin.";
		}
		else return posts;
	}
	
	/*You can regulate who sees what*/
	//TODO have to put SELF in PostPrivileges for free for all
	public static String createPostFreeForAll(Connection conn, String username, String content) {
		return "";
	}
	
	public static String createPost(Connection conn, String boardName, 
			String regionName, String username, String content) {
		return "";
	}
	
	public static String createReplyFreeForAll(Connection conn, int postNum, String username, String content) {
		return "";
	}
	
	public static String createReply(Connection conn, String boardName, 
			String regionName, int postNum, String username, String content) {
		return "";
	}
	
	/** Gets a post from the free for all board designated
	 * by the post number.
	 * postNum is assumed to be an accurate post number.
	 */
	//TODO (author) ensure that the user has access to this post.
	public static String getPostFreeForAll(Connection conn, int postNum) {
		return getPost(conn, "freeforall", "", postNum);
	}
	
	/** Gets a post from the designated board and region
	 *  with the given post number.
	 *  ASSUMES that the board, region, and post are all valid.
	 */
	//TODO (author) ensure that the user has access to the encapsulating region.
	public static String getPost(Connection conn, String boardName, 
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
			"WHERE pid = ? AND rname = \"?\"";
			getReplies = "SELECT * FROM " + boardName + ".replies " +
			"WHERE pid = ? AND rname = \"?\" ORDER BY dateReplied";
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
				postAndReplies += 
					"print ----- Post# " + postNum + "[" + postResult.getString("postedBy") + "]----- " +
					postResult.getTimestamp("datePosted").toString() + "; print \t" +
					postResult.getString("content") + ";";
				
				repliesResult = replies.executeQuery();
				while (repliesResult.next()) { //Print out all replies
					postAndReplies += "print ----- Reply[" + repliesResult.getString("repliedBy") + "] ----- " +
					repliesResult.getTimestamp("dateReplied").toString() + ";print \t" +
					repliesResult.getString("content") + ";";
				}
			}
			// if there's no postResult, the post DNE.
		}
		catch (SQLException e) {
			e.printStackTrace();
			sqlex = true;
		}
		if (postAndReplies.equals("") && !sqlex) {
			return "print Post does not exist.";
		}
		else if (sqlex) {
			return "print Database error while querying post and replies. Contact an admin.";
		}
		else return postAndReplies;
	}
}
