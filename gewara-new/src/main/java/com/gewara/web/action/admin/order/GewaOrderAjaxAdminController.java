package com.gewara.web.action.admin.order;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.drama.Drama;
import com.gewara.model.drama.Theatre;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.OrderAddress;
import com.gewara.model.pay.OrderNote;
import com.gewara.util.BeanUtil;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class GewaOrderAjaxAdminController extends BaseAdminController {

	@RequestMapping("/admin/order/ajax/getBuyItemList.xhtml")
	public String getBuyItemList(Long orderid, ModelMap model){
		GewaOrder order = daoService.getObject(GewaOrder.class, orderid);
		model.put("order", order);
		List<BuyItem> buyItemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
		Set<Long> theatreIdSet = new HashSet<Long>();
		Set<Long> dramaIdSet = new HashSet<Long>();
		model.put("itemList", buyItemList);
		for (BuyItem buyItem : buyItemList) {
			theatreIdSet.add(buyItem.getPlaceid());
			dramaIdSet.add(buyItem.getItemid());
		}
		Map<Long, Theatre> orderTheatreMap = daoService.getObjectMap(Theatre.class, theatreIdSet);
		Map<Long, Drama> orderDramaMap = daoService.getObjectMap(Drama.class, dramaIdSet);
		model.put("orderTheatreMap", orderTheatreMap);
		model.put("orderDramaMap", orderDramaMap);
		if(order.isPaidSuccess()){
			List<OrderNote> noteList = daoService.getObjectListByField(OrderNote.class, "orderid", order.getId());
			Map<Long,OrderNote> noteMap = BeanUtil.beanListToMap(noteList, "smallitemid");
			model.put("noteMap", noteMap);
		}
		OrderAddress orderAddress = daoService.getObject(OrderAddress.class, order.getTradeNo());
		model.put("orderAddress", orderAddress);
		return "admin/order/ajax/buyItemList.vm";
	}
}
