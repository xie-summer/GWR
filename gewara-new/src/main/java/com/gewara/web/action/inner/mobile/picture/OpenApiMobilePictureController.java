package com.gewara.web.action.inner.mobile.picture;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.model.content.Picture;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileController;

@Controller
public class OpenApiMobilePictureController extends BaseOpenApiMobileController{
	@RequestMapping("/openapi/mobile/picture/pictureList.xhtml")
	public String pictureList(String tag,Long relatedid, Integer from, Integer maxnum, ModelMap model, HttpServletRequest request){
		if(StringUtils.isBlank(tag)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "tag不能为空！");
		if(relatedid == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "relateid不能为空！");
		if(from==null) from = 0;
		if(maxnum > 100) maxnum = 100;
		List<Picture> pictureList = pictureService.getPictureListByRelatedid(tag, relatedid, from, maxnum);
		getPictureListMap(pictureList, model, request);
		return getOpenApiXmlList(model);
	}
}
