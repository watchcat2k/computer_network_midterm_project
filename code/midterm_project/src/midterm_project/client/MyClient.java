package midterm_project.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import midterm_project.datagram.Format;
import midterm_project.datagram.Datagram;

public class MyClient {
	private int sourcePort;
	private String destinationIp;
	private DatagramSocket client;
	private int x = 0;
	private int y = 0;
	private int fileTranPort;
	
	public MyClient(int sourcePort, String destinationIp) {
		this.sourcePort = sourcePort;
		this.destinationIp = destinationIp;
		
		try {
			client = new DatagramSocket(sourcePort);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void Upload(String filePath) {
		//	发送表示上传的数据包并接收响应
		Datagram upload = new Datagram();
		upload.setType(0);
		upload.setFileName(filePath);
		send(upload);
		Datagram uploadResponse = receive();
		if (uploadResponse.getACK() == 1) {
			fileTranPort = uploadResponse.getPort();
			System.out.println("获取的端口号为" + fileTranPort);
		}
		
		sendFile();
		
		//	断开连接
		Datagram disconnect = new Datagram();
		disconnect.setFIN(1);
		disconnect.setPort(fileTranPort);
		send(disconnect);
		Datagram disconnectResponse = receive();
		if (disconnectResponse.getACK() == 1) {
			System.out.println("客户端" + sourcePort + "已断开连接");
		}
	}
	
	public void Download(String filePath) {
		//	发送表示下载的数据包并接收响应
		Datagram download = new Datagram();
		download.setType(1);
		download.setFilePath(filePath);
		send(download);
		Datagram response = receive();
		if (response.getACK() == 1) {
			fileTranPort = response.getPort();
		}
		
		receiveFile();
		
		//	断开连接
		Datagram disconnect = new Datagram();
		disconnect.setACK(1);
		send(disconnect);
	}
	
	
	//	发送数据包
	private void send(Datagram upload) {
		try {
			byte[] requstData = Format.datagramToByteArray(upload);
			DatagramPacket requstPacket = new DatagramPacket(requstData, requstData.length, new InetSocketAddress(destinationIp, upload.getPort()));
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
		while (true) {
			Datagram response = receive();
			if (response.getFIN() == 1) {
				System.out.println("客户端" + sourcePort + "已断开连接");
				break;
			}
		}
	}
}

