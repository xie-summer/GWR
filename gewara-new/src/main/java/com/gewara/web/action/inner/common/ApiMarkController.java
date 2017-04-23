package com.gewara.web.action.inner.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.model.user.Member;
import com.gewara.util.WebUtils;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.web.component.ShLoginService;

@Controller
public class ApiMarkController extends BaseApiController {
	public static final List<String> MARK_LIST = Arrays.asList("screenmark",
			"airqualitymark", "attitudemark", "feelingmark", "environmentmark",
			"audiomark", "programmark", "generalmark", "guidemark",
			"storymark", "songmark", "spacemark", "promark", "interactivemark",
			"pricemark", "servicemark", "musicmark", "fieldmark",
			"performmark", "foodmark");
	@Autowired@Qualifier("loginService")
	private ShLoginService loginService;
	
	@RequestMapping("/inner/common/member/addMark.xhtml")
	public String addMark(String tag, Long relatedid, String marks, String sessid, String ip, ModelMap model) {
		if(StringUtils.isBlank(tag) || relatedid == null || StringUtils.isBlank(marks) || StringUtils.isBlank(sessid) || StringUtils.isBlank(ip)){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "参数错误！");
		}
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null) return getErrorXmlView(model, ApiConstant.CODE_MEMBER_NOT_EXISTS,"用户不存在！");
		Map<String, String> markMap = WebUtils.parseQueryStr(marks, "utf-8");
		if (StringUtils.isBlank(marks)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "请评分！");
		}
		if(markMap.get("generalmark")==null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "请填写总评分！");
		}
		Map<String, Integer> memberMarkMap = new HashMap<String, Integer>();
		for (Map.Entry<String, String> entry : markMap.entrySet()) {
			String markname = entry.getKey();
			String markvalue = entry.getValue();
			if (MARK_LIST.contains(markname) && markvalue.matches("\\d+")) {
				int markValue = Integer.valueOf(markvalue);
				if (markValue > 0 && markValue <= 10)
					memberMarkMap.put(markname, Integer.valueOf(markvalue));
			}
		}
		markService.saveOrUpdateMemberMarkMap(tag, relatedid, member,memberMarkMap);
		return getSingleResultXmlView(model, "success");
	}
	
	
	@RequestMapping("/inner/common/list/markByGroupValue.xhtml")
	public String getMarkValue(String tag, Long relatedid, String markname, ModelMap model){
		if(StringUtils.isBlank(tag) || relatedid == null || StringUtils.isBlank(markname)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "参数错误！");
		List<Map> markList = markService.getMarkDetail(tag, relatedid, markname);
		model.put("markList", markList);
		return getXmlView(model, "inner/common/markList.vm");
	}
}
