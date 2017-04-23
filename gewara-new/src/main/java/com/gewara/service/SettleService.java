package com.gewara.service;

import java.util.List;

import com.gewara.model.drama.DramaSettle;
import com.gewara.model.pay.SettleConfig;
import com.gewara.model.sport.SportSettle;
import com.gewara.support.ErrorCode;

public interface SettleService extends BaseService {

	ErrorCode<SettleConfig> addSettleConfig(Long userid, Double discount, String distype);

	ErrorCode<DramaSettle> addDramaSettle(Long userid, Long dramaid, Double discount, String distype);
	
	List<SportSettle> getSportSettleList(Long sportid, Long itemid);
	
	ErrorCode<SportSettle> addSportSettle(Long userid, Long sportid, Long itemid, Double discount,  String distype, String remark);

}
