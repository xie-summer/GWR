package com.gewara.untrans.ticket;

import com.gewara.model.ticket.OpenPlayItem;

public interface BaseOpenService {
	void asynchUpdateOpiStats(OpenPlayItem opi, boolean isFinished);
}
