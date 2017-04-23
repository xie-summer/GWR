package com.gewara.command;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class CommentCommand {
	public static final String TYPE_MODERATOR = "moderator";
	private Integer pageNumber;	//分页页码
	private Integer maxCount;		//条数
	public String tag;			//对象类型
	public Long relatedid;		//对象ID
	public String micrbody;		// 内容
	public String bodypic;		//图片地址
	public String video;		//视频
	public String link;			//连接
	public Integer generalmark;	//单个评分
	public String marks;		// 多评分
	private String isLongWala;	//是否是长哇啦
	public String pointxy;		//经纬度
	public String order;   // 哇啦排序flowernum 热门哇啦
	private String flag;  //值为ticket查询购票用户发表哇啦。
	
	//查询用
	private String hasMarks;	//是否有评分
	private String pages;		//是否分页
	private String isPic;		//是否有图片
	private String isVideo;		//是否有哇拉
	private String isFloor;		//是否需要楼层
	private String isCount;		//是否需要数量
	public String title;		//话题
	private String issue;		//是否有发表框
	private String isJson;		//是否是返回json
	private String isRight;		//是否是右边的哇啦
	private String isWide;		//是否宽屏
	private String isTicket;    //是否需要显示购票哇啦查询
	private String startTime;		//查询哇啦带时间
	private String endTime;
	
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
	public String getMicrbody() {
		return micrbody;
	}
	public void setMicrbody(String micrbody) {
		this.micrbody = micrbody;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public Integer getGeneralmark() {
		return generalmark;
	}
	public void setGeneralmark(Integer generalmark) {
		this.generalmark = generalmark;
	}
	public String getBodypic() {
		return bodypic;
	}
	public void setBodypic(String bodypic) {
		this.bodypic = bodypic;
	}
	public String getVideo() {
		return video;
	}
	public void setVideo(String video) {
		this.video = video;
	}
	public String getMarks() {
		return marks;
	}
	public void setMarks(String marks) {
		this.marks = marks;
	}
	
	public Integer getPageNumber() {
		if(pageNumber == null || pageNumber<0) pageNumber = 0;
		return pageNumber;
	}
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}
	public Integer getMaxCount() {
		if(maxCount == null || maxCount <1) maxCount = 6;
		return maxCount;
	}
	public void setMaxCount(Integer maxCount) {
		this.maxCount = maxCount;
	}
	public String getIsLongWala() {
		return isLongWala;
	}
	public void setIsLongWala(String isLongWala) {
		this.isLongWala = isLongWala;
	}
	
	public boolean hasLongWala(){
		return Boolean.parseBoolean(isLongWala);
	}
	
	public String getPointxy() {
		return pointxy;
	}
	public void setPointxy(String pointxy) {
		this.pointxy = pointxy;
	}
	public String getHasMarks() {
		return hasMarks;
	}
	public void setHasMarks(String hasMarks) {
		this.hasMarks = hasMarks;
	}
	
	public boolean hasMarks(){
		return Boolean.parseBoolean(hasMarks);
	}
	
	public String getPages() {
		return pages;
	}
	public void setPages(String pages) {
		this.pages = pages;
	}
	
	public boolean hasPages(){
		return Boolean.parseBoolean(pages);
	}
	
	public String getIsPic() {
		return isPic;
	}
	
	public void setIsPic(String isPic) {
		this.isPic = isPic;
	}
	
	public boolean hasPic(){
		return Boolean.parseBoolean(isPic);
	}
	
	public String getIsVideo() {
		return isVideo;
	}
	
	public void setIsVideo(String isVideo) {
		this.isVideo = isVideo;
	}
	
	public boolean hasVideo(){
		return Boolean.parseBoolean(isVideo);
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getIssue() {
		return issue;
	}
	public void setIssue(String issue) {
		this.issue = issue;
	}
	
	public boolean hasIssue(){
		return Boolean.parseBoolean(issue);
	}
	
	public static String getTypeModerator() {
		return TYPE_MODERATOR;
	}
	public String getIsJson() {
		return isJson;
	}
	public void setIsJson(String isJson) {
		this.isJson = isJson;
	}
	public String getIsRight() {
		return isRight;
	}
	public void setIsRight(String isRight) {
		this.isRight = isRight;
	}
	public boolean hasTag(String... tags){
		if(StringUtils.isBlank(tag) || ArrayUtils.isEmpty(tags)) return false;
		for (String str : tags) {
			if(StringUtils.equals(tag, str)) return true;
		}
		return false;
	}
	public String getIsFloor() {
		return isFloor;
	}
	public void setIsFloor(String isFloor) {
		this.isFloor = isFloor;
	}
	
	public boolean hasFloor(){
		return Boolean.parseBoolean(isFloor);
	}
	
	public String getIsCount() {
		return isCount;
	}
	public void setIsCount(String isCount) {
		this.isCount = isCount;
	}
	public boolean hasJson(){
		return Boolean.parseBoolean(isJson);
	}
	public boolean hasRight(){
		return Boolean.parseBoolean(isRight);
	}
	
	public boolean hasCount(){
		return Boolean.parseBoolean(isCount);
	}
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	public String getIsWide() {
		return isWide;
	}
	public void setIsWide(String isWide) {
		this.isWide = isWide;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getIsTicket() {
		return isTicket;
	}
	public void setIsTicket(String isTicket) {
		this.isTicket = isTicket;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
}
