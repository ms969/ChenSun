package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SocialNetworkDatabaseRegions {
	//private static int numPostsPerBoard = 2;
	private static String specialStrPostable = "*";
	
	/**
	 * Determine whether the region exists in this board.
	 * ASSUMES THE BOARD EXISTS.
	 */
	public static Boolean regionExists(Connection conn, String boardName, String regionName) {
		String getRegion = "SELECT * FROM " + boardName + ".regions WHERE rname = \"?\"";
		PreparedStatement pstmt = null;
		Boolean regionExists = null;
		try {
			pstmt = conn.prepareStatement(getRegion);
			pstmt.setString(1, regionName);
			regionExists = new Boolean(pstmt.execute());
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			DBManager.closePreparedStatement(pstmt);
		}
		return regionExists;
	}
	
	/**
	 * 
	 * Creates a region under the given board with the given region name.
	 * ASSUMES that the boardName is valid.
	 */
	//TODO (author) ensure the user is an admin for the board.
	public static String createRegion(Connection conn, String username, String boardName, String regionName) {
		PreparedStatement regionPstmt = null;
		String createRegion = "INSERT INTO " + boardName + ".regions VALUES (\"?\")";
		boolean success = false;
		try {
			regionPstmt = conn.prepareStatement(createRegion);
			regionPstmt.setString(1, regionName);
			success =  (regionPstmt.executeUpdate() == 1);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			DBManager.closePreparedStatement(regionPstmt);
		}
		if (success) {
			return "print Region \"" + regionName + "\" successfully created.";
		}
		else {
			return "print Error: Database error while creating the region. Contact the admin.";
		}
	}
	
	/**
	 * Gets a list of regions that the user has access to.
	 * If the user is an admin, the user can see all regions.
	 * Also returns, with each region, the most recently posted posts.
	 * Assumes boardName is not null and is a valid board.
	 * TODO (author) double check that the user can call this method within this board
	 * */
	public static String getRegionListRecentPosts(Connection conn, String username, String boardName){
		String regionsAndPosts = "print Regions:;";
		
		PreparedStatement regionPstmt = null;
		String fetchRegionsMember = "SELECT rname, privileges FROM " +
				boardName + ".regionprivileges " +
				"WHERE username = \"?\"";
		
		Statement regionStmt = null;
		String fetchRegionsAdmin = "SELECT * FROM " +
				boardName + ".regions";
		
		PreparedStatement recentPostsPstmt = null;
		String fetchRecentPost = "SELECT rname, pid, P.postedBy, R.repliedBy, MAX(R.dateReplied)" +
				"FROM " + boardName + ".posts AS P INNER JOIN " +
				boardName + ".replies AS R USING (rname, pid) " +
				"WHERE rname = \"?\"";
		ResultSet regionsResults = null;
		ResultSet recentPostsResults = null;
		
		boolean sqlex = false;
		try {
			String role = SocialNetworkDatabaseBoards.getUserRole(conn, username);
			if (role.equals("member")) {
				regionPstmt = conn.prepareStatement(fetchRegionsMember);
				regionPstmt.setString(1, username);
				regionsResults = regionPstmt.executeQuery();
			}
			else if (!role.equals("")) { //user is an admin
				regionStmt = conn.createStatement();
				regionsResults = regionStmt.executeQuery(fetchRegionsAdmin);
			}
			else { //error occurred while acquiring role
				return "print Error: Database Error while querying viewable regions. Contact an admin.;";
			}
			recentPostsPstmt = conn.prepareStatement(fetchRecentPost);
			while (regionsResults.next()) {
				/*For each region, fetch the two most recent posts*/
				if (role.equals("member")) {
					regionsAndPosts += "print \t" + 
						(regionsResults.getString("privileges").equals("viewpost") ? specialStrPostable : "") +
						regionsResults.getString("rname") + ";";
				}
				else {
					regionsAndPosts += "print \t" + specialStrPostable + regionsResults.getString("rname") + ";";
				}
				recentPostsPstmt.setString(1, regionsResults.getString("rname"));
				recentPostsResults = recentPostsPstmt.executeQuery();
				if (!recentPostsResults.next()) {
					regionsAndPosts += "print \t\tNo Posts for this Region;";
				}
				else {
					regionsAndPosts += "print \t\tMost Recently Updated Post#" + recentPostsResults.getInt("pid") +
					"[" + recentPostsResults.getString("P.postedBy") + "];print \t\t" +
					"Most Recent Reply: [" + recentPostsResults.getString("R.repliedBy") + "] " +
					recentPostsResults.getTimestamp("R.dateReplied").toString() + ";";
				}
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			sqlex = true;
		}
		finally {
			DBManager.closePreparedStatement(regionPstmt);
			DBManager.closeStatement(regionStmt);
			DBManager.closePreparedStatement(recentPostsPstmt);
			DBManager.closeResultSet(regionsResults);
			DBManager.closeResultSet(recentPostsResults);
		}
		if (regionsAndPosts.equals("Regions:\n") && !sqlex) { //boardName assumed to be valid.
			return "print No Regions for this Board";
		}
		else if (sqlex) {
			return "print Error: Database Error while querying viewable regions. Contact an admin.;";
		}
		else return regionsAndPosts;
	}
}
