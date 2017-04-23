package com.gewara.web.action.common;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.json.MobileUpGrade;
import com.gewara.untrans.GewaPicService;
import com.gewara.untrans.GewaPicService.StreamWriter;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.web.support.ResponseStreamWriter;

/**
 * 手机客户端应用下载路由
 * 
 * @author taiqichao
 * 
 */
@Controller
public class MobileAppDowloadController extends BaseApiController {

	@Autowired
	@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;

	public void setGewaPicService(GewaPicService gewaPicService) {
		this.gewaPicService = gewaPicService;
	}

	@RequestMapping("/mobile/appdowload.xhtml")
	public void appDowload(String appid, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (StringUtils.isBlank(appid)) {
			response.sendError(404);
			return;
		}
		MobileUpGrade upgrade = nosqlService.getLastMobileUpGradeById(appid);
		
		if (upgrade != null) {
			String updateUrl = upgrade.getUpgradeUrl();
			String fileName = updateUrl.replaceAll(".*upload", "upload");
			String extName = StringUtil.getFilenameExtension(fileName);
			String contentType = "application/octet-stream";
			long modifySince = request.getDateHeader("If-Modified-Since");
			long cur = System.currentTimeMillis();
			Long modifytime = upgrade.getModifytime();
			dbLogger.warn("appid=" + appid + ", modifySince=" + modifySince);
			if(modifytime!=null){
				Long diff = modifytime - modifySince;
				dbLogger.warn("cache appid=" + appid + ", modifySince=" + modifySince + ", diff=" + diff);
			}
			if ((modifySince > cur - DateUtil.m_day * 365) && modifySince < cur) {
				dbLogger.warn("cache appid=" + appid + ", modifySince=" + modifySince + ", modifytime=" + modifytime);
				response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
			long lastModify = 0L;
			String etag = fileName;
			String versionName=upgrade.getDownloadName();
			if(StringUtils.isBlank(versionName)){
				versionName="Gewara_mobile_app";
			}
			StreamWriter rwp = new ResponseStreamWriter(response, etag,contentType);
			response.addHeader("Content-Disposition", "attachment;filename="+ versionName + "." + extName);
			lastModify = gewaPicService.getFileFromRemote(rwp, fileName);
			if (lastModify == 0) {
				response.sendError(404);
				return;
			}
		} else {
			response.sendError(404);
		}
	}

}
