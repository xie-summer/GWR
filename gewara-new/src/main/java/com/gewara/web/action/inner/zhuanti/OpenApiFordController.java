package com.gewara.web.action.inner.zhuanti;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.json.mobile.FordTestDrive;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.inner.mobile.BaseOpenApiController;

@Controller
public class OpenApiFordController extends BaseOpenApiController {
	/**
	 * 用户报名
	 */
	@RequestMapping("/openapi/mobile/ford/testdrive.xhtml")
	public String savePlayersInfo(FordTestDrive fordTestDrive, ModelMap model) {
		if (fordTestDrive == null || fordTestDrive.checkNotBlankValue()) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "缺少必要参数！");
		}
		if (!ValidateUtil.isMobile(fordTestDrive.getMobileNo())) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "你的手机号码格式不正确！");
		}
		String id = fordTestDrive.getSource() + fordTestDrive.getMobileNo() + fordTestDrive.getDriveName();
		FordTestDrive temp = mongoService.getObject(FordTestDrive.class, "id", id);
		if (temp != null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "你已报名，请不要重复提交！");
		}
		fordTestDrive.setId(id);
		fordTestDrive.setAddTime(System.currentTimeMillis());
		mongoService.saveOrUpdateObject(fordTestDrive, "id");
		return getSingleResultXmlView(model, "success");
	}
	
}
