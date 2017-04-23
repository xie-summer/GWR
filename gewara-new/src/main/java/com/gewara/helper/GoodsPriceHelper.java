package com.gewara.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.agency.TrainingGoods;
import com.gewara.model.goods.GoodsDisQuantity;
import com.gewara.model.goods.GoodsPrice;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.util.StringUtil;

public class GoodsPriceHelper {
	private List<GoodsPrice> goodsPriceList = new ArrayList<GoodsPrice>();
	private List<GoodsDisQuantity> disList = new ArrayList<GoodsDisQuantity>();

	public GoodsPriceHelper(List<GoodsPrice> goodsPriceList){
		this.goodsPriceList = goodsPriceList;
	}
	public GoodsPriceHelper(List<GoodsPrice> goodsPriceList, List<GoodsDisQuantity> disList){
		this.goodsPriceList = goodsPriceList;
		this.disList = disList;
	}
	public List<GoodsPrice> getGoodsPriceListBySno(){
		List<GoodsPrice> result = new ArrayList<GoodsPrice>(goodsPriceList);
		Collections.sort(result, new MultiPropertyComparator<GoodsPrice>(new String[]{"seattype"}, new boolean[]{true}));
		return result;
	}
	public List<Integer> getPriceList(String priceTag){
		Set<Integer> result = new HashSet<Integer>();
		for(GoodsPrice tsp : goodsPriceList){
			if(StringUtils.equals(priceTag, "ori") && (tsp.getOriprice() != null)){
				result.add(tsp.getOriprice());
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
	public List<Integer> getPriceListBySno(String priceTag){
		Set<Integer> result = new HashSet<Integer>();
		for(GoodsPrice tsp : goodsPriceList){
			if(StringUtils.equals(priceTag, "ori") && (tsp.getOriprice() != null)){
				result.add(tsp.getOriprice());
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

	public List<Integer> getGewaPriceListBySno(){
		Set<Integer> result = new HashSet<Integer>();
		for(GoodsPrice tsp : goodsPriceList){
			if(tsp.getPrice()!=null 
				&& tsp.getPrice()>0 
				&& !StringUtils.equalsIgnoreCase(tsp.getStatus(), "N")){
				result.add(tsp.getPrice());
			}
		}
		List list = new ArrayList(result);
		Collections.sort(list);
		return list;
	}
	public List<GoodsPrice> getGoodsPriceBySno(){
		List<GoodsPrice> result = new ArrayList<GoodsPrice>();
		for(GoodsPrice tsp : goodsPriceList){
			if(tsp.getOriprice()!=null && StringUtils.equalsIgnoreCase(tsp.getStatus(), "Y")){
				result.add(tsp);
			}
		}
		Collections.sort(result, new MultiPropertyComparator<GoodsPrice>(new String[]{"price"}, new boolean[]{true}));
		return result;
	}
	public List<GoodsPrice> getGoodsPriceBySno2(){
		List<GoodsPrice> result = new ArrayList<GoodsPrice>();
		for(GoodsPrice tsp : goodsPriceList){
			if(tsp.getOriprice()!=null){
				result.add(tsp);
			}
		}
		Collections.sort(result, new MultiPropertyComparator<GoodsPrice>(new String[]{"oriprice"}, new boolean[]{true}));
		return result;
	}
	@SuppressWarnings("unchecked")
	public List<GoodsPrice> getGoodsPriceBySno3(){
		List<GoodsPrice> result = new ArrayList<GoodsPrice>(goodsPriceList);
		Collections.sort(result, new MultiPropertyComparator<GoodsPrice>(new String[]{"price"}, new boolean[]{true}));
		return result;
	}
	public GoodsPrice getGoodsPriceByPrice(Integer price){
		List<GoodsPrice> tmpList = getGoodsPriceBySno();
		for(GoodsPrice tsp : tmpList){
			if(StringUtils.equals(tsp.getPrice()+"", price+"")) return tsp;
		}
		return null;
	}
	public GoodsPrice getFirstTsp(){
		if(goodsPriceList.size()==0) return null;
		List<GoodsPrice> list = getGoodsPriceListBySno();
		return list.get(0);
	}

	public List<GoodsPrice> getGoodsPriceList() {
		return goodsPriceList;
	}
	public void setGoodsPriceList(List<GoodsPrice> goodsPriceList) {
		this.goodsPriceList = goodsPriceList;
	}
	
	public List<GoodsDisQuantity> getDiscountList(GoodsPrice tsp){
		List<GoodsDisQuantity> newList = new ArrayList<GoodsDisQuantity>();
		for(GoodsDisQuantity dis : disList){
			if(StringUtils.equals(tsp.getId()+"", dis.getGspid()+"")){
				newList.add(dis);
			}
		}
		return newList;
	}
	
	public List<GoodsDisQuantity> getDisList() {
		return disList;
	}
	public void setDisList(List<GoodsDisQuantity> disList) {
		this.disList = disList;
	}
	public static String getGoodsPriceDisabledReason(GoodsPrice goodsPrice, GoodsDisQuantity discount, int quantity){
		if(goodsPrice.getQuantity() <= goodsPrice.getSellquantity() 
				|| goodsPrice.getAllowaddnum() <quantity){
			return goodsPrice.getPrice() + "元商品已抢光！";
		}else if(goodsPrice.getAllowaddnum() <= 0){
			return goodsPrice.getPrice() + "元下单人数过多，您可等15分钟内未支付的订单释放名额！";
		}else if(discount != null){
			int disQuantity = quantity /discount.getQuantity();
			if(discount.getAllownum() <discount.getSellordernum()+disQuantity){
				return discount.getPrice() + "元("+ goodsPrice.getPrice() + " x " +discount.getQuantity()+ ")优惠库存数量不足，不能购票优惠！";
			}
		}
		return "";
	}
	public static String getTrainingGoodsPriceDisabledReason(TrainingGoods trainingGoods, GoodsPrice goodsPrice, int quantity){
		if(trainingGoods.getQuantity() <= trainingGoods.getSales() 
				|| trainingGoods.getAllowaddnum() < quantity){
			return trainingGoods.getGoodsname() + "该商品已抢光！";
		}else if(trainingGoods.getAllowaddnum() <= 0){
			return trainingGoods.getGoodsname() + "商品下单人数过多，您可等15分钟内未支付的订单释放名额！";
		}
		return getGoodsPriceDisabledReason(goodsPrice, null, quantity);
	}
	
	public static GoodsPrice updateGoodsPriceAddCounter(GoodsPrice goodsPrice, int quantity){
		int count = goodsPrice.getAllowaddnum();
		if(quantity>0){
			count -= quantity;
		}
		goodsPrice.setAllowaddnum(count);
		return goodsPrice;
	}
	
	public static List updateGoodsPriceSellCounter(Map<GoodsPrice, Integer> priceMap, Map<Long, Map<GoodsDisQuantity, Integer>> priceDisMap){
		List params = new ArrayList();
		for (GoodsPrice goodsPrice : priceMap.keySet()) {
			int sellorder = goodsPrice.getSellordernum();
			sellorder +=1;
			goodsPrice.setSellordernum(sellorder);
			int sellquantity = goodsPrice.getSellquantity();
			sellquantity += priceMap.get(goodsPrice);
			goodsPrice.setSellquantity(sellquantity);
			params.add(goodsPrice);
			Map<GoodsDisQuantity, Integer> disMap = priceDisMap.get(goodsPrice.getId());
			if(disMap != null){
				for (GoodsDisQuantity discount : disMap.keySet()) {
					int disSellorder = discount.getSellordernum();
					disSellorder += disMap.get(discount);
					discount.setSellordernum(disSellorder);
					params.add(discount);
				}
			}
		}
		return params;
	}
	
	public static List updateGoodsPriceSubSellCounter(Map<GoodsPrice, Integer> priceMap, Map<Long, Map<GoodsDisQuantity, Integer>> priceDisMap){
		List params = new ArrayList();
		for (GoodsPrice goodsPrice : priceMap.keySet()) {
			int sellorder = goodsPrice.getSellordernum();
			sellorder -=1;
			goodsPrice.setSellordernum(sellorder);
			int sellquantity = goodsPrice.getSellquantity();
			int tmp = priceMap.get(goodsPrice);
			sellquantity -= tmp;
			sellquantity = sellquantity<0 ? 0: sellquantity;
			int allownum = goodsPrice.getAllowaddnum();
			allownum += tmp;
			allownum = allownum>goodsPrice.getQuantity()?goodsPrice.getQuantity():allownum;
			goodsPrice.setAllowaddnum(allownum);
			goodsPrice.setSellquantity(sellquantity);
			params.add(goodsPrice);
			Map<GoodsDisQuantity, Integer> disMap = priceDisMap.get(goodsPrice.getId());
			if(disMap != null){
				for (GoodsDisQuantity discount : disMap.keySet()) {
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
	
	public static boolean isValidData(String pricelist){
		if(StringUtils.isBlank(pricelist)) {
			return false;
		}
		return StringUtil.regMatch(pricelist, "^\\[(\\{(\"\\w+\":.*[^,])(,\"\\w+\":.*){3}\\})(,\\{(\"\\w+\":.*[^,])(,\"\\w+\":.*){3}\\})*\\]$", true);
	}
	
	public static boolean isValidTrainingData(String pricelist){
		if(StringUtils.isBlank(pricelist)) {
			return false;
		}
		return StringUtil.regMatch(pricelist, "^\\[(\\{(\"\\w+\":.+[^,])(,\"\\w+\":.+){2}\\})(,\\{(\"\\w+\":.+[^,])(,\"\\w+\":.+){2}\\})*\\]$", true);
	}
}
