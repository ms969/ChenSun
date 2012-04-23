package client;

import java.io.BufferedReader;

public class ClientResponseThread extends Thread{

	private BufferedReader keyboard;
	private boolean gotResponse;
	protected static Object lock_gotResponse = new Object();
	
	public ClientResponseThread(BufferedReader kb) {
		keyboard = kb;
		gotResponse = false;
	}
	
	public void setGotResponse(boolean newResponse) {
		synchronized(lock_gotResponse) {
			gotResponse = newResponse;
		}
	}
	
	public void run() {
		boolean canDie = false;
		try {
			while (!keyboard.ready()) { //nothing to read
				synchronized (lock_gotResponse) {
					if (gotResponse) { //we got a response from the main client.
						canDie = true;
					}
				}
				Thread.sleep(500); //busy wait, but cannot be helped.
			}
			if (!canDie) {
				String response = keyboard.readLine();
				if (response.trim().toLowerCase().equals("cancel")) {
					System.out.println("Closing the connection at the request of the user.");
					System.exit(1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace(); //this should not happen
			//the thread will not be interrupted hopefully.
		} 
	}
	
	

}
