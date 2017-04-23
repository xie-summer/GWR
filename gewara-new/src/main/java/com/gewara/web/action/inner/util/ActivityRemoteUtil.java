package com.gewara.web.action.inner.util;

import java.util.Properties;

public abstract class ActivityRemoteUtil {
	private static String activityDetailUrl;
	private static String activityCountUrl;
	private static String activityListUrl;
	
	private static String joinActivityUrl;
	private static String cancelActivityUrl;
	private static String addClickedUrl;
	private static String addCollectUrl;
	private static String groupByCountyUrl;
	private static String activityIdListUrl;
	private static String relateIdListUrl;
	private static String activityByTagUrl;
	private static String activityByMemberidUrl;
	private static String activityByStatusUrl;
	private static String joinCountByMemberidUrl;
	private static String joinListByMemberidUrl;
	private static String joinByRelatedidUrl;
	private static String joinByMemberidUrl;
	private static String joinByActivityIdUrl;
	private static String topActivityUrl;
	private static String joinCountByTagUrl;
	private static String acivityByMemberidUrl;
	private static String applyJoinByMemberidUrl;
	private static String activityCountByMemberidUrl;
	private static String activityListByFidListUrl;
	private static String activityCountByFidListUrl;
	private static String cancelCollectUrl;
	private static String memberCollUrl;
	
	private static String addActivityUrl;
	private static String treasureListUrl;
	
	private static String categoryCountUrl;
	private static String siteMapCountUrl;
	private static String joinCountUrl;
	
	private static String topAddMemberUrl;
	
	private static String commendActivityUrl;
	private static String hotActivityUrl;
	
	private static String activityUrlBySignname;
	
	private static String activityMpiUrl;
	
	private static String activityUrlByMembers;
	private static String memberOperActivityResult;
	
	private static String activityMpiGuestUrl;
	
	private static String activityOrderUpdateMoblie;
	
	private static String activityOrderReturn;
	
	private static String activityRelatedidByTagUrl;
	
	public static String applyJoinByMemberidsUrl = "/api/applyjoin/getByMemberIdsAndActivityId.xhtml";
	
	private static boolean initialized = false;
	
	public static synchronized void init(String propertyFile) {
		if (initialized)
			return;
		Properties props = new Properties();
		try {
			props.load(TicketRemoteUtil.class.getClassLoader().getResourceAsStream(propertyFile));
		} catch (Exception e) {
			throw new IllegalArgumentException("property File Error!!!!", e);
		}
		activityDetailUrl = props.getProperty("activityDetailUrl");
		activityListUrl = props.getProperty("activityListUrl");
		activityCountUrl = props.getProperty("activityCountUrl");
		
		joinActivityUrl = props.getProperty("joinActivityUrl");
		cancelActivityUrl = props.getProperty("cancelActivityUrl");
		addClickedUrl = props.getProperty("addClickedUrl");
		addCollectUrl = props.getProperty("addCollectUrl");
		cancelCollectUrl = props.getProperty("cancelCollectUrl");
		groupByCountyUrl = props.getProperty("groupByCountyUrl");
		activityIdListUrl = props.getProperty("activityIdListUrl");
		relateIdListUrl = props.getProperty("relateIdListUrl");
		activityByTagUrl = props.getProperty("activityByTagUrl");
		activityByMemberidUrl = props.getProperty("activityByMemberidUrl");
		activityByStatusUrl = props.getProperty("activityByStatusUrl");
		joinCountByMemberidUrl = props.getProperty("joinCountByMemberidUrl");
		joinListByMemberidUrl = props.getProperty("joinListByMemberidUrl");
		joinByRelatedidUrl = props.getProperty("joinByRelatedidUrl");
		joinByMemberidUrl = props.getProperty("joinByMemberidUrl");
		joinByActivityIdUrl = props.getProperty("joinByActivityIdUrl");
		topActivityUrl = props.getProperty("topActivityUrl");
		joinCountByTagUrl = props.getProperty("joinCountByTagUrl");
		acivityByMemberidUrl = props.getProperty("acivityByMemberidUrl");
		applyJoinByMemberidUrl = props.getProperty("applyJoinByMemberidUrl");
		activityCountByMemberidUrl = props.getProperty("activityCountByMemberidUrl");
		activityListByFidListUrl = props.getProperty("activityListByFidListUrl");
		activityCountByFidListUrl = props.getProperty("activityCountByFidListUrl");
		activityRelatedidByTagUrl = props.getProperty("activityRelatedidByTagUrl");
		
		treasureListUrl = props.getProperty("treasureListUrl");
		memberCollUrl = props.getProperty("memberCollUrl");
		addActivityUrl = props.getProperty("addActivityUrl");
		
		categoryCountUrl = props.getProperty("categoryCountUrl");
		siteMapCountUrl = props.getProperty("siteMapCountUrl");
		joinCountUrl = props.getProperty("joinCountUrl");
		
		topAddMemberUrl = props.getProperty("topAddMemberUrl");
		
		commendActivityUrl = props.getProperty("commendActivityUrl");
		hotActivityUrl = props.getProperty("hotActivityUrl");
		
		activityUrlBySignname = props.getProperty("activityUrlBySignname");
		activityMpiUrl = props.getProperty("activityMpiUrl");
		
		activityUrlByMembers = props.getProperty("activityUrlByMembers");
		
		memberOperActivityResult = props.getProperty("memberOperActivityResult");
		
		activityMpiGuestUrl = props.getProperty("activityMpiGuestUrl");
		
		activityOrderUpdateMoblie = props.getProperty("activityOrderUpdateMoblie");
		activityOrderReturn = props.getProperty("activityOrderReturn");
		
		initialized = true;
	}
	public static String getJoinActivityUrl() {
		return joinActivityUrl;
	}

	public static String getCancelActivityUrl() {
		return cancelActivityUrl;
	}

	public static String getAddClickedUrl() {
		return addClickedUrl;
	}

	public static String getAddCollectUrl() {
		return addCollectUrl;
	}

	public static String getGroupByCountyUrl() {
		return groupByCountyUrl;
	}

	public static String getActivityIdListUrl() {
		return activityIdListUrl;
	}

	public static String getRelateIdListUrl() {
		return relateIdListUrl;
	}

	public static String getActivityByTagUrl() {
		return activityByTagUrl;
	}

	public static String getActivityByMemberidUrl() {
		return activityByMemberidUrl;
	}

	public static String getActivityByStatusUrl() {
		return activityByStatusUrl;
	}

	public static String getJoinCountByMemberidUrl() {
		return joinCountByMemberidUrl;
	}

	public static String getJoinListByMemberidUrl() {
		return joinListByMemberidUrl;
	}

	public static String getJoinByRelatedidUrl() {
		return joinByRelatedidUrl;
	}

	public static String getJoinByMemberidUrl() {
		return joinByMemberidUrl;
	}

	public static String getJoinByActivityIdUrl() {
		return joinByActivityIdUrl;
	}

	public static String getTopActivityUrl() {
		return topActivityUrl;
	}

	public static String getJoinCountByTagUrl() {
		return joinCountByTagUrl;
	}

	public static String getAcivityByMemberidUrl() {
		return acivityByMemberidUrl;
	}

	public static String getApplyJoinByMemberidUrl() {
		return applyJoinByMemberidUrl;
	}

	public static String getActivityCountByMemberidUrl() {
		return activityCountByMemberidUrl;
	}

	public static String getActivityCountUrl() {
		return activityCountUrl;
	}

	public static String getActivityListUrl() {
		return activityListUrl;
	}

	public static String getActivityListByFidListUrl() {
		return activityListByFidListUrl;
	}

	public static String getActivityCountByFidListUrl() {
		return activityCountByFidListUrl;
	}

	public static String getCancelCollectUrl() {
		return cancelCollectUrl;
	}

	public static String getActivityDetailUrl() {
		return activityDetailUrl;
	}

	public static String getTreasureListUrl() {
		return treasureListUrl;
	}

	public static String getMemberCollUrl() {
		return memberCollUrl;
	}

	public static String getAddActivityUrl() {
		return addActivityUrl;
	}

	public static String getCategoryCountUrl() {
		return categoryCountUrl;
	}

	public static String getSiteMapCountUrl() {
		return siteMapCountUrl;
	}

	public static String getJoinCountUrl() {
		return joinCountUrl;
	}
	public static String getTopAddMemberUrl() {
		return topAddMemberUrl;
	}
	public static String getCommendActivityUrl() {
		return commendActivityUrl;
	}
	public static String getHotActivityUrl() {
		return hotActivityUrl;
	}
	public static String getActivityUrlBySignname() {
		return activityUrlBySignname;
	}
	public static String getActivityMpiUrl() {
		return activityMpiUrl;
	}
	public static String getActivityUrlByMembers() {
		return activityUrlByMembers;
	}
	public static String getMemberOperActivityResult() {
		return memberOperActivityResult;
	}
	public static String getActivityMpiGuestUrl() {
		return activityMpiGuestUrl;
	}
	public static String getActivityOrderUpdateMoblie() {
		return activityOrderUpdateMoblie;
	}
	public static String getActivityOrderReturn() {
		return activityOrderReturn;
	}
	public static String getActivityRelatedidByTagUrl() {
		return activityRelatedidByTagUrl;
	}
}
