package com.gewara.service.member;

import java.util.Date;
import java.util.List;

public interface OpenMemberService {
	
	Integer getOpenMemberCountBySource(String source);
	
	Integer getTicketOrderMemberCount(Date fromDate, Date toDate, String source);

	List<String> getOpenMemberSourceList();

	Integer getOpenMemberBindEmailCount(Date fromDate, Date toDate, String source, String newTask);
}
