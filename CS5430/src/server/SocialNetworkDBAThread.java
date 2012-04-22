package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Arrays;
//import java.util.Arrays;

import crypto.Hash;
import database.DBManager;
import database.DatabaseDBA;

/**
 * This class runs a thread that the DBA manager can talk to.
 * Allows for these updates to the database:
 * 		Creating an A Cappella Group with a Super Admin
 *
 */
public class SocialNetworkDBAThread implements Runnable{
	
	private BufferedReader keyboard;
	
	public SocialNetworkDBAThread() {
		keyboard = new BufferedReader(new InputStreamReader(System.in));
	}
	
	public void run() {
		String cmd;

		System.out.println("DBA Thread successfully started.");
		System.out.println("To create an A Cappella group, type 'create group'.");
		System.out.println("If you don't want to do anything, just leave this alone!");
		System.out.println("If at any point you desire to cancel inside a command, just type 'cancel'");
		while (true) {
			System.out.print(">> ");
			try {
				cmd = keyboard.readLine().trim();
				if (cmd.equals("create group")) {
					createGroup();
				}
				else {
					System.out.println("Command not recognized. Type 'create group' to create a new group.");
				}
			} catch (IOException e) {
				System.out.println("Error getting your command! please try again");
				continue;
			}
		}
	}

	/**Runs protocol to create a new a cappella group **/
	public void createGroup() {
		String aname, username, pwhash;
		char[] charbuf = new char[24];
		char[] charbuf2 = new char[24];
		System.out.println("Please type the name of the new A Cappella group (or 'cancel' to cancel)");
		try {
			aname = keyboard.readLine().trim();
			if (aname.equals("cancel")) {
				return ;
			}
			System.out.println("Please type the username of the new A Cappella group's Super Admin (LAST CHANCE TO CANCEL!!!)");
			System.out.print(">> ");
			try {
				username = keyboard.readLine().trim();
				if (username.equals("cancel")) {
					return ;
				}
				System.out.println("Please input the password for this System Admin (no cancelling)");
				System.out.print(">> ");
				//TODO password creation restrictions... we should make that function
				keyboard.read(charbuf);
				
				System.out.println("Confirm the password");
				System.out.print(">> ");
				keyboard.read(charbuf2);
				
				if (Arrays.equals(charbuf, charbuf2)) {
					//generates a pwhash for storage into the database.
					pwhash = Hash.createPwdHashStore(charbuf);
					//TODO clear both buffers
					//Arrays.fill(charbuf, '');
					
					Connection conn = DBManager.getConnection();
					DatabaseDBA.createAcappellaGroup(conn, aname, username, pwhash);
					DBManager.closeConnection(conn);
				}
				else {
					System.out.println("Passwords don't match. Exiting command...");
				}
			}
			catch (IOException e) {
				System.out.println("Error getting your input! Exiting command...");
			}
		}
		catch (IOException e) {
			System.out.println("Error getting your input! Exiting command...");
		}
	}
}
