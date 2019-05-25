package com.aft.crawl.socket;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

import com.aft.utils.MyDefaultProp;
import com.aft.utils.date.MyDateFormatUtils;

public class SocketClient {
	
	private static Socket socket = null;
	
	public SocketClient() throws Exception {
		socket = new Socket("192.168.8.202", 55555);
	}
	
	public static void main(String args[]) throws Exception {
		new SocketClient();
		SocketClient.send();
		SocketClient.receive();
	}
	
	public static void send() throws Exception {
		PrintWriter os = new PrintWriter(socket.getOutputStream());
		os.write("xxxx电费单辅导费" + MyDefaultProp.getSocketEndStr());
		os.flush();
		System.out.println(MyDateFormatUtils.SDF_YYYYMMDDHHMMSSSSS().format(new Date()) + ", Client send msg!");
	}
	
	public static void receive() throws Exception {
		InputStreamReader isr = new InputStreamReader(socket.getInputStream(), "GBK");
		char[] temps = new char[20480];
		StringBuilder sbMsg = new StringBuilder();
		int len = 0;
		while((len = isr.read(temps, 0, temps.length)) != -1) {
			sbMsg.append(temps, 0, len);
		}
		System.out.println(MyDateFormatUtils.SDF_YYYYMMDDHHMMSSSSS().format(new Date()) + ", Client receive msg:" + sbMsg);
	}
}