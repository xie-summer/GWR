package com.gewara.helper.ticket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gewara.model.drama.DisQuantity;
import com.gewara.model.drama.TheatreSeatPrice;

public class SeatPriceUtil {
	private Map<String, Integer> limitpointMap = new HashMap<String, Integer>();
	private Map<String, Integer> deductpointMap = new HashMap<String, Integer>();
	private Map<String, Integer> limitnumMap = new HashMap<String, Integer>();
	private Map<String, Integer> priceMap = new HashMap<String, Integer>();
	private Map<String, Integer> costpriceMap = new HashMap<String, Integer>();
	private Map<String, Integer> theatrepriceMap = new HashMap<String, Integer>();
	private List<Integer> priceList = new ArrayList<Integer>();
	private List<Integer> tpriceList = new ArrayList<Integer>();
	private Map<String, Integer> quantityMap = new HashMap<String, Integer>();
	private Set<DisQuantity> discountSet = new HashSet<DisQuantity>();
	private Map<String, String> openMap = new HashMap<String, String>(); 
	private Map<String, String> closeMap = new HashMap<String, String>(); 
	private Map<String, String> partnerMap = new HashMap<String, String>(); 
	private Map<String, String> saletypeMap = new HashMap<String, String>();
	
	public SeatPriceUtil(List<TheatreSeatPrice> seatPriceList, List<DisQuantity> disquantityList){
		Set<Integer> priceSet = new HashSet<Integer>();
		Set<Integer> tpriceSet = new HashSet<Integer>();
		for(TheatreSeatPrice sp:seatPriceList){
			priceMap.put(sp.getSeattype(), sp.getPrice());
			costpriceMap.put(sp.getSeattype(), sp.getCostprice());
			theatrepriceMap.put(sp.getSeattype(), sp.getTheatreprice());
			quantityMap.put(sp.getSeattype(), sp.getQuantity());
			discountSet.addAll(disquantityList);
			priceSet.add(sp.getPrice());
			tpriceSet.add(sp.getTheatreprice());
		}
		priceList.addAll(priceSet);
		tpriceList.addAll(tpriceSet);
		Collections.sort(priceList);
		Collections.sort(tpriceList);
	}
	public List<Integer> getPriceList(){
		return priceList;
	}
	public List<Integer> getTpriceList() {
		return tpriceList;
	}
	public Integer getLimitpoint(String seattype){
		return limitpointMap.get(seattype);
	}
	public Integer getDeductpoint(String seattype){
		return deductpointMap.get(seattype);
	}
	public Integer getLimitnum(String seattype){
		return limitnumMap.get(seattype);
	}
	public Integer getPrice(String seattype){
		return priceMap.get(seattype);
	}
	public Integer getCostPrice(String seattype){
		return costpriceMap.get(seattype);
	}
	public Integer getTheatrePrice(String seattype){
		return theatrepriceMap.get(seattype);
	}
	public Integer getQuantity(String seattype){
		return quantityMap.get(seattype);
	}
	
	public Integer getQuantityCount(){
		return discountSet.size();
	}
	
	public String getOpentime(String seattype){
		return openMap.get(seattype);
	}
	public String getClosetime(String seattype){
		return closeMap.get(seattype);
	}
	public String getPartner(String seattype){
		return partnerMap.get(seattype);
	}
	public String getSaletype(String seattype){
		return saletypeMap.get(seattype);
	}
}
