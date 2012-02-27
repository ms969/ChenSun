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
					posts += "\t" + 
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
		if (posts.equals("print Posts:;") && !sqlex) {
			return "print No Posts for this Region";
		}
		else if (sqlex) {
			return "print Database Error while querying viewable posts. Contact an admin.";
		}
		else return posts;
	}
	
	public static String getPostFreeForAll(Connection conn, int postNum) {
		String getOriginalPost;
		String getReplies;
		return "";
	}
	
	public static String getPost(Connection Conn, String boardName, 
			String regionName, int postNum) {
		String getOriginalPost;
		String getReplies;
		return "";
	}
}
