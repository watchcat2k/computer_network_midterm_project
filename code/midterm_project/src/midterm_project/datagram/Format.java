package midterm_project.datagram;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Format {
	//	二进制数组转Datagram
	public static Datagram byteArrayToDatagram(byte[] bytes) {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bais);
            Object object = ois.readObject();
            return (Datagram)object;
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } finally {
            try {
                ois.close();
            } catch (Exception e) {
            }
        }
	}
	
	//	Datagram转二进制数组
	public static byte[] datagramToByteArray(Datagram obj) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            byte[] bytes = baos.toByteArray();
            return bytes;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                oos.close();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
        return null;
	}
}
