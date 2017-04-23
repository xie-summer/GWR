package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.sql.Timestamp;

import org.apache.commons.codec.binary.Hex;

public class HBaseTest {
	public static void main(String[] args) throws Exception{
		while(true){
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String rowid = in.readLine();
			System.out.println(getTimeFromRowid(rowid));
		}
	}
	public static String getTimeFromRowid(String hexid) throws Exception{
		byte[] rowid = Hex.decodeHex(hexid.toCharArray());
		ByteBuffer bb =  ByteBuffer.wrap(rowid);
		int second = bb.getInt();
		long mill = second * 1000L;
		return new Timestamp(mill).toString();
	}
}
