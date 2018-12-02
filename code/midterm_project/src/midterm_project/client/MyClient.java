package midterm_project.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import midterm_project.datagram.Format;

import midterm_project.datagram.Datagram;

public class MyClient {
	private int sourcePort;
	private String destinationIp;
	private int destinationPort;
	private DatagramSocket client;
	private int x = 0;
	private int y;
	
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
			firstConnect.setSYN(1);
			firstConnect.setSeq(x);
			byte[] requstData = Format.datagramToByteArray(firstConnect);
			DatagramPacket requstPacket = new DatagramPacket(requstData, requstData.length, new InetSocketAddress(destinationIp, destinationPort));
			client.send(requstPacket);
			x++;
			
			//	接收第二次握手
			byte[] resposeData = new byte[1024];
			DatagramPacket resposePacket = new DatagramPacket(resposeData, resposeData.length);
			client.receive(resposePacket);
			Datagram secondConnect = Format.byteArrayToDatagram(resposeData);
			
			if (secondConnect.getSYN() == 1 && secondConnect.getACK() == 1) {
				y = secondConnect.getSeq();
			}
			
			//	发送第三次握手
			Datagram thirdConnect = new Datagram();
			thirdConnect.setACK(1);
			thirdConnect.setSeq(x);
			thirdConnect.setAck(y + 1);
			requstData = Format.datagramToByteArray(thirdConnect);
			requstPacket = new DatagramPacket(requstData, requstData.length, new InetSocketAddress(destinationIp, destinationPort));
			client.send(requstPacket);
			x++;
		} catch (Exception e) {
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

