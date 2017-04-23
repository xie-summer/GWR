package com.gewara.web.action.subject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.DiaryConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.VoteChoose;
import com.gewara.model.bbs.VoteOption;
import com.gewara.model.movie.Movie;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.untrans.CommentService;
import com.gewara.util.BeanUtil;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.AnnotationController;
@Controller
public class ViewGroupProxyController extends AnnotationController {
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@RequestMapping("/subject/proxy/getViewGroup.xhtml")
	public String getViewGroup(String tempId,ModelMap model){
		if(StringUtils.isBlank(tempId)){
			return showJsonError(model, "专题模版为空！");
		}
		Map viewGroupData = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, tempId);
		if(viewGroupData==null){
			return showJsonError(model, "专题模版为空！");
		}
		return showJsonSuccess(model,JsonUtils.writeObjectToJson(viewGroupData));
	}
	
	@RequestMapping("/subject/proxy/getViewItem.xhtml")
	public String getViewItem(String parantId,String tag,String signname,String citycode,ModelMap model){
		if(StringUtils.isBlank(parantId)){
			return showJsonError(model, "专题模版为空！");
		}
		Map params = new HashMap();
		params.put(MongoData.ACTION_PARENTID, parantId);//关联模版ID
		params.put(MongoData.ACTION_TAG, tag);
		params.put(MongoData.ACTION_SIGNNAME, signname);
		params.put(MongoData.ACTION_CITYCODE, citycode);
		if(StringUtils.equals(tag, "moduleConfig")){
			Map data = mongoService.findOne(MongoData.NS_MAINSUBJECT, params);
			return showJsonSuccess(model,JsonUtils.writeObjectToJson(data));
		}else{
			List<Map> list = mongoService.find(MongoData.NS_MAINSUBJECT, params, MongoData.ACTION_ORDERNUM, false);
			return showJsonSuccess(model, JsonUtils.writeObjectToJson(list));
		}
	}
	@RequestMapping("/subject/proxy/getViewCinemaBBSDetail.xhtml")
	public String getViewCinemaBBSDetail(String ids,ModelMap model){
		if(StringUtils.isBlank(ids)) showJsonSuccess(model);
		List<String> idList = Arrays.asList(StringUtils.split(ids, ","));
		List<Long> bbsIds = new ArrayList<Long>();
		for (String string : idList) {
			bbsIds.add(Long.parseLong(string));
		}
		List<Diary> diaryList = daoService.getObjectList(Diary.class, bbsIds);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(diaryList));
	}
	@RequestMapping("/subject/proxy/getViewCinemaBBSDetailByMovie.xhtml")
	public String getViewCinemaBBSDetailByMovie(String categoryids,ModelMap model){
		if(StringUtils.isBlank(categoryids)) showJsonSuccess(model);
		List<Long> idList = BeanUtil.getIdList(categoryids, ",");
		Map movieMap = daoService.getObjectMap(Movie.class, idList);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(movieMap));
	}
	
	@RequestMapping("/subject/proxy/getViewCommentCount.xhtml")
	public String getViewCommentCount(String title,String moderator,ModelMap model){
		int count = commentService.searchCommentCount(title, moderator);//查看话题数量
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(count));
	}
	
	@RequestMapping("/subject/proxy/getToupiaoList.xhtml")
	public String getToupiaoList(String tid,ModelMap model){
		if(StringUtils.isBlank(tid)) showJsonSuccess(model);
		Map toupiaoMap = this.voteDetail(Long.parseLong(tid));
		return showJsonSuccess(model,JsonUtils.writeObjectToJson(toupiaoMap));
	}
	
// 投票细节
	private Map voteDetail(Long vid) {
		Map model = new HashMap();
		List<VoteOption> voList = diaryService.getVoteOptionByVoteid(vid);
		Integer votecount = diaryService.getVotecount(vid);
		Map<Long, Long> perMap = new HashMap();
		if (votecount == 0) {
			for (VoteOption vo : voList) {
				perMap.put(vo.getId(), 0L);
			}
		} else {
			for (VoteOption vo : voList) {
				Double per = (vo.getSelectednum() / new Double(votecount)) * 100;
				perMap.put(vo.getId(), Math.round(per));
			}
		}
		model.put("voList", voList);
		model.put("votecount", votecount);
		model.put("perMap", perMap);
		return model;
	}
	
	@RequestMapping("/subject/proxy/getVoieList.xhtml")
	public String getVoieList(String did,ModelMap model){
		if(StringUtils.isBlank(did)) return showJsonSuccess(model);
		List<VoteChoose> vcList =  daoService.getObjectListByField(VoteChoose.class, "diaryid", Long.parseLong(did));
		Collections.sort(vcList, new PropertyComparator("addtime", false, false));
		return showJsonSuccess(model,JsonUtils.writeObjectToJson(vcList));
	}
	
	@RequestMapping("/subject/proxy/getDiaryCount.xhtml")
	public String getDiaryCount(String mid,String citycode,ModelMap model){
		if(StringUtils.isBlank(mid)) showJsonSuccess(model);
		if(StringUtils.isBlank(citycode)) citycode="310000";
		Integer diaryCount = diaryService.getDiaryCount(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, "movie", Long.parseLong(mid));
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(diaryCount));
	}
}
