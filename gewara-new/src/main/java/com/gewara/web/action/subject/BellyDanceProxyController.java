package com.gewara.web.action.subject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.sys.MongoData;
import com.gewara.mongo.MongoService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.activity.RemoteApplyjoin;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;

// 肚皮舞专题 代理
@Controller
public class BellyDanceProxyController  extends AnnotationController {

	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	
	@RequestMapping("/subject/proxy/bellydance/getNewsList.xhtml")
	public String getNewsList(ModelMap model){
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, MongoData.GYM_BELLYDANCE);
		params.put(MongoData.ACTION_TAG, "newsGYM");
		BasicDBObject query = new BasicDBObject();
		query.put(QueryOperators.GT, 0);
		params.put(MongoData.ACTION_ORDERNUM, query);
		List<Map> newsList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params, MongoData.ACTION_ORDERNUM, true, 0, 5);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(newsList));
	}
	@RequestMapping("/subject/proxy/bellydance/getVideoList.xhtml")
	public String getVideoList(ModelMap model){
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, MongoData.GYM_BELLYDANCE);
		params.put(MongoData.ACTION_TAG, "videoGYM");
		BasicDBObject query = new BasicDBObject();
		query.put(QueryOperators.GT, 0);
		params.put(MongoData.ACTION_ORDERNUM, query);
		List<Map> videoList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params, MongoData.ACTION_ORDERNUM, true, 0, 4);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(videoList));
	}
	@RequestMapping("/subject/proxy/bellydance/getPicList.xhtml")
	public String getPicList(ModelMap model){
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, MongoData.GYM_BELLYDANCE);
		params.put(MongoData.ACTION_TAG, "picGYM");
		BasicDBObject query = new BasicDBObject();
		query.put(QueryOperators.GT, 0);
		params.put(MongoData.ACTION_ORDERNUM, query);
		List<Map> picList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params, MongoData.ACTION_ORDERNUM, true, 0, 5);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(picList));
	}
	
	@RequestMapping("/subject/proxy/bellydance/getActivity.xhtml")
	public String getActivity(Long relatedid, ModelMap model){
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(relatedid);
		RemoteActivity activity = code.getRetval();
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(activity));
	}
	
	@RequestMapping("/subject/proxy/bellydance/isJionActivity.xhtml")
	public String isJionActivity(Long memberid, Long relatedid, ModelMap model){
		if(memberid == null) return showJsonError(model, "请先登录！");
		if(relatedid == null) return showJsonError(model, "参数错误！");
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(relatedid);
		if(!code.isSuccess()) {
			return showJsonError(model, "不存在此活动！");
		}
		ErrorCode<RemoteApplyjoin> code2 = synchActivityService.getApplyJoin(memberid, relatedid);
		String result="no";
		if(code2.isSuccess() && code2.getRetval() != null) result="jion";
		return showJsonSuccess(model, result);
	}
	
	@RequestMapping("/admin/newsubject/bellydance.xhtml")
	public String bellydance(){
		return "admin/newsubject/after/bellydance.vm";
	}
	
}