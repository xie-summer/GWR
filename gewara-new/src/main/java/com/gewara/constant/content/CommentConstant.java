package com.gewara.constant.content;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public abstract class CommentConstant {

	public static final String WALA_EXP = "\\[(0[1-9]|0[1-4][0-9]|05[0-5])\\]"; 
	public static final Map<String, String> EXPMAP = new HashMap<String, String>();
	static{
		EXPMAP.put("[01]", "[º¦Ðß]");	EXPMAP.put("[02]", "[¿É°®]");	EXPMAP.put("[03]", "[ºÇºÇ]");	EXPMAP.put("[04]", "[»¨ÐÄ]");	EXPMAP.put("[05]", "[ÎûÎû]");
		EXPMAP.put("[06]", "[Ç×Ç×]");	EXPMAP.put("[07]", "[Ò®]");	EXPMAP.put("[08]", "[ÀÁµÃÀíÄã]");	EXPMAP.put("[09]", "[±§±§]");	EXPMAP.put("[010]", "[±ÉÊÓ]");
		EXPMAP.put("[011]", "[º¹]"); EXPMAP.put("[012]", "[ÔÎ]"); EXPMAP.put("[013]", "[Àá]"); EXPMAP.put("[014]", "[±¯ÉË]"); EXPMAP.put("[015]", "[±Õ×ì]");
		EXPMAP.put("[016]", "[³Ô¾ª]"); EXPMAP.put("[017]", "[¿á]"); EXPMAP.put("[018]", "[¹þ¹þ]"); EXPMAP.put("[019]", "[À§]"); EXPMAP.put("[020]", "[»¨]");
		EXPMAP.put("[021]", "[ÉËÐÄ]"); EXPMAP.put("[022]", "[ÓÊ¼þ]"); EXPMAP.put("[023]", "[µç»°]"); EXPMAP.put("[024]", "[¸É±­]"); EXPMAP.put("[025]", "[µòÐ»]"); 
		EXPMAP.put("[026]", "[ÎÕÊÖ]");	EXPMAP.put("[027]", "[good]");	EXPMAP.put("[028]", "[³ª¸è]");	EXPMAP.put("[029]", "[µÃÒâ]");	EXPMAP.put("[030]", "[ÒÉÎÊ]");
		EXPMAP.put("[031]", "[±ã±ã]");	EXPMAP.put("[032]", "[´ô]");	EXPMAP.put("[033]", "[ºÓÐ·]");	EXPMAP.put("[034]", "[‡å]");	EXPMAP.put("[035]", "[¿§·È]");
		EXPMAP.put("[036]", "[Àñ»¨]"); EXPMAP.put("[037]", "[ÀñÎï]"); EXPMAP.put("[038]", "[ÀºÇò]"); EXPMAP.put("[039]", "[÷¼÷Ã]"); EXPMAP.put("[040]", "[ÉÁµç]");
		EXPMAP.put("[041]", "[Èõ]"); EXPMAP.put("[042]", "[Å­]"); EXPMAP.put("[043]", "[Ë¥]"); EXPMAP.put("[044]", "[Ê§Íû]"); EXPMAP.put("[045]", "[Éú²¡]");
		EXPMAP.put("[046]", "[Ë¯¾õ]"); EXPMAP.put("[047]", "[Ì«Ñô]"); EXPMAP.put("[048]", "[ÏÂÓê]"); EXPMAP.put("[049]", "[ÐÄ]"); EXPMAP.put("[050]", "[ÐÇ]");
		EXPMAP.put("[051]", "[Ò©]"); EXPMAP.put("[052]", "[ÔÂÁÁ]"); EXPMAP.put("[053]", "[ÖÓ]"); EXPMAP.put("[054]", "[×¥¿ñ]"); EXPMAP.put("[055]", "[×ãÇò]");
	}
	
	public static String getExpMap(String key){
		if(StringUtils.isBlank(key)) return "";
		String str = EXPMAP.get(key);
		if(str == null) return key;
		return str;
	}
}
