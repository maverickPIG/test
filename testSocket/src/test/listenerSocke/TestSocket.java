package test.listenerSocke;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class TestSocket {
	public static void main(String[] args) throws UnknownHostException, IOException {
		Socket s=new Socket("127.0.0.1", 8080);
		OutputStream stream = s.getOutputStream();
		stream.write("年后，你好，hello".getBytes());
		stream.close();
		s.close();
	}
}
