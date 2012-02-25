package server;

import java.net.*;
import java.io.*;

public class SocialNetworkProtocol implements Runnable {

	private Socket clientSocket;

	public SocialNetworkProtocol(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void main(String[] args) throws IOException {
		// Getting client socket's input stream
		BufferedReader in = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));

//		String inputLine;
//		while ((inputLine = in.readLine()) != null) {
//			
//		}

		// closing the connection
		in.close();
		clientSocket.close();
	}

	public void run() {
		try {
			main(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
