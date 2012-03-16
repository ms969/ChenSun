package server;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import shared.InputProcessor;
import crypto.Hash;
import crypto.SharedKeyCrypto;
import crypto.SharedKeyCryptoComm;
import database.DBManager;

import java.security.*;
import javax.crypto.*;

public class ServerInputProcessor extends InputProcessor {
	private OutputStream os;
	private InputStream is;
	private Cipher c;
	private SecretKey sk;
	private SharedKeyCrypto skc;

	private String user = null;
	private String[] currentPath;

	public static final String[] COMMANDS = { "^login .+", // 0
			"^register$", // 1
			"^regRequests$", // 2
			"^addFriend.*", // 3
			"^createBoard .+", // 4
			"^refresh$", // 5
			"^goto .+", // 6
			"^createRegion .+", // 7
			"^post$", // 8
			"^reply$", // 9
			"^friendRequests$", // 10
			"^deleteUser$", // 11
			"^showFriends$", // 12
			"^changeUserRole$", // 13
			"^transferSA$", // 14
			// "^participants$", // 15
			// "^addParticipants$", // 16
			// "^removeParticipants$", // 17
			// "^editParticipants$", // 18
			"^logout$", // 15
			"^help$", // 16
	};

	public void processCommand(String inputLine) throws IOException {
		if (inputLine.matches(COMMANDS[0])) {
			System.out.println("I'm at processCommand login!");
			if (user == null) {
				processLogin(inputLine);
			} else {
				SharedKeyCryptoComm.send("" , os, c, sk);
			}
			return;
		}
		if (inputLine.matches(COMMANDS[1])) {
			if (user == null) {
				processRegistration();
			} else {
				SharedKeyCryptoComm.send("" , os, c, sk);
			}
			return;
		}
		if (inputLine.matches(COMMANDS[2])) {
			if (user != null) {
				processRegRequests();
			} else {
				SharedKeyCryptoComm.send("" , os, c, sk);
			}
			return;
		}
		if (inputLine.matches(COMMANDS[3])) {
			if (user != null) {
				processAddFriend(inputLine);
			} else {
				SharedKeyCryptoComm.send("" , os, c, sk);
			}
			return;
		}
		if (inputLine.matches(COMMANDS[4])) {
			if (user != null) {
				processCreateBoard(inputLine);
			} else {
				SharedKeyCryptoComm.send("" , os, c, sk);
			}
			return;
		}
		if (inputLine.matches(COMMANDS[5])) {
			if (user != null) {
				processRefresh();
			} else {
				SharedKeyCryptoComm.send("" , os, c, sk);
			}
			return;
		}
		if (inputLine.matches(COMMANDS[6])) {
			if (user != null) {
				processGoto(inputLine);
			} else {
				SharedKeyCryptoComm.send("" , os, c, sk);
			}
			return;
		}
		if (inputLine.matches(COMMANDS[7])) {
			if (user != null) {
				processCreateRegion(inputLine);
			} else {
				SharedKeyCryptoComm.send("" , os, c, sk);
			}
			return;
		}
		if (inputLine.matches(COMMANDS[8])) {
			if (user != null) {
				processPost();
			} else {
				SharedKeyCryptoComm.send("" , os, c, sk);
			}
			return;
		}
		if (inputLine.matches(COMMANDS[9])) {
			if (user != null) {
				processReply();
			} else {
				SharedKeyCryptoComm.send("" , os, c, sk);
			}
			return;
		}
		if (inputLine.matches(COMMANDS[10])) {
			if (user != null) {
				processFriendRequests();
			} else {
				SharedKeyCryptoComm.send("" , os, c, sk);
			}
			return;
		}
		if (inputLine.matches(COMMANDS[11])) {
			if (user != null) {
				processDeleteUser();
			} else {
				SharedKeyCryptoComm.send("" , os, c, sk);
			}
			return;
		}
		if (inputLine.matches(COMMANDS[12])) {
			if (user != null) {
				processShowFriends();
			}
			return;
		}
		if (inputLine.matches(COMMANDS[13])) {
			if (user != null) {
				processChangeUserRole();
			} else {
				SharedKeyCryptoComm.send("" , os, c, sk);
			}
			return;
		}
		if (inputLine.matches(COMMANDS[14])) {
			if (user != null) {
				processTransferSA();
			} else {
				SharedKeyCryptoComm.send("" , os, c, sk);
			}
			return;
		}
		/*
		 * if (inputLine.matches(COMMANDS[15])) { if (user != null) {
		 * processParticipants(); } else { out.println(); } return; } if
		 * (inputLine.matches(COMMANDS[16])) { if (user != null) {
		 * processAddParticipants(); } else { out.println(); } return; } if
		 * (inputLine.matches(COMMANDS[17])) { if (user != null) {
		 * processRemoveParticipants(); } else { out.println(); } return; } if
		 * (inputLine.matches(COMMANDS[18])) { if (user != null) {
		 * processEditParticipants(); } else { out.println(); } return; }
		 */
		if (inputLine.matches(COMMANDS[15])) {
			if (user != null) {
				processLogout();
			} else {
				SharedKeyCryptoComm.send("" , os, c, sk);
			}
			return;
		}
		if (inputLine.matches(COMMANDS[16])) {
			if (user != null) {
				SharedKeyCryptoComm.send("help", os, c, sk);
			} else {
				SharedKeyCryptoComm.send("", os, c, sk);
			}
			return;
		}
		SharedKeyCryptoComm.send("", os, c, sk);
	}

	private void processLogout() {
		user = null;
		for (int i = 0; i < currentPath.length; i++) {
			currentPath[i] = null;
		}
		SharedKeyCryptoComm.send("print Logged out.;setLoggedIn false", os, c, sk);
	}

	public ServerInputProcessor(OutputStream os, InputStream is, 
			Cipher c, SecretKey sk) {
		this.os = os;
		this.is = is;
		this.c = c;
		this.sk = sk;
		this.currentPath = new String[3];
		for (int i = 0; i < currentPath.length; i++) {
			currentPath[i] = null;
		}
	}

	/**
	 * return an array with the logged in user's info user[0] = username user[1]
	 * = a cappella name user[2] = role
	 */
	private String[] getCurrentUserInfo() {
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		ResultSet result = null;
		String[] user = new String[3];
		try {
			stmt = conn.createStatement();
			String query = "SELECT username, aname, role FROM main.users NATURAL JOIN "
					+ "main.acappella WHERE username = '" + user + "'";
			result = stmt.executeQuery(query);
			while (result.next()) {
				user[0] = result.getString("username");
				user[1] = result.getString("aname");
				user[2] = result.getString("role");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBManager.closeStatement(stmt);
			DBManager.closeResultSet(result);
			DBManager.closeConnection(conn);
		}
		return user;
	}

	private ArrayList<String> getCurrentUsersFriends() {
		ArrayList<String> friends = new ArrayList<String>();
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		ResultSet results = null;
		try {
			stmt = conn.createStatement();
			String query = "SELECT * FROM main.friends WHERE username1 = '"
					+ user + "' OR username2 = '" + user + "'";
			results = stmt.executeQuery(query);
			while (results.next()) {
				if (results.getString("username1").equals(user)) {
					friends.add(results.getString("username2"));
				} else {
					friends.add(results.getString("username1"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBManager.closeStatement(stmt);
			DBManager.closeResultSet(results);
			DBManager.closeConnection(conn);
		}
		return friends;
	}

	private ArrayList<String> getBoardAdmins() {
		if (currentPath[0].equals("freeforall")) {
			// TODO: what to return if board is freeforall?
			return null;
		}
		String board = currentPath[0];
		ArrayList<String> admins = new ArrayList<String>();

		Connection conn = DBManager.getConnection();
		try {
			Statement stmt = conn.createStatement();
			String query = "SELECT * FROM " + board + ".admins";
			ResultSet adminResults = stmt.executeQuery(query);
			while (adminResults.next()) {
				admins.add(adminResults.getString("username"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return admins;
	}

	private void processLogin(String inputLine) {
		// get password
		
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		ResultSet userTuple = null;
		String username = getValue(inputLine);

		boolean userExist = false;
		boolean pwMatch = false;
		String pwhash = "";
		String role = "";
		String aname = "";

		// Querying database
		try {
			conn = DBManager.getConnection();
			stmt = null;
			String query = "SELECT username, pwhash, aname, role "
					+ "FROM main.users NATURAL JOIN main.acappella "
					+ "WHERE username = '" + username + "'";
			stmt = conn.createStatement();
			userTuple = stmt.executeQuery(query);
			if (userTuple.next()) {
				userExist = true;
				pwhash = userTuple.getString("pwhash");
				role = userTuple.getString("role");
				aname = userTuple.getString("aname");
			}
			String command = "";
			if (userExist) {
				command = "setSalt "+pwhash.substring(0, Hash.SALT_STRING_LENGTH) + ";";
			}
			command += "print Input password:;getPassword";
			SharedKeyCryptoComm.send(command, os, c, sk);
			String enteredPwdHash = SharedKeyCryptoComm.receive(is, c, sk);
			
			if (userExist) {
				pwMatch = Hash.comparePwd(pwhash, enteredPwdHash);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBManager.closeStatement(stmt);
			DBManager.closeResultSet(userTuple);
			DBManager.closeConnection(conn);
		}

		// Output for Client
		if (userExist && pwMatch) {
			user = username;
			String command = "setLoggedIn true;" + "print Logged in as: " + username
					+ ";" + "print Role: " + role.toUpperCase() + ";"
					+ "print A Cappella Group: " + aname + ";print ;";

			// Get friend requests
			command += getFriendReq(username);

			// if admin or SA, get pending registration requests
			if (role.equals("admin") || role.equals("sa")) {
				String regReqCommand = getRegReq(username);
				command += regReqCommand + ";";
			}

			String hr = getHR(80);
			command += hr + "print ;";

			// printing out boards
			SharedKeyCryptoComm.send(command + SocialNetworkNavigation.printPath(currentPath)
					+ SocialNetworkBoards.viewBoards(user), os, c, sk);
		} else {
			SharedKeyCryptoComm.send("print username does not exist or invalid password.", os, c, sk);
		}

	}

	private void processRegistration() throws IOException {
		String newUser = "";
		SharedKeyCryptoComm.send("print Choose a username:;askForInput", os, c, sk);

		boolean userExist = true;
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		ResultSet existingUser = null;

		while (userExist) {
			newUser = SharedKeyCryptoComm.receive(is, c, sk);
			if (newUser.equals("cancel")) {
				SharedKeyCryptoComm.send("", os, c, sk);
				return;
			}

			String query = "SELECT username FROM main.users WHERE username = '"
					+ newUser + "'";
			try {
				stmt = conn.createStatement();
				existingUser = stmt.executeQuery(query);
				if (!existingUser.next()) {
					userExist = false;
				}
			} catch (SQLException e) {
				userExist = false;
				e.printStackTrace();
			} finally {
				DBManager.closeStatement(stmt);
				DBManager.closeResultSet(existingUser);
			}

			// TODO: check that username is legal and isn't keywords like cancel

			if (userExist) {
				SharedKeyCryptoComm.send("print Username already exist. Choose a different one.;"
						+ "askForInput", os, c, sk);
			}
		}

		// username isn't already in the DB
		boolean groupExist = false;
		String command = "";
		String list = "";
		HashMap<String, Integer> groupList = new HashMap<String, Integer>();
		ResultSet groups = null;
		String query = "SELECT aid, aname FROM main.acappella";
		try {
			stmt = conn.createStatement();
			groups = stmt.executeQuery(query);
			while (groups.next()) {
				groupList.put(groups.getString("aname"), groups.getInt("aid"));
				list = list + ";print " + groups.getString("aname");
			}

			String group = "";
			int aid = 0;
			while (!groupExist) {
				command += "print Choose a cappella group for " + newUser
						+ ":" + list + ";askForInput";
				SharedKeyCryptoComm.send(command, os, c, sk);
				group = SharedKeyCryptoComm.receive(is, c, sk);
				if (group.equals("cancel")) {
					SharedKeyCryptoComm.send("", os, c, sk);
					return;
				}

				if (!groupList.containsKey(group)) {
					command = "print Please choose a group from the list.;";
				} else {
					groupExist = true;
					aid = groupList.get(group);
				}
			}

			SharedKeyCryptoComm.send("createPassword", os, c, sk);
			// choose password
			String pwdStore = SharedKeyCryptoComm.receive(is, c, sk);

			// group exists
			query = "INSERT INTO main.registrationrequests (username, aid, pwhash) "
					+ "VALUE ('"
					+ newUser
					+ "', "
					+ aid
					+ ", '"
					+ pwdStore
					+ "')";

			stmt.executeUpdate(query);
			command = "print Registration request for " + newUser + " from "
					+ group
					+ " has been sent.;print Once an admin from your group "
					+ "approves, you will be added to the system.;print ;";
			SharedKeyCryptoComm.send(command, os, c, sk);
		} catch (SQLException e) {
			if (e.getErrorCode() == DBManager.DUPLICATE_KEY_CODE) {
				SharedKeyCryptoComm.send("print User is already in the system. Choose a different username.;print ;", os, c, sk);
			} else {
				e.printStackTrace();
			}
		} finally {
			DBManager.closeResultSet(groups);
			DBManager.closeStatement(stmt);
			DBManager.closeConnection(conn);
		}

	}

//	private boolean pwdsMatch(char[] pwdChar1, char[] pwdChar2) {
//		if (pwdChar1.length != pwdChar2.length) {
//			return false;
//		}
//		for (int i = 0; i < pwdChar1.length; i++) {
//			if (pwdChar1[i] != pwdChar2[i])
//				return false;
//		}
//		return true;
//	}

	private void processRegRequests() throws IOException {
		// TODO: Check if user is an admin
		ArrayList<String> pendingUsers = new ArrayList<String>();
		int count = 0;
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		ResultSet requests = null;
		try {
			stmt = conn.createStatement();
			String query = "SELECT username "
					+ "FROM main.registrationrequests "
					+ "WHERE aid = (SELECT aid FROM main.users WHERE username = '"
					+ user + "')";
			requests = stmt.executeQuery(query);
			while (requests.next()) {
				pendingUsers.add(requests.getString("username"));
				count++;
			}
		} catch (SQLException e) {
			count = 0;
			e.printStackTrace();
		} finally {
			DBManager.closeResultSet(requests);
			DBManager.closeStatement(stmt);
			DBManager.closeConnection(conn);
		}

		if (count > 0) {
			String command = "print Pending User Registration Requests ("
					+ count + "):";
			for (int i = 0; i < pendingUsers.size(); i++) {
				command = command + ";print " + pendingUsers.get(i);
			}
			command = command + ";print ;print [To approve: approve "
					+ "<username1>, <username2>];print [To remove: "
					+ "remove <username1>, <username2>];askForInput";
			SharedKeyCryptoComm.send(command, os, c, sk);
			regApproval(SharedKeyCryptoComm.receive(is, c, sk));
		} else {
			SharedKeyCryptoComm.send("print No pending registration requests at the moment.", os, c, sk);
		}

	}

	private void regApproval(String input) {
		if (input.equals("cancel")) {
			SharedKeyCryptoComm.send("", os, c, sk);
			return;
		}
		if (input.matches("^approve.+")) {
			String value = getValue(input);
			String delim = ",";
			String[] approvedUsers = value.split(delim);
			regApprove(approvedUsers);
			return;
		}
		if (input.matches("^remove.+")) {
			String value = getValue(input);
			String delim = ",";
			String[] usersToDelete = value.split(delim);

			// Building queries
			String deleteQuery = "DELETE FROM main.registrationrequests WHERE ";
			for (int i = 0; i < usersToDelete.length; i++) {
				usersToDelete[i] = usersToDelete[i].trim();

				deleteQuery += "username = " + quote(usersToDelete[i]);
				if (i != usersToDelete.length - 1) {
					deleteQuery += " OR ";
				}
			}

			Connection conn = DBManager.getConnection();
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				stmt.executeUpdate(deleteQuery);
				// confirmation to client
				String command = "print ";
				for (String user : usersToDelete) {
					command += user + ", ";
				}
				// substring to take off the last comma
				command = command.substring(0, command.length() - 2)
						+ " has been deleted from the system.";
				SharedKeyCryptoComm.send(command, os, c, sk);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBManager.closeStatement(stmt);
				DBManager.closeConnection(conn);
			}

			return;
		}
	}

	private void regApprove(String[] approvedUsers) {
		// Building queries: select info from regrequests and delete from
		// regreqests

		String selectQuery = "SELECT * FROM main.registrationrequests WHERE ";
		String deleteQuery = "DELETE FROM main.registrationrequests WHERE ";

		for (int i = 0; i < approvedUsers.length; i++) {
			approvedUsers[i] = approvedUsers[i].trim();

			selectQuery += "username = " + quote(approvedUsers[i]);
			deleteQuery += "username = " + quote(approvedUsers[i]);
			if (i != approvedUsers.length - 1) {
				selectQuery += " OR ";
				deleteQuery += " OR ";
			}
		}
		Connection conn = null;
		Statement stmt = null;
		ResultSet selectResult = null;
		try {
			// building the insert into users table query
			conn = DBManager.getConnection();
			conn.setAutoCommit(false);
			stmt = conn.createStatement();

			selectResult = stmt.executeQuery(selectQuery);

			ArrayList<String[]> pendingUserInfo = new ArrayList<String[]>();
			while (selectResult.next()) {
				String[] userInfo = { selectResult.getString("username"),
						selectResult.getString("aid"),
						selectResult.getString("pwhash") };
				pendingUserInfo.add(userInfo);
			}
			for (String[] userInfo : pendingUserInfo) {
				String insertQuery = "INSERT INTO main.users (username, aid, pwhash, role) "
					+ "VALUE " + "('" + userInfo[0] + "'," + userInfo[1] + ", '" 
					+ userInfo[2] + "','member')";
				String addFriendQuery = addFriendsFromGroupQuery(userInfo[0],
						Integer.parseInt(userInfo[1]));

				if (SocialNetworkServer.DEBUG) {

				}

				stmt.executeUpdate(insertQuery);
				stmt.executeUpdate(addFriendQuery);
				conn.commit();
			}
			stmt.executeUpdate(deleteQuery);
			conn.commit();

			if (SocialNetworkServer.DEBUG) {
				System.out.println(selectQuery);
				System.out.println(deleteQuery);
			}

			// confirmation to client
			String command = "print ";
			for (String user : approvedUsers) {
				command += user + ", ";
			}
			// substring to take off the last comma
			command = command.substring(0, command.length() - 2)
					+ " has been added to the system.";
			SharedKeyCryptoComm.send(command, os, c, sk);
		} catch (SQLException e) {
			e.printStackTrace();
			DBManager.rollback(conn);
		} finally {
			DBManager.trueAutoCommit(conn);
			DBManager.closeStatement(stmt);
			DBManager.closeConnection(conn);
		}

	}

	private String addFriendsFromGroupQuery(String username, int aid) {
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		ResultSet acappellaResult = null;
		String acappellaUsers = "SELECT username FROM main.users WHERE aid = "
				+ aid;
		try {
			stmt = conn.createStatement();
			acappellaResult = stmt.executeQuery(acappellaUsers);
			String addQuery = "INSERT INTO main.friends (username1, username2) VALUES ";
			String user1, user2;
			while (acappellaResult.next()) {
				if (username.compareTo(acappellaResult.getString("username")) < 0) {
					user1 = username;
					user2 = acappellaResult.getString("username");
					addQuery += "('" + user1 + "','" + user2 + "'), ";
				} else if (username.compareTo(acappellaResult
						.getString("username")) > 0) {
					user1 = acappellaResult.getString("username");
					user2 = username;
					addQuery += "('" + user1 + "','" + user2 + "'), ";
				}
			}
			// taking off the last comma, SQL doesn't like it
			return addQuery.substring(0, addQuery.length() - 2);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			DBManager.closeResultSet(acappellaResult);
			DBManager.closeStatement(stmt);
			DBManager.closeConnection(conn);
		}
	}

	private String getRegReq(String username) {
		String command = "";
		int requestCount = 0;
		Connection conn = null;
		Statement stmt = null;
		ResultSet requests = null;
		try {
			conn = DBManager.getConnection();
			stmt = conn.createStatement();
			String query = "SELECT COUNT(username) as count "
					+ "FROM main.registrationrequests "
					+ "WHERE aid = (SELECT aid FROM main.users WHERE username = '"
					+ username + "')";
			requests = stmt.executeQuery(query);
			while (requests.next()) {
				requestCount = requests.getInt("count");
			}
		} catch (SQLException e) {
			requestCount = 0;
			e.printStackTrace();
		} finally {
			DBManager.closeResultSet(requests);
			DBManager.closeStatement(stmt);
			DBManager.closeConnection(conn);
		}
		if (requestCount != 0) {
			command = ";print Pending User Registration Requests ("
					+ requestCount + ") [To view: regRequests]";
		}
		return command;
	}

	private void processAddFriend(String input) throws IOException {
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		ResultSet usersResult = null;
		try {
			stmt = conn.createStatement();
			// Stores a list of users that is not the current user, who is not a
			// friend of the
			// current user, and has not requested current user as friend
			// Users[0]: username. Users[1]: group name
			ArrayList<String[]> friendableUsers = new ArrayList<String[]>();

			// existing friends of user
			ArrayList<String> existingFriends = new ArrayList<String>();
			String query = "SELECT * FROM main.friends WHERE username1 = '"
					+ user + "' OR " + "username2 = '" + user + "'";
			ResultSet friendsResult = stmt.executeQuery(query);
			while (friendsResult.next()) {
				if (friendsResult.getString("username1").equals(user)) {
					existingFriends.add(friendsResult.getString("username2"));
				} else {
					existingFriends.add(friendsResult.getString("username1"));
				}
			}

			// list of users that is not the current user and who have not
			// requested
			// current user as friend
			query = "SELECT username, aname FROM main.users NATURAL JOIN main.acappella "
					+ "WHERE username != '"
					+ user
					+ "' AND username NOT IN "
					+ "(SELECT requester FROM main.friendrequests "
					+ "WHERE requestee = '" + user + "')";
			usersResult = stmt.executeQuery(query);
			while (usersResult.next()) {
				if (!existingFriends
						.contains(usersResult.getString("username"))) {
					String[] userInfo = { usersResult.getString("username"),
							usersResult.getString("aname") };
					friendableUsers.add(userInfo);
				}
			}

			// input of format "addFriend" or "addFriend b"
			if (input.equals("addFriend")) {
				// list everybody
				boolean userExist = false;
				String toFriend = "";

				while (!userExist) {
					String command = "print Users in the system:;";
					for (String[] userInfo : friendableUsers) {
						command += "print " + userInfo[0] + " (" + userInfo[1]
								+ ");";
					}
					command += "print ;print Type the name of the user you wish to friend:;"
							+ "askForInput";
					SharedKeyCryptoComm.send(command, os, c, sk);

					toFriend = SharedKeyCryptoComm.receive(is, c, sk);
					if (toFriend.equals("cancel")) {
						SharedKeyCryptoComm.send("", os, c, sk);
						return;
					}
					for (String[] userInfo : friendableUsers) {
						if (userInfo[0].equals(toFriend)) {
							userExist = true;
							break;
						}
					}
					if (!userExist) {
						SharedKeyCryptoComm.send("print Cannot friend " + toFriend + ";", os, c, sk);
					}
				}

				// Gets back name of person to add as friend
				addFriend(toFriend);
			} else {
				// selective list
				String value = getValue(input);
				boolean userExist = false;
				String toFriend = "";

				while (!userExist) {
					String command = "print Usernames starting with '" + value
							+ "';";
					for (String[] userInfo : friendableUsers) {
						value = value.toLowerCase();
						if (userInfo[0].toLowerCase().startsWith(value)) {
							command += "print " + userInfo[0] + " ("
									+ userInfo[1] + ");";
						}
					}
					command += "print ;print Type the name of the user you wish to friend:;"
							+ "askForInput";
					SharedKeyCryptoComm.send(command, os, c, sk);

					toFriend = SharedKeyCryptoComm.receive(is, c, sk);
					if (toFriend.equals("cancel")) {
						SharedKeyCryptoComm.send("", os, c, sk);
						return;
					}
					for (String[] userInfo : friendableUsers) {
						if (userInfo[0].equals(toFriend)) {
							userExist = true;
							break;
						}
					}
					if (!userExist) {
						SharedKeyCryptoComm.send("print Cannot friend " + toFriend + ";", os, c, sk);
					}
				}

				// Gets back name of person to add as friend
				addFriend(toFriend);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBManager.closeResultSet(usersResult);
			DBManager.closeStatement(stmt);
			DBManager.closeConnection(conn);
		}
	}

	private void addFriend(String username) throws IOException {
		// username exists in the system.
		SharedKeyCryptoComm.send("print Are you sure you want to add " + username
				+ " as a friend? (y/n);askForInput", os, c, sk);
		String input = SharedKeyCryptoComm.receive(is, c, sk);
		Connection conn = null;
		Statement stmt = null;
		if (input.equals("y")) {
			try {
				conn = DBManager.getConnection();
				stmt = null;
				String query = "INSERT INTO main.friendrequests (requestee, requester) "
						+ "VALUE ('" + username + "','" + user + "')";
				stmt = conn.createStatement();
				stmt.executeUpdate(query);

				// print out confirmation
				SharedKeyCryptoComm.send("print Friend request sent to " + username, os, c, sk);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBManager.closeStatement(stmt);
				DBManager.closeConnection(conn);
			}

		} else if (input.equals("n")) {
			SharedKeyCryptoComm.send("print Canceled.", os, c, sk);
		} else if (input.equals("cancel")) {
			SharedKeyCryptoComm.send("", os, c, sk);
		}
	}

	private void processFriendRequests() throws IOException {
		ArrayList<String> pendingFriends = new ArrayList<String>();
		int count = 0;
		Connection conn = null;
		Statement stmt = null;
		ResultSet requests = null;
		try {
			conn = DBManager.getConnection();
			stmt = conn.createStatement();
			String query = "SELECT requester " + "FROM main.friendrequests "
					+ "WHERE requestee = '" + user + "'";
			requests = stmt.executeQuery(query);
			while (requests.next()) {
				pendingFriends.add(requests.getString("requester"));
				count++;
			}
			stmt.close();
		} catch (SQLException e) {
			count = 0;
			e.printStackTrace();
		} finally {
			DBManager.closeResultSet(requests);
			DBManager.closeStatement(stmt);
			DBManager.closeConnection(conn);
		}

		if (count > 0) {
			String command = "print Pending Friend Requests (" + count + "):";
			for (int i = 0; i < pendingFriends.size(); i++) {
				command = command + ";print " + pendingFriends.get(i);
			}
			command = command + ";print ;print [To approve: approve "
					+ "<username1>, <username2>];print [To remove: "
					+ "remove <username1>, <username2>];askForInput";
			SharedKeyCryptoComm.send(command, os, c, sk);
			friendApproval(SharedKeyCryptoComm.receive(is, c, sk));
		} else {
			SharedKeyCryptoComm.send("print No pending friend requests at the moment.", os, c, sk);
		}
	}

	private void friendApproval(String input) {
		if (input.equals("cancel")) {
			SharedKeyCryptoComm.send("", os, c, sk);
			return;
		}
		if (input.matches("^approve.+")) {
			String value = getValue(input);
			String delim = ",";
			String[] approvedFriends = value.split(delim);
			friendApprove(approvedFriends);
			return;
		}
		if (input.matches("^remove.+")) {
			String value = getValue(input);
			String delim = ",";
			String[] usersToDelete = value.split(delim);

			// Building queries
			String deleteQuery = "DELETE FROM main.friendrequests WHERE ";
			for (int i = 0; i < usersToDelete.length; i++) {
				usersToDelete[i] = usersToDelete[i].trim();

				deleteQuery += "(requester = " + quote(usersToDelete[i])
						+ " AND requestee = " + quote(user) + ")";
				if (i != usersToDelete.length - 1) {
					deleteQuery += " OR ";
				}
			}

			if (SocialNetworkServer.DEBUG) {
				System.out.println(deleteQuery);
			}
			Connection conn = null;
			Statement stmt = null;
			try {
				conn = DBManager.getConnection();
				stmt = conn.createStatement();
				stmt.executeUpdate(deleteQuery);

				// confirmation to client
				String command = "print Friend requests from ";
				for (String user : usersToDelete) {
					command += user + ", ";
				}
				// substring to take off the last comma
				command = command.substring(0, command.length() - 2)
						+ " have been deleted.";
				SharedKeyCryptoComm.send(command, os, c, sk);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBManager.closeStatement(stmt);
				DBManager.closeConnection(conn);
			}

			return;
		}
	}

	private void friendApprove(String[] approvedUsers) {
		// Building queries
		String deleteQuery = "DELETE FROM main.friendrequests WHERE ";
		String insertQuery = "INSERT INTO main.friends (username1, username2) VALUES ";

		for (int i = 0; i < approvedUsers.length; i++) {
			approvedUsers[i] = approvedUsers[i].trim();

			deleteQuery += "(requester = " + quote(approvedUsers[i])
					+ " AND requestee = " + quote(user) + ")";
			String user1, user2;
			if (user.compareTo(approvedUsers[i]) < 0) {
				user1 = quote(user);
				user2 = quote(approvedUsers[i]);
			} else {
				user1 = quote(approvedUsers[i]);
				user2 = quote(user);
			}
			insertQuery += "(" + user1 + "," + user2 + "), ";
			if (i != approvedUsers.length - 1) {
				deleteQuery += " OR ";
			}
		}
		// taking off the last comma, SQL doesn't like it
		insertQuery = insertQuery.substring(0, insertQuery.length() - 2);
		Connection conn = null;
		Statement stmt = null;
		try {
			// building the insert into users table query
			conn = DBManager.getConnection();
			conn.setAutoCommit(false);
			stmt = conn.createStatement();

			if (SocialNetworkServer.DEBUG) {
				System.out.println(insertQuery);
				System.out.println(deleteQuery);
			}

			// execute insertion and deletion queries
			stmt.executeUpdate(insertQuery);
			stmt.executeUpdate(deleteQuery);
			conn.commit();

			// confirmation to client
			String command = "print ";
			for (String user : approvedUsers) {
				command += user + ", ";
			}
			// substring to take off the last comma
			command = command.substring(0, command.length() - 2)
					+ " have been added as your friends.";
			SharedKeyCryptoComm.send(command, os, c, sk);
		} catch (SQLException e) {
			e.printStackTrace();
			DBManager.rollback(conn);
		} finally {
			DBManager.trueAutoCommit(conn);
			DBManager.closeStatement(stmt);
			DBManager.closeConnection(conn);
		}
	}

	private String getFriendReq(String username) {
		String command = "";
		int requestCount = 0;
		Connection conn = null;
		Statement stmt = null;
		ResultSet requests = null;
		try {
			conn = DBManager.getConnection();
			stmt = conn.createStatement();
			String query = "SELECT COUNT(requestee) as count "
					+ "FROM main.friendrequests " + "WHERE requestee = '"
					+ username + "'";
			requests = stmt.executeQuery(query);
			while (requests.next()) {
				requestCount = requests.getInt("count");
			}
		} catch (SQLException e) {
			requestCount = 0;
			e.printStackTrace();
		} finally {
			DBManager.closeResultSet(requests);
			DBManager.closeStatement(stmt);
			DBManager.closeConnection(conn);
		}
		if (requestCount != 0) {
			command = ";print Pending Friend Requests (" + requestCount
					+ ") [To view: friendRequests]";
		}
		return command;
	}

	private void processDeleteUser() throws IOException {
		// TODO: check to see if user is actually a SA
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		ResultSet usersResult = null;

		// Stores a list of deletable users
		ArrayList<String> deletableUsers = new ArrayList<String>();
		try {
			stmt = conn.createStatement();

			String query = "SELECT username FROM main.users "
					+ "WHERE username != '" + user + "' AND aid = "
					+ "(SELECT aid FROM main.users WHERE username = '" + user
					+ "')";

			if (SocialNetworkServer.DEBUG) {
				System.out
						.println("Delete User deletable user query: " + query);
			}

			usersResult = stmt.executeQuery(query);
			while (usersResult.next()) {
				deletableUsers.add(usersResult.getString("username"));
			}

			boolean userDeletable = false;
			String toDelete = "";

			while (!userDeletable) {
				String command = "print Users in your A Cappella group that you can delete:;";
				for (String userInfo : deletableUsers) {
					command += "print " + userInfo + ";";
				}
				command += "print ;print Type the name of the user you wish to delete:;"
						+ "askForInput";
				SharedKeyCryptoComm.send(command, os, c, sk);

				toDelete = SharedKeyCryptoComm.receive(is, c, sk);
				if (toDelete.equals("cancel")) {
					SharedKeyCryptoComm.send("", os, c, sk);
					return;
				}
				if (deletableUsers.contains(toDelete)) {
					userDeletable = true;
				}
				if (!userDeletable) {
					SharedKeyCryptoComm.send("print Cannot delete " + toDelete + ";", os, c, sk);
				}
			}

			// toDelete is deletable
			deleteUser(toDelete);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBManager.closeResultSet(usersResult);
			DBManager.closeStatement(stmt);
			DBManager.closeConnection(conn);
		}

	}

	private void deleteUser(String username) throws IOException {
		// username is a deletable user
		SharedKeyCryptoComm.send("print User deletions cannot be undone.;"
				+ "print Are you sure you want to delete this user? (y/n);askForInput", os, c, sk);
		String input = SharedKeyCryptoComm.receive(is, c, sk);
		Connection conn = null;
		Statement stmt = null;
		if (input.equals("y")) {
			try {
				conn = DBManager.getConnection();
				String query = "DELETE FROM main.users WHERE username = '"
						+ username + "'";
				stmt = conn.createStatement();
				stmt.executeUpdate(query);

				// print out confirmation
				SharedKeyCryptoComm.send("print " + username
						+ " has been deleted from the system.", os, c, sk);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				DBManager.closeStatement(stmt);
				DBManager.closeConnection(conn);
			}

		} else if (input.equals("n")) {
			SharedKeyCryptoComm.send("print Canceled.", os, c, sk);
		} else if (input.equals("cancel")) {
			SharedKeyCryptoComm.send("", os, c, sk);
		}
	}

	private void processShowFriends() {
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		ResultSet friendsResult = null;

		// get the list of friends for the current user
		ArrayList<String> existingFriends = new ArrayList<String>();
		try {
			String query = "SELECT * FROM main.friends WHERE username1 = '"
					+ user + "' OR " + "username2 = '" + user + "'";
			stmt = conn.createStatement();
			friendsResult = stmt.executeQuery(query);
			while (friendsResult.next()) {
				if (friendsResult.getString("username1").equals(user)) {
					existingFriends.add(friendsResult.getString("username2"));
				} else {
					existingFriends.add(friendsResult.getString("username1"));
				}
			}

			// printing out friend list
			String command = "print Your friends:;";
			if (existingFriends.size() == 0) {
				command = "print You have no friends right now.;"
						+ "print To add a friend: type addFriend";
			} else {
				for (String friend : existingFriends) {
					command += "print " + friend + ";";
				}
			}
			SharedKeyCryptoComm.send(command, os, c, sk);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBManager.closeResultSet(friendsResult);
			DBManager.closeStatement(stmt);
			DBManager.closeConnection(conn);
		}

	}

	private void processChangeUserRole() throws IOException {
		// TODO: check to see if user is actually a SA
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		ResultSet usersResult = null;
		try {
			stmt = conn.createStatement();

			// Stores a list of changeable users
			// Users[0]: username, Users[1]: role
			ArrayList<String[]> changeableUsers = new ArrayList<String[]>();

			String query = "SELECT username, role FROM main.users "
					+ "WHERE username != '" + user + "' AND aid = "
					+ "(SELECT aid FROM main.users WHERE username = '" + user
					+ "')";

			if (SocialNetworkServer.DEBUG) {
				System.out.println("role: changeable role query: " + query);
			}

			usersResult = stmt.executeQuery(query);
			while (usersResult.next()) {
				String[] userInfo = { usersResult.getString("username"),
						usersResult.getString("role").toUpperCase() };
				changeableUsers.add(userInfo);
			}

			boolean userChangeable = false;
			String toChange = "";
			String role = "";

			while (!userChangeable) {
				String command = "print Users in your A Cappella group that you can change roles for:;";
				for (String[] userInfo : changeableUsers) {
					command += "print " + userInfo[0] + " (" + userInfo[1]
							+ ");";
				}
				command += "print ;print Type the name of the user you wish to change role for:;"
						+ "askForInput";
				SharedKeyCryptoComm.send(command, os, c, sk);

				toChange = SharedKeyCryptoComm.receive(is, c, sk);
				if (toChange.equals("cancel")) {
					SharedKeyCryptoComm.send("", os, c, sk);
					return;
				}
				for (String[] userInfo : changeableUsers) {
					if (toChange.equals(userInfo[0])) {
						if (userInfo[1].equals("ADMIN")) {
							role = "member";
						} else {
							role = "admin";
						}
						userChangeable = true;
						break;
					}
				}
				if (!userChangeable) {
					SharedKeyCryptoComm.send("print Cannot change role for " + toChange + ";", os, c, sk);
				}
			}

			// toChange is changeable
			changeRole(toChange, role);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBManager.closeResultSet(usersResult);
			DBManager.closeStatement(stmt);
			DBManager.closeConnection(conn);
		}

	}

	private void changeRole(String toChange, String role) {
		// change user's role in the DB to 'role'
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		String query = "UPDATE main.users SET role = '" + role
				+ "' WHERE username = '" + toChange + "'";
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(query);

			// print out confirmation on client.
			String from;
			if (role.equals("admin")) {
				from = "MEMBER";
			} else {
				from = "ADMIN";
			}
			SharedKeyCryptoComm.send("print Role for " + toChange
					+ " has been changed from " + from + " to "
					+ role.toUpperCase(), os, c, sk);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBManager.closeStatement(stmt);
			DBManager.closeConnection(conn);
		}

	}

	private void processTransferSA() throws IOException {
		// TODO: check to see if user is actually a SA
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		ResultSet usersResult = null;
		try {
			stmt = conn.createStatement();

			// Stores a list of changeable users
			// Users[0]: username
			ArrayList<String> groupAdmins = new ArrayList<String>();

			String query = "SELECT username FROM main.users "
					+ "WHERE username != '" + user + "' AND aid = "
					+ "(SELECT aid FROM main.users WHERE username = '" + user
					+ "') AND " + "role = 'admin'";

			if (SocialNetworkServer.DEBUG) {
				System.out.println("transfer: adminUser query: " + query);
			}

			usersResult = stmt.executeQuery(query);
			while (usersResult.next()) {
				groupAdmins.add(usersResult.getString("username"));
			}

			boolean transferableUser = false;
			String toChange = "";

			while (!transferableUser) {
				String command = "print Users in your A Cappella group that you can transfer SA tole to:;";
				for (String userInfo : groupAdmins) {
					command += "print " + userInfo + " (ADMIN);";
				}
				command += "print ;print Type the name of the user you wish to transfer SA role to:;"
						+ "askForInput";
				SharedKeyCryptoComm.send(command, os, c, sk);

				toChange = SharedKeyCryptoComm.receive(is, c, sk);
				if (toChange.equals("cancel")) {
					SharedKeyCryptoComm.send("", os, c, sk);
					return;
				}
				if (groupAdmins.contains(toChange)) {
					transferableUser = true;
				}
				if (!transferableUser) {
					SharedKeyCryptoComm.send("print Cannot transfer SA role to " + toChange
							+ ";", os, c, sk);
				}
			}

			// toChange is an admin that can have SA transferred to
			transferSA(toChange);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBManager.closeResultSet(usersResult);
			DBManager.closeStatement(stmt);
			DBManager.closeConnection(conn);
		}

	}

	private void transferSA(String toChange) {
		// transfer SA-ship
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		String promoteQuery = "UPDATE main.users SET role = 'sa' WHERE username = '"
				+ toChange + "'";
		String demoteQuery = "UPDATE main.users SET role = 'admin' WHERE username = '"
				+ user + "';";
		try {
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			stmt.executeUpdate(promoteQuery);
			stmt.executeUpdate(demoteQuery);
			conn.commit();

			// Print confirmation to client
			SharedKeyCryptoComm.send("print SA role has been transferred to " + toChange, os, c, sk);
		} catch (SQLException e) {
			e.printStackTrace();
			DBManager.rollback(conn);
		} finally {
			DBManager.closeStatement(stmt);
			DBManager.closeConnection(conn);
		}

	}

	private void processParticipants() {
		ArrayList<String> admins = getBoardAdmins();
		if (admins.contains(user)) {
			// print displaying participants
			String board = currentPath[0];
			String region = currentPath[1];
			String command = "print Displaying participants:;";

			// print a list of participants of the region
			Connection conn = DBManager.getConnection();
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				String query = "SELECT username, privilege FROM " + board
						+ ".regionprivileges WHERE rname = '" + region + "'";
				ResultSet partResult = stmt.executeQuery(query);
				while (partResult.next()) {
					command += "print " + partResult.getString("username");
					if (partResult.getString("privilege").equals("view")) {
						command += " (view only);";
					} else {
						command += ";";
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			// print Commands: addParticipants, removeParticipants,
			// editParticipants, addAdmin
			command += "print ;print Other Commands: addParticipants, removeParticipants";
			// TODO: add edit and addAdmin to this list of commands
			SharedKeyCryptoComm.send(command, os, c, sk);
		} else {
			SharedKeyCryptoComm.send("print You do not have permission to view participants.", os, c, sk);
		}
	}

	private void processAddParticipants() throws IOException {
		// check if user is admin
		ArrayList<String> admins = getBoardAdmins();
		if (admins.contains(user)) {
			// get a list of the user's friends
			ArrayList<String> usersFriends = getCurrentUsersFriends();

			// get a list of participants
			String board = currentPath[0];
			String region = currentPath[1];
			ArrayList<String> participants = new ArrayList<String>();
			Connection conn = DBManager.getConnection();
			Statement stmt = null;
			String query = "SELECT username, privilege FROM " + board
					+ ".regionprivileges WHERE rname = '" + region + "'";
			try {
				stmt = conn.createStatement();
				ResultSet partResult = stmt.executeQuery(query);
				while (partResult.next()) {
					participants.add(partResult.getString("username"));
				}
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			ArrayList<String> addableUsers = new ArrayList<String>();
			for (String friend : usersFriends) {
				if (!participants.contains(friend)) {
					addableUsers.add(friend);
				}
			}
			boolean userAddable = false;
			ArrayList<String[]> addInfo = new ArrayList<String[]>();

			while (!userAddable) {
				String command = "print List of people you could add:;";
				for (String user : addableUsers) {
					command += "print " + user + ";";
				}
				command += "print ;print [To add user: (<user1>, <privilege>), "
						+ "(<user2>, <privilege>) where <privilege> = view or viewpost];askForInput";
				SharedKeyCryptoComm.send(command, os, c, sk);
				String input = SharedKeyCryptoComm.receive(is, c, sk);
				if (input.equals("cancel")) {
					SharedKeyCryptoComm.send("", os, c, sk);
					return;
				}
				addInfo = parseAddUserInfo(input);
				String notOkUsers = "";
				userAddable = true;
				for (String[] userInfo : addInfo) {
					if (!addableUsers.contains(userInfo[0])) {
						userAddable = false;
						notOkUsers += userInfo[0] + ", ";
					}
				}
				if (!userAddable) {
					notOkUsers = notOkUsers.substring(0,
							notOkUsers.length() - 2);
					SharedKeyCryptoComm.send("print Cannot add " + notOkUsers
							+ " as participants of this region.;", os, c, sk);
				}
			}

			// toAdd is addable
			addParticipant(addInfo);
		} else {
			SharedKeyCryptoComm.send("print You do not have permission to add participants to this region.", os, c, sk);
		}
	}

	private ArrayList<String[]> parseAddUserInfo(String input) {
		ArrayList<String[]> addInfo = new ArrayList<String[]>();
		String[] infoPairs = input.split(" *, *");
		for (String pair : infoPairs) {
			pair = pair.trim();
			String[] userInfo = pair.substring(1, pair.length() - 1).trim()
					.split(" *, *");
			addInfo.add(userInfo);
		}

		return addInfo;
	}

	private void addParticipant(ArrayList<String[]> addInfo) {
		// add everything to the database
		String region = currentPath[1];
		Connection conn = DBManager.getConnection();
		String query = "INSERT INTO board.regionprivileges (rname, username, privilege, grantedBy) VALUES ";
		for (String[] userInfo : addInfo) {
			query += "('" + region + "', '" + userInfo[0] + "', '"
					+ userInfo[1] + "', '" + user + "'), ";
		}
		query = query.substring(0, query.length() - 2);

		if (SocialNetworkServer.DEBUG) {
			System.out.println("addParticipants query: " + query);
		}

		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(query);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// print confirmation
		SharedKeyCryptoComm.send("print Participants added.", os, c, sk);
	}

	private void processRemoveParticipants() throws IOException {
		// TODO: What's the policy for deleting admins from a board?

		// check if user is admin
		ArrayList<String> admins = getBoardAdmins();
		if (admins.contains(user)) {
			// get a list of participants
			String board = currentPath[0];
			String region = currentPath[1];
			ArrayList<String> participants = new ArrayList<String>();
			Connection conn = DBManager.getConnection();
			Statement stmt = null;
			String query = "SELECT username, privilege FROM " + board
					+ ".regionprivileges WHERE rname = '" + region + "'";
			try {
				stmt = conn.createStatement();
				ResultSet partResult = stmt.executeQuery(query);
				while (partResult.next()) {
					participants.add(partResult.getString("username"));
				}
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			boolean userRemovable = false;
			String[] toRemove = null;

			while (!userRemovable) {
				String command = "print List of people you could remove:;";
				for (String user : participants) {
					command += "print " + user + ";";
				}
				command += "print ;print [To remove participants: <user1>, <user2>];askForInput";
				SharedKeyCryptoComm.send(command, os, c, sk);
				String input = SharedKeyCryptoComm.receive(is, c, sk);
				if (input.equals("cancel")) {
					SharedKeyCryptoComm.send("", os, c, sk);
					return;
				}
				toRemove = input.trim().split(" *, *");

				String notOkUsers = "";
				userRemovable = true;
				for (String userInfo : toRemove) {
					if (!participants.contains(userInfo)) {
						userRemovable = false;
						notOkUsers += userInfo + ", ";
					}
				}
				if (!userRemovable) {
					notOkUsers = notOkUsers.substring(0,
							notOkUsers.length() - 2);
					SharedKeyCryptoComm.send("print Cannot remove " + notOkUsers
							+ " from this region.;", os, c, sk);
				}
			}

			// toRemove is removable
			removeParticipant(toRemove);
		} else {
			SharedKeyCryptoComm.send("print You do not have permission to add participants to this region.", os, c, sk);
		}
	}

	private void removeParticipant(String[] toRemove) {
		// add everything to the database
		String region = currentPath[1];
		String board = currentPath[0];
		Connection conn = DBManager.getConnection();
		String query = "DELETE FROM " + board
				+ ".regionprivileges WHERE rname = '" + region + "' AND (";
		for (int i = 0; i < toRemove.length; i++) {
			query += "username = '" + toRemove[i] + "'";
			if (i != toRemove.length - 1) {
				query += " OR ";
			}
		}
		query += ")";

		if (SocialNetworkServer.DEBUG) {
			System.out.println("remove participant query: " + query);
		}

		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(query);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// print confirmation
		SharedKeyCryptoComm.send("print Participants removed.", os, c, sk);
	}

	private void processEditParticipants() throws IOException {
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		String board = currentPath[0];
		String region = currentPath[1];

		// Stores a list of changeable users
		// Users[0]: username, Users[1]: permission
		ArrayList<String[]> changeableUsers = new ArrayList<String[]>();
		try {
			String query = "SELECT username, privilege FROM " + board
					+ ".regionprivileges " + "WHERE rname = '" + region + "'";

			if (SocialNetworkServer.DEBUG) {
				System.out.println("Edit participant query: " + query);
			}

			ResultSet usersResult = stmt.executeQuery(query);
			while (usersResult.next()) {
				String[] userInfo = { usersResult.getString("username"),
						usersResult.getString("privilege") };
				changeableUsers.add(userInfo);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		boolean userChangeable = false;
		String input = "";
		ArrayList<String[]> changingUsersInfo = new ArrayList<String[]>();
		String[] usersToChange = null;

		while (!userChangeable) {
			String command = "print List of people you can edit permission for:;";
			for (String[] userInfo : changeableUsers) {
				String permission = null;
				if (userInfo[1].equals("view")) {
					permission = "View Only";
				} else {
					permission = "View and Post";
				}
				command += "print " + userInfo[0] + " (" + permission + ");";
			}
			command += "print ;print [To toggle permission status: <user1>, <user2>];"
					+ "askForInput";
			SharedKeyCryptoComm.send(command, os, c, sk);

			input = SharedKeyCryptoComm.receive(is, c, sk);
			if (input.equals("cancel")) {
				SharedKeyCryptoComm.send("", os, c, sk);
				return;
			}
			String toChange = "";
			usersToChange = input.trim().split(" *, *");
			userChangeable = true;
			for (String user : usersToChange) {
				boolean userChangeable2 = false;
				for (String[] userInfo : changeableUsers) {
					if (user.equals(userInfo[0])) {
						changingUsersInfo.add(userInfo);
						userChangeable2 = true;
						break;
					}
				}
				if (!userChangeable2) {
					toChange += "user, ";
					userChangeable = false;
				}
			}
			if (!userChangeable) {
				toChange = toChange.substring(0, toChange.length() - 2);
				SharedKeyCryptoComm.send("print Cannot change permission for " + toChange
						+ ";", os, c, sk);
			}
		}

		// toChange is changeable
		changePermission(usersToChange);
	}

	private void changePermission(String[] usersToChange) {
		// TODO: not done
		// add everything to the database
		String board = currentPath[0];
		String region = currentPath[1];
		Connection conn = DBManager.getConnection();

		String query = "UPDATE " + board + ".regionprivileges WHERE rname = '"
				+ region + "' AND (";
		// (username = 'userInfo' OR username = 'userInfo')
		for (int i = 0; i < usersToChange.length; i++) {
			query += "username = '" + usersToChange[i] + "'";
			if (i != usersToChange.length - 1) {
				query += " OR ";
			}
		}
		query += ")";

		if (SocialNetworkServer.DEBUG) {
			System.out.println("remove participant query: " + query);
		}

		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(query);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// print confirmation
		SharedKeyCryptoComm.send("print Participants removed.", os, c, sk);
	}

	/**
	 * Creates a board. MUST be in the home directory.
	 */
	private void processCreateBoard(String input) throws IOException {
		/*
		 * Ensure the person is in the right place to create a board (on the
		 * homepage)
		 */
		if (currentPath[0] != null) {
			SharedKeyCryptoComm.send("print Must be at Home to create a board", os, c, sk);
		} else {
			String boardname = input.substring(("createBoard ").length());
			SharedKeyCryptoComm.send(SocialNetworkBoards.createBoard(user, boardname), os, c, sk);
		}
	}

	/**
	 * Depending on where the user is, fetches the correct view of information.
	 * The user prints their current path and the information associated with
	 * it.
	 */
	private void processRefresh() {
		String boardName = currentPath[0];
		if (boardName == null) {
			SharedKeyCryptoComm.send(SocialNetworkNavigation.printPath(currentPath)
					+ "print ;" + SocialNetworkBoards.viewBoards(user), os, c, sk);
		} else if (boardName.equals("freeforall")) {
			/* No regions */
			String postNum = currentPath[1];
			if (postNum == null) { // Merely in the board
				SharedKeyCryptoComm.send(SocialNetworkNavigation.printPath(currentPath)
						+ "print ;"
						+ SocialNetworkPosts
								.viewPostList(user, boardName, null), os, c, sk);
			} else { // Inside the post
				SharedKeyCryptoComm.send(SocialNetworkNavigation.printPath(currentPath)
						+ "print ;"
						+ SocialNetworkPosts.viewPost(user, boardName, null,
								Integer.parseInt(postNum)), os, c, sk);
			}
		} else { // a regular board
			String regionName = currentPath[1];
			if (regionName == null) { // Merely in the board
				SharedKeyCryptoComm.send(SocialNetworkNavigation.printPath(currentPath)
						+ "print ;"
						+ SocialNetworkRegions.viewRegions(user, boardName), os, c, sk);
			} else {
				String postNum = currentPath[2];
				if (postNum == null) { // Merely in the region
					SharedKeyCryptoComm.send(SocialNetworkNavigation.printPath(currentPath)
							+ "print ;"
							+ SocialNetworkPosts.viewPostList(user, boardName,
									regionName), os, c, sk);
				} else { // Inside the post
					SharedKeyCryptoComm.send(SocialNetworkNavigation.printPath(currentPath)
							+ "print ;"
							+ SocialNetworkPosts.viewPost(user, boardName,
									regionName, Integer.parseInt(postNum)), os, c, sk);
				}
			}
		}
	}

	/**
	 * Depending on where the user is, processes where the user should go.
	 */
	private void processGoto(String inputLine) {
		String destination = inputLine.substring(("goto ").length());
		int validDest = SocialNetworkNavigation.validDestination(currentPath,
				destination);
		switch (validDest) {
		case -1: /* Go backwards */
			SocialNetworkNavigation.goBack(currentPath);
			processRefresh();
			break;
		case 2: /* Go immediately home */
			for (int i = 0; i < currentPath.length; i++) {
				currentPath[i] = null;
			}
			processRefresh();
			break;
		case 1: /* Go forward in the hierarchy to destination */
			/* Have different cases depending on the current path */
			if (currentPath[0] == null) {
				SharedKeyCryptoComm.send(SocialNetworkNavigation.goToBoard(user,
						currentPath, destination), os, c, sk);
			} else if (currentPath[0].equals("freeforall")) {
				Integer postNum = null;
				try {
					postNum = Integer.parseInt(destination);
				} catch (NumberFormatException e) {
					SharedKeyCryptoComm.send("print You entered an invalid post number. Type \"goto ###\", or \"goto ..\" to "
							+ "go backwards", os, c, sk);
				}
				if (postNum != null) {
					SharedKeyCryptoComm.send(SocialNetworkNavigation.goToPost(user,
							currentPath, postNum.intValue()), os, c, sk);
				}
			} else {
				if (currentPath[1] != null) {
					Integer postNum = null;
					try {
						postNum = Integer.parseInt(destination);
					} catch (NumberFormatException e) {
						SharedKeyCryptoComm.send("print You entered an invalid post number. Type \"goto ###\", or \"goto ..\" to "
								+ "go backwards", os, c, sk);
					}
					if (postNum != null) {
						SharedKeyCryptoComm.send(SocialNetworkNavigation.goToPost(user,
								currentPath, postNum), os, c, sk);
					}
				} else {
					SharedKeyCryptoComm.send(SocialNetworkNavigation.goToRegion(user,
							currentPath, destination), os, c, sk);
				}
			}
			break;
		default:
			SharedKeyCryptoComm.send("print Invalid destination given your current path: "
					+ SocialNetworkNavigation.printPath(currentPath) + ".; "
					+ "print You can go backwards by typing \"..\" ", os, c, sk);

		}
	}

	/**
	 * Creates a region for the user. The user must be in a board (except
	 * freeforall) to execute the command.
	 */
	private void processCreateRegion(String inputLine) {
		String boardName = currentPath[0];
		String regionName = inputLine.substring(("createRegion ").length());
		if (boardName == null) {
			SharedKeyCryptoComm.send("print Must be in the desired board in order to create the region.", os, c, sk);
		} else if (boardName.equals("freeforall")) {
			SharedKeyCryptoComm.send("print Cannot create regions in the freeforall board.", os, c, sk);
		} else if (currentPath[1] != null) {
			SharedKeyCryptoComm.send("print Must be exactly in the desired board (i.e., not inside a region in the board) "
					+ "in order to create the region", os, c, sk);
		} else {
			SharedKeyCryptoComm.send(SocialNetworkRegions.createRegion(user, currentPath[0],
					regionName), os, c, sk);
		}
	}

	private void processPost() throws IOException {
		/* Verify the user is in the right place to create a post */
		String boardName = currentPath[0];
		boolean canPost = false;
		if (boardName == null) {
			SharedKeyCryptoComm.send("print Must be within a board's region or in the freeforall board to create a post", os, c, sk);
		} else if (boardName.equals("freeforall")) {
			String postNum = currentPath[1];
			if (postNum == null) {
				canPost = true;
			} else {
				SharedKeyCryptoComm.send("print Must go back to the board page to create a post (not inside a post)", os, c, sk);
			}
		} else { // in a regular board
			String regionName = currentPath[1];
			if (regionName == null) {
				SharedKeyCryptoComm.send("print Must be within a board's region or in the freeforall board to create a post", os, c, sk);
			} else {
				String postNum = currentPath[2];
				if (postNum == null) { // in a board, region, not in a post
					canPost = true;
				} else {
					SharedKeyCryptoComm.send("print Must go back to the region page to create a post (not inside a post)", os, c, sk);
				}
			}
		}
		if (canPost) {
			SharedKeyCryptoComm.send("print Start typing your content. Type 'cancel' after any new line to cancel.;print "
					+ "Press enter once to insert a new line.;print Press enter twice to submit.;askForInput ", os, c, sk);
			String content = SharedKeyCryptoComm.receive(is, c, sk);
			while (content.equals("")) {
				SharedKeyCryptoComm.send("print Content is empty. Please try again. Type 'cancel' to cancel.;askForInput ", os, c, sk);
				content = SharedKeyCryptoComm.receive(is, c, sk);
			}
			boolean cancelled = content.trim().equals("cancel");
			String additionalContent = "";
			while (!cancelled) {
				SharedKeyCryptoComm.send("print ;askForInput ", os, c, sk);
				additionalContent = SharedKeyCryptoComm.receive(is, c, sk);
				if (additionalContent.equals("")) {
					break;
				} else if (additionalContent.trim().equals("cancel")) {
					cancelled = true;
				} else {
					content += ";print \t" + additionalContent;
				}
			}
			if (cancelled) {
				SharedKeyCryptoComm.send("print Post Creation cancelled", os, c, sk);
			} else {
				SharedKeyCryptoComm.send(SocialNetworkPosts.createPost(user, content,
						currentPath[0], currentPath[1]), os, c, sk);
			}
		}
	}

	/**
	 * Similar to processPost basically... except that you must be in a post
	 * 
	 * @throws IOException
	 */
	private void processReply() throws IOException {
		/* Verify the user is in the right place to create a post */
		String boardName = currentPath[0];
		String postNum = "";
		boolean canReply = false;
		if (boardName == null) {
			SharedKeyCryptoComm.send("print Must be within a post to create a reply", os, c, sk);
		} else if (boardName.equals("freeforall")) {
			postNum = currentPath[1];
			if (postNum == null) {
				SharedKeyCryptoComm.send("print Must be within a post to create a reply", os, c, sk);
			} else {
				canReply = true;
			}
		} else { // in a regular board
			String regionName = currentPath[1];
			if (regionName == null) {
				SharedKeyCryptoComm.send("print Must be within a post to create a reply", os, c, sk);
			} else {
				postNum = currentPath[2];
				if (postNum == null) { // in a board, region, not in a post
					SharedKeyCryptoComm.send("print Must be within a post to create a reply", os, c, sk);
				} else {
					canReply = true;
				}
			}
		}
		if (canReply) {
			SharedKeyCryptoComm.send("print Start typing your content. Type 'cancel' after any new line to cancel.;print "
					+ "Press enter once to insert a new line.;print Press enter twice to submit.;askForInput ", os, c, sk);
			String content = SharedKeyCryptoComm.receive(is, c, sk);
			while (content.equals("")) {
				SharedKeyCryptoComm.send("print Content is empty. Please try again. Type 'cancel' to cancel.;askForInput ", os, c, sk);
				content = SharedKeyCryptoComm.receive(is, c, sk);
			}
			boolean cancelled = content.trim().equals("cancel");
			String additionalContent = "";
			while (!cancelled) {
				SharedKeyCryptoComm.send("print ;askForInput ", os, c, sk);
				additionalContent = SharedKeyCryptoComm.receive(is, c, sk);
				if (additionalContent.equals("")) {
					break;
				} else if (additionalContent.trim().equals("cancel")) {
					cancelled = true;
				} else {
					content += ";print \t" + additionalContent;
				}
			}
			if (cancelled) {
				SharedKeyCryptoComm.send("print Reply Creation cancelled", os, c, sk);

			} else {
				SharedKeyCryptoComm.send(SocialNetworkPosts.createReply(user, content,
						currentPath[0], currentPath[1],
						Integer.parseInt(postNum)), os, c, sk);
			}
		}
	}
}
