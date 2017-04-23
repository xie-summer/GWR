package com.gewara.web.action.inner.common;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.ApiConstant;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.service.bbs.CommuService;
import com.gewara.util.BeanUtil;
import com.gewara.web.action.api.BaseApiController;

@Controller
public class ApiCommuController extends BaseApiController {

	@Autowired@Qualifier("commuService")
	private CommuService commuService;
	
	@RequestMapping("/inner/common/commu/commuListByTag.xhtml")
	public String commuList(String tag, Long relatedid, @RequestParam(required = false, value = "citycode") String citycode,
			@RequestParam(required = false, value = "orderby") String orderby, String asc,
			@RequestParam(defaultValue = "0", required = false, value = "from") Integer from,
			@RequestParam(defaultValue = "20", required = false, value = "maxnum") Integer maxnum, ModelMap model) {
		if(StringUtils.isBlank(tag) || StringUtils.isBlank(asc)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "参数错误！");
		if (maxnum > 50) {
			maxnum = 50;
		}
		List<Commu> commuList = commuService.getCommuBySearch(tag, citycode, relatedid, null, orderby, null, from, maxnum);
		model.put("commuList", commuList);
		return getXmlView(model, "inner/commu/commuList.vm");
	}
	
	/**
	 * 根据圈子ID查询圈子信息
	 * @param ids 圈子id集合
	 * @param model
	 * @return
	 */
	@RequestMapping("/inner/common/commu/getIdList.xhtml")
	public String commuListByIds(String ids, ModelMap model){
		if(StringUtils.isBlank(ids)) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		List<Long> memberIds = BeanUtil.getIdList(ids, ",");
		if(!memberIds.isEmpty()){
			List<Commu> commuLists =  daoService.getObjectList(Commu.class, memberIds);
			model.put("commuList", commuLists);
		}
		return getXmlView(model, "inner/commu/commuList.vm");
	}
}
