package midterm_project.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.Connection;
import java.text.Format;

import midterm_project.datagram.Datagram;

public class MyServer {
	private DatagramSocket server = null;
	private DatagramPacket requstPacket = null;
	private byte[] container;
	private int x;   //客户端分组序号
	private int y = 0;   //服务端分组序号
	
	public MyServer(int port) {
		//创建服务器及端口
		try {
			server = new DatagramSocket(port);
			//准备容器,大小为1kB
			container = new byte[1024];   
			//封装成包
			requstPacket = new DatagramPacket(container, container.length);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void connect() {
		//三次握手
		try {
			//接受第一个
			System.out.println("Server is wating for data......");
			server.receive(requstPacket);
			System.out.println("Server has received data.");
			byte[] requestData1 = requstPacket.getData();
			Datagram firstDatagram = midterm_project.datagram.Format.byteArrayToDatagram(requestData1);
			if (firstDatagram.getSYN() == 1) {
				x = firstDatagram.getSeq();
			}
			
			//发送第二个
			InetAddress clientAddress = requstPacket.getAddress();
			int clientPort = requstPacket.getPort();
			Datagram secondDatagram = new Datagram();
			secondDatagram.setSYN(1);
			secondDatagram.setSeq(y);
			y++;
			secondDatagram.setACK(1);
			secondDatagram.setAck(x + 1);
			byte[] resposeData1 = midterm_project.datagram.Format.datagramToByteArray(secondDatagram);
			DatagramPacket resposePacket = new DatagramPacket(resposeData1, resposeData1.length, clientAddress, clientPort);
			server.send(resposePacket);
			
			//接受第三个
			server.receive(requstPacket);
			byte[] requestData2 = requstPacket.getData();
			Datagram thirdDatagram = midterm_project.datagram.Format.byteArrayToDatagram(requestData2);
			if (thirdDatagram.getACK() == 1 && thirdDatagram.getSeq() == (x + 1) && thirdDatagram.getAck() == y) {
				x++;
				System.out.println("The server has successfully connected with the client whose address is " + clientAddress + " and port is " + clientPort);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void transportData() {	
		try {	
			while(true) {
				//返回给客户端数据
				InetAddress clientAddress = requstPacket.getAddress();
				int clientPort = requstPacket.getPort();
				String resposeString = "Server respose to client";
				byte[] resposeData = resposeString.getBytes();
				DatagramPacket resposePacket = new DatagramPacket(resposeData, resposeData.length, clientAddress, clientPort);
				server.send(resposePacket);			
			}

			//释放
			//server.close();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	
}

