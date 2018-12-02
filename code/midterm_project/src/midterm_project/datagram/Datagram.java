package midterm_project.datagram;

import java.io.Serializable;

public class Datagram implements Serializable {
	private int SYN;
	private int ACK;
	private int FIN;
	private int seq;
	private int ack;
	
	public Datagram() {
		SYN = 0;
		ACK = 0;
		FIN = 0;
		seq = 0;
		ack = 0;
	}
	
	public int getSYN() {
		return SYN;
	}
	
	public void setSYN(int SYN) {
		this.SYN = SYN;
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
}
