package server;

import java.net.*;
import java.io.*;

public class SocialNetworkServer {
	public static void main(String[] args) throws IOException {

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
	}
}
