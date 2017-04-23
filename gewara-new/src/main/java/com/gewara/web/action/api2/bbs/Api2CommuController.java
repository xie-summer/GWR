package com.gewara.web.action.api2.bbs;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.gewara.web.action.api.BaseApiController;

/**
 * 圈子API
 * 
 * @author taiqichao
 * 
 */
@Controller
public class Api2CommuController extends BaseApiController {

	@Autowired
	@Qualifier("commuService")
	private CommuService commuService;

	/**
	 * 圈子列表
	 * 
	 * @param tag
	 *            类型
	 * @param relatedid
	 *            关联对象
	 * @param from
	 *            当前页码
	 * @param maxnum
	 *            页大小
	 * @param citycode
	 *            城市代码
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/commu/commuList.xhtml")
	public String commuList(
			@RequestParam("tag") String tag,
			Long relatedid,
			@RequestParam(defaultValue = "0", required = false, value = "from") Integer from,
			@RequestParam(defaultValue = "20", required = false, value = "maxnum") Integer maxnum,
			@RequestParam(required = false, value = "citycode") String citycode,
			@RequestParam(required = false, value = "orderby") String orderby,
			ModelMap model) {
		if (maxnum > 50) {
			maxnum = 50;
		}
		List<Commu> commuList = commuService.getCommuBySearch(tag, citycode,
				relatedid, null, orderby, null, from, maxnum);
		model.put("commuList", commuList);
		return getXmlView(model, "api2/commu/commuList.vm");
	}
	
	/**
	 * 根据圈子ID查询圈子信息
	 * @param ids 圈子id集合
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/commu/commuListByIds.xhtml")
	public String commuListByIds(String ids, ModelMap model){
		if(ids == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		List<String> memberidList = Arrays.asList(StringUtils.split(ids, ","));
		List<Long> memberIds = new ArrayList<Long>();
		for (String string : memberidList) {
			memberIds.add(Long.parseLong(string));
		}
		List<Commu> commuLists = null;
		if(memberIds.size() > 0)
			commuLists = daoService.getObjectList(Commu.class, memberIds);
		model.put("commuLists", commuLists);
		return getXmlView(model, "api2/commu/commuLists.vm");
	}

}
