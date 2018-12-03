package midterm_project.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.Connection;
import java.text.Format;

import midterm_project.datagram.Datagram;

public class MyServer implements Runnable {
	private DatagramSocket server = null;
	private DatagramPacket requstPacket = null;
	private byte[] container;
	private int port;
	private int x = 0;   //客户端分组序号
	private int y = 0;   //服务端分组序号
	private int type;    //0代表上传，1代表下载
	
	public MyServer(int _port) {
		//创建服务器及端口
		try {
			port = _port;
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
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("进入子线程， 服务器端口号为 " + Thread.currentThread().getName());
	}
	
	public void resposeFirstPacket() {
		
	}
	
	public int avaliblePort() {
		return 8081;
	}
	
	public void receiveFirstPacketAndNewThread() {
		try {
			//接受第一个	
			System.out.println("Server is wating for data......");
			server.receive(requstPacket);
			System.out.println("Server has received data.");
			byte[] requestData1 = requstPacket.getData();
			Datagram firstDatagram = midterm_project.datagram.Format.byteArrayToDatagram(requestData1);			
			int operateType = firstDatagram.getType();
			
			//创建线程
			int subport = avaliblePort();
			MyServer subServer = new MyServer(subport);
			subServer.setType(operateType);
			Thread subThread = new Thread(subServer, subport + "");
			subThread.start();
			
			//发送第一个
			InetAddress clientAddress = requstPacket.getAddress();
			int clientPort = requstPacket.getPort();
			Datagram secondDatagram = new Datagram();
			secondDatagram.setACK(1);
			secondDatagram.setPort(subport);
			byte[] resposeData1 = midterm_project.datagram.Format.datagramToByteArray(secondDatagram);
			DatagramPacket resposePacket = new DatagramPacket(resposeData1, resposeData1.length, clientAddress, clientPort);
			server.send(resposePacket);
			
		} catch (IOException e) {
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
	
			
			//发送第二个
			InetAddress clientAddress = requstPacket.getAddress();
			int clientPort = requstPacket.getPort();
			Datagram secondDatagram = new Datagram();
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
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
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

