package com.gewara.untrans;

/**
 * 暴库数据管理
 * 
 * @author zhaorq
 * 
 */
public interface BaoKuService {

	/**
	 * 验证邮件与密码是否存在危险
	 * 
	 * @param authenticode
	 * @return
	 */
	boolean isDanger(String email, String pwd);

	/**
	 * 导入暴库数据
	 * 
	 * @param fileName
	 * @return
	 */
	boolean loadBaoKuTxt(String fileName, boolean isLoad);
	
	/**
	 * 加载目录下的所有文件（大家不要使用）
	 * @param isLoad
	 */
	void loadBaoKuFiles(boolean isLoad);

	void scanLoadBaoKu();
	
	void scanMemberBaoKu();
	
	void loadGewaraBaoKu();

	/**
	 * 验证邮件与密码是否存在危险密码是md5的
	 * @param email
	 * @param pwd
	 * @return
	 */
	boolean isDangerMD5(String email, String pwdMD5);
}
