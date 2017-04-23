package com.gewara.web.action.inner.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.ApiConstant;
import com.gewara.model.bbs.Diary;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.util.BeanUtil;
import com.gewara.web.action.api.BaseApiController;

@Controller
public class ApiDaryController extends BaseApiController {
	
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	
	/**
	 * 日志列表 
	 * @param tag 标签
	 * @param relateid 关联对象id
	 * @param type 类型  comment(影评、心得，剧评),topic_diary(一般帖子),topic_vote_radio(投票（单选）),topic_vote_multi(投票（多选）),topic(所有帖子),topic_vote(投票)
	 * @param orderField 排序字段
	 * @param from 当前页码
	 * @param maxnum 页大小
	 * @param returnField 返回字段 (diaryid,subject,memberid,nickname,memberlogo,summary,flowernum,replycount,diaryImage,content)
	 * @param model
	 * @return
	 */
	@RequestMapping("/inner/common/list/diaryListByTag.xhtml")
	public String diaryList(String tag, Long relateid, String type,	String orderField, String asc, Integer from, Integer maxnum,
			@RequestParam(defaultValue = "id,subject", required = false, value = "returnField") String returnField,
			ModelMap model){
		if(StringUtils.isBlank(tag) || from == null || maxnum== null || from <0 || maxnum <= 0 || StringUtils.isBlank(asc)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "参数错误！");
		if (maxnum > 50) {
			maxnum = 50;
		}
		if("diary".equals(type)){
			type = "%diary";
		}
		List<Diary> diaryList = diaryService.getDiaryListByOrder(Diary.class, null, type, tag, relateid, null, null, orderField, Boolean.parseBoolean(asc), from, maxnum);
		Map<Long,String> diaryContentMap = new HashMap<Long,String>();
		if(returnField.indexOf("content")!=-1){//详细内容
			for(Diary diary:diaryList){
				String diaryContent  = blogService.getDiaryBody(diary.getId());
				diaryContentMap.put(diary.getId(),diaryContent);
			}
			List<Long> memberidList = BeanUtil.getBeanPropertyList(diaryList, Long.class, "memberid", true);
			addCacheMember(model, memberidList);
		}
		
		int count=diaryService.getDiaryCount(Diary.class, null, type, tag, relateid);
		model.put("count", count);
		model.put("diaryList", diaryList);
		model.put("diaryContentMap", diaryContentMap);
		model.put("returnField", returnField);
 		return getXmlView(model,"inner/diary/diaryList.vm");
	}
	
	/**
	 * 根据帖子id集合查询帖子
	 * @param diaryIds 帖子id集合
	 * @param model
	 * @return
	 */
	@RequestMapping("/inner/common/diary/getIdList.xhtml")
	public String getDiaryList(String ids, ModelMap model){
		if(ids == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		List<Long> idList = BeanUtil.getIdList(ids, ",");
		if(!idList.isEmpty()){
			List<Diary> diaryList = daoService.getObjectList(Diary.class, idList);
			model.put("diaryList", diaryList);
		}
		return getXmlView(model,"inner/diary/diaryLists.vm");
	}
}
