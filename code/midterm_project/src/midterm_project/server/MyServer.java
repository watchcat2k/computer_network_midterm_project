package midterm_project.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.interfaces.RSAMultiPrimePrivateCrtKey;
import java.sql.Connection;
import java.text.Format;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.transform.Templates;

import javafx.scene.chart.PieChart.Data;
import midterm_project.datagram.Datagram;

public class MyServer implements Runnable {
	private DatagramSocket server = null;
	private DatagramPacket requstPacket = null;
	private byte[] container;
	private int containerSize = 1024 * 64;
	private int fileSize = 1024 * 32;
	private int fileReadNum = 0;
	private int port;
	private int x = 0;   //客户端已接收的最小分组序号
	private int y = 0;   //服务端已接收的最小分组序号
	private int rwnd = 100;
	private int cwnd = 1;
	private int ssthresh = 8;
	private int nextSeqNum = 0;
	private int base = 0;
	private int prevBase = base;
	private int type;    //0代表客户端上传，1代表客户端下载
	private String filePath; //要上传或下载的文件的路径
	private String storagePath = "D:/user_chen/network_test/"; //要保存的文件的路径,注意后面要加上文件名
	private InetAddress clientAddress;
	private int clientPort;
	private Map<Integer, Datagram> map;

	private static Lock mapLock = new ReentrantLock();
	private static Lock cwndLock = new ReentrantLock();
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
		//如果文件已存在，先删除，后再接受上传的文件
		String[] fileDecode = filePath.split("/");
		String fileName = fileDecode[fileDecode.length - 1];
		String storageFilePath = storagePath + fileName ;
		File file = new File(storageFilePath);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			else {
				file.delete();
				file.createNewFile();
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while (true) {
			Datagram requestDatagram = receivePacketAndFormat();
			System.out.println("子线程 " + port + " 服务器接收到 " + requestDatagram.getSeq() + "号分组 FIN=" +  requestDatagram.getFIN());
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
				System.out.println("子线程"+ port + "缓冲区窗口空间剩余" + (100-map.size()));
				resposeDatagram.setAck(y);;
				sendPacketAndFormat(resposeDatagram);			
			}
				
			
		}
	}
	
	private void writeMapToFile(byte[] data) {
		try {
			String[] fileDecode = filePath.split("/");
			String fileName = fileDecode[fileDecode.length - 1];
			String storageFilePath = storagePath + fileName ;
			File file = new File(storageFilePath);
			FileOutputStream oStream = new FileOutputStream(file, true);
			oStream.write(data, 0, data.length);
			oStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	public void clientDowload() {
		//		子线程--接收响应
			new Thread(new Runnable() {
			  @Override
			  public void run() {
				  while (true) {
					  Datagram datagram = receivePacketAndFormat();
					  if (datagram.getACK() == 1) {					  
						  if (datagram.getFIN() == 1) {
							  System.out.println("客户端" + clientPort + "已断开连接");
							  break;
						  } else {
							  for (int i = base; i < datagram.getAck(); i++) {
								  mapLock.lock();
								  map.remove(i);  
								  mapLock.unlock();
								  System.out.println("子线程" + port + "分组" + i + "被成功接收");
								  cwndLock.lock();
								  if (cwnd < ssthresh) {
									  cwnd *= 2;
									  System.out.println("子线程" + port + "拥塞控制处于慢启动阶段, cwnd = " + cwnd + ", 阈值ssthresh = " + ssthresh);
								  }
								  else {
									  cwnd++;
									  System.out.println("子线程" + port + "拥塞控制处于拥塞避免阶段, cwnd = " + cwnd + ", 阈值ssthresh = " + ssthresh);
								  }
								  cwndLock.unlock();
								  base++;
								  fileRead(filePath, 1);
							  }
							  rwnd = datagram.getRwnd();
							  System.out.println("子线程" + port + "服务器空闲缓冲区大小为" + rwnd + ", 已发送分组数量为" + (nextSeqNum - base));
						  }
					  }
				  }
			  }
			}).start();
		
		fileRead(filePath, 100);
		
		//		开启计时器, 每隔0.5s检查是否丢包
        Timer timer = new Timer();  
        long delay = 0;  
        long intevalPeriod = 1 * 100;  
        
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
            		System.out.println("子线程" + port + "拥塞控制处于快速恢复阶段, cwnd = " + cwnd + ", 阈值ssthresh = " + ssthresh);
            	} else {
            		prevBase = base;
            	}
            }  
        }, delay, intevalPeriod);
		
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
						sendPacketAndFormat(map.get(nextSeqNum));
						System.out.println("子线程" + port + "发送分组" + nextSeqNum);
						nextSeqNum++;
						System.out.println("子线程" + port + "客户端空闲缓冲区大小为" + rwnd + ", 已发送分组数量为" + (nextSeqNum - base));
					}
				}	
			}
		}
		
		System.out.println("文件下载完成");
		
		
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
			
		timer.cancel();
	}
	
	public int avaliblePort() {
		int tempPort = MyServer.idlePort;
		MyServer.idlePort++;
		return tempPort;
	}
	
	//	从文件中读取数据流
	private void fileRead(String FilePath, int num) {		//	num表示读取块数
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
				datagram.setPort(clientPort);
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

