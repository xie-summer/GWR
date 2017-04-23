package com.gewara.untrans.spider;

import java.util.List;

import com.gewara.helper.ticket.UpdateMpiContainer;

public interface SpiderOperationService {

	public void updateMoviePlayItem(UpdateMpiContainer container, Long cinemaid,List<String> msgList, int notUpdateWithMin);

}
