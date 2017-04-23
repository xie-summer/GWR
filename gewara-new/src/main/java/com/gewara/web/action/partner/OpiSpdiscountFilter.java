package com.gewara.web.action.partner;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.helper.discount.MovieSpecialDiscountHelper;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.pay.PayValidHelper;
import com.gewara.util.JsonUtils;

public class OpiSpdiscountFilter extends SpdiscountFilter {
	private OpenPlayItem opi;
	private Timestamp addtime;
	
	public OpiSpdiscountFilter(OpenPlayItem opi, Timestamp addtime){
		this.opi = opi;
		this.addtime = addtime;
	}
	
	@Override
	public boolean excludeOpi(SpecialDiscount sd) {
		PayValidHelper pvh = new PayValidHelper(JsonUtils.readJsonToMap(opi.getOtherinfo()));
		return (StringUtils.isNotBlank(opi.getSpflag()) && !StringUtils.contains(opi.getSpflag(), sd.getTag()))
				|| !MovieSpecialDiscountHelper.isEnabled(sd, opi, pvh).isSuccess() 
				|| !MovieSpecialDiscountHelper.isEnabledByFromToTime(sd, addtime)
				|| StringUtils.isNotBlank(MovieSpecialDiscountHelper.getOpiFirstDisabledReason(sd, opi, addtime));
	}

}
