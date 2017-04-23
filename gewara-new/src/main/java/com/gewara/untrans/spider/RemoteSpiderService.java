package com.gewara.untrans.spider;

import java.sql.Timestamp;
import java.util.List;

import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.ticket.SynchPlayItem;

public interface RemoteSpiderService {

	ErrorCode<List<SynchPlayItem>> getRemotePlayItemListByUpdatetime(
			Timestamp updatetime, Long cinemaid);

}
