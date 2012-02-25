package server;

import java.net.*;
import java.io.*;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.sql.Statement;

/* Setup code and Connection code courtesy of 
 * tutorials in docs.oracle.com
 */

//TODO should we merely just exit if the server finds an error?

public class SocialNetworkServer {
	private static int listenPortNum = 5329;
	private static int dbPortNum = 3306;
	private static String hostName = "localhost";
	private static String user = "root";
	private static String password = "root";

	public static void main(String[] args) throws IOException, SQLException {

		ServerSocket serverSocket = initializeSocket();

//		while (true) {
//			acceptClient(serverSocket);
//		}
		
		Socket clientSocket = null;
		try {
			// connecting to the client
			clientSocket = serverSocket.accept();
		} catch (IOException e) {
			System.err.println("Accept failed.");
			// TODO exit here?
			System.exit(1);
		}
		
		
		// Getting client socket's output stream
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		// Getting client socket's input stream
		BufferedReader in = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));
		System.out.println("Server initiated.");

		System.out.println("Server: This is server.");
		out.println("This is server.");

		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			System.out.print("Client: ");
			System.out.println(inputLine);
			if (inputLine.equals("This is client.")) {
				System.out.println("Server: Connected.");
				out.println("Connected.");
				break;
			}
		}

		// closing the connection
		out.close();
		in.close();
		clientSocket.close();
		serverSocket.close();

		Connection conn = getConnection(user, password, hostName, dbPortNum);
		Statement stmt = null;
		String query = "INSERT INTO test.new_table VALUES (1)";
		try {
			stmt = conn.createStatement();
			int result = stmt.executeUpdate(query);
			System.out.println("Inserted " + result + " rows.");
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		
		 
	}

	private static ServerSocket initializeSocket() {
		ServerSocket serverSocket = null;
		try {
			// creates the server socket and listens on port 4443
			serverSocket = new ServerSocket(listenPortNum);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + listenPortNum
					+ ".");
			// TODO exit here?
			System.exit(1);
		}
		return serverSocket;
	}

	private static void acceptClient(ServerSocket serverSocket) {
		Socket clientSocket = null;
		try {
			// connecting to the client
			clientSocket = serverSocket.accept();
		} catch (IOException e) {
			System.err.println("Accept failed.");
			// TODO exit here?
			System.exit(1);
		}

		SocialNetworkProtocol snp = new SocialNetworkProtocol(clientSocket);
		Thread client = new Thread(snp);
		client.run();
	}

	private static Connection getConnection(String userName, String pwd,
			String host, int port) throws SQLException {
		Connection conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", userName);
		connectionProps.put("password", pwd);
		conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port
				+ "/", connectionProps);
		System.out.println("Connected to database");
		return conn;
	}
}
