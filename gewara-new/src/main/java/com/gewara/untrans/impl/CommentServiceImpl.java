package com.gewara.untrans.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gewara.commons.sign.Sign;
import com.gewara.config.CommentAPIConfig;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.Flag;
import com.gewara.constant.PointConstant;
import com.gewara.constant.Status;
import com.gewara.constant.SysAction;
import com.gewara.constant.TagConstant;
import com.gewara.constant.order.AddressConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.HttpTimeout;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.MemberSign;
import com.gewara.json.MemberStats;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.Point;
import com.gewara.mongo.MongoService;
import com.gewara.pay.PayUtil;
import com.gewara.service.DaoService;
import com.gewara.service.JsonDataService;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.member.MemberService;
import com.gewara.service.member.PointService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.AbstractSynchBaseService;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.MemberCountService;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.ApiUtils;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.util.XmlUtils;
import com.gewara.xmlbind.BaseObjectResponse;
import com.gewara.xmlbind.bbs.Comment;
import com.gewara.xmlbind.bbs.CommentList;
import com.gewara.xmlbind.bbs.CountByMovieIdAddDate;
import com.gewara.xmlbind.bbs.CountByMovieIdAddDateList;
import com.gewara.xmlbind.bbs.MemberCount;
import com.gewara.xmlbind.bbs.MemberCountList;

/**
 * 哇啦http服务客户端
 * 
 * @author quzhuping
 * 
 */
@Service("commentService")
public class CommentServiceImpl extends AbstractSynchBaseService implements InitializingBean, CommentService {
	public static final String R = "<img.*src=(.*?)[^>]*?>";
	@Autowired@Qualifier("blogService")
	private BlogService blogService;

	@Autowired@Qualifier("untransService")
	private UntransService untransService;

	@Autowired@Qualifier("daoService")
	private DaoService daoService;

	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	
	@Autowired
	@Qualifier("userMessageService")
	private UserMessageService userMessageService;

	@Autowired
	@Qualifier("pointService")
	private PointService pointService;
	
	@Autowired
	@Qualifier("mongoService")
	private  MongoService mongoService;
	
	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;
	
	@Autowired
	@Qualifier("jsonDataService")
	private JsonDataService jsonDataService;
	
	@Autowired@Qualifier("markService")
	private MarkService markService;
	
	private String openApiUrl;
	@Override
	public void afterPropertiesSet() throws Exception {
		openApiUrl = config.getString("openApiUrl");
		
	}
	@Autowired
	@Qualifier("commentAPIConfig")
	private CommentAPIConfig commentAPIConfig;

	public void setCommentAPIConfig(CommentAPIConfig commentAPIConfig) {
		this.commentAPIConfig = commentAPIConfig;
	}
	@Value("${openApi.walaAppkey}")
	private String openApiWalaAppkey = null;
	@Value("${openApi.walaSecretCode}")
	private String openApiWalaSecretCode = null;
	@Autowired
	@Qualifier("cacheService")
	private CacheService cacheService;
	@Autowired@Qualifier("memberCountService")
	private MemberCountService memberCountService;
	@Autowired
	@Qualifier("memberService")
	private MemberService memberService;

	private HttpResult postHttpRequest(String method, String format, Map<String, String> params, int timeout){
		params.put("method", method);
		params.put("appkey", openApiWalaAppkey);
		params.put("timestamp", DateUtil.getCurFullTimestampStr());
		params.put("format", format);
		params.put("v", "1.0");
		params.put("signmethod", "MD5");
		String sign = Sign.signMD5(params, openApiWalaSecretCode);
		params.put("sign", sign);
		long cur = System.currentTimeMillis();
		HttpResult hr = HttpUtils.postUrlAsString(openApiUrl, params, timeout);
		Map<String, String> paramsLog = new HashMap<String, String>();
		paramsLog.put("uri", openApiUrl);
		if(params != null) paramsLog.putAll(params);
		monitorService.addApiCall(paramsLog, cur, hr.isSuccess());
		return hr;
	}
	
	private HttpResult postHttpRequestXML(String method, Map<String, String> params, int timeout){
		return this.postHttpRequest(method, "xml", params, timeout);
	}
	
	/**
	 * 请求返回是否是正常值
	 * @param rv
	 * @return
	 */
	private boolean isOk(String rv){
		if(StringUtils.contains(rv, "code><error")){
			return false;
		}
		return true;
	}
	
	@Override
	public Long saveComment(Comment comment) {

		String json = JsonUtils.writeObjectToJson(comment);
		json = JsonUtils.removeJsonKeyValue(json, "fromFlag");
		json = JsonUtils.removeJsonKeyValue(json, "addressInfo");

		Map params = new HashMap();
		params.put("commentJson", json);

		//HttpResult ec = HttpUtils.postUrlAsString(commentAPIConfig.getSiteURL() + commentAPIConfig.getSaveCommentApiURL(), params);
		HttpResult ec = this.postHttpRequestXML(commentAPIConfig.getSaveCommentApiURL(), params, HttpTimeout.SHORT_REQUEST);
		if (ec.isSuccess() && isOk(ec.getResponse())){
			return Long.valueOf(ec.getResponse());
		}
		return null;
	}

	@Override
	public Long updateComment(Comment comment) {
		//TODO:调用者移出
		String json = JsonUtils.writeObjectToJson(comment);
		json = JsonUtils.removeJsonKeyValue(json, "fromFlag");
		json = JsonUtils.removeJsonKeyValue(json, "addressInfo");

		Map params = new HashMap();
		params.put("commentJson", json);

		HttpResult hr = this.postHttpRequestXML(commentAPIConfig.getUpdateCommentURL(), params, HttpTimeout.SHORT_REQUEST);
		if (hr.isSuccess()) {
			String idstr = hr.getResponse();
			if(isOk(idstr)){
				if (StringUtils.isNotBlank(idstr)) {
					Long id = null;
					try {
						id = Long.valueOf(idstr);
					} catch (NumberFormatException e) {
					}
					return id;
				}
			}
		}
		return null;
	}

	@Override
	public Comment getCommentById(Long commentId) {
		Comment comment = null;

		Map params = new HashMap();
		params.put("commentId", commentId != null ? commentId.toString() : null);

		//HttpResult ec = HttpUtils.postUrlAsString(commentAPIConfig.getSiteURL() + commentAPIConfig.getQueryCommentByIdApiURL(), params);
		HttpResult ec = this.postHttpRequest(commentAPIConfig.getQueryCommentByIdApiURL(), "json", params, HttpTimeout.SHORT_REQUEST);
		if (ec.isSuccess()) {
			String commentJson = ec.getResponse();
			if(isOk(commentJson)){
				Map map = JsonUtils.readJsonToMap(commentJson);
	
				comment = new Comment();
				BindUtils.bindData(comment, map);
			}
		}
		return comment;
	}
	
	@Override
	public List<Comment> getCommentByIdList(Collection<Long> idList){
		if(CollectionUtils.isEmpty(idList)) return new ArrayList<Comment>();
		Map<String, String> params = new HashMap<String, String>();
		params.put("ids", StringUtils.join(idList, ","));

		HttpResult result = this.postHttpRequestXML(commentAPIConfig.getCommentListByIDsURL(), params, HttpTimeout.SHORT_REQUEST);
		if (result.isSuccess()) {
			String commentsXML = result.getResponse();
			if(isOk(commentsXML)){
				BeanReader beanReader = ApiUtils.getBeanReader("commentList", CommentList.class);
				try{
					CommentList commentList = (CommentList) ApiUtils.xml2Object(beanReader, XmlUtils.filterInvalid(commentsXML));
					return commentList.getCommentList();
				}catch(Exception e){
					return new ArrayList<Comment>();
				}
			}
		}
		return new ArrayList<Comment>();
	}

	/**
	 * 获取后台定时任务信息
	 * 
	 * @return
	 */
	@Override
	public List<HashMap> getTaskCommentList() {
		//HttpResult hr = HttpUtils.postUrlAsString(commentAPIConfig.getSiteURL() + commentAPIConfig.getTaskCommentListURL(), null, TIME_OUT);
		HttpResult hr = this.postHttpRequestXML(commentAPIConfig.getTaskCommentListURL(), new HashMap<String, String>(), HttpTimeout.SHORT_REQUEST);
		if (hr.isSuccess()) {
			String jsonStr = hr.getResponse();
			if(isOk(jsonStr)){
				if (StringUtils.isNotBlank(jsonStr))
					return JsonUtils.readJsonToObjectList(HashMap.class, jsonStr);
			}
		}
		return new ArrayList<HashMap>();
	}

	public List<Map> getMicroModeratorList(String mtitle, String flag, String address, int from, int maxnum) {

		Map params = new HashMap();
		params.put("mtitle", mtitle);
		params.put("flag", flag);
		params.put("address", address);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));

		//HttpResult hr = HttpUtils.postUrlAsString(commentAPIConfig.getSiteURL() + commentAPIConfig.getMicroModeratorListURL(), params, TIME_OUT);
		HttpResult hr = this.postHttpRequest(commentAPIConfig.getMicroModeratorListURL(), "json", params, HttpTimeout.SHORT_REQUEST);
		List<Map> lm = null;
		if (hr.isSuccess()) {
			String jsonStr = hr.getResponse();
			if(isOk(jsonStr)){
				if (StringUtils.isNotBlank(jsonStr))
					lm = JsonUtils.readJsonToObjectList(Map.class, jsonStr);
			}
		}
		return lm;
	}

	public List<Long> getToFilmfestIntresetMember(String moderator, String tag, int from, int maxnum) {
		Map params = new HashMap(1);
		params.put("moderator", moderator);
		params.put("tag", tag);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		HttpResult hr = this.postHttpRequest(commentAPIConfig.getToFilmfestIntresetMemberURL(), "json", params, HttpTimeout.SHORT_REQUEST);
		List<Long> lm = null;
		if (hr.isSuccess()) {
			String jsonStr = hr.getResponse();
			if(isOk(jsonStr)){
				if (StringUtils.isNotBlank(jsonStr))
					lm = JsonUtils.readJsonToObjectList(Long.class, jsonStr);
			}
		}
		return lm;
	}

	@Override
	public List<Comment> getCommentList(String[] tags, Long memberId, Date beginDate, Date endDate, int from, int maxnum) {
		Map params = new HashMap();
		if (!ArrayUtils.isEmpty(tags)) params.put("tags", StringUtils.join(tags, ","));
		params.put("memberId", memberId != null ? memberId.toString() : null);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		params.put("beginTime", DateUtil.formatTimestamp(beginDate));
		params.put("endTime", DateUtil.formatTimestamp(endDate));

		return this.getHttpCommentList(params, commentAPIConfig.getQueryCommentsURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public List<Comment> getCommentListByTag(String tag, int from, int maxnum) {
		return this.getCommentList(tag, null, Status.Y, null, null, null, null, null, null,null,null, from, maxnum);
	}

	@Override
	public List<Comment> getCommentListByKey(String tag, String key) {
		return getCommentList(tag, null, Status.Y, null, null, key, null, null, null,null, null, 0, 100);
	}

	@Override
	public List<Comment> getCommentListByRelatedId(String tag,String flag, Long relatedId, String order, Long mincommentid, int from, int maxnum) {
		return getCommentList(tag, relatedId, Status.Y, null, null, null, order, null, null,flag, mincommentid, from, maxnum);
	}
	@Override
	public List<Comment> getCommentListByRelatedId(String tag,String flag, Long relatedId, String order, int from, int maxnum) {
		return getCommentList(tag, relatedId, Status.Y, null, null, null, order, null, null,flag, null, from, maxnum);
	}
	private List<Comment> getCommentList(String tag, Long relatedid, String status, Long memberid, Long transferid, String key, String order,
			Timestamp startTime, Timestamp endTime,String flag, Long mincommentid, int from, int maxnum) {
		Map params = new HashMap();

		params.put("tag", tag);
		params.put("relatedid", relatedid != null ? relatedid.toString() : null);
		params.put("status", status);
		params.put("memberid", memberid != null ? memberid.toString() : null);
		params.put("transferid", transferid != null ? transferid.toString() : null);
		params.put("key", key);
		params.put("order", order);
		params.put("startTime", DateUtil.formatTimestamp(startTime));
		params.put("endTime", DateUtil.formatTimestamp(endTime));
		if(mincommentid!=null){
			params.put("mincommentid", mincommentid.toString());
		}
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		if(StringUtils.isNotBlank(flag)){
			params.put("flag", flag);
		}
		return this.getHttpCommentList(params, commentAPIConfig.getCommentListURL(), HttpTimeout.SHORT_REQUEST);
	}
	
	@Override
	public List<Comment> getHotCommentListByRelatedId(String tag,String flag, Long relatedId,Timestamp startTime, Timestamp endTime, int from, int maxnum){
		return this.getHotCommentList(tag, relatedId, Status.Y, null, null, null, startTime, endTime, flag, null, null, null, from, maxnum);
	}
	@Override
	public List<Comment> getHotCommentListByTopic(String topic, Timestamp startTime, Timestamp endTime, String order, int from, int maxnum){
		return this.getHotCommentList(null, null, Status.Y, null, null, null, startTime, endTime, null, "N", topic, order, from, maxnum);
	}
	
	private List<Comment> getHotCommentList(String tag, Long relatedid, String status, Long memberid, Long transferid, String key,
			Timestamp startTime, Timestamp endTime,String flag, String bodyLength, String topic, String order, int from, int maxnum) {
		Map params = new HashMap();

		params.put("tag", tag);
		params.put("relatedid", relatedid != null ? relatedid.toString() : null);
		params.put("status", status);
		params.put("memberid", memberid != null ? memberid.toString() : null);
		params.put("transferid", transferid != null ? transferid.toString() : null);
		params.put("key", key);
		params.put("startTime", DateUtil.formatTimestamp(startTime));
		params.put("endTime", DateUtil.formatTimestamp(endTime));
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		params.put("bodyLength", bodyLength);
		params.put("topic", topic);
		params.put("order", order);
		if(StringUtils.isNotBlank(flag)){
			params.put("flag", flag);
		}
		return this.getHttpCommentList(params, commentAPIConfig.getQueryHotCommentListURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public Integer getCommentCountByRelatedId(String tag, Long relatedId) {
		return getCommentCount(tag,null, relatedId, Status.Y, null, null, null, null, null);
	}
	
	@Override
	public Integer getCommentCountByRelatedId(String tag,String flag, Long relatedId){
		return getCommentCount(tag,flag, relatedId, Status.Y, null, null, null, null, null);
	}

	@Override
	public Integer getCommentCountByTag(String tag) {
		return getCommentCount(tag,null, null, Status.Y, null, null, null, null, null);
	}

	@Override
	public Integer getCommentCount(String tag,String flag,Long relatedid, String status, Long memberid, Long transferid, String body, Timestamp startTime,
			Timestamp endTime) {
		Map params = new HashMap();

		params.put("tag", tag);
		params.put("relatedid", relatedid != null ? relatedid.toString() : null);
		params.put("status", status);
		params.put("memberid", memberid != null ? memberid.toString() : null);
		params.put("transferid", transferid != null ? transferid.toString() : null);
		params.put("body", body);
		params.put("startTime", DateUtil.formatTimestamp(startTime));
		params.put("endTime", DateUtil.formatTimestamp(endTime));
		if(StringUtils.isNotBlank(flag)){
			params.put("flag", flag);
		}
		return this.getHttpIntegerValue(params, commentAPIConfig.getCommentCountURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public List<Comment> getCommentListByTags(String[] tag, Long memberid, boolean isTransfer, int from, int maxnum) {
		Map params = new HashMap();

		params.put("tag", StringUtils.join(tag, ','));
		params.put("memberid", memberid != null ? memberid.toString() : null);
		params.put("isTransfer", String.valueOf(isTransfer));
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));

		return this.getHttpCommentList(params, commentAPIConfig.getCommentListByTagsURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public Integer getCommentCountByTags(String[] tag, Long memberid, boolean isTransfer) {
		Map params = new HashMap();

		params.put("tag", StringUtils.join(tag, ','));
		params.put("memberid", memberid != null ? memberid.toString() : null);
		params.put("isTransfer", String.valueOf(isTransfer));

		return this.getHttpIntegerValue(params, commentAPIConfig.getCommentCountByTagsURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public List<Comment> getMicroBlogListByMemberid(String nickName, Long memberid, int from, int maxnum) {
		Map<String, String> params = new HashMap();

		params.put("nickName", nickName);
		params.put("memberid", memberid != null ? memberid.toString() : null);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));

		return this.getHttpCommentList(params, commentAPIConfig.getMicroBlogListByMemberidURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public Integer getCommentCount(String tag, Long relatedid, Long memberid, String body, String status, Timestamp beginDate, Timestamp endDate) {
		Map params = new HashMap();

		params.put("tag", tag);
		params.put("relatedid", relatedid != null ? relatedid.toString() : null);
		params.put("memberid", memberid != null ? memberid.toString() : null);
		params.put("body", body);
		params.put("status", status);
		params.put("beginDate", DateUtil.formatTimestamp(beginDate));
		params.put("endDate", DateUtil.formatTimestamp(endDate));

		return this.getHttpIntegerValue(params, commentAPIConfig.getCommentCount2URL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public Integer getCommentCount(String tag, Long relatedid, Long memberid, String body, String status) {
		return getCommentCount(tag, relatedid, memberid, body, status, null, null);
	}

	@Override
	public List<Comment> getCommentList(String tag, Long relatedid, Long memberid, String body, String status, Timestamp beginDate,
			Timestamp endDate, int from, int maxnum) {
		Map params = new HashMap();

		params.put("tag", tag);
		params.put("memberid", memberid != null ? memberid.toString() : null);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		params.put("relatedid", relatedid != null ? relatedid.toString() : null);
		params.put("body", body);
		params.put("status", status);
		params.put("beginDate", DateUtil.formatTimestamp(beginDate));
		params.put("endDate", DateUtil.formatTimestamp(endDate));

		return this.getHttpCommentList(params, commentAPIConfig.getCommentList2URL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public List<Comment> getCommentList(String tag, Long relatedid, Long memberid, String body, String status, int from, int maxnum) {
		return getCommentList(tag, relatedid, memberid, body, status, null, null, from, maxnum);
	}

	@Override
	public void updateCommentReplyCount(Long commentid, String type) {
		Map params = new HashMap();

		params.put("commentid", commentid != null ? commentid.toString() : null);
		params.put("type", type);

		//HttpUtils.postUrlAsString(commentAPIConfig.getSiteURL() + commentAPIConfig.getUpdateCommentReplyCountURL(), params);
		this.postHttpRequestXML(commentAPIConfig.getUpdateCommentReplyCountURL(), params, HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public List<Map> getHotMicroMemberList(String tag, Long memberid, int maxnum) {
		List<Map> result = (List<Map>) cacheService.get(CacheConstant.REGION_ONEHOUR, CacheConstant.KEY_HOT_MICROMEMBER + maxnum);
		if (result == null) {
			result = new LinkedList<Map>();
			List<MemberCount> mcl = this.getMemberCountList(tag, memberid, maxnum);
			for (MemberCount mc : mcl) {
				Long mid = mc.getMemberId();
				result.add(memberService.getCacheMemberInfoMap(mid));
			}
			cacheService.set(CacheConstant.REGION_ONEHOUR, CacheConstant.KEY_HOT_MICROMEMBER + maxnum, result);
		}
		return result;
	}

	private List<MemberCount> getMemberCountList(String tag, Long memberid, int maxnum) {
		Map params = new HashMap();

		params.put("tag", tag);
		params.put("memberid", memberid != null ? memberid.toString() : null);
		params.put("maxnum", String.valueOf(maxnum));

		MemberCountList mcl = new MemberCountList();
		//HttpResult ec = HttpUtils.postUrlAsString(commentAPIConfig.getSiteURL() + commentAPIConfig.getHotMicroMemberListURL(), params);
		HttpResult ec = this.postHttpRequestXML(commentAPIConfig.getHotMicroMemberListURL(), params, HttpTimeout.SHORT_REQUEST);
		if (ec.isSuccess()) {
			String commentsXML = ec.getResponse();
			if(isOk(commentsXML)){
				BeanReader beanReader = ApiUtils.getBeanReader("memberCountList", MemberCountList.class);
				mcl = (MemberCountList) ApiUtils.xml2Object(beanReader, XmlUtils.filterInvalid(commentsXML));
			}
		}

		return mcl.getMemberCountList();
	}

	@Override
	public Integer searchCommentCount(String searchkey, String type) {
		Map params = new HashMap();
		params.put("searchkey", searchkey);
		params.put("type", type);

		return this.getHttpIntegerValue(params, commentAPIConfig.getSearchCommentCountURL(), HttpTimeout.SHORT_REQUEST);
	}
	
	@Override
	public List<Comment> searchCommentList(String searchkey, String type, List<Long> memberidList, int from, int maxnum) {
		Map params = new HashMap();

		params.put("searchkey", searchkey);
		params.put("type", type);
		params.put("from", String.valueOf(from));
		if(!CollectionUtils.isEmpty(memberidList)){
			params.put("memberids", StringUtils.join(memberidList, ","));
		}
		params.put("maxnum", String.valueOf(maxnum));

		return this.getHttpCommentList(params, commentAPIConfig.getSearchCommentListURL(), HttpTimeout.SHORT_REQUEST);
	}
	
	@Override
	public List<Comment> searchCommentList(String searchkey, String type, int from, int maxnum) {
		return searchCommentList(searchkey, type, null, from, maxnum);
	}

	@Override
	public Integer getMyAttentionCommentCountByMemberid(Long memberid) {
		Map params = new HashMap();

		params.put("memberid", memberid != null ? memberid.toString() : null);

		return getHttpIntegerValue(params, commentAPIConfig.getMyAttentionCommentCountByMemberidURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public List<Comment> getMyAttentionCommentListByMemberid(Long memberid, int from, int maxnum) {
		return this.getCommentList(null, memberid, null, null, from, maxnum);
	}

	@Override
	public Map getAllCommentList(List<Comment> commentList, String name) {
		Map model = new HashMap();
		if (StringUtils.isNotBlank(name))
			model.put(name, commentList);
		Set<Long> transferidList = new HashSet(BeanUtil.getBeanPropertyList(commentList, Long.class, "transferid", true));
		List<Comment> transferList = this.getCommentByIdList(transferidList);
		model.put("tranferCommentMap", BeanUtil.beanListToMap(transferList, "id"));
		List<Comment> all = new ArrayList<Comment>(commentList);
		all.addAll(transferList);
		Map<Long, List<String>> videosMap = new HashMap<Long, List<String>>();// 存储点评视频地址
		for (Comment comment : all) {
			if (StringUtils.isNotBlank(comment.getBody())) {
				List<String> videos = WebUtils.getVideos(comment.getBody());
				videosMap.put(comment.getId(), videos);
			}
		}
		model.put("videosMap", videosMap);
		return model;
	}

	@Override
	public List<Comment> getCommentsByTagAndAddress(String tag, String address, Timestamp starttime, Timestamp endtime, String topic, String handle,
			int from, int maxnum) {
		Map params = new HashMap();

		params.put("tag", tag);
		params.put("address", address);
		params.put("starttime", DateUtil.formatTimestamp(starttime));
		params.put("endtime", DateUtil.formatTimestamp(endtime));
		params.put("topic", topic);
		params.put("handle", handle);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));

		return this.getHttpCommentList(params, commentAPIConfig.getCommentsByTagAndAddressURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public List<Comment> getModeratorDetailList(String topic, boolean asc, int from, int maxnum) {
		Map params = new HashMap();

		params.put("topic", topic);
		params.put("asc", String.valueOf(asc));
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));

		return this.getHttpCommentList(params, commentAPIConfig.getModeratorDetailListURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public List<Comment> getCommentListByTagMemberids(String[] tag, List<Long> ids, Timestamp startTime, Timestamp endTime, int from, int maxnum) {
		Map params = new HashMap();

		params.put("tag", StringUtils.join(tag, ','));
		params.put("ids", StringUtils.join(ids, ','));
		if(startTime!=null)params.put("startTime", DateUtil.formatTimestamp(startTime));
		if(endTime!=null)params.put("endTime", DateUtil.formatTimestamp(endTime));
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));

		return this.getHttpCommentList(params, commentAPIConfig.getCommentListByTagMemberidsURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public List<Comment> getCommentListByMemberid(Long memberid, int from, int maxnum) {
		Map map = memberCountService.getMemberCount(memberid);
		if (map == null){
			return new ArrayList<Comment>();
		}
		String lastCids = (String) map.get(MemberStats.FIELD_LASTCINEMAID);
		List<Long> cinemaIdList = BeanUtil.getIdList(lastCids, ",");
		if (cinemaIdList.isEmpty()){
			return new ArrayList<Comment>();
		}
		Long cinemaid = cinemaIdList.get(0);
		List<Comment> commentList = getCommentListByRelatedId(TagConstant.TAG_CINEMA,null, cinemaid, "addtime", from, maxnum);
		return commentList;
	}

	@Override
	public List<Comment> pointByFreeBackCommentList(String tag, int from, int maxnum) {
		Map params = new HashMap();

		params.put("tag", tag);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));

		return this.getHttpCommentList(params, commentAPIConfig.getPointByFreeBackCommentListURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public List<Comment> getLongCommentList(String tag, Long relatedid, String status, int from, int maxnum) {
		Map params = new HashMap();

		params.put("tag", tag);
		params.put("relatedid", relatedid == null ? null : relatedid.toString());
		params.put("status", status);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		return this.getHttpCommentList(params, commentAPIConfig.getLongCommentListURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public List<Comment> getCommentList(Long memberid, Timestamp starttime, Timestamp endtime, String transfer, String status, String keyname,
			String isMicro, int from, int maxnum) {
		Map params = new HashMap();
		params.put("memberid", memberid == null ? null : memberid.toString());
		params.put("starttime", DateUtil.formatTimestamp(starttime));
		params.put("endtime", DateUtil.formatTimestamp(endtime));
		params.put("transfer", transfer);
		params.put("status", status);
		params.put("keyname", keyname);
		params.put("isMicro", isMicro);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		return this.getHttpCommentList(params, commentAPIConfig.getAdminGetCommentListURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public Integer getCommentCount(Long memberid, Timestamp starttime, Timestamp endtime, String transfer, String status, String keyname,
			String isMicro) {
		Map params = new HashMap();
		params.put("memberid", memberid == null ? null : memberid.toString());
		params.put("starttime", DateUtil.formatTimestamp(starttime));
		params.put("endtime", DateUtil.formatTimestamp(endtime));
		params.put("transfer", transfer);
		params.put("status", status);
		params.put("keyname", keyname);
		params.put("isMicro", isMicro);
		return this.getHttpIntegerValue(params, commentAPIConfig.getAdminGetCommentCountURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public Integer getLongCommentCount(String tag, Long relatedid, String status) {
		Map params = new HashMap();

		params.put("tag", tag);
		params.put("relatedid", relatedid != null ? relatedid.toString() : null);
		params.put("status", status);

		return this.getHttpIntegerValue(params, commentAPIConfig.getLongCommentCountURL(), HttpTimeout.SHORT_REQUEST);
	}
	@Override
	public Integer pointByFreeBackCommentCount(String tag) {
		Map params = new HashMap();

		params.put("tag", tag);
		return this.getHttpIntegerValue(params, commentAPIConfig.getPointByFreeBackCommentCountURL(), HttpTimeout.SHORT_REQUEST);

	}

	/*******************************************************************************************************************/


	/*
	 * 获取整形返回值
	 */
	private Integer getHttpIntegerValue(Map params, String url, int timeout) {
		HttpResult ec = this.postHttpRequestXML(url, params, timeout);
		if (ec.isSuccess()){
			if(isOk(ec.getResponse())){
				return Integer.valueOf(ec.getResponse());
			}
		}
		return 0;
	}

	/*
	 * 获取Comments返回值
	 */
	private List<Comment> getHttpCommentList(Map params, String url, int timeout) {
		CommentList comments = new CommentList();

		HttpResult ec = this.postHttpRequestXML(url, params, timeout);
		if (ec.isSuccess()) {
			String commentsXML = ec.getResponse();
			if(isOk(commentsXML)){
				BeanReader beanReader = ApiUtils.getBeanReader("commentList", CommentList.class);
				comments = (CommentList) ApiUtils.xml2Object(beanReader, XmlUtils.filterInvalid(commentsXML));
				if (comments == null) {
					dbLogger.error(commentsXML);
					return new ArrayList<Comment>(1);
				}
			}
		}

		return comments.getCommentList();
	}

	@Override
	public List<Comment> getCommentListByMemberIdAndTags(String[] tags, Long memberId, Date beginDate, Date endDate, int from, int maxnum) {
		Map params = new HashMap();

		params.put("memberId", memberId != null ? memberId.toString() : null);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		params.put("tags", StringUtils.join(tags, ","));
		params.put("beginTime", DateUtil.formatTimestamp(beginDate));
		params.put("endTime", DateUtil.formatTimestamp(endDate));

		return this.getHttpCommentList(params, commentAPIConfig.getCommentListByMemberIdAndTagsURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public void deleteComment(Long commentId) {
		Map params = new HashMap();
		params.put("commentId", commentId != null ? commentId.toString() : null);

		//HttpUtils.postUrlAsString(commentAPIConfig.getSiteURL() + commentAPIConfig.getDeleteCommentURL(), params);
		this.postHttpRequestXML(commentAPIConfig.getDeleteCommentURL(), params, HttpTimeout.SHORT_REQUEST);

	}

	@Override
	public List<Comment> getCommentListByTransfer(Long commentId, int from, int maxnum) {
		Map params = new HashMap();
		params.put("commentId", String.valueOf(commentId));
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));
		return this.getHttpCommentList(params, commentAPIConfig.getCommentListByTransferURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public List<Long> getTopAddMemberidList(String tag, int maxnum) {
		Map<String,String> params = new HashMap();
		params.put("maxnu", maxnum+"");
		if(StringUtils.isNotBlank(tag)) params.put("tag", tag);
		HttpResult result = this.postHttpRequestXML(commentAPIConfig.getTopAddMemberUrl(), params, HttpTimeout.SHORT_REQUEST);
		List<Long> idList = new ArrayList<Long>();
		if(!result.isSuccess() || !isOk(result.getResponse())){ 
			return idList;
		}
		BaseObjectResponse response = (BaseObjectResponse) ApiUtils.xml2Object(ApiUtils.getBeanReader("data", BaseObjectResponse.class), XmlUtils.filterInvalid(result.getResponse()));
		for(String str : response.getResult().split(",")){
			idList.add(Long.valueOf(str));
		}
		return idList;
	}
	
	
	@Override
	/**
	 * 根据日期、影片ID，获取影片哇啦数，为了缓解数据库压力，第一次获取总量，以后获取每天累加值
	 * @movieIds 影片ids
	 * @date date
	 * @type all 累计，one 当天 
	 */
	public List<CountByMovieIdAddDate> getCountByMovieIdAddDate(String movieIds, String type, Date date) {
		Map params = new HashMap();
		params.put("movieIds", movieIds);
		params.put("type", type);
		params.put("date", DateUtil.formatTimestamp(date));
		return this.getCountByMovieIdAddDateList(params, commentAPIConfig.getCountByMovieIdAddDateUrl());
	}

	private List<CountByMovieIdAddDate> getCountByMovieIdAddDateList(Map params, String url) {
		CountByMovieIdAddDateList countList = new CountByMovieIdAddDateList();

		HttpResult ec = this.postHttpRequestXML(url, params, HttpTimeout.SHORT_REQUEST);

		if (ec.isSuccess()) {
			String commentsXML = ec.getResponse();
			if (isOk(commentsXML)) {
				BeanReader beanReader = ApiUtils.getBeanReader("countList", CountByMovieIdAddDateList.class);
				countList = (CountByMovieIdAddDateList) ApiUtils.xml2Object(beanReader, XmlUtils.filterInvalid(commentsXML));
				if (countList == null) {
					dbLogger.error(commentsXML);
					return new ArrayList<CountByMovieIdAddDate>(1);
				}
			}
		}
		return countList.getCountList();
	}
	@Override
	public List<Map> getActiveMemberList(int maxnum) {
		String key = CacheConstant.buildKey("getAve4mbe32List", maxnum);
		List<Map> result = (List<Map>) cacheService.get(CacheConstant.REGION_ONEDAY, key);
		if (result == null) {
			result = new LinkedList<Map>();
			List<Long> memberidList = getTopAddMemberidList(null, maxnum);
			for (Long memberid: memberidList) {
				MemberInfo mi = daoService.getObject(MemberInfo.class, memberid);
				if(mi!=null){
					result.add(BeanUtil.getBeanMapWithKey(mi, "id", "nickname", "headpicUrl"));
				}
			}
			cacheService.set(CacheConstant.REGION_ONEDAY, key, result);
		}
		return result;
	}

	@Override
	public ErrorCode<Comment> addComment(Member member, String tag, Long relatedid, String body, String link, boolean ignoreInterval, String pointx,
			String pointy, String ip) {
		return addMicroComment(member, tag, relatedid, body, link, AddressConstant.ADDRESS_WEB, null, ignoreInterval, null, pointx, pointy, ip);
	}

	@Override
	public ErrorCode<Comment> addComment(Member member, String tag, Long relatedid, String body, String link, boolean ignoreInterval,
			Integer generalmark, String pointx, String pointy, String ip) {
		return addMicroComment(member, tag, relatedid, body, link, AddressConstant.ADDRESS_WEB, null, ignoreInterval, generalmark, pointx, pointy, ip);
	}

	@Override
	public ErrorCode<Comment> addMicroComment(Member member, String tag, Long relatedid, String body, String link, String address,
			boolean ignoreInterval, String pointx, String pointy, String ip) {
		return addMicroComment(member, tag, relatedid, body, link, address, null, ignoreInterval, null, pointx, pointy, ip);
	}
	@Override
	public ErrorCode<Comment> addMicroComment(Member member, String tag, Long relatedid, String body, String link, String address, Long transferid,
			boolean ignoreInterval, Integer generalmark, String otherInfo, String pointx, String pointy, String ip, String apptype) {
		String opkey = OperationService.TAG_ADDCONTENT + member.getId();
		if (!ignoreInterval) {
			if (!operationService.isAllowOperation(opkey, OperationService.HALF_MINUTE, 1)) {
				return ErrorCode.getFailure("发的这么快，手有点酸了吧，休息一下再继续吧！");
			}
		}
		Comment comment = null;
		if (transferid != null) {// 转载
			Comment transferComment = getCommentById(transferid);
			transferComment.addTransfercount();// 更新转载次数
			transferComment.setOrderTime(DateUtil.addHour(transferComment.getOrderTime(), 12));
			comment = new Comment(member, transferComment.getTag(), transferComment.getRelatedid(), body);
			comment.setTransferid(transferid);
			updateComment(transferComment);

		} else {// 发表
			String imgPath = StringUtil.findFirstByRegex(body, R);
			if (StringUtils.isNotBlank(imgPath)) {
				body = StringUtils.replace(body, imgPath, "");
				imgPath = imgPath.replaceAll(".*src=[\"'](.*)[\"'].*", "$1");
			}
			comment = new Comment(member, tag, relatedid, body, imgPath, link);
			if (StringUtils.isNotBlank(otherInfo))
				comment.setOtherinfo(otherInfo);
		}
		if (StringUtils.isNotBlank(address))
			comment.setAddress(address);
		if (generalmark != null) {
			if (generalmark < 1 || generalmark > 10)
				return ErrorCode.getFailure("评分有错误！");
			comment.setGeneralmark(generalmark);
		}
		operationService.updateOperation(opkey, OperationService.HALF_MINUTE, 1);
		comment.setPointx(pointx);
		comment.setPointy(pointy);
		if(StringUtils.isNotBlank(apptype)){
			comment.setApptype(apptype);
		}
		if(StringUtils.isNotBlank(ip))comment.setIp(ip);
		// 存储话题
		String topic = StringUtils.substringBetween(body, "#");
		if (StringUtils.isNotBlank(topic))
			comment.setTopic(topic);
		String key = blogService.filterContentKey(body);
		if (StringUtils.isNotBlank(key)) {
			comment.setStatus(Status.N_FILTER);
			String title = "有人发恶意评论：" + key;
			String content = "有人恶意评论，包含过滤关键字memberId = " + member.getId() + body;
			monitorService.saveSysWarn(Comment.class, comment.getId(), title, content, RoleTag.bbs);
			Long commentid = saveComment(comment);
			if (commentid == null){
				return ErrorCode.getFailure("系统远程连接错误！");
			}
			return ErrorCode.getFullErrorCode(ApiConstant.CODE_SIGN_ERROR, "内容中可能包含\"敏感\"关键词，管理员审核后才能在论坛显示", comment);
		}
		boolean bought = untransService.isPlayMemberByTagAndId(comment.getMemberid(), tag, relatedid);
		if (bought) {
			comment.addFlag(Flag.TICKET);
		}
		//用户前台认领

		/*// 只要发表就算完成新手任务 2012-02-17
		if (transferid == null) {
			MemberInfo info = daoService.getObject(MemberInfo.class, member.getId());
			if (!info.isFinishedTask(MemberConstant.TASK_MOVIE_COMMENT)) {
				memberService.saveNewTask(info, MemberConstant.TASK_MOVIE_COMMENT);
			}
		}*/
		
		Long commentId = saveComment(comment);
		if (commentId == null)
			return ErrorCode.getFailure("系统远程连接错误！");
		comment.setId(commentId);
		return ErrorCode.getSuccessReturn(comment);
	}

	@Override
	public ErrorCode<Comment> addMicroComment(Member member, String tag, Long relatedid, String body, String link, String address, Long transferid,
			boolean ignoreInterval, Integer generalmark, String pointx, String pointy, String ip) {
		return addMicroComment(member, tag, relatedid, body, link, address, transferid, ignoreInterval, generalmark, null, pointx, pointy, ip, null);
	}


	@Override
	public Comment getNewCommentByRelatedid(String tag, Long relatedId, Long memberid) {
		List<Comment> result = getCommentList(tag, relatedId, Status.Y, memberid, null, null, null, null, null, null,null, 0, 1);
		if (result.isEmpty())
			return null;
		return result.get(0);
	}

	@Override
	public Integer getCommentCountByTagAndAddress(String tag, String address, Timestamp starttime, Timestamp endtime, String topic, String handle) {
		Map params = new HashMap();

		params.put("tag", tag);
		params.put("address", address);
		params.put("starttime", DateUtil.formatTimestamp(starttime));
		params.put("endtime", DateUtil.formatTimestamp(endtime));
		params.put("topic", topic);
		params.put("handle", handle);

		return this.getHttpIntegerValue(params, commentAPIConfig.getCountCommentsByTagAndAddressURL(), HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public Integer getModeratorDetailCount(String topic) {
		Map params = new HashMap();
		params.put("topic", topic);

		return this.getHttpIntegerValue(params, commentAPIConfig.getModeratorDetailCountTopicURL(), HttpTimeout.SHORT_REQUEST);
	}
	@Override
	public void addReplyToComment(String mobile, String msg, String ip) {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		String strTime = DateUtil.format(cur, "yyyyMMddHHmmss");
		Timestamp lastplay = DateUtil.addMinute(cur, -60 * 48);
		GewaOrder gewaOrder = replyGewaOrder(mobile);
		ErrorCode<Comment> code = null;
		Member member = null;
		String pointType = "";
		String reason = "";
		String body = "";
		Map map = null;
		Timestamp starttime = null;
		Timestamp endtime = null;
		if (gewaOrder != null) {
			member = daoService.getObject(Member.class, gewaOrder.getMemberid());
			if (member != null) {
				String pointx = null, pointy = null;
				MemberSign sign = nosqlService.getMemberSign(member.getId());
				if(sign != null){
					pointx = Double.toString(sign.getPointx());
					pointy = Double.toString(sign.getPointy());
				}
				if (PayUtil.isTicketTrade(gewaOrder.getTradeNo())) {
					TicketOrder order = (TicketOrder) gewaOrder;
					OpenPlayItem item = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
					if (item != null && item.getPlaytime().after(lastplay) && item.getPlaytime().before(cur)) {
						Integer generalmark = null;
						String mark= "";
						if(msg.trim().length() >= 2){
							mark = StringUtils.substring(msg.trim(), 0, 2);
						}else if(msg.trim().length() == 1){
							mark = msg.trim();
						}
						if(StringUtils.equals(mark, "10")){
							generalmark = 10;
						}else if(StringUtils.isNotBlank(mark) && ValidateUtil.isNumber(mark.substring(0, 1))){
							generalmark = Integer.parseInt(mark.substring(0, 1));
							if(generalmark <= 0) generalmark = null;
						}
						Movie movie = daoService.getObject(Movie.class, item.getMovieid());
						if(generalmark == null){
							code = addMicroComment(member, TagConstant.TAG_MOVIE, item.getMovieid(), "#" + movie.getName() + "#" + msg,
								null, AddressConstant.ADDRESS_MOBILE, true, pointx, pointy, ip);
						}else{
							msg = msg.replaceFirst(generalmark + "", "");
							code = addMicroComment(member, TagConstant.TAG_MOVIE, item.getMovieid(), "#" + movie.getName() + "#" + msg,
								null, AddressConstant.ADDRESS_MOBILE, null, true,generalmark, pointx, pointy,ip);
							try{
								markService.saveOrUpdateMemberMark(TagConstant.TAG_MOVIE, item.getMovieid(), "generalmark", generalmark, member);
							}catch (Exception e) {
								dbLogger.warn("mobile reply:saveOrUpdateMemberMark", e);
							}
						}
						if (code != null && !code.isSuccess()) {
							dbLogger.warn("影评错误 " + mobile + ", 错误内容：" + code.getMsg());
						}
						Map param = new HashMap();
						param.put(MongoData.ACTION_TYPE, "integral");
						param.put(MongoData.ACTION_TAG, "movie");
						map = mongoService.findOne(MongoData.NS_INTEGRAL, param);
						if(map != null){
							pointType = PointConstant.REPLY_MESSAGEMOVIE;
							reason = "短信回复发表一句话影评";
							body = "恭喜你回复短息发影评，获得"+map.get("integral")+"积分奖励.";							
						}
					}
				} else if (PayUtil.isDramaOrder(gewaOrder.getTradeNo())) {
					DramaOrder order = (DramaOrder) gewaOrder;
					OpenDramaItem item = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid(), false);
					if (item != null && item.getPlaytime().after(lastplay) && item.getPlaytime().before(cur)) {
						Drama drama = daoService.getObject(Drama.class, item.getDramaid());
						code = addMicroComment(member, TagConstant.TAG_DRAMA, item.getDramaid(), "#" + drama.getName() + "#" + msg,
								null, AddressConstant.ADDRESS_MOBILE, true, pointx, pointy, ip);
						if (code != null && !code.isSuccess()) {
							dbLogger.warn("剧评错误 " + mobile + ", 错误内容：" + code.getMsg());
						}
						Map param = new HashMap();
						param.put(MongoData.ACTION_TYPE, "integral");
						param.put(MongoData.ACTION_TAG, "drama");
						map = mongoService.findOne(MongoData.NS_INTEGRAL, param);
						if(map != null){
							pointType = PointConstant.REPLY_MESSAGEDRAMA;
							reason = "短信回复发表一句话剧评";
							body = "恭喜你回复短息发剧评，获得"+map.get("integral")+"积分奖励.";
						}
					}
				} else if (PayUtil.isSportTrade(gewaOrder.getTradeNo())) {
					SportOrder order = (SportOrder) gewaOrder;
					OpenTimeTable item = daoService.getObject(OpenTimeTable.class, order.getOttid());
					Sport sport = daoService.getObject(Sport.class, order.getSportid());
					if (item != null && item.getPlaydate().after(lastplay) && item.getPlaydate().before(cur) && sport != null) {
						code = addMicroComment(member, TagConstant.TAG_SPORT, item.getSportid(), "#" + sport.getName() + "#" + msg,
								null, AddressConstant.ADDRESS_MOBILE, true, pointx, pointy, ip);
						if (code != null && !code.isSuccess()) {
							dbLogger.warn("运动错误 " + mobile + ", 错误内容：" + code.getMsg());
						}
						Map param = new HashMap();
						param.put(MongoData.ACTION_TYPE, "integral");
						param.put(MongoData.ACTION_TAG, "sport");
						map = mongoService.findOne(MongoData.NS_INTEGRAL, param);
						if(map != null){
							pointType = PointConstant.REPLY_MESSAGESPORT;
							reason = "短信回复发表运动场馆点评";
							body = "恭喜你回复短息发运动心得，获得"+map.get("integral")+"积分奖励.";
						}
					}
				}
			} else {
				Map<String, String> dataMap = new HashMap<String, String>();
				dataMap.put("content", msg);
				jsonDataService.saveJsonData(JsonDataKey.KEY_SMSREPLY + mobile + "_" + strTime, JsonDataKey.KEY_SMSREPLY, dataMap);
			}
		}
		if (code == null) {
			dbLogger.warn("短信回复错误" + mobile + (gewaOrder == null ? "" : ", 订单类型：" + gewaOrder.getOrdertype()));
		} else {
				if(map != null && StringUtils.equals(map.get("isSend")+"", "Y")){
					starttime = DateUtil.parseTimestamp(map.get("starttime")+"");
					endtime = DateUtil.parseTimestamp(map.get("endtime")+"");	
					if(cur.after(starttime) && cur.before(endtime)){
						if (member != null && code.isSuccess()) {
							Comment comment = code.getRetval();
							Point point = pointService.getPointByMemberiAndTagid(comment.getMemberid(), pointType, gewaOrder.getId());
							if (point == null) {
								Point p = pointService.addPointInfo(member.getId(), Integer.valueOf(map.get("integral")+""), reason, pointType, gewaOrder.getId(), null);
								if (p != null) {
									userMessageService.sendSiteMSG(comment.getMemberid(), SysAction.STATUS_RESULT, null, body);
								}
							}
						}
					dbLogger.warn("短信回复" + mobile + ", " + msg);
				}
			}
		}
	}	
	
	private GewaOrder replyGewaOrder(String mobile) {
		String tradeNo = memberCountService.getMobileLastTrade(mobile);
		if(StringUtils.isBlank(tradeNo)) return null;
		GewaOrder gewaOrder = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo);
		return gewaOrder;
	}
}
