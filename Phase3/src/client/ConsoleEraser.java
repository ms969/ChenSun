package client;

class ConsoleEraser extends Thread {
	private boolean running = true;

	public void run() {
		while (running) {
			System.out.print("\b ");
		}
	}

	public synchronized void halt() {
		running = false;
	}
}