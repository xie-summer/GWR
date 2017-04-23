package com.gewara.web.action.api2partner;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.json.GewaSearchKey;
import com.gewara.json.PhoneActivity;
import com.gewara.json.Weixin2Wala;
import com.gewara.json.mobile.WeixinActivity;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.OpenMember;
import com.gewara.service.movie.MCPService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.SearchService;
import com.gewara.untrans.mobile.WeixinService;
import com.gewara.util.ApiUtils;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.util.WeixinUtil;
import com.gewara.xmlbind.partner.WeixinMsg;
import com.gewara.xmlbind.partner.WeixinReply;

@Controller
public class WeixinController extends BaseWeixinController{
	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	
	@Autowired@Qualifier("weixinService")
	private WeixinService weixinService;
	
	@RequestMapping(value="/weixin/apply.xhtml", method=RequestMethod.GET)
	@ResponseBody
	public String apply(String signature, String timestamp, String nonce, String echostr, HttpServletRequest request){
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, WebUtils.getParamStr(request, false));
		if(StringUtils.isNotBlank(signature))dbLogger.warn(weixinService.verify(signature, timestamp, nonce)+"");
		return echostr;
	}
	
	@RequestMapping(value="/weixin/apply.xhtml", method=RequestMethod.POST)
	public String message(String signature, String timestamp, String nonce, HttpServletRequest request, ModelMap model){
		boolean isVerify = weixinService.verify(signature, timestamp, nonce);
		String vm = "partner/weixin/replyText.vm";
		dbLogger.warn("验证：" + isVerify);
		if(!isVerify) return getSingleResultXmlView(model, "false");
		String xml = getXml(request);
		dbLogger.warn("xml:" + xml);
		if(StringUtils.isBlank(xml)){
			getInitContent(model);
			return vm;
		}
		BeanReader beanReader = ApiUtils.getBeanReader("xml", WeixinMsg.class);
		WeixinMsg msg = (WeixinMsg)ApiUtils.xml2Object(beanReader, xml);
		if(msg==null){
			getInitContent(model);
			return vm;
		}
		dbLogger.warn("微信短信：from:" + msg.getFromUserName()+", content:"+msg.getContent());
		WeixinReply reply = new WeixinReply();
		reply.copyMsg(msg, msg.getContent());
		model.put("reply", reply);
		String event = msg.getEvent();
		if(StringUtils.equals(msg.getToUserName(), weixinService.getWxToUser()) && StringUtils.equalsIgnoreCase(event, WeixinUtil.EVENT_SUBSCRIBE)){ //加关注
			return "partner/weixin/event/guanzhu.vm";
		}
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		if(StringUtils.equals(msg.getToUserName(), weixinService.getWxToUser()) && StringUtils.equalsIgnoreCase(event, WeixinUtil.EVENT_UNSUBSCRIBE)){ //取消关注
			OpenMember openMember = memberService.getOpenMemberByLoginname(MemberConstant.SOURCE_WEIXIN, msg.getFromUserName());
			if(openMember!=null){
				openMember.setValidtime(curtime);
				daoService.saveObject(openMember);
			}
		}
		if(StringUtils.isNotBlank(msg.getEventKey())){
			String eventKey = msg.getEventKey();
			if(StringUtils.equals(eventKey, WeixinUtil.KEY_KEFUTEL)){ //客服
				return "partner/weixin/event/kefuTel.vm";
			}else if(StringUtils.equals(eventKey, WeixinUtil.KEY_WEIKEFU)){ //微客服
				return "partner/weixin/event/weiKefu.vm";
			}else if(StringUtils.equals(eventKey, WeixinUtil.KEY_DOWNAPP)){ //下载APP
				return "partner/weixin/event/downApp.vm";
			}else if(StringUtils.equals(eventKey, WeixinUtil.KEY_NEARCINEMA)){ //附近影院
				return "partner/weixin/event/nearCinema.vm";
			}else if(StringUtils.equals(eventKey, WeixinUtil.KEY_DAYWALA) || StringUtils.equals(eventKey, WeixinUtil.KEY_WALAALL)){ //哇啦
				Map<String, Object> params = new HashMap<String, Object>();
				if(StringUtils.equals(eventKey, WeixinUtil.KEY_DAYWALA)){
					params.put("walaType", "everyDay");
				}else {
					params.put("walaType", "noLimit");
				}
				List<Weixin2Wala> walaList = mongoService.getObjectList(Weixin2Wala.class, params, "addTime", false, 0, 1);
				model.put("walaList", walaList);
				return "partner/weixin/event/wala.vm";
			}else if(WeixinUtil.getEventList().contains(eventKey)){
				OpenMember openMember = memberService.getOpenMemberByLoginname(MemberConstant.SOURCE_WEIXIN, msg.getFromUserName());
				if(openMember==null || openMember.getValidtime()==null || openMember.getValidtime().before(DateUtil.getCurFullTimestamp())){
					Timestamp atime = DateUtil.addMinute(curtime, 60);
					String tms = DateUtil.format(atime, "yyyyMMddHHmmss");
					model.put("userid", msg.getFromUserName());
					model.put("tms", tms);
					model.put("wxs", weixinService.getWxs(tms));
					return "partner/weixin/event/auth.vm";
				}else {
					Long memberid = openMember.getMemberid();
					Member member = daoService.getObject(Member.class, memberid);
					model.put("member", member);
					if(StringUtils.equals(eventKey, WeixinUtil.KEY_MYACCOUNT)){
						return getMyAccount(memberid, model);
					}else if(StringUtils.equals(eventKey, WeixinUtil.KEY_TICKETPWD)){
						return getTicketPwd(memberid, model);
					}else if(StringUtils.equals(eventKey, WeixinUtil.KEY_MYORDER)){
						return getMyOrder(memberid, model);
					}else {
						getInitContent(model);
						return vm;
					}
				}
			}else if(StringUtils.equals(eventKey, WeixinUtil.KEY_HOTMOVIE)){
				return getHotMovie(model);
			}else {
				getInitContent(model);
				return vm;
			}
		}else {
			if(StringUtils.contains(msg.getContent(), "@小瓦") 
					|| StringUtils.contains(msg.getContent(), "@咨询") 
					|| StringUtils.contains(msg.getContent(), "@投诉")
					|| StringUtils.contains(msg.getContent(), "@建议")){
				return "partner/weixin/atText.vm";
			}
			if(StringUtils.equals(msg.getContent(), "paytest")){
				model.put("userid", msg.getFromUserName());
				return "partner/weixin/event/paytest.vm";
			}
			return getDefMsg(msg, vm, model, request);
		}
	}
	//我的账户
	private String getMyAccount(Long memberid, ModelMap model){
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, memberid);
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class, "memberid", memberid);
		model.put("account", account);
		model.put("memberInfo", memberInfo);
		return "partner/weixin/event/myAccount.vm";
	}
	//取票密码
	private String getTicketPwd(Long memberid, ModelMap model){
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		Timestamp addtime = DateUtil.addDay(curtime, -6);
		Timestamp playtime = DateUtil.addHour(curtime, -1);
		String hql = "from TicketOrder t where t.memberid=? and t.status=? and t.addtime>? and t.playtime>? order by playtime";
		List<TicketOrder> orderList = hibernateTemplate.find(hql, memberid, OrderConstant.STATUS_PAID_SUCCESS, addtime, playtime);
		List<SMSRecord> smsList = new ArrayList<SMSRecord>();
		for(TicketOrder order : orderList){
			List<SMSRecord> smList = daoService.getObjectListByField(SMSRecord.class, "tradeNo", order.getTradeNo());
			for(SMSRecord sms : smList){
				if(StringUtils.equals(sms.getSmstype(), SmsConstant.SMSTYPE_NOW)){
					smsList.add(sms);
					break;
				}
			}
		}
		model.put("smsList", smsList);
		return "partner/weixin/event/ticketPwd.vm";
	}
	//我的订单
	private String getMyOrder(Long memberid, ModelMap model){
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		Timestamp addtime = DateUtil.addDay(curtime, -30);
		DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class);
		query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.gt("addtime", addtime));
		query.addOrder(Order.desc("addtime"));
		List<TicketOrder> orderList = hibernateTemplate.findByCriteria(query, 0, 5);
		Map<Long, Movie> movieMap = new HashMap<Long, Movie>();
		Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
		for(TicketOrder order : orderList){
			if(!movieMap.containsKey(order.getMovieid())){
				movieMap.put(order.getMovieid(), daoService.getObject(Movie.class, order.getMovieid()));
			}
			if(!opiMap.containsKey(order.getMpid())){
				opiMap.put(order.getMpid(), daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid()));
			}
		}
		model.put("opiMap", opiMap);
		model.put("movieMap", movieMap);
		model.put("orderList", orderList);
		return "partner/weixin/event/myOrder.vm";
	}
	//热门电影
	private String getHotMovie(ModelMap model){
		List<Movie> movieList = new ArrayList<Movie>();
		List<GewaCommend> gcMovieList = commonService.getGewaCommendList(AdminCityContant.CITYCODE_SH, SignName.INDEX_MOVIELIST_NEW, null, null, true, 0, 3);
		for(GewaCommend commend : gcMovieList){
			if(commend.getRelatedid()!=null) {
				Movie movie = daoService.getObject(Movie.class, commend.getRelatedid());
				if(movie!=null){
					movieList.add(movie);
				}
			}
		}
		model.put("movieList", movieList);
		return "partner/weixin/event/hotMovie.vm";
	}
	//非事件处理方式
	private String getDefMsg(WeixinMsg msg, String vm, ModelMap model, HttpServletRequest request){
		List<Movie> movieList = new ArrayList<Movie>();
		List<Cinema> cinemaList = new ArrayList<Cinema>();
		List<WeixinActivity> weixinActivityList = new ArrayList<WeixinActivity>();
		List<PhoneActivity> activityList = new ArrayList<PhoneActivity>();
		boolean isSort = true, isImage = false, isFind = false;
		String content = WeixinUtil.getContent(msg);
		if(StringUtils.length(content)==1 || StringUtils.startsWith(content, "@")){
			if(!WeixinUtil.isValidText(content)){
				return getSingleResultXmlView(model, "false");
			}
			if(ValidateUtil.isNumber(content) || StringUtils.startsWith(content, "@")){ //查询活动的内容
				Map params = new HashMap();
				params.put("replynum", content.replace("@", ""));
				weixinActivityList = mongoService.getObjectList(WeixinActivity.class, params,"rank", true, 0, 5);
				for(WeixinActivity activity : weixinActivityList){
					String[] aids = activity.getActivityid().split(",");
					for(String activityid : aids){
						PhoneActivity pa = mongoService.getObject(PhoneActivity.class, "id", activityid);
						if(pa!=null) activityList.add(pa);
					}
				}
				isImage = true;
			}
		}else {
			if(msg.isText()){
				if(!WeixinUtil.isValidText(content)){
					return getSingleResultXmlView(model, "false");
				}
				String ip = WebUtils.getRemoteIp(request);
				Map<String, Object> map = searchService.searchKey(ip, null, content, TagConstant.TAG_MOVIE, null, null, 0, 5);
				List<GewaSearchKey> skList = (List<GewaSearchKey>) map.get(SearchService.ROWS_SK_LIST);
				if(skList!=null){
					for(GewaSearchKey sk: skList){
						if(StringUtils.equals(sk.getCategory(), TagConstant.TAG_MOVIE)){
							Movie movie = daoService.getObject(Movie.class, sk.getRelatedid());
							if(movie!=null) movieList.add(movie);
						}else if(StringUtils.equals(sk.getTag(), TagConstant.TAG_CINEMA)){
							Cinema cinema = daoService.getObject(Cinema.class, sk.getRelatedid());
							if(cinema!=null) cinemaList.add(cinema);
						}
					}
				}
				
			}else if(msg.isLocation()){
				if(StringUtils.isNotBlank(msg.getLocation_X()) && StringUtils.isNotBlank(msg.getLocation_Y())){
					double pointy = Double.valueOf(msg.getLocation_X());
					double pointx = Double.valueOf(msg.getLocation_Y());
					//TODO 根据城市的中心点
					cinemaList = mcpService.getNearCinemaList(pointx, pointy, 5000, null, AdminCityContant.CITYCODE_SH, null);
					cinemaList = BeanUtil.getSubList(cinemaList, 0, 3);
					isSort = false;
				}
			}else if(msg.isImage()){
				isImage = true;
			}
		}
		if(movieList.size()>0 || cinemaList.size()>0 || weixinActivityList.size()>0){
			Collections.sort(movieList, new PropertyComparator("releasedate", false, false));
			if(isSort)Collections.sort(cinemaList, new PropertyComparator("general", false, false));
			isFind = true;
		}
		model.put("isSort", isSort);
		model.put("isFind", isFind);
		model.put("movieList", movieList);
		model.put("cinemaList", cinemaList);
		model.put("activityList", activityList);
		model.put("weixinActivityList", weixinActivityList);
		model.put("generalmarkMap", getMovieMarkMap(movieList));
		if(!isFind){
			getInitContent(model);
			return vm;
		}
		if(cinemaList.size()>0){
			 return "partner/weixin/event/cinemaList.vm";
		}
		if(isImage) return "partner/weixin/replyPic.vm";
		return vm;
	}
	
	@RequestMapping(value = "/weixin/mobile/appInstalled.xhtml")
	public void appTest(int isappinstalled,  HttpServletResponse response){
		try {
			if(isappinstalled == 0){
				response.sendRedirect("http://m.gewara.com");
			}else if(isappinstalled == 1){
				response.sendRedirect("gewara://open.sport");
			}
		} catch (IOException e) {
		}
	}
	
	
	@RequestMapping(value="/weixin/createMenu.xhtml")
	@ResponseBody
	public String getToken(){
			WeixinUtil weixin = new WeixinUtil();
			String token = "UiLwNdF2e3nbQ9wTUN909Jn9m0hq6FSXd9u4AfsGsXb8dlUvAYPHY7k4mqL4bRDydh3aKbUdJKh2QvDjq7IRwqsDVkhP-wD1b9v4pykDF4OUOP5NnmLic1tOFf36dr1EZCIiHGNwqO8YJwKXzqct1g";
			String body = JsonUtils.writeObjectToJson(weixin.getButtonList());
			ErrorCode<String> wcode = WeixinUtil.createMenu(token, body);
			return "retval=" + wcode.getRetval() + ", msg=" + wcode.getMsg();
	}
}
