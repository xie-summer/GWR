package com.gewara.untrans.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gewara.config.CommentAPIConfig;
import com.gewara.constant.sys.HttpTimeout;
import com.gewara.model.user.Friend;
import com.gewara.model.user.Treasure;
import com.gewara.untrans.AbstractSynchBaseService;
import com.gewara.untrans.WalaApiService;
import com.gewara.util.ApiUtils;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.JsonUtils;
import com.gewara.xmlbind.bbs.ReComment;
import com.gewara.xmlbind.bbs.ReCommentList;

@Service("walaApiService")
public class WalaApiServiceImpl extends AbstractSynchBaseService implements WalaApiService{

	@Value("${openApi.walaAppkey}")
	private String openApiWalaAppkey = null;
	@Value("${openApi.walaSecretCode}")
	private String openApiWalaSecretCode = null;
	
	@Autowired@Qualifier("commentAPIConfig")
	private CommentAPIConfig commentAPIConfig;

	/**
	 * wala远程联接返回
	 * */
	protected HttpResult postWalaHttpRequest(String method, String format, Map<String, String> params, int timeout){
		return postOpenApiRequest(method, format, params, openApiWalaAppkey, openApiWalaSecretCode, timeout);
	}
	protected HttpResult postWalaHttpRequestXML(String method, Map<String, String> params, int timeout){
		return postOpenApiRequest(method, "xml", params, openApiWalaAppkey, openApiWalaSecretCode, timeout);
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
	public Long saveReComment(ReComment reComment) {
		Map params = new HashMap();
		params.put("reCommentJson", JsonUtils.writeObjectToJson(reComment));

		HttpResult ec = this.postWalaHttpRequestXML(commentAPIConfig.getSaveReCommentApiURL(), params, HttpTimeout.SHORT_REQUEST);
		if (ec.isSuccess() && isOk(ec.getResponse())){
			return Long.valueOf(ec.getResponse());
		}
		return null;
	}

	@Override
	public ReComment getReCommentById(Long reCommentId) {
		Map params = new HashMap(1);
		params.put("reCommentId", reCommentId != null ? reCommentId.toString() : null);

		HttpResult ec = this.postWalaHttpRequest(commentAPIConfig.getReCommentByIdURL(), "json", params, HttpTimeout.SHORT_REQUEST);
		ReComment recomment = null;
		if (ec.isSuccess() && isOk(ec.getResponse())) {
			String recommentJson = ec.getResponse();
			Map map = JsonUtils.readJsonToMap(recommentJson);

			recomment = new ReComment();
			BindUtils.bindData(recomment, map);
		}
		return recomment;
	}

	@Override
	public void updateReComment(ReComment recomment) {
		Map params = new HashMap();
		params.put("recommentJson", JsonUtils.writeObjectToJson(recomment));

		this.postWalaHttpRequestXML(commentAPIConfig.getUpdateReCommentURL(), params, HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public void deleteMicroReComment(Long mid) {
		Map params = new HashMap();

		params.put("mid", mid != null ? mid.toString() : null);

		this.postWalaHttpRequestXML(commentAPIConfig.getDeleteMicroReCommentURL(), params, HttpTimeout.SHORT_REQUEST);
	}

	@Override
	public List<ReComment> getReplyMeReCommentList(Long memberid, int from, int maxnum) {
		Map<String, String> params = new HashMap();
		params.put("memberid", memberid != null ? memberid.toString() : null);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));

		return this.getHttpReCommentList(params, commentAPIConfig.getReplyMeReCommentListURL());
	}

	@Override
	public void updateReCommentReadSatus(Long memberid) {
		Map<String, String> params = new HashMap();
		params.put("memberid", memberid != null ? memberid.toString() : null);
		this.getHttpIntegerValue(params, commentAPIConfig.getUpdateReCommentReadSatusURL());
	}

	@Override
	public void updateReplyCommentReadSatus(Long memberid) {
		Map<String, String> params = new HashMap();
		params.put("memberid", memberid != null ? memberid.toString() : null);
		this.getHttpIntegerValue(params, commentAPIConfig.getUpdateReplyCommentReadSatusURL());
	}

	@Override
	public Integer getReCommentCountByMemberid(Long memberid, String status) {
		Map<String, String> params = new HashMap();
		params.put("memberid", memberid != null ? memberid.toString() : null);
		params.put("status", status);

		return this.getHttpIntegerValue(params, commentAPIConfig.getReCommentCountByMemberidURL());
	}

	@Override
	public List<ReComment> getRecommentBycommentid(Long commentid, int from, int maxnum) {
		Map<String, String> params = new HashMap();
		params.put("commentid", commentid != null ? commentid.toString() : null);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));

		return this.getHttpReCommentList(params, commentAPIConfig.getRecommentBycommentidURL());
	}

	@Override
	public Integer getReplyMeCommentCount(Long memberid) {
		Map<String, String> params = new HashMap();
		params.put("memberid", memberid != null ? memberid.toString() : null);

		return this.getHttpIntegerValue(params, commentAPIConfig.getReplyMeCommentCountURL());
	}

	@Override
	public Integer getReplyMeReCommentCount(Long memberid) {
		Map<String, String> params = new HashMap();
		params.put("memberid", memberid != null ? memberid.toString() : null);

		return this.getHttpIntegerValue(params, commentAPIConfig.getReplyMeReCommentCountURL());
	}

	@Override
	public List<ReComment> getReCommentByRelatedidAndTomemberid(Long relatedid, Long tomemberid, Long memberid, int from, int maxnum) {
		Map<String, String> params = new HashMap();
		params.put("relatedid", relatedid != null ? relatedid.toString() : null);
		params.put("tomemberid", tomemberid != null ? tomemberid.toString() : null);
		params.put("memberid", memberid != null ? memberid.toString() : null);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));

		return this.getHttpReCommentList(params, commentAPIConfig.getReCommentByRelatedidAndTomemberidURL());
	}

	@Override
	public Integer getReCommentByRelatedidAndTomemberidCount(Long relatedid, Long tomemberid, Long memberid) {
		Map<String, String> params = new HashMap();
		params.put("relatedid", relatedid != null ? relatedid.toString() : null);
		params.put("tomemberid", tomemberid != null ? tomemberid.toString() : null);
		params.put("memberid", memberid != null ? memberid.toString() : null);

		return this.getHttpIntegerValue(params, commentAPIConfig.getReCommentByRelatedidAndTomemberidCountURL());
	}

	@Override
	public Integer getMicroSendReCommentCount(Long memberid) {
		Map<String, String> params = new HashMap();
		params.put("memberid", memberid != null ? memberid.toString() : null);

		return this.getHttpIntegerValue(params, commentAPIConfig.getMicroSendReCommentCountURL());
	}

	@Override
	public List<ReComment> getMicroSendReCommentList(Long memberid, int from, int maxnum) {
		Map<String, String> params = new HashMap();
		params.put("memberid", memberid != null ? memberid.toString() : null);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));

		return this.getHttpReCommentList(params, commentAPIConfig.getMicroSendReCommentListURL());
	}

	@Override
	public Integer getMicroReceiveReCommentCount(Long memberid) {
		Map<String, String> params = new HashMap();
		params.put("memberid", memberid != null ? memberid.toString() : null);

		return this.getHttpIntegerValue(params, commentAPIConfig.getMicroReceiveReCommentCountURL());
	}

	@Override
	public List<ReComment> getMicroReceiveReCommentList(Long memberid, int from, int maxnum) {
		Map<String, String> params = new HashMap();
		params.put("memberid", memberid != null ? memberid.toString() : null);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));

		return this.getHttpReCommentList(params, commentAPIConfig.getMicroReceiveReCommentListURL());
	}

	@Override
	public List<ReComment> getReplyCommentListByAtMe(String nickName, int from, int maxnum) {
		Map<String, String> params = new HashMap();
		params.put("nickName", nickName);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));

		return this.getHttpReCommentList(params, commentAPIConfig.getReplyCommentListByAtMeURL());
	}

	@Override
	public Integer getReCommentCountByRelatedidAndTomemberid(Long relatedid, Long tomemberid, Long memberid) {
		Map<String, String> params = new HashMap();
		params.put("relatedid", relatedid != null ? relatedid.toString() : null);
		params.put("tomemberid", tomemberid != null ? tomemberid.toString() : null);
		params.put("memberid", memberid != null ? memberid.toString() : null);

		return this.getHttpIntegerValue(params, commentAPIConfig.getReCommentCountByRelatedidAndTomemberidURL());
	}

	@Override
	public List<ReComment> getReCommentList(Long cid, Long memberid, Timestamp starttime, Timestamp endtime, String status, String keyname, int from, int maxnum) {
		Map<String, String> params = new HashMap();
		params.put("cid", cid == null ? null : cid.toString());
		params.put("memberid", memberid == null ? null : memberid.toString());
		params.put("starttime", DateUtil.formatTimestamp(starttime));
		params.put("endtime", DateUtil.formatTimestamp(endtime));
		params.put("status", status);
		params.put("keyname", keyname);
		params.put("from", String.valueOf(from));
		params.put("maxnum", String.valueOf(maxnum));

		return this.getHttpReCommentList(params, commentAPIConfig.adminGetReCommentListURL());
	}

	@Override
	public Integer getReCommentCount(Long cid, Long memberid, Timestamp starttime, Timestamp endtime, String status, String keyname) {
		Map<String, String> params = new HashMap();
		params.put("cid", cid == null ? null : cid.toString());
		params.put("memberid", memberid == null ? null : memberid.toString());
		params.put("starttime", DateUtil.formatTimestamp(starttime));
		params.put("endtime", DateUtil.formatTimestamp(endtime));
		params.put("status", status);
		params.put("keyname", keyname);
		return this.getHttpIntegerValue(params, commentAPIConfig.getAdminGetReCommentCountURL());
	}

	/*******************************************************************************************************************/


	/*
	 * 获取整形返回值
	 */
	private Integer getHttpIntegerValue(Map params, String url) {
		HttpResult ec = this.postWalaHttpRequestXML(url, params, HttpTimeout.SHORT_REQUEST);

		if (ec.isSuccess() && isOk(ec.getResponse()))
			return Integer.valueOf(ec.getResponse());
		else
			return 0;
	}

	/*
	 * 获取Comments返回值
	 */
	private List<ReComment> getHttpReCommentList(Map params, String url) {
		ReCommentList reComments = new ReCommentList();

		HttpResult ec = this.postWalaHttpRequestXML(url, params, HttpTimeout.SHORT_REQUEST);
		if (ec.isSuccess() && isOk(ec.getResponse())) {
			String commentsXML = ec.getResponse();
			BeanReader beanReader = ApiUtils.getBeanReader("reCommentList", ReCommentList.class);
			reComments = (ReCommentList) ApiUtils.xml2Object(beanReader, commentsXML);
			if (reComments == null) {
				dbLogger.error(commentsXML);
			}
		}
		if (reComments == null) {
			return new ArrayList<ReComment>(1);
		}
		return reComments.getReCommentList();
	}

	@Override
	public void addTreasure(Treasure treasure) {
		Map params = new HashMap();
		params.put("treasureJson", JsonUtils.writeObjectToJson(treasure));
		this.postWalaHttpRequestXML(commentAPIConfig.getAddTreasureURL(), params, HttpTimeout.SHORT_REQUEST);

	}

	@Override
	public void delTreasure(Long memberId, Long relatedId, String tag, String action) {
		Map params = new HashMap();
		params.put("memberId", memberId != null ? memberId.toString() : null);
		params.put("relatedId", relatedId != null ? relatedId.toString() : null);
		params.put("tag", tag);
		params.put("action", action);

		this.postWalaHttpRequestXML(commentAPIConfig.getDelTreasureURL(), params, HttpTimeout.SHORT_REQUEST);

	}

	@Override
	public void addFriend(Friend friend) {
		Map params = new HashMap();
		params.put("friendJson", JsonUtils.writeObjectToJson(friend));

		this.postWalaHttpRequestXML(commentAPIConfig.getAddFriendURL(), params, HttpTimeout.SHORT_REQUEST);

	}

	@Override
	public void delFriend(Long memberFrom, Long memberTo) {
		Map params = new HashMap();
		params.put("memberFrom", memberFrom != null ? memberFrom.toString() : null);
		params.put("memberTo", memberTo != null ? memberTo.toString() : null);

		this.postWalaHttpRequestXML(commentAPIConfig.getDelFriendURL(), params, HttpTimeout.SHORT_REQUEST);
	}
}
