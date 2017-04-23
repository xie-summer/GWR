package com.gewara.web.action.inner.util;

import java.util.Properties;

public abstract class GymRemoteUtil {
	
	public static final String CACHE_KEY_GYMCARDITEM = "cache_key_gymcarditem_12";
	public static final String CACHE_KEY_BUYCARDITEM = "cache_key_buycarditem_30";
	public static final String CACHE_KEY_GYM = "cache_key_gym_24";
	public static final String CACHE_KEY_SPECIALCOURSE = "cache_key_specialcourse_10";
	public static final String CACHE_KEY_GYMCOURSE = "cache_key_gymcourse_19";
	public static final String CACHE_KEY_GYMCOACH = "cache_key_gymcoach_90";
	public static final String CACHE_KEY_BOOKINGRECORD = "cache_key_bookingrecord_80";
	
	private static String gymCardUrl;
	private static String showGymCardUrl;
	private static String gymUrl;
	private static String lockCardUrl;
	private static String gymIdListUrl;
	private static String gymListByCourseIdUrl;
	private static String gymCountByCourseIdUrl;
	private static String gymListByCoachIdUrl;
	private static String gymCountByCoachIdUrl;
	private static String gymListUrl;
	private static String gymCountUrl;
	private static String specialUrl;
	private static String specialIdListUrl;
	private static String specialListByGymIdUrl;
	private static String specialCountByGymIdUrl;
	private static String cardListByGymIdUrl;
	private static String cardCoutByGymIdUrl;
	private static String courseUrl;
	private static String courseIdListUrl;
	private static String courseListUrl;
	private static String courseCountUrl;
	private static String courseBySubIdUrl;
	private static String courseListByCoachIdUrl;
	private static String courseCountByCoachIdUrl;
	private static String coachUrl;
	private static String coachIdListUrl;
	private static String coachListByGymIdUrl;
	private static String coachCountByGymIdUrl;
	private static String bookingUrl;
	private static String bookingByUkeyUrl;
	private static String bookingListByGymIdUrl;
	private static String bookingCountByGymIdUrl;
	private static String updateCourseByPropertyUrl;
	private static String updateCoachByPropertyUrl;
	private static String updateGymByPropertyUrl;
	private static String groupGymByBrandUrl;
	
	private static boolean initialized = false;
	public static synchronized void init(String propertyFile){
		if(initialized) return;
		Properties props = new Properties();
		try {
			props.load(TicketRemoteUtil.class.getClassLoader().getResourceAsStream(propertyFile));
		} catch (Exception e) {
			throw new IllegalArgumentException("property File Error!!!!", e);
		}
		gymCardUrl = props.getProperty("gymCardUrl");
		showGymCardUrl = props.getProperty("showGymCardUrl");
		gymUrl = props.getProperty("gymUrl");
		lockCardUrl = props.getProperty("lockCardUrl");
		gymIdListUrl = props.getProperty("gymIdListUrl");
		gymListByCourseIdUrl = props.getProperty("gymListByCourseIdUrl");
		gymCountByCourseIdUrl = props.getProperty("gymCountByCourseIdUrl");
		gymListByCoachIdUrl = props.getProperty("gymListByCoachIdUrl");
		gymCountByCoachIdUrl = props.getProperty("gymCountByCoachIdUrl");
		gymListUrl = props.getProperty("gymListUrl");
		gymCountUrl = props.getProperty("gymCountUrl");
		specialUrl = props.getProperty("specialUrl");
		specialIdListUrl = props.getProperty("specialIdListUrl");
		specialListByGymIdUrl = props.getProperty("specialListByGymIdUrl");
		specialCountByGymIdUrl = props.getProperty("specialCountByGymIdUrl");
		cardListByGymIdUrl = props.getProperty("cardListByGymIdUrl");
		cardCoutByGymIdUrl = props.getProperty("cardCoutByGymIdUrl");
		courseUrl = props.getProperty("courseUrl");
		courseIdListUrl = props.getProperty("courseIdListUrl");
		courseListUrl = props.getProperty("courseListUrl");
		courseCountUrl = props.getProperty("courseCountUrl");
		courseBySubIdUrl = props.getProperty("courseBySubIdUrl");
		courseListByCoachIdUrl = props.getProperty("courseListByCoachIdUrl");
		courseCountByCoachIdUrl = props.getProperty("courseCountByCoachIdUrl");
		coachUrl = props.getProperty("coachUrl");
		coachIdListUrl = props.getProperty("coachIdListUrl");
		coachListByGymIdUrl = props.getProperty("coachListByGymIdUrl");
		coachCountByGymIdUrl = props.getProperty("coachCountByGymIdUrl");
		bookingUrl = props.getProperty("bookingUrl");
		bookingByUkeyUrl = props.getProperty("bookingByUkeyUrl");
		bookingListByGymIdUrl = props.getProperty("bookingListByGymIdUrl");
		bookingCountByGymIdUrl = props.getProperty("bookingCountByGymIdUrl");
		updateCourseByPropertyUrl = props.getProperty("updateCourseByPropertyUrl");
		updateCoachByPropertyUrl = props.getProperty("updateCoachByPropertyUrl");
		updateGymByPropertyUrl = props.getProperty("updateGymByPropertyUrl");
		groupGymByBrandUrl = props.getProperty("groupGymByBrandUrl");
		initialized = true;
	}

	public static String getGymCardUrl() {
		return gymCardUrl;
	}
	public static String getShowGymCardUrl() {
		return showGymCardUrl;
	}

	public static String getGymUrl() {
		return gymUrl;
	}

	public static String getLockCardUrl() {
		return lockCardUrl;
	}

	public static String getGymIdListUrl() {
		return gymIdListUrl;
	}

	public static String getGymListByCourseIdUrl() {
		return gymListByCourseIdUrl;
	}

	public static String getGymCountByCourseIdUrl() {
		return gymCountByCourseIdUrl;
	}

	public static String getGymListByCoachIdUrl() {
		return gymListByCoachIdUrl;
	}

	public static String getGymCountByCoachIdUrl() {
		return gymCountByCoachIdUrl;
	}

	public static String getGymListUrl() {
		return gymListUrl;
	}

	public static String getGymCountUrl() {
		return gymCountUrl;
	}
	
	public static String getSpecialUrl() {
		return specialUrl;
	}
	
	public static String getSpecialIdListUrl() {
		return specialIdListUrl;
	}

	public static String getSpecialListByGymIdUrl() {
		return specialListByGymIdUrl;
	}
	
	public static String getSpecialCountByGymIdUrl() {
		return specialCountByGymIdUrl;
	}

	public static String getCardListByGymIdUrl() {
		return cardListByGymIdUrl;
	}

	public static String getCardCoutByGymIdUrl() {
		return cardCoutByGymIdUrl;
	}

	public static String getCourseUrl() {
		return courseUrl;
	}
	
	public static String getCourseIdListUrl() {
		return courseIdListUrl;
	}

	public static String getCourseListUrl() {
		return courseListUrl;
	}

	public static String getCourseCountUrl() {
		return courseCountUrl;
	}
	
	public static String getCourseBySubIdUrl(){
		return courseBySubIdUrl;
	}
	
	public static String getCourseListByCoachIdUrl(){
		return courseListByCoachIdUrl;
	}
	
	public static String getCourseCountByCoachIdUrl(){
		return courseCountByCoachIdUrl;
	}

	public static String getCoachUrl() {
		return coachUrl;
	}

	public static String getCoachIdListUrl() {
		return coachIdListUrl;
	}

	public static String getCoachListByGymIdUrl() {
		return coachListByGymIdUrl;
	}

	public static String getCoachCountByGymIdUrl() {
		return coachCountByGymIdUrl;
	}

	public static String getBookingUrl() {
		return bookingUrl;
	}

	public static String getBookingByUkeyUrl() {
		return bookingByUkeyUrl;
	}

	public static String getBookingListByGymIdUrl() {
		return bookingListByGymIdUrl;
	}

	public static String getBookingCountByGymIdUrl() {
		return bookingCountByGymIdUrl;
	}

	public static String getUpdateCourseByPropertyUrl() {
		return updateCourseByPropertyUrl;
	}

	public static String getUpdateCoachByPropertyUrl() {
		return updateCoachByPropertyUrl;
	}
	
	public static String getUpdateGymByPropertyUrl() {
		return updateGymByPropertyUrl;
	}

	public static String getGroupGymByBrandUrl() {
		return groupGymByBrandUrl;
	}
	
}
