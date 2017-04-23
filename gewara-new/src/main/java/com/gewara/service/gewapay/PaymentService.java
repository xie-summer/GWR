package com.gewara.service.gewapay;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.ModelMap;

import com.gewara.helper.discount.SpecialDiscountHelper;
import com.gewara.model.pay.AccountRecord;
import com.gewara.model.pay.Adjustment;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.CheckRecord;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.OrderRefund;
import com.gewara.model.pay.PayBank;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.user.Member;
import com.gewara.pay.PayValidHelper;
import com.gewara.service.OrderException;
import com.gewara.support.ErrorCode;

public interface PaymentService {
	String getOrderPayUrl2(GewaOrder order);

	String getChargePayUrl2(Charge charge);

	Map<String, String> getNetPayParams(GewaOrder order, String clientIp, String checkvalue);

	String getChargePayUrl(Charge charge, String clientIp);

	List<String> getPayserverMethodList();

	List<Charge> getChargeList(Long memberid, Timestamp from, Timestamp to, String type);

	List<Charge> getChargeListByMemberId(Long memberId, Timestamp startTime, Timestamp endTime, int from, int maxnum);

	List<Charge> getChargeListByMemberId(Long memberId, boolean ischarge, Timestamp startTime, Timestamp endTime,
			int from, int maxnum);

	List<Adjustment> getAdjustmentList(Long memberid, Timestamp timefrom, Timestamp timeto, String status);

	Integer getChargeCountByMemberId(Long memberId, boolean ischarge, Timestamp startTime, Timestamp endTime);

	List<GewaOrder> getGewaOrderListByMemberId(Long memberid);

	/**
	 * 网上充值：更新、同步用户的账户余额，保存订单，增加用户积分
	 * 
	 * @param tradeNo
	 * @param payseqno
	 *           外部交易号
	 * @param fee
	 *           支付金额
	 * @param paymethod
	 * @param paybank
	 * @return
	 */
	ErrorCode<Charge> bankPayCharge(String tradeNo, boolean bankcharge, String payseqno, int fee, String paymethod,
			String paybank,String gatewayCode,String merchantCode);

	/**
	 * 订单网上付款第一步：付款成功
	 * 
	 * @param tradeNo
	 * @param payseqno
	 *           外部交易号
	 * @param fee
	 * @param paymethod
	 * @param paybank
	 * @return
	 */
	ErrorCode<GewaOrder> netPayOrder(String tradeNo, String payseqno, int fee, String paymethod, String paybank,
			String from);

	ErrorCode<GewaOrder> netPayOrder(String tradeNo, String payseqno, int fee, String paymethod, String paybank,
			String from, Timestamp paidtime,String gatewayCode,String merchantCode);

	ErrorCode<GewaOrder> netPayOrder(String tradeNo, String payseqno, int fee, String paymethod, String paybank,
			String from, Timestamp paidtime, Map<String, String> otherMap,String gatewayCode,String merchantCode);

	void gewaPayOrder(GewaOrder order, Long memberId) throws OrderException;

	/**
	 * 找到上次结账记录
	 * 
	 * @return
	 */
	CheckRecord getLastCheckRecord();

	/**
	 * 所有结账单
	 * 
	 * @return
	 */
	List<CheckRecord> getCheckRecordList(int from, int maxnum);

	/**
	 * 结账第1步：清理余额变化用户
	 */
	ErrorCode<CheckRecord> closeAccountStep1(Long userid);
	/**
	 * 清理账户第2步：清理充值BillRecord, 调整BillRecord，用户余额试算平衡
	 * 
	 * @param record
	 */
	void closeAccountStep2(CheckRecord record);

	/**
	 * 根据支付时间查询支付过的订单
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	List<GewaOrder> getPaidOrderList(Long memberid, Timestamp timefrom, Timestamp timeto);

	List<GewaOrder> getPaidOrderList(Timestamp timefrom, Timestamp timeto, int from, int maxnum);

	List<AccountRecord> getAccountRecordList(Long checkid);
	/**
	 * 某用户未付款的订单
	 * 
	 * @param memberid
	 * @return
	 */
	<T extends GewaOrder> List<T> getUnpaidOrderList(Class<T> clazz, Long memberid,String ukey);

	/**
	 * 根据状态获取余额调整项目
	 * 
	 * @param statusNew
	 * @return
	 */
	List<Adjustment> getAdjustmentList(String status, int from, int maxrow);

	Integer getAdjustmentCount(String status);

	List<Adjustment> getAdjustmentListByMemberId(Long memberid, String status);

	/**
	 * 确认余额调整
	 * 
	 * @param adjustment
	 * @param user
	 * @return
	 */
	ErrorCode approveAdjustment(Adjustment adjustment, Long userid);

	ErrorCode approveAdjustment(Adjustment adjustment, MemberAccount account, Long userid);

	/**
	 * 建立一新账号
	 * 
	 * @param member
	 * @return
	 */
	MemberAccount createNewAccount(Member member);

	/**
	 * 根据tag取goods数量
	 * 
	 * @param tag
	 * @return
	 */
	Integer getGoodsCountByTag(String tag);

	Member getMemberByMobile(String mobile);

	/**
	 * 根据金额验证账户
	 * 
	 * @param memberid
	 * @param amount
	 * @return
	 */
	public ErrorCode validateAccount(Long memberid, Integer amount);

	public ErrorCode validateAccount(Long memberid, String password);

	public ErrorCode validateAccount(Long memberid, Integer amount, String password);

	/**
	 * 判断是否绑定支付方式
	 * 
	 * @param order
	 * @return
	 */
	ErrorCode isAllowChangePaymethod(GewaOrder order, String newpaymethod, String newpaybank);

	AccountRecord getAccountRecord(Long checkid, Long memberid);

	/**
	 * 查询当前所有的优惠信息
	 * 
	 * @param tag
	 * @param gewaprice
	 * @param time
	 * @param pricegap
	 * @return
	 */
	List<SpecialDiscount> getSpecialDiscountList(String tag, String opentype);

	List<SpecialDiscount> getPartnerSpecialDiscountList(String tag, Long partnerid);
	List<SpecialDiscount> getMobileSpecialDiscountList(String tag, Long partnerid);

	int getPaidOrderCount(Timestamp starttime, Timestamp endtime);

	ErrorCode addSpdiscountCharge(GewaOrder order, SpecialDiscount sd, Long userid, boolean isSupplement);

	/**
	 * 特价活动后期处理增加D券返利
	 * 
	 * @return
	 */
	ErrorCode addSpdiscountCard(GewaOrder order, SpecialDiscount sd, Long userid, boolean isSupplement);

	ErrorCode removeSpdiscountCharge(GewaOrder order, SpecialDiscount sd, Long userid);

	/**
	 * 验证是否有手续费等
	 * @param order
	 * @return
	 */
	ErrorCode validUse(GewaOrder order);

	/**
	 * 手机动态码修改支付密码
	 * 
	 * @param member
	 *           用户
	 * @param password
	 *           支付密码
	 * @param repassword
	 *           确认支付密码
	 * @param checkpass
	 *           动态码
	 * @param checkcount
	 *           验证次数
	 * @return map
	 */
	ErrorCode<Map> mobileResetAccountPass(Member member, String password, String repassword, String checkpass);

	/**
	 * @param order
	 * @param clientIp
	 * @return
	 */
	ErrorCode<Map<String, String>> getNetPayParamsV2(GewaOrder order, String clientIp, String version);

	Map<String, String> getNetChargeParamsV2(Charge charge, String clientIp, String version);

	/**
	 * 初始化限制的支付方式
	 * 
	 * @return
	 */
	List<String> getLimitPayList();

	void reInitLimitPayList();

	/**
	 * 绑定的支付方式
	 * 
	 * @param discountList
	 * @param orderOtherinfo
	 * @param order
	 * @return
	 */
	String getBindPay(List<Discount> discountList, Map<String, String> orderOtherinfo, GewaOrder order);

	ErrorCode<Integer> getSpdiscountAmount(SpecialDiscountHelper sdh, GewaOrder order, SpecialDiscount sd,
			Spcounter spcounter, PayValidHelper pvh);

	Spcounter getSpdiscountCounter(SpecialDiscount sd);

	Spcounter updateSpdiscountMemberCount(SpecialDiscount sd, GewaOrder order) throws OrderException;

	void updateSpdiscountPaidCount(SpecialDiscount sd, GewaOrder order);
	/**
	 * 下单名额增加
	 * @param sd
	 * @param spcounter
	 * @param order
	 */
	void updateSpdiscountAddCount(SpecialDiscount sd, Spcounter spcounter, GewaOrder order);
	ErrorCode restoreSdCounterBySpcounter(Long spcounterid);

	/**
	 * 抽奖活动为中奖会员加瓦币
	 * 
	 * @param drawActivityId
	 *           抽奖活动id
	 * @param member
	 *           会员
	 * @param totalfee
	 *           增加瓦币金额
	 * @return
	 */
	ErrorCode<?> addWaiBiByDrawActivity(String drawActivityId, Member member, Integer totalfee);

	/**
	 * 验证瓦币和账户金额支付
	 * 
	 * @param member
	 * @param account
	 * @param payPass
	 * @param wbpay
	 * @return
	 */
	ErrorCode validateWbPay(Member member, MemberAccount account, String payPass, String wbpay);

	/**
	 * 余额不足，进行充值
	 * 
	 * @param member
	 * @param account
	 * @param order
	 * @param chargeMethod
	 * @return
	 */
	Charge addChargeByOrder(Member member, MemberAccount account, GewaOrder order, String chargeMethod);

	/**
	 * 周期计数器复位
	 * @param spcounter
	 * @param userid
	 */
	void resetSpcounter(Spcounter spcounter, Long userid);

	ErrorCode usePayServer(String paymethod, Map<String, String> params, String clientIp, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws Exception;

	ErrorCode usePayServer(GewaOrder order, String clientIp, Map paramsData, String version, HttpServletRequest request, ModelMap model);
	ErrorCode usePayServer(Charge charge, String clientIp, Map paramsData, String version, HttpServletRequest request, ModelMap model);
	/**
	 * 订单调价退款
	 * 
	 * @param refund
	 * @param order
	 * @param userid
	 * @return
	 */
	ErrorCode refundSupplementOrder(OrderRefund refund, GewaOrder order, Long userid);

	/**
	 * 账户金额转化为瓦币
	 * 
	 * @param account
	 * @param bank
	 * @return 赠送积分
	 */
	ErrorCode<Integer> bankToWaBi(MemberAccount account, Integer bank);

	/**
	 * 获取银联列表
	 * 
	 * @param type
	 * @return
	 */
	List<PayBank> getPayBankList(String type);

	ErrorCode<Double> checkAddpoint(Charge charge);
	String getDecryptIdcard(String encryptIdCard);
	String getEncryptIdcard(String idCard);

	List<Charge> getChargeList(Long memberid, String status, String chargeto);
	
	List<MemberAccount> encryAccounts();
	
	
	Integer anlyEncryAccounts();
	
	Long encryIDCard(Long maxid);
	
	/**
	 * 根据特价活动ID查询计数器
	 * @param spids
	 * @return
	 */
	List<Spcounter> getSpcounterBySpids(List<Long> spids);

	List<Discount> getOrderDiscountList(GewaOrder order);

	String getGewaPayPrikey();

	ErrorCode<MemberAccount> createOrUpdateAccount(Member member, String realname, String password, String confirmPassword, String idcard);

	ErrorCode<MemberAccount> updateAccountPassword(Member member, String oldPassword, String password, String confirmPassword);
}