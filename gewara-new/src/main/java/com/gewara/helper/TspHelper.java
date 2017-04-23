package com.gewara.helper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.model.drama.DisQuantity;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;

public class TspHelper {
	private List<TheatreSeatPrice> tspList;
	private List<DisQuantity> dqList;
	public TspHelper(List<TheatreSeatPrice> tspList){
		this.tspList = tspList;
	}
	public TspHelper(List<TheatreSeatPrice> tspList, List<DisQuantity> dqList){
		this.tspList = tspList;
		this.dqList = dqList;
	}
	
	public List<TheatreSeatPrice> getTspListBySno(){
		List<TheatreSeatPrice> result = new ArrayList<TheatreSeatPrice>(tspList);
		Collections.sort(result, new MultiPropertyComparator<TheatreSeatPrice>(new String[]{"seattype"}, new boolean[]{true}));
		return result;
	}
	
	public List<Integer> getPriceList(String priceTag){
		Set<Integer> result = new HashSet<Integer>();
		for(TheatreSeatPrice tsp : tspList){
			if(StringUtils.equals(priceTag, "theatre") && (tsp.getTheatreprice() != null)){
				result.add(tsp.getTheatreprice());
			}else if(StringUtils.equals(priceTag, "cost")){
				result.add(tsp.getCostprice());
			}else if(StringUtils.equals(priceTag, "gewa")){
				result.add(tsp.getPrice());
			}
		}
		List list = new ArrayList();
		list.addAll(result);
		Collections.sort(list);
		return list;
	}
	public List<TheatreSeatPrice> getTspBySno(){
		List<TheatreSeatPrice> result = new ArrayList<TheatreSeatPrice>();
		for(TheatreSeatPrice tsp : tspList){
			if(tsp.getTheatreprice()!=null && StringUtils.equalsIgnoreCase(tsp.getStatus(), "Y")){
				result.add(tsp);
			}
		}
		Collections.sort(result, new MultiPropertyComparator<TheatreSeatPrice>(new String[]{"price"}, new boolean[]{true}));
		return result;
	}
	public TheatreSeatPrice getFirstTsp(){
		if(tspList.size()==0) return null;
		List<TheatreSeatPrice> list = getTspListBySno();
		return list.get(0);
	}
	
	public List<DisQuantity> getDiscountList(TheatreSeatPrice tsp){
		List<DisQuantity> newList = new ArrayList<DisQuantity>();
		for(DisQuantity dq : getValidDiscountList()){
			if(tsp != null && tsp.getId().equals(dq.getTspid())){
				newList.add(dq);
			}
		}
		return newList;
	}

	public List<DisQuantity> getValidDiscountList(){
		List<DisQuantity> tmpList = new ArrayList<DisQuantity>();
		Map<Long, TheatreSeatPrice> priceMap = BeanUtil.beanListToMap(getTspList(), "id");
		Timestamp cur = DateUtil.getCurFullTimestamp();
		for (DisQuantity disQuantity : dqList) {
			TheatreSeatPrice seatPrice = priceMap.get(disQuantity.getTspid());
			if(seatPrice == null) continue;
			if(!seatPrice.hasStatus(Status.DEL) && (disQuantity.hasStatus(Status.Y) && disQuantity.getPrice()>0 && disQuantity.getEndtime().after(cur) || disQuantity.hasStatus(Status.N))){
				tmpList.add(disQuantity);
			}
		}
		return tmpList;
	}
	
	public Map<Integer, String>  getTheatreSeatPriceMap(){
		Map<Integer, String> seatPriceMap = new LinkedHashMap<Integer, String>();
		//30,Y 30 N, 50,Y 50,N 排序
		Collections.sort(tspList, new MultiPropertyComparator(new String[]{"price","status"}, new boolean[]{true,false}));
		for (TheatreSeatPrice seatPrice : tspList) {
			if(!seatPriceMap.containsKey(seatPrice.getPrice())){
				seatPriceMap.put(seatPrice.getPrice(), seatPrice.getStatus());
			}
		}
		return seatPriceMap;
	}
	
	public Map<String,Map<Integer, String>> getValidDiscountMap(){
		Map<String, Map<Integer, String>> discountMap = new LinkedHashMap<String, Map<Integer, String>>();
		List<DisQuantity> disList = getValidDiscountList();
		//50,Y 50,N 30,Y 30,N 排序
		Collections.sort(disList, new MultiPropertyComparator(new String[]{"price","status"}, new boolean[]{false,false}));
		Map<Long, TheatreSeatPrice> priceMap = BeanUtil.beanListToMap(getTspList(), "id");
		Map<Integer, String> seatPriceMap = getTheatreSeatPriceMap();
		for (DisQuantity disQuantity : disList) {
			TheatreSeatPrice seatPrice = priceMap.get(disQuantity.getTspid());
			if(seatPrice == null) continue;
			String status = seatPriceMap.get(seatPrice.getPrice());
			if(StringUtils.isBlank(status)) continue;
			String key = "含" + seatPrice.getPrice() + "元票 x " + disQuantity.getQuantity() + "张";
			if(!discountMap.containsKey(key)){
				Map<Integer,String> value = new HashMap<Integer, String>();
				status = StringUtils.equals(status, Status.N) ? status : disQuantity.getStatus();
				value.put(disQuantity.getPrice(), status);
				discountMap.put(key, value);
			}
		}
		return discountMap;
	}
	
	public List<TheatreSeatPrice> getTspList() {
		return tspList;
	}
	public List<DisQuantity> getDqList() {
		return dqList;
	}

	public static String getTheatrePriceDisabledReason(TheatreSeatPrice seatPrice, final int quantity){
		if(seatPrice.getQuantity() <= seatPrice.getSales() 
				|| seatPrice.getAllowaddnum() <quantity){
			return seatPrice.getPrice() + "元商品库存数量不足，请联系管理员！";
		}else if(seatPrice.getAllowaddnum() <= 0){
			return seatPrice.getPrice() + "元下单人数过多，您可等15分钟内未支付的订单释放名额！";
		}
		return "";
	}
	
	public static TheatreSeatPrice updateTheatrePriceAddCounter(TheatreSeatPrice seatPrice, int quantity){
		int count = seatPrice.getAllowaddnum();
		if(quantity>0){
			count -= quantity;
		}
		seatPrice.setAllowaddnum(count);
		return seatPrice;
	}
	
	public static List updateTheatrePriceSubAddCounter(Map<TheatreSeatPrice, Integer> priceMap){
		List params = new ArrayList();
		for (TheatreSeatPrice seatPrice : priceMap.keySet()) {
			int tmp = priceMap.get(seatPrice);
			int allowaddnum = seatPrice.getAllowaddnum();
			allowaddnum += tmp;
			allowaddnum = allowaddnum<0?0:allowaddnum;
			allowaddnum = allowaddnum>seatPrice.getQuantity()?seatPrice.getQuantity():allowaddnum;
			seatPrice.setAllowaddnum(allowaddnum);
			params.add(seatPrice);
		}
		return params;
	}
	
	public static List updateTheatrePriceSellCounter(Map<TheatreSeatPrice, Integer> priceMap, Map<Long, Map<DisQuantity, Integer>> priceDisMap){
		List params = new ArrayList();
		for (TheatreSeatPrice seatPrice : priceMap.keySet()) {
			int sellorder = seatPrice.getSellordernum();
			sellorder +=1;
			seatPrice.setSellordernum(sellorder);
			int sellquantity = seatPrice.getSales();
			sellquantity += priceMap.get(seatPrice);
			seatPrice.setSales(sellquantity);
			params.add(seatPrice);
			Map<DisQuantity, Integer> disMap = priceDisMap.get(seatPrice.getId());
			if(disMap != null){
				for (DisQuantity discount : disMap.keySet()) {
					int disSellorder = discount.getSellordernum();
					disSellorder += disMap.get(discount);
					discount.setSellordernum(disSellorder);
					params.add(discount);
				}
			}
		}
		return params;
	}
	
	public static List updateTheatrePriceSubSellCounter(Map<TheatreSeatPrice, Integer> priceMap, Map<Long, Map<DisQuantity, Integer>> priceDisMap){
		List params = new ArrayList();
		for (TheatreSeatPrice seatPrice : priceMap.keySet()) {
			int sellorder = seatPrice.getSellordernum();
			sellorder -=1;
			seatPrice.setSellordernum(sellorder);
			int sellquantity = seatPrice.getSales();
			int tmp = priceMap.get(seatPrice);
			sellquantity -= tmp;
			sellquantity = sellquantity<0 ? 0: sellquantity;
			seatPrice.setSales(sellquantity);
			params.add(seatPrice);
			Map<DisQuantity, Integer> disMap = priceDisMap.get(seatPrice.getId());
			if(disMap != null){
				for (DisQuantity discount : disMap.keySet()) {
					int disSellorder = discount.getSellordernum();
					disSellorder -= disMap.get(discount);
					disSellorder = disSellorder<0 ? 0 : disSellorder;
					discount.setSellordernum(disSellorder);
					params.add(discount);
				}
			}
		}
		return params;
	}
}
