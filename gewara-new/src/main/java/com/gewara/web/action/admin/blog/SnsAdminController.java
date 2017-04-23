package com.gewara.web.action.admin.blog;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.Status;
import com.gewara.constant.SysAction;
import com.gewara.constant.content.ManagerCheckConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.json.ManageCheck;
import com.gewara.model.acl.User;
import com.gewara.model.bbs.BlackMember;
import com.gewara.model.bbs.Diary;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.SysMessageAction;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.AgendaService;
import com.gewara.service.bbs.AlbumService;
import com.gewara.service.bbs.CommuService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.bbs.SnsService;
import com.gewara.service.bbs.UserQuestionService;
import com.gewara.service.member.FriendService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;
@Controller
public class SnsAdminController extends BaseAdminController {

	@Autowired@Qualifier("snsService")
	private SnsService snsService;
	public void setSnsService(SnsService snsService) {
		this.snsService = snsService;
	}
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("albumService")
	private AlbumService albumService;
	public void setAlbumService(AlbumService albumService) {
		this.albumService = albumService;
	}
	
	@Autowired@Qualifier("friendService")
	private FriendService friendService;
	public void setMemberService(FriendService friendService) {
		this.friendService = friendService;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("commuService")
	private CommuService commuService;
	public void setCommuService(CommuService commuService) {
		this.commuService = commuService;
	}
	@Autowired@Qualifier("agendaService")
	private AgendaService agendaService;
	public void setAgendaService(AgendaService agendaService) {
		this.agendaService = agendaService;
	}
	@Autowired@Qualifier("userQuestionService")
	private UserQuestionService userQuestionService;
	public void setUserQuestionService(UserQuestionService userQuestionService) {
		this.userQuestionService = userQuestionService;
	}
	@RequestMapping("/admin/sns/searchSnsList.xhtml")
	public String searchSnsList(ModelMap model,Long memberid, String nickname,String mobile, String email,Integer pageNo){
		if(pageNo==null) pageNo=0;
		Integer rowsPage=18;
		Integer from =pageNo*rowsPage;
		nickname=StringUtils.trim(nickname);
		mobile=StringUtils.trim(mobile);
		email=StringUtils.trim(email);
		List<Member> listMember=snsService.searchMember(memberid,nickname,mobile,email, from,rowsPage);
		Integer count=snsService.searchMemberCount(memberid, nickname, mobile,email);
		Map<Long,BlackMember> mapBlackMember=new HashMap<Long, BlackMember>();
		Map<Long, MemberInfo> memberinfoMap = new HashMap<Long, MemberInfo>();
		for (Member member : listMember) {
			mapBlackMember.put(member.getId(),snsService.isJoinBlackMember(member.getId()));
			memberinfoMap.put(member.getId(), daoService.getObject(MemberInfo.class, member.getId()));
		}
		Map params=new HashMap();
		params.put("memberid", memberid);
		params.put("nickname", nickname);
		params.put("mobile", mobile);
		params.put("email", email);
		model.put("memberCount", snsService.searchMemberCount(null, null, null, null ));
		PageUtil pageUtil=new PageUtil(count,rowsPage,pageNo,"admin/sns/searchSnsList.xhtml");
		pageUtil.initPageInfo(params);
		model.put("pageUtil",pageUtil);
		model.put("mapBlackMember", mapBlackMember);
		model.put("listMember", listMember);
		model.put("memberinfoMap", memberinfoMap);
		return "admin/sns/snsList.vm";
	}
	@RequestMapping("/admin/sns/searchMemberList.xhtml")
	public String searchMemberList( ModelMap model,boolean check,Integer pageNo, Long memid){
		if(pageNo == null) pageNo = 0;
		int pageRow = 50;
		Criteria criter1 = new Criteria("tag").is(ManagerCheckConstant.USER_HEAD_SIGN);
		Query query = new Query(criter1);
		List<ManageCheck> manageCheckList = mongoService.getObjectList(ManageCheck.class, query.getQueryObject(), "modifytime", false, 0, 1);
		ManageCheck manageCheck = manageCheckList.get(0);
		model.put("manageCheck", manageCheck);
		Timestamp lasttime = new Timestamp(manageCheck.getModifytime());
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		int checkCount = snsService.getMemberCountByUpdatetime(null, lasttime);
		int uncheckCount = snsService.getMemberCountByUpdatetime(lasttime, cur);
		List<Map> memberMapList= new ArrayList<Map>();
		Timestamp starttime, endtime;
		if(check){
			endtime = lasttime;
			starttime = null;
			PageUtil pageUtil=new PageUtil(checkCount, pageRow, pageNo, "admin/sns/searchMemberList.xhtml");
			Map params=new HashMap();
			params.put("check", check);
			pageUtil.initPageInfo(params);
			model.put("pageUtil",pageUtil);
		}else{
			starttime = lasttime;
			endtime= cur;
		}
		if(memid!=null){
			MemberInfo info = daoService.getObject(MemberInfo.class, memid);
			memberMapList.add(BeanUtil.getBeanMap(info));
		}else{
			memberMapList=snsService.getMemberListByUpdatetime(starttime, endtime, pageNo*pageRow , pageRow);
		}
		if(!memberMapList.isEmpty()){
			cur = (Timestamp)(memberMapList.get(memberMapList.size()-1).get("updatetime"));
		}
		model.put("checkCount", checkCount);
		model.put("uncheckCount", uncheckCount);
		model.put("memberMapList", memberMapList);
		model.put("time", cur.getTime());
		model.put("tag", ManagerCheckConstant.USER_HEAD_SIGN);
		model.put("check", check);
		model.put("memid", memid);
		return "admin/sns/searchMemberList.vm";
	}
	
	@RequestMapping("/admin/sns/operationUserHeadAndSign.xhtml")
	public String operationUserHeadAndSign(ModelMap model, Long modifytime, String tag){
		User user = getLogonUser();
		if(modifytime == null || StringUtils.isBlank(tag))return showJsonError(model, "参数错误！");
		Criteria criter1 = new Criteria("tag").is(tag);
		Query query = new Query(criter1);
		List<ManageCheck> manageCheckList = mongoService.getObjectList(ManageCheck.class, query.getQueryObject(), "modifytime", false, 0, 1);
		Long modify = null;
		if(!manageCheckList.isEmpty()){
			modify = manageCheckList.get(0).getModifytime();
			if(modify >=modifytime) return showJsonError(model, "审核出错！");
		}
		Date temp = null;
		if(modify == null) temp = new Date(modifytime);
		else temp = new Date(modify);
		ManageCheck manageCheck = new ManageCheck(tag, modifytime, user.getId(),user.getUsername(),new Date(),temp);
		manageCheck.setId(System.currentTimeMillis() + "" + RandomUtils.nextInt(1000));
		mongoService.saveOrUpdateObject(manageCheck, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sns/setMemberInfo.xhtml")
	public String setMemberInfo(Long memberid, String param, String reason,String reasonDetail, ModelMap model){
		if(StringUtils.isBlank(param))return showJsonError(model, "参数错误！");
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, memberid);
		if(memberInfo == null) return showJsonError(model, "用户不存在或被删除！");
		User user = getLogonUser();
		if(!reason.equals("5")){
			reason=ServiceHelper.getReason(reason);
		}else{
			reason=reasonDetail;
		}
		SysMessageAction sysmessage=new SysMessageAction(SysAction.STATUS_RESULT);
		sysmessage.setFrommemberid(1l);
		if(StringUtils.equals(param, "sign")){
			ChangeEntry changeEntry = new ChangeEntry(memberInfo);
			sysmessage.setBody("您的签名【"+VmUtils.escabbr(memberInfo.getSign(), 10)+"...】涉及【"+reason+"】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
			sysmessage.setTomemberid(memberInfo.getId());
			daoService.saveObject(sysmessage);
			memberInfo.setSign(null);
			daoService.saveObject(memberInfo);
			monitorService.saveChangeLog(user.getId(), MemberInfo.class, memberInfo.getId(), changeEntry.getChangeMap(memberInfo));
			return showJsonSuccess(model);
		}else if(StringUtils.equals(param, "introduce")){
			ChangeEntry changeEntry = new ChangeEntry(memberInfo);
			sysmessage.setBody("您的简介【"+VmUtils.escabbr(memberInfo.getIntroduce(), 10)+"...】涉及【"+reason+"】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
			sysmessage.setTomemberid(memberInfo.getId());
			daoService.saveObject(sysmessage);
			memberInfo.setIntroduce(null);
			daoService.saveObject(memberInfo);
			monitorService.saveChangeLog(user.getId(), MemberInfo.class, memberInfo.getId(), changeEntry.getChangeMap(memberInfo));
			return showJsonSuccess(model);
		}else if(StringUtils.equals(param, "headpic")){
			ChangeEntry changeEntry = new ChangeEntry(memberInfo);
			sysmessage.setBody("您的头像涉及【"+reason+"】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
			sysmessage.setTomemberid(memberInfo.getId());
			daoService.saveObject(sysmessage);
			memberInfo.setHeadpic(null);
			daoService.saveObject(memberInfo);
			monitorService.saveChangeLog(user.getId(), Member.class, memberInfo.getId(), changeEntry.getChangeMap(memberInfo));
			return showJsonSuccess(model);
		}
		return showJsonError(model, "参数错误！");
	}
	@RequestMapping("/admin/sns/simpleSearchSnsListPage.xhtml")
	public String simpleSearchSnsListPage(){
		return "admin/sns/simplesnsList.vm";
	}
	
	@RequestMapping("/admin/sns/simpleSearchSnsList.xhtml")
	public String simpleSearchSnsList(ModelMap model,Long memberid,String nickname,String mobile,String email,Integer pageNo){
		if(pageNo==null) pageNo=0;
		Integer rowsPage=18;
		Integer from =pageNo*rowsPage;
		Map<Long,MemberInfo> memberInfoMap = new HashMap<Long,MemberInfo>();
		List<Member> listMember=snsService.searchMember(memberid,nickname,mobile,email, from,rowsPage);
		Integer count=snsService.searchMemberCount(memberid, nickname, mobile,email);
		for (Member member : listMember) {
			memberInfoMap.put(member.getId(), daoService.getObject(MemberInfo.class, member.getId()));
		}
		Map<Long,BlackMember> mapBlackMember=new HashMap<Long, BlackMember>();
		for (Member member : listMember) {
			mapBlackMember.put(member.getId(),snsService.isJoinBlackMember(member.getId()));
		}
		Map params=new HashMap();
		params.put("memberid", memberid);
		params.put("nickname", nickname);
		params.put("mobile", mobile);
		params.put("email", email);
		model.put("memberCount", snsService.searchMemberCount(null, null, null, null));
		PageUtil pageUtil=new PageUtil(count,rowsPage,pageNo,"admin/sns/simpleSearchSnsList.xhtml");
		pageUtil.initPageInfo();
		model.put("pageUtil",pageUtil);
		model.put("mapBlackMember", mapBlackMember);
		model.put("listMember", listMember);
		model.put("memberInfoMap", memberInfoMap);
		return "admin/sns/simplesnsList.vm";
	}
	
	@RequestMapping("/admin/sns/simpleSearchSnsExpList.xhtml")
	public String simpleSearchSnsExpList(ModelMap model,Integer startExp,Integer endExp,Integer pageNo){
		if(pageNo==null) pageNo=0;
		Integer maxNum=18;
		Integer from =pageNo*maxNum;
		Map<Long,Member> memberMap = new HashMap<Long, Member>();
		List<MemberInfo> memberInfoList = snsService.getMemberExpValueList(startExp, endExp, from, maxNum);
		for (MemberInfo memberInfo : memberInfoList) {
			memberMap.put(memberInfo.getId(), daoService.getObject(Member.class, memberInfo.getId()));
		}
		Integer count = snsService.getMemberExpValueCount(startExp, endExp);
		Integer sumMemberExp = snsService.getSumExpValue(startExp, endExp);//总经验值
		PageUtil pageUtil = new PageUtil(count,maxNum,pageNo,"admin/sns/simpleSearchSnsExpList.xhtml");
		Map params = new HashMap();
		params.put("startExp", startExp);
		params.put("endExp", endExp);
		model.put("memberMap", memberMap);
		pageUtil.initPageInfo(params);
		model.put("memberInfoList", memberInfoList);
		model.put("pageUtil",pageUtil);
		model.put("sumMemberExp", sumMemberExp);
		return "admin/sns/simplesnsExpList.vm";
	}
	
	@RequestMapping("/admin/sns/savePassword.xhtml")
	public String savePassword(ModelMap model,HttpServletRequest request){
		Map<String, String[]> map=request.getParameterMap();
		Long id=new Long(map.get("id")[0]+"");
		String password = map.get("password")[0];
		String passwordAgain = map.get("passwordAgain")[0];
		Member member=daoService.getObject(Member.class, id);
		if(StringUtils.isBlank(password)||StringUtils.isBlank(passwordAgain)) return showJsonError(model,"密码不能为空");
		if(StringUtils.isBlank(password)||StringUtils.isBlank(passwordAgain)||!StringUtils.equals(password, passwordAgain))return showJsonError(model, "两次输入的密码不一致！");
		if(!ValidateUtil.isPassword(password))return showJsonError(model, "密码格式不正确!");
		member.setPassword(StringUtil.md5(password));
		try{
			snsService.updateMemberPasswordAndDelete(member);
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "管理员：【"+getLogonUser().getUsername()+"】修改了用户编号为【"+member.getId()+"】的密码。");
		}catch (Exception e){
			return showJsonError(model, "修改密码失败！");
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sns/setMember.xhtml")
	public String setMember(ModelMap model,Long memberid, String memberstatus){
		if(memberid==null) return showJsonError(model, "请点击要设置的用户!");
		Member member=daoService.getObject(Member.class, memberid);
		if(memberstatus.equals(Status.Y))
			member.setRejected(Status.Y);
		else if(memberstatus.equals(Status.N))
			member.setRejected(Status.N);
		try{
			snsService.updateMemberPasswordAndDelete(member);
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "管理员：【"+getLogonUser().getUsername()+"】设置了编号为【"+member.getId()+"】的用户不能登录。");
		}catch(Exception e){
			return showJsonError(model,"设置失败");
		}
		return showJsonSuccess(model);
	}
	
	//查询某个时间段内在某个影院买票的用户
	@RequestMapping("/admin/sns/ticketMemberList.xhtml")
	public String searchMember2(ModelMap model, Long cinemaid, Integer pageNo, Date fromDate, Date toDate){
		String cinemaHql = "select new map(c.id as id, c.name as name) from Cinema c, CinemaProfile cp where c.id=cp.id";
		List<Map> cinemaList = hibernateTemplate.find(cinemaHql);
		model.put("cinemaList", cinemaList);
		if(cinemaid==null && (fromDate==null || toDate==null)) return "admin/sns/ticketMemberList.vm";
		if(pageNo==null) pageNo=0;
		Integer rowsPage = 20;
		Integer from =pageNo*rowsPage;
		Integer count = this.getTicketOrderCountByCinemaIdAndDate(cinemaid, fromDate, toDate);
		List<Map> ticketOrderMap = this.getTicketOrderListByCinemaIdAndDate(cinemaid, fromDate, toDate, from, rowsPage);
		Map<Long, Member> memberMap = new HashMap<Long, Member>();
		for(Map order : ticketOrderMap){
			Member member = daoService.getObject(Member.class, new Long(order.get("memberidinfo")+""));
			memberMap.put(new Long(order.get("memberidinfo")+""), member);
		}
		Map params=new HashMap();
		params.put("fromDate", DateUtil.format(fromDate, "yyyy-MM-dd"));
		params.put("toDate", DateUtil.format(toDate, "yyyy-MM-dd"));
		params.put("cinemaid", cinemaid);
		PageUtil pageUtil=new PageUtil(count,rowsPage,pageNo,"admin/sns/ticketMemberList.xhtml");
		pageUtil.initPageInfo(params);
		model.put("memberMap", memberMap);
		model.put("pageUtil",pageUtil);
		model.put("ticketOrderMap", ticketOrderMap);
		model.put("count", count);
		return "admin/sns/ticketMemberList.vm";
	}
	private List<Map> getTicketOrderListByCinemaIdAndDate(Long cinemaid, Date fromDate, Date toDate
			,Integer from, Integer maxNum){
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class);
		if(fromDate != null) {
			fromDate = DateUtil.getBeginningTimeOfDay(fromDate);
			query.add(Restrictions.ge("addtime", fromDate));
		}
		if(toDate != null) {
			toDate = DateUtil.getLastTimeOfDay(toDate);
			query.add(Restrictions.le("addtime", toDate));
		}
		query.setProjection(Projections.projectionList()
				.add(Projections.property("mobile"),"mobileinfo")
				.add(Projections.property("memberid"),"memberidinfo")
				.add(Projections.groupProperty("mobile"))
				.add(Projections.groupProperty("memberid")));
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		query.add(Restrictions.eq("cinemaid", cinemaid));
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		List orderList = hibernateTemplate.findByCriteria(query, from, maxNum);
		return orderList;
	}
	private Integer getTicketOrderCountByCinemaIdAndDate(Long cinemaid, Date fromDate, Date toDate){
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class);
		if(fromDate != null) {
			fromDate = DateUtil.getBeginningTimeOfDay(fromDate);
			query.add(Restrictions.ge("addtime", fromDate));
		}
		if(toDate != null) {
			toDate = DateUtil.getLastTimeOfDay(toDate);
			query.add(Restrictions.le("addtime", toDate));
		}
		query.add(Restrictions.eq("cinemaid", cinemaid));
		query.setProjection(Projections.countDistinct("mobile"));
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		List<TicketOrder> orderList = hibernateTemplate.findByCriteria(query);
		if(orderList.get(0)==null) return 0;
		return new Integer(orderList.get(0)+"");
	}
	
	
	@RequestMapping("/admin/sns/ajaxSearchMemberLog.xhtml")
	public String ajaxSearchMemberLog(Long memberid, ModelMap model){
		// 用户更多信息基本
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, memberid);
		model.put("memberInfoMap", BeanUtil.getBeanMapWithKey(memberInfo, "realname", "expvalue", "pointvalue", "newtask", "regfrom", "address", "finishedCount"));
		
		// 购票数量
		int ticketcount = countTicketOrderListByMemberid(memberid);
		model.put("ticketcount", ticketcount);
		
		// 帖子, 活动, 知道, 生活, 圈子
		int diarycount = diaryService.getDiaryCountByMemberid(Diary.class, null, null, memberid);
		int rediarycount = diaryService.getRepliedDiaryCount(Diary.class, memberid);
		model.put("diarycount", diarycount);
		model.put("rediarycount", rediarycount);
		
		ErrorCode<Integer> code = synchActivityService.getMemberActivityCount(memberid, null, RemoteActivity.TIME_ALL, null, null);
		if(code.isSuccess()) model.put("activitycount", code.getRetval());
		ErrorCode<Integer> joinCode  =  synchActivityService.getMemberJoinActivityCount(memberid);
		if(joinCode.isSuccess()) model.put("activityjoincount", joinCode.getRetval());
		
		int qcount = userQuestionService.getQuestionCountByMemberid(memberid);
		int acount = userQuestionService.getAnswerCountByMemberid(memberid);
		model.put("qcount", qcount);
		model.put("acount", acount);
		
		int agendacount = agendaService.getAgendaCountByDate(memberid, null);
		model.put("agendacount", agendacount);
		
		int quancount = commuService.getCommuCountByMemberId(memberid);
		model.put("quancount", quancount);
		
		int friendCount = friendService.getFriendCount(memberid);
		model.put("friendCount", friendCount);
		
		int albumcount = albumService.getAlbumListCountByMemberId(memberid);
		model.put("albumcount", albumcount);
		return "admin/sns/ajaxMemberLog.vm";
	}
	//重方法不要暴露在外面
	private Integer countTicketOrderListByMemberid(Long memberid){
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class, "t");
		query.setProjection(Projections.rowCount());
		query.add(Restrictions.eq("t.status", OrderConstant.STATUS_PAID_SUCCESS));
		query.add(Restrictions.eq("t.partnerid", 1L));
		query.add(Restrictions.eq("t.memberid", memberid));
		List result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}
	
}
