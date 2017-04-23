/**
 * 
 */
package com.gewara.web.action.admin.report;

import java.sql.Timestamp;

import org.springframework.ui.ModelMap;

import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;

/**
 * @author Administrator
 *
 */
public class CountController extends BaseAdminController{
	public static final int MAX_DAY = 31;
	protected boolean isInvalidTime(Timestamp starttime, Timestamp endtime){
		if(DateUtil.addDay(starttime, MAX_DAY).before(endtime)) return true;
		return false;
	}
	protected String forwardErrorTime(ModelMap model){
		return forwardMessage(model, "开始日期和结束日期相隔不能超过:" + MAX_DAY + "天");
	}
}
