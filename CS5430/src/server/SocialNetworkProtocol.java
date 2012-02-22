package server;

import java.net.*;

public class SocialNetworkProtocol implements Runnable{
  
  private Socket clientSocket;
  
  public SocialNetworkProtocol(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  public void run() {
    main(null);
  }

}
