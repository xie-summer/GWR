package com.gewara.model.goods;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.BaseObject;
import com.gewara.model.pay.TicketOrder;

public class GoodsGift extends BaseObject{
	private static final long serialVersionUID = 1957119618655516192L;
	private Long id;
	private Long goodsid;
	private Long cinemaid;
	private Long movieid;
	private Long mpid;			
	private String rateinfo;	//比率
	private String week;			
	private String mpidlist;	//场次列表
	private Timestamp fromtime;
	private Timestamp totime;
	private Integer everydayLimit;//每天限量
	private String startTime;//下单开始时段
	private String endTime;//下单结束时段
	
	public GoodsGift(){
		
	}
	public GoodsGift(Goods goods, String relateinfo){
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
	public Long getCinemaid() {
		return cinemaid;
	}
	public void setCinemaid(Long cinemaid) {
		this.cinemaid = cinemaid;
	}
	public Long getMovieid() {
		return movieid;
	}
	public void setMovieid(Long movieid) {
		this.movieid = movieid;
	}
	public Long getMpid() {
		return mpid;
	}
	public void setMpid(Long mpid) {
		this.mpid = mpid;
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
	public String getForgeTradeno(TicketOrder order){
		return "z" + order.getTradeNo().substring(1);
	}
	public String getForgePassword(TicketOrder order){
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
	public String getWeek() {
		return week;
	}
	public void setWeek(String week) {
		this.week = week;
	}
	public String getMpidlist() {
		return mpidlist;
	}
	public void setMpidlist(String mpidlist) {
		this.mpidlist = mpidlist;
	}
	public Integer getEverydayLimit() {
		return everydayLimit;
	}
	public void setEverydayLimit(Integer everydayLimit) {
		this.everydayLimit = everydayLimit;
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
