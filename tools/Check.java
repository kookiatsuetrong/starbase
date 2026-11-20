import java.net.Socket;

class Check {
	public static void main() {
		try {
			Socket s = new Socket("127.0.0.1", 9315);
		} catch (Exception e) { }
	}
}
