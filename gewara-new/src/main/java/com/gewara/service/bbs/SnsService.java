package com.gewara.service.bbs;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.model.bbs.BlackMember;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
/**
 * 后台用户管理
 * @author liushusong
 *
 */
public interface SnsService {
	/**
	 * 根据用户名称，用户编号，手机号查询用户信息
	 */
	List<Member> searchMember(Long memberid, String nickname,String mobile,String email, int from,int maxnum);
	/**
	 * 根据用户名称，签名，简介查询用户信息
	 */
	List<Map> getMemberListByUpdatetime(Timestamp starttime, Timestamp endtime, int from, int maxnum);
	/**
	 * 根据用户名称，签名，简介查询用户信息的总数
	 */
	Integer getMemberCountByUpdatetime(Timestamp starttime, Timestamp endtime);
	/**
	 * 修改用户密码,屏蔽用户登录
	 */
	void updateMemberPasswordAndDelete(Member m);
	
	/**
	 * 判断用户是否已被加入黑名单
	 */
	BlackMember isJoinBlackMember(Long memberid);
	
	/**
	 * 根据用户名称，用户编号，手机号查询用户数量
	 */
	Integer searchMemberCount(Long memberid,String nickname,String mobile,String email);
	
	/**
	 * 查询全部经验值
	 */
	Integer getSumExpValue(Integer startExp,Integer endExp);
	/**
	 * 根据时间段，经验值段查询用户经验值信息
	 */
	List<MemberInfo> getMemberExpValueList(Integer startExp,Integer endExp,int from,int maxnum);
	Integer getMemberExpValueCount(Integer startExp,Integer endExp);
}
