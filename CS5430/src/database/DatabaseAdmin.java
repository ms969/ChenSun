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
	public static int deleteFromReg(Connection conn, String username) {
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
	
	public static List<String[]> getFriendableUsers(Connection conn, String username) {
		List<String[]> friendableUsers = new ArrayList<String[]>();
		// BLAH BLAH BLAH!!!!asdfjl;asj dofikm
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
}










