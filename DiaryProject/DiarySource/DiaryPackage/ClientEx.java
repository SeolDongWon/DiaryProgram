package DiaryPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientEx {

	public static void main(String[] args) {
		BufferedReader readFromServer = null;
		BufferedWriter writeToServer = null;
		Socket sock = null;
		Scanner scan = new Scanner(System.in);
		try {
			sock = new Socket("localhost", 9999);
//			sock = new Socket("172.30.1.31", 9999); // 해당 IP와 포트로 접속
			System.out.println("===클라이언트=====");
			readFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); // 서버에서 온 데이터를 담당
			writeToServer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())); // 서버로 보낼 데이터를 담당
			while (true) { // 통신하는 동안 반복
				// 서버가 데이터를 보낼 차례
				String inputMessage = readFromServer.readLine(); // 서버에서 온 데이터는 문자열로 받는다
				String[] strArray = inputMessage.split("::"); // 서버에서 온 문자열을 "::"을 기준으로 분할해서 문자열배열로 만든다
				if (strArray[0].equals("종료")) { // 서버에서 온 문자열이 "종료"면 접속종료
					break;
				}
				for (int i = 0; i < strArray.length; i++) { // 문자열배열을 순서대로 출력
					System.out.println(strArray[i]);
				}
				// 클라이언트가 데이터를 보낼 차례
				System.out.print("입력>>");
				String outputMessage = scan.nextLine();
				writeToServer.write(outputMessage + "\n");
				writeToServer.flush();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				System.out.println("통신종료");
				scan.close();
				if (sock != null)
					sock.close(); // 소켓을 닫고 통신종료
			} catch (IOException e) {
				System.out.println("서버와 채팅 중 오류가 발생했습니다.");
			}
		}
	}
}