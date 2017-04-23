package test;

import com.gewara.util.ElecCardCoder;
import com.gewara.util.StringUtil;


public class TTT {
	public static void main(String[] args) {
		String pass="4lfq9k5yxwhy";
		match(pass);
	}
	public static void match(String passwd){
		String pass = passwd.toUpperCase();
		String md5pass = StringUtil.md5(pass+pass);
		System.out.println(md5pass);
		System.out.println(ElecCardCoder.encode(md5pass));
	}
}
