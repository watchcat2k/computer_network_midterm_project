package midterm_project.datagram;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Format {
	//	Datagram转二进制数组
	public static Datagram byteArrayToDatagram(byte[] b) {
		ByteArrayInputStream bis = null;
	    ObjectInputStream ois = null;
	    try {
	        bis = new ByteArrayInputStream(b);
	        ois = new ObjectInputStream(bis);
	        return (Datagram)ois.readObject();
	    } catch (ClassNotFoundException | IOException e) {
	    	e.printStackTrace();
	    } finally {
	        if(ois != null) {
	            try {
	                ois.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        if(bis != null) {
	            try {
	                bis.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    return null;
	}
	
	//	二进制数组转Datagram
	public static byte[] datagramToByteArray(Datagram obj) {
	    ByteArrayOutputStream bos = null;
	    ObjectOutputStream oos = null;
	    try {
	        bos = new ByteArrayOutputStream();
	        oos = new ObjectOutputStream(bos);
	        //读取对象并转换成二进制数据
	        oos.writeObject(obj);
	        return bos.toByteArray();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } finally {
	        if(oos != null) {
	            try {
	                oos.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        if(bos != null) {
	            try {
	                bos.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    return null;
	}
}
