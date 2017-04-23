/**
 * 
 */
package com.gewara.service.member;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.gewara.model.acl.User;
import com.gewara.model.user.Jobs;
import com.gewara.model.user.JobsUp;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.MemberInfoMore;
import com.gewara.model.user.MemberUsefulAddress;
import com.gewara.model.user.OpenMember;
import com.gewara.model.user.TempMember;
import com.gewara.support.ErrorCode;

/**
 * @author hxs(ncng_2006@hotmail.com)
 * @since Jan 28, 2010 1:36:10 PM
 */
public interface MemberService {
	/**
	 * 根据昵称模糊查找
	 * @param nickname
	 * @return
	 */
	List<Member> searchMember(String nickname, int from, int maxnum);
	/**
	 * 根据昵称模糊查找白名单用户数
	 * @param nickname
	 * @return
	 */
	int searchMemberCount(String nickname);
	Member getMemberByNickname(String nickName);
	/**
	 * 判断是否有Emai、昵称相同的用户
	 * @param emailOrNicknameOrMobile
	 * @param memberId
	 * @return
	 */
	boolean isMemberExists(String emailOrNickname, Long memberId);
	/**
	 * 根据来源和及其登录名获取开放帐户
	 * @param source
	 * @param loginname
	 * @return
	 */
	OpenMember getOpenMemberByLoginname(String source, String loginname);
	/**
	 * 根据memberid获取关联开放账户
	 * @param source
	 * @param memberid
	 * @return
	 */
	OpenMember getOpenMemberByMemberid(String source, Long memberid);
	
	/**
	 *  MemberInfoMore - 根据用户ID, 查询匹配的教育经历/工作经历
	 */
	List<MemberInfoMore> getMemberinfoMoreList(Long memberid, String tag);
	OpenMember createOpenMember(String citycode, String source, String shortSource, String loginname, String ip);
	OpenMember createOpenMemberWithNickname(String citycode, String source, String shortSource, String loginname, String nickname, String ip);
	ErrorCode<Member> regMember(String nickname, String email, String password, Long inviteid, String invitetype, String regfrom, String citycode, String ip);
	ErrorCode<Member> regMemberWithMobile(String nickname, String mobile, String password, String checkpass, Long inviteid, String invitetype, String regfrom, String citycode, String ip);
	ErrorCode<Member> regMemberWithMobile(String nickname, String mobile, String password, Long inviteid, String invitetype, String regfrom, String citycode, String ip);
	ErrorCode<Member> createMemberWithBindMobile(String mobile, String checkpass, String citycode, String ip);
	ErrorCode<Member> createMemberWithDrawMobile(String mobile, String checkpass, String citycode, String ip);
	
	/**
	 * @function 群发站内信, 查询使用
	 * @author bob.hu
	 *	@date	2011-04-29 14:14:40
	 */
	List<MemberInfo> getMemberInfoByOtherInfo(String dkey);
	/**
	 * 新手任务
	 */
	MemberInfo saveNewTask(Long memberid, String newtask);
	/**
	 * @function 用户手机号唯一性检测
	 */
	boolean isMemberMobileExists(String mobile); 
	
	Integer getInviteCountByMemberid(Long memberid, Timestamp startTime, Timestamp endTime);
	/****
	 *  未读短信(包括以下两个, 后期优化查询)
	 * */
	Integer getMemberNotReadMessageCount(Long memberid);
	/****
	 *  未读短信(收件箱)
	 * */
	Integer getMemberNotReadNormalMessageCount(Long memberid);
	/****
	 *  未读短信(系统消息)
	 * */
	Integer getMemberNotReadSysMessageCount(Long memberid);
	/**
	 * @param memberidList
	 * @return Map(memerid, Map(id,nickname,headpic)
	 */
	Map<Long, Map> getCacheMemberInfoMap(Collection<Long> memberidList);
	Map<Long, String> getCacheHeadpicMap(Collection<Long> memberidList);
	Map getCacheMemberInfoMap(Long memberid);
	String getCacheHeadpicMap(Long memberid);

	/**
	 * 获取职位信息
	 * @param member
	 */
	Jobs getMemberPosition(Integer exp);

	/**
	 * 获取下一职位信息
	 * @param exp
	 * @return
	 */
	Jobs getMemberNextPosition(Integer exp);
	/**
	 * 增加经验值
	 * @param exp
	 * @return
	 */
	void addExpForMember(Long memberId, Integer exp);
	void saveJobUp(Long memberId, Long jobsId, String position);

	/**
	 * 升级之路查询
	 * @param memberId
	 * @return
	 */
	List<JobsUp> getJobsUpByMemberId(Long memberId);
	/**
	 * 通过email取用户
	 * @param email
	 * @return
	 */
	Member getMemberByEmail(String email);
	Map getMemberJobsInfo(Long memberid);
	ErrorCode<Member> doLoginByEmailOrMobile(String loginName, String plainPass);
	ErrorCode<String> getAndSetMemberEncode(Member member);
	Member getMemberByEncode(String memberEncode);
	void updateMemberByMemberEncode(String memberEncode, Member member);
	/**
	 * 获取用户常用地址
	 * @param memberid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<MemberUsefulAddress> getMemberUsefulAddressByMeberid(Long memberid, int from, int maxnum);
	
	/**
	 * 保存用户常用地址
	 */
	ErrorCode<MemberUsefulAddress> saveMemberUsefulAddress(Long id, Long memberid, String realname, String provincecode, String provincename,
			String citycode, String cityname, String countycode, String countyname, String address, String mobile, String postalcode, String IDcard);
	MemberInfo updateDanger(Long memberid);
	/**
	 * 绑定手机
	 * @param member
	 * @param mobile
	 * @param checkpass
	 * @param ip
	 * @return
	 */
	ErrorCode bindMobile(Member member, String mobile, String checkpass, String remoteIp);
	/**
	 * 后台用户帮助绑定手机
	 * @param member
	 * @param mobile
	 * @param checkpass
	 * @param user
	 * @param remoteIp
	 * @return
	 */
	ErrorCode bindMobile(Member member, String mobile, String checkpass, User user, String remoteIp);
	/**
	 * 更改手机绑定
	 * @param member
	 * @param newmobile
	 * @param checkpass
	 * @param remoteIp
	 * @return
	 */
	ErrorCode changeBind(Member member, String newmobile, String checkpass, String remoteIp);

	/**
	 * 解除手机绑定
	 * @param member
	 * @param dynamicCode
	 * @param remoteIp
	 * @return
	 */
	ErrorCode unbindMobile(Member member, String checkpass, String remoteIp);
	/**
	 * 管理员帮助乐队锁定
	 * @param memberid
	 * @param user
	 * @param remoteIp
	 * @return
	 */
	ErrorCode unbindMobileByAdmin(Long memberid, User user, String remoteIp);
	/**
	 * 第三方用户登录
	 * @param citycode
	 * @param source
	 * @param shortSource
	 * @param loginname
	 * @param nickname
	 * @param ip
	 * @param userInfo
	 * @return
	 */
	OpenMember createOpenMemberWithBaseInfo(String citycode, String source, String loginname,String ip,Map<String, Object> userInfo);
	/**
	 * 第三方账号绑定格瓦拉账号
	 * @param citycode
	 * @param source
	 * @param shortSource
	 * @param loginname
	 * @param ip
	 * @return
	 */
	OpenMember openMbrBindGewaMbr(String source, String loginname,String ip,Long memberid);
	/**
	 * 是不能解绑或更换手机号
	 * @param member
	 * @return
	 */
	boolean canChangeMobile(Member member);
	OpenMember createOpenMemberByMember(Member member, String source, String loginname);
	
	/**
	 * 第三方用户注册格瓦拉帐号
	 * @param userid 第三方用户唯一标识
	 * @param loginName 注册格瓦拉帐号的邮箱（手机）
	 * @param nickname 注册格瓦拉帐号用户昵称
	 * @param password 注册格瓦拉帐号的密码
	 * @param citycode 城市
	 * @param regForm 注册来源，如：WEB、WAP、IOS、android等
	 * @param source 第三方来源，如：微信等
	 * @param ip 注册格瓦拉帐号的IP地址
	 * @return ErrorCode<Member>
	 */
	public ErrorCode<Member> createMemberWithPartner(String userid, String loginName,  String nickname, String password, String citycode, 
			String regForm, String source, String ip);
	
	/**
	 * 根据手机号码查询用户
	 * @param mobile
	 * @return
	 */
	Member getMemberByMobile(String mobile);
	
	/**
	 * 为电话下单后台创建来电用户
	 * @param mobile
	 * @return
	 */
	ErrorCode<Member> createWithMobile(String mobile, User user);
	/**
	 * 用户不存在，手机号不存在，必须要输入密码
	 * @param mobile
	 * @param password
	 * @param flag
	 * @return
	 */
	ErrorCode<TempMember> createTempMember(String mobile, String password, String flag, String ip, Map<String, String> otherMap);
	/**
	 * 用户已注册，未绑定手机，临时生成绑定
	 * 1）已绑定手机用户:
	 *    a)未生成TempMember，则直接生成一个TempMember
	 *    b)已生成TempMember，直接使用原来的
	 * 2）未绑定手机用户：
	 * @param mobile
	 * @param memberid
	 * @param flag
	 * @return
	 */
	ErrorCode<TempMember> createTempMemberBind(Member member, String mobile, String flag, String ip);
	/**
	 * 从TempMember生成正式注册账号
	 * @param tm
	 * @return
	 */
	ErrorCode<Member> createMemberFromTempMember(TempMember tm);
	/**
	 * 绑定X手机状态
	 * @param tm
	 * @return
	 */
	ErrorCode<Member> bindMobileFromTempMember(TempMember tm);
}
