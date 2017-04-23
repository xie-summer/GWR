package com.gewara.untrans.activity;

/**
 * 天喔抽奖活动数据管理
 * @author zhaorq
 *
 */
public interface TenWowActivityService {

	/**
	 * 验证码是否可用
	 * @param authenticode
	 * @return
	 */
	boolean isAuthenticode(String authenticode);
	
	/**
	 * 使用验证码抽奖
	 * @param authenticode
	 * @param memberid
	 * @param memberCode
	 * @param memberName
	 * @return
	 */
	boolean useAuthenticode(String authenticode,String memberid,String memberCode,String memberName);
	
	/**
	 * 导入验证码数据
	 * @param fileName
	 * @return
	 */
	boolean loadTinWowTxt(String fileName,boolean isLoad);
}
