package com.gewara.web.action.partner;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.helper.discount.SportSpecialDiscountHelper;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.pay.PayValidHelper;
import com.gewara.util.JsonUtils;

public class OttSpdiscountFilter extends SpdiscountFilter {
	private OpenTimeTable ott;
	private Timestamp addtime;
	
	public OttSpdiscountFilter(OpenTimeTable ott, Timestamp addtime){
		this.ott = ott;
		this.addtime = addtime;
	}
	
	@Override
	public boolean excludeOpi(SpecialDiscount sd) {
		PayValidHelper pvh = new PayValidHelper(JsonUtils.readJsonToMap(ott.getOtherinfo()));
		SportSpecialDiscountHelper sdh = new SportSpecialDiscountHelper(null, ott,null, null);
		return (StringUtils.isNotBlank(ott.getSpflag()) && !StringUtils.contains(ott.getSpflag(), sd.getTag()))
				|| !sdh.isEnabled(sd, pvh).isSuccess() 
				|| !SportSpecialDiscountHelper.isEnabledByFromToTime(sd, addtime)
				|| StringUtils.isNotBlank(sdh.getOttFullDisabledReason(sd, ott, addtime));
	}

}
