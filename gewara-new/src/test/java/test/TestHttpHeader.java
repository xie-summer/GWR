package test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class TestHttpHeader {
	public static void main(String[] args) throws Exception {
		String ip = "114.80.171.243";
		String host = "www.gewara.com";
		int port = 82;
		String page = "/mobile/appdowload.xhtml?appid=1375234343556jhbpT";
	    Socket s = new Socket(ip, port);  
	    InputStream ins = s.getInputStream();  
	    OutputStream os = s.getOutputStream();  
	    os.write(("GET " + page + " HTTP/1.1\r\n").getBytes());  
	    os.write(("Host:" + host + "\r\n").getBytes());  
	    os.write("\r\n\r\n".getBytes());//这个也必不可少，http协议规定的  
	    os.flush();
	    BufferedReader br = new BufferedReader(new InputStreamReader(ins));  
	    String line = null;  
	    line = br.readLine();
	    int i=0;
	    while(line != null && i< 30){  
	        System.out.println(line);  
	        line = br.readLine();
	        i++;
	    }  
	    ins.close();  
	}
}
