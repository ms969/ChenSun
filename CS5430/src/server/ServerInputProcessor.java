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
	
	public static final String[] COMMANDS = {
		"^login.+",			// 0
		"^register$",		// 1
		"^regRequests$",	// 2
		"^addFriend.*"		// 3
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
		out.println();
	}

	public ServerInputProcessor(PrintWriter out, BufferedReader in) {
		this.out = out;
		this.in = in;
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
					"print A Cappella Group: " + aname + ";print ");
			
			if (role.equals("admin") || role.equals("sa")) {
				// Get pending registration requests
				String regReqCommand = getRegReq(username);
				out.println(regReqCommand);
			} else {
				out.println();
			}
		} else {
			out.println("print " + username + " does not exist.\\n");
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
		String acappellaQuery = "SELECT username FROM ";// TODO
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
			while (selectResult.next()) {
				insertQuery += "('" + selectResult.getString("username") + "'," + 
						selectResult.getString("aid") + ",'member'), ";
			}
			// taking off the last comma, SQL doesn't like it
			insertQuery = insertQuery.substring(0, insertQuery.length()-2);
			
			if (SocialNetworkServer.DEBUG) {
				System.out.println(selectQuery);
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
				" has been added to the system.";
		out.println(command);
		
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
		
		// Getting the complete list of users. Users[0]: username. Users[1]: group name
		ArrayList<String[]> displayedUsers = new ArrayList<String[]>();
		try {
			String query = "SELECT username, aname " +
				"FROM main.users NATURAL JOIN main.acappella";
			ResultSet usersResult = stmt.executeQuery(query);
			while (usersResult.next()) {
				if (!usersResult.getString("username").equals(user)) {
					String[] userInfo = {usersResult.getString("username"), 
							usersResult.getString("aname")};
					displayedUsers.add(userInfo);
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
				for (String[] userInfo: displayedUsers) {
					command += "print " + userInfo[0] + " (" + userInfo[1] + ");";
				}
				command += "print ;print Type the name of the user you wish to friend:;" +
						"askForInput";
				out.println(command);
				
				toFriend = in.readLine();
				for (String[] userInfo: displayedUsers) {
					if (userInfo[0].equals(toFriend)) {
						userExist = true;
						break;
					}
				}
				if (!userExist) {
					out.print("print " + toFriend + " is not a user in the system.;");
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
				for (String[] userInfo: displayedUsers) {
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
				for (String[] userInfo: displayedUsers) {
					if (userInfo[0].equals(toFriend)) {
						userExist = true;
						break;
					}
				}
				if (!userExist) {
					out.print("print " + toFriend + " is not a user in the system.;");
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

}
