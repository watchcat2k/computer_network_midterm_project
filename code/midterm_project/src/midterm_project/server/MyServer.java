package midterm_project.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.Connection;
import java.text.Format;

import javax.xml.transform.Templates;

import javafx.scene.chart.PieChart.Data;
import midterm_project.datagram.Datagram;

public class MyServer implements Runnable {
	private DatagramSocket server = null;
	private DatagramPacket requstPacket = null;
	private byte[] container;
	private int port;
	private int x = 0;   //客户端分组序号
	private int y = 0;   //服务端分组序号
	private int type;    //0代表客户端上传，1代表客户端下载
	private String filePath; //要上传或下载的文件的路径
	private InetAddress clientAddress;
	private int clientPort;
	public static int idlePort = 8081;  //闲置的端口,每次加1

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
		System.out.println("进入子线程， 服务器端口号为 " + Thread.currentThread().getName() + " 类型为 " + type);
		//当前代理的server，有自己的port，有客户端想要的操作type
		if (type == 0) {
			clientUplaod();
		}
		else if (type == 1) {
			clientDowload();
		}
		
	}
	
	public void clientUplaod() {
		while (true) {
			try {		
				server.receive(requstPacket);
				byte[] requestData = requstPacket.getData();
				Datagram requestDatagram = midterm_project.datagram.Format.byteArrayToDatagram(requestData);
				
				////接受到fin = 1,结束连接，发送对方ACK=1
				if (requestDatagram.getFIN() == 1) {
					Datagram reposeDatagram = new Datagram();
					reposeDatagram.setACK(1);
					byte[] resposeData = midterm_project.datagram.Format.datagramToByteArray(reposeDatagram);
					DatagramPacket resposePacket = new DatagramPacket(resposeData, resposeData.length, clientAddress, clientPort);
					server.send(resposePacket);
					
					server.close();
					System.out.println("端口为 " + port + " 的服务器已经断开连接");
					break;
				}
				else {
					
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void clientDowload() {
		try {
			//主动发送结束连接fin = 1
			Datagram reposeDatagram = new Datagram();
			reposeDatagram.setFIN(1);
			byte[] resposeData = midterm_project.datagram.Format.datagramToByteArray(reposeDatagram);
			DatagramPacket resposePacket = new DatagramPacket(resposeData, resposeData.length, clientAddress, clientPort);
			server.send(resposePacket);
			
			//接受对方发来的确认ACK=1
			server.receive(requstPacket);
			byte[] requestData = requstPacket.getData();
			Datagram requestDatagram = midterm_project.datagram.Format.byteArrayToDatagram(requestData);
			if (requestDatagram.getACK() == 1) {
				server.close();
				System.out.println("端口为 " + port + " 的服务器已经断开连接");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int avaliblePort() {
		int tempPort = MyServer.idlePort;
		MyServer.idlePort++;
		return tempPort;
	}
	
	public void receiveFirstPacketAndNewThread() {
		try {
			//接受第一个	
			System.out.println("main thread server is wating for data......");
			server.receive(requstPacket);
			System.out.println("main thread server has received data.");
			byte[] requestData = requstPacket.getData();
			Datagram firstDatagram = midterm_project.datagram.Format.byteArrayToDatagram(requestData);			
			int operateType = firstDatagram.getType();
			String operateFilePath = firstDatagram.getFilePath();
			setClientAddress(requstPacket.getAddress());
			setClientPort(requstPacket.getPort());
			
			//创建线程
			int subport = avaliblePort();
			MyServer subServer = new MyServer(subport);
			subServer.setType(operateType);
			subServer.setFilePath(operateFilePath);
			subServer.setClientAddress(clientAddress);
			subServer.setClientPort(clientPort);
			Thread subThread = new Thread(subServer, subport + "");
			subThread.start();
			
			//发送第一个
			Datagram secondDatagram = new Datagram();
			secondDatagram.setACK(1);
			secondDatagram.setPort(subport);
			byte[] resposeData = midterm_project.datagram.Format.datagramToByteArray(secondDatagram);
			DatagramPacket resposePacket = new DatagramPacket(resposeData, resposeData.length, clientAddress, clientPort);
			server.send(resposePacket);
			
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
	
	public String getFilePath() {
		return filePath;
	}

	public InetAddress getClientAddress() {
		return clientAddress;
	}

	public void setClientAddress(InetAddress clientAddress) {
		this.clientAddress = clientAddress;
	}

	public int getClientPort() {
		return clientPort;
	}

	public void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	
}

