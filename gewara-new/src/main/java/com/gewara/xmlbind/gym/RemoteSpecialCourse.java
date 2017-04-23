package com.gewara.xmlbind.gym;

import java.io.Serializable;
import java.sql.Timestamp;


public class RemoteSpecialCourse implements Serializable{
	private static final long serialVersionUID = -8595838952649246114L;
	private Long id;
	private String code;
	private String displayname;
	private String briefname;
	private String coursename;
	private String coachnames;
	private Integer duration;
	private Integer classHour;
	private String classRoom;
	private String canBook;
	private Integer quantity;
	private String content;
	private String feature;
	private String logo;
	private Long courseid;
	private Timestamp addtime;
	private Timestamp modifyTime;
	private String status;
	private Long gymid;
	private String onlineStatus;
	private Integer generalmark;
	private Integer generalmarkedtimes;
	private Double avggeneral;
	private String source;
	private String synchStatus;
	private String parentItemName;
	private Long parentItemId;
	private RemoteCourse course;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDisplayname() {
		return displayname;
	}
	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}
	public String getBriefname() {
		return briefname;
	}
	public void setBriefname(String briefname) {
		this.briefname = briefname;
	}
	public String getCoursename() {
		return coursename;
	}
	public void setCoursename(String coursename) {
		this.coursename = coursename;
	}
	public String getCoachnames() {
		return coachnames;
	}
	public void setCoachnames(String coachnames) {
		this.coachnames = coachnames;
	}
	public Long getCourseid() {
		return courseid;
	}
	public void setCourseid(Long courseid) {
		this.courseid = courseid;
	}
	public Integer getDuration() {
		return duration;
	}
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
	public Integer getClassHour() {
		return classHour;
	}
	public void setClassHour(Integer classHour) {
		this.classHour = classHour;
	}
	public String getClassRoom() {
		return classRoom;
	}
	public void setClassRoom(String classRoom) {
		this.classRoom = classRoom;
	}
	public String getCanBook() {
		return canBook;
	}
	public void setCanBook(String canBook) {
		this.canBook = canBook;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getFeature() {
		return feature;
	}
	public void setFeature(String feature) {
		this.feature = feature;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public Timestamp getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Timestamp modifyTime) {
		this.modifyTime = modifyTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getGymid() {
		return gymid;
	}
	public void setGymid(Long gymid) {
		this.gymid = gymid;
	}
	public String getOnlineStatus() {
		return onlineStatus;
	}
	public void setOnlineStatus(String onlineStatus) {
		this.onlineStatus = onlineStatus;
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
	public Double getAvggeneral() {
		return avggeneral;
	}
	public void setAvggeneral(Double avggeneral) {
		this.avggeneral = avggeneral;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getSynchStatus() {
		return synchStatus;
	}
	public void setSynchStatus(String synchStatus) {
		this.synchStatus = synchStatus;
	}
	public String getParentItemName() {
		return parentItemName;
	}
	public void setParentItemName(String parentItemName) {
		this.parentItemName = parentItemName;
	}
	public Long getParentItemId() {
		return parentItemId;
	}
	public void setParentItemId(Long parentItemId) {
		this.parentItemId = parentItemId;
	}
	public String getName() {
		return getBriefname();
	}
	
	public String getLimg(){
		return getLogo();
	}
	public RemoteCourse getCourse() {
		return course;
	}
	public void setCourse(RemoteCourse course) {
		this.course = course;
	}
	
}
