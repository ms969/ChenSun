package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shared.ProjectConfig;

public class DatabaseAdmin {
	private static final boolean DEBUG = ProjectConfig.DEBUG;

	/**
	 * Returns a Map of a cappella groups with aid as key and 
	 * aname as value. If error, return null.
	 * @param conn
	 * @return HashMap of groups
	 */
	public static Map<Integer, String> getGroupList(Connection conn) {
		Map<Integer, String> groupList = new HashMap<Integer, String>();
		Statement stmt = null;
		ResultSet groups = null;
		String query = "SELECT aid, aname FROM main.acappella";
		try {
			stmt = conn.createStatement();
			groups = stmt.executeQuery(query);
			while (groups.next()) {
				groupList.put(groups.getInt("aid"), groups.getString("aname"));
			}
		} catch (SQLException e) {
			return null;
		} finally {
			DBManager.closeResultSet(groups);
			DBManager.closeStatement(stmt);
		}
		return groupList;
	}

	/**
	 * Adds a user to the user table with the given username, pwhash and aid. 
	 * Its role is automatically set to member.
	 * @param conn
	 * @param username
	 * @param pwhash
	 * @param aid
	 * @return 1 if successfully added.
	 */
	public static int addUser(Connection conn, String username, String pwhash, int aid) {
		int status = 0;
		String query = "INSERT INTO main.users (username, pwhash, aid, role) " +
				"VALUE (?, ?, ?, 'member')";
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, username);
			pstmt.setString(2, pwhash);
			pstmt.setInt(3, aid);
			status = pstmt.executeUpdate();
		} catch (SQLException e) {
			if (DEBUG) e.printStackTrace();
			status = 0;
		} finally {
			DBManager.closePreparedStatement(pstmt);
		}
		return status;
	}
	
	public static int deleteUser(Connection conn, String username) {
		int status = 0;
		String query = "DELETE FROM main.users WHERE username = ?";
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, username);
			status = pstmt.executeUpdate();
		} catch (SQLException e) {
			if (DEBUG) e.printStackTrace();
			status = 0;
		} finally {
			DBManager.closePreparedStatement(pstmt);
		}
		return status;
	}

	/**
	 * return an array with the user's info. 
	 * userInfo[0] = username
	 * userInfo[1] = pwhash
	 * userInfo[2] = a cappella name 
	 * userInfo[3] = role
	 * 
	 * if user does not exist return null
	 */
	public static String[] getUserInfo(Connection conn, String user) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		String[] userInfo = new String[4];
		String query = "SELECT username, pwhash, aname, role FROM main.users NATURAL JOIN "
				+ "main.acappella WHERE username = ?";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, user);
			result = pstmt.executeQuery();
			if (result.next()) {
				userInfo[0] = result.getString("username");
				userInfo[1] = result.getString("pwhash");
				userInfo[2] = result.getString("aname");
				userInfo[3] = result.getString("role");
			} else {
				userInfo = null;
			}
		} catch (SQLException e) {
			userInfo = null;
		} finally {
			DBManager.closePreparedStatement(pstmt);
			DBManager.closeResultSet(result);
		}
		return userInfo;
	}
	
	/**
	 * Utility function placed here for now...
	 * Function to get the user's role.
	 */
	public static String getUserRole(Connection conn, String username) {
		String userRole = "SELECT role FROM main.users WHERE username = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String role = "";
		try {
			pstmt = conn.prepareStatement(userRole);
			pstmt.setString(1, username);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				role = rs.getString("role");
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			DBManager.closePreparedStatement(pstmt);
			DBManager.closeResultSet(rs);
		}
		return role;
	}
	
	public static boolean isAdmin(Connection conn, String user) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		String query = "SELECT role FROM main.users WHERE username = ?";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, user);
			result = pstmt.executeQuery();
			if (result.next()) {
				String role = result.getString("role");
				if (role.equals("admin") || role.equals("sa")) {
					return true;
				}
			}
		} catch (SQLException e) {
			return false;
		} finally {
			DBManager.closePreparedStatement(pstmt);
			DBManager.closeResultSet(result);
		}
		return false;
	}
	
	public static boolean isSA(Connection conn, String user) {
		PreparedStatement pstmt = null;
		ResultSet result = null;
		String query = "SELECT role FROM main.users WHERE username = ?";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, user);
			result = pstmt.executeQuery();
			if (result.next()) {
				String role = result.getString("role");
				if (role.equals("sa")) {
					return true;
				}
			}
		} catch (SQLException e) {
			return false;
		} finally {
			DBManager.closePreparedStatement(pstmt);
			DBManager.closeResultSet(result);
		}
		return false;
	}
	
	/**
	 * Retrieves a list of all users of a group given the group's aid. Returns null 
	 * if error.
	 * @param conn
	 * @param aid
	 * @return 
	 */
	public static List<String> getAllUsersOfGroup(Connection conn, int aid) {
		List<String> users = new ArrayList<String>();
		String query = "SELECT username FROM main.users WHERE aid = ?";
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setInt(1, aid);
			result = pstmt.executeQuery();
			while (result.next()) {
				users.add(result.getString("username"));
			}
			if (users.size() == 0) {
				users = null;
			}
		} catch (SQLException e) {
			if (DEBUG) e.printStackTrace();
			users = null;
		} finally {
			DBManager.closeResultSet(result);
			DBManager.closePreparedStatement(pstmt);
		}
		return users;
	}

	/**
	 * user[0] = username
	 * user[1] = role
	 * @param conn
	 * @param username
	 * @return
	 */
	public static List<String[]> getOtherUsersInGroup(Connection conn, String username) {
		List<String[]> users = new ArrayList<String[]>();
		String query = "SELECT username, role FROM main.users "
				+ "WHERE username != ? AND aid = "
				+ "(SELECT aid FROM main.users WHERE username = ?)";
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, username);
			pstmt.setString(2, username);
			result = pstmt.executeQuery();
			while (result.next()) {
				String[] userInfo = {result.getString("username"), result.getString("role")};
				users.add(userInfo);
			}
		} catch (SQLException e) {
			users = null;
		} finally {
			DBManager.closeResultSet(result);
			DBManager.closePreparedStatement(pstmt);
		}
		return users;
	}
	
	public static List<String> getAdminsOfGroup(Connection conn, String username) {
		List<String> admins = new ArrayList<String>();
		String query = "SELECT username FROM main.users "
				+ "WHERE username != ? AND aid = "
				+ "(SELECT aid FROM main.users WHERE username = ?) AND role = 'admin'";
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, username);
			pstmt.setString(2, username);
			result = pstmt.executeQuery();
			while (result.next()) {
				admins.add(result.getString("username"));
			}
		} catch (SQLException e) {
			if (DEBUG) e.printStackTrace();
			admins = null;
		} finally {
			DBManager.closeResultSet(result);
			DBManager.closePreparedStatement(pstmt);
		}
		return admins;
	}

	/**
	 * Returns a list of the user's friends. Returns null if error
	 * @param conn
	 * @param user
	 * @return
	 */
	public static List<String> getFriends(Connection conn, String user) {
		List<String> friends = new ArrayList<String>();
		PreparedStatement pstmt = null;
		ResultSet results = null;
		String query = "SELECT * FROM main.friends " +
				"WHERE username1 = ? OR username2 = ?";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, user);
			pstmt.setString(2, user);
			results = pstmt.executeQuery();
			while (results.next()) {
				if (results.getString("username1").equals(user)) {
					friends.add(results.getString("username2"));
				} else {
					friends.add(results.getString("username1"));
				}
			}
		} catch (SQLException e) {
			friends = null;
		} finally {
			DBManager.closePreparedStatement(pstmt);
			DBManager.closeResultSet(results);
		}
		return friends;
	}
	
	/**
	 * Returns the number of friend requests the user has. Return 0 on error
	 * @param conn
	 * @param username
	 * @return
	 */
	public static int getFriendReqCount(Connection conn, String username) {
		int requestCount = 0;
		PreparedStatement pstmt = null;
		ResultSet requests = null;
		String query = "SELECT COUNT(requestee) as count "
				+ "FROM main.friendrequests WHERE requestee = ?";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, username);
			requests = pstmt.executeQuery();
			while (requests.next()) {
				requestCount = requests.getInt("count");
			}
		} catch (SQLException e) {
			requestCount = 0;
		} finally {
			DBManager.closeResultSet(requests);
			DBManager.closePreparedStatement(pstmt);
		}
		return requestCount;
	}
	
	/**
	 * For a new user, adds all the users from his group to his friend list
	 * 
	 * @param conn
	 * @param username
	 * @param aid
	 * @return number of friend entries added. -1 if error
	 */
	public static int addFriendsFromGroup(Connection conn, String username, int aid) {
		System.out.println("In addFriendsFromGroup");
		int status = 0;
		List<String> friends = getAllUsersOfGroup(conn, aid);
		for (String f: friends) {
			if (!f.equals(username)) {
				int s = addFriend(conn, username, f);
				if (s != -1) {
					status+= s;
				} else {
					return -1;
				}
			}
		}
		return status;
	}

	public static List<String[]> getFriendableUsers(Connection conn, String username) {
		List<String[]> friendableUsers = new ArrayList<String[]>();
		List<String> existingFriends = getFriends(conn, username);
		String query = "SELECT username, aname FROM main.users NATURAL JOIN main.acappella "
				+ "WHERE username != ? AND username NOT IN "
				+ "(SELECT requester FROM main.friendrequests "
				+ "WHERE requestee = ?)";
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, username);
			pstmt.setString(2, username);
			result = pstmt.executeQuery();
			while (result.next()) {
				if (!existingFriends.contains(result.getString("username"))) {
					String[] userInfo = { result.getString("username"),
							result.getString("aname") };
					friendableUsers.add(userInfo);
				}
			}
		} catch (SQLException e) {
			friendableUsers = null;
		} finally {
			DBManager.closeResultSet(result);
			DBManager.closePreparedStatement(pstmt);
		}
		return friendableUsers;
	}

	public static int insertFriendRequest(Connection conn, String requestee, String requester) {
		int status = -1;
		String query = "INSERT INTO main.friendrequests (requestee, requester) VALUE (?,?)";
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, requestee);
			pstmt.setString(2, requester);
			status = pstmt.executeUpdate();
		} catch (SQLException e) {
			status = -1;
		} finally {
			DBManager.closePreparedStatement(pstmt);
		}
		return status;
	}

	public static List<String> getFriendRequestList(Connection conn, String username) {
		List<String> pendingFriends = new ArrayList<String>();
		String query = "SELECT requester FROM main.friendrequests WHERE requestee = ?";
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, username);
			result = pstmt.executeQuery();
			while (result.next()) {
				pendingFriends.add(result.getString("requester"));
			}
		} catch (SQLException e) {
			pendingFriends = null;
		} finally {
			DBManager.closeResultSet(result);
			DBManager.closePreparedStatement(pstmt);
		}
		return pendingFriends;
	}

	public static int deleteFriendRequest(Connection conn, String requester, String requestee) {
		int status = 0;
		String query = "DELETE FROM main.friendrequests WHERE requester = ? AND requestee = ?";
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, requester);
			pstmt.setString(2, requestee);
			status = pstmt.executeUpdate();
		} catch (SQLException e) {
			if (DEBUG) e.printStackTrace();
			status = 0;
		} finally {
			DBManager.closePreparedStatement(pstmt);
		}
		return status;
	}

	public static int addFriend(Connection conn, String username1, String username2) {
		int status = 0;
		String query = "INSERT INTO main.friends (username1, username2) VALUES (?, ?)";
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(query);
			String user1 = "", user2 = "";
			if (username1.compareTo(username2) < 0) {
				user1 = username1;
				user2 = username2;
			}
			if (username1.compareTo(username2) > 0) {
				user1 = username2;
				user2 = username1;
			}
			pstmt.setString(1, user1);
			pstmt.setString(2, user2);
			status = pstmt.executeUpdate();
		} catch (SQLException e) {
			if (DEBUG) e.printStackTrace();
			status = 0;
		} finally {
			DBManager.closePreparedStatement(pstmt);
		}
		return status;
	}

	/**
	 * Returns the number of reg requests that the admin has. Return 0 on error
	 * @param conn
	 * @param username
	 * @return
	 */
	public static int getRegReqCount(Connection conn, String username) {
		int requestCount = 0;
		PreparedStatement pstmt = null;
		ResultSet requests = null;
		String query = "SELECT COUNT(username) as count "
				+ "FROM main.registrationrequests "
				+ "WHERE aid = (SELECT aid FROM main.users WHERE username = ?)";
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, username);
			requests = pstmt.executeQuery();
			while (requests.next()) {
				requestCount = requests.getInt("count");
			}
		} catch (SQLException e) {
			requestCount = 0;
		} finally {
			DBManager.closeResultSet(requests);
			DBManager.closePreparedStatement(pstmt);
		}
		return requestCount;
	}
	
	/**
	 * Add an entry to the registration table with the given information
	 * @param conn
	 * @param newUser
	 * @param aid
	 * @param pwdStore
	 * @return 1 if successfully inserted
	 */
	public static int insertRegRequest(Connection conn, String newUser, int aid, String pwdStore) {
		String query = "INSERT INTO main.registrationrequests (username, aid, pwhash) "
				+ "VALUE (?, ?, ?)";
		PreparedStatement pstmt = null;
		ResultSet result = null;
		int status = 0;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, newUser);
			pstmt.setInt(2, aid);
			pstmt.setString(3, pwdStore);
			status = pstmt.executeUpdate();
		} catch (SQLException e) {
			if (e.getErrorCode() == DBManager.DUPLICATE_KEY_CODE) {
				status = -1;
			} else {
				status = 0;
			}
		} finally {
			DBManager.closeResultSet(result);
			DBManager.closePreparedStatement(pstmt);
		}
		return status;
	}
	
	/**
	 * Returns a list of usernames of registration requests for the user. 
	 * Returns null on error
	 * @param conn
	 * @param username
	 * @return
	 */
	public static List<String> getRegRequestList(Connection conn, String username) {
		List<String> requestList = new ArrayList<String>();
		String query = "SELECT username "
				+ "FROM main.registrationrequests "
				+ "WHERE aid = (SELECT aid FROM main.users WHERE username = ?)";
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, username);
			result = pstmt.executeQuery();
			while (result.next()) {
				requestList.add(result.getString("username"));
			}
		} catch (SQLException e) {
			requestList = null;
		} finally {
			DBManager.closeResultSet(result);
			DBManager.closePreparedStatement(pstmt);
		}
		return requestList;
	}
	
	/**
	 * Returns an array of the registering user's info.
	 * userInfo[0] = username
	 * userInfo[1] = pwhash
	 * userInfo[2] = aid
	 * Returns null if user is not a registering user or db error.
	 * @param conn
	 * @param username the username of the registering user whose info is to be fetched
	 * @return array of user info
	 */
	public static String[] getRegUserInfo(Connection conn, String username) {
		String[] userInfo = new String[3];
		String query = "SELECT * FROM main.registrationrequests WHERE username = ?";
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, username);
			result = pstmt.executeQuery();
			if (result.next()) {
				userInfo[0] = result.getString("username");
				userInfo[1] = result.getString("pwhash");
				userInfo[2] = result.getString("aid");
			} else {
				userInfo = null;
			}
		} catch (SQLException e) {
			if (DEBUG) e.printStackTrace();
			userInfo = null;
		} finally {
			DBManager.closeResultSet(result);
			DBManager.closePreparedStatement(pstmt);
		}
		return userInfo;
	}
	
	/**
	 * Deletes the registering user from the database following a disapproval from 
	 * an admin.
	 * 
	 * @param conn
	 * @param username
	 * @return 1 if successfully deleted.
	 */
	public static int deleteRegRequest(Connection conn, String username) {
		int status = 0;
		String query = "DELETE FROM main.registrationrequests WHERE username = ?";
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, username);
			status = pstmt.executeUpdate();
		} catch (SQLException e) {
			if (DEBUG) e.printStackTrace();
			status = 0;
		} finally {
			DBManager.closePreparedStatement(pstmt);
		}
		return status;
	}
	
	public static int changeRole(Connection conn, String username, String role) {
		int status = 0;
		String query = "UPDATE main.users SET role = ? WHERE username = ?";
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, role);
			pstmt.setString(2, username);
			status = pstmt.executeUpdate();
		} catch (SQLException e) {
			if (DEBUG) e.printStackTrace();
			status = 0;
		} finally {
			DBManager.closePreparedStatement(pstmt);
		}
		return status;
	}
	
	//--------------------Participant stuff------------------------------------//
	
	/**
	 * Get participants to the given region in the given board.
	 * Precond: if freeforall, region is pid, otherwise board and region 
	 * are both valid
	 * partInfo[0] = username
	 * partInfo[1] = privilege ('view' or 'viewpost')
	 * Returns null on error
	 * @param conn
	 * @param board
	 * @param region
	 * @return 
	 */
	public static List<String[]> getParticipants(Connection conn, String board, String region) {
		List<String[]> part = new ArrayList<String[]>();
		String query;
		if (board.equals("freeforall")) {
			query = "SELECT username, privilege " +
					"FROM freeforall.postprivileges " +
					"WHERE pid = ?";
		} else {
			query = "SELECT username, privilege "
					+ "FROM "+board+".regionprivileges "
					+ "WHERE rname = ?";
		}
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = conn.prepareStatement(query);
			if (board.equals("freforall")) {
				pstmt.setInt(1, Integer.parseInt(region));
			} else {
				pstmt.setString(1, region);
			}
			result = pstmt.executeQuery();
			while (result.next()) {
				String[] partInfo = {result.getString("username"), result.getString("privilege")};
				part.add(partInfo);
			}
		} catch (SQLException e) {
			part = null;
		} finally {
			DBManager.closeResultSet(result);
			DBManager.closePreparedStatement(pstmt);
		}
		return part;
	}
	
	public static List<String> getParticipantsOne(Connection conn, String board, String region) {
		List<String> part = new ArrayList<String>();
		String query;
		if (board.equals("freeforall")) {
			query = "SELECT username " +
					"FROM freeforall.postprivileges " +
					"WHERE pid = ?";
		} else {
			query = "SELECT username "
					+ "FROM "+board+".regionprivileges "
					+ "WHERE rname = ?";
		}
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = conn.prepareStatement(query);
			if (board.equals("freforall")) {
				pstmt.setInt(1, Integer.parseInt(region));
			} else {
				pstmt.setString(1, region);
			}
			result = pstmt.executeQuery();
			while (result.next()) {
				part.add(result.getString("username"));
			}
		} catch (SQLException e) {
			part = null;
		} finally {
			DBManager.closeResultSet(result);
			DBManager.closePreparedStatement(pstmt);
		}
		return part;
	}
	
	public static List<String> getAdminsOfBoard(Connection conn, String board) {
		List<String> admins = new ArrayList<String>();
		if (board.equals("freeforall")) {
			return admins;
		}
		String query = "SELECT username FROM main.boardadmins WHERE bname = ?";
		PreparedStatement pstmt = null;
		ResultSet result = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, board);
			result = pstmt.executeQuery();
			while (result.next()) {
				admins.add(result.getString("username"));
			}
		} catch (SQLException e) {
			admins = null;
		} finally {
			DBManager.closeResultSet(result);
			DBManager.closePreparedStatement(pstmt);
		}
		return admins;
	}
	
	public static int removeParticipant(Connection conn, String board, String region, String username) {
		int status = 0;
		String query = "";
		if (board.equals("freeforall")) {
			query = "DELETE FROM freeforall.postprivileges WHERE pid = ? AND username = ?";
		} else {
			query = "DELETE FROM "+board+".regionprivileges WHERE rname = ? AND username = ?";
		}
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(query);
			if (board.equals("freeforall")) {
				pstmt.setInt(1, Integer.parseInt(region));
			} else {
				pstmt.setString(1, region);
			}
			pstmt.setString(2, username);
			status = pstmt.executeUpdate();
		} catch (SQLException e) {
			status = 0;
		} finally {
			DBManager.closePreparedStatement(pstmt);
		}
		return status;
	}

	public static int editParticipant(Connection conn, String board, String region, String username, String priv) {
		int status = 0;
		String query = "";
		if (board.equals("freeforall")) {
			query = "UPDATE freeforall.postprivileges SET privilege = ? " +
					"WHERE pid = ? AND username = ?";
		} else {
			query = "UPDATE " + board + ".regionprivileges SET privilege = ? " +
					"WHERE rname = ? AND username = ?";
		}
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, priv);
			pstmt.setString(3, username);
			if (board.equals("freeforall")) {
				pstmt.setInt(2, Integer.parseInt(region));
			} else {
				pstmt.setString(2, region);
			}
			status = pstmt.executeUpdate();
		} catch (SQLException e) {
			status = 0;
		} finally {
			DBManager.closePreparedStatement(pstmt);
		}
		return status;
	}
	
	public static List<String> getAddableAdmins(Connection conn, String board, String username) {
		List<String> addables = new ArrayList<String>();
		List<String> friends = getFriends(conn, username);
		List<String> admins = getAdmins(conn);
		List<String> adminsOfBoard = getAdminsOfBoard(conn, board);
		for (String f: friends) {
			if (admins.contains(f) && !adminsOfBoard.contains(f)) {
				addables.add(f);
			}
		}
		return addables;
	}
	
	public static List<String> getAdmins(Connection conn) {
		List<String> admins = new ArrayList<String>();
		String query = "SELECT username FROM main.users " +
				"WHERE role = 'admin' OR role = 'sa'";
		Statement stmt = null;
		ResultSet result = null;
		try {
			stmt = conn.createStatement();
			result = stmt.executeQuery(query);
			while (result.next()) {
				admins.add(result.getString("username"));
			}
		} catch (SQLException e) {
			admins = null;
		} finally {
			DBManager.closeResultSet(result);
			DBManager.closeStatement(stmt);
		}
		return admins;
	}
	
	public static int addAdminToBoard(Connection conn, String board, String username) {
		int status = 0;
		String query = "INSERT INTO main.boardadmins (bname, username) " +
				"VALUES (?, ?)";
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, board);
			pstmt.setString(2, username);
			status = pstmt.executeUpdate();
		} catch (SQLException e) {
			status = 0;
		} finally {
			DBManager.closePreparedStatement(pstmt);
		}
		return status;
	}
	
	public static int removeAdminFromBoard(Connection conn, String board, String username) {
		int status = 0;
		String query = "DELETE FROM main.boardadmins WHERE bname = ? AND username = ?";
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, board);
			pstmt.setString(2, username);
			status = pstmt.executeUpdate();
		} catch (SQLException e) {
			status = 0;
		} finally {
			DBManager.closePreparedStatement(pstmt);
		}
		return status;
	}
}










