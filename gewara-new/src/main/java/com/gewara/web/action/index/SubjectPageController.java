package com.gewara.web.action.index;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.GrabTicketMpi;
import com.gewara.model.movie.GrabTicketSubject;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.SpecialActivity;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.movie.FilmFestService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.activity.RemoteActivity;


@Controller
public class SubjectPageController extends AnnotationController {
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	@Autowired@Qualifier("filmFestService")
	private FilmFestService filmFestService;
	
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;

	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	//TODO:还用？？
	@RequestMapping("/subject/price5/member.xhtml")
	public String price5History(ModelMap model, @RequestParam("mpid")String mpid) {
		List<String> mpidList = Arrays.asList(mpid.split(","));
		DetachedCriteria query = null;
		Map<Long, Member> memberMap = new HashMap<Long, Member>();
		Map<Long, String> cinemaMap = new HashMap<Long, String>();
		Map<Long, String> roomMap = new HashMap<Long, String>();
		List<TicketOrder> orderList = new ArrayList();
		for (String mid : mpidList) {
			query = DetachedCriteria.forClass(TicketOrder.class);
			query.add(Restrictions.eq("mpid", new Long(mid)));
			query.add(Restrictions.like("status", OrderConstant.STATUS_PAID, MatchMode.START));
			List<TicketOrder> tempList = orderQueryService.getTicketOrderListByMpid(new Long(mid), OrderConstant.STATUS_PAID);
			orderList.addAll(tempList);
		}
		for (TicketOrder order : orderList) { 
			//TODO:优化：需要这么多数据？？
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
			Member member = daoService.getObject(Member.class, order.getMemberid());
			Cinema cinema = daoService.getObject(Cinema.class, order.getCinemaid());
			if (cinema != null)
				cinemaMap.put(order.getId(), cinema.getRealBriefname());
			if(opi!=null)
				roomMap.put(order.getId(), opi.getRoomname());
			memberMap.put(order.getId(), member);
		}
		model.put("memberMap", memberMap);
		model.put("roomMap", roomMap);
		model.put("cinemaMap", cinemaMap);
		model.put("orderList", orderList);
		return "subject/price5/member.vm";
	}
	//TODO:还用？？
	@RequestMapping("/subject/price5.xhtml")
	public String price5(ModelMap model, Long sid, Long pid, String ck) {
		return newPrice5(model, sid, pid, ck);
	}
 	//新五元抢票
	@RequestMapping("/subject/qiangpiao.xhtml")
	public String newPrice5(ModelMap model, Long sid, Long pid, String ck) {
		if(sid==null) {
			DetachedCriteria query = DetachedCriteria.forClass(GrabTicketSubject.class);
			query.setProjection(Projections.max("id"));
			query.add(Restrictions.isNotNull("updatetime"));
			List<Long> idList = hibernateTemplate.findByCriteria(query);
			if(idList.get(0)==null) return showMessage(model, "没有查到5元抢票信息");
			sid = idList.get(0);
		}
		if(pid != null){ //partnerid //douban: b0e4133e
			ApiUser partner = daoService.getObject(ApiUser.class, pid);
			if(partner!=null){
				String chk = StringUtil.md5(partner.getId() + partner.getPrivatekey(), 8);
				if(chk.equals(ck)) model.put("partner", partner);
			}
		}
		GrabTicketSubject subject = daoService.getObject(GrabTicketSubject.class, sid);
		if(subject==null) return forwardMessage(model, "记录不存在！");
		model.put("subject", subject);
		if(subject.getMovieid()!=null){
			Movie movie = daoService.getObject(Movie.class, subject.getMovieid());
			if(movie != null) {
				model.put("movie", movie);
				model.put("markCount", markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movie.getId()));
			}
			
		}
		if(StringUtils.equals(subject.getTag(), GrabTicketSubject.TAG_PRICE5)&&StringUtils.equals(subject.getStatus(),GrabTicketSubject.STATUS_STOP)){
			DetachedCriteria query = DetachedCriteria.forClass(GrabTicketMpi.class, "gtm");
			query.add(Restrictions.eq("gtm.sid", sid));
			List<GrabTicketMpi> gtmList = hibernateTemplate.findByCriteria(query);
			if(!gtmList.isEmpty()){
				List<Long> mpiIdList = BeanUtil.getBeanPropertyList(gtmList, Long.class, "mpid", true);
				DetachedCriteria queryOrder =  DetachedCriteria.forClass(TicketOrder.class, "t");
				queryOrder.add(Restrictions.eq("t.status", OrderConstant.STATUS_PAID_SUCCESS));
				queryOrder.add(Restrictions.or(Restrictions.isNull("t.unitprice"), Restrictions.eq("t.totalfee", 10)));
				queryOrder.add(Restrictions.in("t.mpid", mpiIdList));
				queryOrder.addOrder(Order.desc("t.addtime"));
				List<TicketOrder> orderList = hibernateTemplate.findByCriteria(queryOrder);
				model.put("orderList", orderList);
			}
		}else{
			DetachedCriteria queryOpi = DetachedCriteria.forClass(OpenPlayItem.class, "opi");
			DetachedCriteria query = DetachedCriteria.forClass(GrabTicketMpi.class, "gtm");
			query.add(Restrictions.eq("gtm.sid", sid));
			query.add(Restrictions.eqProperty("gtm.mpid", "opi.mpid"));
			query.setProjection(Projections.property("mpid"));
			queryOpi.add(Subqueries.exists(query));
			queryOpi.add(Restrictions.gt("opi.closetime", new Timestamp(System.currentTimeMillis())));
			queryOpi.addOrder(Order.asc("opi.playtime"));
			List<OpenPlayItem> opiList = hibernateTemplate.findByCriteria(queryOpi);
			
			List<Movie> movieList = new ArrayList<Movie>();
			Map<Long/*movieid*/, List<OpenPlayItem>> movieOpiMap = BeanUtil.groupBeanList(opiList, "movieid");
			List<Map.Entry<Long, List<OpenPlayItem>>> movieOpiList = new ArrayList<Map.Entry<Long, List<OpenPlayItem>>>(movieOpiMap.entrySet());
			Collections.sort(movieOpiList, new ListSizeComparator());
			for(Map.Entry<Long, List<OpenPlayItem>> entry : movieOpiList){
				Movie movie = daoService.getObject(Movie.class, entry.getKey());
				movieList.add(movie);
			}
			
			Map<Long, Cinema> cinemaMap = daoService.getObjectMap(Cinema.class, 
					BeanUtil.getBeanPropertyList(opiList, Long.class, "cinemaid", true));
			model.put("cinemaMap", cinemaMap);
			model.put("movieOpiList", movieOpiList);
			model.put("movieOpiMap", movieOpiMap);
			model.put("movieList", movieList);
			if(StringUtils.equals(subject.getTag(), GrabTicketSubject.TAG_PROMOTION)){
				Map<Long, String> linkMap = new HashMap<Long, String>();
				for(OpenPlayItem item : opiList){
					GrabTicketMpi grab = daoService.getObjectByUkey(GrabTicketMpi.class, "mpid", item.getMpid(), true);
					if(grab!=null)linkMap.put(item.getId(), grab.getLink());
				}
				model.put("linkMap", linkMap);
				return "subject/price5/promotionActivity.vm";
			}else if(StringUtils.equals(subject.getTag(), GrabTicketSubject.TAG_XPRICE)){ //其他价格抢票
				return "subject/price5/xprice.vm";
			}
		}
		//修改成活动首页的热门活动
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getTopActivityList(subject.getCitycode(), null, null, null);
		if(code.isSuccess()) model.put("activityList", code.getRetval());
		DetachedCriteria hql = DetachedCriteria.forClass(GrabTicketSubject.class);
		hql.add(Restrictions.eq("tag", "price5"));
		hql.addOrder(Order.desc("addtime"));
		Map<Long, Movie> movieMap = new HashMap<Long, Movie>();
		List<GrabTicketSubject> subjectList = commonService.getGrabTicketSubjectList(subject.getCitycode(), "price5", 0, 7);
		subjectList.remove(subject);
		if(subjectList.size()>6) subjectList = subjectList.subList(0, 6);
		for (GrabTicketSubject grabTicketSubject : subjectList) {
			movieMap.put(grabTicketSubject.getId(), daoService.getObject(Movie.class, grabTicketSubject.getMovieid()));
		}
		model.put("subjectList", subjectList);
		model.put("movieMap", movieMap);
		model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		
		return "subject/price5/new_index.vm";
	}
	
	@RequestMapping("/subject/time.xhtml")
	@ResponseBody
	public String time(Long sid, String time){
		String key = "SUBJECT_VALIDTIME_" + sid + time;
		Long valid = (Long) cacheService.get(CacheConstant.REGION_HALFHOUR, key);
		if(valid==null){
			GrabTicketSubject grab = daoService.getObject(GrabTicketSubject.class, sid);
			if(grab != null){
				valid = grab.getStarttime().getTime();
				cacheService.set(CacheConstant.REGION_HALFHOUR, key, valid);
			}
		}
		if(valid!=null){
			Long cur = System.currentTimeMillis();
			Long remain = valid - cur;
			return ""+remain;
		}
		return "";
	}
	
	@RequestMapping("/subject/march/index.xhtml")
	public String marchIndex(ModelMap model, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		SpecialActivity sa = filmFestService.getSpecialActivity("march");
		List<GewaCommend> spnewsList = commonService.getGewaCommendList(citycode, "sp" + sa.getId(), null, "news", true, 0, 5);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		commonService.initGewaCommendList("gcSpecialNewsList", rh, spnewsList);
		model.put("gcSpecialNewsList", spnewsList);
		return "subject/march/index.vm";
	}
	@RequestMapping("/subject/march/easy.xhtml")
	public String marchEasy(){
		return "subject/march/easy.vm";
	}
	@RequestMapping("/subject/march/movie.xhtml")
	public String marchMovie(){
		return "subject/march/movie.vm";
	}
	@RequestMapping("/subject/march/activity.xhtml")
	public String marchActivity(){
		return "subject/march/activity.vm";
	}
	@RequestMapping("/subject/march/try.xhtml")
	public String marchTry(){
		return "subject/march/try.vm";
	}
	
	class ListSizeComparator implements Comparator<Map.Entry<Long, List<OpenPlayItem>>> {
		public int compare(Map.Entry<Long, List<OpenPlayItem>> arg0, Map.Entry<Long, List<OpenPlayItem>> arg1) {
			return (arg1.getValue().size() - arg0.getValue().size());
		}
	}
	@RequestMapping("/subject/compareTime.xhtml")
	@ResponseBody
	public String hoursChangeMinutes(){
		Date date = new Date();
		Long cur = System.currentTimeMillis();
		int d = Integer.valueOf(DateUtil.format(date, "dd"));
		int h = Integer.valueOf(DateUtil.format(date, "HH"));
		int m = Integer.valueOf(DateUtil.format(date, "mm"));
		int s = Integer.valueOf(DateUtil.format(date, "ss"));
		int y = Integer.valueOf(DateUtil.format(date, "yy"));
		long remain = h*60*60*1000 + m*60*1000 + s*1000;
		return cur+"-"+remain+"-"+d+'-'+y;
	}
	@RequestMapping("/subject/chinapay/chinapayActivity.xhtml")
	public String getBankPay(){
		return "subject/partner/chinapayActivity.vm";
	}
	@RequestMapping("/subject/chinapay/summerActivity.xhtml")
	public String summerActivity(ModelMap model) {
		GrabTicketSubject subject = daoService.getObject(GrabTicketSubject.class, 39058493L);
		model.put("subject", subject);
		return "subject/partner/summerActivity.vm";
	}
	@RequestMapping("/subject/unionpay/unionpaySubject.xhtml")
	public String unionpaySubject(HttpServletRequest request, HttpServletResponse response, ModelMap model){
		String citycode = WebUtils.getAndSetDefault(request, response);
		model.put("citycode", citycode);
		return "subject/unionpay/index.vm";
	}
	/**
	 * 农行活动主题跳转
	 * @return
	 */
	@RequestMapping("/subject/abcChina/abcChinaSubject.xhtml")
	public String abcChinaSubject(ModelMap model, @CookieValue(value = LOGIN_COOKIE_NAME, required = false)
		String sessid, HttpServletRequest request){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member != null){
			MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
			model.put("account", daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false));
			model.put("isABCBank", StringUtils.equals(memberInfo.getRegfrom(),"ABCBANK"));
			model.put("logonMember", member);
		}
		return "subject/abcchina/index.vm";
	}
	/**
	 * 农行用户登录定制
	 * @param request
	 * @param response
	 * @param username
	 * @param password
	 * @param captchaId
	 * @param captcha
	 * @param model
	 * @return
	 */
	@RequestMapping("/ajax/abcchina/asynchLogin.dhtml")
	public String asynchLogin(HttpServletRequest request, HttpServletResponse response, String username, String password, ModelMap model) {
		if(!ValidateUtil.isMobile(username)) {
			Map jsonMap = new HashMap();
			jsonMap.put("errorMap", "手机格式不正确！");
			return showJsonError(model, jsonMap);
		}
		Member member = daoService.getObjectByUkey(Member.class, "mobile", username, false);
		if(member == null){
			Map jsonMap = new HashMap();
			jsonMap.put("errorMap", "用户名不存在！");
			return showJsonError(model, jsonMap);
		}
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		if(!StringUtils.equals(memberInfo.getRegfrom(),"ABCBANK")){
			Map jsonMap = new HashMap();
			jsonMap.put("errorMap", "notABCBANK");
			return showJsonError(model, jsonMap);
		}
		ErrorCode<Map> code = loginService.autoLogin(request, response, username, password);
		if (code.isSuccess()) {
			return showJsonSuccess(model, code.getRetval());
		} else {
			Map jsonMap = new HashMap();
			jsonMap.put("errorMap", code.getRetval());
			return showJsonError(model, jsonMap);
		}
	}
}

