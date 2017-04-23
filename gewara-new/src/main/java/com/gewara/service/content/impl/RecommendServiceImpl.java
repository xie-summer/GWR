package com.gewara.service.content.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.sys.MongoData;
import com.gewara.model.bbs.commu.CommuMember;
import com.gewara.mongo.MongoService;
import com.gewara.service.content.RecommendService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;

@Service("recommendService")
public class RecommendServiceImpl  extends BaseServiceImpl implements RecommendService{
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService){
		this.mongoService = mongoService;
	}
	
	@Override
	public List<Map> getCommuMyTreasureMember(Long commuid, Long memberid, String tag, String action) {
		String hql = "select new map(c.memberid as memberid) from CommuMember c, Treasure tr where c.commuid=? and c.memberid=tr.relatedid and tr.memberid=? and tag=? and action=?";
		List<Map> list = readOnlyTemplate.find(hql, commuid, memberid, tag , action);
		return list;
	}
	
	
	
	@Override
	public void memberAddFansCount(Long memberid, String type, String tag, Integer count){
		Map data = mongoService.findOne(MongoData.NS_PROMPT_INFO, MongoData.SYSTEM_ID, memberid);
		if(StringUtils.equals(type, "add")){
			if(data == null){
				data = new HashMap();
				data.put(tag, count);
				data.put(MongoData.SYSTEM_ID, memberid);
				mongoService.addMap(data, MongoData.SYSTEM_ID, MongoData.NS_PROMPT_INFO);
			}else{
				int num = 0;
				if(data.get(tag) != null) num = Integer.parseInt(data.get(tag)+"");
				data.put(tag, num + count);
				mongoService.saveOrUpdateMap(data, MongoData.SYSTEM_ID, MongoData.NS_PROMPT_INFO);
			}
		}else if(StringUtils.equals(type, "remove")){
			mongoService.removeObjectById(MongoData.NS_PROMPT_INFO, MongoData.SYSTEM_ID, memberid);
		}
	}
	//你关注的xx 等用户在这个群中，关注的用户所在的群。
	@Override
	public Map<Long, String> getCommuListByToSameMember(Long memberid, String tag, String action) {
		String hql = "from CommuMember where memberid in (select t.relatedid from Treasure t where t.memberid=? and t.tag=? and t.action=?)";
		List<CommuMember> commuMemberList = readOnlyTemplate.find(hql, memberid, tag, action);
		Map<Long, String> commuMap = new HashMap<Long, String>();
		for(CommuMember commuMember : commuMemberList){
			Long commuid = commuMember.getCommuid();
			if(commuMap.containsKey(commuid)){
				String memberids= commuMap.get(commuid);
				commuMap.put(commuid, memberids+","+commuMember.getMemberid());
			}else{
				commuMap.put(commuid, commuMember.getMemberid()+"");
			}
		}
		return commuMap;
	}
	/********************************************************************************************************************
	 * Common Function 针对专题(MongoDB) tag: 专题唯一标识 signanme: 专题指定类型(新闻, 视频, 论坛, 知道etc.)
	 */
	@Override
	public List<Map> getRecommendMap(String tag, String signname) {
		return getRecommendMap(tag, signname, null, 0, 0);
	}

	@Override
	public List<Map> getRecommendMap(String tag, String signname, Long relatedid, int from, int maxnum) {
		Map params = new HashMap();
		if (StringUtils.isNotBlank(tag)) {
			params.put(MongoData.ACTION_TAG, tag);
		}
		params.put(MongoData.ACTION_SIGNNAME, signname);
		if (relatedid != null) {
			params.put(MongoData.ACTION_RELATEDID, relatedid);
		}
		return mongoService.find(MongoData.NS_MAINSUBJECT, params, MongoData.ACTION_ORDERNUM, true, from, maxnum);
	}

	/**
	 * 专题子模块
	 */
	@Override
	public List<Map> getRecommendMap(String tag, String signname, String parentid) {
		Map params = new HashMap();
		if (StringUtils.isNotBlank(tag)) {
			params.put(MongoData.ACTION_TAG, tag);
		}
		params.put(MongoData.ACTION_SIGNNAME, signname);
		if (StringUtils.isNotBlank(parentid)) {
			params.put(MongoData.ACTION_PARENTID, parentid);
		}
		return mongoService.find(MongoData.NS_MAINSUBJECT, params, MongoData.ACTION_ORDERNUM, true);
	}
}
