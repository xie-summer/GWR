package com.gewara.web.action.common;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.untrans.GewaPicService;
import com.gewara.untrans.GewaPicService.StreamWriter;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.web.support.ResponseStreamWriter;
@Controller
public class GetFlashController{
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	public void setGewaPicService(GewaPicService gewaPicService) {
		this.gewaPicService = gewaPicService;
	}
	private static final Map<String, String> contentTypeMap = new CaseInsensitiveMap();
	static{
		contentTypeMap.put("apk", "application/vnd.android.package-archive");
		contentTypeMap.put("ipa", "application/binary");
		contentTypeMap.put("zip", "application/zip");
	}
	@RequestMapping("/getUpFile.dhtml")
	public void getUpFile(HttpServletRequest request, String n, HttpServletResponse response) throws Exception{
		String extname = StringUtil.getFilenameExtension(n);
		String contentType = contentTypeMap.get(extname);
		if(StringUtils.isBlank(contentType)){
			response.sendError(404);//不支持！！！
		}
		getUpFile(request, n, contentType, response);
	}
	@RequestMapping("/getFlash.dhtml")
	public void getFlash(HttpServletRequest request, String n, HttpServletResponse response) throws Exception{
		String contentType = "application/x-shockwave-flash";
		getUpFile(request, n, contentType, response);
	}
	private void getUpFile(HttpServletRequest request, String n, String contentType, HttpServletResponse response) throws Exception{
		long modifySince = request.getDateHeader("If-Modified-Since");
		long cur = System.currentTimeMillis();
		if(modifySince > cur - DateUtil.m_day*365 && modifySince < cur){//3小时之前就生成的图片，不看更新
			response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}
		long lastModify = 0L;
		
		String etag = n;
		StreamWriter rwp = new ResponseStreamWriter(response, etag, contentType);
		lastModify = gewaPicService.getFileFromRemote(rwp, n);
		if(lastModify==0) response.sendError(404);
	}
}
