package com.gewara.untrans.order.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Service;

import com.gewara.constant.PayConstant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.discount.SpecialDiscountHelper;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.pay.PayValidHelper;
import com.gewara.service.DaoService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.support.ErrorCode;
import com.gewara.util.VmUtils;
import com.gewara.web.action.partner.SpdiscountFilter;

@Service("spdiscountService")
public class SpdiscountService {
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	/**
	 * @param opi
	 * @param order
	 * @param discountList
	 * @return Map 
	 * 	adspdiscount: 广告连接
	 * 	opiOtherinfo: 场次其他信息
	 * 	maxSpdiscount：SpecialDiscount，当前可用最大优惠
	 * 	partnerDiscount: Discount，当前已使用的特价活动
	 * 	curSpdiscount：SpecialDiscount，与partnerDiscount配对出现
	 * 	pointDiscount：Discount，当前使用积分
	 * 	cardDiscountList：List<Discount>，当前使用券优惠
	 * 	cardMap：(discount.getId(), elecCard)，与cardDiscountList对应
	 *		discountList：所有折扣列表
	 *		discountAmountMap：Map(spdiscount.id, discountAmount 使用后的折扣金额)
	 * 	spdiscountList：可显示的所有折扣列表
	 *		disabledSdMap：Map(spdiscount.id, 不可用理由)
	 */
	public Map getSpecialDiscountData(SpecialDiscountHelper sdh, PayValidHelper valHelp, GewaOrder order, 
			boolean openSpdiscount, String spflags, List<Discount> discountList, String opentype, String applytag){
		Map model = new HashMap();
		Set<SpecialDiscount> spdiscountList = new HashSet<SpecialDiscount>();
		String spid = VmUtils.getJsonValueByKey(order.getOtherinfo(), OpiConstant.FROM_SPID);
		SpecialDiscount adspdiscount = null;
		if(StringUtils.isNotBlank(spid)){//来自广告渠道
			adspdiscount = daoService.getObject(SpecialDiscount.class, new Long(spid));
		}
		if(adspdiscount != null && sdh.isEnabled(adspdiscount, valHelp).isSuccess()){
			model.put("adspdiscount", adspdiscount);
			spdiscountList.add(adspdiscount);
		}else{
			if(StringUtils.isNotBlank(spflags)){//opi.getSpflag()
				String[] spflagList = spflags.split(",");
				for(String spflag: spflagList){
					SpecialDiscount sd = daoService.getObjectByUkey(SpecialDiscount.class, "flag", spflag, true);
					if(sd!=null && StringUtils.equals(SpecialDiscount.OPENTYPE_SPECIAL, sd.getOpentype())) spdiscountList.add(sd);
				}
			}
			if(openSpdiscount){//参与商家活动
				/*PayConstant.APPLY_TAG_MOVIE, SpecialDiscount.OPENTYPE_GEWA*/
				List<SpecialDiscount> sdList = null;
				if(StringUtils.equals(opentype, SpecialDiscount.OPENTYPE_PARTNER)){
					sdList = paymentService.getPartnerSpecialDiscountList(applytag, order.getPartnerid());
				}else if(StringUtils.equals(opentype, SpecialDiscount.OPENTYPE_WAP)){
					sdList = paymentService.getMobileSpecialDiscountList(applytag, order.getPartnerid());
				}else{
					sdList = paymentService.getSpecialDiscountList(applytag, opentype);
				}
				spdiscountList.addAll(sdList);
			}
		}
		List<SpecialDiscount> disableList = new ArrayList<SpecialDiscount>();
		SpecialDiscount maxSpdiscount = null;
		Integer maxdiscount = 0;
		Map<Long, String> disabledSdMap = new HashMap<Long, String>();
		Map<Long, SpecialDiscount> diabledMap = new HashMap<Long, SpecialDiscount>();
		Map<Long, Integer> discountAmountMap = new HashMap<Long, Integer>();
		if(spdiscountList.size()>0){
			for(SpecialDiscount sd: spdiscountList){
				if(sdh.isEnabled(sd, valHelp).isSuccess()){
					Spcounter spcounter = paymentService.getSpdiscountCounter(sd);
					ErrorCode<Integer> discount = paymentService.getSpdiscountAmount(sdh, order, sd, spcounter, valHelp);
					if(discount.isSuccess()) {
						if(discount.getRetval() > maxdiscount){
							maxdiscount = discount.getRetval();
							maxSpdiscount = sd;
						}
						discountAmountMap.put(sd.getId(), discount.getRetval());
					}else {
						disabledSdMap.put(sd.getId(), discount.getMsg());
						diabledMap.put(sd.getId(), sd);
					}
				}else{
					disableList.add(sd);
				}
			}
			spdiscountList.removeAll(disableList);
			if(maxSpdiscount!=null) model.put("maxSpdiscount", maxSpdiscount);
		}
		refreshCurDiscount(model, discountList, discountAmountMap);
		List<SpecialDiscount> disabledList = new ArrayList<SpecialDiscount>(diabledMap.values());
		if(model.containsKey("curSpdiscount")){
			spdiscountList.add((SpecialDiscount) model.get("curSpdiscount"));
		}
		List<SpecialDiscount> spList = new ArrayList<SpecialDiscount>(spdiscountList);
		spList.removeAll(disabledList);
		Collections.sort(spList, new PropertyComparator("sortnum", false, false));
		Collections.sort(disabledList, new PropertyComparator("sortnum", false, false));
		spList.addAll(disabledList);
		model.put("discountList", discountList);
		model.put("discountAmountMap", discountAmountMap);
		model.put("spdiscountList", spList);
		model.put("disabledSdMap", disabledSdMap);
		return model;
	}
	private void refreshCurDiscount(Map model, List<Discount> discountList, Map discountAmountMap){
		Map<Long, ElecCard> cardMap = new HashMap<Long, ElecCard>();
		if(discountList.size() > 0){
			List<Discount> cardDiscountList = new ArrayList<Discount>();
			for(Discount discount: discountList){
				if(StringUtils.equals(PayConstant.DISCOUNT_TAG_PARTNER, discount.getTag())){
					SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, discount.getRelatedid());
					discountAmountMap.put(sd.getId(), discount.getAmount());
					model.put("partnerDiscount", discount);
					model.put("curSpdiscount", sd);
				}else if(StringUtils.equals(PayConstant.DISCOUNT_TAG_POINT, discount.getTag())){
					model.put("pointDiscount", discount);
				}else{
					ElecCard elecCard = daoService.getObject(ElecCard.class, discount.getRelatedid());
					if(elecCard != null){
						cardMap.put(discount.getId(), elecCard);
						cardDiscountList.add(discount);
					}
				}
			}
			if(!cardDiscountList.isEmpty()) {
				model.put("cardDiscountList", cardDiscountList);
			}
			model.put("cardMap", cardMap);
		}
	}
	
	public List<SpecialDiscount> getSpecialDiscountData(SpdiscountFilter sdf, String opentype, String applytag){
		return getSpecialDiscountData(sdf, opentype, applytag, null);
	}
	
	public List<SpecialDiscount> getSpecialDiscountData(SpdiscountFilter sdf, String opentype, String applytag, ApiUser parnter){
		List<SpecialDiscount> spdiscountList = new ArrayList<SpecialDiscount>();
		if(parnter != null && !parnter.getId().equals(PartnerConstant.GEWA_SELF) && StringUtils.equals(opentype, SpecialDiscount.OPENTYPE_PARTNER)){
			spdiscountList = paymentService.getPartnerSpecialDiscountList(applytag, parnter.getId());
		}else if(!StringUtils.equals(opentype, SpecialDiscount.OPENTYPE_PARTNER)){
			spdiscountList = paymentService.getSpecialDiscountList(applytag, opentype);
		}
		sdf.applyFilter(spdiscountList);
		Collections.sort(spdiscountList, new PropertyComparator("sortnum", false, false));
		return spdiscountList;
	}
}
