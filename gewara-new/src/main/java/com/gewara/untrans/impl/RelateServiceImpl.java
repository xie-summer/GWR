package com.gewara.untrans.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.helper.sys.RelateClassHelper;
import com.gewara.model.BaseObject;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryHist;
import com.gewara.service.DaoService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.RelateService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.RelatedHelper;
@Service("relateService")
public class RelateServiceImpl implements RelateService{
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	
	

	@Override
	public Object getRelatedObject(String tag, Serializable relatedid){
		if(relatedid==null) return null;
		ErrorCode code = null;
		if(StringUtils.equals(tag, "activity")){
			code = synchActivityService.getRemoteActivity(relatedid);
		}else if(StringUtils.equals(tag, "gym")){
			code = synchGymService.getRemoteGym(relatedid, true);
 		}else if(StringUtils.equals(tag, "gymcarditem")){
 			code = synchGymService.getGymCardItem(relatedid, true);
		}else if(StringUtils.equals(tag, "gymcourse")){
			code = synchGymService.getRemoteCourse(relatedid, true);
		}else if(StringUtils.equals(tag, "gymcoach")){
			code = synchGymService.getRemoteCoach(relatedid, true);
		}else if(StringUtils.equals(tag, "comment")){
			return commentService.getCommentById((Long)relatedid);
		}else {
			Class<? extends BaseObject> clazz = RelateClassHelper.getRelateClazz(tag);
			if(clazz == null) return null;
			BaseObject result = daoService.getObject(clazz, relatedid);
			if(result == null && clazz.equals(Diary.class)){
				result = daoService.getObject(DiaryHist.class, relatedid);
			}
			return result;
		}
		if(code != null && code.isSuccess()) return code.getRetval();
		return null;
	}
	
	@Override
	public void addRelatedObject(int idx, String group, RelatedHelper rh, String tag, Collection<Serializable> idList){
		for(Serializable id: idList){
			addRelatedObject(idx, group, rh, tag, id);
		}
	}
	@Override
	public void addRelatedObject(int idx, String group, RelatedHelper rh, Map<Serializable/*relatedid*/, String/*tag*/> idTagMap){
		for(Serializable id: idTagMap.keySet()){
			addRelatedObject(idx, group, rh, idTagMap.get(id), id);
		}
	}
	
	@Override
	public void addRelatedObject(int idx, String group, RelatedHelper rh, String tag, Serializable id){
		Class clazz = RelateClassHelper.getRelateClazz(tag);
		ErrorCode code = null;
		if(clazz!=null){
			BaseObject relate = daoService.getObject(clazz, id);
			rh.addRelated(idx, group, id, relate);
		}else if(StringUtils.equals(tag, "gym")){
			code = synchGymService.getRemoteGym(id, true);
 		}else if(StringUtils.equals(tag, "gymcarditem")){
 			code = synchGymService.getGymCardItem(id, true);
		}else if(StringUtils.equals(tag, "gymcourse")){
			code = synchGymService.getRemoteCourse(id, true);
		}else if(StringUtils.equals(tag, "gymcoach")){
			code = synchGymService.getRemoteCoach(id, true);
		}
		if(code != null && code.isSuccess()) rh.addRelated(idx, group, id, code.getRetval());
	}
}
