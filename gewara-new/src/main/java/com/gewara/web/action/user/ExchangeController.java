package com.gewara.web.action.user;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.GoodsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.GoodsFilterHelper;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.goods.Goods;
import com.gewara.model.movie.GrabTicketSubject;
import com.gewara.model.pay.PubMember;
import com.gewara.model.pay.PubSale;
import com.gewara.model.user.Agenda;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.AgendaService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.order.GoodsService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.order.PubSaleService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;

@Controller
public class ExchangeController extends AnnotationController {
	private static final String pubSalePriKey = "x#abcd";
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	public void setGoodsService(GoodsService goodsService) {
		this.goodsService = goodsService;
	}
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	public void setControllerService(ControllerService controllerService) {
		this.controllerService = controllerService;
	}
	@Autowired@Qualifier("pubSaleService")
	private PubSaleService pubSaleService;
	public void setPubSaleService(PubSaleService pubSaleService) {
		this.pubSaleService = pubSaleService;
	}
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}

	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}

	@Autowired@Qualifier("agendaService")
	private AgendaService agendaService;
	public void setAgendaService(AgendaService agendaService) {
		this.agendaService = agendaService;
	}
	
	@RequestMapping("/exchange/point/index.xhtml")
	public String exchangePointList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, ModelMap model, HttpServletResponse response){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member!=null)model.putAll(controllerService.getCommonData(model, member, member.getId()));
		List<Goods> goodsList = goodsService.getCurGoodsList(Goods.class, GoodsConstant.GOODS_TAG_POINT, null, 0, 30);
		GoodsFilterHelper.goodsFilter(goodsList, PartnerConstant.GEWA_SELF);
		model.put("goodsList", goodsList);
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<GewaCommend> gcList = commonService.getGewaCommendList(citycode, SignName.POINTEXCHANGE, null,null, true,0, 20);
		model.put("gcList", gcList);
		return "exchange/point/index.vm";
	}
	@RequestMapping("/exchange/point/goodsList.xhtml")
	public String goodsRecordList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request,ModelMap model, Integer pageNo, HttpServletResponse response){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member!=null) {
			model.putAll(controllerService.getCommonData(model, member, member.getId()));
			MemberInfo info = daoService.getObject(MemberInfo.class, member.getId());
			model.put("sumPoint", info.getPointvalue());
		}
		if(pageNo==null) pageNo=0;
		Integer rowsPage = 20;
		Integer from = pageNo*rowsPage;
		List<Goods> goodsList = goodsService.getCurGoodsList(Goods.class, GoodsConstant.GOODS_TAG_POINT, null, from, rowsPage);
		Integer count = goodsService.getGoodsCount(Goods.class, GoodsConstant.GOODS_TAG_POINT, null, true, true, true);
		PageUtil pageUtil=new PageUtil(count,rowsPage,pageNo,"exchange/point/goodsList.xhtml", true, true);
		pageUtil.initPageInfo();
		model.put("pageUtil",pageUtil);
		model.put("goodsList", goodsList);
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<GewaCommend> gcList = commonService.getGewaCommendList(citycode, SignName.POINTEXCHANGE, null,null, true,0, 20);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		commonService.initGewaCommendList("gcList", rh, gcList);
		model.put("gcList", gcList);
		return "exchange/point/goodsList.vm";
	}
	@RequestMapping("/exchange/point/getPointMethod.xhtml")
	public String getPonitList(ModelMap model,@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, HttpServletResponse response){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member!=null)model.putAll(controllerService.getCommonData(model, member, member.getId()));
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<GewaCommend> gcList = commonService.getGewaCommendList(citycode, SignName.POINTEXCHANGE, null,null, true, 0,20);
		RelatedHelper rh = new RelatedHelper(); 
		model.put("relatedHelper", rh);
		commonService.initGewaCommendList("gcList", rh, gcList);
		model.put("gcList", gcList);
		return "exchange/point/getPointMethod.vm";
	}
	@RequestMapping("/exchange/point/introduce.xhtml")
	public String pointDetail(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member!=null)model.putAll(controllerService.getCommonData(model, member, member.getId()));
		return "exchange/point/introduce.vm";
	}
	@RequestMapping("/exchange/point/buyGoods.xhtml")
	public String exchangePointDetail(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long gid, ModelMap model){
		Goods goods = daoService.getObject(Goods.class, gid);
		if(goods==null) return showMessage(model, "该数据不存在！");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member!=null){
			model.putAll(controllerService.getCommonData(model, member, member.getId()));
			MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
			model.put("sumPoint", memberInfo.getPointvalue());
		}
		model.put("goods", goods);
		return "exchange/point/buyGoods.vm";
	}

	@RequestMapping("/exchange/pubsale/index.xhtml")
	public String pubsale(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long sid, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member!=null) {
			MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
			model.put("memberInfo", memberInfo);
			model.put("member", member);
			addCacheMember(model, member.getId());
		}
		PubSale sale = null;
		if(sid!=null) {
			sale = daoService.getObject(PubSale.class, sid);
		}else { 
			List<PubSale> saleList = pubSaleService.getPubSaleList(null, null, 0, 1);
			if(saleList.size()>0){
				sale = saleList.get(0);
			}
		}
		if(sale==null || sale.isClose()) return show404(model, "该竞拍不存在或被删除！");
		DiaryBase diary = diaryService.getDiaryBase(3106298L);
		model.put("sale", sale);
		model.put("diary", diary);
		if(diary != null){
			model.put("diaryBody", blogService.getDiaryBody(diary.getId()));
		}
		
		// 5条最新竞拍
		List<PubSale> saleList = pubSaleService.getPubSaleList(null, Status.Y, 0, 5);
		model.put("saleListMap", BeanUtil.getBeanMapList(saleList, "id", "limg", "name"));
		return "exchange/pubsale/index.vm";
	}
	
	@RequestMapping("/exchange/pubsale/countdown.xhtml")
	public String countdown(Long sid, ModelMap model) {
		PubSale sale = daoService.getObject(PubSale.class, sid);
		if(sale == null || sale.isClose()) return showJsonError_NOT_FOUND(model);
		Map<String, Object> map = new HashMap<String, Object>();
		Long countdown = null;
		boolean isJoin = false;
		Long memberid = null;
		Long cur = System.currentTimeMillis();
		Timestamp curtime = new Timestamp(cur);
		if(sale.isSoon()) { 		//即将竞拍
			countdown = sale.getBegintime().getTime() - cur;
		}else if(sale.isJoin()){ //正在竞拍
			countdown = sale.getLasttime().getTime() - cur;
			if(countdown<=0 || sale.isEnd2()) {
				sale.setStatus(Status.Y);
				daoService.saveObject(sale);
				countdown = 0L;
			}
			isJoin = true;
		}else if(sale.isEnd(curtime) && "N".equals(sale.getStatus())){
			sale.setStatus(Status.Y);
			daoService.saveObject(sale);
		}
		if(sale.isEnd4(curtime) || StringUtils.equals(Status.Y, sale.getStatus())){
			String qry = "select m.memberid from PubMember m where m.pubid=? order by m.addtime desc";
			List<Long> midList = daoService.queryByRowsRange(qry, 0, 1, sid);
			if(midList.size()>0){ 
				memberid = midList.get(0);
				if(!StringUtils.equals(memberid+"", sale.getMemberid()+"")) {
					sale.setMemberid(memberid);
					daoService.saveObject(sale);
				}
			}
		}
		if(memberid!=null){
			MemberInfo mi = daoService.getObject(MemberInfo.class, memberid);
			map.put("memberPic", mi.getHeadpicUrl());
			map.put("memberid", memberid);
		}else{
			if(sale.getMemberid() != null){
				MemberInfo mi = daoService.getObject(MemberInfo.class, sale.getMemberid());
				map.put("memberPic", mi.getHeadpicUrl());
				map.put("memberid", sale.getMemberid());
			}
		}
		map.put("isJoin", isJoin);
		map.put("countdown", countdown);
		map.put("curprice", sale.gainRprice(sale.getCurprice()));
		map.put("nickname", sale.getNickname());
		map.put("cur", cur);
		map.put("checkStr", StringUtil.md5(sale.getId() + pubSalePriKey + cur, 8));
		return showJsonSuccess(model, map);
	}
	@RequestMapping("/exchange/pubsale/pubOrder.xhtml")
	public String pubOrder(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long sid, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member==null) return showJsonError(model, "请先登录！");
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		PubSale sale = daoService.getObject(PubSale.class, sid);
		if(sale == null) return showJsonError_NOT_FOUND(model);
		if(sale.isEnd(curtime) && StringUtils.equals("N", sale.getStatus())){
			sale.setStatus(Status.Y);
			daoService.saveObject(sale);
		}
		if(!StringUtils.equals(sale.getStatus(), Status.Y)) return showJsonError(model, "竞拍未结束！");
		if(!StringUtils.equals(sale.getMemberid()+"", member.getId()+"")) return showJsonError(model, "非竞拍成功用户！");
		Timestamp totime = DateUtil.addDay(sale.getEndtime(), 1);
		int paySuccessCount = pubSaleService.getMemberPubSaleOrderCountByMemberid(sale.getMemberid(), sale.getId(), sale.getBegintime(), totime, null);
		if(paySuccessCount > 0) return showJsonError(model, "你已成功支付！");
		Long orderid = pubSaleService.stopPubSale(sale);
		if(orderid==null) return showJsonError(model, "订单异常！");
		return showJsonSuccess(model, orderid+"");
	}
	@RequestMapping("/exchange/pubsale/join.shtml")
	public String join(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Integer price, Long sid, ModelMap model) {
		if(price == null) return showJsonError(model, "参数错误！");
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		PubSale sale = daoService.getObject(PubSale.class, sid);
		if(sale == null || sale.isClose()) return showJsonError(model, "竞拍不存在或被删除！");
		if(sale.isEnd(cur)) return showJsonError(model, "竞拍已结束！");
		if(Status.Y.equals(sale.getStatus())) return showJsonError(model, "竞拍已结束！");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError_NOT_LOGIN(model);
		if(!member.isBindMobile()){
			return showJsonError(model, "mobile");
		}
		if(sale.isSoon()) return showJsonError(model, "readly");
		if(sale.isJoin()){
			for(int i=0;i<3;i++){
				try {
					ErrorCode<Map> code = pubSaleService.joinPubSale(sale.getId(), member, price, cur);
					if(!code.isSuccess()) return showJsonError(model, code.getMsg());
					Map map = code.getRetval();
					if(map.containsKey("success")){
						return showJsonSuccess(model, map);
					}
				} catch (HibernateOptimisticLockingFailureException e) {
					dbLogger.warn(StringUtil.getExceptionTrace(e));
					if(i==2) return showJsonError(model, "竞拍失败，请重试！");
				}
			}
		}
		
		return showJsonError(model, "出价失败，最新价格已更改为“" + sale.gainRprice(sale.getCurprice()) + "元”！");
	}
	@RequestMapping("/exchange/pubsale/pmList.xhtml")
	public String pmList(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long sid, ModelMap model) {
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		PubSale sale = daoService.getObject(PubSale.class, sid);
		
		Integer count = pubSaleService.getPubMemberCount(sale.getId(), DateUtil.addMinute(curtime, -30));
		List<PubMember> pmList = pubSaleService.getPubMemberList(sid, null, 0, 6);
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		List<PubMember> palist = new ArrayList<PubMember>();
		List<PubMember> mypmList = new ArrayList<PubMember>();
		if(member!=null){
			mypmList = pubSaleService.getPubMemberList(sale.getId(), member.getId(), 0, 6);
			palist.addAll(mypmList);
		}
		palist.addAll(pmList);
		Map<Long, Member> memberMap = new HashMap<Long, Member>();
		for(PubMember pm : palist){
			if(!memberMap.containsKey(pm.getMemberid())){
				memberMap.put(pm.getMemberid(), daoService.getObject(Member.class, pm.getMemberid()));
			}
		}
		model.put("sale", sale);
		model.put("count", count);
		model.put("pmList", pmList);
		model.put("mypmList", mypmList);
		model.put("memberMap", memberMap);
		return "exchange/pubsale/pmList.vm";
	}
	@RequestMapping("/exchange/pubsale/walaList.xhtml")
	public String wala(Long sid, ModelMap model) {
		PubSale sale = daoService.getObject(PubSale.class, sid);
		String searchkey = sale.getName();
		Integer count = commentService.searchCommentCount(searchkey, null);
		List<Comment> commentList = commentService.searchCommentList(searchkey, null, 0, 10);
		model.put("sale", sale);
		model.put("count", count);
		model.put("commentList", commentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		return "exchange/pubsale/wala.vm";
	}
	
	@RequestMapping("/exchange/pubsale/ordertime.xhtml")
	@ResponseBody
	public String time(Long tid){
		Long valid = orderQueryService.getOrderValidTimeById(tid);
		if(valid!=null){
			Long cur = System.currentTimeMillis();
			Long remain = valid - cur;
			return ""+remain;
		}
		return null;
	}
	//竞拍加入我的生活
	@RequestMapping("/exchange/pubsale/pubSaleAgenda.xhtml")
	public String addAgenda(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long sid, Long aid, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member==null) {
			return gotoLogin("/exchange/pubsale/pubSaleAgenda.xhtml", request, model);
		}
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(aid);
		if(!code.isSuccess()) return show404(model, "参数错误，竞拍数据不存在！");
		RemoteActivity activity = code.getRetval();
		PubSale psale = daoService.getObject(PubSale.class, sid);
		if(psale==null || psale.isClose()) return show404(model, "参数错误，竞拍数据不存在！");
		if(!psale.isSoon()) return show404(model, "抢票已经开始了……");
		//if(PubSale.STATUS_Y.equals(psale.getStatus())) return show404(model, "竞拍已结束，不能添加生活");
		Agenda agenda = agendaService.getAgendaByAction(psale.getId(), TagConstant.AGENDA_ACTION_PUBSALE, member.getId());
		if(agenda != null){
			Map jsonMap = new HashMap();
			Map errorMap = new HashMap();
			errorMap.put("error", "你已设置过生活安排！");
			jsonMap.put("errorMap", errorMap);
			return showJsonError(model, jsonMap);
		}
		agenda = agendaService.addActivityAgenda(activity, member);
		Map dataMap = new HashMap();
		dataMap.put("agendaid", agenda.getId());
		return showJsonSuccess(model, dataMap);
	}
	@RequestMapping("/exchange/pubsale/oldPubSaleAgenda.xhtml")
	public String oldAddAgenda(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long sid, Long aid, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(aid);
		if(!code.isSuccess()) {
			return show404(model, "参数错误，竞拍数据不存在！");
		}
		RemoteActivity activity = code.getRetval();
		if(member==null) return gotoLogin("/exchange/pubsale/pubSaleAgenda.xhtml", request, model);
		PubSale psale = daoService.getObject(PubSale.class, sid);
		if(psale==null || psale.isClose()) return show404(model, "参数错误，竞拍数据不存在！");
		if(!psale.isSoon()) return show404(model, "抢票已经开始了……");
		//if(PubSale.STATUS_Y.equals(psale.getStatus())) return show404(model, "竞拍已结束，不能添加生活");
		Agenda agenda = agendaService.getAgendaByAction(psale.getId(), TagConstant.AGENDA_ACTION_PUBSALE, member.getId());
		if(agenda != null){
			Map jsonMap = new HashMap();
			Map errorMap = new HashMap();
			errorMap.put("error", "你已设置过生活安排！");
			jsonMap.put("errorMap", errorMap);
			return showJsonError(model, jsonMap);
		}
		agenda = agendaService.addActivityAgenda(activity, member);
		model.put("agendaid", agenda.getId());
		return "redirect:/home/agenda/addAgenda.xhtml";
	}
	@RequestMapping("/exchange/subject/price5Agenda.xhtml")
	public String addPirce5Agenda(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long sid,Long aid, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member==null) return showJsonError(model, "请先登录！");
		String opkey = OperationService.TAG_SUBJECTAGENDA + member.getId();
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(aid);
		if(!code.isSuccess()) {
			return showJsonError(model, "参数错误，5元抢票活动不存在！");
		}
		GrabTicketSubject subject = daoService.getObject(GrabTicketSubject.class, sid);
		if(subject == null) return showJsonError(model, "参数错误，5元抢票活动不存在！");
		if(StringUtils.equals(subject.getStatus(), GrabTicketSubject.STATUS_STOP)) return showJsonError(model, "本期5元抢票活动已结束！");
		if(subject.getStarttime().before(DateUtil.currentTime()))return showJsonError(model, "5元抢票已开始，不能添加生活");
		boolean allow = operationService.isAllowOperation(opkey, 5, OperationService.ONE_DAY, 1);
		if(!allow) {
			Map jsonMap = new HashMap();
			Map errorMap = new HashMap();
			errorMap.put("memberid", member.getId());
			jsonMap.put("errorMap", errorMap);
			return showJsonError(model, jsonMap); 
		}
		Agenda agenda = agendaService.getAgendaByAction(subject.getId(), TagConstant.AGENDA_ACTION_PRICE5, member.getId());
		if(agenda != null){
			Map jsonMap = new HashMap();
			Map errorMap = new HashMap();
			errorMap.put("error", "你已设置过生活安排！");
			jsonMap.put("errorMap", errorMap);
			return showJsonError(model, jsonMap);
		}
		RemoteActivity activity = code.getRetval();
		agenda = agendaService.addActivityAgenda(activity, member);
		Map dataMap = new HashMap();
		dataMap.put("agendaid", agenda.getId());
		return showJsonSuccess(model, dataMap);
	}
	@RequestMapping("/exchange/activity/activityAgenda.xhtml")
	public String addActivityAgenda(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, @RequestParam("aid")Long aid, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member==null) return showJsonError(model, "请先登录！");
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(aid);
		if(!code.isSuccess()) {
			return showJsonError(model, "参数错误，活动不存在！");
		}
		RemoteActivity activity = code.getRetval();
		if(activity.isOver()) return showJsonError(model, "此活动已结束！");
		if(activity.isStart())return showJsonError(model, "活动已开始，不能添加生活");
		String action = TagConstant.AGENDA_ACTION_JOIN_ACTIVITY;
		if(StringUtils.equals(activity.getSign(), RemoteActivity.SIGN_RESERVE)) action = TagConstant.AGENDA_ACTION_JOIN_RESERVE;
		Agenda agenda = agendaService.getAgendaByAction(activity.getId(), action, member.getId());
		if(agenda != null){
			Map jsonMap = new HashMap();
			Map errorMap = new HashMap();
			errorMap.put("error", "你已设置过生活安排！");
			jsonMap.put("errorMap", errorMap);
			return showJsonError(model, jsonMap);
		}
		agenda = agendaService.addActivityAgenda(activity, member);
		Map dataMap = new HashMap();
		dataMap.put("agendaid", agenda.getId());
		return showJsonSuccess(model, dataMap);
	}
	
	@RequestMapping("/exchange/subject/saveAgenda.xhtml")
	public String savePirce5Agenda(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, String agendaid, String mobile, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showError_NOT_LOGIN(model);
		String opkey = OperationService.TAG_SUBJECTAGENDA + member.getId();
		Agenda agenda = daoService.getObject(Agenda.class, new Long(agendaid));
		if(agenda == null) return showJsonError(model, "该生活数据不存在!");
		//对自己发送手机号码
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号格式不正确");
		boolean allow = operationService.updateOperation(opkey, 5, OperationService.ONE_DAY, 1);
		if(!allow) return showJsonError(model, "操作过于频繁！");
		String msgContent = "";
		Object relate = null;
		msgContent += "你安排了:";
		if(agenda.getStartdate()!=null)
			msgContent += DateUtil.format(agenda.getStartdate(), "MM月dd日");
		if(StringUtils.isNotBlank(agenda.getStarttime()))
			msgContent += agenda.getStarttime();
		if(agenda.getRelatedid() != null){
			relate = relateService.getRelatedObject(agenda.getTag(), agenda.getRelatedid());
			msgContent += " 在"+ BeanUtil.get(relate, "name");
		}
		if(StringUtils.isNotBlank(agenda.getTitle())){
			msgContent += " "+agenda.getTitle();
		}
		//字数超过60个字，自动截取60个字
		if(msgContent.length()>60){
			msgContent = msgContent.substring(0, 60);
		}
		Map dataMap = new HashMap();
		dataMap.put("memberid", member.getId());
		return showJsonSuccess(model, dataMap);
	}
}