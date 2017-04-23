package com.gewara.xmlbind.gym;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.xmlbind.BaseInnerResponse;

public class RemoteCourse extends BaseInnerResponse {
	private Long id;
	private Long parentid;
	private String coursename;
	private String englishname;
	private String logo;
	private String content;
	private String commendlogo;
	private Integer thinindex;
	private Integer popularindex;
	private Integer sexyindex;
	private Integer flexindex;
	private String labelids;
	private String otherinfo;
	private Integer clickedtimes;
	private Integer together;
	private Integer playing;
	private Integer played;
	private Integer collectedtimes;
	private Timestamp addtime;
	private Timestamp updatetime;
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getCoursename() {
		return coursename;
	}

	public void setCoursename(String coursename) {
		this.coursename = coursename;
	}

	public String getName(){
		return coursename;
	}
	
	public Long getParentid() {
		return parentid;
	}

	public void setParentid(Long parentid) {
		this.parentid = parentid;
	}

	public String getUrl(){
		return "gym/course/" + getId();
	}
	
	public String getEnglishname() {
		return englishname;
	}

	public void setEnglishname(String englishname) {
		this.englishname = englishname;
	}
	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCommendlogo() {
		return commendlogo;
	}

	public void setCommendlogo(String commendlogo) {
		this.commendlogo = commendlogo;
	}

	public Integer getThinindex() {
		return thinindex;
	}

	public void setThinindex(Integer thinindex) {
		this.thinindex = thinindex;
	}

	public Integer getPopularindex() {
		return popularindex;
	}

	public void setPopularindex(Integer popularindex) {
		this.popularindex = popularindex;
	}

	public Integer getSexyindex() {
		return sexyindex;
	}

	public void setSexyindex(Integer sexyindex) {
		this.sexyindex = sexyindex;
	}

	public Integer getFlexindex() {
		return flexindex;
	}

	public void setFlexindex(Integer flexindex) {
		this.flexindex = flexindex;
	}

	public String getLabelids() {
		return labelids;
	}

	public void setLabelids(String labelids) {
		this.labelids = labelids;
	}

	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}

	public Integer getClickedtimes() {
		return clickedtimes;
	}

	public void setClickedtimes(Integer clickedtimes) {
		this.clickedtimes = clickedtimes;
	}

	public Integer getTogether() {
		return together;
	}

	public void setTogether(Integer together) {
		this.together = together;
	}

	public Integer getPlaying() {
		return playing;
	}

	public void setPlaying(Integer playing) {
		this.playing = playing;
	}

	public Integer getPlayed() {
		return played;
	}

	public void setPlayed(Integer played) {
		this.played = played;
	}

	public Integer getCollectedtimes() {
		return collectedtimes;
	}

	public void setCollectedtimes(Integer collectedtimes) {
		this.collectedtimes = collectedtimes;
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

	public String getSkey(){
		return coursename;
	}
	
	public boolean hasChild(){
		return this.parentid==null || this.parentid == 0;
	}
	
	public String getLimg() {
		return StringUtils.defaultString(logo, "img/default_pic.png");
	}
	
	public String getLimglogo() {
		if(StringUtils.isBlank(commendlogo)) return "img/default_pic.png";
		return commendlogo;
	}
	
	public String briefContent(int wordNumber){
		if(StringUtils.isBlank(content)) return null;
		String tmp = content.replaceAll("<p>", "").replaceAll("</p>", "");
		return "<p>" + StringUtils.abbreviate(tmp, wordNumber) + "</p>";
	}

}
