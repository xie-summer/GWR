package com.gewara.xmlbind.gym;

import java.sql.Timestamp;

import com.gewara.xmlbind.BaseInnerResponse;

public class BookingRecord extends BaseInnerResponse {
	private Long id;
	private Long courseId;
	private Long memberId;
	private Long gymId;
	private String nickname;
	private String head;
	private Timestamp addtime;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getCourseId() {
		return courseId;
	}
	public void setCourseId(Long courseId) {
		this.courseId = courseId;
	}
	
	public Long getMemberId() {
		return memberId;
	}
	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}
	public Long getGymId() {
		return gymId;
	}
	public void setGymId(Long gymId) {
		this.gymId = gymId;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getHead() {
		return head;
	}
	public void setHead(String head) {
		this.head = head;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	
	
}
