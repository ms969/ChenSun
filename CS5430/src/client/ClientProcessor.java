package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import shared.InputProcessor;

public class ClientProcessor extends InputProcessor {
	private boolean loggedIn = false;
	private boolean exit = false;
	//private String user = null;
	
	private BufferedReader keyboard;
	private PrintWriter serverOut;
	private BufferedReader serverIn;
	
	public ClientProcessor(PrintWriter serverOut, BufferedReader serverIn, BufferedReader keyboard) {
		this.serverOut = serverOut;
		this.serverIn = serverIn;
		this.keyboard = keyboard;
	}
	
	private void processCommands(String response) {
		String delims = ";";
		String[] commands = response.split(delims);
		for (int i=0; i < commands.length; i++) {
			if (commands[i].matches("^print.+")) {
				String value = getValue(commands[i]);
				System.out.println(value);
			}
			if (commands[i].equals("askForInput")) {
				askForInput();
			}
			if (commands[i].equals("isLoggedIn")) {
				serverOut.println(isLoggedIn());
			}
			if (commands[i].equals("isExit")) {
				serverOut.println(isExit());
			}
			/*if (commands[i].equals("getUser")) {
				serverOut.println(getUser());
			}*/
			if (commands[i].matches("^setLoggedIn.+")) {
				String value = getValue(commands[i]);
				setLoggedIn(Boolean.parseBoolean(value));
			}
			if (commands[i].matches("^setExit.+")) {
				String value = getValue(commands[i]);
				setExit(Boolean.parseBoolean(value));
			}
			/*if (commands[i].matches("^setUser.+")) {
				String value = getValue(commands[i]);
				setUser(value);
			}*/
			// *** Add commands here ***
		}
	}
	
	public void askForInput() {
		System.out.print(">> ");
		try {
			String input = keyboard.readLine();
			serverOut.println(input);
			processCommands(serverIn.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void processLogin() {
		System.out.println("To log in, type 'login <username>'");
		System.out.println("To register, type 'register'");
		askForInput();
	}
	
	public boolean isLoggedIn() {
		return loggedIn;
	}

	public boolean isExit() {
		return exit;
	}
	
//	public String getUser() {
//		return user;
//	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public void setExit(boolean exit) {
		this.exit = exit;
	}
	
//	public void setUser(String user) {
//		this.user = user;
//	}
}
