package com.gewara.service.member;

import java.sql.Timestamp;
import java.util.List;

import com.gewara.command.InviteReport;

public interface MobileInviteStatisticsService {
	List<InviteReport> getInviteReportList(Timestamp time);
}
