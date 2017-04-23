package com.gewara.helper.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.json.PhoneActivity;
import com.gewara.json.Weixin2Wala;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.content.News;
import com.gewara.util.DateUtil;
import com.gewara.util.VmUtils;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;

public class GewaApiBbsHelper {
	//影评
	public static Map<String, Object> getMovieDiary(DiaryBase diary, String headpic, String content){
		String isbuy = StringUtils.contains(diary.getFlag(), "ticket")?"1":"0";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("diaryid", diary.getId());
		params.put("subject", diary.getSubject());
		params.put("addtime", diary.getAddtime());
		params.put("memberid", diary.getMemberid());
		params.put("nickname", diary.getMembername());
		if(StringUtils.isNotBlank(headpic)) params.put("headpic", headpic);
		params.put("summary", diary.getSummary());
		params.put("isbuy", isbuy);
		params.put("flowernum", diary.getFlowernum());
		params.put("poohnum", diary.getPoohnum());
		params.put("content", content); //不去除标签，app自己处理
		return params;
	}
	
	//哇啦
	public static Map<String, Object> getComment(Comment comment, String headpic){
		String isbuy = StringUtils.contains(comment.getFlag(), "ticket")?"1":"0";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("commentid", comment.getId());
		params.put("memberid", comment.getMemberid());
		params.put("tag", comment.getTag());
		params.put("relatedid", comment.getRelatedid());
		params.put("nickname", comment.getNickname());
		params.put("headpic", headpic);
		params.put("addtime", comment.getAddtime());
		params.put("timedesc", DateUtil.getTimeDesc(comment.getAddtime()));
		params.put("generalmark", comment.getGeneralmark());
		params.put("isbuy", isbuy);
		params.put("fromflag", comment.getFromFlag2());
		params.put("flowernum", comment.getFlowernum());
		params.put("replycount", comment.getReplycount());
		params.put("transfercount", comment.getTransfercount());
		params.put("flowernum", comment.getFlowernum());
		params.put("body", comment.getBody());
		return params;
	}

	//哇啦转载
	public static Map<String, Object> getTransferMap(Comment orgComment, Comment comment, String headpic, String picture){
		Map<String, Object> params = new HashMap<String, Object>();
		if(orgComment.getTransferid()==null) return params;
		params.put("transferid", comment.getId());
		params.put("transfermemberid", comment.getMemberid());
		params.put("transfernickname", comment.getNickname());
		params.put("transferlogo", headpic);
		params.put("transferbody", comment.getBody());
		if(StringUtils.isNotBlank(picture)) params.put("transferpicture", picture);
		params.put("transfergeneralmark", comment.getGeneralmark());
		return params;
	}
	//新闻
	public static Map<String, Object> getNews(News news, String logo, String smallLogo){
		String linksource = news.getLinksource();
		if(StringUtils.isBlank(linksource)) linksource = "格瓦拉生活网";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("newsid", news.getId());
		params.put("title", news.getTitle());
		params.put("logo", logo);
		params.put("smallLogo", smallLogo);
		if(StringUtils.isBlank(news.getSummary())){
			params.put("summary", VmUtils.getHtmlText(news.getContent(), 500));
		}else {
			params.put("summary", VmUtils.getHtmlText(news.getSummary(), 500));
		}
		params.put("addtime", news.getAddtime());
		params.put("newslabel", news.getNewslabel());
		params.put("author", news.getAuthor());
		params.put("clickedtimes", news.getClickedtimes());
		params.put("linksource", linksource);
		params.put("citycode", news.getCitycode());
		params.put("tag", news.getTag());
		params.put("relatedid", news.getRelatedid());
		params.put("categoryid", news.getCategoryid());
		params.put("category", news.getCategory());
		return params;
	}
	
	//活动
	public static Map<String, Object> getActivity(RemoteActivity activity, String headpic, String logo){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("activityid", activity.getId());
		params.put("title", activity.getTitle());
		params.put("atype", activity.getAtype());
		params.put("logo", logo);
		params.put("tag", activity.getTag());
		params.put("contactway", activity.getContactway());
		params.put("address", activity.getAddress());
		params.put("priceinfo", activity.getPriceinfo());
		params.put("signtype", activity.getSign());
		
		params.put("headpic", headpic);
		params.put("memberid", activity.getMemberid());
		params.put("nickname", activity.getMembername());
		params.put("contactway", activity.getContactway());
		params.put("qq", activity.getQq());
		params.put("needprepay", activity.getNeedprepay());
		
		params.put("clickedtimes", activity.getClickedtimes());
		params.put("membercount", activity.getMembercount());
		params.put("replycount", activity.getReplycount());
		
		params.put("startdate", activity.getStartdate());
		params.put("starttime", activity.getStarttime());
		params.put("enddate", activity.getEnddate());
		params.put("endtime", activity.getEndtime());
		params.put("duetime", activity.getDuetime());
		
		params.put("addtime", activity.getAddtime());
		params.put("replytime", activity.getReplytime());
		
		params.put("summary", activity.getSummary());
		params.put("bodyFirstPic", VmUtils.getBodyFirstPic(activity.getContentdetail()));
		params.put("content", VmUtils.getHtmlText(activity.getContentdetail(), 10000));
		
		return params;
	}
	//phoneactivity活动
	public static Map<String, Object> getPhoneActivity(PhoneActivity activity, String logo){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", activity.getId());
		params.put("activityid", activity.getId());
		params.put("title", activity.getTitle());
		params.put("logo", logo);
		params.put("summary", VmUtils.getHtmlText(activity.getContent(), 30));
		params.put("addtime", activity.getAddtime());
		return params;
	}
	
	public static Map<String, Object> getWeixin2Wala(Weixin2Wala wala, String picUrl){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", wala.getId());
		params.put("title", wala.getTitle());
		params.put("context", wala.getContext());
		params.put("addTime", wala.getAddTime());
		params.put("picUrl", picUrl);
		return params;
	}
}
