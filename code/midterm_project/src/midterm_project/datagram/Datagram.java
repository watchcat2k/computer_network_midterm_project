package midterm_project.datagram;

public class Datagram {
	private int SYN;
	private int ACK;
	private int FIN;
	
	public Datagram() {
		
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
}
