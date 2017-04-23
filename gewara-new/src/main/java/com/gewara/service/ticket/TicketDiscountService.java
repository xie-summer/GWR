package com.gewara.service.ticket;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.gewara.helper.discount.SportSpecialDiscountHelper.OrderCallback;
import com.gewara.helper.order.OrderContainer;
import com.gewara.helper.order.TicketOrderContainer;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.SpCode;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.user.Member;
import com.gewara.model.user.TempMember;
import com.gewara.service.OrderException;
import com.gewara.support.ErrorCode;

/**
 * 订单优惠相关
 * @author gebiao(ge.biao@gewara.com)
 * @since Feb 1, 2013 7:41:18 PM
 */
public interface TicketDiscountService {
	/**
	 * 使用积分
	 * @param orderId
	 * @param member
	 * @param usePoint
	 * @return
	 */
	ErrorCode usePoint(Long orderId, Long memberId, int usePoint);
	/**
	 * 增加特殊活动优惠
	 * @param order
	 * @param spdiscountFlag
	 * @return
	 */
	/**
	 * @param order
	 * @param sd
	 * @param jsonValue 
	 * @param jsonKey 
	 * @return
	 * @throws Exception
	 */
	ErrorCode<OrderContainer> useSpecialDiscount(Long orderid, SpecialDiscount sd, OrderCallback callback) throws OrderException;
	/**
	 * @param orderId
	 * @param card
	 * @param memberid
	 * @return TicketOrderContainer(order, opi, seatList, curDiscount, discountList)
	 */
	ErrorCode<TicketOrderContainer> useElecCard(Long orderId, ElecCard card, Long memberid);
	/**
	 * @param orderId
	 * @param card
	 * @param memberid
	 * @return TicketOrderContainer(order, opi, seatList, curDiscount, discountList)
	 */
	ErrorCode<TicketOrderContainer> useElecCardByTradeNo(String tradeno, ElecCard card, Long memberid);
	ErrorCode<String> randomSendPrize(Prize prize, WinnerInfo winner);
	/**
	 * 随机抽取一张券
	 * @param member
	 * @param spid
	 * @return
	 */
	ErrorCode<String> randomBindCard(Member member, Long spid);
	/**
	 * 生成动态码
	 * @param sd
	 * @param maxnum
	 * @return
	 */
	List<SpCode> genSpCode(SpecialDiscount sd, int maxnum);
	/**
	 * 获取动态码
	 * @param pass
	 * @return
	 */
	SpCode getSpCodeByPass(String pass);
	
	List<SpCode> getSpCodeList(Long memberid, Long spid, int fromnum, int maxnum);
	Integer getSpCodeCountByMemberid(Long memberid);
	/**
	 * 获取统计数量
	 * @param sdid
	 * @return
	 */
	Map getSpCodeCountStats(Long sdid);
	void exportSpCodePassBySd(SpecialDiscount sd, Writer writer, Long userid) throws IOException;
	
	/**
	 * 处理付款完成请求
	 * FIXME:临时使用，后期删除
	 * @param tmid
	 * @return
	 */
	ErrorCode<Member> processBaiduPaySuccess(TempMember tm, Long spid);
}
