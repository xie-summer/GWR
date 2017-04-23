package com.gewara.web.action.partner;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.gewara.constant.PayConstant;
import com.gewara.helper.discount.SpecialDiscountHelper;
import com.gewara.model.drama.Drama;
import com.gewara.model.pay.SpecialDiscount;

public class DramaSpdiscountFilter extends ObjectSpdiscountFilter {

	public DramaSpdiscountFilter(Drama drama, Timestamp addtime) {
		this(drama.getCitycode(), PayConstant.APPLY_TAG_DRAMA, drama.getId(), addtime);
	} 
	
	private DramaSpdiscountFilter(String citycode, String tag, Long relatedid, Timestamp addtime) {
		super(citycode, tag, relatedid, addtime);
	}

	@Override
	public boolean excludeOpi(SpecialDiscount item) {
		boolean exclude = super.excludeOpi(item);
		if(exclude) return exclude;
		//Ãÿ ‚πÊ‘Ú
		Map<String,Object> ruleMap = new HashMap<String, Object>();
		ruleMap.put(SpecialDiscountHelper.SPECIAL_RULE_CATEGORY_KEY, this.relatedid);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("ruleObj", ruleMap);
		return !SpecialDiscountHelper.validSpecialcountRule(item, ruleMap, true).isSuccess();
	}

	
}
