package crypto;

import java.math.BigInteger;
import javax.crypto.SecretKey;

/**A wrapper class for the return argument for
 * the authentication exchange done between Client and Server.
 */
public class KeyNonceBundle {
	
	private SecretKey sk;
	public BigInteger sendNonce = null;
	public BigInteger recvNonce = null;
	
	public KeyNonceBundle(SecretKey sk, BigInteger sendNonce, BigInteger recvNonce) {
		this.sk = sk;
		this.sendNonce = sendNonce;
		this.recvNonce = recvNonce;
	}
	
	public SecretKey getSk() {
		return sk;
	}
	public BigInteger getSendNonce() {
		return sendNonce;
	}
	public BigInteger getRecvNonce() {
		return recvNonce;
	}
	
}
