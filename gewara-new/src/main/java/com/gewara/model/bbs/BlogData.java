package com.gewara.model.bbs;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public class BlogData extends BaseObject {

	private static final long serialVersionUID = -4609626353436703287L;
	public static String[] disallowBindField = new String[]{"ukey", "addtime", "updatetime"};
	public static String KEY_DIARY_NAME = "diarycount";
	public static String KEY_COMMENT_NAME = "commentcount";
	public static String KEY_NEWS_NAME = "newscount";
	public static String KEY_ACTIVITY_NAME = "activitycount";
	public static String KEY_PICTURE_NAME = "picturecount";
	public static String KEY_VIDEO_NAME = "videocount";
	private String ukey;
	private String tag;
	private Long relatedid;
	private Integer diarycount;
	private Integer commentcount;
	private Integer newscount;
	private Integer activitycount;
	private Integer picturecount;
	private Integer videocount;
	private Timestamp addtime;
	private Timestamp updatetime;
	
	public BlogData(){}
	
	public BlogData(String tag, Long relatedid){
		this.ukey = relatedid + tag;
		this.tag = tag;
		this.relatedid = relatedid;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = this.addtime;
		this.diarycount = 0;
		this.commentcount = 0;
		this.newscount = 0;
		this.activitycount = 0;
		this.picturecount = 0;
		this.videocount = 0;
	}
	
	@Override
	public Serializable realId() {
		return ukey;
	}

	public String getUkey() {
		return ukey;
	}

	public void setUkey(String ukey) {
		this.ukey = ukey;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public Integer getDiarycount() {
		return diarycount;
	}

	public void setDiarycount(Integer diarycount) {
		this.diarycount = diarycount;
	}

	public Integer getCommentcount() {
		return commentcount;
	}

	public void setCommentcount(Integer commentcount) {
		this.commentcount = commentcount;
	}

	public Integer getNewscount() {
		return newscount;
	}

	public void setNewscount(Integer newscount) {
		this.newscount = newscount;
	}

	public Integer getPicturecount() {
		return picturecount;
	}

	public void setPicturecount(Integer picturecount) {
		this.picturecount = picturecount;
	}

	public Integer getVideocount() {
		return videocount;
	}

	public void setVideocount(Integer videocount) {
		this.videocount = videocount;
	}

	public Integer getActivitycount() {
		return activitycount;
	}

	public void setActivitycount(Integer activitycount) {
		this.activitycount = activitycount;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

}
