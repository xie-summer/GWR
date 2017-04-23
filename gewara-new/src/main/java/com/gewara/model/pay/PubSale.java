package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;

public class PubSale extends BaseObject {
	private static final long serialVersionUID = 674312580464307634L;
	public static final String SALETYPE_GOODS = "goods";
	public static final String SALETYPE_CARD = "card";
	private Long id;
	private String name; 			// 名称
	private Integer lowerprice; 	// 低价 单位分 不是元
	private Integer curprice; 		// 当前竞拍价 单位分 不是元
	private String dupprice; 		// 每次提升金额 单位分 不是元
	private Integer needpoint; 	// 每次竞拍需要积分
	private Integer countdown; 	// 倒计时
	private Integer ordernum; 		// 排序
	private Timestamp begintime; 	// 开始时间
	private Timestamp endtime; 	// 结束时间
	private String status; 			// 状态 N, Y, N_DELETE 
	private String logo;
	private String source;			//来源
	private String remark; 			// 描述
	private String description;	// 详细
	private Integer version;
	private String nickname;
	private Long memberid;
	private Timestamp lasttime;
	private String saletype;
	private String cardpass;
	private String citycode;
	private Long goodsid;
	private Integer pubperiod;			//可拍周期
	private Integer pubnumber;			//可拍周期次数
	private Integer unitMinute;			//成功后分钟数
	@Override
	public Serializable realId() {
		return id;
	}
	public String getCardpass() {
		return cardpass;
	}

	public void setCardpass(String cardpass) {
		this.cardpass = cardpass;
	}

	public String getSaletype() {
		return saletype;
	}

	public void setSaletype(String saletype) {
		this.saletype = saletype;
	}

	public Timestamp getLasttime() {
		return lasttime;
	}

	public void setLasttime(Timestamp lasttime) {
		this.lasttime = lasttime;
	}
	
	public PubSale() {}
	
	public PubSale(String name) {
		this.ordernum = 0;
		this.status = Status.N;
		this.name = name;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getNeedpoint() {
		return needpoint;
	}

	public void setNeedpoint(Integer needpoint) {
		this.needpoint = needpoint;
	}

	public Timestamp getBegintime() {
		return begintime;
	}

	public void setBegintime(Timestamp begintime) {
		this.begintime = begintime;
	}

	public Timestamp getEndtime() {
		return endtime;
	}

	public void setEndtime(Timestamp endtime) {
		this.endtime = endtime;
	}

	public Integer getCountdown() {
		return countdown;
	}

	public void setCountdown(Integer countdown) {
		this.countdown = countdown;
	}

	public Integer getOrdernum() {
		return ordernum;
	}

	public void setOrdernum(Integer ordernum) {
		this.ordernum = ordernum;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getLimg() {
		if (StringUtils.isBlank(logo))
			return "img/default_head.png";
		return logo;
	}


	public String getTitle() {
		return name;
	}

	public boolean isProgress() {
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		if(endtime != null)
			return begintime.before(curtime) && endtime.after(curtime);
		return begintime.before(curtime);
	}
	public boolean isEnd(Timestamp curtime) {
		if(endtime == null) return false;
		return endtime.before(curtime);
	}
	public boolean isEnd2() {
		if(endtime == null) return false;
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		return curtime.after(endtime);
	}
	public boolean isEnd3() {
		if(endtime == null){
			if(Status.N.equals(status))
				return true;
			else  return false;
		}
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		Timestamp endt = DateUtil.addMinute(endtime, 5);
		return endt.after(curtime);
	}
	public boolean isEnd4(Timestamp curtime) {
		if(endtime == null){
			if(Status.N.equals(status))
				return false;
			else return true;
		}
		Timestamp t = DateUtil.addSecond(curtime, -2);
		return t.after(endtime);
	}
	public boolean isSoon() {
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		return begintime.after(curtime);
	}
	public boolean isJoin() {
		return isProgress() && Status.N.equals(status);
	}
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	public void addCurprice(Integer price){
		if(price != null){
			this.curprice +=  price;
		}
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	public boolean saleSuccess() {
		return memberid!=null && Status.Y.equals(status);
	}
	public boolean isGoods(){
		return SALETYPE_GOODS.equals(saletype);
	}
	public boolean isCard(){
		return SALETYPE_CARD.equals(saletype);
	}

	public Integer getLowerprice() {
		return lowerprice;
	}

	public void setLowerprice(Integer lowerprice) {
		this.lowerprice = lowerprice;
	}

	public Integer getCurprice() {
		return curprice;
	}

	public void setCurprice(Integer curprice) {
		this.curprice = curprice;
	}

	public String getDupprice() {
		return dupprice;
	}

	public void setDupprice(String dupprice) {
		this.dupprice = dupprice;
	}
	public Double gainRprice(Integer p){
		if(p == null) return 0.0;
		Double d = p/100.00;
		return d;
	}
	
	public List<Double> gainDupprice(){
		List<Double> dList = new ArrayList<Double>();
		if(StringUtils.isBlank(this.dupprice)) return dList;
		List<Integer> tmpList = BeanUtil.getIntgerList(this.dupprice, ",");
		try{
			for (Integer tmp : tmpList) {
				dList.add(gainRprice(tmp));
			}
		}catch(Exception e){
			
		}
		return dList;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	public boolean isClose(){
		return Status.N_DELETE.equals(status);
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public Long getGoodsid() {
		return goodsid;
	}
	public void setGoodsid(Long goodsid) {
		this.goodsid = goodsid;
	}
	public Integer getPubnumber() {
		return pubnumber;
	}
	public void setPubnumber(Integer pubnumber) {
		this.pubnumber = pubnumber;
	}
	public Integer getUnitMinute() {
		return unitMinute;
	}
	public void setUnitMinute(Integer unitMinute) {
		this.unitMinute = unitMinute;
	}
	public Integer getPubperiod() {
		return pubperiod;
	}
	public void setPubperiod(Integer pubperiod) {
		this.pubperiod = pubperiod;
	}
	
}
