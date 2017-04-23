package com.gewara.model.bbs.qa;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;

public class GewaQuestion extends BaseObject {
	private static final long serialVersionUID = 5578727148166770087L;
	public static Map<String,String> ssMap = new HashMap<String,String>();
	public static final int HOTVALUE_HOT = 30000; // 热门
	public static final int HOTVALUE_RECOMMEND = 50000; // 推荐
	public static final String QS_STATUS_N = "N"; //待解决
	public static final String QS_STATUS_Y = "Y"; //已解决
	public static final String QS_STATUS_Z = "Z"; //零解决
	public static final String QS_STATUS_NOPROPER = "noproper"; //无满意答案
	public static final Integer MAXDAYS = 15; //最大过期时间
	private Long id;
	private String title; // 标题
	private String content; // 内容
	private String addinfo; //补充
	private Integer reward; // 悬赏分
	private String tag; // 版块：movie,gym,....
	private String category;
	private Long categoryid;
	private Long relatedid;
	private Long memberid; // 提问人
	private Integer replycount; // 共回复次数
	private Long replymemberid; // 最后回复人
	private Integer clickedtimes;
	private Integer hotvalue;
	private String questionstatus; // 解决状态 待解决:N, 已解决：Y, 零解决：Z  无满意答案 noproper
	private String status;// 删除状态 未删除：N 已删除：Y
	private Timestamp addtime;
	private Timestamp updatetime;
	private Timestamp modtime;	//最后修改时间
	private Timestamp addinfotime;
	private Timestamp recommendtime;
	private Timestamp dealtime;
	private Long tomemberid; // 向Ta提问
	private String countycode;
	private String membername;
	private String citycode;
	private String ip; //提问者IP
	private BaseObject relate;
	private BaseObject relate2;
	
	public String getMembername() {
		return membername;
	}

	public void setMembername(String membername) {
		this.membername = membername;
	}

	public String getCountycode() {
		return countycode;
	}

	public void setCountycode(String countycode) {
		this.countycode = countycode;
	}
	public GewaQuestion(){}
	
	public GewaQuestion(Long memberid) {
		this.memberid = memberid;
		this.hotvalue = 0;
		this.replycount = 0;
		this.clickedtimes = 0;
		this.questionstatus = QS_STATUS_Z;
		this.status = Status.Y_NEW;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.updatetime = addtime;
		this.recommendtime = addtime;
		this.modtime = addtime;
		this.reward = 0;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public Serializable realId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Integer getReplycount() {
		return replycount;
	}

	public void setReplycount(Integer replycount) {
		this.replycount = replycount;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public Long getReplymemberid() {
		return replymemberid;
	}

	public void setReplymemberid(Long replymemberid) {
		this.replymemberid = replymemberid;
	}

	public Long getTomemberid() {
		return tomemberid;
	}

	public void setTomemberid(Long tomemberid) {
		this.tomemberid = tomemberid;
	}

	public Integer getClickedtimes() {
		return clickedtimes;
	}

	public void setClickedtimes(Integer clickedtimes) {
		this.clickedtimes = clickedtimes;
	}

	public Integer getHotvalue() {
		return hotvalue;
	}

	public void setHotvalue(Integer hotvalue) {
		this.hotvalue = hotvalue;
	}

	public String getQuestionstatus() {
		return questionstatus;
	}

	public void setQuestionstatus(String questionstatus) {
		this.questionstatus = questionstatus;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Long getCategoryid() {
		return categoryid;
	}

	public void setCategoryid(Long categoryid) {
		this.categoryid = categoryid;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public Integer getReward() {
		return reward;
	}

	public void setReward(Integer reward) {
		this.reward = reward;
	}

	public void addReplycount() {
		this.replycount++;
	}

	public Timestamp getRecommendtime() {
		return recommendtime;
	}

	public void setRecommendtime(Timestamp recommendtime) {
		this.recommendtime = recommendtime;
	}
	public String getTagname(){
		return ssMap.get(this.tag);
	}
	static{
		ssMap.put("cinema", "看电影");
		ssMap.put("gym", "去健身");
		ssMap.put("bar", "泡酒吧");
		ssMap.put("sport", "做运动");
		ssMap.put("ktv", "KTV");
		ssMap.put("theatre", "话剧");
		ssMap.put("activity", "活动");
		ssMap.put("gymcard", "健身卡");
		ssMap.put("gymcurriculum", "健身课程");
		ssMap.put("", "其它");
	}
	public String getAddinfo() {
		return addinfo;
	}

	public void setAddinfo(String addinfo) {
		this.addinfo = addinfo;
	}
	
	public Timestamp getAddinfotime() {
		return addinfotime;
	}

	public void setAddinfotime(Timestamp addinfotime) {
		this.addinfotime = addinfotime;
	}

	public Timestamp getDealtime() {
		return dealtime;
	}

	public void setDealtime(Timestamp dealtime) {
		this.dealtime = dealtime;
	}
	public Timestamp getModtime() {
		return modtime;
	}
	public void setModtime(Timestamp modtime) {
		this.modtime = modtime;
	}
	public String getCname() {
		return this.title;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public String getIp(){
		return ip;
	}
	public void setIp(String ip){
		this.ip = ip;
	}
	
	public BaseObject getRelate() {
		return relate;
	}

	public void setRelate(BaseObject relate) {
		this.relate = relate;
	}

	public BaseObject getRelate2() {
		return relate2;
	}

	public void setRelate2(BaseObject relate2) {
		this.relate2 = relate2;
	}
}
