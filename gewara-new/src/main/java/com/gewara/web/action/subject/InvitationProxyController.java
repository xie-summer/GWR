package com.gewara.web.action.subject;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.sys.MongoData;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

@Controller
public class InvitationProxyController extends AnnotationController {
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService){
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("controllerService")
	protected ControllerService controllerService;
	public void setControllerService(ControllerService controllerService) {
		this.controllerService = controllerService;
	}
	@Autowired@Qualifier("drawActivityService")
	private DrawActivityService drawActivityService;
	public void setDrawActivityService(DrawActivityService drawActivityService) {
		this.drawActivityService = drawActivityService;
	}
	private String getTime(String tag){
		Map params = new HashMap();
		params.put("tag", tag);
		Map timeMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		if(timeMap == null) return "ready";
		Date curTimestamp = DateUtil.currentTime();
		Date startTimestamp = DateUtil.parseDate(timeMap.get("starttime").toString());
		Date endTimestamp = DateUtil.parseDate( timeMap.get("endtime").toString());
		if(curTimestamp.before(startTimestamp)) return "ready";
		if(curTimestamp.after(endTimestamp)) return "over";
		return "start";
	}
	
	@RequestMapping("/admin/newsubject/invitation.xhtml")
	public String invitation(ModelMap model){
		Map params = new HashMap();
		params.put(MongoData.ACTION_TAG, "invitation");
		Map whiteActivity = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		model.put("whiteActivity", whiteActivity);
		return "admin/newsubject/invitation.vm";
	}
	//剧院
	@RequestMapping("/subject/proxy/invitation/cinemaList.xhtml")
	public String getCinemaList(ModelMap model){
		Map param = new HashMap();
		param.put(MongoData.ACTION_TYPE, "invitation");
		param.put(MongoData.ACTION_TAG, "invitation");
		List<Map> cinemaMap = mongoService.find(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, param, MongoData.ACTION_SUPPORT, false, 0, 10);
		for(Map map : cinemaMap){
			Long id = Long.parseLong(map.get("relatedid").toString());
			Cinema cinema = daoService.getObject(Cinema.class, id);
			map.put("id", cinema.getId());
			map.put("name", cinema.getName());
			map.put("feature", cinema.getFeature());
			map.put("limg", cinema.getLimg());
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(cinemaMap));
	}
	//电影
	@RequestMapping("/subject/proxy/invitation/movieList.xhtml")
	public String getMovieList(ModelMap model){
		Map param = new HashMap();
		param.put(MongoData.ACTION_TYPE, "inMovie");
		param.put(MongoData.ACTION_TAG, "inMovie");
		List<Map> movieMap = mongoService.find(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, param,MongoData.ACTION_SUPPORT, false, 0, 5);
		for(Map map : movieMap){
			Long id = Long.parseLong(map.get("relatedid").toString());
			Movie movie = daoService.getObject(Movie.class, id);
			map.put("id", movie.getId());
			map.put("name", movie.getName());
			map.put("releasedate", movie.getReleasedate());
			map.put("highlight", movie.getHighlight());
			map.put("limg", movie.getLimg());
			map.put("content", movie.getContent());
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(movieMap));
	}
	//支持
	@RequestMapping("/ajax/subject/proxy/invitation/getSupport.xhtml")
	public String getSupport(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, String id, HttpServletRequest request,ModelMap model,String tag){
		if(!StringUtils.equals(getTime("invitation"), "start")) return showJsonError(model, "活动未开始或已结束！");
		Map params = new HashMap();
		params.put(MongoData.ACTION_TAG, "invitation");
		Map whiteActivity = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		Timestamp startTime = DateUtil.parseTimestamp(whiteActivity.get("starttime").toString());
		Timestamp endTime = DateUtil.parseTimestamp(whiteActivity.get("endtime").toString());
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError_NOT_LOGIN(model);
		if(id == null)return showJsonError_NOT_FOUND(model);
		if(StringUtils.equals(tag, "invitation")){
			Map paraMap = new HashMap();
			paraMap.put(MongoData.ACTION_MEMBERID, member.getId());
			paraMap.put(MongoData.ACTION_TAG, "invitation");
			paraMap.put(MongoData.ACTION_TYPE, "invitation");
			Map map = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_MEMBER, paraMap);
			Map map2 = mongoService.findOne(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, MongoData.SYSTEM_ID, id);
			if(map == null){
				map = new HashMap();
				map.put(MongoData.SYSTEM_ID, System.currentTimeMillis() + StringUtil.getRandomString(5));
				map.put(MongoData.ACTION_TYPE, "invitation");
				map.put(MongoData.ACTION_TAG, "invitation");
				map.put(MongoData.ACTION_ADDTIME, System.currentTimeMillis());
				map.put(MongoData.ACTION_MEMBERID, member.getId());
				map.put("clickCount", 1);
				if(Long.parseLong(map2.get("relatedid").toString())==39712846){
					int random = (int)((Math.random()*3)+1);
					map2.put(MongoData.ACTION_SUPPORT, new Integer(map2.get(MongoData.ACTION_SUPPORT)+"")+random);
				}else{
					map2.put(MongoData.ACTION_SUPPORT, new Integer(map2.get(MongoData.ACTION_SUPPORT)+"")+1);
					Map parMap = new HashMap();
					parMap.put(MongoData.ACTION_TYPE, "invitation");
					parMap.put(MongoData.ACTION_TAG, "invitation");
					parMap.put(MongoData.ACTION_RELATEDID, "39712846");
					Map nanAnMap = mongoService.findOne(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, parMap);
					int nanAnsupport = Integer.parseInt(nanAnMap.get(MongoData.ACTION_SUPPORT).toString());
					int support = Integer.parseInt(map2.get(MongoData.ACTION_SUPPORT).toString());
					if(support >= nanAnsupport){
						nanAnMap.put(MongoData.ACTION_SUPPORT, new Integer(support)+5);
						mongoService.saveOrUpdateMap(nanAnMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_PUBLIC_CINEMA);
					}
				}
				mongoService.addMap(map, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
				mongoService.saveOrUpdateMap(map2, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_PUBLIC_CINEMA);
			}else{
				int count = Integer.valueOf(map.get("clickCount")+"");
				Integer inviteCount = drawActivityService.getInviteMemberCount(member.getId(),null, false, startTime, endTime);
				if(count > inviteCount) return showJsonError(model, "支持次数已达上限！");
				map.put("clickCount", Integer.valueOf(map.get("clickCount")+"")+1);
				if(Integer.valueOf(map2.get("relatedid").toString())==39712846){
					int random =(int)((Math.random()*3)+1);
					map2.put(MongoData.ACTION_SUPPORT, new Integer(map2.get(MongoData.ACTION_SUPPORT)+"")+random);
				}else{
					map2.put(MongoData.ACTION_SUPPORT, new Integer(map2.get(MongoData.ACTION_SUPPORT)+"")+1);
					Map parMap = new HashMap();
					parMap.put(MongoData.ACTION_TYPE, "invitation");
					parMap.put(MongoData.ACTION_TAG, "invitation");
					parMap.put(MongoData.ACTION_RELATEDID, "39712846");
					Map nanAnMap = mongoService.findOne(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, parMap);
					int nanAnsupport = Integer.parseInt(nanAnMap.get(MongoData.ACTION_SUPPORT).toString());
					int support = Integer.parseInt(map2.get(MongoData.ACTION_SUPPORT).toString());
					if(support >= nanAnsupport){
						nanAnMap.put(MongoData.ACTION_SUPPORT, new Integer(support)+5);
						mongoService.saveOrUpdateMap(nanAnMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_PUBLIC_CINEMA);
					}					
				}
				mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
				mongoService.saveOrUpdateMap(map2, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_PUBLIC_CINEMA);
			}
		}else if(StringUtils.equals(tag, "inMovie")){
			Map paraMap = new HashMap();
			paraMap.put(MongoData.ACTION_MEMBERID, member.getId());
			paraMap.put(MongoData.ACTION_TAG, "inMovie");
			paraMap.put(MongoData.ACTION_TYPE, "inMovie");
			Map map = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_MEMBER, paraMap);
			Map map2 = mongoService.findOne(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, MongoData.SYSTEM_ID, id);
			if(map == null){
				map = new HashMap();
				map.put(MongoData.SYSTEM_ID, System.currentTimeMillis() + StringUtil.getRandomString(5));
				map.put(MongoData.ACTION_TYPE, "inMovie");
				map.put(MongoData.ACTION_TAG, "inMovie");
				map.put(MongoData.ACTION_ADDTIME, System.currentTimeMillis());
				map.put(MongoData.ACTION_MEMBERID, member.getId());
				map.put("clickCount", 1);
				map2.put(MongoData.ACTION_SUPPORT, new Integer(map2.get(MongoData.ACTION_SUPPORT)+"")+1);
				mongoService.addMap(map, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
				mongoService.saveOrUpdateMap(map2, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_PUBLIC_CINEMA);
			}else{
				int count = Integer.valueOf(map.get("clickCount")+"");
				Integer inviteCount = drawActivityService.getInviteMemberCount(member.getId(),null, false, startTime, endTime);
				if(count > inviteCount) return showJsonError(model, "支持次数已达上限！");
				map.put("clickCount", Integer.valueOf(map.get("clickCount")+"")+1);
				map2.put(MongoData.ACTION_SUPPORT, new Integer(map2.get(MongoData.ACTION_SUPPORT)+"")+1);
				mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
				mongoService.saveOrUpdateMap(map2, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_PUBLIC_CINEMA);
			}
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/ajax/subject/proxy/invitation/getMember.xhtml")
	public String getMember(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request,ModelMap model){
		Map result = new HashMap();
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		result.put("memberId", member.getId());
		result.put("encodes", StringUtil.md5WithKey("" + member.getId()));
		return showJsonSuccess(model,result);
	}
}
