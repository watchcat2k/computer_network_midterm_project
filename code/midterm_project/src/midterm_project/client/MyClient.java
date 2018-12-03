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
	private int y = 0;
	private int fileTranPort;
	
	public MyClient(int sourcePort, String destinationIp, int destinationPort) {
		this.sourcePort = sourcePort;
		this.destinationIp = destinationIp;
		this.destinationPort = destinationPort;
		
		try {
			client = new DatagramSocket(sourcePort);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void Upload(String fileName) {
		//	发送表示上传的数据包并接收响应
		Datagram upload = new Datagram();
		upload.setType(0);
		send(upload);
		Datagram response = receive();
		if (response.getACK() == 1) {
			fileTranPort = response.getPort();
			System.out.println("获取的端口号为" + fileTranPort);
		}
		
		sendFile();
	}
	
	public void Download(String fileName) {
		//	发送表示下载的数据包并接收响应
		Datagram download = new Datagram();
		download.setType(1);
		send(download);
		Datagram response = receive();
		if (response.getACK() == 1) {
			fileTranPort = response.getPort();
		}
		
		receiveFile();
	}
	
	
	//	发送数据包
	private void send(Datagram upload) {
		try {
			byte[] requstData = Format.datagramToByteArray(upload);
			DatagramPacket requstPacket = new DatagramPacket(requstData, requstData.length, new InetSocketAddress(destinationIp, destinationPort));
			client.send(requstPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//	接收数据包
	private Datagram receive() {
		try {
			byte[] resposeData = new byte[1024];
			DatagramPacket resposePacket = new DatagramPacket(resposeData, resposeData.length);
			client.receive(resposePacket);
			Datagram datagram = Format.byteArrayToDatagram(resposeData);
			return datagram;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//	上传文件
	private void sendFile() {
		
	}
	
	//	下载文件
	private void receiveFile() {
		
		
	}
	
	//	断开连接
	private void disconnect() {
		
	}
}

