package com.gewara.model.common;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */

public abstract class BaseEntity extends BaseObject{
	public static final int HOTVALUE_HOT = 30000; //热门
	public static final int HOTVALUE_RECOMMEND = 50000; //推荐
	public static final int HOTVALUE_SEARCH = 70000;//搜索下面的推荐
	//DWR @RemoteProperty放到get方法上，否则，子类找不到该属性
	private static final long serialVersionUID = 6889418516482337249L;
	protected Long id;
	protected String name;
	protected String briefname;//名称简称
	protected String seotitle; //SEO关键字
	protected String seodescription; //SEO描述
	protected String englishname;
	protected String pinyin;
	protected String content;
	protected Timestamp addtime;
	protected Timestamp updatetime;
	protected Integer hotvalue=0; 
	protected String logo;
	protected String firstpic;
	protected Integer clickedtimes;
	protected Integer quguo;
	protected Integer xiangqu;	// 话剧明星版块: 想成为粉丝
	protected Integer generalmark;
	protected Integer generalmarkedtimes;
	protected Integer avggeneral;
	protected Integer collectedtimes; //被收藏次数
	@Override
	public final Serializable realId() {
		return id;
	}

	public String getLogo() {
		return logo;
	}
	public String getLimg(){
		if(StringUtils.isBlank(logo)) return "img/default_logo.png";
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public Integer getHotvalue() {
		return hotvalue;
	}
	public void setHotvalue(Integer hotvalue) {
		this.hotvalue = hotvalue;
	}
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public String getEnglishname() {
		return this.englishname;
	}

	public void setEnglishname(String englishname) {
		this.englishname = englishname;
	}

	public String getPinyin() {
		return this.pinyin;
	}
	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
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

	public Integer getQuguo() {
		return quguo;
	}

	public Integer getXiangqu() {
		return xiangqu;
	}

	public void setQuguo(Integer quguo) {
		this.quguo = quguo;
	}

	public void setXiangqu(Integer xiangqu) {
		this.xiangqu = xiangqu;
	}
	public void addQuguo(){
		this.quguo +=1;
	}
	public void addXiangqu() {
		this.xiangqu += 1;
	}
	public Integer getClickedtimes() {
		return clickedtimes;
	}

	public void setClickedtimes(Integer clickedtimes) {
		this.clickedtimes = clickedtimes;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
		
	}
	public Integer getAvggeneral() {
		return avggeneral;
	}

	public void setAvggeneral(Integer avggeneral) {
		this.avggeneral = avggeneral;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public Integer getCollectedtimes() {
		return collectedtimes;
	}

	public void setCollectedtimes(Integer collectedtimes) {
		this.collectedtimes = collectedtimes;
	}
	public void addCollection(){
		this.collectedtimes += 1;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	public String getBriefname() {
		return briefname;
	}
	public void setBriefname(String briefname) {
		this.briefname = briefname;
	}
	public String getRealBriefname(){
		return StringUtils.isNotBlank(this.briefname)? briefname: getName();
	}
	public String getSeotitle() {
		return seotitle;
	}
	public void setSeotitle(String seotitle) {
		this.seotitle = seotitle;
	}
	public String getSeodescription() {
		return seodescription;
	}
	public void setSeodescription(String seodescription) {
		this.seodescription = seodescription;
	}
	public String getCname() {
		return this.name;
	}
	public String getFirstpic() {
		return firstpic;
	}
	public void setFirstpic(String firstpic) {
		this.firstpic = firstpic;
	}

}
