package com.gewara.untrans.ticket;

import java.util.List;
import java.util.Map;

import com.gewara.command.GoodsCommand;
import com.gewara.model.api.ApiUser;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.user.Member;
import com.gewara.support.ErrorCode;

public interface TicketGoodsService {
	/**
	 * 单个场次或物品单个价格生成订单
	 * @param goods
	 * @param member
	 * @param mobile
	 * @param quantity
	 * @param disid
	 * @param priceid
	 * @return
	 */
	ErrorCode<GoodsOrder> addTicketGoodsOrder(TicketGoods goods, Member member, String mobile, Integer quantity, Long disid, Long priceid, ApiUser partner, String ukey);
	ErrorCode<GoodsOrder> addTicketGoodsOrder(TicketGoods goods, Member member, String mobile, Integer quantity, Long disid, Long priceid);
	/**
	 * 多个场次或物品多个价格或套票生成订单
	 * @param pricelist
	 * @param member
	 * @param mobile
	 * @return
	 */
	ErrorCode<GoodsOrder> addTicketGoodsOrder(String pricelist, Member member, String mobile, ApiUser partner, String ukey);
	ErrorCode<GoodsOrder> addTicketGoodsOrder(String pricelist, Member member, String mobile);
	ErrorCode<String> payTicketGoodsOrder(GoodsOrder order, BaseGoods goods);
	
	ErrorCode<Map> getGoodsPriceInfo(List<GoodsCommand> commandList);
	
	ErrorCode<GoodsOrder> addTrainingGoodsOrder(Long goodsId, Long gspId, Integer quantity, String mobile, String infoList, Member member);
	ErrorCode<GoodsOrder> addTrainingGoodsOrder(Long goodsId, Long gspId, Integer quantity, String mobile, String infoList, Member member, ApiUser partner, String ukey);
	
}
