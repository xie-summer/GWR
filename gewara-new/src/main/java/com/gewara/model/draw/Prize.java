package com.gewara.model.draw;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class Prize extends BaseObject {
	private static final long serialVersionUID = -953298279131566566L;
	public static final String PRIZE_TYPE_DRAMA = "drama";
	public static final String PRIZE_REMARK = "remark";//其他奖品
	public static final String PRIZE_TYPE_WAIBI="waibi";//瓦币
	public static final String PRIZE_TYPE_SPDISCOUNT="sd";//特价活动名额
	
	
	private Long id;
	private Long activityid;
	private String ptype;			//奖品类型
	private Integer pvalue;			//面值
	private Integer chancenum; 		//概率值
	private Integer pnumber;		//奖品数量
	private Integer psendout;		//此奖品已出多少
	private Timestamp addtime;
	private String remark;
	private String tag;				//对应不同电子券的标识
	private String plevel;			//奖品级别
	private String msgcontent;  	//短信模板
	private String otype;			//其它奖品类型(自定义)
	private String otherinfo;
	private String topPrize;		//是否是大奖（禁止黄牛中大奖）
	
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	public String getMsgcontent() {
		return msgcontent;
	}
	public void setMsgcontent(String msgcontent) {
		this.msgcontent = msgcontent;
	}
	public Integer getLeavenum(){
		return pnumber - psendout;
	}
	public void addPsendout(){
		this.psendout +=1;
	}
	
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public Prize() {}
	
	public Prize(String tag){
		this.pvalue = 0;
		this.pnumber = 0;
		this.psendout = 0;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.tag = tag;
		this.topPrize = "N";
	}
	public Prize(Long activityid, String ptype,Integer pvalue,Integer pnumber,String remark,String tag,String plevel,String msgcontent, String otype){
		this.activityid = activityid;
		this.ptype = ptype;
		this.pvalue = pvalue;
		this.pnumber = pnumber;
		this.remark = remark;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.psendout = 0;
		this.tag = tag;
		this.plevel = plevel;
		this.msgcontent = msgcontent;
		this.chancenum = 0;
		this.otype = otype;
		this.topPrize = "N";
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getActivityid() {
		return activityid;
	}

	public void setActivityid(Long activityid) {
		this.activityid = activityid;
	}

	public String getPtype() {
		return ptype;
	}

	public void setPtype(String ptype) {
		this.ptype = ptype;
	}

	public Integer getPvalue() {
		return pvalue;
	}

	public void setPvalue(Integer pvalue) {
		this.pvalue = pvalue;
	}

	public Integer getPnumber() {
		return pnumber;
	}

	public void setPnumber(Integer pnumber) {
		this.pnumber = pnumber;
	}

	public Integer getPsendout() {
		return psendout;
	}

	public void setPsendout(Integer psendout) {
		this.psendout = psendout;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getPlevel() {
		return plevel;
	}

	public void setPlevel(String plevel) {
		this.plevel = plevel;
	}
	public Integer getChancenum() {
		return chancenum;
	}

	public void setChancenum(Integer chancenum) {
		this.chancenum = chancenum;
	}
	public String getOtype() {
		return otype;
	}
	
	public void setOtype(String otype) {
		this.otype = otype;
	}
	public String getTopPrize() {
		return topPrize;
	}
	public void setTopPrize(String topPrize) {
		this.topPrize = topPrize;
	}
}
