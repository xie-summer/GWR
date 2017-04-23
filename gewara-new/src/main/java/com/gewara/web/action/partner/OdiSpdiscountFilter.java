package com.gewara.web.action.partner;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.helper.discount.DramaSpecialDiscountHelper;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.pay.PayValidHelper;
import com.gewara.util.JsonUtils;

public class OdiSpdiscountFilter extends SpdiscountFilter {
	
	private OpenDramaItem item;
	private Timestamp addtime;
	
	public OdiSpdiscountFilter(OpenDramaItem item, Timestamp addtime){
		this.item = item;
		this.addtime = addtime;
	}
	
	@Override
	public boolean excludeOpi(SpecialDiscount sd) {
		PayValidHelper pvh = new PayValidHelper(JsonUtils.readJsonToMap(item.getOtherinfo()));
		return (StringUtils.isNotBlank(item.getSpflag()) && !StringUtils.contains(item.getSpflag(), sd.getTag())) 
				|| !DramaSpecialDiscountHelper.isEnabled(sd, item, pvh).isSuccess() 
				|| StringUtils.isNotBlank(DramaSpecialDiscountHelper.getOdiFirstDisabledReason(sd, item, addtime));
	}

}
