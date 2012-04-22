package client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import shared.Utils;

import comm.CommManager;

import crypto.Hash;
import crypto.SharedKeyCryptoComm;

public class ClientProcessor {
	private boolean loggedIn = false;
	private boolean exit = false;

	private BufferedReader keyboard;
	private OutputStream serverOut;
	private InputStream serverIn;
	private Cipher c;
	private SecretKey sk;
	
	public BigInteger sendNonce = null;
	public BigInteger recvNonce = null;
	
	public void sendWithNonce(String msg) {
		CommManager.send(msg, serverOut, c, sk, sendNonce);
		this.sendNonce = this.sendNonce.add(BigInteger.ONE);
	}
	
	public String recvWithNonce() {
		String msg = CommManager.receive(serverIn, c, sk, recvNonce);
		this.recvNonce = this.recvNonce.add(BigInteger.ONE);
		return msg;
	}
	
	public void sendWithNonce(byte[] msg) {
		SharedKeyCryptoComm.send(msg, serverOut, c, sk, sendNonce);
		this.sendNonce = this.sendNonce.add(BigInteger.ONE);
	}
	
	public ClientProcessor(OutputStream serverOut, InputStream serverIn, 
			BufferedReader keyboard, Cipher c, SecretKey sk, BigInteger sendNonce, BigInteger recvNonce) {
		this.keyboard = keyboard;
		this.serverOut = serverOut;
		this.serverIn = serverIn;
		this.c = c;
		this.sk = sk;
		this.sendNonce = sendNonce;
		this.recvNonce = recvNonce;
	}

	private void processCommands(String response) {
		String delims = ";";
		String[] commands = response.split(delims);
		for (int i = 0; i < commands.length; i++) {
			if (commands[i].matches("^print.+")) {
				String value = Utils.getValue(commands[i]);
				System.out.println(value);
			}
			if (commands[i].equals("askForInput")) {
				askForInput();
			}
			if (commands[i].equals("isLoggedIn")) {
				sendWithNonce("" + isLoggedIn());
			}
			if (commands[i].equals("isExit")) {
				sendWithNonce("" + isExit());
			}
			if (commands[i].matches("^setLoggedIn.+")) {
				String value = Utils.getValue(commands[i]);
				setLoggedIn(Boolean.parseBoolean(value));
			}
			if (commands[i].matches("^setExit.+")) {
				String value = Utils.getValue(commands[i]);
				setExit(Boolean.parseBoolean(value));
			}
			if (commands[i].equals("help")) {
				processHelp();
			}
			if (commands[i].equals("getPassword")) {
				getPassword();
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
			sendWithNonce(input);
			processCommands(recvWithNonce());
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getPassword() {
		char[] charBuff = new char[24];
		System.out.print(">> ");
		try {
			int i = keyboard.read(charBuff);
			char[] pwd = Arrays.copyOfRange(charBuff, 0, i-2);
			byte[] pwdBytes = Utils.charToByteArray(pwd);
			
			sendWithNonce(pwdBytes);
			Arrays.fill(charBuff, ' ');
			Arrays.fill(pwd, ' ');
			Arrays.fill(pwdBytes, (byte)0x00);
			processCommands(recvWithNonce());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void createPassword() {
		boolean pwdValid = false;
		char[] pwdChar1 = null;
		char[] pwdChar2 = null;
		while (!pwdValid) {
			System.out.println("Create a password for your account (6-20 char long):");
			System.out.println("Password should contain at least 1 lower case letter, " +
					"one upper case letter, and 1 number.");
			System.out.print(">> ");
			char[] charBuff = new char[20];
			try {
				int length = keyboard.read(charBuff);
				if (keyboard.ready()) {
					keyboard.readLine();
					pwdValid = false;
				}
				pwdChar1 = Arrays.copyOfRange(charBuff, 0, length - 2);
				// XXX setting pwdValid for overflow
			} catch (IOException e1) {
				System.err.println("Input error: closing connection...");
				System.exit(1);
			}

			System.out.println("Confirm new password:");
			System.out.print(">> ");
			charBuff = new char[20];
			try {
				int i = keyboard.read(charBuff);
				if (keyboard.ready()) {
					String line = keyboard.readLine();
					System.out.println("extra: " + line);
				}
				pwdChar2 = Arrays.copyOfRange(charBuff, 0, i - 2);
			} catch (IOException e) {
				System.err.println("Input error: closing connection...");
				System.exit(1);
			}
			pwdValid = pwdsMatch(pwdChar1, pwdChar2) && validPassword(pwdChar1);
			if (!pwdValid) {
				System.out.println("Invalid passwords. Please re-enter.");
				System.out.println();
			}
		}
		
		String pwdHash = Hash.createPwdHashStore(pwdChar1);
		sendWithNonce(pwdHash);
		processCommands(recvWithNonce());
	}
	
	private boolean validPassword(char[] pwd) {
		// between 6 and 20 character
		
		return true;
	}
		
	public void processLogin() {
		System.out.println();
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
			System.out.println("Help file not found. Contact system admin.");
		} catch (IOException e) {
			System.out
					.println("Help file may be corrupted. Contact system admin.");
		}
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
