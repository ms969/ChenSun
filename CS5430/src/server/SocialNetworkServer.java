package server;

import java.net.*;
import java.io.*;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.sql.Statement;

public class SocialNetworkServer {
	public static void main(String[] args) throws IOException, SQLException {

		ServerSocket serverSocket = null;
		try {
			// creates the server socket and listens on port 4443
			serverSocket = new ServerSocket(4443);
		} catch (IOException e) {
			System.err.println("Could not listen on port: 4443.");
			System.exit(1);
		}

		Socket clientSocket = null;
		try {
			// connecting to the client
			clientSocket = serverSocket.accept();
		} catch (IOException e) {
			System.err.println("Accept failed.");
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
		
		Connection conn = getConnection();
		Statement stmt = null;
		String query = "INSERT INTO test.new_table VALUES (1)";
		try {
      stmt = conn.createStatement();
      int result = stmt.executeUpdate(query);
      System.out.println("Inserted "  + result + " rows.");
		}
		finally {
		  if (stmt != null) {
		    stmt.close();
		  }
		}
	}
	
	private static Connection getConnection() throws SQLException {
	  Connection conn = null;
	  Properties connectionProps = new Properties();
    connectionProps.put("user", "root");
    connectionProps.put("password", "root");
    conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/",
            connectionProps);
    System.out.println("Connected to database");
    return conn;
	}
}
