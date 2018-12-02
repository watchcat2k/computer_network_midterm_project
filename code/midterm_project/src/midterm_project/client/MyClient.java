package midterm_project.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import midterm_project.datagram.Format;

import midterm_project.datagram.Datagram;

public class MyClient {
	private int sourcePort;
	private String destinationIp;
	private int destinationPort;
	private DatagramSocket client;
	
	public MyClient(int sourcePort, String destinationIp, int destinationPort) {
		this.sourcePort = sourcePort;
		this.destinationIp = destinationIp;
		this.destinationPort = destinationPort;
	}
	
	//	三次握手
	public void connect() {
		try {
			client = new DatagramSocket(sourcePort);
			
			//	发送第一次握手
			Datagram firstConnect = new Datagram();
			firstConnect.setACK(0);
			firstConnect.setSYN(1);
			byte[] requstData = Format.datagramToByteArray(firstConnect);
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
	
	//	四次挥手
	public void disconnect() {
		
		
		
		try {
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

