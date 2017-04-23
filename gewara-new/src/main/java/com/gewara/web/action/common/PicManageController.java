package com.gewara.web.action.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.support.ErrorCode;
import com.gewara.untrans.GewaPicService;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.StringUtil;
import com.gewara.web.action.AnnotationController;

@Controller
public class PicManageController extends AnnotationController{
	@Autowired
	private GewaPicService picService;
	@RequestMapping("/compress.xhtml")
	public String compressStep1(String action, String picname, ModelMap model){
		model.put("picname", picname);
		if(StringUtils.equals(action, "split") && StringUtils.isNotBlank(picname)){//’˚¿ÌÕº∆¨
			List<String> picList = StringUtil.findByRegex(picname, "http://img[\\d]*\\.gewara\\.(com|cn)/[/]*([cs]*w\\d+h\\d+/)*(images|userfiles)/.*\\.jp[e]*g", true);
			List<String> cmdList = new ArrayList<String>();
			for(String pic: picList){
				cmdList.add("bin/squidclient -p80 -m PURGE " + pic);
			}
			model.put("cmd", StringUtils.join(cmdList, "\n"));
			model.put("picname", StringUtils.join(picList, "\n"));
		}else if(StringUtils.equals(action, "compress")){
			String[] picList = StringUtils.split(picname, "\n");
			List<String> msgList = new ArrayList<String>();
			List<String> cmdList = new ArrayList<String>();
			for(String pic: picList){
				pic = pic.trim();
				if(StringUtil.regMatch(pic, "http://img[\\d]*\\.gewara\\.(com|cn)/[/]*([cs]*w\\d+h\\d+/)*(images|userfiles)/.*\\.jp[e]*g", true)){
					int idx = pic.indexOf("/images/");
					if(idx<0) idx = pic.indexOf("/userfiles/");
					String path = pic.substring(idx + 1).trim(); 
					if(StringUtil.regMatch(pic, "http://img[\\d]*\\.gewara\\.(com|cn)/[/]*[cs]*w\\d+h\\d+/", true)){//”–≥ﬂ¥Á
						idx = pic.indexOf("gewara.com/");
						path = pic.substring(idx + "gewara.com/".length()).trim();
						ErrorCode result = picService.removeScalePic(path);
						msgList.add("remove:" + picname + ", " + result.getMsg());
						cmdList.add("bin/squidclient -p80 -m PURGE " + pic);
						dbLogger.warn("delete img:" + path);
					}else{
						try {
							picService.compressPic(path);
							msgList.add("compress:" + path);
							cmdList.add("bin/squidclient -p80 -m PURGE " + pic);
						} catch (IOException e) {
							msgList.add("compress error:" + path + ", " + e.getMessage());
							dbLogger.warn(StringUtil.getExceptionTrace(e, 5));
						}
						dbLogger.warn("compress img:" + path);
					}
				}
			}
			model.put("cmd", StringUtils.join(cmdList, "\n"));
			model.put("msg", StringUtils.join(msgList, "\n"));
		}else if(StringUtils.equals("clear", action)){
			String[] picList = StringUtils.split(picname, "\n");
			List<String> urls = new ArrayList<String>();
			for(String pic: picList){
				if(StringUtil.regMatch(pic, "http://img[\\d]*\\.gewara\\.(com|cn)/[/]*([cs]*w\\d+h\\d+/)*(images|userfiles)/.*\\.jp[e]*g", true)){
					urls.add(pic.trim());
				}
			}
			String result = clean(urls);
			model.put("msg", result);
		}
		return "compressStep1.vm";
	}
	public static String clean(List<String> urls){
		String url = "http://ccms.chinacache.com/index.jsp";
		Map<String, String> params = new HashMap<String, String>();
		params.put("user", "gewara");
		params.put("pswd", "9qqxP8Kl");
		params.put("ok", "ok");
		params.put("urls", StringUtils.join(urls, "\r\n"));
		HttpResult result = HttpUtils.getUrlAsString(url, params);
		return "success:" + result.isSuccess() + ", response:" + result.getResponse();
	}
}
