package test;


import org.apache.commons.codec.binary.Hex;
import org.apache.hadoop.hbase.util.Bytes;

public class TestObjectId {
	public static void main(String[] args) throws Exception{
		Long cur = System.currentTimeMillis();
		System.out.println("yyyy:" + cur);
		String hex = new String(Hex.encodeHex(Bytes.toBytes(cur)));
		System.out.println("aaaa:" + hex);
		byte[] out = Hex.decodeHex(hex.toCharArray());
		System.out.println("xxxx:" + Hex.encodeHexString(out));
	}
}
