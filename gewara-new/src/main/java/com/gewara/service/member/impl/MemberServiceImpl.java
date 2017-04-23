/**
 * 
 */
package com.gewara.service.member.impl;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.BindConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.PointConstant;
import com.gewara.constant.Status;
import com.gewara.constant.order.AddressConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.MemberStats;
import com.gewara.model.acl.User;
import com.gewara.model.bbs.BlackMember;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.user.Jobs;
import com.gewara.model.user.JobsUp;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.MemberInfoMore;
import com.gewara.model.user.MemberUsefulAddress;
import com.gewara.model.user.OpenMember;
import com.gewara.model.user.TempMember;
import com.gewara.model.user.Treasure;
import com.gewara.mongo.MongoService;
import com.gewara.service.JsonDataService;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.member.BindMobileService;
import com.gewara.service.member.MemberService;
import com.gewara.service.member.PointService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.MemberCountService;
import com.gewara.untrans.WalaApiService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;

@Service("memberService")
public class MemberServiceImpl extends BaseServiceImpl implements MemberService {
	@Autowired@Qualifier("memberCountService")
	private MemberCountService memberCountService;
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	@Autowired@Qualifier("jsonDataService")
	private JsonDataService jsonDataService;
	public void setJsonDataService(JsonDataService jsonDataService) {
		this.jsonDataService = jsonDataService;
	}
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	@Autowired@Qualifier("pointService")
	private PointService pointService;
	public void setpointService(PointService pointService) {
		this.pointService = pointService;
	}
	@Autowired@Qualifier("bindMobileService")
	private BindMobileService bindMobileService;
	public void setBindMobileService(BindMobileService bindMobileService){
		this.bindMobileService = bindMobileService;
	}
	@Autowired@Qualifier("walaApiService")
	private WalaApiService walaApiService;
	public void setWalaApiService(WalaApiService walaApiService){
		this.walaApiService = walaApiService;
	}
	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;
	public void setElecCardService(ElecCardService elecCardService) {
		this.elecCardService = elecCardService;
	}
	@Override
	public List<Member> searchMember(String nickname, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Member.class, "m");
		query.add(Restrictions.like("nickname", nickname, MatchMode.ANYWHERE));

		DetachedCriteria subquery = DetachedCriteria.forClass(BlackMember.class, "bl");
		subquery.setProjection(Projections.property("bl.memberId"));
		subquery.add(Restrictions.eqProperty("bl.memberId", "m.id"));
		query.add(Subqueries.notExists(subquery));
		query.addOrder(Order.desc("m.id"));

		List result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	
	@Override
	public int searchMemberCount(String nickname){
		DetachedCriteria query = DetachedCriteria.forClass(Member.class, "m");
		query.add(Restrictions.like("nickname", nickname, MatchMode.ANYWHERE));
		
		DetachedCriteria subquery = DetachedCriteria.forClass(BlackMember.class, "bl");
		subquery.setProjection(Projections.property("bl.memberId"));
		subquery.add(Restrictions.eqProperty("bl.memberId", "m.id"));
		query.add(Subqueries.notExists(subquery));
		query.setProjection(Projections.rowCount());
		
		List result = readOnlyTemplate.findByCriteria(query);
		return new Integer(result.get(0) + "");
	}

	@Override
	public boolean isMemberMobileExists(String mobile){
		DetachedCriteria query = DetachedCriteria.forClass(Member.class);
		query.add(Restrictions.eq("mobile", mobile));
		List list = readOnlyTemplate.findByCriteria(query, 0, 1);
		if(list.isEmpty()) return false;
		return true;
	}
	
	@Override
	public boolean isMemberExists(String emailOrNickname, Long memberId){
		DetachedCriteria query = DetachedCriteria.forClass(Member.class);
		if(memberId != null) query.add(Restrictions.ne("id", memberId));
		query.add(Restrictions.or(
				Restrictions.eq("email", emailOrNickname),
				Restrictions.eq("nickname", emailOrNickname)));
		query.setProjection(Projections.property("id"));
		List list = readOnlyTemplate.findByCriteria(query, 0, 1);
		if(list.isEmpty()) return false;
		return true;
	}
	@Override
	public Member getMemberByNickname(String nickname) {
		return baseDao.getObjectByUkey(Member.class, "nickname", nickname, true);
	}
	@Value("${member.checkPass}")
	private String checkPass = null;
	@Override
	public ErrorCode<String> getAndSetMemberEncode(Member member) {
		if(!member.isEnabled()){
			return ErrorCode.getFailure("帐户被禁用，请联系客服！");
		}
		String time = DateUtil.format(DateUtils.addMonths(new Date(System.currentTimeMillis()), 1), "yyyyMM");
		String pass = StringUtil.getRandomString(3) + StringUtil.md5(member.getPassword() + checkPass).substring(8, 16);
		
		String memberEncode = pass + member.getId() + time;
		memberEncode = PKCoderUtil.encryptString(memberEncode, checkPass);
		memberEncode += "@" + StringUtil.md5(memberEncode + checkPass).substring(0, 8);
		cacheService.set(CacheConstant.REGION_API_LOGINAUTH, "API" + memberEncode, member);
		return ErrorCode.getSuccessReturn(memberEncode);
	}
	@Override
	public Member getMemberByEncode(String memberEncode) {
		if(StringUtils.isBlank(memberEncode)) return null;
		Member member = (Member)cacheService.get(CacheConstant.REGION_API_LOGINAUTH, "API" + memberEncode);
		if(member!=null){
			//TODO:被禁用的用户怎么办？
			return member;
		}
		String[] pair = memberEncode.split("@");
		try{
			if(pair.length > 1){
				if(StringUtil.md5(pair[0] + checkPass).substring(0, 8).equalsIgnoreCase(pair[1])){
					String data = PKCoderUtil.decryptString(pair[0], checkPass);
					Long valtime = new Long(StringUtils.substring(data, data.length() - 6));
					Long newtime = new Long(DateUtil.format(new Date(System.currentTimeMillis()), "yyyyMM"));
					if(newtime <= valtime) {//有效期内
						Long memberid = new Long(data.substring(11, data.length() - 6));
						String pass = StringUtils.substring(data, 3, 11);
						member = baseDao.getObject(Member.class, new Long(memberid));
						if(!member.isEnabled()){
							return null;
						}
						String mypass = StringUtil.md5(member.getPassword() + checkPass).substring(8, 16);
						if(StringUtils.equals(pass, mypass)){
							cacheService.set(CacheConstant.REGION_API_LOGINAUTH, "API" + memberEncode, member);
							//验证合法
							return member;
						}else{
							dbLogger.warn("memberEncode用户登录错误：" + memberid);
						}
					}
				}
			}
		}catch(Exception e){
			dbLogger.warn("memberEncode用户登录错误：", e);
		}
		return null;
	}
	@Override
	public void updateMemberByMemberEncode(String memberEncode, Member member) {
		Member old = getMemberByEncode(memberEncode);
		if(old==null) return;
		old.setMobile(member.getMobile());
		old.setBindStatus(member.getBindStatus());
		old.setPassword(member.getPassword());
		old.setEmail(member.getEmail());
		old.setNickname(member.getNickname());
		cacheService.set(CacheConstant.REGION_API_LOGINAUTH, "API" + memberEncode, member);
	}

	@Override
	public OpenMember getOpenMemberByLoginname(String source, String loginname) {
		DetachedCriteria query = DetachedCriteria.forClass(OpenMember.class);
		query.add(Restrictions.eq("source",source));
		query.add(Restrictions.eq("loginname", loginname));
		List<OpenMember> result = hibernateTemplate.findByCriteria(query);
		if(!result.isEmpty()) return result.get(0);
		return null;
	}
	@Override
	public OpenMember getOpenMemberByMemberid(String source, Long memberid) {
		DetachedCriteria query = DetachedCriteria.forClass(OpenMember.class);
		query.add(Restrictions.eq("source",source));
		query.add(Restrictions.eq("memberid", memberid));
		List<OpenMember> result = hibernateTemplate.findByCriteria(query);
		if(result.size() > 0) return result.get(0);
		return null;
	}
	
	@Override
	public List<MemberInfoMore> getMemberinfoMoreList(Long memberid, String tag){
		DetachedCriteria query = DetachedCriteria.forClass(MemberInfoMore.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("tag", tag));
		return readOnlyTemplate.findByCriteria(query);
	}
	@Override
	public OpenMember createOpenMember(String citycode, String source, String shortSource, String loginname, String ip) {
		String nickname = StringUtil.getRandomString(10);
		return createOpenMemberWithNickname(citycode, source, shortSource, loginname, nickname, ip);
	}
		
	@Override
	public OpenMember createOpenMemberWithNickname(String citycode, String source, String shortSource, String loginname, String shortNickname, String ip) {
		Member member = new Member("");
		String email = loginname;
		if(!ValidateUtil.isEmail(email)) {
			email = StringUtil.getRandomString(15) + "-open@" + source + ".com";
			Member exists = getMemberByEmail(email);
			if(exists!=null){
				//不可能两次都一样吧？？？
				email = StringUtil.getRandomString(15) + "-open@" + source + ".com";
			}
		}
		member.setPassword(StringUtil.md5(StringUtil.getRandomString(10)));
		member.setEmail(email);
		String nickname = shortSource + shortNickname;
		member.setNickname(nickname);
		baseDao.saveObject(member);
		
		MemberInfo memberInfo = new MemberInfo(member.getId(), nickname);
		memberInfo.setSource(MemberConstant.REGISTER_APP);
		memberInfo.setFromcity(citycode);
		Map<String, String> otherInfo = VmUtils.readJsonToMap(memberInfo.getOtherinfo());
		otherInfo.put(MemberConstant.OPENMEMBER, source);
		otherInfo.put(MemberConstant.TAG_SOURCE, "fail");

		memberInfo.setOtherinfo(JsonUtils.writeObjectToJson(otherInfo));
		memberInfo.setInvitetype("self");
		memberInfo.setRegfrom(source);
		memberInfo.setInviteid(1L);
		memberInfo.setIp(ip);
		baseDao.saveObject(memberInfo);
		OpenMember om = new OpenMember(member.getId(), source, loginname, member.getId());
		om.setOtherinfo(JsonUtils.addJsonKeyValue(om.getOtherinfo(), "rights","ticketorder,moviecomment,dramacomment,topic,agenda,activity,wala"));
		baseDao.saveObject(om);
		return om;
	}
	@Override
	public OpenMember createOpenMemberByMember(Member member, String source, String loginname) {
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		OpenMember om = getOpenMemberByLoginname(source, loginname);
		if(om!=null){
			om.setValidtime(DateUtil.addDay(curtime, 180));
			baseDao.saveObject(om);
			return om;
		}
		om = new OpenMember(member.getId(), source, loginname, member.getId());
		om.setValidtime(DateUtil.addDay(curtime, 180));
		om.setOtherinfo(JsonUtils.addJsonKeyValue(om.getOtherinfo(), "rights","ticketorder,moviecomment,dramacomment,topic,agenda,activity,wala"));
		baseDao.saveObject(om);
		return om;
	}
	@Override
	public ErrorCode<Member> regMember(String nickname, String email, String password, Long inviteid, String invitetype, String regfrom, String citycode, String ip){
				email = StringUtils.trim(StringUtils.lowerCase(email));
		boolean isEmail = ValidateUtil.isEmail(email);
		if(!isEmail)return ErrorCode.getFailure("邮箱格式不正确！");
		if(StringUtil.regMatch(email, "^[A-Z0-9._-]+@yahoo.com.cn$|^[A-Z0-9._-]+@yahoo.cn$", true)) return ErrorCode.getFailure("为了保证您的后续服务，请不要使用雅虎中国邮箱！");
		ErrorCode<Member> code = createMember(nickname, email, password, inviteid, invitetype, regfrom, citycode, MemberConstant.REGISTER_EMAIL, ip, "N");
		if(!code.isSuccess()) return code;
		Member member = code.getRetval();
		if(inviteid != null){
			//加好友
			//Friend friend = new Friend(inviteid, member.getId());
			//Friend friend2 = new Friend(member.getId(), inviteid);
			try {
				Treasure treasure = new Treasure(member.getId());
				treasure.setRelatedid(inviteid);
				treasure.setAction(Treasure.ACTION_COLLECT);
				treasure.setTag(Treasure.TAG_MEMBER);
				baseDao.saveObject(treasure);
				//TODO: 移出
				walaApiService.addTreasure(treasure);
				//关注，粉丝数加一
				memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_ATTENTIONCOUNT, 1, true);
				memberCountService.updateMemberCount(inviteid, MemberStats.FIELD_FANSCOUNT, 1, true);
				
				Treasure treasure1 = new Treasure(inviteid);
				treasure1.setRelatedid(member.getId());
				treasure1.setAction(Treasure.ACTION_COLLECT);
				treasure1.setTag(Treasure.TAG_MEMBER);
				baseDao.saveObject(treasure1);
				//TODO: 移出
				walaApiService.addTreasure(treasure1);
				//关注，粉丝数加一
				memberCountService.updateMemberCount(inviteid, MemberStats.FIELD_ATTENTIONCOUNT, 1, true);
				memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_FANSCOUNT, 1, true);
				
			} catch (Exception e) {
				dbLogger.warn("", e);
			}
			
			String hql = "update EmailInvite e set e.registerid = ? where e.email = ?";
			hibernateTemplate.bulkUpdate(hql, member.getId(), email);
			//baseDao.saveObject(friend);
			//baseDao.saveObject(friend2);
		}
		return code;
	}
	
	@Override
	public ErrorCode<Member> regMemberWithMobile(String nickname, String mobile, String password, String checkpass, Long inviteid, String invitetype, String regfrom, String citycode, String ip){
		ErrorCode bindCode = bindMobileService.checkBindMobile(BindConstant.TAG_REGISTERCODE, mobile, checkpass);
		if(!bindCode.isSuccess()) return ErrorCode.getFailure(bindCode.getMsg());
		return regMemberWithMobile(nickname, mobile, password, inviteid, invitetype, regfrom, citycode, ip);
	}
	@Override
	public ErrorCode<Member> regMemberWithMobile(String nickname, String mobile, String password, Long inviteid, String invitetype, String regfrom, String citycode, String ip){
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure("手机格式不正确！");
		Member member = baseDao.getObjectByUkey(Member.class, "mobile", mobile, false);
		if(member != null){
			MemberInfo memberInfo = baseDao.getObject(MemberInfo.class, member.getId());
			Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(memberInfo.getOtherinfo());
			String bindstats = otherInfoMap.get(MemberConstant.TAG_SOURCE);
			if(!StringUtils.equals(memberInfo.getSource(), MemberConstant.REGISTER_CODE) ||(StringUtils.equals(memberInfo.getSource(), MemberConstant.REGISTER_CODE) && !StringUtils.equals(bindstats, "fail"))){
				return ErrorCode.getFailure("手机号已被使用，请换一个！");
			}
			member.setPassword(StringUtil.md5(password));
			member.setNickname(nickname);
			otherInfoMap.put(MemberConstant.TAG_SOURCE, "success");
			memberInfo.setOtherinfo(JsonUtils.writeObjectToJson(otherInfoMap));
			baseDao.saveObjectList(member, memberInfo);
			return ErrorCode.getSuccessReturn(member);
		}
		ErrorCode<Member> code = createMember(nickname, mobile, password, inviteid, invitetype, regfrom, citycode, MemberConstant.REGISTER_MOBLIE, ip, "Y");
		if(code.isSuccess()){
			MemberInfo memberInfo = baseDao.getObject(MemberInfo.class, code.getRetval().getId());
			memberInfo.finishTask(MemberConstant.TASK_BINDMOBILE);
			baseDao.saveObject(memberInfo);
		}
		return code;
	}
	
	@Override
	public ErrorCode<Member> createMemberWithBindMobile(String mobile, String checkpass, String citycode, String ip){
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure("手机格式不正确！");
		ErrorCode bindMobileCode = bindMobileService.checkBindMobile(BindConstant.TAG_REGISTERCODE, mobile, checkpass);
		if(!bindMobileCode.isSuccess()) return ErrorCode.getFailure(bindMobileCode.getMsg());
		
		Member member = baseDao.getObjectByUkey(Member.class, "mobile", mobile, false);
		if(member != null) return ErrorCode.getSuccessReturn(member);

		String singleMobile = StringUtils.substring(mobile, 0, 3);
		String nickname = singleMobile + StringUtil.getRandomString(10);
		boolean nickExist = isMemberExists(nickname, null);
		while (nickExist) {
			nickname = singleMobile + StringUtil.getRandomString(10);
			nickExist = isMemberExists(nickname, null);
		}
		String password = StringUtil.getDigitalRandomString(10);
		ErrorCode<Member> code = createMember(nickname, mobile, password, null, null, AddressConstant.ADDRESS_WEB, citycode, MemberConstant.REGISTER_CODE, ip, "Y");
		if(code.isSuccess()){
			MemberInfo memberInfo = baseDao.getObject(MemberInfo.class, code.getRetval().getId());
			Map<String, String> otherInfo = JsonUtils.readJsonToMap(memberInfo.getOtherinfo());
			otherInfo.put("bindstatus", "fail");
			memberInfo.setOtherinfo(JsonUtils.writeObjectToJson(otherInfo));
			memberInfo.finishTask(MemberConstant.TASK_BINDMOBILE);
			baseDao.saveObject(memberInfo);
		}
		return code;
	}
	
	@Override
	public ErrorCode<Member> createMemberWithDrawMobile(String mobile, String checkpass, String citycode, String ip){
		Member member = baseDao.getObjectByUkey(Member.class, "mobile", mobile, false);
		if(member != null) return ErrorCode.getSuccessReturn(member);

		String singleMobile = StringUtils.substring(mobile, 0, 3);
		String nickname = singleMobile + StringUtil.getRandomString(10);
		boolean nickExist = isMemberExists(nickname, null);
		while (nickExist) {
			nickname = singleMobile + StringUtil.getRandomString(10);
			nickExist = isMemberExists(nickname, null);
		}
		String password = StringUtil.getDigitalRandomString(10);
		ErrorCode<Member> code = createMember(nickname, mobile, password, null, null, AddressConstant.ADDRESS_WEB, citycode, MemberConstant.REGISTER_CODE, ip, "Y");
		if(code.isSuccess()){
			MemberInfo memberInfo = baseDao.getObject(MemberInfo.class, code.getRetval().getId());
			Map<String, String> otherInfo = JsonUtils.readJsonToMap(memberInfo.getOtherinfo());
			otherInfo.put("bindstatus", "fail");
			memberInfo.setOtherinfo(JsonUtils.writeObjectToJson(otherInfo));
			memberInfo.finishTask(MemberConstant.TASK_BINDMOBILE);
			baseDao.saveObject(memberInfo);
		}
		return code;
	}
	
	private ErrorCode<Member> createMember(String nickname, String mobileOrEmail, String password, Long inviteid, String invitetype, 
			String regfrom, String citycode, String source, String ip, String bindStatus){
		boolean flag = false;
		if(StringUtils.isBlank(password) || StringUtils.isBlank(nickname))
			return ErrorCode.getFailure("昵称，密码需要全部填写！");
		if(!ValidateUtil.isPassword(password)){
			return ErrorCode.getFailure("密码格式不正确,只能是字母，数字，下划线，长度6—14位！");
		}
		boolean matchNickname = ValidateUtil.isCNVariable(nickname, 1, 20);
		if(!matchNickname) return ErrorCode.getFailure("用户昵称格式不正确，不能包含特殊符号！");
		boolean match = ValidateUtil.isVariable(password, 6, 14);
		if(!match) return ErrorCode.getFailure("密码格式不正确!");
		if(ValidateUtil.isMobile(mobileOrEmail)){
			boolean emailExist = isMemberMobileExists(mobileOrEmail);
			if(emailExist) return ErrorCode.getFailure("该手机已经存在！");
			flag = true;
		}else if(ValidateUtil.isEmail(mobileOrEmail)){
			boolean emailExist = isMemberExists(mobileOrEmail, null);
			if(emailExist) return ErrorCode.getFailure("该email已经存在！");
		}else return ErrorCode.getFailure("手机或email格式不正确！");
		boolean nickExist = isMemberExists(nickname, null);
		if(nickExist) return ErrorCode.getFailure("该昵称已经存在！");
		
		
		Member member = new Member(nickname);
		member.setPassword(StringUtil.md5(password));
		if(flag) {
			member.setMobile(mobileOrEmail);
			member.setBindStatus(bindStatus);
		}
		else member.setEmail(mobileOrEmail);
		baseDao.saveObject(member);//保存数据

		MemberInfo memberInfo = new MemberInfo(member.getId(), nickname);
		if(StringUtils.isBlank(citycode)) citycode = AdminCityContant.CITYCODE_SH;
		memberInfo.setFromcity(citycode);
		if(StringUtils.isNotBlank(regfrom)) memberInfo.setRegfrom(regfrom);
		memberInfo.setSource(source);
		memberInfo.setInvitetype(invitetype);
		if(StringUtils.isNotBlank(ip)) memberInfo.setIp(ip);
		if(inviteid!=null) memberInfo.setInviteid(inviteid);
		else memberInfo.setInviteid(1L);
		baseDao.saveObject(memberInfo);
		
		//日志
		StringBuilder msg=new StringBuilder();
		if(flag)msg.append("手机号码用户注册成功");
		else msg.append("Email用户注册成功");
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_REGISTER, msg.toString() + member.getId());
		return ErrorCode.getSuccessReturn(member);
	}
	
	@Override
	public List<MemberInfo> getMemberInfoByOtherInfo(String dkey){
		DetachedCriteria query = DetachedCriteria.forClass(MemberInfo.class);
		if(StringUtils.isNotBlank(dkey)){
			query.add(Restrictions.like("otherinfo", dkey));
		}
		return hibernateTemplate.findByCriteria(query);
	}
	@Override
	public MemberInfo saveNewTask(Long memberid, String newtask){
		MemberInfo info =baseDao.getObject(MemberInfo.class, memberid);
		if(info==null || info.isFinishedTask(newtask) || info.isReceived()){
			return info;
		}
		if(StringUtils.equals(newtask, MemberConstant.TASK_CONFIRMREG)) {
			info.finishTask(MemberConstant.TASK_CONFIRMREG);
			info.finishTaskOtherInfo(MemberConstant.TASK_CONFIRMREG);
			baseDao.saveObject(info);
			//用户高级确认加50积分(从100积分改到50积分)
			pointService.addPointInfo(memberid, PointConstant.SCORE_USERCONFIRM, "用户高级确认加50积分", PointConstant.TAG_CONFIRM);
			//完成高级确认
			monitorService.saveMemberLog(info.getId(), MemberConstant.ACTION_NEWTASK, newtask, null);
		}else {
			if(StringUtils.equals(newtask, MemberConstant.TASK_BINDMOBILE)){
				Map<String, String> otherInfo = JsonUtils.readJsonToMap(info.getOtherinfo());
				otherInfo.put(BindConstant.KEY_BINDTIME, DateUtil.getCurFullTimestampStr());
				info.setOtherinfo(JsonUtils.writeMapToJson(otherInfo));
			}
			monitorService.saveMemberLog(info.getId(), MemberConstant.ACTION_NEWTASK, newtask, null);
			info.finishTask(newtask);
			baseDao.saveObject(info);
		}
		return info;
	}
	@Override
	public Integer getInviteCountByMemberid(Long memberid, Timestamp startTime, Timestamp endTime){
		DetachedCriteria query = DetachedCriteria.forClass(MemberInfo.class);
		query.add(Restrictions.eq("inviteid", memberid));
		query.add(Restrictions.ne("inviteid", 1L));
		query.add(Restrictions.lt("addtime", endTime));
		query.add(Restrictions.gt("addtime", startTime));
		query.setProjection(Projections.rowCount());
		List list = hibernateTemplate.findByCriteria(query);
		if(list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	@Override
	public Integer getMemberNotReadMessageCount(final Long memberid) {
		String key = CacheConstant.KEY_USER_MSGCOUNT_ + memberid ;
		Integer valid = (Integer) cacheService.get(CacheConstant.REGION_TWOHOUR, key);
		if(valid == null){
			Integer count1 = getMemberNotReadNormalMessageCount(memberid);
			Integer count2 = getMemberNotReadSysMessageCount(memberid);
			
			valid = count1 + count2;
			cacheService.set(CacheConstant.REGION_TWOHOUR, key, valid);
		}
		return valid;
	}
	
	@Override
	public Integer getMemberNotReadNormalMessageCount(final Long memberid){
		final String hql = "select count(*) from UserMessageAction m where m.tomemberid = ? and m.isread=0 and m.status != 'tdel'";
		List list = readOnlyTemplate.find(hql, memberid);
		if(list.size() > 0) return new Integer(""+list.get(0));
		return 0;
	}
	@Override
	public Integer getMemberNotReadSysMessageCount(final Long memberid){
		Integer count1 = 0;
		final String hql = "select count(*) from SysMessageAction s where s.tomemberid = ? and s.isread = 0";
		List listSys = readOnlyTemplate.find(hql,memberid);
		if(listSys.size() > 0) {
			count1 = new Integer(""+listSys.get(0));
		}
		
		// 群发站内信, 查询大于当前最大ID值
		Integer count2 = 0;
		String qry = "select count(*) from JsonData t where t.tag='websiteMsg' and t.validtime > ?  and t.dkey > ?";
		MemberInfo memberInfo = baseDao.getObject(MemberInfo.class, memberid);
		String maxKey = VmUtils.getJsonValueByKey(memberInfo.getOtherinfo(), "maxKey");
		if(StringUtils.isNotBlank(maxKey)){
			List list3 = readOnlyTemplate.find(qry, DateUtil.getCurFullTimestamp(), maxKey);
			count2 = Integer.parseInt(""+list3.get(0));
		}else{
			count2 = jsonDataService.countListByTag(JsonDataKey.KEY_WEBSITEMSG, DateUtil.getCurFullTimestamp());
		}
		
		// mongoDB 中查询
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(MongoData.ACTION_MEMBERID, memberid);
		paramMap.put(MongoData.ACTION_MULTYWSMSG_ISREAD, "0");
		Integer count3 = mongoService.getCount(MongoData.NS_ACTION_MULTYWSMSG, paramMap);
		
		return count1 + count2 + count3;
	}
	@Override
	public Map<Long, String> getCacheHeadpicMap(Collection<Long> memberidList) {
		Map<Long, Map> memberinfoMap = getCacheMemberInfoMap(memberidList);
		Map<Long, String> headpicMap = new LinkedHashMap<Long, String>();
		for(Long memberid: memberinfoMap.keySet()){
			headpicMap.put(memberid, (String)memberinfoMap.get(memberid).get("headpicUrl"));
		}
		return headpicMap;
	}
	@Override
	public Map<Long, Map> getCacheMemberInfoMap(Collection<Long> memberidList){
		if(memberidList.isEmpty()) return new HashMap<Long, Map>();
		List<String> keys = Arrays.asList(("MI"+StringUtils.join(memberidList, ",MI")).split(","));
		Map bulkMap = cacheService.getBulk(CacheConstant.REGION_ONEHOUR, keys);
		Map<Long, Map> result = new LinkedHashMap<Long, Map>();
		for(Object key: bulkMap.keySet()){
			Map singleMap = (Map) bulkMap.get(key);
			result.put((Long) singleMap.get("id"), singleMap);
		}
		for(Long memberid: memberidList){
			if(!result.containsKey(memberid)){
				MemberInfo mi = baseDao.getObject(MemberInfo.class, memberid);
				if(mi!=null){
					Map info = BeanUtil.getBeanMapWithKey(mi, "id", "nickname", "headpicUrl", "buyticket");
					cacheService.set(CacheConstant.REGION_ONEHOUR, "MI" + memberid, info);
					result.put(memberid, info);
				}
			}
		}
		
		return result;
	}
	@Override
	public Map getCacheMemberInfoMap(Long memberid) {
		Map result = (Map) cacheService.get(CacheConstant.REGION_ONEDAY, "MI" + memberid);
		if(result==null){
			MemberInfo mi = baseDao.getObject(MemberInfo.class, memberid);
			if(mi!=null){
				result = BeanUtil.getBeanMapWithKey(mi, "id", "nickname", "headpicUrl", "addtime");
				cacheService.set(CacheConstant.REGION_ONEDAY, "MI" + memberid, result);
			}
		}
		return result;
	}
	@Override
	public String getCacheHeadpicMap(Long memberid) {
		Map result = getCacheMemberInfoMap(memberid);
		if(result==null) return null;
		return (String) result.get("headpicUrl");
	}
	@Override
	public Jobs getMemberPosition(Integer exp) {
		if(exp<0) exp = 0;
		DetachedCriteria query = DetachedCriteria.forClass(Jobs.class);
		query.add(Restrictions.le("expvalue", exp));
		query.addOrder(Order.desc("id"));
		List<Jobs> jobsList = hibernateTemplate.findByCriteria(query, 0, 1);
		if (jobsList.size() > 0)
			return jobsList.get(0);
		return null;
	}

	@Override
	public Jobs getMemberNextPosition(Integer exp) {
		if(exp<0) exp = 0;
		DetachedCriteria query = DetachedCriteria.forClass(Jobs.class);
		query.add(Restrictions.gt("expvalue", exp));
		query.addOrder(Order.asc("id"));
		List<Jobs> jobsList = hibernateTemplate.findByCriteria(query, 0, 1);
		if (jobsList.size() > 0)
			return jobsList.get(0);
		return null;
	}

	@Override
	public void addExpForMember(Long memberId, Integer exp) {
		//添加积分
		MemberInfo memberInfo = baseDao.getObject(MemberInfo.class, memberId);
		Integer beforeExp = memberInfo.getExpvalue();
		Integer afterExp = beforeExp+exp;
		if(afterExp<0) afterExp = 0;
		memberInfo.setExpvalue(afterExp);
		baseDao.saveObject(memberInfo);
		//升级记录与通知
		Jobs jobBefore = getMemberPosition(beforeExp);
		Jobs jobAfter = getMemberPosition(afterExp);
		if(jobBefore.getId() != jobAfter.getId()){
			saveJobUp(memberId, jobAfter.getId(), jobAfter.getPosition());
		}
	}

	@Override
	public void saveJobUp(Long memberId, Long jobsId, String position) {
		JobsUp jobsUp = new JobsUp();
		jobsUp.setJobsid(jobsId);
		jobsUp.setMemberid(memberId);
		jobsUp.setPosition(position);
		jobsUp.setAddtime(new Timestamp(System.currentTimeMillis()));
		baseDao.saveObject(jobsUp);
	}
	@Override
	public List<JobsUp> getJobsUpByMemberId(Long memberId) {
		DetachedCriteria query = DetachedCriteria.forClass(JobsUp.class);
		query.add(Restrictions.eq("memberid", memberId));
		query.addOrder(Order.desc("addtime"));
		List<JobsUp> jobsUpList = hibernateTemplate.findByCriteria(query);
		if(jobsUpList.size()>0){
			return jobsUpList;
		}
		return null;
	}
	@Override
	public Member getMemberByEmail(String email) {
		String query = "from Member a where a.email = ? ";
		List<Member> memberList = queryByRowsRange(query, 0, 1, email);
		if (memberList.size() > 0)
			return memberList.get(0);
		return null;
	}
	//获取职位信息
	@Override
	public Map getMemberJobsInfo(Long memberid){
		Map model=new HashMap();
		//总经验值
		MemberInfo memberInfo = baseDao.getObject(MemberInfo.class, memberid);
		if (memberInfo != null){
				Jobs jobs = getMemberPosition(memberInfo.getExpvalue());
				Jobs nextJobs = getMemberNextPosition(memberInfo.getExpvalue());
				model.put("nextJobs", nextJobs);
				model.put("jobs", jobs);
				model.put("nextExp", nextJobs.getExpvalue()-memberInfo.getExpvalue());
		}
		return model;
	}
	@Override
	public ErrorCode<Member> doLoginByEmailOrMobile(String loginName, String plainPass) {
		String encodePass = StringUtil.md5(plainPass);
		String query1 = "from Member m where m.email = ? and m.password=? ";
		String query2 = "from Member m where m.mobile = ? and m.password=? ";
		List<Member> members = null;
		if(ValidateUtil.isMobile(loginName)){
			members = hibernateTemplate.find(query2, loginName, encodePass);
		}else{
			members = hibernateTemplate.find(query1, loginName.toLowerCase(), encodePass);
		}
		if(members.size()> 0) {
			Member member = members.get(0);
			if(member.isEnabled()){
				return ErrorCode.getSuccessReturn(member);
			}else{
				return ErrorCode.getFailure("用户被禁用，请联系客服！");
			}
		}
		return ErrorCode.getFailure("用户名或密码错误！");
	}
	@Override
	public List<MemberUsefulAddress> getMemberUsefulAddressByMeberid(Long memberid, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(MemberUsefulAddress.class);
		if(memberid != null)query.add(Restrictions.eq("memberid", memberid));
		query.addOrder(Order.desc("addtime"));
		List<MemberUsefulAddress> addressList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return addressList;
	}
	@Override
	public ErrorCode<MemberUsefulAddress> saveMemberUsefulAddress(Long id, Long memberid, String realname, String provincecode, String provincename,
			String citycode, String cityname, String countycode, String countyname, String address, String mobile, String postalcode, String IDcard){
		//if(memberid == null) return ErrorCode.getFailure("用户ID不能为空！");
		if(StringUtils.isBlank(realname)) return ErrorCode.getFailure("收件人不能为空！");
		if(StringUtils.isBlank(provincecode) || StringUtils.isBlank(provincename)) return ErrorCode.getFailure("省份不能为空！");
		if(StringUtils.isBlank(citycode) || StringUtils.isBlank(cityname)) return ErrorCode.getFailure("城市不能为空！");
		if(StringUtils.isBlank(countycode) || StringUtils.isBlank(countyname)) return ErrorCode.getFailure("区域不能为空！");
		if(StringUtils.isBlank(address)) return ErrorCode.getFailure("地址不能为空！");
		if(!ValidateUtil.isMobile(mobile)) return ErrorCode.getFailure("手机号错误！");
		
		MemberUsefulAddress memAddress = baseDao.getObject(MemberUsefulAddress.class, id);
		if(memAddress == null){
			memAddress = new MemberUsefulAddress();
			memAddress.setMemberid(memberid);
			memAddress.setAddtime(DateUtil.getCurFullTimestamp());
		}else{
			if(memberid != null){
				if(memAddress.getMemberid() != null && !memAddress.getMemberid().equals(memberid)) return ErrorCode.getFailure("不能修改他人地址！");
			}else{
				if(!StringUtils.equals(memAddress.getMobile(), mobile)){
					return ErrorCode.getFailure("不能修改他人地址！");
				}
			}
		}
		memAddress.setRealname(realname);
		memAddress.setAddress(address);
		memAddress.setMobile(mobile);
		memAddress.setRealname(realname);
		memAddress.setProvincecode(provincecode);
		memAddress.setProvincename(provincename);
		memAddress.setCitycode(citycode);
		memAddress.setCityname(cityname);
		memAddress.setCountycode(countycode);
		memAddress.setCountyname(countyname);
		if(StringUtils.isNotBlank(postalcode))memAddress.setPostalcode(postalcode);
		if(StringUtils.isNotBlank(IDcard))memAddress.setIDcard(IDcard);
		baseDao.saveObject(memAddress);
		return ErrorCode.getSuccessReturn(memAddress);
	}
	@Override
	public MemberInfo updateDanger(Long memberid) {
		MemberInfo info = baseDao.getObject(MemberInfo.class, memberid);
		Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(info.getOtherinfo());
		if(StringUtils.isBlank(otherInfoMap.get(MemberConstant.TAG_DANGER))){
			otherInfoMap.put(MemberConstant.TAG_DANGER, Status.Y);
			info.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
			baseDao.saveObject(info);
		}
		return info;
	}
	public void setCheckPass(String checkPass) {
		this.checkPass = checkPass;
	}
	@Override
	public ErrorCode changeBind(Member member, String newmobile, String checkpass, String remoteIp) {
		boolean exists = isMemberMobileExists(newmobile);
		if(exists) return ErrorCode.getFailure("该号码已绑定其他帐号，请更换其他手机号码！");
		return bindMobile(member, newmobile, newmobile, checkpass, null, remoteIp);
	}

	@Override
	public ErrorCode bindMobile(Member member, String mobile, String checkpass, String remoteIp) {
		ErrorCode code = bindMobile(member, mobile, mobile ,checkpass, null, remoteIp);
		return code;
	}
	@Override
	public ErrorCode bindMobile(Member member, String mobile, String checkpass, User user, String remoteIp) {
		if(user==null) return ErrorCode.getFailure("未登录用户！");
		return bindMobile(member, mobile, mobile, checkpass, user, remoteIp);
	}
	private ErrorCode bindMobile(Member member, String checkMobile, String bindMobile, String checkpass, User user, String remoteIp) {
		String oldMobile = member.getMobile();
		if(!ValidateUtil.isMobile(bindMobile)){
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "手机号输入错误！");
		}
		if(bindMobile.equals(member.getMobile())){
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "该手机号码已经绑定过账户，请更换新的手机号码！");
		}
		Member bindedMember = baseDao.getObjectByUkey(Member.class, "mobile", bindMobile, false);
		if(bindedMember != null){
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "该手机号已经被绑定！");
		}
		ErrorCode bindCode = bindMobileService.checkBindMobile(BindConstant.TAG_BINDMOBILE, checkMobile, checkpass);
		if(!bindCode.isSuccess()){
			return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, bindCode.getMsg());
		}
		//绑定到用户
		member.setMobile(bindMobile);
		member.setBindStatus(MemberConstant.BINDMOBILE_STATUS_Y);
		baseDao.saveObject(member);
		MemberInfo info = saveNewTask(member.getId(), MemberConstant.TASK_BINDMOBILE);
		
		Map<String, String> logInfo = new HashMap<String, String>();
		if(StringUtils.isNotBlank(oldMobile)){
			logInfo.put("oldMobile", VmUtils.getSmobile(oldMobile));
			logInfo.put("newMobile", VmUtils.getSmobile(bindMobile));
		}
		if(user!=null){
			logInfo.put("bindByAdmin", "" + user.getId());
		}
		monitorService.saveMemberLogMap(member.getId(), MemberConstant.ACTION_BINDMOBILE, logInfo, remoteIp);

		return ErrorCode.getSuccessReturn(info);
	}
	@Override
	public ErrorCode unbindMobileByAdmin(Long memberid, User user, String remoteIp) {
		Member member = baseDao.getObject(Member.class, memberid);
		if (member == null){
			return ErrorCode.getFailure("用户不存在！");
		}
		if (!member.isBindMobile()){
			return ErrorCode.getFailure("该用户没有绑定手机号");
		}
		MemberInfo memberInfo = baseDao.getObject(MemberInfo.class, member.getId());
		MemberAccount memberAccount = baseDao.getObjectByUkey(MemberAccount.class, "memberid", member.getId());
		List<ElecCard> cardList = elecCardService.getCardListByMemberid(member.getId(), null, 0, 1);
		boolean noElecCard = true;
		if (cardList!=null&&cardList.size()!=0) {
			noElecCard = false;
		}
		if (memberInfo.isRegisterSource(MemberConstant.REGISTER_MOBLIE) && StringUtils.isBlank(member.getEmail())
				&& memberAccount!=null && (memberAccount.getBankcharge() != 0 || memberAccount.getOthercharge() != 0 || !noElecCard)) {
			return ErrorCode.getFailure("该用户为手机注册用户 且 未绑定邮箱 且 账户有可用余额、瓦币、票券，不能解除手机绑定！");
		}
		Map<String, String> logInfo = new HashMap<String, String>();
		logInfo.put("oldMobile", VmUtils.getSmobile(member.getMobile()));
		logInfo.put("newMobile", "解绑");
		logInfo.put("bindByAdmin", "" + user.getId());
		if(StringUtils.isBlank(member.getEmail()))
			member.setEmail(member.getMobile()+StringUtil.getRandomString(6)+"@unbindmobile.com");
		member.setMobile(null);
		member.setBindStatus(MemberConstant.BINDMOBILE_STATUS_N);
		baseDao.updateObject(member);
		monitorService.saveMemberLogMap(member.getId(), MemberConstant.ACTION_BINDMOBILE, logInfo, remoteIp);
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode unbindMobile(Member member, String checkpass, String remoteIp) {
		//TODO:功能内聚：将手机注册的不能解绑等逻辑加入进来
		MemberInfo memberInfo = baseDao.getObject(MemberInfo.class, member.getId());
		if(StringUtils.equals(memberInfo.getSource(), MemberConstant.REGISTER_MOBLIE)) {
			return ErrorCode.getFailure("您未绑定手机！");
		}
		if(StringUtils.isBlank(member.getEmail()) || !memberInfo.isBindSuccess()) {
			return ErrorCode.getFailure("请设置安全邮箱！");
		}
		if(StringUtils.isBlank(checkpass)) {
			return ErrorCode.getFailure("手机验证码不能为空！");
		}
		
		if(!canChangeMobile(member)){
			return ErrorCode.getFailure("手机绑定后7天内不能解除绑定！");			
		}
		ErrorCode code = bindMobileService.checkBindMobile(BindConstant.TAG_BINDMOBILE, member.getMobile(), checkpass);
		if(!code.isSuccess()) return ErrorCode.getFailure(code.getMsg());
		
		baseDao.saveObject(memberInfo);
		
		String oldMobile = VmUtils.getSmobile(member.getMobile());
		member.setMobile(null);
		member.setBindStatus(MemberConstant.BINDMOBILE_STATUS_N);
		baseDao.saveObject(member);
		monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_RELIEVEMOBILE, oldMobile, remoteIp);
		return ErrorCode.SUCCESS;
	}
	@Override
	public boolean canChangeMobile(Member member){
		if(StringUtils.isNotBlank(member.getMobile())){
			MemberInfo info = baseDao.getObject(MemberInfo.class, member.getId());
			Map<String, String> otherinfo = JsonUtils.readJsonToMap(info.getOtherinfo());
			if(otherinfo.containsKey(BindConstant.KEY_BINDTIME)){
				Timestamp bindTime = DateUtil.parseTimestamp(otherinfo.get(BindConstant.KEY_BINDTIME));
				return DateUtil.addDay(bindTime, 7).before(DateUtil.getCurFullTimestamp());
			}
		}
		return true;
	}
	@Override
	public OpenMember createOpenMemberWithBaseInfo(String citycode, String source, String loginname,String ip,Map<String, Object> userInfo) {
		Member member = new Member("");
		String email = loginname;
		if(!ValidateUtil.isEmail(email)) {
			email = StringUtil.getRandomString(15) + "-open@" + source + ".com";
			Member exists = getMemberByEmail(email);
			if(exists!=null){
				email = StringUtil.getRandomString(15) + "-open@" + source + ".com";
			}
		}
		
		String nickname = "";
		String sex = "";
		String headpic = "";
		if (StringUtils.equals(source, MemberConstant.SOURCE_TENCENT)) {
			nickname = String.valueOf(userInfo.get("nickname"));
			sex = String.valueOf(userInfo.get("gender"));
			headpic = String.valueOf(userInfo.get("figureurl_qq_2"));
		}else if (StringUtils.equals(source, MemberConstant.SOURCE_SINA)) {
			if (userInfo.get("error_code")==null) {
				nickname = String.valueOf(userInfo.get("name"));
				sex = String.valueOf(userInfo.get("gender"));
				if(!StringUtils.isBlank(sex)) sex = sex.equals("m")?"男":"女";
				headpic = String.valueOf(userInfo.get("avatar_large"));
			}
		}
		
		member.setPassword(StringUtil.md5(StringUtil.getRandomString(10)));
		member.setEmail(email);
		if(!StringUtils.isBlank(nickname)){
			boolean nickExist = isMemberExists(nickname, null);
			member.setNickname(nickExist?nickname+StringUtil.getRandomString(2):nickname);
		}
			
		baseDao.saveObject(member);
		
		MemberInfo memberInfo = new MemberInfo(member.getId(), member.getNickname());
		memberInfo.setHeadpic(headpic);
		memberInfo.setSource(MemberConstant.REGISTER_APP);
		memberInfo.setFromcity(citycode);
		Map<String, String> otherInfo = VmUtils.readJsonToMap(memberInfo.getOtherinfo());
		otherInfo.put(MemberConstant.OPENMEMBER, source);
		otherInfo.put(MemberConstant.TAG_SOURCE, "fail");
		memberInfo.setOtherinfo(JsonUtils.writeObjectToJson(otherInfo));
		memberInfo.setInvitetype("self");
		memberInfo.setRegfrom(source);
		memberInfo.setInviteid(1L);
		memberInfo.setIp(ip);
		if(StringUtils.isNotBlank(sex))
			memberInfo.setSex(sex);

		baseDao.saveObject(memberInfo);
		
		OpenMember om = new OpenMember(member.getId(), source, loginname, member.getId());
		om.setOtherinfo(JsonUtils.addJsonKeyValue(om.getOtherinfo(), "rights","ticketorder,moviecomment,dramacomment,topic,agenda,activity,wala"));
		baseDao.saveObject(om);
		return om;
	}
	
	@Override
	public OpenMember openMbrBindGewaMbr(String source,String loginname,String ip,Long memberid) {	
		OpenMember om = new OpenMember(memberid, source, loginname, memberid);
		om.setOtherinfo(JsonUtils.addJsonKeyValue(om.getOtherinfo(), "rights","ticketorder,moviecomment,dramacomment,topic,agenda,activity,wala"));
		baseDao.saveObject(om);
		return om;
	}
	
	
	/**
	 * 第三方用户注册格瓦拉帐号
	 */
	@Override
	public ErrorCode<Member> createMemberWithPartner(String userid, String loginName, String nickname, String password, String citycode, String regForm, String source, String ip){
		if (getMemberByEmailOrMobile(loginName) != null) {
			return ErrorCode.getFailure("该帐号已被注册，请直接登录！");
		}
		
		boolean nickExist = isMemberExists(nickname, null);
		if (nickExist) {
			return ErrorCode.getFailure("注册失败，昵称已存在！"); 
		}
		ErrorCode<Member> code = createMember(nickname, loginName, password, null, null, regForm, citycode, source, ip, "N");
		if (!code.isSuccess()) {
			return ErrorCode.getFailure("注册失败，请稍候重试！");
		}
		Member member = code.getRetval();
		createOpenMemberByMember(member, source, userid);
		return code;
	}
	
	@Override
	public Member getMemberByMobile(String mobile) {
		return baseDao.getObjectByUkey(Member.class, "mobile", mobile);
	}
	
	@Override
	public ErrorCode<Member> createWithMobile(String mobile, User user){
		dbLogger.warn("后台用户创建用户[" + user.getId() + "]" + mobile);
		String nickname = StringUtil.getRandomString(10);
		String password = StringUtil.getRandomString(8); 
		ErrorCode<Member> code = createMember(nickname, mobile, password, null, null, null, null, MemberConstant.REGISTER_MOBLIE, null, "Y");
		if(code.isSuccess()){
			MemberInfo memberInfo = baseDao.getObject(MemberInfo.class, code.getRetval().getId());
			memberInfo.finishTask(MemberConstant.TASK_BINDMOBILE);
			baseDao.saveObject(memberInfo);
		}
		return code;
	}
	private Member getMemberByEmailOrMobile(String loginName) {
		String query1 = "from Member m where m.email = ?";
		String query2 = "from Member m where m.mobile = ?";
		List<Member> members = null;
		if(ValidateUtil.isMobile(loginName)){
			members = hibernateTemplate.find(query2, loginName);
		}else{
			members = hibernateTemplate.find(query1, loginName.toLowerCase());
		}
		if(members.size()> 0) {
			return members.get(0);
		}
		return null;
	}
	@Override
	public ErrorCode<TempMember> createTempMember(String mobile, String password, String flag, String ip, Map<String, String> otherMap) {
		if(!ValidateUtil.isMobile(mobile)){
			return ErrorCode.getFailure("手机号有错误！");
		}
		if(!ValidateUtil.isPassword(password)){
			return ErrorCode.getFailure("密码有错误，必须是6-14位字符！");
		}
		Member member = getMemberByMobile(mobile);
		if(member!=null){
			return ErrorCode.getFailure("您的手机已在格瓦拉注册，请登录！");
		}
		String encodePass = StringUtil.md5(password);
		TempMember tm = baseDao.getObjectByUkey(TempMember.class, "mobile", mobile);
		if(tm!=null){
			if(!StringUtils.equals(tm.getTmppwd(), encodePass)){
				return ErrorCode.getFailure("您的手机已在格瓦拉注册，请登录格瓦拉账户！");
			}
			if(StringUtils.equals(tm.getStatus(), Status.Y)){
				return ErrorCode.getFailure("对不起，您已经抢购过活动码，本活动仅限购买一次！");
			}
			tm.setFlag(flag);
		}else{
			tm = new TempMember(mobile, encodePass, flag, ip);
		}
		Map<String, String> otMap = JsonUtils.readJsonToMap(tm.getOtherinfo());
		otMap.putAll(otherMap);
		tm.setOtherinfo(JsonUtils.writeMapToJson(otMap));
		tm.setMembertype(TempMember.MEMBERTYPE_NEW);
		baseDao.saveObject(tm);
		return ErrorCode.getSuccessReturn(tm);
	}
	@Override
	public ErrorCode<TempMember> createTempMemberBind(Member member, String mobile, String flag, String ip) {
		String membertype = TempMember.MEMBERTYPE_BIND;
		if(StringUtils.isNotBlank(mobile)){//针对未绑定的用户
			if(StringUtils.isNotBlank(member.getMobile())){//实际已绑定手机
				return ErrorCode.getFailure("您已绑定手机，不能更改！");
			}
			membertype = TempMember.MEMBERTYPE_UNBIND;
			Member same = baseDao.getObjectByUkey(Member.class, "mobile", mobile);
			if(same!=null){
				return ErrorCode.getFailure("此手机号已经注册过！");
			}
		}else{//针对已绑定的用户
			if(StringUtils.isBlank(member.getMobile())){//实际未绑定手机
				return ErrorCode.getFailure("您未绑定手机，请填写手机号！");
			}
		}
		TempMember tm = baseDao.getObjectByUkey(TempMember.class, "memberid", member.getId());
		if(tm==null){
			tm = new TempMember(mobile, member.getId(), flag, ip);	
		}else{
			if(StringUtils.equals(tm.getStatus(), Status.Y)){
				return ErrorCode.getFailure("对不起，您已经抢购过活动码，本活动仅限购买一次！");
			}
			if(!StringUtils.equals(tm.getMobile(), mobile)){
				tm.setMobile(mobile);
			}
		}
		tm.setMembertype(membertype);
		tm.setFlag(flag);//修改Flag
		baseDao.saveObject(tm);
		return ErrorCode.getSuccessReturn(tm);
	}
	@Override
	public ErrorCode<Member> createMemberFromTempMember(TempMember tm) {
		Map<String, String> otherMap = JsonUtils.readJsonToMap(tm.getOtherinfo());
		String citycode = otherMap.get("citycode");
		String regfrom = otherMap.get("regfrom");
		ErrorCode<Member> code = createMember(StringUtil.getRandomString(4)+tm.getId(), tm.getMobile(), StringUtil.getRandomString(8), 
				null, null, regfrom, citycode, MemberConstant.REGISTER_MOBLIE, tm.getIp(), "X");
		if(code.isSuccess()){
			Member m = code.getRetval();
			m.setPassword(tm.getTmppwd());
			m.setBindStatus("X");//未知状态
			baseDao.saveObject(m);
			tm.setMemberid(m.getId());
			tm.setStatus(Status.Y);
			baseDao.saveObject(tm);
		}
		return code;
	}
	@Override
	public ErrorCode<Member> bindMobileFromTempMember(TempMember tm) {
		Member member = baseDao.getObject(Member.class, tm.getMemberid());
		if(StringUtils.isBlank(member.getMobile())){//未绑定
			member.setBindStatus("X");
			member.setMobile(tm.getMobile());
			baseDao.saveObject(member);
			tm.setStatus(Status.Y);
			baseDao.saveObject(tm);
			return ErrorCode.getSuccessReturn(member);
		}else if(StringUtils.equals(TempMember.MEMBERTYPE_BIND, tm.getMembertype())){//原来已经绑定
			tm.setStatus(Status.Y);
			baseDao.saveObject(tm);
			return ErrorCode.getSuccessReturn(member);
		}
		return ErrorCode.getFailure("已处理过！");
	}

}