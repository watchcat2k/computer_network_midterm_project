package midterm_project.server;

public class ServerMain {
	public static void main(String[] args) {
		int port = 8080;
		MyServer server = new MyServer(port);
		while (true) {
			server.receiveFirstPacketAndNewThread();
		}
	
	}
	
}
