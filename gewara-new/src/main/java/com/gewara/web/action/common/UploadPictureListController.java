package com.gewara.web.action.common;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.gewara.Config;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.content.Picture;
import com.gewara.service.OperationService;
import com.gewara.untrans.GewaPicService;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-10-9上午08:59:19
 * 固定最多一次上传四个文件，file1, file2, file3, 加其他参数：tag, relatedid, uploadPath
 **********/
@Controller
public class UploadPictureListController extends AnnotationController {
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	public void setGewaPicService(GewaPicService gewaPicService) {
		this.gewaPicService = gewaPicService;
	}
	@RequestMapping(value="/common/uploadPictureList.xhtml", method=RequestMethod.POST)
	public String processResult(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, String paramchk, String tag, Long relatedid, String uploadPath, 
			String name1, String name2, String name3, String name4, String name5, String ukey,
			String desc1, String desc2, String desc3, String desc4, String desc5,
			String successField, String successFile, String realName, ModelMap model) throws Exception {
		String mycheck = StringUtil.md5(successFile + config.getString("uploadKey"));
		if(!mycheck.equals(paramchk)) return showError(model, "校验错误");
		boolean allow = operationService.updateOperation(ukey, 3600, 1);
		if(!allow) return showError(model, "不能后退或重复提交！");
		if(StringUtils.isBlank(successFile)) return showError(model, "请重新选择文件！");
		
		uploadPath = getUploadPath(uploadPath);
		String[] successFields = successField.split("@@");
		String[] successFiles = successFile.split("@@");
		GewaraUser user = loginService.getLogonGewaraUserBySessid(WebUtils.getRemoteIp(request), sessid);
		gewaPicService.moveRemoteTempListTo(user.getId(), tag, relatedid, uploadPath, successFiles);//将文件移动到正式文件夹
		Map<String, String> nameMap = new HashMap<String, String>();
		nameMap.put("file1", name1);
		nameMap.put("file2", name2);
		nameMap.put("file3", name3);
		nameMap.put("file4", name4);
		nameMap.put("file5", name5);
		Map<String, String> descMap = new HashMap<String, String>();
		descMap.put("file1", desc1);
		descMap.put("file2", desc2);
		descMap.put("file3", desc3);
		descMap.put("file4", desc4);
		descMap.put("file5", desc5);
		//3、处理上传的文件
		for(int i=0;i< successFields.length; i++){
			Picture picture = new Picture(tag, relatedid, uploadPath + successFiles[i], descMap.get(successFields[i]));
			picture.setMemberid(user.getId());
			picture.setMemberType(user.getUsertype());
			picture.setName(nameMap.get(successFields[i]));
			picture.setDescription(descMap.get(successFields[i]));
			daoService.saveObject(picture);
		}
		if(StringUtils.isNotBlank(request.getParameter("returnUrl"))){
			model.put("returnUrl", request.getParameter("returnUrl"));
		}
		return showMessage(model, ("成功上传：" + realName).replaceAll("@@", ","));
	}
	private String getUploadPath(String uploadPath) {
		if(StringUtils.isBlank(uploadPath)) throw new IllegalArgumentException("上传路径有错误！");
		if(!uploadPath.endsWith("/") && !uploadPath.endsWith("\\")) return uploadPath + "/" + DateUtil.format(new Date(), "yyyyMM") + "/";
		return uploadPath + DateUtil.format(new Date(), "yyyyMM") + "/";
	}
}
