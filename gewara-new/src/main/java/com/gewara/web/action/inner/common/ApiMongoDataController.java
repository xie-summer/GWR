package com.gewara.web.action.inner.common;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.service.bbs.BlogService;
import com.gewara.untrans.MemberCountService;
import com.gewara.web.action.api.BaseApiController;
@Controller
public class ApiMongoDataController extends BaseApiController{
	@Autowired@Qualifier("memberCountService")
	private MemberCountService memberCountService;
	@Autowired
	private BlogService blogService;
	@RequestMapping("/inner/mongo/getKeywords.xhtml")
	public String getFixedKeywords(ModelMap model, String keytype){
		if(StringUtils.equals(keytype, ConfigConstant.KEY_FIXEDKEYWORDS) || 
				StringUtils.equals(keytype, ConfigConstant.KEY_MANUKEYWORDS) || 
				StringUtils.equals(keytype, ConfigConstant.KEY_MEMBERKEYWORDS)){
			String keywords = (String)mongoService.getPrimitiveObject(keytype);
			return getSingleResultXmlView(model, keywords);
		}
		return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "¥ÌŒÛµƒ¿‡–Õ");
	}
	@RequestMapping("/inner/mongo/updateMemberCount.xhtml")
	public String updateMemberCount(ModelMap model, Long memberid, String key, int value, boolean isAdd){
		memberCountService.updateMemberCount(memberid, key, value, isAdd);
		return getSingleResultXmlView(model, "true");
	}
	@RequestMapping("/inner/mongo/isPlayMember.xhtml")
	public String isPlayMemberByTagAndId(ModelMap model, Long memberid, String tag, Long relatedid){
		boolean result = untransService.isPlayMemberByTagAndId(memberid, tag, relatedid);
		return getSingleResultXmlView(model, result+"");
	}
	@RequestMapping("/inner/mongo/getDiaryBody.xhtml")
	public String getDiayBody(ModelMap model, Long diaryid){
		String result = blogService.getDiaryBody(diaryid);
		return getSingleResultXmlView(model, result);
	}
}
