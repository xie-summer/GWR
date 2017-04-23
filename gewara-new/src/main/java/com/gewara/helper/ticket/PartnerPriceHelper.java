package com.gewara.helper.ticket;

import com.gewara.model.ticket.OpenPlayItem;

public class PartnerPriceHelper {
	public PartnerPriceHelper(){
	}
	public int getPrice(OpenPlayItem opi){
		return opi.getGewaprice();
	}
	
	/**
	 * 服务费
	 * @param opi
	 * @return
	 */
	public int getServiceFee(OpenPlayItem opi){
		//服务费=格瓦价格-成本价
		int fee=this.getPrice(opi)-opi.getCostprice();
		return fee<0?0:fee;
	}
}
