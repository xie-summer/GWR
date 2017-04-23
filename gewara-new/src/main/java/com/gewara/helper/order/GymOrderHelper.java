package com.gewara.helper.order;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.pay.GymOrder;
import com.gewara.util.DateUtil;
import com.gewara.xmlbind.gym.CardItem;

public abstract class GymOrderHelper implements Serializable {
	private static final long serialVersionUID = 7595503375246942430L;

	public static GymOrder createGymOrder(Long memberid, String membername, CardItem gymCardItem, String ukey){
		GymOrder order = new GymOrder();
		order.setCreatetime(new Timestamp(System.currentTimeMillis()));
		order.setAddtime(order.getCreatetime());
		order.setUpdatetime(order.getAddtime());
		order.setModifytime(order.getAddtime());
		order.setValidtime(DateUtil.addHour(order.getAddtime(), 2));
		order.setPaymethod(PaymethodConstant.PAYMETHOD_PNRPAY); // Ä¬ÈÏÍøÒø
		order.setStatus(OrderConstant.STATUS_NEW_UNLOCK);
		order.setPricategory(OrderConstant.ORDER_PRICATEGORY_GYM);
		order.setMemberid(memberid);
		order.setMembername(membername);
		order.setAlipaid(0);
		order.setOtherfee(0);
		order.setGewapaid(0);
		order.setDiscount(0);
		order.setItemfee(0);
		order.setWabi(0);
		order.setGymid(gymCardItem.getGymid());
		order.setGci(gymCardItem.getId());
		order.setCitycode("310000");
		order.setStatus(OrderConstant.STATUS_NEW);
		order.setUkey(ukey);
		order.setPartnerid(PartnerConstant.GEWA_SELF);
		order.setOtherinfo("{}");
		order.setSettle(OrderConstant.SETTLE_NONE);
		order.setExpress(Status.N);
		return order;
	}
}
