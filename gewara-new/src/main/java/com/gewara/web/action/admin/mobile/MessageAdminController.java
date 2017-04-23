package com.gewara.web.action.admin.mobile;

import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.ConfigTag;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.json.PlayItemMessage;
import com.gewara.model.acl.User;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.common.JsonData;
import com.gewara.model.drama.Drama;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.sport.Sport;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.UserMessage;
import com.gewara.model.user.UserMessageAction;
import com.gewara.mongo.MongoService;
import com.gewara.service.JsonDataService;
import com.gewara.service.MessageService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.drama.DramaService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.sport.SportService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.support.FirstLetterComparator;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.LockCallback;
import com.gewara.untrans.LockService;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.mobile.MobileService;
import com.gewara.untrans.monitor.ConfigCenter;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.untrans.monitor.SysLogType;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ObjectId;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Controller
public class MessageAdminController extends BaseAdminController {
	@Autowired@Qualifier("configCenter")
	private ConfigCenter configCenter;
	@Autowired @Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate template) {
		jdbcTemplate = template;
	}
	@Autowired
	@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	@Autowired
	@Qualifier("lockService")
	private LockService lockService;

	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	@Autowired
	@Qualifier("messageService")
	private MessageService messageService;

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	@Autowired
	@Qualifier("userMessageService")
	private UserMessageService userMessageService;

	public void setUserMessageService(UserMessageService userMessageService) {
		this.userMessageService = userMessageService;
	}

	@Autowired
	@Qualifier("jsonDataService")
	private JsonDataService jsonDataService;

	public void setJsonDataService(JsonDataService jsonDataService) {
		this.jsonDataService = jsonDataService;
	}

	@Autowired
	@Qualifier("commentService")
	private CommentService commentService;

	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}

	@Autowired
	@Qualifier("mongoService")
	private  MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}

	@Autowired
	@Qualifier("mcpService")
	private MCPService mcpService;

	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}

	@Autowired
	@Qualifier("dramaService")
	private DramaService dramaService;

	public void setDramaService(DramaService dramaService) {
		this.dramaService = dramaService;
	}

	@Autowired@Qualifier("sportService")
	private SportService sportService;

	public void setSportService(SportService sportService) {
		this.sportService = sportService;
	}
	// 发送手机短信
	@RequestMapping(method = RequestMethod.GET, value = "/admin/message/sendMobileMsg.xhtml")
	public String sendMobileMsg() {
		return "admin/message/sendMobileMsg.vm";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/admin/message/sendMobileMsg.xhtml")
	public String sendMobileMsg(String mobile, String msg, String channel, ModelMap model) {
		User user = getLogonUser();
		SMSRecord sms = messageService.addManualMsg(user.getId(), mobile, msg, null);
		if (StringUtils.equals(MobileService.CHANNEL_MAS, channel) && !ValidateUtil.isYdMobile(mobile))
			return showJsonError(model, "手机号所选择的通道不一致！");
		ErrorCode code = untransService.sendMsgAtServer(sms, channel, false);
		if (code.isSuccess())
			return showJsonSuccess(model, code.getMsg());
		return showJsonError(model, code.getMsg());
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/message/sendBatchMobileMsg.xhtml")
	public String sendBatchMobileMsg() {
		return "admin/message/sendBatchMobileMsg.vm";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/admin/message/sendBatchMobileMsg.xhtml")
	public String sendBatchMobileMsg(String mobile, String msg, String channel, ModelMap model) {
		User user = getLogonUser();
		if (!StringUtils.equals(MobileService.CHANNEL_GEWAMAIL, channel)){
			return showJsonError(model, "选择的通道不正确，只支持GewaMail！");
		}
		
		if(StringUtils.isBlank(mobile)){
			return showJsonError(model, "请输入要发送的手机号！");
		}
		if(StringUtils.isBlank(msg)){
			return showJsonError(model, "请输入短信内容！");
		}
		String[] mobileArr = StringUtils.split(mobile, ",");
		Set<String> mobileSet = new HashSet<String>();
		for (String mobileNo : mobileArr) {
			String tm = mobileNo.trim();
			if (ValidateUtil.isMobile(tm)) mobileSet.add(tm);				
		}
		
		List<String> errMobile = new ArrayList<String>();
		for(String mobileNo : mobileSet){
			SMSRecord sms = messageService.addManualMsg(user.getId(), mobileNo, msg, null);
			ErrorCode code = untransService.sendMsgAtServer(sms, channel, false);
			if(!code.isSuccess()){
				errMobile.add(mobileNo);
			}
		}
		
		if (errMobile.isEmpty()){
			return showJsonSuccess(model, "已全部发送！");
		}else{
			return showJsonError(model, "部分已发送，下列手机号发送失败：\n" + StringUtils.join(errMobile, ","));
		}
			
		
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/admin/message/sendBatchMobileMsg/test.xhtml")
	public String testSendBatchMobileMsg(String mobile, String msg, String channel, ModelMap model) {
		User user = getLogonUser();
		if (!StringUtils.equals(MobileService.CHANNEL_GEWAMAIL, channel)){
			return showJsonError(model, "选择的通道不正确，只支持GewaMail！");
		}
		
		if(StringUtils.isBlank(mobile)){
			return showJsonError(model, "请输入要发送的手机号！");
		}
		if(StringUtils.isBlank(msg)){
			return showJsonError(model, "请输入短信内容！");
		}
		
		SMSRecord sms = messageService.addManualMsg(user.getId(), mobile, msg, null);
		ErrorCode code = untransService.sendMsgAtServer(sms, channel, false);
		
		if (code.isSuccess())
			return showJsonSuccess(model, code.getMsg());
		return showJsonError(model, code.getMsg());		
	}

	@RequestMapping("/admin/message/smsList.xhtml")
	public String smsList(Long relatedid, String tradeNo, String contact, Integer pageNo, String smstype, String status, ModelMap model) {
		if(relatedid==null && StringUtils.isBlank(tradeNo) && StringUtils.isBlank(contact) && StringUtils.isBlank(smstype) && StringUtils.isBlank(status)){
			return "admin/message/smsList.vm";
		}
		DetachedCriteria query = DetachedCriteria.forClass(SMSRecord.class);
		if (StringUtils.isNotBlank(tradeNo)) {
			query.add(Restrictions.like("tradeNo", tradeNo, MatchMode.END));
		}
		if (StringUtils.isNotBlank(status)){
			query.add(Restrictions.like("status", status, MatchMode.START));
			if(StringUtils.startsWith(status, "N")) {
				query.add(Restrictions.ge("sendnum", 1));
			}
		}
		if (relatedid != null) {
			query.add(Restrictions.eq("relatedid", relatedid));
		}
		if (StringUtils.isNotBlank(contact)) {
			query.add(Restrictions.like("contact", contact.trim(), MatchMode.START));
		}
		if (StringUtils.isNotBlank(smstype)) {
			query.add(Restrictions.eq("smstype", smstype));
		}
		if(pageNo==null) pageNo = 0;
		query.addOrder(Order.desc("sendtime"));
		List<SMSRecord> smsList = hibernateTemplate.findByCriteria(query, pageNo * 500, 500);
		model.put("SmsHelper", new SmsConstant());
		model.put("smsList", smsList);
		return "admin/message/smsList.vm";
	}

	@RequestMapping("/admin/message/unSmsRecordOrderList.xhtml")
	public String unSmsRecordOrderList(String ordertype, ModelMap model) {
		model.put("ordertype", ordertype);
		List orderList = new ArrayList<GewaOrder>();
		if (TagConstant.TAG_DRAMA.equals(ordertype)) {
			orderList = messageService.getUnSendDramaOrderList();
		} else if (TagConstant.TAG_SPORT.equals(ordertype)) {
			orderList = messageService.getUnSendSportOrderList();
		} else if (TagConstant.TAG_GYM.equals(ordertype)) {
			orderList = messageService.getUnSendGymOrderList();
		} else {
			orderList = messageService.getUnSendOrderList();
		}
		model.put("orderList", orderList);
		return "admin/message/unSmsRecordOrderList.vm";
	}

	@RequestMapping("/admin/message/qryMobile.xhtml")
	public String qryMobile(ModelMap model) {
		String hql = "from Cinema c where exists(select cp.id from CinemaProfile cp where cp.id=c.id and cp.status=?) order by c.avggeneral desc";
		List<Cinema> cinemaList = hibernateTemplate.find(hql, CinemaProfile.STATUS_OPEN);
		model.put("cinemaList", cinemaList);
		return "admin/message/qryMobile.vm";
	}

	@RequestMapping("/admin/message/getMobile.xhtml")
	public String getMobile(String type, String cinemaid, Long movieid, Long relatedid, Timestamp fromtime, Timestamp totime, ModelMap model) {
		List<Long> cinemaidList = new ArrayList<Long>();
		if (StringUtils.isNotBlank(cinemaid)) {
			for (String cid : cinemaid.split(",")) {
				cinemaidList.add(Long.valueOf(cid));
			}
		}
		String mobileList = messageService.getMobileList(type, cinemaidList, movieid, relatedid, fromtime, totime);
		model.put("mobileList", mobileList);
		if (StringUtils.isNotBlank(mobileList)) {
			model.put("mobileList", mobileList);
			model.put("mobileCount", mobileList.split(",").length);
		}
		return "admin/message/batchMessage.vm";
	}

	/**
	 * author: bob date: 20100728 删除网站公告
	 */
	@RequestMapping("/admin/message/delPM.xhtml")
	public String delPM(Long id, ModelMap model) {
		UserMessageAction userMessageAction = this.daoService.getObject(UserMessageAction.class, id);
		UserMessage userMessage = daoService.getObject(UserMessage.class, userMessageAction.getUsermessageid());
		this.daoService.removeObject(userMessage);
		this.daoService.removeObject(userMessageAction);
		return showJsonSuccess(model, BeanUtil.getBeanMap(userMessage, false));
	}

	/**
	 * author: bob date: 20100728 预加载 修改网站公告
	 */
	@RequestMapping("/admin/message/preModiPM.xhtml")
	public String preModiPM(Long id, ModelMap model) {
		UserMessageAction userMessageAction = this.daoService.getObject(UserMessageAction.class, id);
		UserMessage userMessage = daoService.getObject(UserMessage.class, userMessageAction.getUsermessageid());
		return showJsonSuccess(model, BeanUtil.getBeanMap(userMessage, false));
	}

	/**
	 * author: bob date: 20100810 修改显示状态 (暂时将 isread 设置为是否在首页显示)
	 */
	@RequestMapping("/admin/message/modiPMStatus.xhtml")
	public String modiPMStatus(Long id, ModelMap model) {
		UserMessageAction oldUserMessageAction = this.userMessageService.getPublicNotice();
		if (oldUserMessageAction != null) {
			oldUserMessageAction.setIsread(0);
			this.daoService.saveObject(oldUserMessageAction);
		}
		UserMessageAction userMessageAction = this.daoService.getObject(UserMessageAction.class, id);
		userMessageAction.setIsread(1);
		this.daoService.saveObject(userMessageAction);
		return showJsonSuccess(model);
	}

	@RequestMapping("/message/callback.xhtml")
	@ResponseBody
	public String ytcallback(HttpServletRequest request, String spid, String mtmsgid, String mtstat) {
		String status = "DELIVRD".equals(mtstat) ? SmsConstant.STATUS_Y : SmsConstant.STATUS_N_ERROR;
		if(StringUtils.equals(mtstat, "ET:0265") || StringUtils.equals(mtstat, "ET:0266")){//长短信拆分
			dbLogger.warn("短信超长，拆分多条：" + WebUtils.getParamStr(request, false));
			status = SmsConstant.STATUS_Y_LARGE;
		}
		String update = "update SMSRecord set status=?,validtime=? where seqno=?";
		hibernateTemplate.bulkUpdate(update, status, new Timestamp(System.currentTimeMillis()), mtmsgid);
		String output = "command=RT_RESPONSE&spid=" + spid + "&mtmsgid=" + mtmsgid + "&rtstat=ACCEPT&rterrcode=000";
		dbLogger.warn(status + ", " +output);
		if(StringUtils.equals(status, SmsConstant.STATUS_N_ERROR)){
			Map<String, String> reqMap = WebUtils.getRequestMap(request);
			monitorService.addSysLog(SysLogType.SMSERR, reqMap);
		}
		return output;
	}

	@RequestMapping("/message/reply.xhtml")
	@ResponseBody
	public String reply(String spid, String momsgid, String sa, String dc, String sm, HttpServletRequest request) throws Exception {
		if (StringUtils.isBlank(sm)/* || !StringUtils.equals(spid, MlinkMobileServiceImpl.spid)*/) {
			return "check error";
		}
		String params = WebUtils.getParamStr(request, false);
		dbLogger.warn("mlink reply params: " + params);
		String charset = "UTF-16BE";
		if (StringUtils.equals(dc, "15"))
			charset = "GBK";
		else if (StringUtils.equals(dc, "0"))
			charset = "ISO8859-1";
		String msg = new String(Hex.decodeHex(sm.toCharArray()), charset);
		String mobile = sa;
		if (StringUtils.startsWith(mobile, "86"))
			mobile = mobile.substring(2);
		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("momsgid", momsgid);
		dataMap.put("mobile", mobile);
		dataMap.put("msg", msg);
		dbLogger.warn("mlink reply: " + mobile + "," + msg);
		commentService.addReplyToComment(mobile, msg, WebUtils.getIpAndPort(WebUtils.getRemoteIp(request), request));
		String output = "MO_RESPONSE&spid=" + spid + "&momsgid=" + momsgid + "&mostat=ACCEPT&moerrcode=000";
		return output;
	}

	@RequestMapping("/message/masreply.xhtml")
	@ResponseBody
	public String masreply(String smsidStatus) {
		if (StringUtils.isEmpty(smsidStatus))
			return "parms error";
		Map<String, String> idStatusMap = new HashMap<String, String>();
		String[] idStatusArrs = smsidStatus.split(";");
		String[] idStatusArr = null;
		String id = null;
		String status = null;
		for (String idStatus : idStatusArrs) {
			idStatusArr = idStatus.split(":");
			if (idStatusArr != null && idStatusArr.length == 2) {
				id = idStatusArr[0];
				status = idStatusArr[1];
				idStatusMap.put(id, status);
			} else {
				return "parms error";
			}
		}
		dbLogger.warn(smsidStatus);
		return "success";
	}

	@RequestMapping("/message/masreplyToComment.xhtml")
	@ResponseBody
	public String replyToComment(String key, String content, HttpServletRequest request) throws Exception {
		String sep = "masreply";
		Date date = DateUtil.getBeginningTimeOfDay(new Date());
		String strdate = DateUtil.format(date, "yyyy-MM-dd");
		String result = StringUtil.md5(strdate + sep);
		if (!StringUtils.equals(result, key))
			return "key error";
		String[] messageList = content.split(",");
		dbLogger.warn("mas reply content: " + content);
		for (String message : messageList) {
			try{
				message = URLDecoder.decode(message, "gbk");
				String[] moMsg = message.split(sep);
				if (moMsg.length < 2)
					continue;
				String mobile = moMsg[0];
				if (mobile.startsWith("86"))
					mobile = mobile.substring(2);
				String msg = moMsg[1];
				dbLogger.warn("mas reply: " + mobile + ", " + msg);
				if(StringUtils.isNotBlank(msg)){
					commentService.addReplyToComment(mobile, msg, WebUtils.getIpAndPort(WebUtils.getRemoteIp(request), request));
				}
			}catch (Exception e) {
				dbLogger.error("message: " + message,e);
			}			
		}
		return "success";
	}

	@RequestMapping("/message/masCallBack.xhtml")
	@ResponseBody
	public String masCallBack(String key, String ids) throws Exception {
		String sep = "mascallback";
		Date date = DateUtil.getBeginningTimeOfDay(new Date());
		String strdate = DateUtil.format(date, "yyyy-MM-dd");
		String result = StringUtil.md5(strdate + sep);
		if (!StringUtils.equals(result, key))
			return "key error";
		List<Long> idarr = BeanUtil.getIdList(ids, ",");
		if(idarr.size()>0){
			List paramList = new ArrayList(idarr.size()+1);
			paramList.add(new Timestamp(System.currentTimeMillis()));
			paramList.addAll(idarr);
			String update = "update SMSRecord t set t.status='" + SmsConstant.STATUS_Y + "', validtime=? where t.id in (?"
					+ StringUtils.repeat(",?", idarr.size() - 1) + ")";
			int updated = hibernateTemplate.bulkUpdate(update, paramList.toArray());
			dbLogger.warn("mas callback: " + StringUtils.join(idarr, ",") + ", updated:" + updated);
			return "success";
		}else{
			return "error";
		}
	}

	/**
	 * @function 全局站内信
	 * @author bob.hu
	 * @date 2011-04-28 12:20:24
	 */
	@RequestMapping("/admin/message/websiteMessage.xhtml")
	public String websiteMessage(ModelMap model, Integer pageNo) {
		final int rowsPerPage = 10;
		if (pageNo == null)
			pageNo = 0;
		int firstRow = pageNo * rowsPerPage;
		List<JsonData> list = jsonDataService.getListByTag(JsonDataKey.KEY_WEBSITEMSG, DateUtil.getCurTruncTimestamp(), firstRow, rowsPerPage);
		Integer count = jsonDataService.countListByTag(JsonDataKey.KEY_WEBSITEMSG, DateUtil.getCurTruncTimestamp());
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/message/websiteMessage.xhtml");
		pageUtil.initPageInfo();
		initJsonMap(list, model);
		model.put("list", list);
		model.put("pageUtil", pageUtil);
		return "admin/message/websitemessageAll.vm";
	}

	private void initJsonMap(List<JsonData> list, ModelMap model) {
		Map<String, Map<String, String>> dataMap = new HashMap<String, Map<String, String>>();
		for (JsonData jsonData : list) {
			Map<String, String> map = VmUtils.readJsonToMap(jsonData.getData());
			dataMap.put(jsonData.getDkey(), map);
		}
		model.put("dataMap", dataMap);
	}

	@RequestMapping("/admin/message/saveWebsiteMessage.xhtml")
	public String saveWebsiteMessage(ModelMap model, String dkey, String content, String validtime) {
		JsonData jsonData = null;
		if (StringUtils.isBlank(dkey)) {
			dkey = JsonDataKey.KEY_WEBSITEMSG + System.currentTimeMillis();
			jsonData = new JsonData(dkey);
		} else {
			jsonData = daoService.getObject(JsonData.class, dkey);
		}
		if (StringUtils.isBlank(content))
			return showJsonError(model, "内容不能为空!");
		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("content", content);
		dataMap.put("addtime", DateUtil.format(DateUtil.getCurFullTimestamp(), "yyyy-MM-dd HH:mm"));
		jsonData.setTag(JsonDataKey.KEY_WEBSITEMSG);
		jsonData.setData(JsonUtils.writeMapToJson(dataMap));
		jsonData.setValidtime(DateUtil.parseTimestamp(validtime + " 00:00:00.0"));
		daoService.saveObject(jsonData);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/message/preLoadWebsiteMessage.xhtml")
	public String preLoadWebsiteMessage(String dkey, ModelMap model) {
		JsonData jsonData = daoService.getObject(JsonData.class, dkey);
		if (jsonData != null) {
			Map<String, String> map = VmUtils.readJsonToMap(jsonData.getData());
			map.put("dkey", jsonData.getDkey());
			map.put("validtime", DateUtil.formatDate(jsonData.getValidtime()));
			map.put("content", map.get("content"));
			return showJsonSuccess(model, map);
		}
		return showJsonError_NOT_FOUND(model);
	}

	@RequestMapping("/admin/message/delWebsiteMessage.xhtml")
	public String delWebsiteMessage(String dkey, ModelMap model) {
		JsonData jsonData = daoService.getObject(JsonData.class, dkey);
		// 查找所有Memberinfo 表中有该属性的, 删除.
		List<MemberInfo> list = memberService.getMemberInfoByOtherInfo(dkey);
		updateRelatedList(list, dkey);
		daoService.removeObject(jsonData);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/message/smsChannel.xhtml")
	public String smsChannel(ModelMap model) {
		Map<String, String> tagMap = SmsConstant.typeMap;
		JsonData jsonData = daoService.getObject(JsonData.class, JsonDataKey.KEY_SMSCHANNEL);
		Map<String, String> result = new HashMap<String, String>();
		if (jsonData != null) {
			String json = jsonData.getData();
			result = VmUtils.readJsonToMap(json);
		}
		model.put("result", result);
		model.put("tagMap", tagMap);
		return "admin/message/smsChannel.vm";
	}

	@RequestMapping("/admin/message/saveSmsChannel.xhtml")
	public String saveSmsChannel(HttpServletRequest request, ModelMap model) {
		JsonData jsonData = daoService.getObject(JsonData.class, JsonDataKey.KEY_SMSCHANNEL);
		String old = jsonData.getData();
		ChangeEntry entry = new ChangeEntry(jsonData);
		Map<String, String> dataMap = new HashMap<String, String>();
		Map<String, String[]> result = request.getParameterMap();
		for (Map.Entry<String, String[]> m : result.entrySet()) {
			dataMap.put(m.getKey(), m.getValue()[0]);
		}
		String data = JsonUtils.writeMapToJson(dataMap);
		jsonData.setData(data);

		daoService.saveObject(jsonData);
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_SMSCHANNEL);
		monitorService.saveSysWarn("更改短信通道", old + "====>" + data, RoleTag.jishu);
		monitorService.saveSysWarn("更改短信通道", "有人更改短信通道", RoleTag.dingpiao);
		monitorService.saveChangeLog(getLogonUser().getId(), JsonData.class, JsonDataKey.KEY_SMSCHANNEL, entry.getChangeMap(jsonData));
		return showJsonSuccess(model);
	}

	private void updateRelatedList(List<MemberInfo> list, String dkey) {
		for (MemberInfo memberInfo : list) {
			memberInfo.setOtherinfo(JsonUtils.removeJsonKeyValue(memberInfo.getOtherinfo(), dkey));
			daoService.saveObject(memberInfo);
		}
	}

	/**************************************************************************************/
	@RequestMapping("/admin/message/udfwebsiteMessage.xhtml")
	public String udfwebsiteMessage() {
		return "admin/message/websitemessageUdf.vm";
	}

	@RequestMapping("/admin/message/savewebsiteMessageUDF.xhtml")
	public String savewebsiteMessageUDF(ModelMap model, String basecontent, String content) {
		// 1. 判断模板里有几个变量
		int varscount = 0;
		if (StringUtils.isNotBlank(basecontent)) {
			String[] vars = StringUtils.split(basecontent + " ", "@");
			varscount = (vars.length - 1) / 2;
		}
		List<String> records = Arrays.asList(StringUtils.split(content, "[\n]+"));
		if (records.size() > 1000)
			return showJsonError(model, "一次不能添加超过1000条记录!");
		List<String> errors = new ArrayList<String>();
		int succount = 0;
		for (String record : records) {
			Long memberid = 0l;
			try {
				List<String> recordVO = Arrays.asList(StringUtils.split(record, "[ \t]+"));
				int tmpcount = varscount + 1;
				if (recordVO.size() != tmpcount)
					return showJsonError(model, "确认模板与发送内容是否匹配.【变量数" + tmpcount + ", 实际数" + recordVO.size() + "】");
				memberid = new Long(recordVO.get(0));
				String body = basecontent;
				for (int i = 1; i < tmpcount; i++) {
					String v1 = recordVO.get(i);
					body = StringUtils.replace(body, "@v" + i + "@", v1);
				}
				Map<String, Object> mongoMap = new HashMap<String, Object>();
				mongoMap.put(MongoData.SYSTEM_ID, MongoData.buildId());
				mongoMap.put(MongoData.DEFAULT_ID_NAME, "ext" + memberid + System.currentTimeMillis());
				mongoMap.put(MongoData.ACTION_MEMBERID, memberid);
				mongoMap.put(MongoData.ACTION_BODY, body);
				mongoMap.put(MongoData.ACTION_MULTYWSMSG_ISREAD, "0");
				mongoMap.put(MongoData.ACTION_MULTYWSMSG_ISDEL, "0");
				mongoMap.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestamp());

				mongoService.saveOrUpdateMap(mongoMap, MongoData.DEFAULT_ID_NAME, MongoData.NS_ACTION_MULTYWSMSG);
				succount++;
			} catch (Exception e) {
				dbLogger.error("", e);
				errors.add(memberid.toString());
			}
		}
		if (errors.size() > 0)
			return showJsonError(model, "以下用户发送失败: 【" + errors.toString() + "】");
		return showJsonSuccess(model, "" + succount);
	}

	/**
	 * @function 个别用户发送站内信
	 * @author bob.hu
	 * @date 2011-06-09 17:59:20
	 */
	@RequestMapping("/admin/message/websiteMessageMulty.xhtml")
	public String websiteMessageMulty() {
		return "admin/message/websitemessageMuity.vm";
	}

	@RequestMapping("/admin/message/saveWebsiteMessageMulty.xhtml")
	public String saveWebsiteMessageMulty(ModelMap model, String memberid, String content) {
		List<String> memberids = Arrays.asList(StringUtils.split(memberid, "[ ,，]+"));
		if (memberids.size() > 1000)
			return showJsonError(model, "一次不能添加超过1000条记录!");
		List<String> idList2 = new ArrayList<String>(new HashSet(memberids));
		List<String> diffList = (List<String>) CollectionUtils.disjunction(memberids, idList2);
		String diffstr = VmUtils.printList(diffList);
		if (diffList != null && diffList.size() > 0)
			return showJsonError(model, "用户ID有重复, 核实后再添加! 重复ID为: " + diffstr);
		if (StringUtils.isBlank(content))
			return showJsonError(model, "内容不能为空!");
		Timestamp cur = DateUtil.getCurFullTimestamp();
		// 添加关联
		List<String> errors = new ArrayList<String>();
		int succount = 0;
		for (String mid : idList2) {
			Map<String, Object> mongoMap = new HashMap<String, Object>();
			mongoMap.put(MongoData.SYSTEM_ID, MongoData.buildId());
			mongoMap.put(MongoData.DEFAULT_ID_NAME, "ext" + mid + System.currentTimeMillis());
			mongoMap.put(MongoData.ACTION_MEMBERID, new Long(mid));
			mongoMap.put(MongoData.ACTION_BODY, content);
			mongoMap.put(MongoData.ACTION_MULTYWSMSG_ISREAD, "0");
			mongoMap.put(MongoData.ACTION_MULTYWSMSG_ISDEL, "0");
			mongoMap.put(MongoData.ACTION_ADDTIME, cur);
			try {
				mongoService.saveOrUpdateMap(mongoMap, MongoData.DEFAULT_ID_NAME, MongoData.NS_ACTION_MULTYWSMSG);
				succount++;
			} catch (Exception e) {
				errors.add(mid);
			}
		}
		if (errors.size() > 0)
			return showJsonError(model, "以下用户发送失败: 【" + errors.toString() + "】");
		return showJsonSuccess(model, "" + succount);
	}

	@RequestMapping("/admin/message/movieMsgCustom.xhtml")
	public String movieMsgCustomer(Long movieid, ModelMap model, String tag, HttpServletRequest request) {
		String citycode = getAdminCitycode(request);
		if (StringUtils.isBlank(tag) || StringUtils.equals(TagConstant.TAG_MOVIE, tag)) {
			GewaConfig smsConfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_COMMENTMSG);
			Set<Movie> movieSet = new HashSet<Movie>();
			if (movieid != null) {
				Movie movie = daoService.getObject(Movie.class, movieid);
				movieSet.add(movie);
			} else {
				List<Movie> movieList = mcpService.getCurMovieList();
				movieSet.addAll(movieList);
				List<Movie> movieList2 = mcpService.getFutureMovieList(0, 100,null);
				movieSet.addAll(movieList2);
			}
			List<Movie> movieList = new ArrayList<Movie>(movieSet);
			Collections.sort(movieList, new FirstLetterComparator());
			model.put("movieList", movieList);
			model.put("smsConfig", smsConfig);
			model.put("tag", "movie");
		} else if (StringUtils.equals(TagConstant.TAG_DRAMA, tag)) {
			GewaConfig smsConfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_DRAMAMSG);
			Set<Drama> dramaSet = new HashSet<Drama>();
			if (movieid != null) {
				Drama drama = daoService.getObject(Drama.class, movieid);
				dramaSet.add(drama);
			} else {
				List<Drama> dramaList = dramaService.getCurDramaList(citycode, "name");
				dramaSet.addAll(dramaList);
				List<Drama> dramaList2 = dramaService.getFutureDramaList(citycode, new Date(), 0, 100);
				dramaSet.addAll(dramaList2);
			}
			List<Drama> dramaList = new ArrayList<Drama>(dramaSet);
			Collections.sort(dramaList, new FirstLetterComparator());
			model.put("movieList", dramaList);
			model.put("smsConfig", smsConfig);
			model.put("tag", "drama");
		} else if (StringUtils.equals(TagConstant.TAG_SPORT, tag)) {
			GewaConfig smsConfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_SPORTMSG);
			Set<Sport> sportSet = new HashSet<Sport>();
			if (movieid != null) {
				Sport sport = daoService.getObject(Sport.class, movieid);
				sportSet.add(sport);
			} else {
				List<Sport> sportList = sportService.getCurSportList("name");
				sportSet.addAll(sportList);
			}
			List<Sport> sportList = new ArrayList<Sport>(sportSet);
			Collections.sort(sportList, new FirstLetterComparator());
			model.put("movieList", sportList);
			model.put("smsConfig", smsConfig);
			model.put("tag", "sport");
		}
		Map param = new HashMap();
		String magTag = null;
		if(StringUtils.isBlank(tag)) magTag = "movie";
		else magTag = tag;
		param.put(MongoData.ACTION_TYPE, "integral");
		param.put(MongoData.ACTION_TAG, magTag);
		Map map = mongoService.findOne(MongoData.NS_INTEGRAL, param);
		model.put("map", map);
		return "admin/message/movieMsgCustom.vm";
	}

	@RequestMapping("/admin/message/getMovieMsgCustom.xhtml")
	public String getMovieMsgCustom(@RequestParam Long mid, ModelMap model, String tag) {
		String dkey = "";
		if (StringUtils.equals(TagConstant.TAG_MOVIE, tag)) {
			dkey = JsonDataKey.KEY_SMSMOVIE + mid;
		} else if (StringUtils.equals(TagConstant.TAG_DRAMA, tag)) {
			dkey = JsonDataKey.KEY_SMSDRAMA + mid;
		} else if (StringUtils.equals(TagConstant.TAG_SPORT, tag)) {
			dkey = JsonDataKey.KEY_SMSSPORT + mid;
		}
		JsonData data = daoService.getObject(JsonData.class, dkey);
		String result = "";
		if (data != null) {
			Map<String, String> m = VmUtils.readJsonToMap(data.getData());
			result = m.get("msg");
		}
		return showJsonSuccess(model, result);
	}

	@RequestMapping("/admin/message/saveMovieMsgCustom.xhtml")
	public String getMovieMsgCustom(@RequestParam Long mid, String msg, ModelMap model, String tag) {
		if (StringUtils.isNotBlank(msg) && msg.length() > 60)
			return showJsonError(model, "短信内容过长！");
		User user = getLogonUser();
		String dkey = "";
		if (StringUtils.equals(TagConstant.TAG_MOVIE, tag)) {
			dkey = JsonDataKey.KEY_SMSMOVIE + mid;
		} else if (StringUtils.equals(TagConstant.TAG_DRAMA, tag)) {
			dkey = JsonDataKey.KEY_SMSDRAMA + mid;
		} else if (StringUtils.equals(TagConstant.TAG_SPORT, tag)) {
			dkey = JsonDataKey.KEY_SMSSPORT + mid;
		}
		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("msg", msg);
		String json = JsonUtils.writeMapToJson(dataMap);
		JsonData data = daoService.getObject(JsonData.class, dkey);
		if (data == null) {
			data = new JsonData(dkey, json);
		} else {
			data.setData(json);
		}
		daoService.saveObject(data);
		dbLogger.warn("用户修改短信内如：" + user.getId() + ", msg:" + msg);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/message/saveDefaultMsg.xhtml")
	public String saveDefaultMsg(String content, ModelMap model, String tag) {
		if (StringUtils.isBlank(content))
			return showJsonError(model, "内如不能为空！");
		if (content.length() > 60)
			return showJsonError(model, "短信内如过长！");
		GewaConfig smsConfig = null;
		if (StringUtils.equals(TagConstant.TAG_MOVIE, tag)) {
			smsConfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_COMMENTMSG);
		} else if (StringUtils.equals(TagConstant.TAG_DRAMA, tag)) {
			smsConfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_DRAMAMSG);
		} else if (StringUtils.equals(TagConstant.TAG_SPORT, tag)) {
			smsConfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_SPORTMSG);
		}
		if (smsConfig == null)
			return showJsonError(model, "GewaConfig ID错误");
		smsConfig.setContent(content);
		daoService.saveObject(smsConfig);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/message/batchQry.xhtml")
	public String batchQry(ModelMap model) {
		this.putMap(model);
		return "admin/message/batchQry.vm";
	}

	// 多长时间内没有购买
	@RequestMapping("/admin/message/batchQryOrder.xhtml")
	public String batchQryOrder(@RequestParam Date startdate, @RequestParam String type, Integer pageNo, ModelMap model) {
		if (pageNo == null)
			pageNo = 0;
		int count = 0;
		int rows = 5000;
		List strList = new ArrayList();
		Timestamp starttime = new Timestamp(startdate.getTime());
		List list = new ArrayList();
		String start1 = "", start2 = "", qry = "";
		Object[] params = new Object[] { OrderConstant.STATUS_PAID_SUCCESS, OrderConstant.STATUS_PAID_SUCCESS, starttime };
		String type0 = type;
		if (StringUtils.equals(type, "id") || StringUtils.equals(type, "email"))
			type0 = "memberid";
		start1 = "select count(distinct o." + type0 + ") ";
		start2 = "select distinct o." + type0 + " ";
		qry = "from GewaOrder o where o.status=? ";
		qry = qry + "and not exists(select d.id from GewaOrder d where d.memberid=o.memberid and d.status=? and d.addtime>=?) ";
		list = hibernateTemplate.find(start1 + qry, params);
		strList = daoService.queryByRowsRange(start2 + qry, pageNo * rows, rows, params);

		if (!list.isEmpty())
			count = Integer.parseInt(list.get(0) + "");
		if (StringUtils.equals(type, "email")) {
			List temp = daoService.getObjectList(Member.class, strList);
			strList = BeanUtil.getBeanPropertyList(temp, String.class, "email", false);
		}
		String result = StringUtils.join(strList, ",");
		PageUtil pageUtil = new PageUtil(count, rows, pageNo, "admin/message/batchQryOrder.xhtml");
		Map map = new HashMap();
		map.put("startdate", DateUtil.formatDate(startdate));
		map.put("type", type);
		pageUtil.initPageInfo(map);
		model.put("pageUtil", pageUtil);
		model.put("result", result);
		model.put("count", count);
		this.putMap(model);
		return "admin/message/batchQry.vm";
	}

	// 某一时间段购票用户
	@RequestMapping("/admin/message/batchQryTicketOrder.xhtml")
	public String batchQryOrder(Date starttime, Date endtime, String movieid, String cinemaid, String paytype, String status, String type,
			Integer pageNo, ModelMap model) {
		if (pageNo == null)
			pageNo = 0;
		int count = 0;
		int rows = 1000;
		List strList = new ArrayList();
		if (starttime == null || endtime == null)
			return showJsonError(model, "请输入起止日期！");
		Timestamp sTime = new Timestamp(starttime.getTime());
		Timestamp eTime = new Timestamp(endtime.getTime());
		List list = new ArrayList();
		String start1 = "", start2 = "", qry = "";
		Object[] params = new Object[] { sTime, eTime };
		String type0 = type;
		if (StringUtils.equals(type, "id") || StringUtils.equals(type, "email"))
			type0 = "memberid";
		start1 = "select count(distinct o." + type0 + ") ";
		start2 = "select distinct o." + type0 + " ";
		qry = "from TicketOrder o where o.addtime >= ? and o.addtime <=? ";
		if (!StringUtils.isBlank(movieid))
			qry += " and o.movieid= " + movieid;
		if (!StringUtils.isBlank(cinemaid))
			qry += " and o.cinemaid= " + cinemaid;
		if (!StringUtils.isBlank(paytype))
			qry += " and o.paymethod= '" + paytype + "'";
		if (!StringUtils.isBlank(status))
			qry += " and o.status= '" + status + "'";
		list = hibernateTemplate.find(start1 + qry, params);
		strList = daoService.queryByRowsRange(start2 + qry, pageNo * rows, rows, params);

		if (!list.isEmpty())
			count = Integer.parseInt(list.get(0) + "");
		if (StringUtils.equals(type, "email")) {
			List temp = daoService.getObjectList(Member.class, strList);
			strList = BeanUtil.getBeanPropertyList(temp, String.class, "email", false);
		}
		String result = StringUtils.join(strList, ",");
		PageUtil pageUtil = new PageUtil(count, rows, pageNo, "admin/message/batchQryTicketOrder.xhtml");
		Map map = new HashMap();
		map.put("starttime", DateUtil.formatDate(sTime));
		map.put("endtime", DateUtil.formatDate(eTime));
		map.put("paytype", paytype);
		map.put("movieid", movieid);
		map.put("cinemaid", cinemaid);
		map.put("status", status);
		map.put("type", type);
		pageUtil.initPageInfo(map);
		model.put("pageUtil", pageUtil);
		model.put("result", result);
		model.put("count", count);
		this.putMap(model);
		return "admin/message/batchQry.vm";
	}

	private void putMap(ModelMap model) {
		model.put("payMap", PaymethodConstant.getPayTextMap());
		model.put("keySet", PaymethodConstant.getPayTextMap().keySet());
		model.put("statusMap", OrderConstant.statusMap);
		model.put("skeySet", OrderConstant.statusMap.keySet());
	}

	@RequestMapping("/admin/message/hisSmsList.xhtml")
	public String hisSms(String mobile, ModelMap model) {
		String url = "admin/message/hisSmsList.vm";
		if (StringUtils.isBlank(mobile))
			return url;
		String sql = "select s.tradeno as tradeNo, s.contact as contact, sendtime as sendtime, channel as channel, status as status, s.content as content from WEBDATA.smsrecord_his s where s.contact=? order by s.recordid desc";
		List<Map<String, Object>> smsList = jdbcTemplate.queryForList(sql, mobile);
		model.put("smsList", smsList);
		return url;
	}

	@RequestMapping("/admin/message/querywebsiteMessageMulty.xhtml")
	public String querywebsiteMessageMulty(Long memberid, ModelMap model) {
		if (memberid == null)
			return "admin/message/querywebsitemessageUdf.vm";
		int count = 0;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(MongoData.ACTION_MEMBERID, memberid);
		List<Map> resultMap = mongoService.find(MongoData.NS_ACTION_MULTYWSMSG, paramMap, MongoData.ACTION_ADDTIME, false);
		count = resultMap.size();
		model.put("resultMap", resultMap);
		model.put("count", count);
		model.put("memberid", memberid);
		return "admin/message/querywebsitemessageUdf.vm";
	}

	@RequestMapping("/admin/message/upload.xhtml")
	public String upload(ModelMap model, String parentid) {
		model.put("parentid", parentid);
		return "admin/message/smsInfo.vm";
	}

	@RequestMapping("/admin/message/movieMsgAction.xhtml")
	public String movieMsgAction(Long movieid, ModelMap model, String tag) {
		if (StringUtils.isBlank(tag) || StringUtils.equals(TagConstant.TAG_MOVIE, tag)) {
			GewaConfig msgConfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_MOVIEMSGACTION);
			Set<Movie> movieSet = new HashSet<Movie>();
			if (movieid != null) {
				Movie movie = daoService.getObject(Movie.class, movieid);
				movieSet.add(movie);
			} else {
				List<Movie> movieList = mcpService.getCurMovieList();
				movieSet.addAll(movieList);
				List<Movie> movieList2 = mcpService.getFutureMovieList(0, 100, null);
				movieSet.addAll(movieList2);
			}
			List<Movie> movieList = new ArrayList<Movie>(movieSet);
			Collections.sort(movieList, new FirstLetterComparator());
			model.put("movieList", movieList);
			model.put("msgConfig", msgConfig);
			model.put("tag", TagConstant.TAG_MOVIE);
		}
		return "admin/message/movieMsgAction.vm";
	}

	@RequestMapping("/admin/message/saveDefaultMsgAction.xhtml")
	public String saveDefaultMsgAction(String content, ModelMap model, String tag) {
		if (StringUtils.isBlank(content))
			return showJsonError(model, "内如不能为空！");
		GewaConfig msgConfig = null;
		if (StringUtils.equals(TagConstant.TAG_MOVIE, tag)) {
			msgConfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_MOVIEMSGACTION);
		}
		if (msgConfig == null)
			return showJsonError(model, "GewaConfig ID错误");
		msgConfig.setContent(content);
		daoService.saveObject(msgConfig);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/message/saveMovieMsgAction.xhtml")
	public String getMovieMsgAction(@RequestParam Long mid, String msg, ModelMap model, String tag) {
		User user = getLogonUser();
		String dkey = "";
		if (StringUtils.equals(TagConstant.TAG_MOVIE, tag)) {
			dkey = JsonDataKey.KEY_MSGMOVIE + mid;
		}
		Map<String, String> dataMap = new HashMap<String, String>();
		dataMap.put("msg", msg);
		String json = JsonUtils.writeMapToJson(dataMap);
		JsonData data = daoService.getObject(JsonData.class, dkey);
		if (data == null) {
			data = new JsonData(dkey, json);
		} else {
			data.setData(json);
		}
		daoService.saveObject(data);
		dbLogger.warn("用户修改站内信如：" + user.getId() + ", msg:" + msg);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/message/getMovieMsgAction.xhtml")
	public String getMovieMsgAction(@RequestParam Long mid, ModelMap model, String tag) {
		String dkey = "";
		if (StringUtils.equals(TagConstant.TAG_MOVIE, tag)) {
			dkey = JsonDataKey.KEY_MSGMOVIE + mid;
		}
		JsonData data = daoService.getObject(JsonData.class, dkey);
		String result = "";
		if (data != null) {
			Map<String, String> m = VmUtils.readJsonToMap(data.getData());
			result = m.get("msg");
		}
		return showJsonSuccess(model, result);
	}
	@RequestMapping("/admin/message/saveIntegral.xhtml")
	public String saveIntegral(String isSend,Timestamp starttime,Timestamp endtime
			,Integer integral, String tag,ModelMap model) {
		Map param = new HashMap();
		param.put(MongoData.ACTION_TYPE, "integral");
		param.put(MongoData.ACTION_TAG, tag);
		Map map = mongoService.findOne(MongoData.NS_INTEGRAL, param);
		if(map == null){
			map = new HashMap();
			map.put(MongoData.SYSTEM_ID, ObjectId.uuid());
			map.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestampStr());
			map.put("starttime", DateUtil.formatTimestamp(starttime));
			map.put("isSend", isSend);
			map.put("endtime", DateUtil.formatTimestamp(endtime));
			map.put("integral", integral);
			map.put(MongoData.ACTION_TAG, tag);
			map.put(MongoData.ACTION_TYPE, "integral");
			mongoService.addMap(map, MongoData.SYSTEM_ID, MongoData.NS_INTEGRAL);
		}else{
			map.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestampStr());
			map.put("starttime", DateUtil.formatTimestamp(starttime));
			map.put("isSend", isSend);
			map.put("endtime", DateUtil.formatTimestamp(endtime));
			map.put("integral", integral);
			mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_INTEGRAL);
		}
		return showJsonSuccess(model);
	}
	
	private List<String> ips = Arrays.asList("180.166.23.170", "180.166.48.210");
	private long last = Timestamp.valueOf("2013-12-20 00:00:00").getTime();
	
	@RequestMapping("/message/addTeleSms.xhtml")
	public String addTeleSms(ModelMap model, String mobile, String content, HttpServletRequest request){
		String ip = WebUtils.getRemoteIp(request);
		if(!ips.contains(ip) && !Config.isGewaServerIp(ip) && !WebUtils.isLocalIp(ip) || System.currentTimeMillis() > last){
			return showError(model, "");
		}
		untransService.addTeleSms(mobile, content);
		return showMessage(model, content);
	}
	@RequestMapping("/message/getTelecomSms.xhtml")
	public String getTelecomSms(ModelMap model, final String test, HttpServletRequest request){
		String ip = WebUtils.getRemoteIp(request);
		if(!ips.contains(ip) && !WebUtils.isLocalIp(ip) || System.currentTimeMillis() > last ){
			return showError(model, "");
		}
		final Map jsonMap = new HashMap();
		lockService.doWithWriteLock("getTelecomSms", new LockCallback(){

			@Override
			public void processWithInLock() {
				DBObject params = mongoService.queryBasicDBObject("querycount", "=", "0");
				List<Map> result = mongoService.find(MongoData.NS_TELE_MOBILE, params, "addtime", true, 0, 1);
				if(result.isEmpty()){
					return ;
				}
				Map<String, String> row = result.get(0);
				if(StringUtils.isBlank(test)){
					row.put("querycount", "1");
					mongoService.saveOrUpdateMap(row, "_id", MongoData.NS_TELE_MOBILE);
				}
				jsonMap.put("mobile", row.get("mobile"));
				jsonMap.put("content", row.get("content")+"[格瓦拉]");				
			}
		});

		return showJsonSuccess(model, jsonMap);
	}
	
	@RequestMapping("/message/getRemainSmsCount.xhtml")
	@ResponseBody
	public String getRemainSmsCount(ModelMap model, HttpServletRequest request){
		String ip = WebUtils.getRemoteIp(request);
		if(!ips.contains(ip)&& !WebUtils.isLocalIp(ip) || System.currentTimeMillis() > last ){
			return showError(model, "");
		}
		
		DBObject query = mongoService.queryBasicDBObject("querycount", "=", "0");
		return ""+mongoService.getCount(MongoData.NS_TELE_MOBILE, query);
	}
	
	@RequestMapping("/admin/message/getPlayItemMessage.xhtml")
	public String getPlayItemMessage(Integer pageNo, Date startDate, Date endDate, String status, ModelMap model) {
		String startDateStr = null;
		String endDateStr = null;
		if (startDate == null && endDate == null) {
			startDate = DateUtil.getCurDate();
			endDate = DateUtil.addDay(startDate, 3);
		}
		if (status == null)
			status = Status.N;
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPages = 100;
		int firstPages = pageNo * rowsPerPages;
		DBObject queryCondition = new BasicDBObject();
		DBObject object = mongoService.queryBasicDBObject("status", "=", status);
		queryCondition.putAll(object);
		if (startDate != null && endDate != null) {
			endDate = DateUtil.addDay(endDate, 1);
			startDateStr = DateUtil.format(startDate, "yyyy-MM-dd HH:mm:ss");
			endDateStr = DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss");
			object = mongoService.queryAdvancedDBObject("adddate", new String[] { ">=", "<=" }, new String[] { startDateStr, endDateStr });
			queryCondition.putAll(object);
		} else if (startDate != null) {
			startDateStr = DateUtil.format(startDate, "yyyy-MM-dd HH:mm:ss");
			object = mongoService.queryBasicDBObject("adddate", ">=", startDateStr);
			queryCondition.putAll(object);
		} else {
			endDate = DateUtil.addDay(endDate, 1);
			endDateStr = DateUtil.format(endDate, "yyyy-MM-dd HH:mm:ss");
			object = mongoService.queryBasicDBObject("adddate", "<=", endDateStr);
			queryCondition.putAll(object);
		}
		dbLogger.warn("【" + queryCondition + "】");
		int count = mongoService.getObjectCount(PlayItemMessage.class, queryCondition);
		List<PlayItemMessage> playitemMessageList = mongoService.getObjectList(PlayItemMessage.class, queryCondition, "updatetime", true, firstPages,
				rowsPerPages);
		for (PlayItemMessage playItemMessage : playitemMessageList) {
			if (StringUtils.equals(playItemMessage.getTag(), "cinema")) {
				if (StringUtils.equals(playItemMessage.getFlag(), Status.N)) {
					Timestamp starttime = new Timestamp(playItemMessage.getPlaydate().getTime());
					Timestamp endtime = DateUtil.getLastTimeOfDay(starttime);
					List<OpenPlayItem> opiList = openPlayService.getOpiList(null, playItemMessage.getRelatedid(), playItemMessage.getCategoryid(),
							starttime, endtime, true, 1);
					if (!opiList.isEmpty()) {
						playItemMessage.setFlag(Status.Y);
					}
				}
			}
		}
		PageUtil pageUtil = new PageUtil(count, rowsPerPages, pageNo, "admin/message/getPlayItemMessage.xhtml");
		Map params = new HashMap();
		params.put("startDate", DateUtil.formatDate(startDate));
		params.put("status", status);
		params.put("endDate", DateUtil.formatDate(endDate));
		pageUtil.initPageInfo(params);
		mongoService.saveOrUpdateObjectList(playitemMessageList, MongoData.DEFAULT_ID_NAME);
		model.put("playitemMessageList", playitemMessageList);
		model.put("pageUtil", pageUtil);
		model.put("count", count);
		return "admin/common/playItemMessageList.vm";
	}

}
