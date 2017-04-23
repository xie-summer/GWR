package com.gewara.web.action.inner.mobile.subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.model.user.Member;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.subject.DoubleElevenService;
import com.gewara.web.action.inner.OpenApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;

@Controller
public class OpenapiMobileDoubleElevenController extends BaseOpenApiController {
	@Autowired
	@Qualifier("doubleElevenService")
	private DoubleElevenService doubleElevenService;
	
	
	@RequestMapping("/openapi/mobile/subject/doubleEleven/todayWinnerCount.xhtml")
	public String getTodayWinnerCount(String tag, ModelMap model) {
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if (member == null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "Äú»¹Î´µÇÂ¼£¡");
		}
		Integer todayCount = doubleElevenService.getTodayWinnerCount(member.getId(), tag);
		return getSingleResultXmlView(model, todayCount + "");
	}

	// ³é½±
	@RequestMapping("/openapi/mobile/subject/doubleEleven/drawClick.xhtml")
	public String drawClick(String tag, Integer dayCount, ModelMap model) {
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		Member member = auth.getMember();
		if (member == null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "Äú»¹Î´µÇÂ¼£¡");
		}
		
		ErrorCode<String> drawClickResult = doubleElevenService.drawClick(member.getId(), tag, auth.getRemoteIp(), dayCount);
		if (drawClickResult.isSuccess()) {
			return getSingleResultXmlView(model, drawClickResult.getRetval());
		} else {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, drawClickResult.getMsg());
		}
	}

	// µÃµ½³é½±Ê±¼ä
	@RequestMapping("/openapi/mobile/subject/doubleEleven/getClickTime.xhtml")
	public String getClickTime(String tag, ModelMap model) {
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if (member == null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "Äú»¹Î´µÇÂ¼£¡");
		}
		ErrorCode<String> getClickTimeResult = doubleElevenService.getClickTime(member.getId(), tag);
		if (getClickTimeResult.isSuccess()) {
			return getSingleResultXmlView(model, getClickTimeResult.getRetval());
		} else {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, getClickTimeResult.getMsg());
		}
	}

	// µÃµ½³é½±´ÎÊý
	@RequestMapping("/openapi/mobile/subject/doubleEleven/getClickCount.xhtml")
	public String getClickCount(String tag, ModelMap model) {
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if (member == null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "Äú»¹Î´µÇÂ¼£¡");
		}
		ErrorCode<String> getClickCountResult = doubleElevenService.getClickCount(member.getId(), tag);
		if (getClickCountResult.isSuccess()) {
			return getSingleResultXmlView(model, getClickCountResult.getRetval());
		} else {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, getClickCountResult.getMsg());
		}
	}

	// ±£´æ·ÖÏíÎ¢²©
	@RequestMapping("/openapi/mobile/subject/doubleEleven/saveShareWeibo.xhtml")
	public String saveShareWeibo(String tag, String source, ModelMap model) {
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if (member == null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "Äú»¹Î´µÇÂ¼£¡");
		}
		ErrorCode<String> saveShareWeiboCode = doubleElevenService.saveShareWeibo(member.getId(), tag, source);
		if (saveShareWeiboCode.isSuccess()) {
			return getSuccessXmlView(model);
		} else {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, saveShareWeiboCode.getMsg());
		}
	}
}
