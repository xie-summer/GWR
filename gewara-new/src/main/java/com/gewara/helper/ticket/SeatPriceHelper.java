/**
 * 
 */
package com.gewara.helper.ticket;

import java.util.Map;

import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.util.Assert;
import com.gewara.util.VmUtils;

/**
 * @author gebiao(ge.biao@gewara.com)
 * @since Feb 1, 2013 4:23:33 PM
 */
public class SeatPriceHelper {
	private OpenPlayItem opi;
	private Long partnerid;
	public SeatPriceHelper(OpenPlayItem opi, Long partnerid){
		Assert.notNull(partnerid);
		this.opi = opi;
		this.partnerid = partnerid;
	}
	public Integer getPrice(@SuppressWarnings("unused") OpenSeat oseat) {
		if(PartnerConstant.isMacBuy(partnerid)) {
			return opi.getPrice();
		}
		return opi.getGewaprice();
	}
	public static boolean isHasSeattype(OpenPlayItem opi){
		Map<String, String> map = VmUtils.readJsonToMap(opi.getOtherinfo());
		return map.containsKey(OpiConstant.SEATYPE);
	}
}
