package com.gewara.xmlbind.gym;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class SpecialCourseListResponse extends BaseObjectListResponse<RemoteSpecialCourse> {
	
	private List<RemoteSpecialCourse> specialCourseList = new ArrayList<RemoteSpecialCourse>();

	public List<RemoteSpecialCourse> getSpecialCourseList() {
		return specialCourseList;
	}

	public void setSpecialCourseList(List<RemoteSpecialCourse> specialCourseList) {
		this.specialCourseList = specialCourseList;
	}
	
	public void addSpecialCourse(RemoteSpecialCourse specialCourse){
		this.specialCourseList.add(specialCourse);
	}

	@Override
	public List<RemoteSpecialCourse> getObjectList() {
		return specialCourseList;
	}
}
