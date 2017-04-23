package com.gewara.web.action.subject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.movie.Movie;
import com.gewara.model.user.Member;
import com.gewara.service.bbs.CommonVoteService;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;

// 年度评选
@Controller
public class EndYearChooseProxyController  extends AnnotationController {
	
	private static final String FLAG = "endYearChoose";
	
	@Autowired@Qualifier("commonVoteService")
	private CommonVoteService commonVoteService;
	
	// 虚拟造假投票
	@RequestMapping("/subject/proxy/choose/virtualVote.xhtml")
	public String virtualVote(String itemid, Integer support, ModelMap model){
		commonVoteService.addCommonVote(FLAG, itemid, support);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/subject/proxy/choose/isVote.xhtml")
	public String isVote(String tag, Long memberid, ModelMap model){
		Map<String, Object> voteMap = commonVoteService.getSingleVote(tag, memberid, null);
		if(voteMap == null){
			return showJsonSuccess(model);
		}else{
			Date addDate = DateUtil.parseDate(voteMap.get("addtime")+"","yyyy-MM-dd HH:mm:ss");
			Date endDate = DateUtil.addDay(addDate, 1);
			if(DateUtil.isAfter(endDate)) return showJsonSuccess(model, voteMap.get("itemid")+"");
			else return showJsonSuccess(model);
		}
	}
	
	@RequestMapping("/subject/proxy/choose/voteit.xhtml")
	public String voteit(Long memberid, String check, String itemid, String tag, ModelMap model){
		String checkcode = StringUtil.md5(memberid + "njmk5678");
		if(!StringUtils.equals(check, checkcode)) return showJsonSuccess(model, "请先登录。");
		Member member = daoService.getObject(Member.class, memberid);
		if(member == null) return showJsonSuccess(model, "请先登录。");
		//1. 活动时间: 20121220 ~ 20130120
		Date endDate = DateUtil.parseDate("2013-01-21 00:00:00");
		if(!DateUtil.isAfter(endDate)) return showJsonSuccess(model, "活动已结束！");
		//2. memberid + movieid 验证, 并增加投票数量
		Map<String, Object> voteMap = commonVoteService.getSingleVote(tag, memberid, null);
		if(voteMap != null){
			Date addDate = DateUtil.parseDate(voteMap.get("addtime")+"","yyyy-MM-dd HH:mm:ss");
			Date dueDate = DateUtil.addDay(addDate, 1);
			if(DateUtil.isAfter(dueDate)) return showJsonSuccess(model, "24小时内一类型只能投票一次！");
		}
		commonVoteService.addVoteMap(tag, itemid, memberid, FLAG);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/newsubject/endYearChoose.xhtml")
	public String bellydance(){
		return "admin/newsubject/after/endYearChoose.vm";
	}
	
	@RequestMapping("/admin/newsubject/getVoteInfo.xhtml")
	public String getVoteInfo(Integer pageNo, String tag, ModelMap model){
		if(pageNo == null) pageNo = 0;
		int rowsPerPage = 1000;
		int from = pageNo * rowsPerPage;
		List<Map> result = commonVoteService.getVoteInfo(tag, from, rowsPerPage);
		PageUtil pageUtil = new PageUtil(commonVoteService.getVoteInfoCount(tag), rowsPerPage, pageNo, "admin/newsubject/getVoteInfo.xhtml", true, true);
		Map params = new HashMap();
		params.put("tag", tag);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("dataList", result);
		for(Map dataMap : result){
			String itemid = (String) dataMap.get("itemid");
			Long memberid = (Long)dataMap.get("memberid");
			Member member = daoService.getObject(Member.class, memberid);
			dataMap.put("moviename", daoService.getObject(Movie.class, Long.parseLong(itemid)).getName());
			dataMap.put("nickname", member.getNickname());
			dataMap.put("mobile", member.getMobile());
		}
		return "admin/newsubject/after/voteInfo.vm";
	}
	
}