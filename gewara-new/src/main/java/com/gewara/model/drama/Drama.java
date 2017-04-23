package com.gewara.model.drama;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.common.BaseEntity;
import com.gewara.util.DateUtil;

public class Drama extends BaseEntity {
	private static final long serialVersionUID = -505984720950483214L;
	
	private String language;
	private String dramaname;
	private String dramaalias;
	private String director;
	private String playwright;
	private String actors;
	private Date releasedate;
	private Date enddate;
	private String type;
	private String website;
	private String length;
	private String state;
	private String highlight;
	private String actorstext;//主演文本
	private String directortext;//导演文本
	private String troupecompany;//出品剧社
	private String troupecompanytext;//出品剧社文本
	//add 20101215 start
	private String dramacompany;//出品方
	private String dramadata;//
	//20110210
	private String dramatype;//话剧类型
	//20110401
	private String playinfo; //放映时间
	private Integer boughtcount;	// 购票人次
	private String citycode;
	private String actorcontent;	//演员的介绍
	
	private String otherinfo;
	private String pretype;			//预售类型
	private String saleCycle;		//预售周期
	private String prices;		//价格数据"已, 隔开"
	
	public Drama(){}
	
	public Drama(String dramaname) {
		this.generalmark = 21;
		this.generalmarkedtimes = 3;
		this.quguo = 1;
		this.xiangqu = 1;
		this.clickedtimes = 20;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.collectedtimes = 1;
		this.enddate = new Timestamp(System.currentTimeMillis());
		this.dramaname = dramaname;
		this.boughtcount = 0;
	}

	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getDramaname() {
		return dramaname;
	}
	public void setDramaname(String dramaname) {
		this.dramaname = dramaname;
	}
	public String getDramaalias() {
		return dramaalias;
	}
	public void setDramaalias(String dramaalias) {
		this.dramaalias = dramaalias;
	}
	public String getDirector() {
		return director;
	}
	public void setDirector(String director) {
		this.director = director;
	}
	public String getPlaywright() {
		return playwright;
	}
	public void setPlaywright(String playwright) {
		this.playwright = playwright;
	}
	public String getActors() {
		return actors;
	}
	public void setActors(String actors) {
		this.actors = actors;
	}
	public Date getReleasedate() {
		return releasedate;
	}
	public void setReleasedate(Date releasedate) {
		this.releasedate = releasedate;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getLength() {
		return length;
	}
	public void setLength(String length) {
		this.length = length;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getHighlight() {
		return highlight;
	}
	public void setHighlight(String highlight) {
		this.highlight = highlight;
	}

	public String getName(){
		return this.dramaname;
	}
	public Date getEnddate() {
		return enddate;
	}
	public void setEnddate(Date enddate) {
		this.enddate = enddate;
	}
	public String getLogo() {
		return logo;
	}
	public String getLimg() {
		if(StringUtils.isBlank(logo)) return "img/default_head.png";
		return logo;
	}
	public String getDramacompany() {
		return dramacompany;
	}
	public void setDramacompany(String dramacompany) {
		this.dramacompany = dramacompany;
	}
	public String getUrl(){
		return "drama/"+this.id;
	}
	public String getDramadata() {
		return dramadata;
	}
	public void setDramadata(String dramadata) {
		this.dramadata = dramadata;
	}
	public String getDramatype() {
		return dramatype;
	}
	public void setDramatype(String dramatype) {
		this.dramatype = dramatype;
	}
	public String getPlayinfo() {
		return playinfo;
	}
	public void setPlayinfo(String playinfo) {
		this.playinfo = playinfo;
	}
	
	public Integer getBoughtcount() {
		return boughtcount;
	}
	public void setBoughtcount(Integer boughtcount) {
		this.boughtcount = boughtcount;
	}
	
	public void addBoughtcount(int num){
		this.boughtcount += num;
	}

	public String getActorstext() {
		return actorstext;
	}

	public void setActorstext(String actorstext) {
		this.actorstext = actorstext;
	}

	public String getDirectortext() {
		return directortext;
	}

	public void setDirectortext(String directortext) {
		this.directortext = directortext;
	}

	public String getTroupecompany() {
		return troupecompany;
	}

	public void setTroupecompany(String troupecompany) {
		this.troupecompany = troupecompany;
	}

	public String getTroupecompanytext() {
		return troupecompanytext;
	}

	public void setTroupecompanytext(String troupecompanytext) {
		this.troupecompanytext = troupecompanytext;
	}
	
	public String getCitycode(){
		return citycode;
	}
	
	public void setCitycode(String citycode){
		this.citycode = citycode;
	}
	
	public String getActorcontent() {
		return actorcontent;
	}

	public void setActorcontent(String actorcontent) {
		this.actorcontent = actorcontent;
	}
	
	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}

	public boolean hasShow(){
		if(releasedate == null || enddate == null) return false;
		Date cur = DateUtil.currentTime();
		return cur.after(releasedate) && cur.before(enddate);
	}
	
	public String getPretype() {
		return pretype;
	}

	public void setPretype(String pretype) {
		this.pretype = pretype;
	}

	public String getSaleCycle() {
		return saleCycle;
	}

	public void setSaleCycle(String saleCycle) {
		this.saleCycle = saleCycle;
	}
	
	public boolean hasPretype(String stype){
		return StringUtils.equals(this.pretype, stype);
	}

	public String getPrices() {
		return prices;
	}

	public void setPrices(String prices) {
		this.prices = prices;
	}
	
}
