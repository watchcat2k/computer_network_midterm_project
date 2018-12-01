package midterm_project;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

public class MyClient {
	//static private String ipAddress = "47.107.245.11";
	static private String ipAddress = "localhost";
	
	public static void main(String[] args) {
		DatagramSocket client = null;
		
		try {
			//创建客户和端口
			client = new DatagramSocket(8888);
			//准备数据
			String mString = "HelloWorld";
			byte[] requstData = mString.getBytes();
			//打包
			DatagramPacket requstPacket = new DatagramPacket(requstData, requstData.length, new InetSocketAddress(ipAddress, 8080));
			//发送
			client.send(requstPacket);
			
			//接收来自服务器的respost
			byte[] resposeData = new byte[1024];
			DatagramPacket resposePacket = new DatagramPacket(resposeData, resposeData.length);
			client.receive(resposePacket);
			String resposeString = new String(resposeData, 0, resposePacket.getLength());
			System.out.println(resposeString);
			
			//释放
			client.close();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

