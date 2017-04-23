package com.gewara.service.sport;
import com.gewara.model.acl.User;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.service.OrderException;

public interface InsteadSportOrderService {
	
	/**
	 * @param ott
	 * @param oldSportOrder
	 * @param fields
	 * @param user
	 * @return
	 * @throws OrderException
	 * 更改运动订单(场次)
	 */
	public SportOrder changeSportOrderByField(OpenTimeTable ott, SportOrder oldSportOrder, String fields, User user) throws OrderException;
	
//	public ErrorCode confirmSuccess(SportOrder order, OpenTimeTable ott, Long userid, boolean isAuto);
	
	
	/**
	 * @param ott
	 * @param oldSportOrder
	 * @param oti
	 * @param starttime
	 * @param quantity
	 * @param time
	 * @param user
	 * @return
	 * @throws OrderException
	 * 更改运动订单(时间)
	 */
	public SportOrder changeSportOrderByPeriod(OpenTimeTable ott, SportOrder oldSportOrder,
		OpenTimeItem oti, String starttime, Integer quantity, Integer time, User user) throws OrderException;


}
