package com.gewara.web.action.api;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.ui.ModelMap;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.app.AppConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.json.AppSourceCount;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.common.BaseEntity;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.mongo.MongoService;
import com.gewara.service.DaoService;
import com.gewara.service.OperationService;
import com.gewara.service.PlaceService;
import com.gewara.service.api.ApiMobileService;
import com.gewara.service.api.ApiService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.content.PictureService;
import com.gewara.service.member.MemberService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.GewaMailService;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.RelateService;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.OrderMonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.util.MarkHelper;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.XmlUtils;
import com.gewara.web.filter.ApiAuthenticationFilter;
import com.gewara.web.support.GewaVelocityView;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;
/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public abstract class BaseApiController {
	public static final String SUCCESS="success";
	public static final String FAIL="fail";
	
	protected final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	@Autowired@Qualifier("gewaMailService")
	protected GewaMailService gewaMailService;
	public void setGewaMailService(GewaMailService gewaMailService) {
		this.gewaMailService = gewaMailService;
	}
	@Autowired@Qualifier("untransService")
	protected UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	@Autowired@Qualifier("orderMonitorService")
	protected OrderMonitorService orderMonitorService;
	@Autowired@Qualifier("monitorService")
	protected MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	@Autowired@Qualifier("hibernateTemplate")
	protected HibernateTemplate hibernateTemplate;
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	@Autowired@Qualifier("daoService")
	protected DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	@Autowired@Qualifier("pictureService")
	protected PictureService pictureService;
	public void setPictureService(PictureService pictureService) {
		this.pictureService = pictureService;
	}
	@Autowired@Qualifier("apiService")
	protected ApiService apiService;
	public void setApiService(ApiService apiService) {
		this.apiService = apiService;
	}
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	public void setVelocityTemplate(VelocityTemplate velocityTemplate) {
		this.velocityTemplate = velocityTemplate;
	}
	@Autowired@Qualifier("placeService")
	protected PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}
	@Autowired@Qualifier("mongoService")
	protected MongoService mongoService;
	@Autowired@Qualifier("nosqlService")
	protected NosqlService nosqlService;
	@Autowired@Qualifier("memberService")
	protected MemberService memberService;
	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
	@Autowired@Qualifier("relateService")
	protected RelateService relateService;
	public void setRelateService(RelateService relateService) {
		this.relateService = relateService;
	}
	@Autowired@Qualifier("commentService")
	protected CommentService commentService;

	@Autowired@Qualifier("markService")
	protected MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@Autowired@Qualifier("apiMobileService")
	protected ApiMobileService apiMobileService;
	public void setApiMobileService(ApiMobileService apiMobileService) {
		this.apiMobileService = apiMobileService;
	}
	@Autowired@Qualifier("operationService")
	protected OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@Autowired@Qualifier("ticketOrderService")
	protected TicketOrderService ticketOrderService;
	public void setTicketOrderService(TicketOrderService ticketOrderService) {
		this.ticketOrderService = ticketOrderService;
	}
	
	protected String getXmlView(ModelMap model, String view){
		String result = velocityTemplate.parseTemplate(view, model);
		model.put(GewaVelocityView.RENDER_XML, "true");
		model.put(GewaVelocityView.KEY_IGNORE_TOOLS, "true");
		result = XmlUtils.formatXml(XmlUtils.filterInvalid(result), "utf-8");
		model.put("result", result);
		return "api/result.vm";
	}
	@Deprecated
	protected String getDirectXmlView(ModelMap model, String result){
		model.put(GewaVelocityView.RENDER_XML, "true");
		model.put(GewaVelocityView.KEY_IGNORE_TOOLS, "true");
		result = XmlUtils.formatXml(result, "utf-8");
		model.put("result", result);
		return "api/result.vm";
	}
	protected String getSingleResultXmlView(ModelMap model, boolean result){
		return getSingleResultXmlView(model, String.valueOf(result));
	}
	
	protected String getSingleResultXmlView(ModelMap model, long result){
		return getSingleResultXmlView(model, String.valueOf(result));
	}
	
	protected String getSingleResultXmlView(ModelMap model, int result){
		return getSingleResultXmlView(model, String.valueOf(result));
	}
	
	protected String getSingleResultXmlView(ModelMap model, String result){
		model.put(GewaVelocityView.RENDER_XML, "true");
		model.put(GewaVelocityView.KEY_IGNORE_TOOLS, "true");
		model.put("result", result);
		return "api/singleResult.vm";
	}
	
	/**
	 * 结果消息视图返回
	 * @param model
	 * @param resultType 结果
	 * @param msg 消息
	 * @return
	 */
	protected String getMsgResult(ModelMap model,String resultType,String msg){
		model.put("result", resultType);
		model.put("msg", msg);
		String result = velocityTemplate.parseTemplate("/api/mobile/resultWithMsg.vm", model);
		model.put(GewaVelocityView.RENDER_XML, "true");
		model.put(GewaVelocityView.KEY_IGNORE_TOOLS, "true");
		result = XmlUtils.formatXml(XmlUtils.filterInvalid(result), "utf-8");
		model.put("result", result);
		return "api/result.vm";
	}
	
	protected String getErrorXmlView(ModelMap model, String errorcode, String msg){
		model.put("errmsg", msg);
		model.put("errcode", errorcode);
		model.put(GewaVelocityView.RENDER_XML, "true");
		model.put(GewaVelocityView.KEY_IGNORE_TOOLS, "true");
		return "api/error.vm";
	}
	
	protected ApiAuth checkRights(String encryptCode, String... checkStrList){
		ApiAuth apiAuth = ApiAuthenticationFilter.getApiAuth();
		String check = StringUtil.md5(StringUtils.join(checkStrList, "")+apiAuth.getApiUser().getPrivatekey());
		if(check.equalsIgnoreCase(encryptCode)){
			apiAuth.setChecked(true);
		}else{
			apiAuth.setCode(ApiConstant.CODE_SIGN_ERROR);
			apiAuth.setMsg("校验错误！");
			apiAuth.setChecked(false);
		}
		return apiAuth;
	}
	
	protected void addCacheMember(ModelMap model, List<Long> memberidList) {
		Map<Long, Map> cacheMemberMap = (Map<Long, Map>) model.get("cacheMemberMap");
		if(cacheMemberMap==null){
			cacheMemberMap = new HashMap<Long, Map>();
			model.put("cacheMemberMap", cacheMemberMap);
		}
		Set<Long> idSet = new HashSet<Long>(memberidList);
		idSet.removeAll(cacheMemberMap.keySet());
		Map<Long, Map> newinfoMap = memberService.getCacheMemberInfoMap(idSet);
		cacheMemberMap.putAll(newinfoMap);
	}
	protected void addCacheMember(ModelMap model, Long memberid) {
		Map<Long, Map> cacheMemberMap = (Map<Long, Map>) model.get("cacheMemberMap");
		if(cacheMemberMap==null){
			cacheMemberMap = new HashMap<Long, Map>();
			model.put("cacheMemberMap", cacheMemberMap);
		}
		Map singleInfo = memberService.getCacheMemberInfoMap(memberid);
		cacheMemberMap.put(memberid, singleInfo);
	}

	protected ApiAuth check(String key1,String encryptCode, HttpServletRequest request){
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_API, "key:"+key1+",encryptCode:"+encryptCode);
		Map<String,String[]> params = request.getParameterMap();
		List<String> keyList = new ArrayList(params.keySet());
		Collections.sort(keyList);
		StringBuilder sb = new StringBuilder();
		for (String key : keyList) {
			if("key".equals(key) || "encryptCode".equals(key))continue;
			sb.append("&").append(key).append("=").append(params.get(key)[0]);
		}
		String paramsStr = sb.toString().replaceFirst("&", "");
		ApiAuth apiAuth = ApiAuthenticationFilter.getApiAuth();
		String check = StringUtil.md5(key1+apiAuth.getApiUser().getPrivatekey()+paramsStr);
		if(check.equalsIgnoreCase(encryptCode)){
			apiAuth.setChecked(true);
		}else{
			apiAuth.setCode(ApiConstant.CODE_SIGN_ERROR);
			apiAuth.setMsg("校验错误！");
			apiAuth.setChecked(false);
		}
		return apiAuth;
	}
	protected void logAppSource(HttpServletRequest request, String citycode, Long memberid, Long orderid, String tradeno, Long partnerid, String type, String apptype, String originStr) {
		String appSource = request.getParameter("appSource");
		String newdeviceid = request.getParameter("newdeviceid");
		AppSourceCount asc = new AppSourceCount(appSource);
		BindUtils.bindData(asc, request.getParameterMap());
		if(StringUtils.isNotBlank(appSource)) asc.setAppSource(appSource);
		if(memberid!=null) asc.setMemberid(memberid);
		if(orderid!=null) asc.setOrderid(orderid);
		if(partnerid!=null) asc.setPartnerid(partnerid);
		if(StringUtils.isNotBlank(citycode)) asc.setCitycode(citycode);
		if(StringUtils.isNotBlank(tradeno)) asc.setTradeno(tradeno);
		if(StringUtils.isNotBlank(type)) asc.setType(type);
		if(StringUtils.isNotBlank(apptype)) asc.setApptype(apptype);
		if(StringUtils.isNotBlank(newdeviceid)) asc.setNewdeviceid(newdeviceid);
		if(StringUtils.isNotBlank(originStr)) asc.setOrderOrigin(originStr);
		monitorService.addMonitorEntry(AppConstant.TABLE_APPSOURCE, BeanUtil.getSimpleStringMap(asc));
	}
	protected void logAppSource(HttpServletRequest request, String citycode, Long memberid, String type, String apptype) {
		logAppSource(request, citycode, memberid, null,null, null, type, apptype, null);
	}
	protected void logAppSourceOrder(HttpServletRequest request, GewaOrder order, String apptype, String orderOrigin) {
		if(order==null) return;
		logAppSource(request, order.getCitycode(), order.getMemberid(), order.getId(), order.getTradeNo(), order.getPartnerid(), AppSourceCount.TYPE_ORDER, apptype, orderOrigin);
	}
	protected void getCommCommentData(ModelMap model, List<Comment> commentList, String haveface) {
		if(commentList==null || commentList.isEmpty()) return;
		Map<Long,Integer> isBuyMap = new HashMap<Long, Integer>();
		Map<Long, Comment> transferMap = new HashMap<Long, Comment>();
		Map<Long /*哇啦id*/,String/*图片地址*/> picMap = new HashMap<Long, String>();
		Map<Long, Object> relateMap = new HashMap<Long, Object>();
		Map<Long, Object> bodyMap = new HashMap<Long, Object>();
		for(Comment comment: commentList){
			if(StringUtils.isNotBlank(comment.getPicturename()))  picMap.put(comment.getId(), comment.getPicturename());
			if(comment.getTransferid()!=null){
				Comment c = commentService.getCommentById(comment.getTransferid());
				if(null!=c){
					transferMap.put(comment.getId(), c);
					if(StringUtils.isNotBlank(c.getPicturename()))  picMap.put(c.getId(), c.getPicturename());
				}
			}
			if(StringUtils.isNotBlank(comment.getTag()) && comment.getRelatedid()!=null){
				Object relate = relateService.getRelatedObject(comment.getTag(), comment.getRelatedid());
				if(relate!=null) relateMap.put(comment.getId(), relate);
			}
			bodyMap.put(comment.getId(), getCommentBody(comment, haveface));
		}
		model.put("picMap", picMap);
		model.put("isBuyMap", isBuyMap);
		model.put("transferMap", transferMap);
		model.put("relateMap", relateMap);
		model.put("bodyMap", bodyMap);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(transferMap.values()));
		model.put("commentList", commentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
	}
	
	protected String getSimpleHtmlContent(String body, String isSimpleHtml, Integer width, Integer height){
		String content = body;
		if(StringUtils.isNotBlank(content)){
			if(width!=null && height !=null){
				content = content.replaceAll("<img[^>]+src=\"([^\"]+)\"([^>]+)>","<img src=\"$1?w="+width+"&h="+height+"\" />");
			}
			if(StringUtils.equals(isSimpleHtml, Status.Y)){
				content = content.replaceAll(" style=\"[^>]+\"", "");
				content = content.replaceAll(" css=\"[^>]+\"", "");
				content = content.replaceAll("<object.*data=(.*?)[^>]*?</object>", "");
				content = content.replaceAll("<embed.*src=(.*?)[^>]*?</embed>","");
			}else {
				content = VmUtils.getHtmlText(content, 5000);
			}
		}
		return content;
	}
	protected String getCommentBody(Comment comment, String haveface){
		if(comment==null) return "";
		String body = VmUtils.getHtmlText(comment.getBody(), 5000);
		/*if(StringUtils.isNotBlank(body)){
			body = body.replaceAll("#([^(#|\'|\"|\\\\)]+)#", "");
		}*/
		if(StringUtils.isNotBlank(body) && !StringUtils.equals(haveface, "Y")){
			body = body.replaceAll("\\[((0[1-9]|0[1-4][0-9]|05[0-5])|[^\\]])\\]" ,"");
		}
		return body;
	}
	protected Map<Long, String> getMovieMarkMap(List<Movie> movieList) {
		Map<Long, String> markMap = new HashMap<Long, String>();
		for (Movie movie : movieList) {
			markMap.put(movie.getId(), getMovieMark(movie));
		}
		return markMap;
	}

	protected String getMovieMark(Movie movie) {
		MarkCountData markCount = markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movie.getId());
		Integer general = VmUtils.getLastMarkStar(movie, "general", markCount, markService.getMarkdata(TagConstant.TAG_MOVIE));
		return general / 10 + "." + general % 10;
	}
	protected void putRelateMap(List<RemoteActivity> activityList, ModelMap model){
		if(activityList==null || activityList.isEmpty()) return;
		Map<Long, MemberInfo> infoMap = new HashMap<Long, MemberInfo>();
		Map<Long, Object> relateMap = new HashMap<Long, Object>();
		Map<Long, Object> categoryMap = new HashMap<Long, Object>();
		for(RemoteActivity activity : activityList){
			MemberInfo info = daoService.getObject(MemberInfo.class, activity.getMemberid());
			if(info != null) infoMap.put(activity.getId(),info);
			if(activity.getRelatedid() != null && StringUtils.isNotBlank(activity.getTag())){
				Object relate = relateService.getRelatedObject(activity.getTag(), activity.getRelatedid());
				relateMap.put(activity.getId(), relate);
			}
			if(activity.getCategoryid() != null && StringUtils.isNotBlank(activity.getCategory())){
				Object relate = relateService.getRelatedObject(activity.getCategory(), activity.getCategoryid());
				categoryMap.put(activity.getId(), relate);
			}
		}
		model.put("relateMap", relateMap);
		model.put("categoryMap", categoryMap);
		model.put("infoMap", infoMap);
		model.put("activityList", activityList);
	}
	protected String notSupport(ModelMap model){
		return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "本版本不支持，请下载最新版本客户端！");
	}
	protected String getPlaceGeneralmark(BaseEntity bean){
		Integer generalmark = MarkHelper.getSingleMarkStar(bean, "general");
		return generalmark / 10 + "." + generalmark % 10;
	}
	protected String getMovieGeneralmark(BaseEntity bean, Map markData){
		MarkCountData markCount = markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, bean.getId());
		Integer generalmark = MarkHelper.getLastMarkStar(bean, "general", markCount, markData);
		return  generalmark / 10 + "." + generalmark % 10;
	}
	
	protected Map getGeneralmarkMap(Set<BaseEntity> set) {
		Map<Long, String> generalmarkMap = new HashMap<Long, String>();
		Map markData = markService.getMarkdata(TagConstant.TAG_MOVIE);
		for (BaseEntity base : set) {
			if(base instanceof Movie){
				generalmarkMap.put(base.getId(), getMovieGeneralmark(base, markData));
			}else{
				generalmarkMap.put(base.getId(), getPlaceGeneralmark(base));
			}
		}
		return generalmarkMap;
	}
	
	protected ErrorCode validLoginLimit(String ip){
		ErrorCode code = operationService.updateLoginLimitInCache(ip, getLoginLimitNum());
		if(!code.isSuccess()){
			dbLogger.warn("非法登录：" + ip);
			return ErrorCode.getFailure("登录繁忙请稍后再试！");
		}
		return ErrorCode.getSuccess("");
	}
	
	protected ErrorCode validLoginLimitNum(String ip){
		ErrorCode<String> code = operationService.checkLoginLimitNum(ip, getLoginLimitNum());
		if(!code.isSuccess()){
			//monitorService.addSysLog(LogTypeConstant.LOG_TYPE_LOGIN, map);
			return ErrorCode.getFailure(code.getMsg());
		}
		return ErrorCode.getSuccess("");
	}
	
	protected Integer getLoginLimitNum(){
		GewaConfig gcc = daoService.getObject(GewaConfig.class, ConfigConstant.LOGIN_FAIL_LIMIT);
		Integer num = Integer.valueOf(gcc.getContent());
		return num;
	}
	
	protected void sendWarning(final String msg, Member member, String... mobiles){
		if(ArrayUtils.isEmpty(mobiles)) return;
		Timestamp cur = DateUtil.getCurFullTimestamp();
		for (String mobile : mobiles) {
			if(ValidateUtil.isMobile(mobile)){
				String tmp = "你于"+DateUtil.getCurTimeStr()+" 在格瓦拉生活网设置了绑定" + msg + "，如果是你本人操作，请不必理会此短信！";
				SMSRecord sms = new SMSRecord(MemberConstant.ACTION_BINDMOBILE + "_" + member.getId(), mobile, tmp, cur, DateUtil.addHour(cur, 2), SmsConstant.SMSTYPE_NOW);
				untransService.sendMsgAtServer(sms, false);
			}else if(ValidateUtil.isEmail(mobile)){
				String tmp = "你于" + DateUtil.formatTime(cur) + "在格瓦拉生活网设置了绑定" + msg + "，如果是你本人操作，请不必理会此邮件！";
				gewaMailService.sendAdviseEmail(member.getNickname(), tmp, mobile);
			}
		}
	}
}
