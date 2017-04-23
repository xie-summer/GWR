/**
 * 
 */
package com.gewara.untrans.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.cooperate.UnionPayFastCardbin;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.mongo.MongoService;
import com.gewara.pay.BackConstant;
import com.gewara.service.DaoService;
import com.gewara.service.OperationService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CooperateService;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;

@Service("cooperateService")
public class CooperateServiceImpl implements CooperateService {
	public static final String DISNEY_NEW = "N";
	public static final String DISNEY_UNUSE = "L";
	public static final String DISNEY_USE = "Y";
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	@Autowired@Qualifier("operationService")
	private OperationService operationService;

	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	private String[] SHBANKCODE = new String[]{"940021", "622892", "622985", "622468", "622987", "621243", "621050", "622278", "622279", "438600"};
	@Override
	public ErrorCode<String> checkShbankCode(Long orderid, String preCardno, String endCardno) {
		
		return this.checkBankCode(orderid, preCardno, endCardno, SHBANKCODE);
	}
	private String[] XYBANKCODE = new String[]{"90592", "966666","622909"};
	@Override
	public ErrorCode<String> checkXybankCode(Long orderid, String preCardno, String endCardno) {
		
		return this.checkBankCode(orderid, preCardno, endCardno, XYBANKCODE);
	}
	private String[] HXBANKCODE = new String[]{"539867", "528708","523959","528708","622636","622637","622638","625969"};
	@Override
	public ErrorCode<String> checkHxbankCode(Long orderid, String preCardno, String endCardno) {
		return this.checkHxBankCode(orderid, preCardno, endCardno, HXBANKCODE);
	}
	
	/**
	 * 验证卡bin
	 * 
	 * @param order
	 * @param paybank
	 * @param cardNumber
	 * @param spid
	 * @return
	 *
	 * @author leo.li
	 * Modify Time May 28, 2013 5:38:46 PM
	 */
	private ErrorCode<String> checkUnionPayFastCardBin(GewaOrder order,String paybank,String cardNumber,Long spid){
		if(StringUtils.isBlank(paybank)){
			paybank = "UNIONPAYFAST";
		}
		
		List<Map> qryMapList = mongoService.find(MongoData.NS_UNIONPAYFAST_CARDBIN + ":" + paybank, new HashMap());
		if(qryMapList == null || qryMapList.isEmpty()){
			return ErrorCode.SUCCESS;
		}
		for(Map prefixMap : qryMapList){
			if(StringUtils.startsWith(cardNumber,prefixMap.get("cardbin").toString())){
				Map<String, String> map = new HashMap<String, String>();
				map.put("orderid", order.getId()+"");
				map.put("cardno", cardNumber);
				map.put("memberid", order.getMemberid()+"");
				map.put("bankname", paybank);
				map.put("paid_success", "false");
				mongoService.saveOrUpdateMap(map, "orderid", MongoData.NS_PAY_CARDNUMBER);
				return ErrorCode.getSuccessReturn("");
			}
		}
		return ErrorCode.getFailure("很抱歉！您输入的卡号不在此次优惠中，请重新输入！");
	}
	
	@Override
	public ErrorCode<String> checkUnionPayFastCode(GewaOrder order,String paybank,String cardNumber,Long spid) {
		ErrorCode<String> result = checkUnionPayFastCardBin(order, paybank, cardNumber, spid);
		if(!result.isSuccess()) return result;
		
		String opkey = spid + ":"+ cardNumber;
		if(StringUtils.equals(paybank, "PABASFB")){
			boolean allow = operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 5);
			if(!allow){
				return ErrorCode.getFailure("很抱歉！此优惠活动每张银行卡一周内只能使用一次！");
			}
		}else if(StringUtils.equals(paybank, "BOC")){
			boolean allow = operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 7, OperationService.ONE_DAY * 30, 2);
			if(!allow){
				return ErrorCode.getFailure("很抱歉！此优惠活动每张银行卡一周内只能使用一次，且在一个月内最多只能使用两次！");
			}
		}
		return ErrorCode.getSuccessReturn("");
		
	}
	
	
	@Override
	public ErrorCode<String> checkUnionPayFastCodeForSZ(GewaOrder order,String paybank,String cardNumber,Long spid) {		
		ErrorCode<String> result = checkUnionPayFastCardBin(order, paybank, cardNumber, spid);
		if(!result.isSuccess()) return result;
		
		String opkey = spid + ":"+ cardNumber;
		if(StringUtils.equals(paybank, "BOC")){
			boolean allow = operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 6);
			if(!allow){
				return ErrorCode.getFailure("很抱歉！此优惠活动每张银行卡一周内只能使用一次！");
			}
		}
		return ErrorCode.getSuccessReturn("");		
	}
	
	@Override
	public ErrorCode<String> checkUnionPayFastCodeForNyyh(GewaOrder order,String paybank,String cardNumber,Long spid) {
		if(!StringUtils.equals(paybank, "ABC")){
			return ErrorCode.getFailure("很抱歉！此优惠活动只支持农业银行！");
		}
		ErrorCode<String> result = checkUnionPayFastCardBin(order, paybank, cardNumber, spid);
		if(!result.isSuccess()) return result;		
		String opkey = getOpkey(spid, cardNumber);
		boolean allow = operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 6);
		if(!allow){
			return ErrorCode.getFailure("很抱歉！此优惠活动每张银行卡一周内只能使用一次！");
		}
		return ErrorCode.getSuccessReturn("");
	}
	
	@Override
	public ErrorCode<String> checkUnionPayFastCodeForCqnsyh(GewaOrder order,String paybank,String cardNumber,Long spid) {
		if(!StringUtils.equals(paybank, "CQRCB")){
			return ErrorCode.getFailure("很抱歉！此优惠活动只支持重庆农商银行！");
		}
		ErrorCode<String> result = checkUnionPayFastCardBin(order, paybank, cardNumber, spid);
		if(!result.isSuccess()) return result;
		String opkey = getOpkey(spid, cardNumber);
		boolean allow = operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 6);
		if(!allow){
			return ErrorCode.getFailure("很抱歉！此优惠活动每张银行卡一周内只能使用一次！");
		}
		return ErrorCode.getSuccessReturn("");
	}
	
	@Override
	public ErrorCode<String> checkUnionPayFastCodeForYouJie(GewaOrder order,String paybank,String cardNumber,Long spid) {
		ErrorCode<String> result = checkUnionPayFastCardBin(order, paybank, cardNumber, spid);
		if(!result.isSuccess()) return result;
		String opkey = getOpkey(spid, cardNumber);
		
		//opkey = "spd" + spcounter.getId();
		//String opkey = "UNIONPAYFASTYOUJIE0608:"+ cardNumber;
		boolean allow = operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 6);
		if(!allow){
			return ErrorCode.getFailure("很抱歉！此优惠活动每张银行卡只能使用一次！");
		}
		return ErrorCode.getSuccessReturn("");
	}
	
	public ErrorCode<String> checkUnionPayFastCodeForWzcb(GewaOrder order,String paybank,String cardNumber,Long spid) {
		if(!StringUtils.equals(paybank, "WZCB")){
			return ErrorCode.getFailure("很抱歉！此优惠活动只支持温州银行！");
		}
		ErrorCode<String> result = checkUnionPayFastCardBin(order, paybank, cardNumber, spid);
		if(!result.isSuccess()) return result;
		String opkey = getOpkey(spid, cardNumber);
		boolean allow = operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 6);
		if(!allow){
			return ErrorCode.getFailure("很抱歉！此优惠活动每张银行卡一周内只能使用一次！");
		}
		return ErrorCode.getSuccessReturn("");
	}
	
	public ErrorCode<String> checkUnionPayFastCodeForZdcb(GewaOrder order,String paybank,String cardNumber,Long spid) {
		if(!StringUtils.equals(paybank, "ZDCB")){
			return ErrorCode.getFailure("很抱歉！此优惠活动只支持渣打银行！");
		}
		ErrorCode<String> result = checkUnionPayFastCardBin(order, paybank, cardNumber, spid);
		if(!result.isSuccess()) return result;

		String opkey = getOpkey(spid, cardNumber);
		boolean allow = operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 6, OperationService.ONE_DAY * 30, 2);
		if(!allow){
			return ErrorCode.getFailure("很抱歉！此优惠活动每张银行卡一周内只能使用一次，且在一个月内最多只能使用两次！");
		}
		return ErrorCode.getSuccessReturn("");
	}
	
	public ErrorCode<String> checkCommonCardbinOrCardNumLimit(GewaOrder order,Long spid,String cardNumber){
		SpecialDiscount specialDiscount = daoService.getObject(SpecialDiscount.class, spid);
		if(StringUtils.equals("true",specialDiscount.getCardNumUnique())){
			String opkey = getOpkey(spid, cardNumber);
			if(specialDiscount.getCardNumPeriodSpan() == null){
				boolean allow = operationService.isAllowOperation(opkey, OperationService.ONE_MINUTE * specialDiscount.getCardNumPeriodIntvel(), 
						specialDiscount.getCardNumLimitnum());
				if(!allow){
					return ErrorCode.getFailure("很抱歉！此优惠活动每张银行卡" + specialDiscount.getCardNumPeriodIntvel() + "分钟内只能使用" + 
							specialDiscount.getCardNumLimitnum() + "次！");	
				}
			}else{
				boolean allow = operationService.isAllowOperation(opkey,  OperationService.ONE_MINUTE * specialDiscount.getCardNumPeriodIntvel(), 
					OperationService.ONE_MINUTE * specialDiscount.getCardNumPeriodSpan(), specialDiscount.getCardNumLimitnum());
				if(!allow){
					return ErrorCode.getFailure("很抱歉！此优惠活动每张银行卡" + specialDiscount.getCardNumPeriodIntvel() + "分钟内只能使用一次，且在" + 
							specialDiscount.getCardNumPeriodSpan() + "内最多只能使用" + specialDiscount.getCardNumLimitnum() + "次！");
				}
			}
		}
		if(StringUtils.isNotBlank(specialDiscount.getCardbinUkey())){
			// 现在把卡bin配置都移到UnionPayFastCardbin集合中，不像以前一个cardbin配置对应mongo的一个集合，但为了兼容这部分代码得留着
			// 等到后期老的卡BIN号活动过期了，可以删除
			List<Map> qryMapList = mongoService.find(specialDiscount.getCardbinUkey(), new HashMap());
			// 如果从原来的集合没有取到卡BIN，则从现在的UnionPayFastCardbin这个集合中获取卡Bin号，并构造原先相同的数据结构
			if (CollectionUtils.isEmpty(qryMapList)) {
				UnionPayFastCardbin unionPayFastCardbin = mongoService.getObject(UnionPayFastCardbin.class, "cardbinUkey", specialDiscount.getCardbinUkey());
				if (unionPayFastCardbin != null && CollectionUtils.isNotEmpty(unionPayFastCardbin.getCardbinList())) {
					for (String cardbin : unionPayFastCardbin.getCardbinList()) {
						Map cardbinMap = new HashMap();
						cardbinMap.put("cardbin", cardbin);
						qryMapList.add(cardbinMap);
					}
				}
			}
			if(qryMapList == null || qryMapList.isEmpty()){
				return ErrorCode.SUCCESS;
			}
			for(Map prefixMap : qryMapList){
				String prefixbin = prefixMap.get("cardbin") == null ? "" : prefixMap.get("cardbin").toString();
				String suffixbin = prefixMap.get("suffixCardbin") == null ? "" : prefixMap.get("suffixCardbin").toString();
				if((StringUtils.isNotBlank(prefixbin) && StringUtils.startsWith(cardNumber,prefixbin) && StringUtils.isBlank(suffixbin))
						|| (StringUtils.isNotBlank(suffixbin) && StringUtils.endsWith(cardNumber,suffixbin) && StringUtils.isBlank(prefixbin))
						|| (StringUtils.isNotBlank(prefixbin) && StringUtils.startsWith(cardNumber,prefixbin) && 
								StringUtils.isNotBlank(suffixbin) && StringUtils.endsWith(cardNumber,suffixbin))){
					Map<String, String> map = new HashMap<String, String>();
					map.put("orderid", order.getId()+"");
					map.put("cardno", cardNumber);
					map.put("memberid", order.getMemberid()+"");
					map.put("bankname", order.getPaybank());
					map.put("paid_success", "false");
					mongoService.saveOrUpdateMap(map, "orderid", MongoData.NS_PAY_CARDNUMBER);
					return ErrorCode.getSuccessReturn("");
				}
			}
			return ErrorCode.getFailure("很抱歉！您输入的卡号不在此次优惠中，请重新输入！");
		}
		return ErrorCode.getSuccessReturn("");
	}
	
	private String getOpkey(Long spid,String cardNumber){
		SpecialDiscount specialDiscount = daoService.getObject(SpecialDiscount.class, spid);
		String opkey = spid + ":" + cardNumber;
		if(specialDiscount != null && StringUtils.isNotBlank(specialDiscount.getCardUkey())){
			opkey = "spd" + specialDiscount.getCardUkey() + ":" + cardNumber;
		}else if(specialDiscount != null && specialDiscount.getSpcounterid() != null){
			opkey = "spd" + specialDiscount.getSpcounterid() + ":" + cardNumber;
		}
		return opkey;
	}
	
	/**
	 * 江苏银联2.0快捷接口支付卡bin校验，包括使用次数
	 * 
	 * @param order
	 * @param cardNumber
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Mar 19, 2013 7:40:07 PM
	 */
	@Override
	public ErrorCode<String> checkUnionPayFastAJS(GewaOrder order,String cardNumber,Long spid) {
		String paybank = PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_JS;
		
		List<Map> qryMapList = mongoService.find(MongoData.NS_UNIONPAYFAST_CARDBIN + ":" + paybank, new HashMap());
		if(qryMapList == null || qryMapList.isEmpty()){
			return ErrorCode.SUCCESS;
		}
		//String opkey = PayUtil.PAYMETHOD_UNIONPAYFAST_ACTIVITY_JS + ":" + cardNumber;	
		for(Map prefixMap : qryMapList){
			if(StringUtils.startsWith(cardNumber,prefixMap.get("cardbin").toString())){
				Map<String, String> map = new HashMap<String, String>();
				map.put("orderid", order.getId()+"");
				map.put("cardno", cardNumber);
				map.put("memberid", order.getMemberid()+"");
				map.put("bankname", paybank);
				map.put("paid_success", "false");
				mongoService.saveOrUpdateMap(map, "orderid", MongoData.NS_PAY_CARDNUMBER);
				
				//江苏银联活动-2.0认证支付， 一个卡号，在一个活动内，一个月内只能用一次
				String opkey = spid + ":" + cardNumber;	
				boolean allow = operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 30);
				if(!allow){
					return ErrorCode.getFailure("很抱歉！此优惠活动每张银行卡一个月内只能使用一次！");
				}					
					
				return ErrorCode.getSuccessReturn("");
			}
		}
		return ErrorCode.getFailure("很抱歉！您输入的卡号不在此次优惠中，请重新输入！");
	}
	
	/**
	 * 北京银联2.0活动快捷接口支付卡bin校验，包括使用次数
	 * @param order
	 * @param cardNumber
	 * @return
	 */
	@Override
	public ErrorCode<String> checkUnionPayFastBJ(GewaOrder order,String cardNumber,Long spid) {
		String paybank = PaymethodConstant.PAYMETHOD_UNIONPAYFAST_ACTIVITY_BJ;
		List<Map> qryMapList = mongoService.find(MongoData.NS_UNIONPAYFAST_CARDBIN + ":" + paybank, new HashMap());
		if(qryMapList == null || qryMapList.isEmpty()){
			return ErrorCode.SUCCESS;
		}
		for(Map prefixMap : qryMapList){
			if(StringUtils.startsWith(cardNumber,prefixMap.get("cardbin").toString())){
				Map<String, String> map = new HashMap<String, String>();
				map.put("orderid", order.getId()+"");
				map.put("cardno", cardNumber);
				map.put("memberid", order.getMemberid()+"");
				map.put("bankname", paybank);
				map.put("paid_success", "false");
				mongoService.saveOrUpdateMap(map, "orderid", MongoData.NS_PAY_CARDNUMBER);
				//北京银联活动-2.0认证支付， 一个卡号，在一个活动内，一周内只能用一次
				String opkey = spid + ":" + cardNumber;	
				boolean allow = operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 6);
				if(!allow){
					return ErrorCode.getFailure("很抱歉！此优惠活动每张银行卡一周内只能使用一次！");
				}					
				return ErrorCode.getSuccessReturn("");
			}
		}
		return ErrorCode.getFailure("很抱歉！您输入的卡号不在此次优惠中，请重新输入！");
	}
	
	@Override
	public ErrorCode checkShbankBack(Long orderid, String otherinfo, String isSave) {
		Map<String, String> map = VmUtils.readJsonToMap(otherinfo);
		String backcardno = map.get(BackConstant.cardNumber);
		String precardno = map.get(BackConstant.shbankcardno);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("cardNumber", backcardno);
		params.put("paid_success", "true");
		Map fm = mongoService.findOne(MongoData.NS_PAY_CARDNUMBER, params);
		if(fm!=null) return ErrorCode.getFailure("error", "此卡已经有过优惠，不能再参加优惠！");
		if(StringUtils.isNotBlank(precardno) && StringUtils.isNotBlank(backcardno)){
			int len = backcardno.length();
			if(len<7) return ErrorCode.getFailure("上海银行卡, 卡号："+backcardno + "返回有错误！");
			String[] pres = precardno.split("~");
			if(pres.length<2) return ErrorCode.getFailure("上海银行卡, 卡号：" + backcardno + "加入有错误！");
			String pre1 = pres[0];
			String pre2 = pres[1];
			String back1 = StringUtils.substring(backcardno, 0, 6);
			String back2 = StringUtils.substring(backcardno, len-4, len);
			if(StringUtils.equals(pre1, back1) && StringUtils.equals(pre2, back2)){
				if(StringUtils.equalsIgnoreCase(isSave, "true")){
					Map<String, String> mg = new HashMap<String, String>();
					mg.put("orderid", orderid+"");
					mg.put("cardNumber", backcardno);
					mg.put("paid_success", "true");
					mongoService.saveOrUpdateMap(mg, "orderid", MongoData.NS_PAY_CARDNUMBER);
				}
				return ErrorCode.SUCCESS;
			}else {
				return ErrorCode.getFailure(backcardno + "不符合上海银行卡的规则：" + precardno);
			}
		}
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode checkXybankBack(Long orderid, String otherinfo, String isSave) {
		return this.checkbankBack(orderid, otherinfo, BackConstant.xybankcardno, isSave, "兴业银行");
	}

	private ErrorCode checkbankBack(Long orderid, String otherinfo,String key, String isSave,String bankName) {
		GewaOrder gewaOrder = daoService.getObject(GewaOrder.class, orderid);
		Map<String, String> map = VmUtils.readJsonToMap(otherinfo);
		String backcardno = map.get(BackConstant.cardNumber);
		String precardno = map.get(key);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("cardNumber", backcardno);
		params.put("paid_success", "true");
		Map fm = mongoService.findOne(MongoData.NS_PAY_CARDNUMBER, params);
		if(fm!=null) return ErrorCode.getFailure("error", "此卡已经有过优惠，不能再参加优惠！");
		if(StringUtils.isNotBlank(precardno) && StringUtils.isNotBlank(backcardno)){
			int len = backcardno.length();
			if(len<7) return ErrorCode.getFailure(bankName+"卡, 卡号："+backcardno + "返回有错误！");
			String[] pres = precardno.split("~");
			if(pres.length<2) return ErrorCode.getFailure(bankName+"卡, 卡号：" + backcardno + "加入有错误！");
			String pre1 = pres[0];
			String pre2 = pres[1];
			String back1 = StringUtils.substring(backcardno, 0, pre1.length());
			String back2 = StringUtils.substring(backcardno, len-4, len);
			if(StringUtils.equals(pre1, back1) && StringUtils.equals(pre2, back2)){
				if(StringUtils.equalsIgnoreCase(isSave, "true")){
					Map<String, String> mg = new HashMap<String, String>();
					mg.put("orderid", orderid+"");
					mg.put("memberid", gewaOrder.getMemberid()+"");
					mg.put("bankname", BackConstant.YXBACK);
					mg.put("cardNumber", backcardno);
					mg.put("paid_success", "true");
					mongoService.saveOrUpdateMap(mg, "orderid", MongoData.NS_PAY_CARDNUMBER);
				}
				return ErrorCode.SUCCESS;
			}else {
				return ErrorCode.getFailure(backcardno + "不符合"+bankName+"的规则：" + precardno);
			}
		}
		return ErrorCode.SUCCESS;
	
	}
	private ErrorCode<String> checkHxBankCode(Long orderid,String preCardno,String endCardno,String[] prefixs){
		for(String prefix : prefixs){
			if(StringUtils.startsWith(preCardno, prefix)){
				/**
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("cardno", preCardno + "~" + endCardno);
				params.put("paid_success", "true");
				Map fm = mongoService.findOne(MongoData.NS_PAY_CARDNUMBER, params);
				if(fm!=null) return ErrorCode.getFailure("此卡已经有过优惠，不能再参加优惠！");
				*/
				GewaOrder gewaOrder = daoService.getObject(GewaOrder.class, orderid);
				Map<String, String> map = new HashMap<String, String>();
				map.put("orderid", orderid+"");
				map.put("cardno", preCardno + "~" + endCardno);
				map.put("memberid", gewaOrder.getMemberid()+"");
				map.put("bankname", BackConstant.HXBACK);
				map.put("paid_success", "false");
				mongoService.saveOrUpdateMap(map, "orderid", MongoData.NS_PAY_CARDNUMBER);
				return ErrorCode.getSuccessReturn("");
			}
		}
		return ErrorCode.getFailure("你输入的信用卡卡号不在此次优惠范围！");
	}
	
	private ErrorCode<String> checkBankCode(Long orderid,String preCardno,String endCardno,String[] prefixs){
		for(String prefix : prefixs){
			if(StringUtils.startsWith(preCardno, prefix)){
				/**
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("cardno", preCardno + "~" + endCardno);
				params.put("paid_success", "true");
				Map fm = mongoService.findOne(MongoData.NS_PAY_CARDNUMBER, params);
				if(fm!=null) return ErrorCode.getFailure("此卡已经有过优惠，不能再参加优惠！");
				*/
				Map<String, String> map = new HashMap<String, String>();
				map.put("orderid", orderid+"");
				map.put("cardno", preCardno + "~" + endCardno);
				map.put("paid_success", "false");
				mongoService.saveOrUpdateMap(map, "orderid", MongoData.NS_PAY_CARDNUMBER);
				return ErrorCode.getSuccessReturn("");
			}
		}
		return ErrorCode.getFailure("你输入的借记卡卡号不在此次优惠范围！");
	}
	//深圳平安银行 一张卡一周只能使用一次
	@Override
	public ErrorCode<String> checkUnionPayFastShenZhenCodeForPingAn(GewaOrder order,String paybank,String cardNumber,Long spid) {
		if(!StringUtils.equals(paybank, "PAB")){
			return ErrorCode.getFailure("很抱歉！此优惠活动只支持平安银行！");
		}
		ErrorCode<String> result = checkUnionPayFastCardBin(order, "unionPayFast_activity_sz_PAB", cardNumber, spid);
		if(!result.isSuccess()) return result;
		String opkey = getOpkey(spid, cardNumber);
		boolean allow = operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 6);
		if(!allow){
			return ErrorCode.getFailure("很抱歉！此优惠活动每张银行卡一周内只能使用一次！");
		}
		return ErrorCode.getSuccessReturn("");
	}
		
	//深圳平安银行 一张卡一周只能使用一次
	@Override
	public ErrorCode<String> checkUnionPayFastGuangzhouCodeForBocByWeekone(GewaOrder order,String paybank,String cardNumber,Long spid) {
		if(!StringUtils.equals(paybank, "BOC")){
			return ErrorCode.getFailure("很抱歉！此优惠活动只支持平安银行！");
		}
		ErrorCode<String> result = checkUnionPayFastCardBin(order, "unionPayFast_activity_gz_BOCSH", cardNumber, spid);
		if(!result.isSuccess()) return result;
		String opkey = getOpkey(spid, cardNumber);
		boolean allow = operationService.isAllowOperation(opkey, OperationService.ONE_DAY * 6);
		if(!allow){
			return ErrorCode.getFailure("很抱歉！此优惠活动每张银行卡一周内只能使用一次！");
		}
		return ErrorCode.getSuccessReturn("");
	}
	
	//广州中国银行 一张卡一个月只能使用两次
	@Override
	public ErrorCode<String> checkUnionPayFastGuangzhouCodeForBocByMonthTwo(GewaOrder order,String paybank,String cardNumber,Long spid) {
		if(!StringUtils.equals(paybank, "BOC")){
			return ErrorCode.getFailure("很抱歉！此优惠活动只支持中国银行！");
		}
		ErrorCode<String> result = checkUnionPayFastCardBin(order, "unionPayFast_activity_gz_BOCSH", cardNumber, spid);
		if(!result.isSuccess()) return result;
		String opkey = getOpkey(spid, cardNumber);
		boolean allow = operationService.isAllowOperation(opkey+"_1", OperationService.ONE_DAY * 6);
		if(!allow){
			return ErrorCode.getFailure("很抱歉！此优惠活动每张银行卡一周内只能使用一次！");
		}
		allow = operationService.isAllowOperation(opkey+"_2", OperationService.ONE_DAY * 30, 2);
		if(!allow){
			return ErrorCode.getFailure("很抱歉！此优惠活动每张银行卡一个月内只能使用两次！");
		}
		return ErrorCode.getSuccessReturn("");
	}

	@Override
	public boolean addCardnumOperation(String tradeno, String otherinfo,
			Long spid) {
		Map<String, String> map = JsonUtils.readJsonToMap(otherinfo);
		if(map.containsKey("cardNumber")){
			String cardNumber = map.get("cardNumber");
			String opkey = getOpkey(spid, cardNumber);
			SpecialDiscount specialDiscount = daoService.getObject(SpecialDiscount.class, spid);
			boolean allow = false;
			if(specialDiscount.getCardNumPeriodSpan() != null){
				allow = operationService.updateOperation(opkey, OperationService.ONE_MINUTE * specialDiscount.getCardNumPeriodIntvel(), 
						OperationService.ONE_MINUTE * specialDiscount.getCardNumPeriodSpan(), specialDiscount.getCardNumLimitnum(), tradeno);
			}else{
				allow = operationService.updateOperation(opkey, OperationService.ONE_MINUTE * specialDiscount.getCardNumPeriodIntvel(), 
						specialDiscount.getCardNumLimitnum(), tradeno);
			}
			return allow; 
		}
		return false;
	}
}
