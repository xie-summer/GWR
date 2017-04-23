package com.gewara.xmlbind.sport;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class RemoteMemberCardInfoList extends BaseObjectListResponse<RemoteMemberCardInfo>{
	private List<RemoteMemberCardInfo> memberCardInfoList = new ArrayList<RemoteMemberCardInfo>();
	@Override
	public List<RemoteMemberCardInfo> getObjectList() {
		return getMemberCardInfoList();
	}
	
	public void addMemberCardInfo(RemoteMemberCardInfo memberCardInfo){
		this.memberCardInfoList.add(memberCardInfo);
	}

	public List<RemoteMemberCardInfo> getMemberCardInfoList() {
		return memberCardInfoList;
	}

	public void setMemberCardInfoList(List<RemoteMemberCardInfo> memberCardInfoList) {
		this.memberCardInfoList = memberCardInfoList;
	}
}
