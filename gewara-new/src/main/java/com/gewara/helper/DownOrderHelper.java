package com.gewara.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.TagConstant;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderNote;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;

public class DownOrderHelper {
	public static Map<String, String> downDramaOrder(GewaOrder gorder, OrderNote orderNote, OpenDramaItem odi){
		Map<String, String> map = new HashMap<String, String>();
		if(gorder instanceof DramaOrder) {
			Map<String, String> otherMap = JsonUtils.readJsonToMap(gorder.getOtherinfo());
			String seatprice = "";
			String pricetype = "";
			String dis = "N";
			if(otherMap.containsKey("disid")){	
				dis = "Y";
			}
			for(int i =1; i<=gorder.getQuantity();i++){
				seatprice = seatprice + "," + gorder.getUnitprice();
				pricetype = pricetype + "," + dis;
			}
			DramaOrder order = (DramaOrder) gorder;
			String smobile = StringUtils.substring(order.getMobile(), 7);
			map.put("tradeno", orderNote.getSerialno());
			map.put("showid", order.getDpid()+"");
			map.put("dramaname", odi.getDramaname());
			map.put("theatrename", odi.getTheatrename());
			map.put("theatreid", order.getTheatreid()+"");
			map.put("roomname", odi.getRoomname());
			map.put("ticketnum", orderNote.getTicketnum()+"");
			map.put("unitprice", order.getUnitprice()+"");
			map.put("nickname", order.getMembername());
			map.put("ordertype", "drama");
			map.put("opentype", odi.getOpentype());
			map.put("seller", odi.getSeller());
			map.put("mobile", smobile);
			map.put("shortmobile", smobile);
			map.put("playtime", DateUtil.formatTimestamp(odi.getPlaytime()));
			map.put("ordertime", DateUtil.formatTimestamp(order.getAddtime()));
			map.put("synchtype", "0");
			map.put("seatprice", seatprice.substring(1));
			map.put("pricetype", pricetype.substring(1));
		}
		return map;
	}
	
	public static Map<String, String> downOrder(GewaOrder gorder, OrderNote orderNote, BaseGoods goods, List<BuyItem> buyItemList){
		Map<String, String> map = new HashMap<String, String>();
		if(gorder instanceof GoodsOrder) {
			if(goods instanceof TicketGoods){
				if(StringUtils.equals(goods.getTag(), TagConstant.TAG_THEATRE)){
					return downDramaOrder(gorder, orderNote, (TicketGoods)goods, buyItemList);
				}
			}
		}
		return map;
	}
	
	public static Map<String, String> downDramaOrder(GewaOrder gorder,  OrderNote orderNote, TicketGoods goods, List<BuyItem> buyItemList){
		Map<String, String> map = new HashMap<String, String>();
		if(gorder instanceof GoodsOrder) {
			String seatprice = "";
			String pricetype = "";
			for(BuyItem buyItem : buyItemList){
				if(buyItem.getRelatedid().equals(goods.getId())){
					for(int i =1; i<=buyItem.getQuantity();i++){
						seatprice +="," + buyItem.getUnitprice();
						pricetype +="," + (buyItem.getDisid()!=null?"Y":"N");
					}
				}
			}
			GoodsOrder order = (GoodsOrder) gorder;
			String smobile = StringUtils.substring(order.getMobile(), 7);
			map.put("tradeno", orderNote.getSerialno());
			map.put("showid", order.getGoodsid()+"");
			map.put("dramaname", goods.gainBriefname());
			map.put("theatrename", orderNote.getPlacename());
			map.put("roomname", orderNote.getPlacename());
			map.put("theatreid", orderNote.getPlaceid()+"");
			map.put("ticketnum", orderNote.getTicketnum()+"");
			map.put("unitprice", order.getUnitprice()+"");
			map.put("nickname", order.getMembername());
			map.put("ordertype", "goods");
			map.put("mobile", smobile);
			map.put("shortmobile", smobile);
			if(goods.getFromvalidtime()!=null){
				map.put("playtime", DateUtil.formatTimestamp(goods.getFromvalidtime()));
			}else {
				map.put("playtime", DateUtil.formatTimestamp(order.getValidtime()));
			}
			map.put("ordertime", DateUtil.formatTimestamp(order.getAddtime()));
			map.put("seatprice", order.getTotalfee()+"");
			map.put("synchtype", "0");
			map.put("seatprice", seatprice.substring(1));
			map.put("pricetype", pricetype.substring(1));
		}
		
		return map;
	}
	
}
