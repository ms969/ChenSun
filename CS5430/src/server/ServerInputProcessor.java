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
	
	public static final String[] COMMANDS = {
		"^login.+", // 0
		"register", // 1
	};
	
	public void processCommand(String command, String inputLine) throws IOException {
		if (command.equals(COMMANDS[0])) {
			processLogin(inputLine);
			return;
		}
		if (command.equals(COMMANDS[1])) {
			processRegistration();
			return;
		}
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
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
		String query = "SELECT username, aname, role " +
				"FROM main.users NATURAL JOIN main.acappella " +
				"WHERE username = '" + username + "'";
		try {
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
			out.println("setLoggedIn true;setUser " + username + ";" +
					"print Logged in as: " + username + ";" + 
					"print Role: " + role.toUpperCase() + ";" +
					"print A Cappella Group: " + aname + ";print ");
		} else {
			out.println("print " + username + " does not exist.\\n");
		}
	}

	private void processRegistration() throws IOException {
		String newUser = "";
		out.println("print Choose a username:;askForInput");

		boolean userExist = true;
		
		while(userExist) {
			newUser = in.readLine();
			
			Connection conn = DBManager.getConnection();
			Statement stmt = null;
			String query = "SELECT username FROM main.users WHERE username = '" +
					newUser + "'";
			try {
				stmt = conn.createStatement();
				ResultSet existingUser = stmt.executeQuery(query);
				if (!existingUser.next()) {
					userExist = false;
				}
				stmt.close();
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
		
		Connection conn = DBManager.getConnection();
		Statement stmt = null;
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
		query = "INSERT INTO main.registrationrequests (username, aid, role) " +
				"VALUE ('" + newUser + "', " + aid + ", 'member')";
		try {
			conn = DBManager.getConnection();
			stmt = conn.createStatement();
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

}
