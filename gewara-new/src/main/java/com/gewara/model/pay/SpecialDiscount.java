package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.model.BaseObject;
import com.gewara.util.VmBaseUtil;

/**
 * 特殊优惠方案
 * @author acerge(acerge@163.com)
 * @since 2:29:44 PM Dec 8, 2010
 */
public class SpecialDiscount extends BaseObject{
	private static final long serialVersionUID = 1238746L;
	public static final String DISCOUNT_TYPE_PERORDER = "order";			//每笔订单折扣
	public static final String DISCOUNT_TYPE_PERTICKET = "uprice";			//每个物品折扣
	public static final String DISCOUNT_TYPE_PERCENT = "percent";			//订单总额百分比
	public static final String DISCOUNT_TYPE_BUYONE_GIVEONE = "one2one";	//买1送1
	public static final String DISCOUNT_TYPE_FIXPRICE = "fprice";			//fix price统一单价（单价固定）
	public static final String DISCOUNT_TYPE_EXPRESSION = "exp";				//表达式

	public static final String DISCOUNT_PERIOD_A = "A";				//自动的周期
	public static final String DISCOUNT_PERIOD_D = "D";				//自然天
	public static final String DISCOUNT_PERIOD_W = "W";				//自然的周期(一周)
	public static final String DISCOUNT_PERIOD_DW = "DW";			//自然的周期(两周)
	public static final String DISCOUNT_PERIOD_M = "M";				//自然的周期(一个月)
	
	public static final String REBATES_CASH = "Y";		//现金
	public static final String REBATES_CARDA = "A";		//A卡
	public static final String REBATES_CARDC = "C";		//C卡
	public static final String REBATES_CARDD = "D";		//D卡
	public static final String REBATES_POINT = "P";		//积分
	
	public static final String OPENTYPE_GEWA = "G"; 	//自动Gewa场次开放 
	public static final String OPENTYPE_PARTNER = "P";	//商家渠道场次开放
	public static final String OPENTYPE_WAP = "W";		//WAP开放
	public static final String OPENTYPE_SPECIAL = "S";	//特别设置的才开放
	public static final String ENCODE_KEY = "KE3a&h@";
	
	//电子支付方式
	public static final String VERIFYTYPE_FIXED = "fixed";		//固定码
	public static final String VERIFYTYPE_ONLYONE = "onlyone";	//只用一次
	
	private Long id;
	private String flag;				//特殊标识
	private String bankname;			//银行名称
	private String description;			//简要说明
	private Timestamp timefrom;			//优惠开始时间
	private Timestamp timeto;			//优惠结束时间
	private Integer limitnum;			//个人限够次数
	private Integer limitperiod;		//个人限够周期（分钟）
	private String periodtype;			//限够周期类型
	private Long spcounterid;			//使用数量控制器的ID
	private Integer sellcount;			//卖出总数
	private String opentype;			//开放类型   

	private String tag;					//订单类型
	private String addtime1;			//下单时段限定1：0000
	private String addtime2;			//下单时段限定2：2400
	private String addweek;				//下单周几
	private String time1;				//放映时段限定1：0000
	private String time2;				//放映时段限定2：2400
	private String weektype;			//场次周几
	private Integer pricegap;			//gewa卖价与成本价 差价范围
	private Integer price1;				//卖价范围1
	private Integer price2;				//卖价范围2
	private Integer costprice1;			//成本价范围1
	private Integer costprice2;			//成本价范围2
	private String specialrule;			//特殊规则表达式
	
	private String citycode;			//城市列表
	private String relatedid;			//大分类列表 
	private String categoryid;			//小分类列表
	private String itemid;				//详细分类列表
	private String fieldid;				//场地分类列表
	private String edition;				//版本数据
	
	private Integer rebates;			//每笔订单返利
	private Integer rebatesmax;			//前多少名返利
	private String rebatestype;			//返利类型：充值、送卡
	private Long bindgoods;				//赠送套餐
	private Integer bindnum;			//套餐购票数量
	private String logo;				//银行Logo
	
	private String paymethod;			//限制支付方式
	private Integer discount;			//优惠金额
	private Integer extdiscount;		//外部优惠金额
	private String expression;			//计算表达式
	private String distype;				//优惠方式
	private String remark;				//不可用时说明
	private String enableRemark;		//可用时的说明
	private String recommendRemark;		//推荐时的说明
	private Timestamp createtime;		//创建时间
	private Timestamp updatetime;		//数量更新时间
	private String banner;				//顶层banner图片
	private String adcontent;			//广告内容
	private String otherinfo;			//其他信息
	private Integer minbuy;				//每单购买最少数量
	private Integer buynum;				//每单购买数量限制
	private String validateUrl;			//验证跳转URL
	private String validBackUrl;		//支付通知URL验证
	private Integer sortnum;			//排序数字
	private String uniqueby;			//使用什么限制唯一性
	private String ptnids;				//支付渠道id集合
	private String channel;				//类型：活动优惠/渠道合作优惠
	private String bindmobile;			//绑定手机
	private Long drawactivity;  		//用户购完票后返回券采用抽奖方式返到用户
	private Integer drawperiod;			//购票成功领取返券周期
	private Integer bindDrawCardNum;	//周期内领取次数控制
	private Integer ipLimitedOrderCount;//单ip限制下单数量
	private String cardUkey;			//卡验证唯一标识
	private String cardNumUnique;  		//是否银行卡次数验证
	private Integer cardNumPeriodIntvel; //银行卡每次使用限购周期
	private Integer cardNumPeriodSpan;	//银行卡最大周期
	private Integer cardNumLimitnum; 	//银行卡限够次数
	private String cardbinUkey;  	 	//卡号验证标识
	private String loginfrom;			//登录来源
	private String extraInfo;			//扩展信息

	//更改成本价，与券的逻辑一样
	private String costtype;		//成本价类型：影片最底价加x元，结算价加x元
	private Integer costnum;		//成本价增量
	
	private String verifyType;		//动态码验证类型：无，可重复使用（不记名使用），不可重复使用
	
	public SpecialDiscount(){
		
	}

	public Integer getBindDrawCardNum() {
		return bindDrawCardNum;
	}
	public void setBindDrawCardNum(Integer bindDrawCardNum) {
		this.bindDrawCardNum = bindDrawCardNum;
	}
	
	public Long getDrawactivity() {
		return drawactivity;
	}
	public void setDrawactivity(Long drawactivity) {
		this.drawactivity = drawactivity;
	}
	public SpecialDiscount(String flag, String tag){
		this.tag = tag;
		this.flag = flag;
		this.createtime = new Timestamp(System.currentTimeMillis());
		this.updatetime = createtime;
		this.sellcount = 0;
		this.extraInfo = Status.N;
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
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
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
	public String getPaymethod() {
		return paymethod;
	}
	public void setPaymethod(String paymethod) {
		this.paymethod = paymethod;
	}
	public boolean isValidPaymethod(String spaymethod, String paybank){
		if(StringUtils.isBlank(this.paymethod)) return true;
		String[] pmList = StringUtils.split(this.paymethod, ",");
		for(String pm: pmList){
			if(StringUtils.equals(pm, spaymethod)) return true;
			String[] pair = StringUtils.split(pm, ":");
			if(StringUtils.equals(pair[0], spaymethod)){
				if(pair.length==1 || pair.length>1 && StringUtils.equals(pair[1], paybank)) return true;
			}
		}
		return false;
	}
	public Integer getDiscount() {
		return discount;
	}
	public void setDiscount(Integer discount) {
		this.discount = discount;
	}
	
	public Integer getExtdiscount() {
		return extdiscount;
	}

	public void setExtdiscount(Integer extdiscount) {
		this.extdiscount = extdiscount;
	}

	public String getDistype() {
		return distype;
	}
	public void setDistype(String distype) {
		this.distype = distype;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Integer getLimitnum() {
		return limitnum;
	}
	public void setLimitnum(Integer limitnum) {
		this.limitnum = limitnum;
	}
	public Integer getLimitperiod() {
		return limitperiod;
	}
	public void setLimitperiod(Integer limitperiod) {
		this.limitperiod = limitperiod;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	public Integer getSellcount() {
		return sellcount;
	}
	public void setSellcount(Integer sellcount) {
		this.sellcount = sellcount;
	}
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	public String getTime1() {
		return time1;
	}
	public void setTime1(String time1) {
		this.time1 = time1;
	}
	public String getTime2() {
		return time2;
	}
	public void setTime2(String time2) {
		this.time2 = time2;
	}
	public Integer getPricegap() {
		return pricegap;
	}
	public void setPricegap(Integer pricegap) {
		this.pricegap = pricegap;
	}
	public Integer getPrice1() {
		return price1;
	}
	public void setPrice1(Integer price1) {
		this.price1 = price1;
	}
	public Integer getPrice2() {
		return price2;
	}
	public void setPrice2(Integer price2) {
		this.price2 = price2;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public String getRelatedid() {
		return relatedid;
	}
	public void setRelatedid(String relatedid) {
		this.relatedid = relatedid;
	}
	public String getCategoryid() {
		return categoryid;
	}
	public void setCategoryid(String categoryid) {
		this.categoryid = categoryid;
	}
	public String getItemid() {
		return itemid;
	}
	public void setItemid(String itemid) {
		this.itemid = itemid;
	}
	public String getFieldid() {
		return fieldid;
	}
	public void setFieldid(String fieldid) {
		this.fieldid = fieldid;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public String getOpentype() {
		return opentype;
	}
	public void setOpentype(String opentype) {
		this.opentype = opentype;
	}
	public String getWeektype() {
		return weektype;
	}
	public void setWeektype(String weektype) {
		this.weektype = weektype;
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
	public String getBanner() {
		return banner;
	}
	public void setBanner(String banner) {
		this.banner = banner;
	}
	public String getAdcontent() {
		return adcontent;
	}
	public void setAdcontent(String adcontent) {
		this.adcontent = adcontent;
	}
	public Integer getBuynum() {
		return buynum;
	}
	public void setBuynum(Integer buynum) {
		this.buynum = buynum;
	}
	public Integer getRebates() {
		return rebates;
	}
	public void setRebates(Integer rebates) {
		this.rebates = rebates;
	}
	public Integer getRebatesmax() {
		return rebatesmax;
	}
	public void setRebatesmax(Integer rebatesmax) {
		this.rebatesmax = rebatesmax;
	}
	public String getRebatestype() {
		return rebatestype;
	}
	public void setRebatestype(String rebatestype) {
		this.rebatestype = rebatestype;
	}
	public String getEnableRemark() {
		return enableRemark;
	}
	public void setEnableRemark(String enableRemark) {
		this.enableRemark = enableRemark;
	}
	public String getRecommendRemark() {
		return recommendRemark;
	}
	public void setRecommendRemark(String recommendRemark) {
		this.recommendRemark = recommendRemark;
	}
	public String getFullEnableRemark(Integer amount, Integer sdiscount){
		int tmpDiscount = sdiscount == null ? 0: sdiscount.intValue();
		return StringUtils.replace(StringUtils.replace(enableRemark, "amount", amount - tmpDiscount+".00"), "discount", tmpDiscount + ".00");
	}
	public String getFullRecommendRemark(Integer amount, Integer sdiscount){
		int tmpDiscount = sdiscount == null ? 0: sdiscount.intValue();
		return StringUtils.replace(StringUtils.replace(recommendRemark, "amount", amount - tmpDiscount+".00"), "discount", tmpDiscount + ".00");
	}
	public Long getBindgoods() {
		return bindgoods;
	}
	public void setBindgoods(Long bindgoods) {
		this.bindgoods = bindgoods;
	}
	public Integer getBindnum() {
		return bindnum;
	}
	public String getBankname() {
		return bankname;
	}

	public void setBankname(String bankname) {
		this.bankname = bankname;
	}

	public void setBindnum(Integer bindnum) {
		this.bindnum = bindnum;
	}
	public Integer getMinbuy() {
		return minbuy;
	}
	public void setMinbuy(Integer minbuy) {
		this.minbuy = minbuy;
	}
	public String getValidateUrl() {
		return validateUrl;
	}
	public void setValidateUrl(String validateUrl) {
		this.validateUrl = validateUrl;
	}
	public Integer getSortnum() {
		return sortnum;
	}
	public void setSortnum(Integer sortnum) {
		this.sortnum = sortnum;
	}
	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getUniqueby() {
		return uniqueby;
	}
	public void setUniqueby(String uniqueby) {
		this.uniqueby = uniqueby;
	}
	public String getValidBackUrl() {
		return validBackUrl;
	}
	public void setValidBackUrl(String validBackUrl) {
		this.validBackUrl = validBackUrl;
	}
	public String getLimitperiodStr() {
		int hour = limitperiod/60;
		int min = limitperiod%60;
		int day = 0;
		if(hour > 24){
			day = hour/24;
			hour = hour % 24;
		}
		String result = (day > 0?day+"天":"") + (hour>0? hour+"小时":"") + (min>0?min+"分":"");
		return result;
	}
	
	public String getChannel() {
		return channel;
	}
	
	public String getLimg(){
		return this.logo;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public Timestamp getCreatetime() {
		return createtime;
	}
	public void setCreatetime(Timestamp createtime) {
		this.createtime = createtime;
	}
	public String getBindmobile() {
		return bindmobile;
	}
	public void setBindmobile(String bindmobile) {
		this.bindmobile = bindmobile;
	}
	public Long getSpcounterid() {
		return spcounterid;
	}
	public void setSpcounterid(Long spcounterid) {
		this.spcounterid = spcounterid;
	}
	public Integer getCostprice1() {
		return costprice1;
	}
	public void setCostprice1(Integer costprice1) {
		this.costprice1 = costprice1;
	}
	public Integer getCostprice2() {
		return costprice2;
	}
	public void setCostprice2(Integer costprice2) {
		this.costprice2 = costprice2;
	}

	public Integer getDrawperiod() {
		return drawperiod;
	}

	public void setDrawperiod(Integer drawperiod) {
		this.drawperiod = drawperiod;
	}
	
	public boolean hasDistype(String type){
		return StringUtils.equals(this.distype, type);
	}
	
	public boolean hasRebatestype(String retype){
		return StringUtils.equals(this.rebatestype, retype);
	}
	
	public String gainDiscountType(){
		String tmp = "";
		if(hasDistype(DISCOUNT_TYPE_PERORDER)){
			tmp = "立减";
		}else if(hasDistype(DISCOUNT_TYPE_PERTICKET)){
			tmp = "立减";
		}else if(hasDistype(DISCOUNT_TYPE_PERCENT)){
			tmp = "折扣";
		}else if(hasDistype(DISCOUNT_TYPE_BUYONE_GIVEONE)){
			tmp = tmp + "买一送一";
		}else if(hasDistype(DISCOUNT_TYPE_FIXPRICE)){
			tmp = "固定价格";
		}else if(hasDistype(DISCOUNT_TYPE_EXPRESSION)){
			tmp = "活动";
		}
		if(StringUtils.isBlank(tmp)){
			if(this.rebates>0){
				tmp = "立返";
			}else{
				tmp = "优惠";
			}
		}
		return tmp;
	}
	
	public String getDiscountText(String bankText, String discountText, String rebatesText){
		String tmp = "bankname";
		if(hasDistype(DISCOUNT_TYPE_PERORDER)){
			if(this.discount != null && this.discount > 0){
				tmp = tmp + "每笔订单立减discount元";
			}
		}else if(hasDistype(DISCOUNT_TYPE_PERTICKET)){
			if(this.discount != null && this.discount > 0){
				tmp = tmp + "每张票立减discount元";
			}
		}else if(hasDistype(DISCOUNT_TYPE_PERCENT)){
			tmp = tmp + "每笔订单discount折优惠";
		}else if(hasDistype(DISCOUNT_TYPE_BUYONE_GIVEONE)){
			tmp = tmp + "可享受买一送一";
		}else if(hasDistype(DISCOUNT_TYPE_FIXPRICE)){
			tmp = tmp + "每张票仅需discount元";
		}else if(hasDistype(DISCOUNT_TYPE_EXPRESSION)){
			//TODO:如何显示
			tmp = tmp + this.description;
		}
		tmp = StringUtils.replace(tmp, "bankname", bankText);
		tmp = StringUtils.replace(tmp, "discount", discountText);
		if(this.rebates>0){
			if((hasDistype(DISCOUNT_TYPE_PERTICKET) || hasDistype(DISCOUNT_TYPE_PERORDER)) && !(this.discount != null && this.discount > 0)){
				tmp += "立返rebates";
			}else{
				tmp += ", 立返rebates";
			}
			if(hasRebatestype("Y")) tmp += "元";
			if(hasRebatestype("P")) tmp += "积分";
			if(hasRebatestype("A") || hasRebatestype("D")) tmp += "元券";
			tmp = StringUtils.replace(tmp, "rebates", rebatesText);
		}
		return tmp;
	}
	
	public String gainDiscount(String noRebates){
		int tmp = this.discount;
		if(hasExtdicount()){
			tmp = this.extdiscount;
		}
		String tmpDiscount = String.valueOf(tmp);
		if(Boolean.parseBoolean(noRebates) && hasRebatestype("Y")){
			tmpDiscount = String.valueOf(tmp + this.rebates);
		}
		if(hasDistype(DISCOUNT_TYPE_PERCENT)){
			String tmpPattern = "0";
			if(tmp !=0 && 100%tmp>0) tmpPattern = "0.0";
			tmpDiscount = VmBaseUtil.formatPercent(100-tmp, 10, tmpPattern);
		}else if(hasDistype(DISCOUNT_TYPE_BUYONE_GIVEONE)){
			tmpDiscount = "X";
		}else if(hasDistype(DISCOUNT_TYPE_EXPRESSION)){
			tmpDiscount = "X元";
		}
		return tmpDiscount;
	}
	
	public boolean hasExtdicount(){
		return this.extdiscount != null && this.discount == 0 && this.extdiscount > 0;
	}

	public Integer getIpLimitedOrderCount() {
		return ipLimitedOrderCount;
	}

	public void setIpLimitedOrderCount(Integer ipLimitedOrderCount) {
		this.ipLimitedOrderCount = ipLimitedOrderCount;
	}

	public String getCardUkey() {
		return cardUkey;
	}

	public void setCardUkey(String cardUkey) {
		this.cardUkey = cardUkey;
	}

	public String getCardNumUnique() {
		return cardNumUnique;
	}

	public void setCardNumUnique(String cardNumUnique) {
		this.cardNumUnique = cardNumUnique;
	}

	public Integer getCardNumLimitnum() {
		return cardNumLimitnum;
	}

	public void setCardNumLimitnum(Integer cardNumLimitnum) {
		this.cardNumLimitnum = cardNumLimitnum;
	}

	public String getCardbinUkey() {
		return cardbinUkey;
	}

	public void setCardbinUkey(String cardbinUkey) {
		this.cardbinUkey = cardbinUkey;
	}

	public Integer getCardNumPeriodIntvel() {
		return cardNumPeriodIntvel;
	}

	public void setCardNumPeriodIntvel(Integer cardNumPeriodIntvel) {
		this.cardNumPeriodIntvel = cardNumPeriodIntvel;
	}

	public Integer getCardNumPeriodSpan() {
		return cardNumPeriodSpan;
	}

	public void setCardNumPeriodSpan(Integer cardNumPeriodSpan) {
		this.cardNumPeriodSpan = cardNumPeriodSpan;
	}
	
	public String getLoginfrom() {
		return loginfrom;
	}

	public void setLoginfrom(String loginfrom) {
		this.loginfrom = loginfrom;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getSpecialrule() {
		return specialrule;
	}

	public void setSpecialrule(String specialrule) {
		this.specialrule = specialrule;
	}

	public String getPeriodtype() {
		return periodtype;
	}

	public void setPeriodtype(String periodtype) {
		this.periodtype = periodtype;
	}

	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}

	public String getPtnids() {
		return ptnids;
	}

	public void setPtnids(String ptnids) {
		this.ptnids = ptnids;
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

	public String getVerifyType() {
		return verifyType;
	}

	public void setVerifyType(String verifyType) {
		this.verifyType = verifyType;
	}
}
