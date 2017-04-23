package com.gewara.web.action.subject.gewacup;

import java.util.ArrayList;
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

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.order.AddressConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.gewacup.ClubInfo;
import com.gewara.json.gewacup.MiddleTable;
import com.gewara.json.gewacup.Players;
import com.gewara.model.BaseObject;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.content.News;
import com.gewara.model.user.Member;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.CommuService;
import com.gewara.service.movie.FilmFestService;
import com.gewara.support.ServiceHelper;
import com.gewara.util.BindUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Controller
public class GewaCupController extends BaseGewaCupController {
	
	@Autowired@Qualifier("commuService")
	private CommuService commuService;
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	@Autowired@Qualifier("filmFestService")
	private FilmFestService filmFestService;
	
	@RequestMapping("/subject/gewacup/2012/index.xhtml")
	public String gewaIndex(ModelMap model){
		model.put("personalstatus", getTime("gewaCupPersonal", MongoData.GEWA_CUP_YEARS_2012));
		model.put("clubstatus", getTime("gewaCupCommu", MongoData.GEWA_CUP_YEARS_2012));
		return "subject/gewacup/2012/index.vm";
	}
	@RequestMapping("/subject/gewacup/2012/personalApply.xhtml")
	public String personalApply(HttpServletRequest request, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, String mid, ModelMap model){
		return personalApplyMethod(request, sessid, mid, "subject/gewacup/2012/personalmemu.vm", "/subject/gewacup/2012/personalApply.xhtml", MongoData.GEWA_CUP_YEARS_2012, model);
	}
	@RequestMapping("/subject/gewacup/2012/clubApply.xhtml")
	public String clubApply(HttpServletRequest request, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, Long commuid, String temp, ModelMap model){
		return clubApplyMethod(request, sessid, commuid, temp, MongoData.GEWA_CUP_YEARS_2012, "subject/gewacup/2012/clubmemu.vm", "/subject/gewacup/2012/clubApply.xhtml", model);
	}
	//保存俱乐部联系人信息
	@RequestMapping("/subject/gewacup/2012/saveClubInfo.xhtml")
	public String saveClubInfo(HttpServletRequest request, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, Long commuid, ModelMap model){
		return saveClubInfoMethod(request, sessid, commuid, MongoData.GEWA_CUP_YEARS_2012, model);
	}
	//保存参赛人员信息
	@RequestMapping("/subject/gewacup/2012/savePlayersInfo.xhtml")
	public String savePlayersInfo(HttpServletRequest request, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, Long clubInfoId, String mid, ModelMap model){
		return savePlayersInfoMethod(request, sessid, clubInfoId, mid, MongoData.GEWA_CUP_YEARS_2012, model);
	}
	//我的报名信息
	@RequestMapping("/subject/gewacup/2012/myPlayersInfo.xhtml")
	public String myPlayersInfo(HttpServletRequest request, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, ModelMap model){
		return myPlayersInfoMethod(request, sessid, MongoData.GEWA_CUP_YEARS_2012, "subject/gewacup/2012/clubstate.vm", "/subject/gewacup/2012/myPlayersInfo.xhtml", model);
	}
	//绑定订单
	@RequestMapping("/subject/gewacup/2012/bindGewaCupOrder.xhtml")
	public String bindGewaCupOrder(Long commuid, String cupid, Long orderid, ModelMap model){
		return bindGewaCupOrderMethod(commuid, cupid, orderid, MongoData.GEWA_CUP_YEARS_2012, model);
	}
	@RequestMapping("/subject/gewacup/2012/popup.xhtml")
	public String popup(HttpServletRequest request, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, String mid, Long commuid, ModelMap model){
		return popupMethod(request, sessid, mid, commuid, MongoData.GEWA_CUP_YEARS_2012, "subject/gewacup/2012/popup.vm", "/subject/gewacup/2012/popup.xhtml", model);
	}
	@RequestMapping("/subject/gewacup/2012/deletePlayers.xhtml")
	public String deletePlayers(String mid, ModelMap model){
		if(StringUtils.isBlank(mid)) return showJsonError(model,"参数错误！");
		if(deletePlayers(mid))return showJsonSuccess(model);
		else return showJsonError(model,"没有找到此选手信息！");
	}
	@RequestMapping("/subject/gewacup/2012/intro.xhtml")
	public String intro(){
		return "subject/gewacup/2012/intro.vm";
	}
	@RequestMapping("/subject/gewacup/2012/award.xhtml")
	public String award(){
		return "subject/gewacup/2012/award.vm";
	}
	@RequestMapping("/subject/gewacup/2012/instruction.xhtml")
	public String instruction(){
		return "subject/gewacup/2012/instruction.vm";
	}
	@RequestMapping("/subject/gewacup/2012/answer.xhtml")
	public String answer(ModelMap model){
		Map mainsubject = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, "ss1332925252076");
		if(mainsubject != null){
			String board = ""+mainsubject.get(MongoData.ACTION_BOARD);
			Long relatedid = (Long) mainsubject.get(MongoData.ACTION_RELATEDID);
			BaseObject object = (BaseObject)relateService.getRelatedObject(board, relatedid);
			if(object != null) model.put("headInfo", object);
		}
		return "subject/gewacup/2012/answer.vm";
	}
	@RequestMapping("/ajax/subject/gewacup/reportAnswer.xhtml")
	public String reportAnswer(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,String toName, String newslogo, String content, String realname, String phone,
			String type, HttpServletRequest request, ModelMap model) throws Exception {
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError_NOT_LOGIN(model);
		boolean allow = operationService.updateOperation("uploadPic" + member.getId(), OperationService.HALF_MINUTE, 1);
		if(!allow) return showJsonError(model, "你操作过于频繁，请稍后再试！");
		if(StringUtils.isBlank(toName)) return showJsonError(model, "被举报人姓名不能为空！");
		if(StringUtils.isBlank(content)) return showJsonError(model, "举报内容不能为空！");
		if(WebUtils.checkString(content)) return showJsonError(model, "举报内容含有非法字符！");
		if(StringUtils.isBlank(type)) type = MongoData.GEWA_CUP_YEARS_2012;
		Map map = new HashMap();
		map.put(MongoData.ACTION_ADDTIME, System.currentTimeMillis());
		map.put(MongoData.SYSTEM_ID, System.currentTimeMillis() + StringUtil.getRandomString(5));
		map.put(MongoData.GEWA_CUP_MEMBERID, member.getId());
		map.put(MongoData.ACTION_MEMBERNAME, member.getNickname());
		map.put(MongoData.ACTION_TYPE, type);
		map.put(MongoData.ACTION_TAG, MongoData.GEWA_CUP_ANSWER);
		map.put(MongoData.ACTION_TO_NAME, toName);
		map.put(MongoData.ACTION_BODY, content);
		map.put(MongoData.ACTION_FROM_NAME, realname);
		map.put(MongoData.FIELD_TELEPHONE, phone);
		map.put(MongoData.ACTION_PICTRUE_URL, newslogo);
		mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
		return showJsonSuccess(model);
	}
	//2013格瓦拉杯羽毛球赛
	@RequestMapping("/gewacup2013/index.xhtml")
	public String index(ModelMap model){
		model.put("personalstatus", getTime("gewaCupPersonal", MongoData.GEWA_CUP_YEARS_2013));
		model.put("clubstatus", getTime("gewaCupCommu", MongoData.GEWA_CUP_YEARS_2013));
		return "subject/gewacup/2013/index.vm";
	}
	@RequestMapping("/gewacup2013/personalApply.xhtml")
	public String personalApply2013(HttpServletRequest request, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, String mid, ModelMap model){
		return personalApplyMethod(request, sessid, mid, "subject/gewacup/2013/personalmemu.vm", "/gewacup2013/personalApply.xhtml", MongoData.GEWA_CUP_YEARS_2013, model);
	}
	@RequestMapping("/gewacup2013/clubApply.xhtml")
	public String clubApply2013(HttpServletRequest request, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, Long commuid, String temp, ModelMap model){
		return clubApplyMethod(request, sessid, commuid, temp, MongoData.GEWA_CUP_YEARS_2013, "subject/gewacup/2013/clubmemu.vm", "/gewacup2013/clubApply.xhtml", model);
	}
	//保存俱乐部联系人信息
	@RequestMapping("/gewacup2013/saveClubInfo.xhtml")
	public String saveClubInfo2013(HttpServletRequest request, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, Long commuid, ModelMap model){
		return saveClubInfoMethod(request, sessid, commuid, MongoData.GEWA_CUP_YEARS_2013, model);
	}
	//保存参赛人员信息
	@RequestMapping("/gewacup2013/savePlayersInfo.xhtml")
	public String savePlayersInfo2013(HttpServletRequest request, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, Long clubInfoId, String mid, ModelMap model){
		if(clubInfoId != null && StringUtils.isBlank(mid)){
			DBObject queryCondition = new BasicDBObject();
			DBObject relate2 = mongoService.queryBasicDBObject("yearstype", "=", MongoData.GEWA_CUP_YEARS_2013);
			DBObject relate3 = mongoService.queryBasicDBObject("clubInfoId", "=", clubInfoId);
			queryCondition.putAll(relate2);
			queryCondition.putAll(relate3);
			List<MiddleTable> midList = mongoService.getObjectList(MiddleTable.class, queryCondition);
			if(!midList.isEmpty() && midList.size() >= 8) return showJsonError(model, "每个俱乐部最多只能报8个项目！");
		}
		return savePlayersInfoMethod(request, sessid, clubInfoId, mid, MongoData.GEWA_CUP_YEARS_2013, model);
	}
	//我的报名信息
	@RequestMapping("/gewacup2013/myPlayersInfo.xhtml")
	public String myPlayersInfo2013(HttpServletRequest request, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, ModelMap model){
		return myPlayersInfoMethod(request, sessid, MongoData.GEWA_CUP_YEARS_2013, "subject/gewacup/2013/clubstate.vm", "/gewacup2013/myPlayersInfo.xhtml", model);
	}
	//绑定订单
	@RequestMapping("/gewacup2013/bindGewaCupOrder.xhtml")
	public String bindGewaCupOrder2013(Long commuid, String cupid, Long orderid, ModelMap model){
		return bindGewaCupOrderMethod(commuid, cupid, orderid, MongoData.GEWA_CUP_YEARS_2013, model);
	}
	@RequestMapping("/gewacup2013/popup.xhtml")
	public String popup2013(HttpServletRequest request, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, String mid, Long commuid, ModelMap model){
		return popupMethod(request, sessid, mid, commuid, MongoData.GEWA_CUP_YEARS_2013, "subject/gewacup/2013/popup.vm","/gewacup2013/popup.xhtml", model);
	}
	@RequestMapping("/gewacup2013/deletePlayers.xhtml")
	public String deletePlayers2013(String mid, ModelMap model){
		if(StringUtils.isBlank(mid)) return showJsonError(model,"参数错误！");
		if(deletePlayers(mid))return showJsonSuccess(model);
		else return showJsonError(model,"没有找到此选手信息！");
	}
	@RequestMapping("/gewacup2013/intro.xhtml")
	public String intro2013(){
		return "subject/gewacup/2013/intro.vm";
	}
	@RequestMapping("/gewacup2013/award.xhtml")
	public String award2013(){
		return "subject/gewacup/2013/award.vm";
	}
	@RequestMapping("/gewacup2013/instruction.xhtml")
	public String instruction2013(){
		return "subject/gewacup/2013/instruction.vm";
	}
	@RequestMapping("/gewacup2013/answer.xhtml")
	public String answer2013(){
		return "subject/gewacup/2013/answer.vm";
	}
	@RequestMapping("/gewacup2013/newsList.xhtml")
	public String newsList(ModelMap model){
		List<News> newsList = filmFestService.getFilmFestNewsList(AdminCityContant.CITYCODE_SH, MongoData.GEWA_CUP_YEARS_2013.toLowerCase(), null, null, 0, 15);
		model.put("newsList", newsList);
		return "subject/gewacup/2013/news.vm";
	}
	private String personalApplyMethod(HttpServletRequest request, String sessid, String mid, String url, String result, String yearsType, ModelMap model) {
		String clubstatus = getTime("gewaCupPersonal", yearsType);
		if(!StringUtils.equals(clubstatus,"game")) return forwardMessage(model, "此报名方式已经停止或未开始!");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return gotoLogin(result, request, model);
		if(StringUtils.isNotBlank(mid)){
			MiddleTable mt = mongoService.getObject(MiddleTable.class, "id", mid);
			if(mt == null)return forwardMessage(model,"此报名信息不存在！");
			Players player = mongoService.getObject(Players.class, "id", mt.getFromid());
			model.put("playerMap", player);
			String type = mt.getType();
			if(StringUtils.equals(type, MongoData.GEWA_CUP_BOY_DOUBLE) || StringUtils.equals(type, MongoData.GEWA_CUP_GIRL_DOUBLE) || StringUtils.equals(type, MongoData.GEWA_CUP_MIXED_DOUBLE)){
				Players partner = mongoService.getObject(Players.class, "id", mt.getToid());
				model.put("partnerMap", partner);
			}
			model.put("type", type);
			model.put("mid", mid);
		}
		model.put("clubstatus", getTime("gewaCupCommu", yearsType));
		return url;
	}
	private String clubApplyMethod(HttpServletRequest request, String sessid, Long commuid, String temp, String yearsType, String url, String result, ModelMap model) {
		String clubstatus = getTime("gewaCupCommu", yearsType);
		if(!StringUtils.equals(clubstatus,"game")) return forwardMessage(model, "此报名方式已经停止或未开始!");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return gotoLogin(result, request, model);
		if(commuid != null && StringUtils.isBlank(temp)){
			ClubInfo ci = mongoService.getObject(ClubInfo.class, "id", commuid);
			if(ci == null)return forwardMessage(model,"此俱乐部报名信息不存在！");
			model.put("clubMap", ci);
		}
		List<Commu> commuList = commuService.getManagedCommuList(member.getId());
		model.put("commuList", commuList);
		model.put("commuid", commuid);
		model.put("personalstatus", getTime("gewaCupPersonal", yearsType));
		return url;
	}
	private String saveClubInfoMethod(HttpServletRequest request, String sessid, Long commuid, String yearsType, ModelMap model) {
		String clubstatus = getTime("gewaCupCommu", yearsType);
		if(!StringUtils.equals(clubstatus,"game")) return showJsonError(model, "此报名方式已经停止或未开始!");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录!");
		Map datamap = request.getParameterMap();
		ClubInfo club = new ClubInfo();
		BindUtils.bindData(club,datamap);
		Commu commu = daoService.getObject(Commu.class, club.getId());
		if(commu == null) return showJsonError(model, "请选择俱乐部！");
		if(StringUtils.isBlank(club.getContact())) return showJsonError(model, "联系人为必填项！");
		if(!ValidateUtil.isMobile(club.getPhone())) return showJsonError(model, "联系人手机号码格式不正确！！");
		if(commuid == null){
			ClubInfo temp = mongoService.getObject(ClubInfo.class, "id", club.getId());
			if(temp != null) return showJsonError(model, "此俱乐部已经报过名了,请从我的报名状态处添加选手信息！");
		}
		club.setCommuname(commu.getName());
		club.setMemberid(member.getId());
		club.setMembername(member.getNickname());
		saveClubInfo(club, yearsType);
		return showJsonSuccess(model);
	}
	private String savePlayersInfoMethod(HttpServletRequest request, String sessid, Long clubInfoId, String mid, String yearsType, ModelMap model) {
		String clubstatus = "";
		if(clubInfoId == null) clubstatus = getTime("gewaCupPersonal", yearsType);
		else clubstatus = getTime("gewaCupCommu", yearsType);
		if(!StringUtils.equals(clubstatus,"game")) return showJsonError(model, "此报名方式已经停止或未开始!");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		Map datamap = request.getParameterMap();
		String type = ServiceHelper.get(datamap, "type");
		String idcards = ServiceHelper.get(datamap, "idcards");
		MiddleTable mt = new MiddleTable();
		boolean playerstatus = true;
		boolean partnerstatus = true;
		if(StringUtils.isBlank(type)) return showJsonError(model, "报名类型不正确！");
		if(!ValidateUtil.isMobile(ServiceHelper.get(datamap, "phone"))) return showJsonError(model, "你的手机号码格式不正确！");
		if(StringUtils.isNotBlank(mid)){
			mt = mongoService.getObject(MiddleTable.class, "id", mid);
			if(mt == null) return showJsonError(model, "参数错误！");
			Players play = mongoService.getObject(Players.class, "id", mt.getFromid());
			if(StringUtils.equals(play.getIdcards(), idcards)) playerstatus = false;
		}
		if(playerstatus){
			if(getIdcards(idcards, type, yearsType)) return showJsonError(model, "你的身份证已报过这项目，或已经参加了两个项目！");
		}
		List<Players> playerList = new ArrayList<Players>();
		if(StringUtils.equals(type, MongoData.GEWA_CUP_BOY_DOUBLE) || StringUtils.equals(type, MongoData.GEWA_CUP_GIRL_DOUBLE) || StringUtils.equals(type, MongoData.GEWA_CUP_MIXED_DOUBLE)){
			String partnerphone = ServiceHelper.get(datamap, "partnerphone");
			String partneridcards = ServiceHelper.get(datamap, "partneridcards");
			if(!ValidateUtil.isMobile(partnerphone)) return showJsonError(model, "搭档手机号码格式不正确！！");
			if(StringUtils.equals(idcards, partneridcards)) return showJsonError(model, "你的身份证不能和搭档身份证相同！");
			if(StringUtils.isNotBlank(mid)){
				Players partner = mongoService.getObject(Players.class, "id", mt.getToid()); 
				if(partner != null){
					if(StringUtils.equals(partner.getIdcards(), partneridcards)) partnerstatus = false;
				}
			}
			if(partnerstatus){
				if(getIdcards(partneridcards, type, yearsType)) return showJsonError(model, "搭档身份证已报过这项目，或已经参加了两个项目！");
			}
			Players partnerplayer = new Players();
			partnerplayer.setIdcards(partneridcards);
			partnerplayer.setIdcardslogo(ServiceHelper.get(datamap, "partneridcardslogo"));
			partnerplayer.setPlayer(ServiceHelper.get(datamap, "partnerplayer"));
			partnerplayer.setPhone(partnerphone);
			partnerplayer.setSex(ServiceHelper.get(datamap, "partnersex"));
			if(clubInfoId != null) partnerplayer.setClubInfoId(clubInfoId);
			playerList.add(partnerplayer);
		}
		Players player = new Players();
		BindUtils.bindData(player,datamap);
		player.setMemberid(member.getId());
		player.setMembername(member.getNickname());
		if(clubInfoId != null) player.setClubInfoId(clubInfoId);
		playerList.add(0, player);
		savePlayers(playerList, ServiceHelper.get(datamap, "type"), yearsType, AddressConstant.ADDRESS_WEB);
		return showJsonSuccess(model);
	}
	private String myPlayersInfoMethod(HttpServletRequest request, String sessid, String yearsType, String url, String result, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return gotoLogin(result, request, model);
		DBObject queryCondition = new BasicDBObject();
		DBObject relate1 = mongoService.queryBasicDBObject("memberid", "=", member.getId());
		DBObject relate2 = mongoService.queryBasicDBObject("yearstype", "=", yearsType);
		queryCondition.putAll(relate1);
		queryCondition.putAll(relate2);
		List<ClubInfo> clubList = mongoService.getObjectList(ClubInfo.class, queryCondition);
		DBObject relate3 = mongoService.queryBasicDBObject("clubInfoId", "=", null);
		queryCondition.putAll(relate3);
		List<MiddleTable> midList = mongoService.getObjectList(MiddleTable.class, queryCondition);
		if(!clubList.isEmpty()){
			getClubPlayersInfo(clubList, yearsType, model);
		}
		if(!midList.isEmpty()){
			model.put("personalList", getPersonalPlayersInfo(midList));
		}
		model.put("personalstatus", getTime("gewaCupPersonal", yearsType));
		model.put("clubstatus", getTime("gewaCupCommu", yearsType));
		return url;
	}
	private String bindGewaCupOrderMethod(Long commuid, String cupid, Long orderid, String yearsType, ModelMap model) {
		if(commuid == null){
			String clubstatus = getTime("gewaCupPersonal", yearsType);
			if(!StringUtils.equals(clubstatus,"game")) return showJsonError(model, "此报名方式已经停止或未开始!");
			Players player = mongoService.getObject(Players.class, "id", cupid);
			if(player == null)return showJsonError(model, "此报名信息不存在");
			player.setOrderid(orderid);
			mongoService.saveOrUpdateObject(player, "id");
		}else{
			String clubstatus = getTime("gewaCupCommu", yearsType);
			if(!StringUtils.equals(clubstatus,"game")) return showJsonError(model, "此报名方式已经停止或未开始!");
			ClubInfo ci = mongoService.getObject(ClubInfo.class, "id", commuid);
			if(ci == null)return showJsonError(model,"此俱乐部报名信息不存在！");
			ci.setOrderid(orderid);
			mongoService.saveOrUpdateObject(ci, "id");
		}
		return showJsonSuccess(model);
	}
	private String popupMethod(HttpServletRequest request, String sessid, String mid, Long commuid, String yearsType, String url, String result, ModelMap model) {
		String clubstatus = getTime("gewaCupCommu", yearsType);
		if(!StringUtils.equals(clubstatus,"game")) return forwardMessage(model, "此报名方式已经停止或未开始!");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return gotoLogin(result, request, model);
		if(StringUtils.isNotBlank(mid)){
			MiddleTable mt = mongoService.getObject(MiddleTable.class, "id", mid);
			if(mt == null) return forwardMessage(model, "此报名信息不存在");
			Players player = mongoService.getObject(Players.class, "id", mt.getFromid());
			model.put("playerMap", player);
			String type = mt.getType();
			if(StringUtils.equals(type, MongoData.GEWA_CUP_BOY_DOUBLE) || StringUtils.equals(type, MongoData.GEWA_CUP_GIRL_DOUBLE) || StringUtils.equals(type, MongoData.GEWA_CUP_MIXED_DOUBLE)){
				Players partner = mongoService.getObject(Players.class, "id", mt.getToid());
				model.put("partnerMap", partner);
			}
			model.put("type", mt.getType());
		}
		model.put("mid", mid);
		model.put("commuid", commuid);
		return url;
	}
}
