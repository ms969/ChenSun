package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import shared.InputProcessor;

import database.DBManager;


public class ServerInputProcessor extends InputProcessor {
	private PrintWriter out;
	private BufferedReader in;
	
	private String user = null;
	private String[] currentPath;
	
	public static final String[] COMMANDS = {
		"^login .+",		// 0
		"^register$",		// 1
		"^regRequests$",	// 2
		"^addFriend.*",		// 3
		"^createBoard .+",  // 4
		"^refresh$",        // 5
		"^goto .+",         // 6
		"^createRegion .+", // 7
		"^post$",           // 8
		"^reply$",          // 9
		"^friendRequests$",	// 10
		"^deleteUser$",		// 11
		"^showFriends$",	// 12
	};
	
	public void processCommand(String inputLine) throws IOException {
		if (inputLine.matches(COMMANDS[0])) {
			if (user == null) {
				processLogin(inputLine);
			} else {
				out.println();
			}
			return;
		}
		if (inputLine.matches(COMMANDS[1])) {
			if (user == null) {
				processRegistration();
			} else {
				out.println();
			}
			return;
		}
		if (inputLine.matches(COMMANDS[2])) {
			if (user != null) {
				processRegRequests();
			} else {
				out.println();
			}
			return;
		}
		if (inputLine.matches(COMMANDS[3])) {
			if (user != null) {
				processAddFriend(inputLine);
			} else {
				out.println();
			}
			return;
		}
		if (inputLine.matches(COMMANDS[4])) {
			if (user != null) {
				processCreateBoard(inputLine);
			} else {
				out.println();
			}
			return;
		}
		if (inputLine.matches(COMMANDS[5])) {
			if (user != null) {
				processRefresh();
			} else {
				out.println();
			}
			return;
		}
		if (inputLine.matches(COMMANDS[6])) {
			if (user != null) {
				processGoto(inputLine);
			} else {
				out.println();
			}
			return;
		}
		if (inputLine.matches(COMMANDS[7])) {
			if (user != null) {
				processCreateRegion(inputLine);
			} else {
				out.println();
			}
			return;
		}
		if (inputLine.matches(COMMANDS[8])) {
			if (user != null) {
				processPost();
			} else {
				out.println();
			}
			return;
		}
		if (inputLine.matches(COMMANDS[9])) {
			if (user != null) {
				processReply();
			} else {
				out.println();
			}
			return;
		}
		if (inputLine.matches(COMMANDS[10])) {
			if (user != null) {
				processFriendRequests();
			} else {
				out.println();
			}
		}
		if (inputLine.matches(COMMANDS[11])) {
			if (user != null) {
				processDeleteUser();
			} else {
				out.println();
			}
		}
		if (inputLine.matches(COMMANDS[12])) {
			if (user != null) {
				processShowFriends();
			}
		}
		out.println();
	}

	public ServerInputProcessor(PrintWriter out, BufferedReader in) {
		this.out = out;
		this.in = in;
		this.currentPath = new String[3];
		for (int i = 0; i < currentPath.length; i++) {
			currentPath[i] = null;
		}
	}

	private void processLogin(String inputLine) {
		String username = getValue(inputLine);
		
		boolean userExist = false;
		String role = "";
		String aname = "";
		
		// Querying database
		try {
			Connection conn = DBManager.getConnection();
			Statement stmt = null;
			String query = "SELECT username, aname, role " +
					"FROM main.users NATURAL JOIN main.acappella " +
					"WHERE username = '" + username + "'";
			stmt = conn.createStatement();
			ResultSet userTuple = stmt.executeQuery(query);
			if (userTuple.next()) {
				userExist = true;
				role = userTuple.getString("role");
				aname = userTuple.getString("aname");
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Output for Client
		if (userExist) {
			user = username;
			out.print("setLoggedIn true;setUser " + username + ";" +
					"print Logged in as: " + username + ";" + 
					"print Role: " + role.toUpperCase() + ";" +
					"print A Cappella Group: " + aname + ";print ;");
			
			// Get friend requests
			String friendReqCommand = getFriendReq(username);
			out.print(friendReqCommand);
			
			// if admin or SA, get pending registration requests
			if (role.equals("admin") || role.equals("sa")) {
				String regReqCommand = getRegReq(username);
				out.print(regReqCommand + ";");
			}
			
			String hr = getHR(80);
			out.print(hr + "print ;");
			
			// printing out boards
			out.println(SocialNetworkNavigation.printPath(currentPath) +
					 SocialNetworkBoards.viewBoards(user));
		} else {
			out.println("print " + username + " does not exist.");
		}
	}


	private void processRegistration() throws IOException {
		String newUser = "";
		out.println("print Choose a username:;askForInput");

		boolean userExist = true;
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		
		while(userExist) {
			newUser = in.readLine();
			
			String query = "SELECT username FROM main.users WHERE username = '" +
					newUser + "'";
			try {
				stmt = conn.createStatement();
				ResultSet existingUser = stmt.executeQuery(query);
				if (!existingUser.next()) {
					userExist = false;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			// TODO: check that username is legal and isn't keywords like cancel
			
			if (userExist) {
				out.println("print Username already exist. Choose a different one.;" +
						"askForInput");
			}
		}
		
		// username isn't already in the DB
		boolean groupExist = false;
		String command = "";
		HashMap<String, Integer> groupList = new HashMap<String, Integer>();
		
		String query = "SELECT aid, aname FROM main.acappella";
		try {
			stmt = conn.createStatement();
			ResultSet groups = stmt.executeQuery(query);
			while (groups.next()) {
				groupList.put(groups.getString("aname"), groups.getInt("aid"));
				command = command + ";print " + groups.getString("aname");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String group = "";
		int aid = 0;
		while (!groupExist) {
			out.println("print Choose a cappella group for " + newUser + ":" +
					command + ";askForInput");
			group = in.readLine();
			
			if (!groupList.containsKey(group)) {
				out.print("print Please choose a group from the list.;");
			} else {
				groupExist = true;
				aid = groupList.get(group);
			}
		}
		
		// group exists
		out.println("print Registration request for " + newUser + " from " +
				group + " has been sent.;print Once an admin from your group " +
				"approves, you will be added to the system.");
		query = "INSERT INTO main.registrationrequests (username, aid) " +
				"VALUE ('" + newUser + "', " + aid + ")";
		try {
			stmt.executeUpdate(query);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	private void processRegRequests() throws IOException {
		// TODO: Check if user is an admin
		ArrayList<String> pendingUsers = new ArrayList<String>();
		int count = 0;
		try {
			Connection conn = DBManager.getConnection();
			Statement stmt = conn.createStatement();
			String query = "SELECT username " +
				"FROM main.registrationrequests " +
				"WHERE aid = (SELECT aid FROM main.users WHERE username = '" +
				user + "')";
			ResultSet requests = stmt.executeQuery(query);
			while (requests.next()) {
				pendingUsers.add(requests.getString("username"));
				count++;
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (count > 0) {
			String command = "print Pending User Registration Requests (" + 
					count + "):";
			for (int i=0; i < pendingUsers.size(); i++) {
				command = command + ";print " + pendingUsers.get(i);
			}
			command = command + ";print ;print [To approve: approve " +
					"<username1>, <username2>];print [To remove: " +
					"remove <username1>, <username2>];askForInput";
			out.println(command);
			regApproval(in.readLine());
		} else {
			out.println("print No pending registration requests at the moment.");
		}
	
	}

	private void regApproval(String input) {
		if (input.equals("cancel")) {
			out.println();
		}
		if (input.matches("^approve.+")) {
			String value = getValue(input);
			String delim = ",";
			String[] approvedUsers = value.split(delim);
			regApprove(approvedUsers);
		}
		if (input.matches("^remove.+")) {
			String value = getValue(input);
			String delim = ",";
			String[] usersToDelete = value.split(delim);
			
			// Building queries
			String deleteQuery = "DELETE FROM main.registrationrequests WHERE ";
			for (int i=0; i < usersToDelete.length; i++) {
				usersToDelete[i] = usersToDelete[i].trim();
				
				deleteQuery += "username = " + quote(usersToDelete[i]);
				if (i != usersToDelete.length -1) {
					deleteQuery += " OR ";
				}
			}
			try {
				Connection conn = DBManager.getConnection();
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(deleteQuery);
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			// confirmation to client
			String command = "print ";
			for (String user: usersToDelete) {
				command += user + ", ";
			}
			// substring to take off the last comma
			command = command.substring(0, command.length()-2) + 
					" has been deleted from the system.";
			out.println(command);
		}
	}

	private void regApprove(String[] approvedUsers) {
		// Building queries: select info from regrequests and delete from regreqests
		String selectQuery = "SELECT * FROM main.registrationrequests WHERE ";
		String deleteQuery = "DELETE FROM main.registrationrequests WHERE ";
		
		for (int i=0; i < approvedUsers.length; i++) {
			approvedUsers[i] = approvedUsers[i].trim();
			
			selectQuery += "username = " + quote(approvedUsers[i]);
			deleteQuery += "username = " + quote(approvedUsers[i]);
			if (i != approvedUsers.length -1) {
				selectQuery += " OR ";
				deleteQuery += " OR ";
			}
		}
		try {
			// building the insert into users table query
			Connection conn = DBManager.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet selectResult = stmt.executeQuery(selectQuery);
			String insertQuery = "INSERT INTO main.users (username, aid, role) " +
					"VALUES ";
			ArrayList<String> addFriendQueries = new ArrayList<String>();
			while (selectResult.next()) {
				insertQuery += "('" + selectResult.getString("username") + "'," + 
						selectResult.getString("aid") + ",'member'), ";
				addFriendQueries.add(addFriendsFromGroupQuery(selectResult.getString("username"), 
						selectResult.getInt("aid")));
			}
			// taking off the last comma, SQL doesn't like it
			insertQuery = insertQuery.substring(0, insertQuery.length()-2);
			
			if (SocialNetworkServer.DEBUG) {
				System.out.println(selectQuery);
				System.out.println(insertQuery);
				System.out.println(deleteQuery);
				for (String query: addFriendQueries) {
					System.out.println(query);
				}
			}
			
			// execute insertion and deletion queries
			stmt.executeUpdate(insertQuery);
			stmt.executeUpdate(deleteQuery);
			for (String query: addFriendQueries) {
				stmt.executeUpdate(query);
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// confirmation to client
		String command = "print ";
		for (String user: approvedUsers) {
			command += user + ", ";
		}
		// substring to take off the last comma
		command = command.substring(0, command.length()-2) + 
				" has been added to the system.";
		out.println(command);
		
	}

	private String addFriendsFromGroupQuery(String username, int aid) {
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		String acappellaUsers = "SELECT username FROM main.users WHERE aid = " + aid;
		try {
			stmt = conn.createStatement();
			ResultSet acappellaResult = stmt.executeQuery(acappellaUsers);
			String addQuery = "INSERT INTO main.friends (username1, username2) VALUES ";
			String user1, user2;
			while (acappellaResult.next()) {
				if (username.compareTo(acappellaResult.getString("username")) < 0) {
					user1 = username;
					user2 = acappellaResult.getString("username");
					addQuery += "('" + user1 + "','" + user2 + "'), ";
				} else if (username.compareTo(acappellaResult.getString("username")) > 0) {
					user1 = acappellaResult.getString("username");
					user2 = username;
					addQuery += "('" + user1 + "','" + user2 + "'), ";
				}
			}
			// taking off the last comma, SQL doesn't like it
			return addQuery.substring(0, addQuery.length()-2);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String getRegReq(String username) {
		String command = "";
		int requestCount = 0;
		try {
			Connection conn = DBManager.getConnection();
			Statement stmt = conn.createStatement();
			String query = "SELECT COUNT(username) as count " +
					"FROM main.registrationrequests " +
					"WHERE aid = (SELECT aid FROM main.users WHERE username = '" +
					username + "')";
			ResultSet requests = stmt.executeQuery(query);
			while (requests.next()) {
				requestCount = requests.getInt("count");
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (requestCount != 0) {
			command = ";print Pending User Registration Requests (" +
					requestCount + ") [To view: regRequests]";
		}
		return command;
	}

	private void processAddFriend(String input) throws IOException {
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Stores a list of users that is not the current user, who is not a friend of the 
		// current user, and has not requested current user as friend
		// Users[0]: username. Users[1]: group name
		ArrayList<String[]> friendableUsers = new ArrayList<String[]>();
		try {
			// existing friends of user
			ArrayList<String> existingFriends = new ArrayList<String>();
			String query = "SELECT * FROM main.friends WHERE username1 = '" + user + "' OR " +
					"username2 = '" + user + "'";
			ResultSet friendsResult = stmt.executeQuery(query);
			while (friendsResult.next()) {
				if (friendsResult.getString("username1").equals(user)) {
					existingFriends.add(friendsResult.getString("username2"));
				} else {
					existingFriends.add(friendsResult.getString("username1"));
				}
			}
			
			// list of users that is not the current user and who have not requested 
			// current user as friend
			query = "SELECT username, aname FROM main.users NATURAL JOIN main.acappella " +
					"WHERE username != '" + user + "' AND username NOT IN " +
							"(SELECT requester FROM main.friendrequests " +
							"WHERE requestee = '" + user + "')";
			ResultSet usersResult = stmt.executeQuery(query);
			while (usersResult.next()) {
				if (!existingFriends.contains(usersResult.getString("username"))) {
					String[] userInfo = {usersResult.getString("username"), 
							usersResult.getString("aname")};
					friendableUsers.add(userInfo);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// input of format "addFriend" or "addFriend b"
		if (input.equals("addFriend")) {
			// list everybody
			boolean userExist = false;
			String toFriend = "";
			
			while (!userExist) {
				String command = "print Users in the system:;";
				for (String[] userInfo: friendableUsers) {
					command += "print " + userInfo[0] + " (" + userInfo[1] + ");";
				}
				command += "print ;print Type the name of the user you wish to friend:;" +
						"askForInput";
				out.println(command);
				
				toFriend = in.readLine();
				for (String[] userInfo: friendableUsers) {
					if (userInfo[0].equals(toFriend)) {
						userExist = true;
						break;
					}
				}
				if (!userExist) {
					out.print("print Cannot friend " + toFriend + ";");
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
				String command = "print Usernames starting with '" + value + "';";
				for (String[] userInfo: friendableUsers) {
					value = value.toLowerCase();
					if (userInfo[0].toLowerCase().startsWith(value)) {
						command += "print " + userInfo[0] + " (" + userInfo[1] + ");";
					}
				}
				command += "print ;print Type the name of the user you wish to friend:;" +
						"askForInput";
				out.println(command);
				
				toFriend = in.readLine();
				if (toFriend.equals("cancel")) {
					out.println();
					return;
				}
				for (String[] userInfo: friendableUsers) {
					if (userInfo[0].equals(toFriend)) {
						userExist = true;
						break;
					}
				}
				if (!userExist) {
					out.print("print Cannot friend " + toFriend + ";");
				}
			}
			
			// Gets back name of person to add as friend
			addFriend(toFriend);
		}
	}
	

	private void addFriend(String username) throws IOException {
		// username exists in the system.
		out.println("print Are you sure you want to add " + username + 
				" as a friend? (y/n);askForInput");
		String input = in.readLine();
		if (input.equals("y")) {
			try {
				Connection conn = DBManager.getConnection();
				Statement stmt = null;
				String query = "INSERT INTO main.friendrequests (requestee, requester) " +
						"VALUE ('" + username + "','" + user + "')";
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			// print out confirmation
			out.println("print Friend request sent to " + username);
		} else if (input.equals("n")) {
			out.println("print Canceled.");
		} else if (input.equals("cancel")) {
			out.println();
		}
	}

	private void processFriendRequests() throws IOException {
		ArrayList<String> pendingFriends = new ArrayList<String>();
		int count = 0;
		try {
			Connection conn = DBManager.getConnection();
			Statement stmt = conn.createStatement();
			String query = "SELECT requester " +
				"FROM main.friendrequests " +
				"WHERE requestee = '" + user + "'";
			ResultSet requests = stmt.executeQuery(query);
			while (requests.next()) {
				pendingFriends.add(requests.getString("requester"));
				count++;
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (count > 0) {
			String command = "print Pending Friend Requests (" + 
					count + "):";
			for (int i=0; i < pendingFriends.size(); i++) {
				command = command + ";print " + pendingFriends.get(i);
			}
			command = command + ";print ;print [To approve: approve " +
					"<username1>, <username2>];print [To remove: " +
					"remove <username1>, <username2>];askForInput";
			out.println(command);
			friendApproval(in.readLine());
		} else {
			out.println("print No pending friend requests at the moment.");
		}
	}

	private void friendApproval(String input) {
		if (input.equals("cancel")) {
			out.println();
		}
		if (input.matches("^approve.+")) {
			String value = getValue(input);
			String delim = ",";
			String[] approvedFriends = value.split(delim);
			friendApprove(approvedFriends);
		}
		if (input.matches("^remove.+")) {
			String value = getValue(input);
			String delim = ",";
			String[] usersToDelete = value.split(delim);
			
			// Building queries
			String deleteQuery = "DELETE FROM main.friendrequests WHERE ";
			for (int i=0; i < usersToDelete.length; i++) {
				usersToDelete[i] = usersToDelete[i].trim();
				
				deleteQuery += "(requester = " + quote(usersToDelete[i]) + 
						" AND requestee = " + quote(user) + ")";
				if (i != usersToDelete.length -1) {
					deleteQuery += " OR ";
				}
			}
			
			if (SocialNetworkServer.DEBUG) {
				System.out.println(deleteQuery);
			}
			
			try {
				Connection conn = DBManager.getConnection();
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(deleteQuery);
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			// confirmation to client
			String command = "print Friend requests from ";
			for (String user: usersToDelete) {
				command += user + ", ";
			}
			// substring to take off the last comma
			command = command.substring(0, command.length()-2) + 
					" have been deleted.";
			out.println(command);
		}
	}

	private void friendApprove(String[] approvedUsers) {
		// Building queries
		String deleteQuery = "DELETE FROM main.friendrequests WHERE ";
		String insertQuery = "INSERT INTO main.friends (username1, username2) VALUES ";
		
		for (int i=0; i < approvedUsers.length; i++) {
			approvedUsers[i] = approvedUsers[i].trim();
			
			deleteQuery += "(requester = " + quote(approvedUsers[i]) + 
					" AND requestee = " + quote(user) + ")";
			String user1, user2;
			if (user.compareTo(approvedUsers[i]) < 0) {
				user1 = quote(user);
				user2 = quote(approvedUsers[i]);
			} else {
				user1 = quote(approvedUsers[i]);
				user2 = quote(user);
			}
			insertQuery += "(" + user1+ "," + user2 + "), ";
			if (i != approvedUsers.length -1) {
				deleteQuery += " OR ";
			}
		}
		// taking off the last comma, SQL doesn't like it
		insertQuery = insertQuery.substring(0, insertQuery.length()-2);
		try {
			// building the insert into users table query
			Connection conn = DBManager.getConnection();
			Statement stmt = conn.createStatement();

			if (SocialNetworkServer.DEBUG) {
				System.out.println(insertQuery);
				System.out.println(deleteQuery);
			}
			
			// execute insertion and deletion queries
			stmt.executeUpdate(insertQuery);
			stmt.executeUpdate(deleteQuery);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// confirmation to client
		String command = "print ";
		for (String user: approvedUsers) {
			command += user + ", ";
		}
		// substring to take off the last comma
		command = command.substring(0, command.length()-2) + 
				" have been added as your friends.";
		out.println(command);
	}

	private String getFriendReq(String username) {
		String command = "";
		int requestCount = 0;
		try {
			Connection conn = DBManager.getConnection();
			Statement stmt = conn.createStatement();
			String query = "SELECT COUNT(requestee) as count " +
					"FROM main.friendrequests " +
					"WHERE requestee = '" +
					username + "'";
			ResultSet requests = stmt.executeQuery(query);
			while (requests.next()) {
				requestCount = requests.getInt("count");
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (requestCount != 0) {
			command = ";print Pending Friend Requests (" +
					requestCount + ") [To view: friendRequests]";
		}
		return command;
	}

	private void processDeleteUser() throws IOException {
		// TODO: check to see if user is actually a SA
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Stores a list of deletable users
		ArrayList<String> deletableUsers = new ArrayList<String>();
		try {
			String query = "SELECT username FROM main.users " +
					"WHERE username != '" + user + "' AND aid = " +
					"(SELECT aid FROM main.users WHERE username = '" + user + "')";
			
			if (SocialNetworkServer.DEBUG) {
				System.out.println("Delete User deletable user query: " + query);
			}
			
			ResultSet usersResult = stmt.executeQuery(query);
			while (usersResult.next()) {
				deletableUsers.add(usersResult.getString("username"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		boolean userDeletable = false;
		String toDelete = "";
		
		while (!userDeletable) {
			String command = "print Users in your A Cappella group that you can delete:;";
			for (String userInfo: deletableUsers) {
				command += "print " + userInfo + ";";
			}
			command += "print ;print Type the name of the user you wish to delete:;" +
					"askForInput";
			out.println(command);
			
			toDelete = in.readLine();
			if (toDelete.equals("cancel")) {
				return;
			}
			if (deletableUsers.contains(toDelete)) {
				userDeletable = true;
			}
			if (!userDeletable) {
				out.print("print Cannot delete " + toDelete + ";");
			}
		}
		
		// toDelete is deletable
		deleteUser(toDelete);
	}

	private void deleteUser(String username) throws IOException {
		// username is a deletable user
		out.println("print User deletions cannot be undone.;" + 
				"print Are you sure you want to delete this user? (y/n);askForInput");
		String input = in.readLine();
		if (input.equals("y")) {
			try {
				Connection conn = DBManager.getConnection();
				Statement stmt = null;
				String query = "DELETE FROM main.users WHERE username = '"+username+"'";
				stmt = conn.createStatement();
				stmt.executeUpdate(query);
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			// print out confirmation
			out.println("print " + username + " has been deleted from the system.");
		} else if (input.equals("n")) {
			out.println("print Canceled.");
		} else if (input.equals("cancel")) {
			out.println();
		}
	}

	private void processShowFriends() {
		Connection conn = DBManager.getConnection();
		Statement stmt = null;

		// get the list of friends for the current user
		ArrayList<String> existingFriends = new ArrayList<String>();
		try {
			String query = "SELECT * FROM main.friends WHERE username1 = '" + user + "' OR " +
					"username2 = '" + user + "'";
			stmt = conn.createStatement();
			ResultSet friendsResult = stmt.executeQuery(query);
			while (friendsResult.next()) {
				if (friendsResult.getString("username1").equals(user)) {
					existingFriends.add(friendsResult.getString("username2"));
				} else {
					existingFriends.add(friendsResult.getString("username1"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// printing out friend list
		String command = "print Your friends:;";
		if (existingFriends.size() == 0) {
			command = "print You have no friends right now.;" +
					"print To add a friend: type addFriend";
		} else {
			for (String friend: existingFriends) {
				command += "print " + friend + ";";
			}
		}
		out.println(command);
	}

	/**
	 * Creates a board. MUST be in the home directory.
	 */
	private void processCreateBoard(String input) throws IOException {
		/* Ensure the person is in the right place to create a board (on the homepage)*/
		if (currentPath[0] != null) {
			out.println("print Must be at Home to create a board;" +
					"print Current Path: " + SocialNetworkNavigation.printPath(currentPath));
		}
		else {
			String boardname = input.substring(("createBoard ").length());
			out.println(SocialNetworkBoards.createBoard(user, boardname));
		}
	}
	
	/**
	 * Depending on where the user is, fetches the correct view of information.
	 * The user prints their current path and the information associated with it.
	 */
	private void processRefresh() {
		String boardName = currentPath[0];
		if (boardName == null) {
			out.println(SocialNetworkNavigation.printPath(currentPath) + 
					"print ;" + SocialNetworkBoards.viewBoards(user));
		}
		else if (boardName.equals("freeforall")) {
			/*No regions*/
			String postNum = currentPath[1];
			if (postNum == null) { //Merely in the board
				out.println(SocialNetworkNavigation.printPath(currentPath) + 
						"print ;" + SocialNetworkPosts.viewPostList(user, boardName, null));
			}
			else { //Inside the post
				out.println(SocialNetworkNavigation.printPath(currentPath) + 
						"print ;" + SocialNetworkPosts.viewPost(user, boardName, null, 
								Integer.parseInt(postNum)));
			}
		}
		else { //a regular board
			String regionName = currentPath[1];
			if (regionName == null) { //Merely in the board
				out.println(SocialNetworkNavigation.printPath(currentPath) + 
						"print ;" + SocialNetworkRegions.viewRegions(user, boardName));
			}
			else {
				String postNum = currentPath[2];
				if (postNum == null) { //Merely in the region
					out.println(SocialNetworkNavigation.printPath(currentPath) + 
							"print ;" + SocialNetworkPosts.viewPostList(user, boardName, regionName));
				}
				else { //Inside the post
					out.println(SocialNetworkNavigation.printPath(currentPath) + 
							"print ;" + SocialNetworkPosts.viewPost(user, boardName, regionName,
									Integer.parseInt(postNum)));
				}
			}
		}
	}
	
	/**
	 * Depending on where the user is, processes where the user should go.
	 */
	private void processGoto(String inputLine) {
		String destination = inputLine.substring(("goto ").length());
		int validDest = SocialNetworkNavigation.validDestination(currentPath, destination);
		switch (validDest) {
		case -1: /*Go backwards*/
			SocialNetworkNavigation.goBack(currentPath);
			processRefresh();
			break;
		case 2: /*Go immediately home*/
			for (int i = 0; i < currentPath.length; i++) {
				currentPath[i] = null;
			}
			processRefresh();
			break;
		case 1: /*Go forward in the hierarchy to destination*/
			/*Have different cases depending on the current path*/
			if (currentPath[0] == null) {
				out.println(SocialNetworkNavigation.goToBoard(user, currentPath, destination));
			}
			else if (currentPath[0].equals("freeforall")) {
				Integer postNum = null;
				try {
					postNum = Integer.parseInt(destination);
				}
				catch (NumberFormatException e) {
					out.println("print You entered an invalid post number. Your current path (" 
							+ SocialNetworkNavigation.printPath(currentPath) + ")" +
									"implies you are going to a post. Type \"goto ###\", or \"goto ..\" to " +
									"go backwards");
				}
				if (postNum != null) {
					out.println(SocialNetworkNavigation.goToPost(user, currentPath, postNum.intValue()));
				}
			}
			else {
				if (currentPath[1] != null) {
					Integer postNum = null;
					try {
						postNum = Integer.parseInt(destination);
					}
					catch (NumberFormatException e) {
						out.println("print You entered an invalid post number. Your current path (" 
								+ SocialNetworkNavigation.printPath(currentPath) + ")" +
										"implies you are going to a post. Type \"goto ###\", or \"goto ..\" to " +
										"go backwards");
					}
					if (postNum != null) {
						out.println(SocialNetworkNavigation.goToPost(user, currentPath, postNum));
					}
				}
				else {
					out.println(SocialNetworkNavigation.goToRegion(user, currentPath, destination));
				}
			}
			break;
		default:
			out.println("print Invalid destination given your current path: " + 
					SocialNetworkNavigation.printPath(currentPath) + ".; " +
							"print You can go backwards by typing \"..\" ");
			
		}
	}
	
	/**
	 * Creates a region for the user.
	 * The user must be in a board (except freeforall) to execute the command.
	 */
	private void processCreateRegion(String inputLine) {
		String boardName = currentPath[0];
		String regionName = inputLine.substring(("createRegion ").length());
		if (boardName == null) {
			out.println("print Must be in the desired board in order to create the region.");
		}
		else if (boardName.equals("freeforall")) {
			out.println("print Cannot create regions in the freeforall board.");
		}
		else if (currentPath[1] != null) {
			out.println("print Must be exactly in the desired board (i.e., not inside a region in the board) " +
					"in order to create the region");
		}
		else {
			out.println(SocialNetworkRegions.createRegion(user, currentPath[0], regionName));
		}
	}

	private void processPost() throws IOException {
		/*Verify the user is in the right place to create a post*/
		String boardName = currentPath[0];
		boolean canPost = false;
		if (boardName == null) {
			out.println("print Must be within a board's region or in the freeforall board to create a post");
		}
		else if (boardName.equals("freeforall")) {
			String postNum = currentPath[1];
			if (postNum == null) {
				canPost = true;
			}
			else {
				out.println("print Must go back to the board page to create a post (not inside a post)");
			}
		}
		else { //in a regular board
			String regionName = currentPath[1];
			if (regionName == null) {
				out.println("print Must be within a board's region or in the freeforall board to create a post");
			}
			else {
				String postNum = currentPath[2];
				if (postNum == null) { //in a board, region, not in a post
					canPost = true;
				}
				else {
					out.println("print Must go back to the region page to create a post (not inside a post)");
				}
			}
		}
		if (canPost) {
			out.println("print Start typing your content (or enter 'cancel' to cancel). Press enter to submit.");
			String content = in.readLine();
			if (!content.trim().equals("cancel")) {
				out.println(SocialNetworkPosts.createPost(user, content, currentPath[0], currentPath[1]));
			}
			else {
				out.println("print Post Creation cancelled");
			}
		
		}
	}
	
	/**
	 * Similar to processPost basically... except that you must be in
	 * a post
	 * @throws IOException 
	 */
	private void processReply() throws IOException {
		/*Verify the user is in the right place to create a post*/
		String boardName = currentPath[0];
		String postNum = "";
		boolean canReply = false;
		if (boardName == null) {
			out.println("print Must be within a post to create a reply");
		}
		else if (boardName.equals("freeforall")) {
			postNum = currentPath[1];
			if (postNum == null) {
				out.println("print Must be within a post to create a reply");
			}
			else {
				canReply = true;
			}
		}
		else { //in a regular board
			String regionName = currentPath[1];
			if (regionName == null) {
				out.println("print Must be within a post to create a reply");
			}
			else {
				postNum = currentPath[2];
				if (postNum == null) { //in a board, region, not in a post
					out.println("print Must be within a post to create a reply");
				}
				else {
					canReply = true;
				}
			}
		}
		if (canReply) {
			out.println("print Start typing your content (or enter 'cancel' to cancel). Press enter to submit.");
			String content = in.readLine();
			if (!content.trim().equals("cancel")) {
				out.println(SocialNetworkPosts.createReply(user, content, 
						currentPath[0], currentPath[1], Integer.parseInt(postNum)));
			}
			else {
				out.println("print Reply Creation cancelled");
			}
		}
	}
}
