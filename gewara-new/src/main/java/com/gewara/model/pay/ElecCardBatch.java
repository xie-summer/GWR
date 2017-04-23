package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.PKCoderUtil;
public class ElecCardBatch extends BaseObject {
	private static final long serialVersionUID = 3754546507279229426L;
	public static String COSTTYPE_MINADD = "minadd"; 		//影片最底价加x元
	public static String COSTTYPE_FIXED = "fixed"; 			//固定价格
	public static String ACTIVATION_Y = "Y"; 				//需要激活
	public static String ACTIVATION_N = "N";				//不需激活
	public static String EXCHANGETYPE_A = "A";				//兑换券类别：橙券
	public static String EXCHANGETYPE_B = "B";				//兑换券类别：蓝券
	public static String EXCHANGETYPE_D = "D";				//兑换券类别：兑换券
	public static String EXCHANGETYPE_E = "E";				//兑换券类别：IMAX券
	public static String SOLD_TYPE_P = "P";					//P 赠券  S销售
	public static String SOLD_TYPE_S = "S";					//销售
	private Long id;				//ID
	private Long pid;				//父批次ID
	private String tag;				//标识：movie drama
	private Integer amount;			//抵用金额
	private String addtime1;		//下单时段限定1：0000
	private String addtime2;		//下单时段限定2：2400
	private String addweek;			//下单周几
	private String opentime;		//场次时段：开始 0600
	private String closetime;		//场次时段：结束 2300
	private String weektype;		//场次周几
	private String validcinema;		//可用影院
	private String validmovie;		//可用影片
	private String validitem;		//可用场次
	private String validprice;		//可用场次下的价格id
	private String bindpay;			//绑定支付方式
	private String remark;			//说明
	private Timestamp timefrom;		//卡有效开始时间
	private Timestamp timeto;		//卡有效结束时间
	private String cardtype;		//卡类型
	private String notifymsg;		//短信模板
	private Integer daynum;			//有效天数（发送奖品之日起的有效天数）
	private String citycode;		//适用城市
	private String citypattern;		//包含还是排除？
	private String validpartner;	//可用商家
	private Long bindgoods;			//绑定套餐
	private Integer bindratio;		//套餐兑换比例：X张券兑换1份
	private String costtype;		//成本价类型：影片最底价加x元，结算价加x元
	private Integer costnum;		//成本价增量
	private Integer costnum3D;		//3D版本对应成本价
	private String edition;			//可用影片版本：2D、3D、All(2D+3D)、IMAX、4D、4D+IMAX
	private String limitdesc;		//A卡超出限额出错给用户提示
	private String activation;		//是否需要激活
	private String exchangetype;	//兑换券类别
	private String channelinfo;		//渠道说明
	private Integer appoint;		//最大绑定数量
	private String soldType;   //销售类型， P 赠券  S销售
	private Integer delayDays; //可申请延期天数
	private Integer delayUseDays;  // 延期通过后使用天数
	private Integer delayFee;   // 延期费用
	
	public Integer getAppoint() {
		return appoint;
	}
	public void setAppoint(Integer appoint) {
		this.appoint = appoint;
	}
	public String getCosttype() {
		return costtype;
	}
	public void setCosttype(String costtype) {
		this.costtype = costtype;
	}
	public Integer getCostnum() {
		return costnum;
	}
	public void setCostnum(Integer costnum) {
		this.costnum = costnum;
	}
	public String getValidpartner() {
		return validpartner;
	}
	public void setValidpartner(String validpartner) {
		this.validpartner = validpartner;
	}
	public String getOpentime() {
		return opentime;
	}
	public void setOpentime(String opentime) {
		this.opentime = opentime;
	}
	public String getClosetime() {
		return closetime;
	}
	public void setClosetime(String closetime) {
		this.closetime = closetime;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public void copyFrom(ElecCardBatch from){
		this.tag = from.tag;
		this.amount = from.amount;
		this.timefrom = from.timefrom;
		this.timeto = from.timeto;
		this.weektype = from.weektype;
		this.validcinema = from.validcinema;
		this.validmovie = from.validmovie;
		this.validitem = from.validitem;
		this.cardtype = from.cardtype;
		this.remark = from.remark;
		this.daynum = from.daynum;
		this.citycode = from.citycode;
		this.citypattern = from.citypattern;
		this.opentime = from.opentime;
		this.closetime = from.closetime;
		this.validpartner = from.validpartner;
		this.bindpay = from.bindpay;
		this.costtype = from.costtype;
		this.costnum = from.costnum;
		this.costnum3D = from.costnum3D;
		this.bindgoods = from.bindgoods;
		this.bindratio = from.bindratio;
		this.addtime1 = from.addtime1;
		this.addtime2 = from.addtime2;
		this.addweek = from.addweek;
		this.edition = from.edition;
		this.limitdesc = from.limitdesc;
		this.activation = from.activation;
		this.exchangetype = from.exchangetype;
		this.channelinfo = from.channelinfo;
		this.appoint = from.appoint;
		this.validprice = from.getValidprice();
		this.soldType = from.getSoldType();
		this.delayDays = from.getDelayDays();
		this.delayFee = from.getDelayFee();
		this.delayUseDays = from.getDelayUseDays();
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getWeektype() {
		return weektype;
	}
	public void setWeektype(String weektype) {
		this.weektype = weektype;
	}
	public String getValidcinema() {
		return validcinema;
	}
	public void setValidcinema(String validcinema) {
		this.validcinema = validcinema;
	}
	public boolean isValidtime(){
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		return timefrom.before(cur) && timeto.after(cur);
	}
	public Timestamp getTimefrom() {
		return timefrom;
	}
	public void setTimefrom(Timestamp timefrom) {
		this.timefrom = timefrom;
	}
	public Timestamp getTimeto() {
		return timeto;
	}
	public void setTimeto(Timestamp timeto) {
		this.timeto = timeto;
	}
	public String getCardtype() {
		return cardtype;
	}
	public void setCardtype(String cardtype) {
		this.cardtype = cardtype;
	}
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public String getValidmovie() {
		return validmovie;
	}
	public void setValidmovie(String validmovie) {
		this.validmovie = validmovie;
	}
	public String getValiditem() {
		return validitem;
	}
	public void setValiditem(String validitem) {
		this.validitem = validitem;
	}
	public String getNotifymsg() {
		return notifymsg;
	}
	public void setNotifymsg(String notifymsg) {
		this.notifymsg = notifymsg;
	}
	public Integer getDaynum() {
		return daynum;
	}
	public void setDaynum(Integer daynum) {
		this.daynum = daynum;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getPkid() {
		if(id==null) return "";
		return PKCoderUtil.encodeString(id+"");
	}
	public String getBindpay() {
		return bindpay;
	}
	public void setBindpay(String bindpay) {
		this.bindpay = bindpay;
	}
	public Long getBindgoods() {
		return bindgoods;
	}
	public void setBindgoods(Long bindgoods) {
		this.bindgoods = bindgoods;
	}
	public Integer getBindratio() {
		return bindratio;
	}
	public void setBindratio(Integer bindratio) {
		this.bindratio = bindratio;
	}
	public String getAddtime1() {
		return addtime1;
	}
	public void setAddtime1(String addtime1) {
		this.addtime1 = addtime1;
	}
	public String getAddtime2() {
		return addtime2;
	}
	public void setAddtime2(String addtime2) {
		this.addtime2 = addtime2;
	}
	public String getAddweek() {
		return addweek;
	}
	public void setAddweek(String addweek) {
		this.addweek = addweek;
	}
	public String getCitypattern() {
		return citypattern;
	}
	public void setCitypattern(String citypattern) {
		this.citypattern = citypattern;
	}
	public String getEdition() {
		return edition;
	}
	public void setEdition(String edition) {
		this.edition = edition;
	}
	public Long getPid() {
		return pid;
	}
	public void setPid(Long pid) {
		this.pid = pid;
	}
	public boolean hasParent(){
		return pid != null;
	}
	public String getLimitdesc() {
		return limitdesc;
	}
	public void setLimitdesc(String limitdesc) {
		this.limitdesc = limitdesc;
	}
	public String getActivation() {
		return activation;
	}
	public void setActivation(String activation) {
		this.activation = activation;
	}
	public String getExchangetype() {
		return exchangetype;
	}
	public void setExchangetype(String exchangetype) {
		this.exchangetype = exchangetype;
	}
	public String getChannelinfo() {
		return channelinfo;
	}
	public void setChannelinfo(String channelinfo) {
		this.channelinfo = channelinfo;
	}
	public Integer getCostnum3D() {
		return costnum3D;
	}
	public void setCostnum3D(Integer costnum3d) {
		costnum3D = costnum3d;
	}
	public String getValidprice() {
		return validprice;
	}
	public void setValidprice(String validprice) {
		this.validprice = validprice;
	}
	public String getSoldType() {
		return soldType;
	}
	public void setSoldType(String soldType) {
		this.soldType = soldType;
	}
	public Integer getDelayDays() {
		return delayDays;
	}
	public void setDelayDays(Integer delayDays) {
		this.delayDays = delayDays;
	}
	public Integer getDelayUseDays() {
		return delayUseDays;
	}
	public void setDelayUseDays(Integer delayUseDays) {
		this.delayUseDays = delayUseDays;
	}
	public Integer getDelayFee() {
		return delayFee;
	}
	public void setDelayFee(Integer delayFee) {
		this.delayFee = delayFee;
	}
}
