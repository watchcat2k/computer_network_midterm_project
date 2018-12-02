package midterm_project.client;

public class ClientMain {
	public static void main(String[] args) {
		MyClient myClient = new MyClient(8888, "localhost", 8080);
		myClient.connect();
		
		//	´«Êä
		
		myClient.disconnect();
	}
}
