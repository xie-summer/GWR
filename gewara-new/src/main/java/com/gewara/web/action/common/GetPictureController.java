package com.gewara.web.action.common;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.untrans.GewaPicService;
import com.gewara.untrans.GewaPicService.StreamWriter;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.support.ResponseStreamWriter;
@Controller
public class GetPictureController{
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	public void setGewaPicService(GewaPicService gewaPicService) {
		this.gewaPicService = gewaPicService;
	}
	@RequestMapping("/wap/image.dhtml")
	public void getWapImage(HttpServletRequest request, String n, String[] w, String[] h, String[] r, 
			HttpServletResponse response) throws Exception{
		getImage(request, n, w, h, r, response);
	}
	@RequestMapping("/image.dhtml")
	public void getImage(HttpServletRequest request, String n, String[] w, String[] h, String r[], 
			HttpServletResponse response) throws Exception{
		String referer = request.getHeader("referer");
		if(StringUtils.contains(referer, "gz.o.cn")){
			response.sendError(400);
			return;
		}
		String picname = n;//name
		if(!isPicture(picname)) {
			response.sendError(404, "not picture!");
			return;
		}
		long lastModify = 0L;
		String contentType = "image/jpeg";
		if(StringUtils.endsWithIgnoreCase(picname, ".gif")){
			contentType = "image/gif";
		}
		if(r==null || r.length==0) r = new String[]{""};
		boolean crop = StringUtils.equals(r[0], "c");
		long modifySince = request.getDateHeader("If-Modified-Since");
		
		if(w!=null && w.length>0 || h!=null && h.length>0){
			int width, height;
			try{
				width = Integer.parseInt(w[w.length-1]);
				height = Integer.parseInt(h[w.length-1]);
			}catch(Exception e){
				response.sendError(404);
				return;
			}
			if(width<=0 || height<=0){
				response.sendError(404);
				return;
			}
			lastModify = gewaPicService.exists(picname, width, height, crop);
			if(lastModify >0 && modifySince >= lastModify){//直接返回Http 304
				response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
			if(!gewaPicService.isValidSize(width, height)){
				dbLogger.error("BADPICTURE: w" + w + "h" + h + "/" + n + ", Referer:" + request.getHeader("Referer"));
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			String etag = n + "w=" + width + "h=" + height + "r=" + r[0];
			StreamWriter rwp = new ResponseStreamWriter(response, etag, contentType);

			lastModify = gewaPicService.getPicture(rwp, picname, width, height, crop);
		}else{
			lastModify = gewaPicService.exists(picname);
			if(lastModify >0){
				if(modifySince >= lastModify){//直接返回Http 304
					response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
					dbLogger.warn(n + new Date(request.getDateHeader("If-Modified-Since")));
					return;
				}
			}else{
				response.sendError(404);
				return;
			}
			StreamWriter rwp = new ResponseStreamWriter(response, n, contentType);
			lastModify = gewaPicService.getFileFromRemote(rwp, picname);
		}
		if(lastModify==0) {
			dbLogger.error("PICTURE_NOT_FOUND:" + WebUtils.getParamStr(request, false) + "," + WebUtils.getHeaderStr(request));
			response.sendError(404);
		}
	}
	private boolean isPicture(String picname){
		picname = StringUtils.lowerCase(picname);
		return StringUtils.endsWith(picname, ".jpg") || StringUtils.endsWith(picname, ".jpeg") 
				|| StringUtils.endsWith(picname, ".gif") || StringUtils.endsWith(picname, ".png");
	}
}
