package server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import shared.ProjectConfig;
import shared.Utils;

import database.DBManager;
import database.DatabaseAdmin;

public class SocialNetworkAdmin {
	private static final boolean DEBUG = ProjectConfig.DEBUG;
	private static final String EMPTY_LIST = ProjectConfig.COMMAND_EMPTY_LIST;
	
	public static String friendReqNotification(Connection conn, String username) {
		String command = "";
		int requestCount = DatabaseAdmin.getFriendReqCount(conn, username);
		if (requestCount != 0) {
			command = "print Pending Friend Requests (" + requestCount
				+ ") [To view: friendRequests];";
		}
		return command;
	}
	
	public static String regReqNotification(Connection conn, String username) {
		String command = "";
		int requestCount = DatabaseAdmin.getRegReqCount(conn, username);
		if (requestCount != 0) {
			command = "print Pending User Registration Requests ("
					+ requestCount + ") [To view: regRequests];";
		}
		return command;
	}
	
	public static String insertRegRequest(Connection conn, String newUser, int aid, 
			String pwdStore, String answerStore) {
		String command = "";
		int success = DatabaseAdmin.insertRegRequest(conn, newUser, aid, pwdStore, answerStore);
		if (success == 1) {
			command = "print Registration request for " + newUser
					+ " has been sent.;print Once an admin from your group "
					+ "approves, you will be added to the system.;print ;";
		} else if (success == -2) {
			command = "print User already pending registration approval. Try again with a different username.;print ;";
		} else {
			command = "print Registration failed due to database error. " +
					"Please try again or contact System Admin.;print ;";
		}
		return command;
	}
	
	public static String regRequests(Connection conn, String username) {
		String command = "";
		List<String> pendingUsers = DatabaseAdmin.getRegRequestList(conn, username);
		if (pendingUsers == null) {
			command = "print Database error. Please contact System Admin.;";
		} else if (pendingUsers.size() == 0) {
			command = "print No pending registration requests at the moment.;";
		} else {
			command = "print Pending User Registration Requests ("
					+ pendingUsers.size() + "):;";
			for (String u: pendingUsers) {
				command = command + "print " + u + ";";
			}
			command += "print ;print [To approve: approve "
					+ "<username1>, <username2>];print [To remove: "
					+ "remove <username1>, <username2>];askForInput;";
		}
		return command;
	}
	
	public static String friendRequests(Connection conn, String username) {
		String command = "";
		List<String> pendingFriends = DatabaseAdmin.getFriendRequestList(conn, username);
		if (pendingFriends == null) {
			command = "print Database error. Please contact System Admin.;";
		} else if (pendingFriends.size() == 0) {
			command = "print No pending friend requests at the moment.;";
		} else {
			command = "print Pending Friend Requests (" + pendingFriends.size() + "):;";
			for (String f: pendingFriends) {
				command = command + "print " + f + ";";
			}
			command += ";print ;print [To approve: approve "
					+ "<username1>, <username2>];print [To remove: "
					+ "remove <username1>, <username2>];askForInput;";
		}
		return command;
	}

	public static String regApprove(Connection conn, String username) {
		String success = "print "+username+" has been added to the system.;";
		String error = "print Database error occurred while approving registration for " + 
				username + ". Please try again or contact the System Admin.;";
		String[] userInfo = DatabaseAdmin.getRegUserInfo(conn, username);
		if (userInfo == null) {
			if (DEBUG) System.err.println("regApprove: userInfo returned null");
			return error;
		}
		String pwhash = userInfo[1];
		int aid = Integer.parseInt(userInfo[2]);
		String secanswer = userInfo[3];
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			if (DEBUG) System.err.println("regApprove: turning off auto commit failed.");
			return error;
		}
		
		int deleteStatus = DatabaseAdmin.deleteRegRequest(conn, username);
		int addStatus = DatabaseAdmin.addUser(conn, username, pwhash, aid, secanswer);
		if (DEBUG) System.out.println("Going into addFriendsFromGroup");
		int addFriendStatus = DatabaseAdmin.addFriendsFromGroup(conn, username, aid);
		
		if (deleteStatus != 1 || addStatus != 1 || addFriendStatus <= 0) {
			DBManager.rollback(conn);
			DBManager.trueAutoCommit(conn);
			if (DEBUG) System.err.printf("regApprove: DB operations failed. " +
					"deleteStatus: %d, addStatus: %d, addFriendStatus: %d\n", deleteStatus, addStatus, addFriendStatus);
			return error;
		} else {
			try {
				conn.commit();
				DBManager.trueAutoCommit(conn);
				return success;
			} catch (SQLException e) {
				DBManager.trueAutoCommit(conn);
				if (DEBUG) e.printStackTrace();
				return error;
			}
		}
	}
	
	public static String friendApprove(Connection conn, String requester, String requestee) {
		String success = "print "+requester+" has been added as your friend.;";
		String error = "print Database error occurred while friending " + 
				requester + ". Please try again or contact the System Admin.;";
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			if (DEBUG) System.err.println("friendApprove: turning off auto commit failed.");
			return error;
		}
		
		int deleteStatus = DatabaseAdmin.deleteFriendRequest(conn, requester, requestee);
		int addStatus = DatabaseAdmin.addFriend(conn, requester, requestee);
		
		if (deleteStatus != 1 || addStatus != 1) {
			DBManager.rollback(conn);
			DBManager.trueAutoCommit(conn);
			if (DEBUG) System.err.printf("friendApprove: DB operations failed. " +
					"deleteStatus: %d, addStatus: %d\n", deleteStatus, addStatus);
			return error;
		} else {
			try {
				conn.commit();
				DBManager.trueAutoCommit(conn);
				return success;
			} catch (SQLException e) {
				DBManager.trueAutoCommit(conn);
				if (DEBUG) e.printStackTrace();
				return error;
			}
		}
	}

	public static String regRemove(Connection conn, String username) {
		String success = "print "+username+" has been deleted from the system.;";
		String error = "print Database error occurred while removing registration for " + 
				username + ". Please try again or contact the System Admin.;";
		int status = DatabaseAdmin.deleteRegRequest(conn, username);
		if (status == 1) {
			return success;
		} else {
			return error;
		}
	}
	
	public static String friendReqRemove(Connection conn, String requester, String requestee) {
		String success = "print Friend request from " + requester + " has been deleted.;";
		String error = "print Database error occurred while removing friend request from " + 
				requester + ". Please try again or contact the System Admin.;";
		int status = DatabaseAdmin.deleteFriendRequest(conn, requester, requestee);
		if (status == 1) {
			return success;
		} else {
			return error;
		}
	}

	public static String displayFriendableUsers(Connection conn, String prefix, List<String[]> friendableUsers) {
		String command;
		if (prefix == "") {
			command = "print Users in the system:;";
		} else {
			command = "print Usernames starting with '" + prefix + "';";
		}
		if (friendableUsers == null || friendableUsers.size() == 0) {
			command += EMPTY_LIST;
		} else {
			for (String[] userInfo : friendableUsers) {
				prefix = prefix.toLowerCase();
				if (userInfo[0].toLowerCase().startsWith(prefix)) {
					command += "print " + userInfo[0] + " (" + userInfo[1]
							+ ");";
				}
			}
		}
		command += "print ;print Type the name of the user you wish to friend:;"
				+ "askForInput;";
		return command;
	}
	
	public static String displayGroupList(Connection conn, Map<Integer, String> groupList, String newUser) {
		String list = "";
		if (groupList == null || groupList.size() == 0) {
			list = EMPTY_LIST;
		} else {
			Iterator<Entry<Integer, String>> it = groupList.entrySet().iterator();
			while (it.hasNext()) {
		        Map.Entry<Integer, String> pairs = (Map.Entry<Integer, String>)it.next();
		        list += "print " + pairs.getKey() + " " + pairs.getValue() + ";";
		    }
		}
		return "print Choose a cappella group for " + newUser
				+ " by entering the group number:;" + list + "askForInput;";
	}
	
	public static String insertFriendRequest(Connection conn, String requestee, String requester) {
		String command = "";
		int status = DatabaseAdmin.insertFriendRequest(conn, requestee, requester);
		if (status >= 0) {
			command = "print Friend request sent to " + requestee + ".;";
		} else {
			command = "print Database Error while sending friend request. Please try again or contact the System Admin.;";
		}
		return command;
	}
	
	public static String displayDeletableUsers(List<String[]> users) {
		String command = "print Users in your A Cappella group that you can delete:;";
		if (users == null || users.size() == 0) {
			command += EMPTY_LIST;
		} else {
			for (String[] u: users) {
				command += "print " + u[0] + ";";
			}
		}
		command += "print ;print Type the name of the user you wish to delete:;askForInput;";
		return command;
	}
	
	public static String deleteUser(Connection conn, String username) {
		String success = "print " + username + " has been deleted from the system.;";
		String error = "print Database Error while deleting " + username + ". Please try again or contact the System Admin.;";
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			return error;
		}
		int deleteStatus = DatabaseAdmin.deleteUser(conn, username);
		int sideEffectStatus1 = DatabaseAdmin.replaceBoardManager(conn, username);
		int sideEffectStatus2 = DatabaseAdmin.deletionBoardDBEffects(conn, username);
		if (DEBUG) System.err.printf("Deleting user:\ndeleteStatus = %d, sideES1 = %d, sideES2 = %d\n", 
				deleteStatus, sideEffectStatus1, sideEffectStatus2);
		try {
			if (deleteStatus == 1 && sideEffectStatus1 >= 0 && sideEffectStatus2 >= 0) {
				conn.commit();
				conn.setAutoCommit(true);
				return success;
			} else {
				conn.rollback();
				conn.setAutoCommit(true);
				return error;
			}
		} catch (SQLException e) {
			return error;
		}
	}
	
	public static String showFriends(Connection conn, String username) {
		String command = "print Your Friends:;";
		List<String> friends = DatabaseAdmin.getFriends(conn, username);
		if (friends == null) {
			command = "print Database Error while getting friend list. Please try again or contact the System Admin.;";
		} else if (friends.size() == 0) {
			command = "print You have no friends right now.;print To add a friend: type addFriend;";
		} else {
			for (String f: friends) {
				String[] userInfo = DatabaseAdmin.getUserInfo(conn, f);
				command += "print " + userInfo[0] + " (" + userInfo[2] + 
						"-" + userInfo[3].toUpperCase() + ");";
				// April (Fantasia-MEMBER)
			}
		}
		return command;
	}
	
	public static String displayRoleChange(List<String[]> users) {
		String command = "print Users in your A Cappella group that you can change roles for:;";
		if (users == null || users.size() == 0) {
			command += EMPTY_LIST;
		} else {
			for (String[] u: users) {
				command += "print " + u[0] + " (" + u[1].toUpperCase() + ");";
			}
		}
		command += "print ;print Type the name of the user you wish to change role for:;askForInput;";
		return command;
	}
	
	public static String changeRole(Connection conn, String username, String role) {
		int status = DatabaseAdmin.changeRole(conn, username, role);
		if (status == 1) {
			String from;
			if (role.equals("admin")) {
				from = "MEMBER";
			} else {
				from = "ADMIN";
			}
			return "print Role for " + username + " has been changed from " + from + 
					" to " + role.toUpperCase() + ";";
		} else {
			return "print Database Error while changing role for " + username + 
					". Please try again or contact the System Admin.;";
		}
	}
	
	public static String displaySATransferableUsers(List<String> admins) {
		String command = "print Users in your A Cappella group that you can transfer SA role to:;";
		if (admins == null || admins.size() == 0) {
			command += EMPTY_LIST;
		} else {
			for (String a: admins) {
				command += "print " + a + " (ADMIN);";
			}
		}
		command += "print ;print Type the name of the user you wish to transfer SA role to:;"
				+ "askForInput;";
		return command;
	}
	
	public static String transferSA(Connection conn, String from, String to) {
		String success = "print SA role has been transferred to " + to + ";";
		String error = "print Database error occurred while transferring SA role to " + 
				to + ". Please try again or contact the System Admin.;";
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			if (DEBUG) System.err.println("transferSA: turning off auto commit failed.");
			return error;
		}
		
		int demoteStatus = DatabaseAdmin.changeRole(conn, from, "admin");
		int promoteStatus = DatabaseAdmin.changeRole(conn, to, "sa");
		
		if (demoteStatus != 1 || promoteStatus != 1) {
			DBManager.rollback(conn);
			DBManager.trueAutoCommit(conn);
			if (DEBUG) System.err.printf("transferSA: DB operations failed. " +
					"demoteStatus: %d, promoteStatus: %d\n", demoteStatus, promoteStatus);
			return error;
		} else {
			try {
				conn.commit();
				DBManager.trueAutoCommit(conn);
				return success;
			} catch (SQLException e) {
				DBManager.trueAutoCommit(conn);
				if (DEBUG) e.printStackTrace();
				return error;
			}
		}
	}
	
	private static String printUserInfo(String username, String role, String aname) {
		String command = "print Logged in as: " + username + ";";
		command += "print Role: " + role.toUpperCase() + ";";
		command += "print A Cappella Group: " + aname + ";print ;";
		return command;
	}
	
	public static String printUserInfo(Connection conn, String username) {
		String[] userInfo = DatabaseAdmin.getUserInfo(conn, username);
		String command = "";
		if (userInfo == null) {
			command = "print Error while getting user info.";
		} else {
			command = printUserInfo(userInfo[0], userInfo[3], userInfo[2]);
		}
		command += friendReqNotification(conn, username);
		if (userInfo[3].equals("sa") || userInfo[3].equals("admin")) {
			command += regReqNotification(conn, username);
		}
		command += Utils.getHR(80) + "print ;";
		return command;
	}
	
	public static String printUserInfo(String username) {
		Connection conn = DBManager.getConnection();
		String command = printUserInfo(conn, username);
		DBManager.closeConnection(conn);
		return command;
	}
	
	/**
	 * Precond: if board is freeforall, region is post, if not, board and regions are valid
	 * @param conn
	 * @param board
	 * @param region
	 * @return
	 */
	public static String displayParticipAndAdmins(Connection conn, String board, String region) {
		String command = "print Displaying participants in "+board+"/"+region+":;";
		List<String> admins = DatabaseAdmin.getAdminsOfBoard(conn, board);
		List<String[]> part = DatabaseAdmin.getParticipants(conn, board, region);
		if ((admins == null || admins.size() == 0) && (part == null || part.size() == 0)) {
			command += EMPTY_LIST;
		} else {
			for (String a: admins) {
				command += "print " + a + " (Admin);";
			}
	
			for (String[] p: part) {
				command += "print " + p[0];
				if (p[1].equals("view")) {
					command += " (view only)";
				}
				command += ";";
			}
		}
		//command += "print ;print Other Commands: addParticipants, removeParticipants, editParticipants;";
		return command;
	}
	
	public static List<String> getAddableParticip(Connection conn, String username, String board, String region) {
		List<String> addables = new ArrayList<String>();
		// friends that are not already participants of the region and not admins
		List<String> friends = DatabaseAdmin.getFriends(conn, username);
		List<String[]> participants = DatabaseAdmin.getParticipants(conn, board, region);
		if (friends == null || participants == null) {
			return null;
		}
		for (String f: friends) {
			if (board == "freeforall" || !DatabaseAdmin.isAdmin(conn, f)) {
				boolean isPart = false;
				for (String[] p: participants) {
					if (f.equals(p[0])) {
						isPart = true;
						break;
					}
				}
				if (!isPart) {
					addables.add(f);
				}
			}
		}
		return addables;
	}
	
	public static String displayAddableParticip(List<String> addables, String board) {
		String command = "print Friends you can add as a participant to this region/post:;";
		if (addables == null || addables.size() == 0) {
			command += EMPTY_LIST;
		} else {
			for (String a: addables) {
				command += "print " + a + ";";
			}
		}
		command += "print To add participants with view and post privilege, " +
				"type 'viewpost <user1>, <user2>';" +
				"print To add participants with view only privilege, " +
				"type 'view <user1>, <user2>';askForInput;";
		return command;
	}
	
	public static String displayRemoveParticip(Connection conn, String board, String region) {
		String command = "print Displaying participants in "+board+"/"+region+":;";
		List<String[]> part = DatabaseAdmin.getParticipants(conn, board, region);
		if (part == null || part.size() == 0) {
			command += EMPTY_LIST;
		} else {
			for (String[] p: part) {
				command += "print " + p[0];
				if (p[1].equals("view")) {
					command += " (view only)";
				}
				command += ";";
			}
		}
		command += "print To remove participants, type their usernames separated " +
				"by comma: '<user1>, <user2>';askForInput;";
		return command;
	}
	
	public static String displayEditableParticip(List<String[]> editables) {
		String command = "print Participants you can edit in this region/post:;";
		if (editables == null || editables.size() == 0) {
			command += EMPTY_LIST;
		} else {
			for (String[] e: editables) {
				command += "print " + e[0];
				if (e[1].equals("view")) {
					command += " (view only)";
				} else if (e[1].equals("viewpost")) {
					command += " (view and post)";
				}
				command += ";";
			}
		}
		command += "print Type the name of the user you wish to change permission for:;" +
				"askForInput;";
		return command;
	}
	
	public static String removeParticipant(Connection conn, String board, String region, String username) {
		String success = "print " + username + " has been removed from " + 
				board + "/" + region + ";";
		String error = "print Database Error while removing " + username + ". Please " +
				"try again or contact the System Admin.;";
		int status = DatabaseAdmin.removeParticipant(conn, board, region, username);
		if (status == 1) {
			return success;
		} else {
			return error;
		}
	}
	
	public static String editParticipant(Connection conn, String board, String region, String username, String priv) {
		String success = "print permission for username has been changed.;";
		String error = "print Database Error while changing permission for "+username+". " +
				"Please try again or contact the System Admin.;";
		int status = DatabaseAdmin.editParticipant(conn, board, region, username, priv);
		if (status == 1) {
			return success;
		} else {
			return error;
		}
	}
	
	public static String displayAddableAdmins(List<String> addables) {
		String command = "print Admins you are friends with:;";
		if (addables == null || addables.size() == 0) {
			command += EMPTY_LIST;
		} else {
			for (String a: addables) {
				command += "print " + a + ";";
			}
		}
		command += "print To add admins, type their usernames separated " +
				"by comma: '<user1>, <user2>';askForInput;";
		return command;
	}
	
	public static String addAdmin(Connection conn, String board, String username) {
		String success = "print " + username + " has been added as an admin to " +
				board + ".;";
		String error = "print Database Error while adding " + username + " to " + board +
				"as admin. Please try again or contact the System Admin.;";
		int status = DatabaseAdmin.addAdminToBoard(conn, board, username);
		if (status == 1) {
			return success;
		} else {
			return error;
		}
	}
	
	public static String removeAdmin(Connection conn, String board, String username) {
		String success = "print " + username + " has been removed from " + board + ".;";
		String error = "print Database Error while removing " + username + " from " + 
				board + ";";
		int status = DatabaseAdmin.removeAdminFromBoard(conn, board, username);
		if (status == 1) {
			return success;
		} else {
			return error;
		}
	}

	public static String displayRemovableAdmins(List<String> removables,
			String user) {
		String command = "print Admins of this board:;";
		if (removables == null || removables.size() == 0) {
			command += EMPTY_LIST;
		} else {
			for (String r: removables) {
				if (!r.equals(user)) {
					command += "print " + r + ";";
				}
			}
		}
		command += "print To remove admins, type their usernames separated by comma: " +
				"'<user1>, <user2>';askForInput;";
		return command;
	}
	
	public static String changePassword(Connection conn, String username, String pwdStore) {
		String success = "print Password changed successfully.;";
		String error = "print Password change failed. Try again or contact System Admin.;";
		int status = DatabaseAdmin.changePassword(conn, username, pwdStore);
		if (status >= 0) {
			return success;
		} else {
			return error;
		}
	}
}







