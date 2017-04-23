package com.gewara.web.action.subject.gewacup;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.Status;
import com.gewara.constant.SysAction;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.gewacup.ClubInfo;
import com.gewara.json.gewacup.MiddleTable;
import com.gewara.json.gewacup.Players;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.acl.User;
import com.gewara.model.pay.GewaOrder;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.web.util.PageUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Controller
public class GewaCupAdminController extends BaseGewaCupController {
	
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	
	@RequestMapping("/admin/sport/gewacupList.xhtml")
	public String gewaCupList2012(String type, String status, String yearsType, ModelMap model){
		if(StringUtils.isBlank(yearsType)) yearsType = MongoData.GEWA_CUP_YEARS_2012;
		Map params = new HashMap();
		params.put("type", yearsType);
		params.put("tag", "gewaCupPersonal");
		Map thirteenMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		params.put("tag", "gewaCupCommu");
		Map pomMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		model.put("thirteenMap", thirteenMap);
		model.put("pomMap", pomMap);
		DBObject queryCondition = new BasicDBObject();
		DBObject relate2 = mongoService.queryBasicDBObject("yearstype", "=", yearsType);
		if(StringUtils.isNotBlank(type)){
			DBObject relate1 = mongoService.queryBasicDBObject("type", "=", type);
			queryCondition.putAll(relate1);
		}
		queryCondition.putAll(relate2);
		if(StringUtils.isNotBlank(status)) {
			DBObject relate3 = mongoService.queryBasicDBObject("status", "=", Status.Y);
			queryCondition.putAll(relate3);
		}
		List<MiddleTable> clubList = mongoService.getObjectList(MiddleTable.class, queryCondition, "clubInfoId", true, 0, 2000);
		if(!clubList.isEmpty()){
			List<Map> personalList = getPersonalPlayersInfo(clubList);
			if(StringUtils.isNotBlank(status)) {
				Collections.shuffle(personalList);
			}
			model.put("personalList", personalList);
		}
		return "admin/gewacup/gewacup2012List.vm";
	}
	
	@RequestMapping("/admin/sport/ClubInfo.xhtml")
	public String ClubInfo(String yearsType, ModelMap model){
		if(StringUtils.isBlank(yearsType)) yearsType = MongoData.GEWA_CUP_YEARS_2012;
		DBObject queryCondition = new BasicDBObject();
		DBObject relate2 = mongoService.queryBasicDBObject("yearstype", "=", yearsType);
		queryCondition.putAll(relate2);
		List<ClubInfo> clubList = mongoService.getObjectList(ClubInfo.class, queryCondition);
		getClubPlayersInfo(clubList, yearsType, model);
		return "admin/gewacup/gewacup2012List.vm";
	}
	
	@RequestMapping("/admin/sport/updatePlayersStatus.xhtml")
	public String updatePlayersStatus(Long commuid, String mid, String status, String yearsType, ModelMap model){
		if(StringUtils.isBlank(yearsType)) yearsType = MongoData.GEWA_CUP_YEARS_2012;
		if(StringUtils.equals(Status.Y_NEW, status)) return showJsonError(model,"不能把状态改为申请中！");
		Long memberid= null;
		if(StringUtils.isBlank(mid)){
			DBObject queryCondition = new BasicDBObject();
			DBObject relate1 = mongoService.queryBasicDBObject("clubInfoId", "=", commuid);
			DBObject relate2 = mongoService.queryBasicDBObject("yearstype", "=", yearsType);
			queryCondition.putAll(relate1);
			queryCondition.putAll(relate2);
			List<MiddleTable> middleList = mongoService.getObjectList(MiddleTable.class, queryCondition);
			if(middleList.isEmpty()) return showJsonError(model, "此俱乐部暂时没有报名信息！");
			for(MiddleTable mt : middleList){
				mt.setStatus(status);
				mongoService.saveOrUpdateObject(mt, "id");
			}
			memberid = middleList.get(0).getMemberid();
		}else{
			MiddleTable middle = mongoService.getObject(MiddleTable.class, "id", mid);
			middle.setStatus(status);
			mongoService.saveOrUpdateObject(middle, "id");
			memberid = middle.getMemberid();
		}
		if(StringUtils.equals(Status.Y, status) && StringUtils.equals(yearsType, MongoData.GEWA_CUP_YEARS_2012)){
			String body = "您报名参加的“2012年格瓦拉羽毛球活力赛”已经通过审核，4月1日将公布赛事对阵图，感谢您的参与！手机登录m.gewara.com还可以随时随地，查看比赛结果和大赛新闻。<br/>客服电话：4000-406-506";
			userMessageService.sendSiteMSG(memberid,  SysAction.STATUS_RESULT, null, body);
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sport/deletePlayers.xhtml")
	public String deletePlayers(String mid, ModelMap model){
		if(StringUtils.isBlank(mid)) return showJsonError(model,"参数错误！");
		if(deletePlayers(mid)){
			GewaraUser user = getLogonUser();
			dbLogger.warn("admin："+user.getRealname()+"...删除了一位选手！");
			return showJsonSuccess(model);
		}else return showJsonError(model,"没有找到此选手信息！");
	}
	
	@RequestMapping("/admin/sport/updatePlayers.xhtml")
	public String updatePlayers(String mid, ModelMap model){
		if(StringUtils.isBlank(mid)) return showJsonError(model,"参数错误！");
		MiddleTable mt = mongoService.getObject(MiddleTable.class, "id", mid);
		if(mt == null) return showJsonError(model,"没有找到此选手信息");
		Players player = mongoService.getObject(Players.class, "id", mt.getFromid());
		if(player == null) return showJsonError(model,"没有找到此选手信息");
		String type = mt.getType();
		Map data = new HashMap();
		data.put("player", player.getPlayer());
		data.put("phone", player.getPhone());
		data.put("idcards", player.getIdcards());
		data.put("type", type);
		if(StringUtils.equals(type, MongoData.GEWA_CUP_BOY_DOUBLE) || StringUtils.equals(type, MongoData.GEWA_CUP_GIRL_DOUBLE) || StringUtils.equals(type, MongoData.GEWA_CUP_MIXED_DOUBLE)){
			Players partner = mongoService.getObject(Players.class, "id", mt.getToid());
			if(partner != null){
				data.put("partnerplayer", partner.getPlayer());
				data.put("partnerphone", partner.getPhone());
				data.put("partneridcards", partner.getIdcards());
			}
		}
		return showJsonSuccess(model,data);
	}
	@RequestMapping("/admin/sport/saveupdatePlayers.xhtml")
	public String saveupdatePlayers(String mid, String player, String idcards, String phone, String partnerplayer, String partneridcards, String partnerphone, ModelMap model){
		if(StringUtils.isBlank(mid)) return showJsonError(model,"参数错误！");
		MiddleTable mt = mongoService.getObject(MiddleTable.class, "id", mid);
		if(mt == null) return showJsonError(model,"没有找到此选手信息");
		Players players = mongoService.getObject(Players.class, "id", mt.getFromid());
		if(players == null) return showJsonError(model,"没有找到此选手信息");
		String type = mt.getType();
		if(StringUtils.equals(type, MongoData.GEWA_CUP_BOY_DOUBLE) || StringUtils.equals(type, MongoData.GEWA_CUP_GIRL_DOUBLE) || StringUtils.equals(type, MongoData.GEWA_CUP_MIXED_DOUBLE)){
			Players partner = mongoService.getObject(Players.class, "id", mt.getToid());
			if(partner == null) return showJsonError(model, "没有找到此选手信息");
			partner.setPlayer(partnerplayer);
			partner.setPhone(partnerphone);
			partner.setIdcards(partneridcards);
			mongoService.saveOrUpdateObject(partner, "id");
		}
		players.setPlayer(player);
		players.setPhone(phone);
		players.setIdcards(idcards);
		mt.setFromPlayer(idcards);
		mt.setToPlayer(partneridcards);
		mongoService.saveOrUpdateObject(mt, "id");
		mongoService.saveOrUpdateObject(players, "id");
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sport/updateOrderid.xhtml")
	public String updateOrderid(Long orderid, Long commuid, String id, ModelMap model){
		GewaOrder order = daoService.getObject(GewaOrder.class, orderid);
		if(order == null) return showJsonError(model,"不存在此订单！");
		if(commuid == null){
			Players player = mongoService.getObject(Players.class,"id", id);
			if(player == null) return showJsonError(model,"此选手没有报名！");
			player.setOrderid(order.getId());
			mongoService.saveOrUpdateObject(player, "id");
		}else{
			ClubInfo ci = mongoService.getObject(ClubInfo.class, "id", commuid);
			if(ci == null) return showJsonError(model,"此俱乐部没有报名！");
			ci.setOrderid(order.getId());
			mongoService.saveOrUpdateObject(ci, "id");
		}
		GewaraUser user = getLogonUser();
		dbLogger.warn("admin："+user.getRealname()+"...修改了订单号！改为："+orderid);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sport/reportAnswer.xhtml")
	public String reportAnswer(Integer pageNo, String yearsType, ModelMap model){
		if(null == pageNo) pageNo=0;
		int rowsPerPage = 20;
		int forms = pageNo * rowsPerPage;
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, yearsType);
		params.put(MongoData.ACTION_TAG, MongoData.GEWA_CUP_ANSWER);
		List<Map> dataList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_MEMBER, params, forms, rowsPerPage);
		PageUtil pageUtil = new PageUtil(mongoService.getCount(MongoData.NS_ACTIVITY_COMMON_MEMBER, params), rowsPerPage, pageNo, "/admin/sport/reportAnswer.xhtml");
		Map param = new HashMap();
		pageUtil.initPageInfo(param);
		model.put("pageUtil", pageUtil);
		model.put("dataList", dataList);
		return "admin/gewacup/gewacup2012List.vm";
	}
	
	private final User getLogonUser(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth == null) return null;
		if(auth.isAuthenticated() && !auth.getName().equals("anonymous")){//登录
			GewaraUser user = (GewaraUser) auth.getPrincipal();
			//refresh(user);
			if(user instanceof User) return (User)user;
		}
		return null;
	}
}