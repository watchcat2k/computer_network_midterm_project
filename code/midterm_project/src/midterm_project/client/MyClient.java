package midterm_project.client;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import midterm_project.datagram.Format;
import midterm_project.datagram.Datagram;

public class MyClient {
	private int sourcePort;
	private String destinationIp;
	private DatagramSocket client;
	private int x = 0;
	private int y = 0;
	private int fileTranPort;
	private int packetSize = 1024 * 64;
	private int fileSize = 1024 * 32;
	private Map<Integer, Datagram> map;
	private int rwnd;
	private int hasSent;		//	已发送但未被ACK=1的数据包的数量
	
	public MyClient(int sourcePort, String destinationIp) {
		this.sourcePort = sourcePort;
		this.destinationIp = destinationIp;
		
		map = new HashMap<Integer, Datagram>();
		
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
		upload.setFilePath(filePath);
		send(upload);
		Datagram uploadResponse = receive();
		if (uploadResponse.getACK() == 1) {
			fileTranPort = uploadResponse.getPort();
			System.out.println("获取的端口号为" + fileTranPort);
		}
		
		sendFile(filePath);
		
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
		disconnect.setPort(fileTranPort);
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
			byte[] resposeData = new byte[packetSize];
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
	private void sendFile(String filePath) {
		//	读取100个Datagram到缓冲区
		fileRead(filePath, 100);
		
		//	子线程--接收响应
		new Thread(new Runnable() {
		  @Override
		  public void run() {
			  while (true) {
				  Datagram datagram = receive();
				  if (datagram.getACK() == 1) {
					  map.remove(datagram.getSeq());
					  rwnd = datagram.getRwnd();
					  hasSent--;
					  fileRead(filePath, 1);
					  
					  if (datagram.getFIN() == 1) {
						  break;
					  }
				  }
			  }
		  }
		}).start();
		
		//	主线程--发送数据包
		hasSent = 0;		
		while (!map.isEmpty()) {
			if (hasSent <= rwnd) {
				send(map.get(x));
			}
		}
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
	
	private boolean fileRead(String FilePath, int num) {		//	num表示读取数, 返回false代表文件读取完毕
		File src = new File(FilePath);
		RandomAccessFile rFile;
		try {
			rFile = new RandomAccessFile(src, "r");
			rFile.seek(x * fileSize);
			for (int i = 0; i < num; i++) {
				Datagram datagram = new Datagram();
				byte[] buf = new byte[fileSize];
				if (rFile.read(buf) == -1) {
					return false;
				}
				datagram.setPort(fileTranPort);
				datagram.setBuf(buf);
				datagram.setSeq(x + i);
				map.put(x + i, datagram);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
}

