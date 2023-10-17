package DiaryPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

public class ServerEx {
	public static Scanner scan = new Scanner(System.in);

	public static void main(String[] args) throws Exception {

		BufferedReader readFromCT = null;
		BufferedWriter writeToCT = null;
		ServerSocket serverSock = null;
		Socket sock = null;
		Account account = null; // 로그인시 계정정보를 저장할 객체
		ArrayList<RecordRead> recordReadArrayList; // 출력을 위해 텍스트 내용을 저장할 객체

		try {
			serverSock = new ServerSocket(9999); // 서버 오픈
			System.out.println("===서버=======");
			System.out.println("연결을 기다리고 있습니다.....");
			sock = serverSock.accept(); // 클라이언트의 연결을 기다림
			System.out.println("연결되었습니다.");
			readFromCT = new BufferedReader(new InputStreamReader(sock.getInputStream())); // 클라이언트에서 온 데이터를 담당
			writeToCT = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())); // 클라이언트로 보낼 데이터를 담당

			String selectMenu;
			boolean menuFlag = false;
			boolean loginFlag = false;

			while (!menuFlag) { // 통신하는 동안 반복
				writeToCT.write("0종료 1로그인 2회원가입" + "\n"); // 메뉴를 클라이언트에게 보내고
				writeToCT.flush();
				selectMenu = readFromCT.readLine(); // 클라이언트가 선택한 메뉴를 받는다

				switch (selectMenu) {
				case "0": // "종료"를 클라이언트에 보내고
					writeToCT.write("종료" + "\n");
					writeToCT.flush();
					menuFlag = true; // while반복문을 끝낸다
					break;
				case "1": // 로그인 화면으로 이동, 데이터를 주고받는 객체를 매개변수로 보냄
					account = login(readFromCT, writeToCT);
					if (account != null) { // 계정 객체가 null이 아니라면 로그인에 성공한 상태
						loginFlag = true;
						menuFlag = true; // 로그인에 성공했다는 플래그와 로그인메뉴 반복문을 빠져나갈 플래그를 준다
					}
					break;
				case "2": // 계정을 새로 등록한다
					signUp(readFromCT, writeToCT);
					break;

				default:
				}
			}
			if (loginFlag) {
				logWrite(account.getId(), 1); // 클라이언트가 행동한 기록을 저장한다
				writeToCT.write("로그인 성공" + "::" + "(진행:엔터)" + "\n");
				writeToCT.flush();
				readFromCT.readLine();
				menuFlag = false;
				String loginId = null;

				while (!menuFlag) { // 로그인 성공시 해당 메뉴를 클라이언트에 보낸다
					writeToCT.write("0종료 1내정보 2글쓰기 3내글읽기 4남글읽기" + "\n");
					writeToCT.flush();
					selectMenu = readFromCT.readLine();
					switch (selectMenu) {
					case "0":
						logWrite(account.getId(), 0);
						menuFlag = true;
						writeToCT.write("종료" + "\n");
						writeToCT.flush();
						break;
					case "1": // 자신의 계정정보를 확인한다
						myAccount(readFromCT, writeToCT, account);
						break;
					case "2": // 글을 쓴다
						logWrite(account.getId(), 2);
						write(readFromCT, writeToCT, account);
						break;
					case "3": // 내가 쓴 글을 읽는다
						logWrite(account.getId(), 3);
						loginId = account.getId(); // 기록을 검색할 아이디로 자신의 아이디를 지정한다
						recordReadArrayList = new ArrayList<RecordRead>(); // 글목록을 저장할 객체를 만들고 변수를 지정한다
						readme(readFromCT, writeToCT, loginId, recordReadArrayList);
						break;
					case "4": // 남이 쓴 글을 읽는다
						writeToCT.write("글을 검색할 아이디" + "\n");
						writeToCT.flush();
						logWrite(account.getId(), 4);
						loginId = readFromCT.readLine(); // 누가 쓴 글을 읽을지 클라이언트에서 아이디를 적는다
						recordReadArrayList = new ArrayList<RecordRead>();
						readme(readFromCT, writeToCT, loginId, recordReadArrayList);
						break;
					default:
					}
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				System.out.println("통신종료");
				if (sock != null)
					sock.close();
				if (serverSock != null)
					serverSock.close();
			} catch (IOException e) {
				System.out.println("클라이언트와 채팅 중 오류가 발생했습니다.");
			}
		}
	}

	private static void readme(BufferedReader readFromCT, BufferedWriter writeToCT, String loginId,
			ArrayList<RecordRead> recordReadArrayList) throws Exception {
		txtFileToRecordList(loginId, recordReadArrayList);
		printRecordList(readFromCT, writeToCT, loginId, recordReadArrayList);

	}

	private static void printRecordList(BufferedReader readFromCT, BufferedWriter writeToCT, String loginId,
			ArrayList<RecordRead> recordReadArrayList) throws Exception {
		String strToClient = ""; // 어레이리스트의 내용을 받을 문자열 변수

		for (int i = 0; i < recordReadArrayList.size(); i++) { // 어레이리스트의 모든 내용을 하나의 문자열로 만든다
			RecordRead recordRead = recordReadArrayList.get(i);
			if (loginId.equals(recordRead.getId())) {
				strToClient = strToClient + "::" + "날짜 : " + recordRead.getData() + "::" + "아이디 : " + recordRead.getId()
						+ "::" + "내용 : " + recordRead.getContent() + "::" + "-";
			}
		}
		strToClient = strToClient + "::" + "(진행:엔터)"; // 보낸 문자열은 클라이언트에서 "::"로 분할 후 출력한다
		writeToCT.write(strToClient + "\n");
		writeToCT.flush();
		readFromCT.readLine();
	}

	private static void txtFileToRecordList(String loginId, ArrayList<RecordRead> recordReadArrayList) {
		try {
			FileReader fileRead = new FileReader("DiarySource/record.txt");
			BufferedReader buffRead = new BufferedReader(fileRead);

			String str = null;
			String[] strArray = new String[3];
			// record.txt에서 아이디와 같은 문자열을 검색한다
			while ((str = buffRead.readLine()) != null) {
				if (str.equals(loginId)) { // 같은 문자열이 나오면
					strArray[0] = str; // 해당문자열(=아이디)를 배열에 넣는다
					strArray[1] = buffRead.readLine(); // 그 다음 문자열(=날짜)를 배열에 넣는다
					strArray[2] = buffRead.readLine(); // 그 다음 문자열(=내용)을 배열에 넣는다
					RecordRead recordRead = new RecordRead(strArray[0], strArray[1], strArray[2]);
					recordReadArrayList.add(recordRead); // 배열의 내용으로 객체를 만들고 객체를 어레이리스트에 넣는다
				}
			}
			buffRead.close();
			fileRead.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private static void write(BufferedReader readFromCT, BufferedWriter writeToCT, Account account) throws Exception {
		String[] writeRecord = new String[3];
		Date date = new Date();

		SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
		String strDate = formatter.format(date); // 현재시각을 배열에 저장한다
		writeRecord[0] = account.getId(); // 아이디를 배열에 저장한다
		writeRecord[1] = strDate;

		writeToCT.write("글을 쓰세요" + "\n");
		writeToCT.flush();
		writeRecord[2] = readFromCT.readLine(); // 클라이언트로부터 받은 데이터를 배열에 저장한다
		try {
			FileWriter fileWrite = new FileWriter("DiarySource/record.txt", true); // 배열의 내용을 기록텍스트파일에 저장한다
			for (int i = 0; i < writeRecord.length; i++) {
				fileWrite.write(writeRecord[i] + "\n");
			}
			fileWrite.close();
			writeToCT.write("글을 썼습니다" + "::" + "(진행:엔터)" + "\n");
			writeToCT.flush();
			readFromCT.readLine();
			;
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private static void myAccount(BufferedReader readFromCT, BufferedWriter writeToCT, Account account)
			throws Exception {
		String selectMenu = null;
		// 로그인시 만든 계정객체에 저장한 계정정보를 클라이언트에게 보낸다
		writeToCT.write(
				"아이디 : " + account.getId() + "::" + "패스워드 : " + account.getPass() + "::" + "연락처 : " + account.getCall()
						+ "::" + "주소 : " + account.getAddress() + "::" + "0나가기   1패스워드 변경   2연락처 변경   3주소 변경" + "\n");
		writeToCT.flush();
		selectMenu = readFromCT.readLine();
		// 계정정보 변경(미완성)
		String changeInput = null;
		boolean loopFlag = false;
//		while (!loopFlag) {
		switch (selectMenu) {
		case "0":
			loopFlag = true;
			break;
		case "1":
			writeToCT.write("새로운 패스워드 입력(미완성) " + "\n");
			writeToCT.flush();
			changeInput = readFromCT.readLine();
			break;
		case "2":
			writeToCT.write("새로운 연락처 입력(미완성)" + "\n");
			writeToCT.flush();
			changeInput = readFromCT.readLine();
			break;
		case "3":
			writeToCT.write("새로운 주소 입력(미완성) " + "\n");
			writeToCT.flush();
			changeInput = readFromCT.readLine();
			break;
		default:
		}
		modifyAccount(account.getId(), selectMenu, changeInput);
//		}
	}

	private static void modifyAccount(String accountID, String selectMenu, String changeInput) throws Exception {
		FileReader fileRead = new FileReader("DiarySource/account.txt");
		BufferedReader buffRead = new BufferedReader(fileRead);
		ArrayList<String> readList = new ArrayList<String>();
		String readStr = null;
		while ((readStr = buffRead.readLine()) != null) {
			readList.add(readStr);
		}
		System.out.println("readList println");
		System.out.println(readList);
		int idLine = readList.indexOf("id:" + accountID);
		System.out.print("\nidLine println\n");
		System.out.println(idLine);
		switch (selectMenu) {
		case "1":
			readList.remove(idLine + 1);
			readList.add(idLine + 1, "pass:" + changeInput);
			break;
		case "2":
			readList.remove(idLine + 4);
			readList.add(idLine + 4, "call:" + changeInput);
			break;
		case "3":
			readList.remove(idLine + 5);
			readList.add(idLine + 5, "adress:" + changeInput);
			break;
		default:
			break;
		}
		System.out.println("readList println");
		System.out.println(readList);
		FileWriter fileWrite = new FileWriter("DiarySource/account.txt");
		for (int i = 0; i < readList.size(); i++) {
			fileWrite.write(readList.get(i) + "\n");
		}
		fileWrite.close();
		fileRead.close();
		buffRead.close();
	}

	private static void signUp(BufferedReader readFromCT, BufferedWriter writeToCT) throws Exception {
		// 아이디 입력
		String inputId = null;
		boolean idFlag = false;
		while (!idFlag) {
			String str = null;
			String[] strArray = null;
			writeToCT.write("사용할 아이디 입력 10자 제한" + "\n");
			writeToCT.flush();
			inputId = readFromCT.readLine().trim();
			if (inputId.length() > 9) { // 아이디가 너무 길면 다시한다
				writeToCT.write("너무 길어요" + "::" + "(진행:엔터)" + "\n");
				writeToCT.flush();
				readFromCT.readLine();
				continue;
			}
			idFlag = true; // 우선 반복문을 빠져나가기 위한 플래그를 주고 이후 과정이 문제없으면 아이디입력 반복문을 나간다
			FileReader fileRead = new FileReader("DiarySource/account.txt");
			BufferedReader buffRead = new BufferedReader(fileRead);
			while ((str = buffRead.readLine()) != null) {// 계정정보가 있는 텍스트파일에 사용할 아이디와 중복된 아이디가 있는지 검사한다
				strArray = str.split(":");
				if (strArray[0].equals("id") && strArray[1].equals(inputId)) {
					idFlag = false; // 중복을 발견하면 아이디입력 반복문을 다시 시작할 플래그를 준다
				}
			}
			if (!idFlag) {
				writeToCT.write("아이디 중복" + "::" + "(진행:엔터)" + "\n");
				writeToCT.flush();
				readFromCT.readLine();
			}
			fileRead.close();
			buffRead.close();
		}
		writeToCT.write(inputId + " 사용가능" + "::" + "(진행:엔터)" + "\n");
		writeToCT.flush();
		readFromCT.readLine();

		// 비밀번호 입력
		idFlag = false;
		String inputPass = null;
		String inputPass2 = null;
		while (!idFlag) {
			writeToCT.write("비밀번호 입력 10자 제한" + "\n");
			writeToCT.flush();
			inputPass = readFromCT.readLine();
			if (inputPass.length() > 9) {
				writeToCT.write("너무 길어요" + "::" + "(진행:엔터)" + "\n");
				writeToCT.flush();
				readFromCT.readLine();
				continue;
			}
			writeToCT.write("비밀번호 재입력" + "\n");
			writeToCT.flush();
			inputPass2 = readFromCT.readLine();
			// 처음 입력한 패스워드와 재입력한 패스워드가 같은지 판단한다
			if (inputPass.equalsIgnoreCase(inputPass2)) {
				writeToCT.write("비밀번호 사용가능" + "::" + "(진행:엔터)" + "\n");
				writeToCT.flush();
				readFromCT.readLine();
				idFlag = true;
			} else {
				writeToCT.write("비밀번호가 달라요" + "::" + "(진행:엔터)" + "\n");
				writeToCT.flush();
				readFromCT.readLine();
			}
		}

		// 전화번호 입력
		String inpuCallNum = null;
		writeToCT.write("전화번호 입력" + "\n");
		writeToCT.flush();
		inpuCallNum = readFromCT.readLine();

		// 주소 입력
		String inputAddress = null;
		writeToCT.write("주소 입력" + "\n");
		writeToCT.flush();
		inputAddress = readFromCT.readLine();

		String str = null;
		// 보낸 데이터를 클라이언트 쪽에서 배열로 나눌 수 있도록 "::"을 문자열 사이에 추가한다
		writeToCT.write("아이디 : " + inputPass + "::" + "연락처 : " + inpuCallNum + "::" + "거주지 : " + inputAddress + "::"
				+ "회원가입을 완료하시겠습니까? Y | N" + "\n");
		writeToCT.flush();
		str = readFromCT.readLine();

		if (str.toUpperCase().equals("Y")) {
			String[] writeAccount = new String[6]; // 클라이언트가 입력한 정보를 배열에 저장하고
			writeAccount[0] = inputId;
			writeAccount[1] = inputPass;
			writeAccount[2] = "일반";
			writeAccount[3] = "일반";
			writeAccount[4] = inpuCallNum;
			writeAccount[5] = inputAddress;

			FileWriter fileWrite = new FileWriter("DiarySource/account.txt", true); // 계정목록텍스트의 마지막에 해당 배열의 내용을 추가한다

			fileWrite.write("id:" + writeAccount[0] + "\n");
			fileWrite.write("pass:" + writeAccount[1] + "\n");
			fileWrite.write("type:" + writeAccount[2] + "\n");
			fileWrite.write("authority:" + writeAccount[3] + "\n");
			fileWrite.write("call:" + writeAccount[4] + "\n");
			fileWrite.write("adress:" + writeAccount[5] + "\n");

			fileWrite.close();
			writeToCT.write("새 회원 정보가 저장되었습니다" + "::" + "(진행:엔터)" + "\n");
			writeToCT.flush();
			readFromCT.readLine();
		} else {
			writeToCT.write("처음으로 돌아갑니다" + "::" + "(진행:엔터)" + "\n");
			writeToCT.flush();
			readFromCT.readLine();
		}
	}

	private static Account login(BufferedReader readFromCT, BufferedWriter writeToCT) throws Exception {
		String inputId = null;
		String inputPass = null;

		writeToCT.write("아이디 : " + "\n"); // 클라이언트에 보낼 데이터
		writeToCT.flush();
		inputId = readFromCT.readLine().trim(); // 클라이언트에게 받은 데이터를 inputId 변수에 저장
		writeToCT.write("패스워드 : " + "\n");
		writeToCT.flush();
		inputPass = readFromCT.readLine().trim(); // 클라이언트에게 받은 데이터를 inputPass 변수에 저장

		FileReader fileRead = new FileReader("DiarySource/account.txt"); // 계정목록를 저장한 텍스트파일을 불러옴
		BufferedReader buffRead = new BufferedReader(fileRead);

		String str;
		String[] strArray;
		boolean idFlag = false;
		while ((str = buffRead.readLine()) != null) { // 텍스트파일을 처음부터 끝까지 읽는다, null이면 종료
			strArray = str.split(":"); // 해당 라인을 ":"을 기준으로 분할해서 배열에 넣는다
			// 텍스트파일 중 ":"의 왼쪽에 "id"가 있는 줄을 발견하면 클라이언트가 보낸 데이터를 저장한 inputId와 같은 문자열이 있는지 판단
			if (strArray[0].equals("id") && strArray[1].equals(inputId)) {
				// id목록에서 inputId와 같은 문자열을 찾으면 패스워드를 같은 방식으로 검색하고 판단
				str = buffRead.readLine();
				strArray = str.split(":");
				if (strArray[0].equals("pass") && strArray[1].equals(inputPass)) {
					// inputId와 inputPass의 쌍이 모두 일치하면 해당 아이디와 패스워드로 로그인을 허가
					idFlag = true;
				}
				break;
			}
		}
		String[] readAccount = new String[6];
		if (idFlag) { // 로그인을 허가하면 해당 아이디와 비밀번호를 가진 계정의 정보를 문자열배열에 저장한다
			readAccount[0] = inputId;
			readAccount[1] = inputPass;
			readAccount[2] = buffRead.readLine().split(":")[1];
			readAccount[3] = buffRead.readLine().split(":")[1];
			readAccount[4] = buffRead.readLine().split(":")[1];
			readAccount[5] = buffRead.readLine().split(":")[1];

			System.out.println(Arrays.toString(readAccount));

		} else { // 로그인 불허시 나오는 내용
			writeToCT.write("아이디 혹은 비밀번호가 잘못되었습니다" + "::" + "(진행:엔터)" + "\n");
			writeToCT.flush();
			readFromCT.readLine();
			return null; // 아무것도 반환하지 않고 끝난다
		}
		buffRead.close();
		fileRead.close(); // 계정정보를 저장한 텍스트파일을 읽는 객체를 종료한다
		// 계정정보가 담긴 문자열로 계정객체를 하나 만들어서 반환한다
		return new Account(readAccount[0], readAccount[1], readAccount[2], readAccount[3], readAccount[4],
				readAccount[5]);
	}

	private static void logWrite(String accountId, int selectMenu) throws Exception {
		// 클라이언트의 행동을 기록한다
		Date date = new Date();
		String[] logArray = new String[2];

		FileWriter fileWrite = new FileWriter("DiarySource/log.txt", true);
		SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
		String strDate = formatter.format(date); // 현재시간을 저장한다
		logArray[0] = strDate;
		logArray[1] = accountId;

		for (int i = 0; i < logArray.length; i++) {
			fileWrite.write(logArray[i] + "\t");
		}

		String state = null;

		switch (selectMenu) {
		case 0:
			state = "logOut"; // 로그아웃
			break;
		case 1:
			state = "logIn"; // 로그인
			break;
		case 2:
			state = "write"; // 글쓰기
			break;
		case 3:
			state = "readme"; // 내글보기
			break;
		case 4:
			state = "readother"; // 남글보기
			break;
		}
		fileWrite.write(state + "\n");
		fileWrite.close();
	}
}