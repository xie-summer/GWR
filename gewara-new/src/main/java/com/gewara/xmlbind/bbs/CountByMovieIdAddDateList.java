package com.gewara.xmlbind.bbs;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class CountByMovieIdAddDateList extends BaseObjectListResponse<CountByMovieIdAddDate> {
	private List<CountByMovieIdAddDate> countList = new ArrayList<CountByMovieIdAddDate>();
	@Override
	public List<CountByMovieIdAddDate> getObjectList() {
		return countList;
	}
	public List<CountByMovieIdAddDate> getCountList() {
		return countList;
	}
	public void setCountList(List<CountByMovieIdAddDate> countList) {
		this.countList = countList;
	}
	
	public void addDataCount(CountByMovieIdAddDate dataCount) {
		this.countList.add(dataCount);
	}

}
