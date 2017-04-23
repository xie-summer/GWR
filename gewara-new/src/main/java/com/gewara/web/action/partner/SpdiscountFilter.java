package com.gewara.web.action.partner;

import com.gewara.helper.sys.ObjectFilter;
import com.gewara.model.pay.SpecialDiscount;

public abstract class SpdiscountFilter extends ObjectFilter<SpecialDiscount> {
	
	@Override
	public boolean hasFilter() {
		return true;
	}
}
