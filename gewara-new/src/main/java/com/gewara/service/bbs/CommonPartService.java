package com.gewara.service.bbs;

import java.util.Date;

import com.gewara.model.user.Festival;

/**
 * 共用方法
 * @author 
 *
 */
public interface CommonPartService {

	/**
	 * 获取节日信息
	 */
	Festival getCurFestival(Date date);
	Festival getNextFestival(Date date);
}
