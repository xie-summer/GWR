package com.gewara.model.content;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

/**
 * @author Administrator
 *
 */
public class Bulletin extends BaseObject{
	private static final long serialVersionUID = 4914995483381697551L;
	public static final int RECOMMEND_HOTVALUE = 1000;
	public static final String BULLETION_COMMON="common";
	public static final String BULLETION_DISCOUNT="discount";
	public static final String BULLETION_COUPON="coupon";
	public static final String BULLETION_SYSMSG="sysmsg";
	private Long id;
	private Long relatedid;
	private String bulletintitle;
	private String content;
	private Date posttime;
	private Date validtime;
	private String tag;//类型标签
	private Integer hotvalue; //热门程度
	private String bulletintype;//公告类型：1公告、2优惠信息、3优惠券
	private String logo;	//优惠券图片
	private Integer printnum; //打印次数
	private Integer downnum; //下载次数
	private String citycode;
	private String countycode;
	
	public Bulletin(){}
	
	public Bulletin(String content){
		this.posttime = new Date();
		this.hotvalue = 0;
		this.printnum = 0;
		this.downnum = 0;
		this.content = content;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public Date getPosttime() {
		return posttime;
	}

	public void setPosttime(Date posttime) {
		this.posttime = posttime;
	}

	public Date getValidtime() {
		return validtime;
	}

	public String getVaildtimeStr(){
		if(validtime==null) return "";
		return DateUtil.formatDate(validtime);
	}
	public void setValidtime(Date validtime) {
		this.validtime = validtime;
	}
	@Override
	public Serializable realId() {
		return id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	public String getBulletintitle() {
		return bulletintitle;
	}
	public void setBulletintitle(String bulletintitle) {
		this.bulletintitle = bulletintitle;
	}
	public Integer getHotvalue() {
		return hotvalue;
	}
	public void setHotvalue(Integer hotvalue) {
		this.hotvalue = hotvalue;
	}
	public String getBulletintype() {
		return bulletintype;
	}
	public void setBulletintype(String bulletintype) {
		this.bulletintype = bulletintype;
	}
	public String getLogo() {
		return logo;
	}
	public String getLimg() {
		if(StringUtils.isBlank(logo)) return "img/default_head.png";
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public Integer getPrintnum() {
		return printnum;
	}
	public void setPrintnum(Integer printnum) {
		this.printnum = printnum;
	}
	public Integer getDownnum() {
		return downnum;
	}
	public void setDownnum(Integer downnum) {
		this.downnum = downnum;
	}
	public void addPrintnum(){
		this.printnum +=1;
	}
	public void addDownnum(){
		this.downnum+=1;
	}
	public String getCname() {
		return this.bulletintitle;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public String getCountycode() {
		return countycode;
	}

	public void setCountycode(String countycode) {
		this.countycode = countycode;
	}
}
