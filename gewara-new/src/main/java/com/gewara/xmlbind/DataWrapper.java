package com.gewara.xmlbind;

import com.gewara.bank.AliUserDetail;
import com.gewara.bank.AliUserToken;
import com.gewara.xmlbind.activity.CategoryCount;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.activity.RemoteApplyjoin;
import com.gewara.xmlbind.gym.BookingRecord;
import com.gewara.xmlbind.gym.CardItem;
import com.gewara.xmlbind.gym.RemoteCoach;
import com.gewara.xmlbind.gym.RemoteCourse;
import com.gewara.xmlbind.gym.RemoteGym;
import com.gewara.xmlbind.gym.RemoteSpecialCourse;
import com.gewara.xmlbind.sport.RemoteCardPayOrder;
import com.gewara.xmlbind.sport.RemoteMemberCardInfo;
import com.gewara.xmlbind.sport.RemoteMemberCardOrder;
import com.gewara.xmlbind.sport.RemoteMemberCardType;

public class DataWrapper extends BaseInnerResponse{
	private RemoteActivity activity;
	private RemoteApplyjoin applyjoin;
	private RemoteGym gym;					//健身场馆
	private RemoteCourse item;				//健身项目
	private RemoteSpecialCourse course;		//健身课程
	private RemoteCoach gymCoach;			//健身教练
	private BookingRecord bookingRecord;	//健身预约
	private CardItem card;					//健身卡
	private CategoryCount categoryCount;	//category统计
	private RemoteMemberCardOrder sportMemberCardOrder;
	private RemoteMemberCardInfo memberInfo;
	private RemoteCardPayOrder cardPayOrder;
	private RemoteMemberCardType memberCardType;
	private AliUserDetail aliUserDetail;
	private AliUserToken aliUserToken;
	public RemoteActivity getActivity() {
		return activity;
	}
	public void setActivity(RemoteActivity activity) {
		this.activity = activity;
	}
	public RemoteApplyjoin getApplyjoin() {
		return applyjoin;
	}
	public void setApplyjoin(RemoteApplyjoin applyjoin) {
		this.applyjoin = applyjoin;
	}
	
	public RemoteGym getGym() {
		return gym;
	}
	
	public void setGym(RemoteGym gym) {
		this.gym = gym;
	}
	
	public RemoteCourse getItem() {
		return item;
	}
	public void setItem(RemoteCourse item) {
		this.item = item;
	}
	public RemoteSpecialCourse getCourse() {
		return course;
	}
	public void setCourse(RemoteSpecialCourse course) {
		this.course = course;
	}
	public RemoteCoach getGymCoach() {
		return gymCoach;
	}
	
	public void setGymCoach(RemoteCoach gymCoach) {
		this.gymCoach = gymCoach;
	}
	public BookingRecord getBookingRecord() {
		return bookingRecord;
	}
	public void setBookingRecord(BookingRecord bookingRecord) {
		this.bookingRecord = bookingRecord;
	}
	public CardItem getCard() {
		return card;
	}
	
	public void setCard(CardItem card) {
		this.card = card;
	}
	public CategoryCount getCategoryCount() {
		return categoryCount;
	}
	public void setCategoryCount(CategoryCount categoryCount) {
		this.categoryCount = categoryCount;
	}
	public RemoteMemberCardOrder getSportMemberCardOrder() {
		return sportMemberCardOrder;
	}
	public void setSportMemberCardOrder(RemoteMemberCardOrder sportMemberCardOrder) {
		this.sportMemberCardOrder = sportMemberCardOrder;
	}
	public RemoteMemberCardInfo getMemberInfo() {
		return memberInfo;
	}
	public void setMemberInfo(RemoteMemberCardInfo memberInfo) {
		this.memberInfo = memberInfo;
	}
	public RemoteCardPayOrder getCardPayOrder() {
		return cardPayOrder;
	}
	public void setCardPayOrder(RemoteCardPayOrder cardPayOrder) {
		this.cardPayOrder = cardPayOrder;
	}
	public RemoteMemberCardType getMemberCardType() {
		return memberCardType;
	}
	public void setMemberCardType(RemoteMemberCardType memberCardType) {
		this.memberCardType = memberCardType;
	}
	public AliUserDetail getAliUserDetail() {
		return aliUserDetail;
	}
	public void setAliUserDetail(AliUserDetail aliUserDetail) {
		this.aliUserDetail = aliUserDetail;
	}
	public AliUserToken getAliUserToken() {
		return aliUserToken;
	}
	public void setAliUserToken(AliUserToken aliUserToken) {
		this.aliUserToken = aliUserToken;
	}
	
}
