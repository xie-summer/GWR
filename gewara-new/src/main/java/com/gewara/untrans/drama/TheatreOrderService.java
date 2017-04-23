package com.gewara.untrans.drama;

import java.util.List;
import java.util.Map;

import org.springframework.ui.ModelMap;

import com.gewara.command.TheatrePriceCommand;
import com.gewara.helper.order.OrderOther;
import com.gewara.model.acl.User;
import com.gewara.model.api.ApiUser;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.user.Member;
import com.gewara.support.ErrorCode;

public interface TheatreOrderService {

	ErrorCode<Map> getTheatreSeatPriceInfo(List<TheatrePriceCommand> commandList);

	ErrorCode<DramaOrder> addDramaOrder(String pricelist, Member member, String mobile, ApiUser partner, String ukey, String spkey);

	ErrorCode<DramaOrder> addDramaOrder(String pricelist, Member member, String mobile, String spkey);
	ErrorCode<DramaOrder> addDramaOrder(String pricelist, Member member, String mobile, ApiUser partner, String ukey);

	ErrorCode<String> payDramaOrder(DramaOrder order, OpenDramaItem odi);

	ErrorCode<DramaOrder> addDramaOrder(OpenDramaItem odi, Member member, String mobile, Integer quantity, Long disid, Long priceid, ApiUser partner, String ukey, String spkey);

	ErrorCode<DramaOrder> addDramaOrder(OpenDramaItem odi, Member member, String mobile, Integer quantity, Long disid, Long priceid, String spkey);

	ErrorCode<DramaOrder> addDramaOrder(OpenDramaItem odi, TheatreSeatArea seatArea, String seatLabel, Long disid, String mobile, Member member, ApiUser partner, String spkey);

	ErrorCode<DramaOrder> addDramaOrder(OpenDramaItem odi, TheatreSeatArea seatArea, List<Long> seatidList, Long disid, String mobile, Member member, String spkey);
	
	ErrorCode<DramaOrder> addDramaOrder(String pricelist, String mobile, ApiUser partner, User user, String telephone, final boolean isBind, String bindmobile, String checkpass);
	
	ErrorCode<DramaOrder> addDramaOrder(OpenDramaItem odi, TheatreSeatArea seatArea, List<Long> seatidList, Long disid, String mobile, ApiUser partner, User user, String telephone, final boolean isBind, String bindmobile, String checkpass);
	
	void cancelDramaOrder(DramaOrder order, String ukey, String reason);

	void cancelDramaOrder(String tradeNo, String ukey, String reason);

	ErrorCode processLastOrder(Long memberid, String ukey);

	OrderOther getDramaOrderOtherData(DramaOrder order, List<BuyItem> buyList, final Map<Long, OpenDramaItem> odiMap, ModelMap model);
	
	ErrorCode updateOtherInfo(DramaOrder order, String greeting, Long memberid, ModelMap model);

}
