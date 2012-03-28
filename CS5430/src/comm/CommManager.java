package comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import crypto.SharedKeyCryptoComm;

import shared.ProjectConfig;
import shared.ProjectConfig.SendingType;

public class CommManager {
	
	
	public static boolean send(String msg, OutputStream os, Cipher c, SecretKey sk) {
		if (ProjectConfig.SENDING_METHOD == SendingType.SHARED_KEY) {
			//System.out.println("Sending method = sharedkey");
			return SharedKeyCryptoComm.send(msg, os, c, sk);
		}
		if (ProjectConfig.SENDING_METHOD == SendingType.NO_ENCRYPTION) {
			//System.out.println("Sending method = no encryption");
			PrintWriter pw = new PrintWriter(os);
			pw.println(msg);
			pw.flush();
			//pw.close();
			return true;
		}
		//System.out.println("Sending method other");
		return false;
	}
	
	public static String receive(InputStream is, Cipher c, SecretKey sk) {
		if (ProjectConfig.SENDING_METHOD == SendingType.SHARED_KEY) {
			return SharedKeyCryptoComm.receive(is, c, sk);
		}
		if (ProjectConfig.SENDING_METHOD == SendingType.NO_ENCRYPTION) {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			try {
				String msg = br.readLine();
				//br.close();
				return msg;
			} catch (IOException e) {
				return null;
			}
		}
		return null;
	}
}
