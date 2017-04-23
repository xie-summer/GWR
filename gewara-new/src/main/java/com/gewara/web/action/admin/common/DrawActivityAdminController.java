package com.gewara.web.action.admin.common;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.DrawActicityConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Festival;
import com.gewara.mongo.MongoService;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class DrawActivityAdminController extends BaseAdminController {
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	@Autowired@Qualifier("drawActivityService")
	private DrawActivityService drawActivityService;
	public void setDrawActivityService(DrawActivityService drawActivityService) {
		this.drawActivityService = drawActivityService;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	
	/**
	 * 抽奖活动列表
	 */
	@RequestMapping("/admin/draw/drawactivityList.xhtml")
	public String drawActivityList(String order, ModelMap model,Integer pageNo){
		if(pageNo == null) pageNo = 0;
		Integer maxNum = 10;
		Integer from = pageNo * maxNum;
		List<DrawActivity> drawActivityList = drawActivityService.getDrawActivityList(order, from, maxNum);
		Map<Long, String> flashTagMap = new HashMap<Long, String>();
		for(DrawActivity drawActivity : drawActivityList){
			Map<String, String> daOtherinfoMap = JsonUtils.readJsonToMap(drawActivity.getOtherinfo());
			Timestamp timestamp = DateUtil.getCurFullTimestamp();
			if(timestamp.after(drawActivity.getStarttime()) && timestamp.before(drawActivity.getEndtime())){
				flashTagMap.put(drawActivity.getId(), daOtherinfoMap.get("flashTag"));
			}
		}
		Integer count = drawActivityService.getDrawActivityCount();
		PageUtil pageUtil = new PageUtil(count,maxNum,pageNo,"admin/draw/drawactivityList.xhtml");
		Map params = new HashMap();
		params.put("order", order);
		pageUtil.initPageInfo(params);
		model.put("order", order);
		model.put("drawActivityList", drawActivityList);
		model.put("flashTagMap", flashTagMap);
		model.put("pageUtil", pageUtil);
		return "admin/drawactivity/drawActivityList.vm";
	}
	
	@RequestMapping("/admin/draw/appsourceList.xhtml")
	public String drawActivityList(Long activityid, ModelMap model){
		DrawActivity da = daoService.getObject(DrawActivity.class, activityid);
		model.put("da", da);
		Map<String, String> otherinfoMap = VmUtils.readJsonToMap(da.getOtherinfo());
		String appsource = otherinfoMap.get(DrawActicityConstant.TASK_APPSOURCE);
		List<String> appList = new ArrayList<String>();
		if(StringUtils.isNotBlank(appsource)){
			 appList = Arrays.asList(StringUtils.split(appsource, ","));
		}
		model.put("appList", appList);
		model.put("appSourcesMap", getAppSourceMap());
		return "admin/drawactivity/appsourceList.vm";
	}
	@RequestMapping("/admin/draw/saveAppsource.xhtml")
	public String saveAppsource(Long activityid, String appsource, ModelMap model){
		DrawActivity da = daoService.getObject(DrawActivity.class, activityid);
		String other = JsonUtils.addJsonKeyValue(da.getOtherinfo(), DrawActicityConstant.TASK_APPSOURCE, appsource);
		da.setOtherinfo(other);
		daoService.saveObject(da);
		model.put("activityid", activityid);
		return "redirect:/admin/draw/appsourceList.xhtml";
	}
	
	/**
	 * 奖品信息列表
	 */
	@RequestMapping("/admin/draw/prizeList.xhtml")
	public String prizeList(ModelMap model,Long did){
		DrawActivity da = daoService.getObject(DrawActivity.class, did);
		if(da == null) return showError(model, "不存在此抽奖活动信息！");
		List<Prize> prizeList = drawActivityService.getPrizeListByDid(did,null);
		double sum=0;
		if(!prizeList.isEmpty())
			for(Prize prize:prizeList){
				 int chancenum = prize.getChancenum() == null? 0: prize.getChancenum();
				sum += chancenum;
			}
		model.put("countsum", sum);
		model.put("prizeList", prizeList);
		return "admin/drawactivity/prizeList.vm";
	}
	
	/**
	 * add奖品信息
	 */
	@RequestMapping("/admin/draw/addPrizeInfo.xhtml")
	public String addPrizeInfo(Long id, Long activityid, Integer pnumber, HttpServletRequest request, ModelMap model){
		DrawActivity da = daoService.getObject(DrawActivity.class, activityid);
		if(da == null) return showJsonError(model, "数据错误！");
		if(pnumber==null || pnumber<0) return showJsonError(model, "奖品数量不能为空或小于0！");
		try{
			Prize prize = null;
			if(id == null){
				prize = new Prize();
				prize.setPsendout(0);
			}else{
				prize = daoService.getObject(Prize.class, id);
				if(prize==null) return showJsonError(model, "不存在此奖品信息！");
				prize.setPsendout(drawActivityService.getSendOutPrizeCount(activityid, id));
			}
			BindUtils.bindData(prize, request.getParameterMap());
			String[] otheritemList = new String[]{DrawActicityConstant.TASK_SYS_MESSAGE, DrawActicityConstant.TASK_WALA_CONTENT, DrawActicityConstant.TASK_WALA_LINK};
			Map<String, Object> otherinfoMap = controllerService.getAndSetOtherinfo(prize.getOtherinfo(), Arrays.asList(otheritemList), request);
			prize.setOtherinfo(JsonUtils.writeObjectToJson(otherinfoMap));
			daoService.saveObject(prize);
			return showJsonSuccess(model);
		}catch(Exception e){
			return showJsonError(model, "操作失败！");
		}
	}
	
	/**
	 * update奖品信息
	 */
	@RequestMapping("/admin/draw/updatePrizeInfo.xhtml")
	public String updatePrizeInfo(ModelMap model,Long pid){
		Prize p = daoService.getObject(Prize.class, pid);
		Map result = BeanUtil.getBeanMap(p, false);
		if(result.get("otherinfo") != null){
			result.put("otherinfo", VmUtils.readJsonToMap(result.get("otherinfo") + ""));
		}
		return showJsonSuccess(model, result);
	}
	
	/**
	 * add抽奖活动信息
	 */
	@RequestMapping("/admin/draw/addDrawActivityInfo.xhtml")
	public String addDrawActivityInfo(Long did, ModelMap model, String name, String tag, Timestamp startTime, Timestamp endTime, String showsite, HttpServletRequest request){
		if(startTime == null) return showJsonError(model,"活动开始时间不能为空！");
		if(endTime!=null){
			if(startTime.getTime()>endTime.getTime()){
				return showJsonError(model, "活动开始时间不能大于结束时间！");
			}
		}
		List<String> task_List = Arrays.asList(DrawActicityConstant.TASK_MOBILE, DrawActicityConstant.TASK_EMAIL, DrawActicityConstant.TASK_TICKET, DrawActicityConstant.TASK_MOREDRAW, DrawActicityConstant.TASK_POINT, DrawActicityConstant.TASK_MOVIEID, DrawActicityConstant.TASK_HOUR, DrawActicityConstant.TASK_FRIEND, 
				DrawActicityConstant.TASK_POINTVALUE, DrawActicityConstant.TASK_WEIBO, DrawActicityConstant.TASK_ONLYONE, DrawActicityConstant.TASK_ONLYMOBILE, DrawActicityConstant.TASK_DAY_COUNT);
		DrawActivity da = null;
		if(did != null){
			da = daoService.getObject(DrawActivity.class, did);
			da.setName(name);
			da.setTag(tag);
			da.setShowsite(showsite);
			da.setStarttime(startTime);
			da.setEndtime(endTime);
		}else{
			da = new DrawActivity(name,tag,startTime,endTime,showsite);
		}
		Map<String, Object> otherinfoMap = JsonUtils.readJsonToMap(da.getOtherinfo());
		for (String string : task_List) {
			String info = request.getParameter(string);
			if(StringUtils.isNotBlank(info)){
				otherinfoMap.put(string, info);
			}else otherinfoMap.remove(string);
		}
		da.setOtherinfo(JsonUtils.writeObjectToJson(otherinfoMap));
		try{
			daoService.saveObject(da);
			return showJsonSuccess(model);
		}catch(Exception e){
			return showJsonError(model, "添加失败");
		}
	}
	
	/**
	 * update抽奖活动信息
	 */
	@RequestMapping("/admin/draw/updateDrawActivityInfo.xhtml")
	public String updateDrawActivity(ModelMap model,Long did){
		DrawActivity da = daoService.getObject(DrawActivity.class, did);
		Map daMap = BeanUtil.getBeanMap(da);
		Map<String, String> map = VmUtils.readJsonToMap(da.getOtherinfo());
		daMap.put("map", map);
		return showJsonSuccess(model, daMap);
	}
	
	/**
	 * 获奖用户信息列表
	 */
	@RequestMapping("/admin/draw/winnerList.xhtml")
	public String winnerList(ModelMap model,Integer pageNo, Long activityid, Long prizeid, Long memberid, Integer maxNum,
			String mobile, String nickname, Timestamp startTime, Timestamp endTime, String tag){
		if(pageNo == null) pageNo = 0;
		if(maxNum == null) maxNum = 200;
		Integer from = pageNo * maxNum;
		List<DrawActivity> drawActivityList = drawActivityService.getDrawActivityList(null, 0, 100);
		Map<Long, DrawActivity> drawActivityMap = BeanUtil.beanListToMap(drawActivityList, "id");
		List<WinnerInfo> winnerList = drawActivityService.getWinnerList(activityid,prizeid!=null?Arrays.asList(prizeid):null,startTime,endTime,tag,memberid,mobile,nickname, from, maxNum);
		//List<Prize> prizeList = drawActivityService.getPrizeListByDid(activityid,null);
		Integer count = drawActivityService.getWinnerCount(activityid, prizeid!=null?Arrays.asList(prizeid):null,startTime,endTime,tag,memberid,mobile,nickname);
		Map params = new HashMap();
		params.put("activityid", activityid);
		params.put("prizeid", prizeid);
		params.put("startTime", startTime);
		params.put("memberid", memberid);
		params.put("endTime", endTime);
		params.put("mobile",mobile);
		params.put("nickname", nickname);
		PageUtil pageUtil = new PageUtil(count,maxNum,pageNo,"admin/draw/winnerList.xhtml");
		pageUtil.initPageInfo(params);
		//统计改活动，各奖项获奖人数
		
		List<Long> prizeIdList = BeanUtil.getBeanPropertyList(winnerList, "prizeid", true);
		Map<Long, Prize> prizeMap = daoService.getObjectMap(Prize.class, prizeIdList);
		if(activityid != null){
			Map<Long,List<WinnerInfo>> winnerInfoMap = BeanUtil.groupBeanList(winnerList, "prizeid");
			model.put("winnerInfoMap", winnerInfoMap);
		}
		DrawActivity da = daoService.getObject(DrawActivity.class, activityid);
		if(da != null && memberid != null){
			Integer chanceNum = drawActivityService.getCurChanceNum(da, memberid);
			model.put("chanceNum", chanceNum);//当天剩余抽奖次数
			//当前用户邀请了多少人
			Integer inviteCount = drawActivityService.getInviteMemberCount(memberid ,da.getTag(), false, startTime, endTime);
			model.put("inviteCount", inviteCount);
		}
		model.put("pageUtil", pageUtil);
		model.put("winnerList",winnerList);
		model.put("drawActivityMap", drawActivityMap);
		model.put("prizeid", prizeid);
		model.put("prizeMap", prizeMap);
		return "admin/drawactivity/winnerList.vm";
	}
	//收货信息
	@RequestMapping("/admin/draw/winningReceiptInfo.xhtml")
	public String winningReceiptInfo(Long memberid, String tag, ModelMap model){
		Map params = new HashMap();
		params.put(MongoData.GEWA_CUP_MEMBERID, memberid);
		params.put(MongoData.ACTION_TAG, tag);
		Map map = mongoService.findOne(MongoData.NS_WINNING_RECEIPT_INFO, params);
		if(map == null)return showJsonError(model, "该用户没填收货信息！");
		return showJsonSuccess(model, map);
	}
	/**
	 * 根据抽奖活动id，获取该活动下的奖品信息
	 */
	@RequestMapping("/admin/draw/winnerPrizeList.xhtml")
	public String getWinnerPrizeList(ModelMap model,Long did){
		List<Prize> prizeList = drawActivityService.getPrizeListByDid(did,null);
		Map map = new HashMap();
		map.put("prizeLists", BeanUtil.getBeanMapList(prizeList, "id","plevel"));
		return showJsonSuccess(model,map);
	}
	
	/**
	 * 通过后台添加中奖用户信息列表
	 */
	@RequestMapping("/admin/draw/shamWinnerList.xhtml")
	public String shamWinnerList(ModelMap model,Long activityid,Integer pageNo){
		DrawActivity da = daoService.getObject(DrawActivity.class, activityid);
		if(da == null) return showJsonError(model, "不存在此抽奖活动信息！");
		if(pageNo == null) pageNo = 0 ;
		Integer maxNum = 20;
		Integer from = pageNo * maxNum;
		Map<Long,Prize> prizeMap = new HashMap<Long, Prize>();
		List<WinnerInfo> winnerList = drawActivityService.getWinnerList(activityid, null,null,null,WinnerInfo.TAG_USER, null,null,null,from, maxNum);
		for (WinnerInfo winnerInfo : winnerList) {
			prizeMap.put(winnerInfo.getId(), daoService.getObject(Prize.class, winnerInfo.getPrizeid()));
		}
		Integer count = drawActivityService.getWinnerCount(activityid, null,null,null,WinnerInfo.TAG_USER,null,null,null);
		Map params = new HashMap();
		params.put("activityid", activityid);
		PageUtil pageUtil = new PageUtil(count,maxNum,pageNo,"admin/draw/shamWinnerList.xhtml");
		pageUtil.initPageInfo(params);
		List<Prize> prizeList = drawActivityService.getPrizeListByDid(activityid,null);
		model.put("prizeList", prizeList);
		model.put("pageUtil", pageUtil);
		model.put("winnerList", winnerList);
		model.put("prizeMap", prizeMap);
		return "admin/drawactivity/shamWinnerList.vm";
	}
	
	@RequestMapping("/admin/draw/getWinnerInfo.xhtml")
	public String getWinnerInfo(String tag, Integer pageNo, ModelMap model){
		if(pageNo == null) pageNo = 0 ;
		Integer maxNum = 50;
		Integer from = pageNo * maxNum;
		Map params = new HashMap();
		params.put(MongoData.ACTION_TAG, tag);
		int count = mongoService.getCount(MongoData.NS_WINNING_RECEIPT_INFO, params);
		if(count > 0){
			List resultList = mongoService.find(MongoData.NS_WINNING_RECEIPT_INFO, params, MongoData.ACTION_ADDTIME, false, from, maxNum);
			PageUtil pageUtil = new PageUtil(count,maxNum,pageNo,"admin/draw/getWinnerInfo.xhtml");
			pageUtil.initPageInfo(params);
			model.put("pageUtil", pageUtil);
			model.put("resultList", resultList);
		}
		return "admin/newsubject/commonWinnerInfo.vm";
	}
	
	/**
	 * 后台添加中奖用户信息
	 */
	@RequestMapping("/admin/draw/addShamWinnerInfo.xhtml")
	public String addShamWinnerInfo(ModelMap model,Long wid,Long activityid,Long memberid,String nickname,Long prizeid,String mobile,Timestamp addtime){
		DrawActivity da = daoService.getObject(DrawActivity.class, activityid);
		if(da == null) return showJsonError(model, "不存在此抽奖活动信息！");
		WinnerInfo wi = null;
		try{
			if(wid == null){
				wi = new WinnerInfo(activityid, prizeid, mobile, addtime, WinnerInfo.TAG_USER);
				wi.setMemberid(memberid);
				wi.setNickname(nickname);
			}else{
				wi = daoService.getObject(WinnerInfo.class, wid);
				wi.setActivityid(activityid);
				wi.setAddtime(addtime);
				wi.setNickname(nickname);
				wi.setMobile(mobile);
				wi.setPrizeid(prizeid);
				wi.setMemberid(memberid);
			}
			daoService.addObject(wi);
			return showJsonSuccess(model);
		}catch(Exception e){
			return showJsonError(model, "操作失败！");
		}
	}
	
	/**
	 * 修改后台手动添加的中奖用户信息
	 */
	@RequestMapping("/admin/draw/updateShamWinnerInfo.xhtml")
	public String updateShamWinnerInfo(ModelMap model,Long wid,Long activityid){
		DrawActivity da = daoService.getObject(DrawActivity.class, activityid);
		if(da == null) return showJsonError(model, "不存在此抽奖活动信息！");
		WinnerInfo wi = daoService.getObject(WinnerInfo.class, wid);
		return showJsonSuccess(model,BeanUtil.getBeanMap(wi, false));
	}
	
	/**
	 * 判断奖品是否已经发出
	 */
	@RequestMapping("/admin/draw/sendDrawSms.xhtml")
	public String sendDrawSms(Long wid,ModelMap model){
		WinnerInfo winner = daoService.getObject(WinnerInfo.class, wid);
		Prize prize = daoService.getObject(Prize.class, winner.getPrizeid());
		SMSRecord sms = drawActivityService.sendPrize(prize, winner, true);
		if(sms!=null) untransService.sendMsgAtServer(sms, false);
		return showJsonSuccess(model);
	}
	
	/**
	 * 批量发送奖品
	 */
	@RequestMapping("/admin/draw/batchSendDraw.xhtml")
	public String batchSendDraw(String wids,ModelMap model){
		String[] ids=wids.split(";");
		for (String id : ids) {
			Long wid=Long.valueOf(id);
			WinnerInfo winner = daoService.getObject(WinnerInfo.class, wid);
			Prize prize = daoService.getObject(Prize.class, winner.getPrizeid());
			SMSRecord sms = drawActivityService.sendPrize(prize, winner, true);
			if(sms!=null) untransService.sendMsgAtServer(sms, false);
		}
		return showJsonSuccess(model);
	}
	

	/**
	 * 节日列表
	 * @return
	 */
	@RequestMapping("/admin/draw/festivalList.xhtml")
	public String festivalList(ModelMap model){
		List<Festival> festivalList = daoService.getObjectList(Festival.class, "addtime", false, 0, 1000);
		model.put("festivalList", festivalList);
		return "admin/drawactivity/festivalList.vm";
	}
	
	/**
	 * 添加或修改节日信息
	 */
	@RequestMapping("/admin/draw/saveFestivalInfo.xhtml")
	public String addOrUpdateFestival(Long fid,String festName,Date festDate,Long drawid,String summary,String logo,String link,ModelMap model){
		Festival f = null;
		if(fid == null){
			f = new Festival(festName,festDate,drawid,summary);
			f.setLogo(logo);
			f.setLink(link);
			daoService.saveObject(f);
		}else{
			f = daoService.getObject(Festival.class, fid);
			if(f == null) return showJsonError(model, "你修改的节日信息不存在！");
			f.setDrawid(drawid);
			f.setFestdate(festDate);
			f.setFestname(festName);
			f.setSummary(summary);
			f.setLogo(logo);
			f.setLink(link);
			daoService.updateObject(f);
		}
		return showJsonSuccess(model);
	}
	
	/**
	 * 删除节日信息
	 */
	@RequestMapping("/admin/draw/deletefestivalInfo.xhtml")
	public String deleteFestival(Long fid,ModelMap model){
		Festival f = daoService.getObject(Festival.class, fid);
		if(f != null){
			daoService.removeObject(f);
			return showJsonSuccess(model);
		} else return showJsonError(model,"你要删除的节日信息不存在！");
	}
}
