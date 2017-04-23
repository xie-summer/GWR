package com.gewara.xmlbind.api;

public abstract class ApiSportConfig {
	public static final String KEY = "gewasport";
	public static final String PRIMARY_KEY = "prikey-gewara-sport";//校验Key
	public static final String SPORT_SYN_OPENTABLE = "/api/sport/synSportOpenTable.xhtml"; //运动场馆场次同步
	public static final String SPORT_SYN_ITEM = "/api/sport/synSportItem.xhtml";				//场馆项目同步
	public static final String SPORT_SYN_FIELD = "/api/sport/synSportField.xhtml";			//场地同步
}
