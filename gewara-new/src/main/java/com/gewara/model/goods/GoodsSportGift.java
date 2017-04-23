package com.gewara.model.goods;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;
import com.gewara.model.pay.SportOrder;

public class GoodsSportGift extends BaseObject{
	private static final long serialVersionUID = 1957119618655516192L;
	private Long id;
	private Long goodsid;
	private Long sportid;
	private Long itemid;
	private String rateinfo;	//比率
	private String hours;	//时间段
	private Timestamp fromtime;
	private Timestamp totime;
	
	public GoodsSportGift(){
		
	}
	public GoodsSportGift(Goods goods, String relateinfo){
		this.goodsid = goods.getId();
		this.fromtime = goods.getFromtime();
		this.totime = goods.getTotime();
		this.rateinfo = relateinfo;
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
	public Long getGoodsid() {
		return goodsid;
	}
	public void setGoodsid(Long goodsid) {
		this.goodsid = goodsid;
	}
	public Long getSportid() {
		return sportid;
	}
	public void setSportid(Long sportid) {
		this.sportid = sportid;
	}
	public Long getItemid() {
		return itemid;
	}
	public void setItemid(Long itemid) {
		this.itemid = itemid;
	}
	public String getHours() {
		return hours;
	}
	public void setHours(String hours) {
		this.hours = hours;
	}
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getRateinfo() {
		return rateinfo;
	}
	public void setRateinfo(String rateinfo) {
		this.rateinfo = rateinfo;
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
	public String getForgeTradeno(SportOrder order){
		return "z" + order.getTradeNo().substring(1);
	}
	public String getForgePassword(SportOrder order){
		String str = StringUtils.reverse(order.getCheckpass());
		if(!str.startsWith("12")) return "12"+str.substring(2);
		return "34"+str.substring(2);
	}
	public Map<String, Integer> getRateMap(){
		Map<String, Integer> m = new HashMap<String, Integer>();
		String info = this.rateinfo;
		if(StringUtils.isNotBlank(info)){
			String[] args = info.split(",");
			if(args!=null && args.length>0){
				for(String arg : args){
					String[] tmp = arg.split(":");
					if(tmp!=null && tmp.length>1) m.put(tmp[0], Integer.valueOf(tmp[1]));
				}
			}
		}
		return m;
	}
	public Integer getRatenum(Integer q){
		String key = q+"";
		Integer num = getRateMap().get(key);
		if(num!=null) return num;
		return 0;
	}
	public boolean isGainGift(Integer q){
		return getRatenum(q)>0;
	}
}
