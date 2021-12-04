package cashier_register;

public class ThreadConnector {
	String inBuffer;
	String outBuffer;
	
	public ThreadConnector () {
	}
	
	public void sendMessage(String msg) {
		outBuffer = msg;
	}
	
	public void getResponse(String msg) {
		inBuffer = msg;
	}
}
