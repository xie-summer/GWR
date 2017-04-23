package com.gewara.xmlbind.sport;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class RemoteMemberCardOrderList extends BaseObjectListResponse<RemoteMemberCardOrder>{
	private List<RemoteMemberCardOrder> memberCardOrderList = new ArrayList<RemoteMemberCardOrder>();

	public List<RemoteMemberCardOrder> getMemberCardOrderList() {
		return memberCardOrderList;
	}

	public void setMemberCardOrderList(List<RemoteMemberCardOrder> memberCardOrderList) {
		this.memberCardOrderList = memberCardOrderList;
	}
	
	@Override
	public List<RemoteMemberCardOrder> getObjectList() {
		return memberCardOrderList;
	}
	public void addMemberCardOrder(RemoteMemberCardOrder memberCardOrder){
		this.memberCardOrderList.add(memberCardOrder);
	}
}
