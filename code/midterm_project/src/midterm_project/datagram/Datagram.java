package midterm_project.datagram;

import java.io.Serializable;

public class Datagram implements Serializable {
	private int ACK;	//	分组响应
	private int FIN;	//	断开连接
	private int seq;	//	当前分组的序号
	private int ack;	//	期望收到的下一个分组的序号
	private int type;	//	0表示上传, 1表示下载
	private int port;	//	指定服务器端口号
	private String filePath;	//	传输文件名
	private byte[] buf;
	private int rwnd;	//	流量控制

	public Datagram() {
		ACK = 0;          
		FIN = 0;
		seq = 0;
		ack = 0;
		port = 8080;
		filePath = "";
		buf = new byte[1024 * 32];
		rwnd = 1000;
	}
	
	public int getRwnd() {
		return rwnd;
	}

	public void setRwnd(int rwnd) {
		this.rwnd = rwnd;
	}
	
	public byte[] getBuf() {
		return buf;
	}

	public void setBuf(byte[] buf) {
		this.buf = buf;
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getACK() {
		return ACK;
	}
	
	public void setACK(int ACK) {
		this.ACK = ACK;
	}
	
	public int getFIN() {
		return FIN;
	}
	
	public void setFIN(int FIN) {
		this.FIN = FIN;
	}
	
	public int getSeq() {
		return seq;
	}
	
	public void setSeq(int seq) {
		this.seq = seq;
	}
	
	public int getAck() {
		return ack;
	}
	
	public void setAck(int ack) {
		this.ack = ack;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
}
