package server;

import java.net.*;
import java.io.*;

public class SocialNetworkProtocol implements Runnable {
	
	private ServerInputProcessor iprocessor;
	
	private Socket clientSocket;

	public SocialNetworkProtocol(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void main() throws IOException {
		// Getting client socket's input and output streams
		BufferedReader in = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		
		iprocessor = new ServerInputProcessor(out, in);
		
		// request input
		
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			iprocessor.processCommand(inputLine);
		}

		// closing the connection
		in.close();
		clientSocket.close();
	}

	public void run() {
		try {
			main();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
