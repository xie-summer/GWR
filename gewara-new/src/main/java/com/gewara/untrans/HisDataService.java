package com.gewara.untrans;

import java.util.List;
import java.util.Map;

import com.gewara.model.pay.SMSRecordBase;

public interface HisDataService {
	/**
	 * 备份积分明细，主键按（用户ID+月份）保存
	 * @return
	 */
	int backupPointHist();
	/**
	 * 积分按用户ID索引
	 * @return
	 */
	int createPointIndex();
	/**
	 * 备份短信，ID=手机号+时间
	 * @param recordid
	 * @return
	 */
	int backupSMSRecordHist();
	<T extends SMSRecordBase> int saveSMSRecordList(List<T> smsList);
	List<Map<String, String>> getHisSmsList(String mobile);
	int backupOrder();
}
