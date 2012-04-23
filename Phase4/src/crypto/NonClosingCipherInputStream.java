package crypto;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NonClosingCipherInputStream extends FilterInputStream{

	protected NonClosingCipherInputStream(InputStream in) {
		super(in);
	}
	
	public void close() throws IOException {
		;
	}

}
