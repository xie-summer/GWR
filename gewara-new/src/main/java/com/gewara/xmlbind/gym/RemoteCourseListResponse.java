package com.gewara.xmlbind.gym;

import java.util.ArrayList;
import java.util.List;

import com.gewara.xmlbind.BaseObjectListResponse;

public class RemoteCourseListResponse extends BaseObjectListResponse<RemoteCourse>{
	private List<RemoteCourse> courseList = new ArrayList<RemoteCourse>();

	public List<RemoteCourse> getCourseList() {
		return courseList;
	}

	public void setCourseList(List<RemoteCourse> courseList) {
		this.courseList = courseList;
	}
	
	public void addCourse(RemoteCourse course){
		this.courseList.add(course);
	}

	@Override
	public List<RemoteCourse> getObjectList() {
		return courseList;
	}
}
