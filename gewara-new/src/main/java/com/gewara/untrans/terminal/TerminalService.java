package com.gewara.untrans.terminal;

import java.util.List;

import com.gewara.model.pay.GewaOrder;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.terminal.TakeInfo;



public interface TerminalService {
	ErrorCode<List<TakeInfo>> getTakeInfoList(String tradenos);

	TakeInfo getTakeInfo(GewaOrder order);

	TakeInfo getTakeInfoByTradeno(String tradeno);
}
