package com.gewara.web.action.subject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.sys.MongoData;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.service.OperationService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ObjectId;
import com.gewara.web.action.AnnotationController;
@Controller
public class OneNineFourTwoProxyController extends AnnotationController{
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	@RequestMapping("/subject/proxy/onft/onftSupport.xhtml")
	public String supportPicture(Long memberid, String id, ModelMap model){
		Member member = daoService.getObject(Member.class, memberid);
		if(member == null) return showJsonSuccess(model, "请先登录。");
		if(id == null) return showJsonSuccess(model, "参数错误。");
		boolean allow = operationService.updateOperation("onft" + member.getId(), OperationService.HALF_MINUTE, 1);
		if(!allow) return showJsonSuccess(model, "你操作过于频繁，请稍后再试！");
		Map paraMap = new HashMap();
		paraMap.put(MongoData.ACTION_TYPE, "onft");
		paraMap.put(MongoData.ACTION_RELATEDID, id);
		Map map = mongoService.findOne(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, paraMap);
		paraMap.put(MongoData.ACTION_MEMBERID, member.getId());
		int count = mongoService.getCount(MongoData.NS_ACTIVITY_COMMON_MEMBER, paraMap);
		if(count >= 1) return showJsonSuccess(model, "每个影片只能投一次");
		Map memberMap = new HashMap();
		memberMap.put(MongoData.SYSTEM_ID, ObjectId.uuid());
		memberMap.put(MongoData.ACTION_TYPE, "onft");
		memberMap.put(MongoData.ACTION_RELATEDID, id);
		memberMap.put(MongoData.ACTION_ADDTIME, System.currentTimeMillis());
		memberMap.put(MongoData.ACTION_MEMBERID, member.getId());
		memberMap.put(MongoData.ACTION_MEMBERNAME, member.getNickname());
		mongoService.saveOrUpdateMap(memberMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
		if(map == null){
			map = new HashMap();
			map.put(MongoData.SYSTEM_ID, ObjectId.uuid());
			map.put(MongoData.ACTION_TYPE, "onft");
			map.put(MongoData.ACTION_RELATEDID, id);
			map.put(MongoData.ACTION_ADDTIME, System.currentTimeMillis());
			map.put(MongoData.ACTION_SUPPORT, 1);
			mongoService.addMap(map, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_PUBLIC_CINEMA);
		}else{
			map.put(MongoData.ACTION_ADDTIME, System.currentTimeMillis());
			map.put(MongoData.ACTION_SUPPORT, Integer.parseInt(map.get("support")+"") + 1);
			mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_PUBLIC_CINEMA);
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/subject/proxy/onft/getMovie.xhtml")
	public String getOscar(String id, ModelMap model){
		if(id == null) return showJsonSuccess(model, "参数错误。");
		Map paraMap = new HashMap();
		paraMap.put(MongoData.ACTION_TYPE, "onft");
		paraMap.put(MongoData.ACTION_RELATEDID, id);
		Map oscarMap = mongoService.findOne(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, paraMap);
		if(oscarMap != null)return showJsonSuccess(model,JsonUtils.writeObjectToJson(oscarMap));
		return showJsonSuccess(model);
	}
	//获取排片信息
	@RequestMapping("/subject/proxy/onft/getOpenplayInfo.xhtml")
	public String getOpenplayInfo(Timestamp timeTo,String citycode,Long cinemaId, Long movieId, ModelMap model){
		Timestamp timeFrom = DateUtil.getCurFullTimestamp();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		List<OpenPlayItem> list = openPlayService.getOpiList(citycode, cinemaId, movieId, timeFrom, timeTo, true);
		for (OpenPlayItem opi: list) {
			if(StringUtils.containsIgnoreCase(opi.getEdition(), "IMAX")){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("mpid", opi.getMpid());
				map.put("gewaprice", opi.getGewaprice());
				map.put("costprice", opi.getCostprice());
				map.put("playtime", opi.getPlaytime());
				result.add(map);
			}
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(result));
	}
}
