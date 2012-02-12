package client;

import java.io.*;
import java.net.*;

public class ClientGUI {
	public static void main(String[] args) throws IOException {

		Socket kkSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;

		try {
			// connecting to socket 4443
			kkSocket = new Socket(InetAddress.getLocalHost(), 4443);
			out = new PrintWriter(kkSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					kkSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: Localhost.");
			System.exit(1);
		} catch (IOException e) {
			System.err
					.println("Couldn't get I/O for the connection to: Local Host.");
			System.exit(1);
		}
		System.out.println("Client initiated.");
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
		}

		out.close();
		in.close();
		kkSocket.close();
	}
}
