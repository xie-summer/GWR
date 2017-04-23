package com.gewara.xmlbind.bbs;

import java.util.ArrayList;
import java.util.List;

public class MemberCountList {
	
	private List<MemberCount> memberCountList = new ArrayList<MemberCount>();

	public List<MemberCount> getMemberCountList() {
		return memberCountList;
	}

	public void setMemberCountList(List<MemberCount> memberCountList) {
		this.memberCountList = memberCountList;
	}
	
	public void addMemberCount(MemberCount memberCount)
	{
		this.memberCountList.add(memberCount);
	}

}
