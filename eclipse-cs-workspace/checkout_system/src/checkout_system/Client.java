package checkout_system;

public class Client {
	public String name;
	public ServerThread thread;
	
	public Client(ServerThread thread) {
		this.thread = thread;
		this.name = null;
	}
	
	public Client(String name, ServerThread thread) {
		this.name = name;
		this.thread = thread;
	}
	
	public Client() {
		
	}
}
