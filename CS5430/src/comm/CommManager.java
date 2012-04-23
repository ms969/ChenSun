package comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import crypto.SharedKeyCryptoComm;

import shared.ConnectionException;
import shared.ProjectConfig;
import shared.ProjectConfig.SendingType;

public class CommManager {
	
	
	public static boolean send(String msg, OutputStream os, Cipher c, SecretKey sk, 
			BigInteger sendNonce) throws ConnectionException {
		if (ProjectConfig.DEBUG) {
			//System.out.println("Sent msg: " + msg);
		}
		if (ProjectConfig.SENDING_METHOD == SendingType.SHARED_KEY) {
			return SharedKeyCryptoComm.send(msg, os, c, sk, sendNonce);
		}
		if (ProjectConfig.SENDING_METHOD == SendingType.NO_ENCRYPTION) {
			PrintWriter pw = new PrintWriter(os);
			pw.println(msg);
			pw.flush();
			//pw.close();
			return true;
		}
		return false;
	}
	
	public static String receive(InputStream is, Cipher c, SecretKey sk, 
			BigInteger recvNonce) throws ConnectionException {
		if (ProjectConfig.SENDING_METHOD == SendingType.SHARED_KEY) {
			String msg = SharedKeyCryptoComm.receiveString(is, c, sk, recvNonce);
			if (ProjectConfig.DEBUG) {
				System.out.println("Recv msg: " + msg);
			}
			return msg;
		}
		if (ProjectConfig.SENDING_METHOD == SendingType.NO_ENCRYPTION) {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			try {
				String msg = br.readLine();
				if (ProjectConfig.DEBUG) {
					//System.out.println("Recv msg: " + msg);
				}
				//br.close();
				if (msg == null) {
					throw new ConnectionException();
				}
				return msg;
			} catch (IOException e) {
				return null;
			}
		}
		return null;
	}
}
