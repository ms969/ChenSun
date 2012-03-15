package crypto;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NonClosingCipherOutputStream extends FilterOutputStream {
	
	public NonClosingCipherOutputStream(OutputStream out) {
		super(out);
	}

	public void close() throws IOException{
		try {
			flush();
		}
		catch (IOException e) {
			
		}
	}
}
