package midterm_project.client;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import midterm_project.datagram.Format;
import midterm_project.datagram.Datagram;

public class MyClient {
	private int sourcePort;
	private String destinationIp;
	private DatagramSocket client;
	private int base = 0;			//	最小未接收到的分组号
	private int prevBase = base;	//	1s前的base
	private int nextSeqNum = 0;		//	最小未发送的分组号
	private int fileTranPort;
	private int packetSize = 1024 * 64;
	private int fileSize = 1024 * 32;
	private int fileReadNum = 0;
	private Map<Integer, Datagram> map;
	private int rwnd;			//	流量控制
	private int cwnd = 1;		//	拥塞控制当前值
	private int ssthresh = 8;	//	拥塞控制阈值	
	private static Lock mapLock  = new ReentrantLock();
	private static Lock cwndLock = new ReentrantLock();
	
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
		
		
		//	开启计时器, 每隔0.5s检查是否丢包
        Timer timer = new Timer();  
        long delay = 0;  
        long intevalPeriod = 1 * 500;  
        
        timer.scheduleAtFixedRate(new TimerTask() {  
            @Override  
            public void run() {  
            	if (base == prevBase) {
            		System.out.println("分组" + base + "丢失");
            		nextSeqNum = base;
            		cwndLock.lock();
            		ssthresh = cwnd / 2;
            		cwnd = 1;
            		cwndLock.unlock();
            		System.out.println("拥塞控制处于快速恢复阶段, cwnd = " + cwnd + ", 阈值ssthresh = " + ssthresh);
            	} else {
            		prevBase = base;
            	}
            }  
        }, delay, intevalPeriod);
		
		//	子线程--接收响应
		new Thread(new Runnable() {
		  @Override
		  public void run() {
			  while (true) {
				  Datagram datagram = receive();
				  if (datagram.getACK() == 1) {					  
					  if (datagram.getFIN() == 1) {
						  System.out.println("客户端" + sourcePort + "已断开连接");
						  break;
					  } else {
						  for (int i = base; i < datagram.getAck(); i++) {
							  mapLock.lock();
							  map.remove(i);  
							  mapLock.unlock();
							  System.out.println("分组" + i + "传输成功");
							  cwndLock.lock();
							  if (cwnd < ssthresh) {
								  cwnd *= 2;
								  System.out.println("拥塞控制处于慢启动阶段, cwnd = " + cwnd + ", 阈值ssthresh = " + ssthresh);
							  }
							  else {
								  cwnd++;
								  System.out.println("拥塞控制处于拥塞避免阶段, cwnd = " + cwnd + ", 阈值ssthresh = " + ssthresh);
							  }
							  cwndLock.unlock();
							  base++;
							  fileRead(filePath, 1);
						  }
						  rwnd = datagram.getRwnd();
					  }
				  }
			  }
		  }
		}).start();
		
		//	主线程--传输数据包
		fileRead(filePath, 100);
		
		while (true) {
			mapLock.lock();
			if (map.isEmpty()) {
				mapLock.unlock();
				break;
			}
			mapLock.unlock();
			
			for (int i = 0; i < cwnd; i++) {
				if (nextSeqNum - base <= rwnd) {
					if (map.get(nextSeqNum) != null) {
						send(map.get(nextSeqNum));
						System.out.println("发送分组" + map.get(nextSeqNum));
						nextSeqNum++;
					}
				}	
			}
		}
		
		System.out.println("文件上传完成");
		
		//	传输完成, 主动断开连接
		Datagram disconnect = new Datagram();
		disconnect.setFIN(1);
		disconnect.setPort(fileTranPort);
		send(disconnect);
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
	
	private void fileRead(String FilePath, int num) {		//	num表示读取数
		File src = new File(FilePath);
		RandomAccessFile rFile;
		try {
			rFile = new RandomAccessFile(src, "r");
			rFile.seek(fileReadNum * fileSize);
			for (int i = 0; i < num; i++) {
				Datagram datagram = new Datagram();
				byte[] buf = new byte[fileSize];
				if (rFile.read(buf) == -1) {
					break;
				}
				datagram.setPort(fileTranPort);
				datagram.setBuf(buf);
				datagram.setSeq(fileReadNum);
				mapLock.lock();
				map.put(fileReadNum, datagram);
				mapLock.unlock();
				fileReadNum++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

