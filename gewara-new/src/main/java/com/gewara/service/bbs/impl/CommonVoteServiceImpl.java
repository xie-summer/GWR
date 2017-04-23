package com.gewara.service.bbs.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.sys.MongoData;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.CommonVoteService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.util.DateUtil;
import com.gewara.util.ObjectId;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
@Service("commonVoteService")
public class CommonVoteServiceImpl extends BaseServiceImpl implements CommonVoteService {
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	
	public static final String NAMESPACE = MongoData.NS_COMMON_VOTE;
	
	@Override
	public void addCommonVote(String flag, String itemid, Integer support){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("flag", flag);
		params.put("itemid", itemid);
		Map data = mongoService.findOne(MongoData.NS_COMMON_VOTE, params);
		if(data == null){
			Map newdataMap = new HashMap();
			newdataMap.put(MongoData.SYSTEM_ID, ObjectId.uuid());
			newdataMap.put("flag", flag);
			newdataMap.put("itemid", itemid);
			newdataMap.put("support", support);
			mongoService.addMap(newdataMap, MongoData.SYSTEM_ID, NAMESPACE);
		}else{
			Map destMap = new HashMap(data);
			destMap.put("support", support);
			mongoService.update(NAMESPACE, data, destMap);
		}
	}

	@Override
	public Map<String, Object> getSingleVote(String tag, Long memberid, String itemid) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("tag", tag);
		params.put("memberid", memberid);
		if(StringUtils.isNotBlank(itemid))params.put("itemid", itemid);
		List<Map> voteMapList = mongoService.find(NAMESPACE, params, "addtime", false, 0, 1);
		if(voteMapList == null || voteMapList.isEmpty()){
			return null;
		}else{
			return voteMapList.get(0);
		}
	}

	@Override
	public void addVoteMap(String tag, String itemid, Long memberid, String flag) {
		
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("tag", tag);
		paramsMap.put("itemid", itemid);
		paramsMap.put("memberid", memberid);
		Map dataMap = mongoService.findOne(NAMESPACE, paramsMap);
		if(dataMap == null){
			dataMap = new HashMap<String, Object>();
			dataMap.putAll(paramsMap);
			dataMap.put(MongoData.SYSTEM_ID, ObjectId.uuid());
		}
		dataMap.put("addtime", DateUtil.getCurFullTimestampStr());
		mongoService.addMap(dataMap, MongoData.SYSTEM_ID, NAMESPACE);
		
		// 查找到原来投票VO. support+1, 并返回
		addCommonVote(flag, itemid, getSupportCount(flag, itemid)+1);
	}
	
	@Override
	public List<Map> getVoteInfo(String tag, int from, int maxnum){
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("tag", tag);
		return mongoService.find(NAMESPACE, paramsMap, "addtime", false, from, maxnum);
	}
	
	@Override
	public int getVoteInfoCount(String tag){
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("tag", tag);
		return mongoService.getCount(NAMESPACE, paramsMap);
	}

	@Override
	public List<Map> getItemVoteList(String flag) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("flag", flag);
		return mongoService.find(NAMESPACE, params);
	}
	
	@Override
	public Integer getSupportCount(String flag, String itemid){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("flag", flag);
		params.put("itemid", itemid);
		Map data = mongoService.findOne(MongoData.NS_COMMON_VOTE, params);
		if(data == null) return 0;
		return (Integer) data.get("support");
	}
	
	@Override
	public void delVote(String id){
		mongoService.removeObjectById(NAMESPACE, MongoData.SYSTEM_ID, id);
	}
}
