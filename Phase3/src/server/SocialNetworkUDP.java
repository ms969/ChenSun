package server;

import java.net.*;
import java.io.IOException;

//TODO In memory key store for all potential private key uses.
//TODO In memory login system... for multiple users.
//For now, we could just implement the extra phase of 
//exchanging keys on the TCP side...

public class SocialNetworkUDP implements Runnable{
	
	private DatagramSocket listenPort;
	
	public SocialNetworkUDP(DatagramSocket ds) {
		listenPort = ds;
	}
	
	public void main() throws IOException {
		DatagramPacket dp = null;
		byte[] buf = new byte[1024];
		while (true) {
			listenPort.receive(dp);
			//dp.g
		}
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
