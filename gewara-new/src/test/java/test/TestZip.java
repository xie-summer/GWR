package test;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;

import com.gewara.util.StringUtil;
public class TestZip {
	public static void main(String[] args) throws Exception{
	}
	public static void fileHash() throws Exception{
		InputStream is = new FileInputStream("F:\\Lollipop\\icon.png");
		byte[] content = new byte[is.available()];
		is.read(content);
		String s = rsa(content);
		System.out.println(s);
	}
	public static String rsa(byte[] content) throws Exception{
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.reset();
		md.update(content);
		byte[] encodedPassword = md.digest();
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < encodedPassword.length; i++) {
			if ((encodedPassword[i] & 0xff) < 0x10) {
				buf.append("0");
			}
			buf.append(Long.toString(encodedPassword[i] & 0xff, 16));
		}
		return buf.toString();
	}

	public static void testZip() throws Exception{
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream("F:\\test.zip")));
		ZipEntry entry = new ZipEntry("test.txt");
		byte[] content = ("xxyyy中国人民" + StringUtils.repeat(StringUtil.getRandomString(1000), 100)).getBytes("utf-8");
		zos.putNextEntry(entry);
		zos.write(content);
		ZipEntry entry2 = new ZipEntry("mydocument/");
		zos.putNextEntry(entry2);
		ZipEntry entry3 = new ZipEntry("mydocument/xxxx.txt");
		zos.putNextEntry(entry3);
		zos.write(content);
		//entry2.
		zos.close();
	}
}
