package com.gewara.xmlbind.gym;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.xmlbind.BaseInnerResponse;

public class RemoteCoach extends BaseInnerResponse {
	private Long id;
	private Long memberId;//关联的用户
	private String coachname;//中文名
	private String pinyin;//拼音
	private String englishname;//英文名
	private String gender;//性别
	private String birthday;//生日
	private String constellation;//星座
	private String special;//特长
	private String content;//简介
	private String experience;//工作经验
	private String honor;//个人荣誉
	private String contactphone; //联系电话
	private String logo; //代表图片
	private String workplace; //工作地点
	private String tag; //类别
	private String qq;
	private String msn;
	private String motto; //箴言
	private String coachtype; //教练类型：私教、课程教练 
	private String skill;
	private Integer studentnum; //学生数量
	private Integer xiangxue;   //想学的数量
	private Integer collectedtimes;
	private Integer hotvalue;
	private Timestamp addtime;//添加的时间
	private Timestamp updatetime;//修改的时间
	private String otherinfo;

	//评分
	private Integer generalmark;
	private Integer generalmarkedtimes;
	private Integer avggeneral;
	private Integer promark;
	private Integer promarkedtimes;
	private Integer avgpro;
	private Integer attitudemark;
	private Integer attitudemarkedtimes;
	private Integer avgattitudemark;
	private Integer interactivemark;
	private Integer interactivemarkedtimes;
	private Integer avginteractive;
	private Integer feelingmark;
	private Integer feelingmarkedtimes;
	private Integer avgfeeling;
	private Integer clickedtimes;
	
	private Integer flowernum;
	private Long gymid; 		//场馆ID
	private Long remoteid;
	private String status;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getCoachname() {
		return coachname;
	}

	public void setCoachname(String coachname) {
		this.coachname = coachname;
	}

	public String getCoachtype() {
		return coachtype;
	}

	public void setCoachtype(String coachtype) {
		this.coachtype = coachtype;
	}
	
	public String getName(){
		return coachname;
	}

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

	public String getEnglishname() {
		return englishname;
	}

	public void setEnglishname(String englishname) {
		this.englishname = englishname;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getConstellation() {
		return constellation;
	}

	public void setConstellation(String constellation) {
		this.constellation = constellation;
	}

	public String getSpecial() {
		return special;
	}

	public void setSpecial(String special) {
		this.special = special;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getExperience() {
		return experience;
	}

	public void setExperience(String experience) {
		this.experience = experience;
	}

	public String getHonor() {
		return honor;
	}

	public void setHonor(String honor) {
		this.honor = honor;
	}

	public String getContactphone() {
		return contactphone;
	}

	public void setContactphone(String contactphone) {
		this.contactphone = contactphone;
	}

	public String getWorkplace() {
		return workplace;
	}

	public void setWorkplace(String workplace) {
		this.workplace = workplace;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	public String getMsn() {
		return msn;
	}

	public void setMsn(String msn) {
		this.msn = msn;
	}

	public String getMotto() {
		return motto;
	}

	public void setMotto(String motto) {
		this.motto = motto;
	}

	public String getSkill() {
		return skill;
	}

	public void setSkill(String skill) {
		this.skill = skill;
	}

	public Integer getStudentnum() {
		return studentnum;
	}

	public void setStudentnum(Integer studentnum) {
		this.studentnum = studentnum;
	}

	public Integer getXiangxue() {
		return xiangxue;
	}

	public void setXiangxue(Integer xiangxue) {
		this.xiangxue = xiangxue;
	}

	public Integer getCollectedtimes() {
		return collectedtimes;
	}

	public void setCollectedtimes(Integer collectedtimes) {
		this.collectedtimes = collectedtimes;
	}

	public Integer getHotvalue() {
		return hotvalue;
	}

	public void setHotvalue(Integer hotvalue) {
		this.hotvalue = hotvalue;
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

	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}

	public Integer getGeneralmark() {
		return generalmark;
	}

	public void setGeneralmark(Integer generalmark) {
		this.generalmark = generalmark;
	}

	public Integer getGeneralmarkedtimes() {
		return generalmarkedtimes;
	}

	public void setGeneralmarkedtimes(Integer generalmarkedtimes) {
		this.generalmarkedtimes = generalmarkedtimes;
	}

	public Integer getAvggeneral() {
		return avggeneral;
	}

	public void setAvggeneral(Integer avggeneral) {
		this.avggeneral = avggeneral;
	}

	public Integer getPromark() {
		return promark;
	}

	public void setPromark(Integer promark) {
		this.promark = promark;
	}

	public Integer getPromarkedtimes() {
		return promarkedtimes;
	}

	public void setPromarkedtimes(Integer promarkedtimes) {
		this.promarkedtimes = promarkedtimes;
	}

	public Integer getAvgpro() {
		return avgpro;
	}

	public void setAvgpro(Integer avgpro) {
		this.avgpro = avgpro;
	}

	public Integer getAttitudemark() {
		return attitudemark;
	}

	public void setAttitudemark(Integer attitudemark) {
		this.attitudemark = attitudemark;
	}

	public Integer getAttitudemarkedtimes() {
		return attitudemarkedtimes;
	}

	public void setAttitudemarkedtimes(Integer attitudemarkedtimes) {
		this.attitudemarkedtimes = attitudemarkedtimes;
	}

	public Integer getAvgattitudemark() {
		return avgattitudemark;
	}

	public void setAvgattitudemark(Integer avgattitudemark) {
		this.avgattitudemark = avgattitudemark;
	}

	public Integer getInteractivemark() {
		return interactivemark;
	}

	public void setInteractivemark(Integer interactivemark) {
		this.interactivemark = interactivemark;
	}

	public Integer getInteractivemarkedtimes() {
		return interactivemarkedtimes;
	}

	public void setInteractivemarkedtimes(Integer interactivemarkedtimes) {
		this.interactivemarkedtimes = interactivemarkedtimes;
	}

	public Integer getAvginteractive() {
		return avginteractive;
	}

	public void setAvginteractive(Integer avginteractive) {
		this.avginteractive = avginteractive;
	}

	public Integer getFeelingmark() {
		return feelingmark;
	}

	public void setFeelingmark(Integer feelingmark) {
		this.feelingmark = feelingmark;
	}

	public Integer getFeelingmarkedtimes() {
		return feelingmarkedtimes;
	}

	public void setFeelingmarkedtimes(Integer feelingmarkedtimes) {
		this.feelingmarkedtimes = feelingmarkedtimes;
	}

	public Integer getAvgfeeling() {
		return avgfeeling;
	}

	public void setAvgfeeling(Integer avgfeeling) {
		this.avgfeeling = avgfeeling;
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

	public Long getGymid() {
		return gymid;
	}

	public void setGymid(Long gymid) {
		this.gymid = gymid;
	}

	public Long getRemoteid() {
		return remoteid;
	}

	public void setRemoteid(Long remoteid) {
		this.remoteid = remoteid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getFullname(){
		if(StringUtils.isBlank(this.englishname)) return this.coachname;
		else return coachname + "(" + englishname + ")";
	}
}
