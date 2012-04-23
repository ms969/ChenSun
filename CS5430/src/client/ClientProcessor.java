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

import shared.ConnectionException;
import shared.ProjectConfig;
import shared.Utils;

import comm.CommManager;

import crypto.CryptoUtil;
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
	
	private static final boolean DEBUG = ProjectConfig.DEBUG;
	
	public void sendWithNonce(String msg) throws ConnectionException {
		CommManager.send(msg, serverOut, c, sk, sendNonce);
		this.sendNonce = this.sendNonce.add(BigInteger.ONE);
	}
	
	public String recvWithNonce() throws ConnectionException {
		ClientResponseThread crt = new ClientResponseThread(keyboard);
		crt.start();
		String msg = CommManager.receive(serverIn, c, sk, recvNonce);
		crt.setGotResponse(true);
		this.recvNonce = this.recvNonce.add(BigInteger.ONE);
		return msg;
	}
	
	public void sendWithNonce(byte[] msg) throws ConnectionException {
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

	private void processCommands(String response) throws ConnectionException {
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
			if (commands[i].equals("getSecAnswer")) {
				getSecAnswer();
			}
			if (commands[i].equals("createPassword")) {
				createPassword();
			}
			if (commands[i].equals("quit")) {
				if (!DEBUG) {
					System.out.println("Logged out. Exiting system.");
					System.exit(1);
				}
			}
			// *** Add commands here ***
		}
	}
	
	private void getSecAnswer() throws ConnectionException {
		char[] charBuff = new char[22];
		System.out.print(">> ");
		try {
			boolean overflow = false;
			int i = keyboard.read(charBuff);
			if (keyboard.ready()) {
				keyboard.readLine();
				overflow = true;
			}
			char[] pwd = Arrays.copyOfRange(charBuff, 0, i-2);
			if (overflow) {
				// if longer than 20 char, blank the array to avoid partially correct pwd
				Arrays.fill(charBuff, ' ');
			}
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

	public void askForInput() throws ConnectionException {
		System.out.print(">> ");
		try {
			String input = keyboard.readLine();
			sendWithNonce(input);
			processCommands(recvWithNonce());
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getPassword() throws ConnectionException {
		char[] charBuff = new char[22];
		System.out.print(">> ");
		try {
			boolean overflow = false;
			int i = keyboard.read(charBuff);
			if (keyboard.ready()) {
				keyboard.readLine();
				overflow = true;
			}
			char[] pwd = Arrays.copyOfRange(charBuff, 0, i-2);
			if (overflow) {
				// if longer than 20 char, blank the array to avoid partially correct pwd
				Arrays.fill(charBuff, ' ');
			}
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
	
	private void createPassword() throws ConnectionException {
		boolean pwdValid = false;
		char[] pwdChar1 = null;
		char[] pwdChar2 = null;
		while (!pwdValid) {
			boolean pwdOverflow = false;
			System.out.println("Create password for your account (6-20 char long):");
			System.out.println("Password should contain at least 1 lower case letter, " +
					"1 upper case letter, and 1 number.");
			System.out.print(">> ");
			char[] charBuff = new char[22]; // 20 for data, last 2 for \r\n
			try {
				int length = keyboard.read(charBuff);
				if (keyboard.ready()) {
					keyboard.readLine();
					pwdOverflow = true;
				}
				// length-2 because \r\n
				pwdChar1 = Arrays.copyOfRange(charBuff, 0, length - 2);
			} catch (IOException e1) {
				System.err.println("Input error: closing connection...");
				System.exit(1);
			}

			System.out.println("Confirm new password:");
			System.out.print(">> ");
			charBuff = new char[22];
			try {
				int i = keyboard.read(charBuff);
				if (keyboard.ready()) {
					keyboard.readLine();
					pwdOverflow = true;
				}
				pwdChar2 = Arrays.copyOfRange(charBuff, 0, i - 2);
				Arrays.fill(charBuff, ' ');
			} catch (IOException e) {
				System.err.println("Input error: closing connection...");
				System.exit(1);
			}
			pwdValid = Arrays.equals(pwdChar1, pwdChar2) && 
					CryptoUtil.validPassword(pwdChar1) && !pwdOverflow;
			if (!pwdValid) {
				System.out.println("Invalid passwords. Please re-enter.");
				System.out.println();
			}
		}
		
		sendWithNonce(Utils.charToByteArray(pwdChar1));
		Arrays.fill(pwdChar1, ' ');
		Arrays.fill(pwdChar2, ' ');
		processCommands(recvWithNonce());
	}
		
	public void processLogin() throws ConnectionException {
		System.out.println();
		System.out.println("To log in, type 'login <username>'");
		System.out.println("To register, type 'register'");
		System.out.println("If you forgot your password, type 'pwdRecovery <username>' " +
				"to reset password.");
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

}
