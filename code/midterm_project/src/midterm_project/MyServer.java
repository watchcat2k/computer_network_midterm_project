package midterm_project;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class MyServer {
	public static void main(String[] args) {
		DatagramSocket server = null;
	
		try {
			//创建服务器及端口
			server = new DatagramSocket(8080);
			//准备容器,大小为1kB
			byte[] container = new byte[1024];   
			//封装成包
			DatagramPacket requstPacket = new DatagramPacket(container, container.length);
			
			while(true) {
				//接受数据
				System.out.println("Server is wating for data......");
				server.receive(requstPacket);
				System.out.println("Server has received data.");
				//分析数据
				byte[] requestData = requstPacket.getData();
				int len = requstPacket.getLength();
				System.out.println(new String(requestData, 0, len));
				System.out.println('\n');
				
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
}

