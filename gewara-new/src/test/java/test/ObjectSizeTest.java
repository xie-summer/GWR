package test;

import java.sql.Timestamp;

import org.apache.hadoop.hbase.util.Bytes;

import com.gewara.web.util.LoginUtils;


public class ObjectSizeTest {
	public static void main(String[] args){
		System.out.println(new Timestamp(2147483647000L));
		System.out.println(Bytes.toStringBinary(Bytes.toBytes(130012125L)));
		System.out.println(Integer.MAX_VALUE);
		System.out.println(LoginUtils.getTraceId("39408083"));
	}
}
