package com.gewara.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.TheatreSeatArea;

public class UpdateDpiContainer {
	private List<DramaPlayItem> updateList = new ArrayList<DramaPlayItem>();
	private List<DramaPlayItem> insertList = new ArrayList<DramaPlayItem>();
	private List<Long> delList = new ArrayList<Long>();
	private Map<Long,List<TheatreSeatArea>> updateAreaMap = new HashMap<Long,List<TheatreSeatArea>>();
	public List<DramaPlayItem> getUpdateList() {
		return updateList;
	}
	public List<DramaPlayItem> getInsertList() {
		return insertList;
	}
	public List<Long> getDelList() {
		return delList;
	}
	public void addUpdate(DramaPlayItem dpi){
		this.updateList.add(dpi);
	}
	public void addInsert(DramaPlayItem dpi){
		this.insertList.add(dpi);
	}
	public void addDelete(Long dpid){
		this.delList.add(dpid);
	}
	public Map<Long, List<TheatreSeatArea>> getUpdateAreaMap() {
		return updateAreaMap;
	}
	
	public void addUpdateArea(Long dpid, TheatreSeatArea seatArea){
		List<TheatreSeatArea> areaList = updateAreaMap.get(dpid);
		if(areaList == null){
			areaList = new ArrayList<TheatreSeatArea>();
		}
		if(!areaList.contains(seatArea)){
			areaList.add(seatArea);
		}
	}
}
