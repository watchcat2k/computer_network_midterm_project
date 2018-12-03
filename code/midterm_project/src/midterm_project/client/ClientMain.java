package midterm_project.client;

import java.util.Scanner;

public class ClientMain {
	public static void main(String[] args) {
		MyClient myClient = new MyClient(8888, "localhost");
		
		System.out.println("请输入要上传或下载的文件");
		Scanner input = new Scanner(System.in);
		String inputStr = input.nextLine();
		
		String[] inputArray = inputStr.split("\\s+");
		String filePath = inputArray[2];
		
		if (inputArray[1].equals("lget")) {			//	����
			myClient.Download(filePath);
		}
		else if (inputArray[1].equals("lsend")) {	//	�ϴ�
			myClient.Upload(filePath);
		}
	}
}
