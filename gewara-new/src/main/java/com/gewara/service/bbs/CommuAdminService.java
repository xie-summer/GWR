package com.gewara.service.bbs;

import java.util.List;
import java.util.Map;

import com.gewara.model.bbs.commu.CommuManage;

public interface CommuAdminService {

	/**
	 * 根据圈子名称，圈主名称
	 */
	Map getCommuInfoList(Long commuid,String commname,String nickname,String status,int from, int maxnum);
	
	/**
	 * 更改状态
	 * @param commuid
	 * @param status
	 */
	void updateCommuStatus(Long commuid,String status);
	
	/**
	 * 根据圈子名称，圈主名称，圈子编号查询圈子数量
	 * @param commuid
	 * @param commname
	 * @param nickname
	 * @param status
	 * @return
	 */
	Integer getCommuInfoCount(Long commuid,String commname,String nickname,String status);
	
	/**
	 *  圈子认证申请 列表.
	 */
	List<CommuManage> getCommuManageListByStatus(String status, int from, int maxnum);
	Integer getCommuManageCount(String status);
}
