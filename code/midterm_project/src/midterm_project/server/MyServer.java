package midterm_project.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.Connection;
import java.text.Format;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Templates;

import javafx.scene.chart.PieChart.Data;
import midterm_project.datagram.Datagram;

public class MyServer implements Runnable {
	private DatagramSocket server = null;
	private DatagramPacket requstPacket = null;
	private byte[] container;
	private int containerSize = 1024 * 64;
	private int port;
	private int x = 0;   //客户端已接收的最小分组序号
	private int y = 0;   //服务端已接收的最小分组序号
	private int type;    //0代表客户端上传，1代表客户端下载
	private String filePath; //要上传或下载的文件的路径
	private InetAddress clientAddress;
	private int clientPort;
	private Map<Integer, Datagram> map;
	public static int idlePort = 8081;  //闲置的端口,每次加1
	
	public MyServer(int _port) {
		//创建服务器及端口
		try {
			port = _port;
			server = new DatagramSocket(port);
			//准备容器,大小为1kB
			container = new byte[containerSize];   
			//封装成包
			requstPacket = new DatagramPacket(container, container.length);
			//map初始化
			map = new HashMap<Integer, Datagram>();
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
			Datagram requestDatagram = receivePacketAndFormat();
			System.out.println("子线程 " + port + " 服务器接收到 " + requestDatagram.getSeq() + "号分组");
			////接受到fin = 1,结束连接，发送对方ACK=1
			if (requestDatagram.getFIN() == 1) {
				Datagram reposeDatagram = new Datagram();
				reposeDatagram.setACK(1);
				reposeDatagram.setFIN(1);
				sendPacketAndFormat(reposeDatagram);
				
				server.close();
				System.out.println("端口为 " + port + " 的服务器已经断开连接");
				break;
			}
			else {
				map.put(requestDatagram.getSeq(), requestDatagram);
				while(map.get(y) != null) {
					writeMapToFile(map.get(y).getBuf());
					map.remove(y);
					y++;
				}
				
				Datagram resposeDatagram = new Datagram();
				resposeDatagram.setACK(1);
				resposeDatagram.setRwnd(100 - map.size());
				resposeDatagram.setAck(y);;
				sendPacketAndFormat(resposeDatagram);			
			}
				
			
		}
	}
	
	private void writeMapToFile(byte[] data) {
		try {
			String[] fileDecode = filePath.split("/");
			String fileName = fileDecode[fileDecode.length - 1];
			String storagePath = "D:/user_chen/network_test/" + fileName ;
			File file = new File(storagePath);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream oStream = new FileOutputStream(file, true);
			oStream.write(data, 0, data.length);
			oStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	public void clientDowload() {
		//主动发送结束连接fin = 1
		Datagram reposeDatagram = new Datagram();
		reposeDatagram.setFIN(1);
		sendPacketAndFormat(reposeDatagram);
		
		//接受对方发来的确认ACK=1
		Datagram requestDatagram = receivePacketAndFormat();
		if (requestDatagram.getACK() == 1) {
			server.close();
			System.out.println("端口为 " + port + " 的服务器已经断开连接");
		}
			
	}
	
	public int avaliblePort() {
		int tempPort = MyServer.idlePort;
		MyServer.idlePort++;
		return tempPort;
	}
	
	public void receiveFirstPacketAndNewThread() {
		//接受第一个	
		System.out.println("main thread server is wating for data......");
		Datagram firstDatagram = receivePacketAndFormat();
		System.out.println("main thread server has received data.");	
		setType(firstDatagram.getType());
		setFilePath(firstDatagram.getFilePath());
		setClientAddress(requstPacket.getAddress());
		setClientPort(requstPacket.getPort());
		
		int subport = createSubThread(); //返回子线程服务器的端口
		
		//发送第一个
		Datagram secondDatagram = new Datagram();
		secondDatagram.setACK(1);
		secondDatagram.setPort(subport);
		sendPacketAndFormat(secondDatagram);		
		
	}
	
	private int createSubThread() {
		//创建线程
		int subport = avaliblePort();
		MyServer subServer = new MyServer(subport);
		subServer.setType(type);
		subServer.setFilePath(filePath);
		subServer.setClientAddress(clientAddress);
		subServer.setClientPort(clientPort);
		Thread subThread = new Thread(subServer, subport + "");
		subThread.start();
		return subport;
	}
	
	private Datagram receivePacketAndFormat() {
		try {
			server.receive(requstPacket);
			byte[] requestData = requstPacket.getData();
			Datagram requestDatagram = midterm_project.datagram.Format.byteArrayToDatagram(requestData);	
			return requestDatagram;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private void sendPacketAndFormat(Datagram reposeDatagram) {
		try {
			byte[] resposeData = midterm_project.datagram.Format.datagramToByteArray(reposeDatagram);
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

