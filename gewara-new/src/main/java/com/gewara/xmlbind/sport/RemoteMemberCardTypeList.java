package com.gewara.xmlbind.sport;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class RemoteMemberCardTypeList extends BaseObjectListResponse<RemoteMemberCardType>{
	private List<RemoteMemberCardType> memberCardTypeList = new ArrayList<RemoteMemberCardType>();
	@Override
	public List<RemoteMemberCardType> getObjectList() {
		return getMemberCardTypeList();
	}
	public List<RemoteMemberCardType> getMemberCardTypeList() {
		return memberCardTypeList;
	}
	public void setMemberCardTypeList(List<RemoteMemberCardType> memberCardTypeList) {
		this.memberCardTypeList = memberCardTypeList;
	}
	public void addMemberCardType(RemoteMemberCardType memberCardType){
		this.memberCardTypeList.add(memberCardType);
	}
}
