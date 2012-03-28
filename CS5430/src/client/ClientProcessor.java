package client;

import java.io.*;

import javax.crypto.*;

import comm.CommManager;

import crypto.Hash;
import crypto.SharedKeyCryptoComm;

import java.security.*;
import java.util.Arrays;

import shared.InputProcessor;

public class ClientProcessor extends InputProcessor {
	private boolean loggedIn = false;
	private boolean exit = false;
	private String salt;

	private BufferedReader keyboard;
	private OutputStream serverOut;
	private InputStream serverIn;
	private Cipher c;
	private SecretKey sk;
	
	
	public ClientProcessor(OutputStream serverOut, InputStream serverIn, 
			BufferedReader keyboard, Cipher c, SecretKey sk) {
		this.keyboard = keyboard;
		this.serverOut = serverOut;
		this.serverIn = serverIn;
		this.c = c;
		this.sk = sk;
	}

	private void processCommands(String response) {
		String delims = ";";
		String[] commands = response.split(delims);
		for (int i = 0; i < commands.length; i++) {
			if (commands[i].matches("^print.+")) {
				String value = getValue(commands[i]);
				System.out.println(value);
			}
			if (commands[i].equals("askForInput")) {
				askForInput();
			}
			if (commands[i].equals("isLoggedIn")) {
				CommManager.send("" + isLoggedIn(), serverOut, c, sk);
			}
			if (commands[i].equals("isExit")) {
				CommManager.send("" + isExit(), serverOut, c, sk);
			}
			if (commands[i].matches("^setLoggedIn.+")) {
				String value = getValue(commands[i]);
				setLoggedIn(Boolean.parseBoolean(value));
			}
			if (commands[i].matches("^setExit.+")) {
				String value = getValue(commands[i]);
				setExit(Boolean.parseBoolean(value));
			}
			if (commands[i].equals("help")) {
				processHelp();
			}
			if (commands[i].equals("getPassword")) {
				getPassword();
			}
			if (commands[i].matches("^setSalt.+")) {
				String value = getValue(commands[i]);
				setSalt(value);
			}
			if (commands[i].equals("createPassword")) {
				createPassword();
			}
			// *** Add commands here ***
		}
	}

	public void askForInput() {
		System.out.print(">> ");
		try {
			String input = keyboard.readLine();
			CommManager.send(input, serverOut, c, sk);
			processCommands(CommManager.receive(serverIn, c, sk));
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getPassword() {
		char[] charBuff = new char[24];
		System.out.print(">> ");
		try {
			String hashedPwd = "";
			int i = keyboard.read(charBuff);
			char[] pwd = Arrays.copyOfRange(charBuff, 0, i-2);
			if (salt != null) {
				hashedPwd = Hash.hashExistingPwd(salt, pwd);
				salt = null;
			}
			
			CommManager.send(hashedPwd, serverOut, c, sk);
			Arrays.fill(charBuff, ' ');
			Arrays.fill(pwd, ' ');
			processCommands(CommManager.receive(serverIn, c, sk));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void createPassword() {
		boolean pwdValid = false;
		char[] pwdChar1 = null;
		char[] pwdChar2 = null;
		while (!pwdValid) {
			System.out.println("Create a password for your account:");
			System.out.print(">> ");
			char[] charBuff = new char[24];
			try {
				int i = keyboard.read(charBuff);
				pwdChar1 = Arrays.copyOfRange(charBuff, 0, i - 2);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			System.out.println("Confirm new password:");
			System.out.print(">> ");
			charBuff = new char[24];
			try {
				int i = keyboard.read(charBuff);
				pwdChar2 = Arrays.copyOfRange(charBuff, 0, i - 2);
			} catch (IOException e) {
				e.printStackTrace();
			}
			pwdValid = pwdsMatch(pwdChar1, pwdChar2);
			if (!pwdValid) {
				System.out.println("Invalid passwords. Please re-enter.");
			}
		}
		
		String pwdHash = Hash.createPwdHashStore(pwdChar1);
		CommManager.send(pwdHash, serverOut, c, sk);
		processCommands(CommManager.receive(serverIn, c, sk));
	}
		
	public void processLogin() {
		System.out.println("To log in, type 'login <username>'");
		System.out.println("To register, type 'register'");
		askForInput();
	}

	private void processHelp() {
		try {
			BufferedReader br = new BufferedReader(new FileReader("HELP.txt"));
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (FileNotFoundException e) {
			// TODO: do this?
			System.out.println("Help file not found. Contact system admin.");
		} catch (IOException e) {
			System.out
					.println("Help file may be corrupted. Contact system admin.");
		}
	}
	
	private void setSalt(String salt) {
		this.salt = salt;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public boolean isExit() {
		return exit;
	}

	private void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	private void setExit(boolean exit) {
		this.exit = exit;
	}

	private boolean pwdsMatch(char[] pwdChar1, char[] pwdChar2) {
		if (pwdChar1.length != pwdChar2.length) {
			return false;
		}
		for (int i = 0; i < pwdChar1.length; i++) {
			if (pwdChar1[i] != pwdChar2[i])
				return false;
		}
		return true;
	}

}
