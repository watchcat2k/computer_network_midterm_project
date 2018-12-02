package midterm_project.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class MyClient {
	static private int sourcePort;
	static private String destinationIp;
	static private int destinationPort;
	private DatagramSocket client;
	
	public MyClient(int sourcePort, String destinationIp, int destinationPort) {
		this.sourcePort = sourcePort;
		this.destinationIp = destinationIp;
		this.destinationPort = destinationPort;
	}
	
	public void connect() {
		try {
			client = new DatagramSocket(sourcePort);
			
			//	发送第一次握手
			String mString = "HelloWorld";
			byte[] requstData = mString.getBytes();
			
			DatagramPacket requstPacket = new DatagramPacket(requstData, requstData.length, new InetSocketAddress(destinationIp, destinationPort));
			client.send(requstPacket);
			
			//	接收第二次握手
			byte[] resposeData = new byte[1024];
			DatagramPacket resposePacket = new DatagramPacket(resposeData, resposeData.length);
			client.receive(resposePacket);
			String resposeString = new String(resposeData, 0, resposePacket.getLength());
			System.out.println(resposeString);
			
			//	发送第三次握手
			
			
			client.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		
		
		
		try {
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

