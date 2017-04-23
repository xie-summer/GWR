package com.gewara.helper.order;

import com.gewara.model.pay.ElecCard;

public interface ElecCardFilter {
	boolean available(ElecCard card);
}
