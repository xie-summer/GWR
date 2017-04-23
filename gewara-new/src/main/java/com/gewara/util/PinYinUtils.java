package com.gewara.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

import org.apache.commons.lang.StringUtils;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public abstract class PinYinUtils {
	private static HanyuPinyinOutputFormat fmt = new HanyuPinyinOutputFormat();
	static{
		fmt.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
	}
	// 得到一个字的拼音.
	private static String getPinyin(char tt) {
		try {
			return PinyinHelper.toHanyuPinyinStringArray(tt, fmt)[0];
		} catch (Exception e) {
			return ""+tt; //原样返回
		}
	}
	public static String getPinyin(String target) {
		if(StringUtils.isBlank(target)) return "";
		int len = target.length();
		StringBuilder bf = new StringBuilder();
		for (int j = 0; j < len; j++) {
			bf.append(getPinyin(target.charAt(j)));
		}
		return bf.toString();
	}
	public static String getFirstSpell(String target){
		if(target==null){target="";}
		int len = target.length();
		StringBuilder bf = new StringBuilder();
		String py=null;
		for (int j = 0; j < len; j++) {
				py = getPinyin(target.charAt(j));
				if(StringUtils.isNotBlank(py)){
					if(StringUtils.isAlpha(py)) bf.append(py.charAt(0));
				}
		}
		return bf.toString();
	}
}
