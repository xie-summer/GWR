package com.gewara.model.bbs;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Flag;
import com.gewara.constant.Status;
import com.gewara.model.BaseObject;

public abstract class DiaryBase extends BaseObject {

	private static final long serialVersionUID = 4909399739817173386L;

	protected Long id;
	protected Long memberid;		//用户id
	protected String subject;		//标题
	protected Integer flowernum;	// 鲜花数
	protected Integer poohnum;		// 差评数
	protected Integer sumnum;		//总评
	protected Integer sumnumed;
	protected Timestamp addtime;	//添加时间
	protected Integer clickedtimes;	//点击次数
	protected String tag;			
	protected String category;		
	protected Long categoryid;		
	protected Long relatedid;
	protected String status; 		// 状态，如：被删除
	protected String summary; 		// 摘要，概要
	protected Timestamp replytime;	//最后回复时间
	protected Integer replycount;	//回复数量
	protected Long replyid;
	protected String type; 			// 一般帖、投票帖子、影评？
	protected Boolean viewed; 		// 查看过
	protected Timestamp updatetime;
	protected Date overdate; 		// 投票结束日期
	protected Timestamp utime; 		// 排序字段concat(REPLYTIME, UPDATETIME)
	protected Long communityid;
	protected String flag;
	protected String range;
	protected String membername;
	protected String replyname;
	protected String countycode;
	protected Long moderatorid;
	protected String citycode;
	protected String division;
	protected String otherinfo;
	protected String diaryImage;
	protected String ip;			//发帖用户IP
	@Override
	public Serializable realId() {
		return id;
	}
	public String getDiaryImage() {
		return diaryImage;
	}

	public void setDiaryImage(String diaryImage) {
		this.diaryImage = diaryImage;
	}

	public String getDivision() {
		return division;
	}

	public void setDivision(String division) {
		this.division = division;
	}

	public Long getModeratorid() {
		return moderatorid;
	}

	public void setModeratorid(Long moderatorid) {
		this.moderatorid = moderatorid;
	}

	public String getCountycode() {
		return countycode;
	}

	public void setCountycode(String countycode) {
		this.countycode = countycode;
	}

	public String getMembername() {
		return membername;
	}

	public void setMembername(String membername) {
		this.membername = membername;
	}

	public String getReplyname() {
		return replyname;
	}

	public void setReplyname(String replyname) {
		this.replyname = replyname;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	
	public void init(String stype, String stag, Long srelatedid, String scategory, Long scategoryid, String ssubject) {
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.type = stype;
		this.tag = stag;
		this.relatedid = srelatedid;
		this.category = scategory;
		this.categoryid = scategoryid;
		this.subject = ssubject;
		this.status = Status.Y_NEW;
	}

	public void addFlowernum() {
		this.flowernum += 1;
		
	}
	public void addPoohnum(){
		this.poohnum += 1;
	}

	public void addReplycount() {
		this.replycount += 1;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public Timestamp getReplytime() {
		return replytime;
	}

	public void setReplytime(Timestamp replytime) {
		this.replytime = replytime;
	}

	public Integer getReplycount() {
		return replycount;
	}

	public void setReplycount(Integer replycount) {
		this.replycount = replycount;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Timestamp getUtime() {
		return utime;
	}

	public void setUtime(Timestamp utime) {
		this.utime = utime;
	}

	public Boolean getViewed() {
		return viewed;
	}

	public void setViewed(Boolean viewed) {
		this.viewed = viewed;
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public Long getReplyid() {
		return replyid;
	}

	public void setReplyid(Long replyid) {
		this.replyid = replyid;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}


	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
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

	public Integer getClickedtimes() {
		return clickedtimes;
	}

	public void setClickedtimes(Integer clickedtimes) {
		this.clickedtimes = clickedtimes;
	}

	public Integer getFlowernum() {
		return flowernum;
	}

	public void setFlowernum(Integer flowernum) {
		this.flowernum = flowernum;
	}
	public Integer getPoohnum() {
		return poohnum;
	}

	public Integer getSumnum() {
		return sumnum;
	}
	public void setSumnum(Integer sumnum) {
		this.sumnum = sumnum;
	}
	public Integer getSumnumed() {
		return sumnumed;
	}
	public void setSumnumed(Integer sumnumed) {
		this.sumnumed = sumnumed;
	}
	public void setPoohnum(Integer poohnum) {
		if(poohnum == null)  poohnum = 0; 
		this.poohnum = poohnum;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDtag() {
		if (this.categoryid != null)
			return category;
		return tag;
	}

	public Long getDrelatedid() {
		if (this.categoryid != null)
			return categoryid;
		return relatedid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean getPic() {// 有无图片
		return StringUtils.contains(flag, Flag.FLAG_USERFILES);
	}

	public Date getOverdate() {
		return overdate;
	}

	public void setOverdate(Date overdate) {
		this.overdate = overdate;
	}

	public Long getCommunityid() {
		return communityid;
	}

	public void setCommunityid(Long communityid) {
		this.communityid = communityid;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}
	public void addFlag(String sflag){
		if(StringUtils.isBlank(this.flag)) this.flag = sflag;
		if(!StringUtils.contains(this.flag, sflag))
		this.flag += "," + sflag;
	}
	public String getCname() {
		return this.subject;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	
	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	
	public String getIp(){
		return ip;
	}
	
	public void setIp(String ip){
		this.ip = ip;
	}
	
	public String getLimg() {
		return diaryImage;
	}
	public abstract boolean canModify();
}
