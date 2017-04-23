package com.gewara.xmlbind.activity;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class RemoteApplyjoinList extends BaseObjectListResponse<RemoteApplyjoin>{
	private List<RemoteApplyjoin> applyjoinList = new ArrayList<RemoteApplyjoin>();
	
	public List<RemoteApplyjoin> getApplyjoinList() {
		return applyjoinList;
	}
	public void setApplyjoinList(List<RemoteApplyjoin> applyjoinList) {
		this.applyjoinList = applyjoinList;
	}
	
	public void addApplyjoin(RemoteApplyjoin applyjoin){
		this.applyjoinList.add(applyjoin);
	}
	@Override
	public List<RemoteApplyjoin> getObjectList() {
		return applyjoinList;
	}
}
