package client;

import java.io.*;
import java.net.*;

public class ClientGUI {
  private static int serverPortNum = 5329;
  private static String serverHostName = "localhost";
	public static void main(String[] args) throws IOException {
	  
		Socket kkSocket = null;
		PrintWriter socketOut = null;
		BufferedReader socketIn = null;
		BufferedReader keyboard = null;

		try {
			kkSocket = new Socket(InetAddress.getByName(serverHostName), serverPortNum);
			socketOut = new PrintWriter(kkSocket.getOutputStream(), true);
			socketIn = new BufferedReader(new InputStreamReader(
					kkSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: " + serverHostName);
			System.exit(1);
		} catch (IOException e) {
			System.err
					.println("Couldn't get I/O for the connection to: " + serverHostName);
			System.exit(1);
		}
		System.out.println("Connected to " + serverHostName + ":" + serverPortNum);
		
		keyboard = new BufferedReader(new InputStreamReader(System.in));
		
		/** LOGGING IN **/
		boolean notLoggedIn = true;
		boolean exit = false;
		
		while (notLoggedIn && !exit) {
		  System.out.println("Please input your Username.");
		  String username = keyboard.readLine();
		  System.out.println("Input Password");
		  String password = keyboard.readLine();
		  /**Send to server for verification**/
		}
		
		/*TODO Get Information from Server if successfully logged in*/
		
		boolean inSession = true;
		String input;
		String socketMsg;
		
		while (inSession) {
		  System.out.print(">> ");
		  input = keyboard.readLine();
		  socketOut.println(input);
		  socketMsg = socketIn.readLine();
		  System.out.println(socketMsg);
		}
		
		
		// -----
		
		/*
		String fromServer;
		
		while ((fromServer = in.readLine()) != null) {
			System.out.print("Server: ");
			System.out.println(fromServer);
			if (fromServer.equals("This is server.")) {
				System.out.println("Client: This is client.");
				out.println("This is client.");
			}
			if (fromServer.equals("Connected.")) {
				break;
			}
		}*/

		socketOut.close();
		socketIn.close();
		kkSocket.close();
	}
}
