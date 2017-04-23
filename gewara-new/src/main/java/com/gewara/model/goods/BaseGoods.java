package com.gewara.model.goods;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.GoodsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;

public abstract class BaseGoods extends BaseObject {
	private static final long serialVersionUID = 4914995483381697551L;
	
	protected Long id;						//ID
	protected String tag;					//分类
	protected Long relatedid; 				//场馆ID, 活动ID
	protected String itemtype;
	protected Long itemid;
	protected String goodsname;				//商品名称
	
	
	protected Integer oriprice;				//原价	
	protected Integer unitprice;			//单价
	protected Integer costprice;			//成本价
	
	protected Integer maxprice;				//最大价格
	protected Integer minprice;				//最小价格
	
	protected Integer limitnum;				//支付限购数量
	protected Integer allowaddnum;			//下单数量限制，防止因同事下单人数过多，而导致库存不足
	
	protected Integer quantity;				//原始的库存数量，不随订单的增加而减少
	protected Integer maxbuy;				//每次最多购买的数量
	protected Integer sales;				//卖出数量
	
	protected Integer minpoint;				//使用积分下限
	protected Integer maxpoint;				//使用积分上限
	
	protected Integer goodssort;    		//商品排序
	protected String deliver;				//是否需要地址//N,Y		
	protected String spflag;				//特价活动标识
	
	protected String partners;				//该物品适用于合作商，如我们的android，ihphone，wap
	
	protected Long clerkid;					//加入人			
	protected String manager;				//创建人类别 
	
	
	protected String shortname;				//商品简称[打票使用]
	protected String printcontent;			//票纸打印内容	
	protected String ordermsg;				//订单短信				
	protected String notifymsg;				//3小时提醒短息		
	
	protected String status;				//状态
	protected String otherinfo;				//其他设置 json格式， 存放支付配置信息等
	protected String citycode;				//城市代码
	protected String summary;				//描述摘要
	protected String description;			//商品描述
	protected String logo;					//图片	
	protected String biglogo;				//大图
	
	protected Timestamp releasetime;		//展示时间
	protected Timestamp fromtime;   		//开卖时间
	protected Timestamp totime;     		//结束时间
	protected Timestamp addtime;			//加入时间
	protected Timestamp fromvalidtime;		//通票入场时间
	protected Timestamp tovalidtime;		//通票入场结束时间
	
	protected String elecard;		
	protected String expressid;				//配送方式id
	protected String period;				//是否有时段
	protected Integer msgMinute;		//短信提前发送时间(分钟)
	
	protected String barcode;				//条形码
	protected String feetype;				//业务模式
	protected String servicetype;			//服务板块
	protected String seotitle; 				//SEO关键字
	protected String seodescription;		//SEO描述
	protected Integer clickedtimes;
	
	public String getExpressid() {
		return expressid;
	}

	public void setExpressid(String expressid) {
		this.expressid = expressid;
	}

	@Override
	public final Serializable realId() {
		return id;
	}
	
	public Timestamp getFromvalidtime() {
		return fromvalidtime;
	}
	public void setFromvalidtime(Timestamp fromvalidtime) {
		this.fromvalidtime = fromvalidtime;
	}
	public Timestamp getTovalidtime() {
		return tovalidtime;
	}
	public void setTovalidtime(Timestamp tovalidtime) {
		this.tovalidtime = tovalidtime;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getPrintcontent() {
		return printcontent;
	}
	public void setPrintcontent(String printcontent) {
		this.printcontent = printcontent;
	}
	public Integer getOriprice() {
		return oriprice;
	}
	public void setOriprice(Integer oriprice) {
		this.oriprice = oriprice;
	}
	
	public String getBiglogo() {
		return biglogo;
	}
	public void setBiglogo(String biglogo) {
		this.biglogo = biglogo;
	}
	
	public abstract String getGoodstype();
	public abstract boolean hasBooking();

	public String getOrdermsg() {
		return ordermsg;
	}
	public Timestamp getReleasetime() {
		return releasetime;
	}
	public void setReleasetime(Timestamp releasetime) {
		this.releasetime = releasetime;
	}
	public void setOrdermsg(String ordermsg) {
		this.ordermsg = ordermsg;
	}
	public String getNotifymsg() {
		return notifymsg;
	}
	public void setNotifymsg(String notifymsg) {
		this.notifymsg = notifymsg;
	}
	public String getGoodsname() {
		return goodsname;
	}
	public void setGoodsname(String goodsname) {
		this.goodsname = goodsname;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
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
	public String getItemtype() {
		return itemtype;
	}

	public void setItemtype(String itemtype) {
		this.itemtype = itemtype;
	}

	public Long getItemid() {
		return itemid;
	}

	public void setItemid(Long itemid) {
		this.itemid = itemid;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Integer getUnitprice() {
		return unitprice;
	}
	public void setUnitprice(Integer unitprice) {
		this.unitprice = unitprice;
	}
	public Long getClerkid() {
		return clerkid;
	}
	public void setClerkid(Long clerkid) {
		this.clerkid = clerkid;
	}
	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
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
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public Integer getMinpoint() {
		return minpoint;
	}
	public void setMinpoint(Integer minpoint) {
		this.minpoint = minpoint;
	}
	public Integer getMaxpoint() {
		return maxpoint;
	}
	public void setMaxpoint(Integer maxpoint) {
		this.maxpoint = maxpoint;
	}
	
	public Timestamp getFromtime() {
		return fromtime;
	}
	public void setFromtime(Timestamp fromtime) {
		this.fromtime = fromtime;
	}
	public Timestamp getTotime() {
		return totime;
	}
	public void setTotime(Timestamp totime) {
		this.totime = totime;
	}
	public Integer getGoodssort() {
		return goodssort;
	}
	public void setGoodssort(Integer goodssort) {
		this.goodssort = goodssort;
	}
	public Integer getLimitnum() {
		return limitnum;
	}
	public void setLimitnum(Integer limitnum) {
		this.limitnum = limitnum;
	}
		
	public boolean isStart(){
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		return cur.before(fromtime);
	}
	
	public boolean isEnd(){
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		return cur.after(totime);
	}
	public boolean isNeedDeliver() {
		return GoodsConstant.DELIVER_ENTITY.equals(deliver);
	}
	public Integer getRealpoint() {
		return this.unitprice * ConfigConstant.POINT_RATIO;
	}
	public String getReallogo(){
		if(StringUtils.isBlank(logo)) return "img/default_head.png";
		return logo;
	}
	public String getDeliver() {
		return deliver;
	}
	public void setDeliver(String deliver) {
		this.deliver = deliver;
	}
	public String getShortname() {
		return shortname;
	}
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
	public Integer getCostprice() {
		return costprice;
	}
	public void setCostprice(Integer costprice) {
		this.costprice = costprice;
	}
	public boolean isPointType(){
		return GoodsConstant.GOODS_TAG_POINT.equals(tag);
	}
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	public String getSpflag() {
		return spflag;
	}
	public void setSpflag(String spflag) {
		this.spflag = spflag;
	}
	public String getTipMsg(){
		String result = "";
		if(StringUtils.isNotBlank(ordermsg)) {
			result = ordermsg.replaceAll("quantity", "X").replaceAll("password", "******");
		}
		return result;
	}
	public List getSummaryList(){
		if(StringUtils.isNotBlank(this.summary)) {
			return Arrays.asList(StringUtils.split(this.summary, ","));
		}
		return null;
	}
	public boolean isOpenPointPay(){
		return maxpoint !=null && maxpoint > 0;
	}
	
	public boolean isOpenCardPay(){
		return StringUtils.containsAny(elecard, "ABD");
	}
	public String getElecard() {
		return elecard;
	}

	public void setElecard(String elecard) {
		this.elecard = elecard;
	}
	public Integer getAllowaddnum() {
		return allowaddnum;
	}
	public void setAllowaddnum(Integer allowaddnum) {
		this.allowaddnum = allowaddnum;
	}
	public Integer getMaxbuy() {
		return maxbuy;
	}
	public void setMaxbuy(Integer maxbuy) {
		this.maxbuy = maxbuy;
	}

	public String getPartners() {
		return partners;
	}

	public void setPartners(String partners) {
		this.partners = partners;
	}

	public Integer getSales() {
		return sales;
	}

	public void setSales(Integer sales) {
		this.sales = sales;
	}
	
	public boolean hasStatus(String stats){
		return StringUtils.equals(this.status, stats);
	}
	
	public boolean hasExpired(){
		return tovalidtime.before(DateUtil.getCurFullTimestamp());
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}
	
	public boolean hasPeriod(){
		if(StringUtils.isBlank(this.period)) return false;
		return StringUtils.equals(this.period, GoodsConstant.PERIOD_Y);
	}
	public Integer getMsgMinute() {
		return msgMinute;
	}

	public void setMsgMinute(Integer msgMinute) {
		this.msgMinute = msgMinute;
	}
	
	public boolean isOpenBarcode(){
		return StringUtils.equals(barcode, Status.Y);
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public String gainBriefname(){
		return StringUtils.isNotBlank(this.shortname)?this.shortname:this.goodsname;
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

	public Integer getMaxprice() {
		return maxprice;
	}

	public void setMaxprice(Integer maxprice) {
		this.maxprice = maxprice;
	}

	public Integer getMinprice() {
		return minprice;
	}

	public void setMinprice(Integer minprice) {
		this.minprice = minprice;
	}
	public String getFeetype() {
		return feetype;
	}

	public void setFeetype(String feetype) {
		this.feetype = feetype;
	}
	public String getName(){
		return this.goodsname;
	}
	public Integer getClickedtimes() {
		return clickedtimes;
	}
	public void setClickedtimes(Integer clickedtimes) {
		this.clickedtimes = clickedtimes;
	}

	public String getServicetype() {
		return servicetype;
	}

	public void setServicetype(String servicetype) {
		this.servicetype = servicetype;
	}
}
