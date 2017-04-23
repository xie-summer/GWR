package com.gewara.web.action.admin.ticket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.TagConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.drama.DisQuantity;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.GoodsDisQuantity;
import com.gewara.model.goods.GoodsPrice;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.SMSRecord;
import com.gewara.service.MessageService;
import com.gewara.untrans.UntransService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class OrderNoteAdminController extends BaseAdminController {
	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	@Autowired@Qualifier("untransService")
	private UntransService untransService;

	@RequestMapping("/admin/order/getOrderInfo.xhtml")
	public String getOrderNote(Long orderId, ModelMap model){
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		model.put("order", order);
		List<OrderNote> noteList = daoService.getObjectListByField(OrderNote.class, "orderid", order.getId());
		model.put("noteList", noteList);
		List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
		if(order instanceof GoodsOrder){
			Map<Long, List<BuyItem>> itemMap = new HashMap<Long, List<BuyItem>>();
			Map<Long, BaseGoods> goodsMap = new HashMap<Long, BaseGoods>();
			Map<Long, GoodsPrice> priceMap = new HashMap<Long, GoodsPrice>();
			Map<Long, GoodsDisQuantity> disMap = new HashMap<Long, GoodsDisQuantity>();
			Map<Long, Object> relateMap = new HashMap<Long, Object>();
			Map<Long, Object> categoryMap = new HashMap<Long, Object>();
			for (BuyItem item : itemList) {
				BaseGoods goods = goodsMap.get(item.getRelatedid());
				if(goods == null){
					goods = daoService.getObject(BaseGoods.class, item.getRelatedid());
					goodsMap.put(item.getRelatedid(), goods);
				}
				Object relate = relateMap.get(goods.getRelatedid());
				if(relate == null){
					relate = relateService.getRelatedObject(goods.getTag(), goods.getRelatedid());
					relateMap.put(goods.getRelatedid(), relate);
				}
				Long itemid = (Long) BeanUtil.get(goods, "itemid");
				String itemtype = (String) BeanUtil.get(goods, "itemtype");
				if(itemid != null && StringUtils.isNotBlank(itemtype)){
					Object category = categoryMap.get(itemid);
					if(category == null){
						category = relateService.getRelatedObject(itemtype, itemid);
						categoryMap.put(itemid, category);
					}
				}
				
				GoodsPrice price = priceMap.get(item.getSmallitemid());
				if(price == null){
					price = daoService.getObject(GoodsPrice.class, item.getSmallitemid());
					priceMap.put(item.getSmallitemid(), price);
				}
				if(item.getDisid() != null){
					GoodsDisQuantity disQuantity = disMap.get(item.getDisid());
					if(disQuantity == null){
						disQuantity = daoService.getObject(GoodsDisQuantity.class, item.getDisid());
						disMap.put(item.getDisid(), disQuantity);
					}
				}
				List<BuyItem> tmpList = itemMap.get(item.getRelatedid());
				if(tmpList == null){
					tmpList = new ArrayList<BuyItem>();
					itemMap.put(item.getRelatedid(), tmpList);
				}
				tmpList.add(item);
			}
			model.put("itemList", itemList);
			model.put("itemMap", itemMap);
			model.put("goodsMap", goodsMap);
			model.put("priceMap", priceMap);
			model.put("disMap", disMap);
			model.put("relateMap", relateMap);
			model.put("categoryMap", categoryMap);
		}else if(order instanceof DramaOrder){
			Map<Long, List<BuyItem>> itemMap = new HashMap<Long, List<BuyItem>>();
			Map<Long, OpenDramaItem> goodsMap = new HashMap<Long, OpenDramaItem>();
			Map<Long, TheatreSeatPrice> priceMap = new HashMap<Long, TheatreSeatPrice>();
			Map<Long, DisQuantity> disMap = new HashMap<Long, DisQuantity>();
			Map<Long, Object> relateMap = new HashMap<Long, Object>();
			Map<Long, Object> categoryMap = new HashMap<Long, Object>();
			for (BuyItem item : itemList) {
				OpenDramaItem odi = goodsMap.get(item.getRelatedid());
				if(odi == null){
					odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", item.getRelatedid());
					goodsMap.put(item.getRelatedid(), odi);
				}
				Object relate = relateMap.get(odi);
				if(relate == null){
					relate = relateService.getRelatedObject(TagConstant.TAG_THEATRE, odi.getTheatreid());
					relateMap.put(odi.getTheatreid(), relate);
				}
				Object category = categoryMap.get(odi.getDramaid());
				if(category == null){
					category = relateService.getRelatedObject(TagConstant.TAG_DRAMA, odi.getDramaid());
					categoryMap.put(odi.getDramaid(), category);
				}
				
				TheatreSeatPrice price = priceMap.get(item.getSmallitemid());
				if(price == null){
					price = daoService.getObject(TheatreSeatPrice.class, item.getSmallitemid());
					priceMap.put(item.getSmallitemid(), price);
				}
				if(item.getDisid() != null){
					DisQuantity disQuantity = disMap.get(item.getDisid());
					if(disQuantity == null){
						disQuantity = daoService.getObject(DisQuantity.class, item.getDisid());
						disMap.put(item.getDisid(), disQuantity);
					}
				}
				List<BuyItem> tmpList = itemMap.get(item.getRelatedid());
				if(tmpList == null){
					tmpList = new ArrayList<BuyItem>();
					itemMap.put(item.getRelatedid(), tmpList);
				}
				tmpList.add(item);
			}
			model.put("itemList", itemList);
			model.put("itemMap", itemMap);
			model.put("goodsMap", goodsMap);
			model.put("priceMap", priceMap);
			model.put("disMap", disMap);
			model.put("relateMap", relateMap);
			model.put("categoryMap", categoryMap);
		}
		return "admin/goods/orderInfo.vm";
	}
	
	@RequestMapping("/admin/order/sendOrderNoteSms.xhtml")
	public String sendOrderNoteSms(Long id, ModelMap model){
		OrderNote orderNote = daoService.getObject(OrderNote.class, id);
		if(orderNote == null) return showJsonError_NOT_FOUND(model);
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", orderNote.getTradeno());
		if(!StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)) return showJsonError(model, "不是成功订单不能发短信！");
		if(order instanceof GoodsOrder){
			SMSRecord sms = messageService.addOrderNoteSms((GoodsOrder)order, orderNote, DateUtil.getCurFullTimestamp());
			if(sms != null){
				untransService.sendMsgAtServer(sms, false);
				return showJsonSuccess(model);
			}
		}else if(order instanceof DramaOrder){
			SMSRecord sms = messageService.addOrderNoteSms((DramaOrder)order, orderNote, DateUtil.getCurFullTimestamp());
			if(sms != null){
				untransService.sendMsgAtServer(sms, false);
				return showJsonSuccess(model);
			}
		}
		return showJsonError(model, "发送短信错误！");
	}
}
