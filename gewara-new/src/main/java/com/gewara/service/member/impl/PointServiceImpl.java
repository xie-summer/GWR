/**
 * 
 */
package com.gewara.service.member.impl;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.PointConstant;
import com.gewara.constant.Status;
import com.gewara.constant.SysAction;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.model.bbs.Diary;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.draw.Prize;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.PubSaleOrder;
import com.gewara.model.user.Festival;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.Point;
import com.gewara.model.user.PointHist;
import com.gewara.model.user.ShareMember;
import com.gewara.service.DaoService;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.CommonPartService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.member.MemberService;
import com.gewara.service.member.PointService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.ShareService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;

/**
 * @author hxs(ncng_2006@hotmail.com)
 * @since Feb 2, 2010 10:13:44 AM 
 */
@Service("pointService")
public class PointServiceImpl extends BaseServiceImpl implements PointService {
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	@Autowired@Qualifier("memberService")
	private MemberService memberService;
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	public void setUserMessageService(UserMessageService userMessageService) {
		this.userMessageService = userMessageService;
	}
	@Autowired@Qualifier("drawActivityService")
	private DrawActivityService drawActivityService;
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	@Autowired@Qualifier("commonPartService")
	private CommonPartService commonPartService;
	
	@Override
	public List<Point> getPointListByMemberid(Long memberid, String tag, Timestamp addtime, Timestamp endtime, String order, int from, int maxnum){
		return getPointListByMemberidAndPointValue(memberid, tag, 0, addtime, endtime, order, from, maxnum);
	}
	@Override
	public List<Point> getPointListByMemberidAndPointValue(Long memberid, String tag, int pointvalue, Timestamp addtime, Timestamp endtime, String order, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Point.class);
		if(memberid!=null)query.add(Restrictions.eq("memberid", memberid));
		if(StringUtils.isNotBlank(tag))query.add(Restrictions.eq("tag", tag));
		if(pointvalue > 0 ) query.add(Restrictions.ge("point", pointvalue));
		if(addtime!=null)query.add(Restrictions.ge("addtime", addtime));
		if(endtime!=null)query.add(Restrictions.le("addtime", endtime));
		if(StringUtils.isNotBlank(order)){
			query.addOrder(Order.desc(order));
		}
		query.addOrder(Order.desc("addtime"));
		List<Point> pointList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return pointList;
	}
	
	@Override
	public List<PointHist> getPointHistListByMemberidAndPointValue(Long memberid, String tag, int pointvalue, Timestamp addtime, Timestamp endtime, String order, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(PointHist.class);
		if(memberid!=null)query.add(Restrictions.eq("memberid", memberid));
		if(StringUtils.isNotBlank(tag))query.add(Restrictions.eq("tag", tag));
		if(pointvalue > 0 ) query.add(Restrictions.ge("point", pointvalue));
		if(addtime!=null)query.add(Restrictions.ge("addtime", addtime));
		if(endtime!=null)query.add(Restrictions.le("addtime", endtime));
		if(StringUtils.isNotBlank(order)){
			query.addOrder(Order.desc(order));
		}
		query.addOrder(Order.desc("addtime"));
		List<PointHist> pointList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return pointList;
	}
	
	@Override
	public Integer getPointCountByMemberid(Long memberid, String tag, Timestamp addtime, Timestamp endtime){
		return getPointCountByMemberidAndPointValue(memberid, tag, 0, addtime, endtime);
	}
	
	@Override
	public Integer getPointCountByMemberidAndPointValue(Long memberid, String tag, int pointvalue, Timestamp addtime, Timestamp endtime){
		DetachedCriteria query = DetachedCriteria.forClass(Point.class);
		query.setProjection(Projections.rowCount());
		if(memberid!=null)query.add(Restrictions.eq("memberid", memberid));
		if(pointvalue > 0 ) query.add(Restrictions.ge("point", pointvalue));
		if(StringUtils.isNotBlank(tag))query.add(Restrictions.eq("tag", tag));
		if(addtime!=null)query.add(Restrictions.ge("addtime", addtime));
		if(endtime!=null)query.add(Restrictions.le("addtime", endtime));
		List result = readOnlyTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}
	@Override
	public Integer getPointHistCountByCondition(Long memberid, String tag, int pointvalue, Timestamp addtime, Timestamp endtime){
		DetachedCriteria query = DetachedCriteria.forClass(PointHist.class);
		query.setProjection(Projections.rowCount());
		if(memberid!=null)query.add(Restrictions.eq("memberid", memberid));
		if(pointvalue > 0 ) query.add(Restrictions.ge("point", pointvalue));
		if(StringUtils.isNotBlank(tag))query.add(Restrictions.eq("tag", tag));
		if(addtime!=null)query.add(Restrictions.ge("addtime", addtime));
		if(endtime!=null)query.add(Restrictions.le("addtime", endtime));
		List result = readOnlyTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		return Integer.parseInt("" + result.get(0));
	}
	@Override
	public Point addPointInfo(Long memberid, Integer pointvalue, String reason, String tag) {
		return addPointInfo(memberid, pointvalue, reason, tag, null, null, new Timestamp(System.currentTimeMillis()), null, null);
	}
	@Override
	public Point addPointInfo(Long memberid, Integer pointvalue, String reason, String tag, Long tagid, Long adminid) {
		return addPointInfo(memberid, pointvalue, reason, tag, tagid, adminid, new Timestamp(System.currentTimeMillis()), null, null);
	}
	@Override
	public Point addPointInfo(Long memberid, Integer pointvalue, String reason, String tag, Long tagid, Long adminid, Timestamp addtime){
		return addPointInfo(memberid, pointvalue, reason, tag, tagid, adminid, addtime, null, null);
	}
	@Override
	public Point addPointInfo(Long memberid, Integer pointvalue, String reason, String tag, Long tagid, Long adminid, Timestamp addtime, String uniquetag, String statflag) {
		MemberInfo info = baseDao.getObject(MemberInfo.class, memberid);
		return addFullPointInfo(info, pointvalue, reason, tag, tagid, adminid, addtime, uniquetag, statflag);
	}
	private Point addFullPointInfo(MemberInfo info, Integer pointvalue, String reason, String tag, Long tagid, Long adminid, Timestamp addtime, String uniquetag, String statflag) {
		Point point = new Point(info.getId());
		point.setUniquetag(uniquetag);
		point.setAddtime(addtime);
		point.setPoint(pointvalue);
		point.setTagid(tagid);
		point.setTag(tag);
		point.setStatflag(statflag);
		if(reason != null) point.setReason(reason);
		if(adminid != null) point.setAdminid(adminid);
		info.addPointValue(point.getPoint());
		baseDao.updateObject(info);
		baseDao.saveObject(point);
		return point;
	}
	
	@Override
	public ErrorCode<Point> addOrderPoint(GewaOrder order){
		//增加积分
		if(order instanceof PubSaleOrder) return ErrorCode.getFailure("竞拍不加积分");
		if(order.sureOutPartner()) return ErrorCode.getFailure("合作商不加积分");					
		Integer pointvalue = 0;
		String reason = order.getTradeNo();
		if(order.getPaymethod().equals(PaymethodConstant.PAYMETHOD_GEWAPAY)){
			pointvalue = order.getDue() - order.getWabi();
			if(pointvalue<0) pointvalue = 0;
		}else{
			pointvalue = order.getDue();
		}
		Point point = addPointInfo(order.getMemberid(), pointvalue, reason, PointConstant.TAG_TRADE);
		return ErrorCode.getSuccessReturn(point);
	}

	@Override
	public List<Map> getPointVariableList(Timestamp startTime, Timestamp endTime, String tag, 
			int valueStart, int valueEnd, int from, int maxnum) {
		List<Map> result = null;
		//用户总结分
		String hql="select new map(p.memberid as memberid, " +
				"sum(case(sign(p.point)) when 1 then p.point else 0 end) as gainpoint, " +
				"sum(case(sign(p.point)) when -1 then p.point else 0 end) as paypoint " +
				") from Point p where p.addtime >=? and p.addtime <= ? and " +
				"exists (select id from MemberInfo s where s.id=p.memberid and s.pointvalue >=? and s.pointvalue<=?) ";
		if(StringUtils.isNotBlank(tag)){
			hql += "and tag= ? group by p.memberid order by sum(p.point) desc";
			result = queryByRowsRange(hql, from, maxnum, startTime, endTime, valueStart, valueEnd, tag);
		}else{
			hql += "group by p.memberid order by sum(p.point) desc";
			result = queryByRowsRange(hql, from, maxnum, startTime, endTime, valueStart, valueEnd);
		}
		return result;
	}
	@Override
	public Map getPointVariableMap(Timestamp startTime, Timestamp endTime, String tag, 
			int valueStart, int valueEnd){
		List<Map> result = null;
		String hql="select new map(" +
				"sum(case(sign(p.point)) when 1 then p.point else 0 end) as gainpoint, " +
				"sum(case(sign(p.point)) when -1 then p.point else 0 end) as paypoint " +
				") from Point p where p.addtime >=? and p.addtime <= ? and " +
				"exists (select id from MemberInfo s where s.id=p.memberid and s.pointvalue >=? and s.pointvalue<=?) ";
		if(StringUtils.isNotBlank(tag)){
			hql += "and p.tag= ?";
			result = hibernateTemplate.find(hql, startTime, endTime, valueStart, valueEnd, tag);
		}else{
			result = hibernateTemplate.find(hql, startTime, endTime, valueStart, valueEnd);
		}
		return result.get(0);
	}
	@Override
	public List<Map> getTopPointByDateMap(Timestamp startTime, Timestamp endTime, List<String> tagList, int from,int maxnum){
		String key = CacheConstant.buildKey("get1Top2Point3By4Date5Map6", tagList.toArray());
		List<Map> result = (List<Map>) cacheService.get(CacheConstant.REGION_ONEDAY, key);
		if(result == null){
			String hql = "select new map(p.memberid as memberid, sum(p.point) as point) from Point p where p.addtime >=:startTime " +
					"and p.addtime <=:endTime and tag in(:tagList) group by p.memberid order by sum(p.point) desc";
			result = queryByNameParams(hql, from, maxnum, "startTime,endTime,tagList", startTime, endTime, tagList);
			for(Map pointMap : result){
				Map cacheMemberInfo = memberService.getCacheMemberInfoMap(Long.parseLong(pointMap.get("memberid")+""));
				pointMap.put("nickname",cacheMemberInfo.get("nickname"));
			}
			cacheService.set(CacheConstant.REGION_ONEDAY, key, result);
		}
		return result;
	}
	
	@Override
	public Integer getSumPoint() {
		DetachedCriteria query=DetachedCriteria.forClass(Point.class);
		query.setProjection(Projections.sum("point"));
		List list=hibernateTemplate.findByCriteria(query);
		if (list.get(0)==null) return 0;
		return Integer.parseInt(""+list.get(0));
	}

	@Override
	public Integer getPointVariableCount(Timestamp startTime, Timestamp endTime, String tag, int valueStart, int valueEnd) {
		List result = null;
		String hql="select count(distinct p.memberid) from Point p" +
				" where p.addtime >=? and p.addtime <= ? and " +
				"exists (select id from MemberInfo s where s.id=p.memberid and s.pointvalue >=? and s.pointvalue<=?)";
		if(StringUtils.isNotBlank(tag)){
			hql += "and p.tag= ?";
			result = hibernateTemplate.find(hql, startTime, endTime, valueStart, valueEnd, tag);
		}else{
			result = hibernateTemplate.find(hql, startTime, endTime, valueStart, valueEnd);
		}
		if(result.get(0)==null) return 0;
		return new Integer(result.get(0)+"");
	}

	@Override
	public List getPointTagList() {
		String hql="select distinct tag from Point";
		List list=hibernateTemplate.find(hql);
		if(list==null) return null;
		return list;
	}

	@Override
	public List<Point> getPointListByIdAndType(Long memberid, Timestamp startTime, Timestamp endTime, String tag, String type, int from,int maxNum) {
		DetachedCriteria query=DetachedCriteria.forClass(Point.class);
		if(memberid != null)query.add(Restrictions.eq("memberid",memberid));
		if(startTime!=null)query.add(Restrictions.ge("addtime",startTime));
		if(endTime!=null)query.add(Restrictions.le("addtime",endTime));
		if(StringUtils.isNotBlank(tag))query.add(Restrictions.eq("tag", tag));
		if("huoqu".equals(type)){
			query.add(Restrictions.gt("point",0));
		}else if("expend".equals(type)){
			query.add(Restrictions.lt("point",0));
		}
		query.addOrder(Order.desc("addtime"));
		List<Point> listPoint=hibernateTemplate.findByCriteria(query, from, maxNum);
		return listPoint;
	}

	@Override
	public Integer getPointByIdAndTypeCount(Long memberid, Timestamp startTime, Timestamp endTime, String tag, String type){
		DetachedCriteria query=DetachedCriteria.forClass(Point.class);
		query.add(Restrictions.eq("memberid",memberid));
		if(StringUtils.isNotBlank(tag))query.add(Restrictions.eq("tag", tag));
		if(startTime!=null)query.add(Restrictions.ge("addtime",startTime));
		if(endTime!=null)query.add(Restrictions.le("addtime",endTime));
		if("huoqu".equals(type)){
			query.add(Restrictions.gt("point",0));
		}else if("expend".equals(type)){
			query.add(Restrictions.lt("point",0));
		}
		query.setProjection(Projections.rowCount());
		List listPoint=hibernateTemplate.findByCriteria(query);
		if(listPoint.get(0)==null)return 0;
		return new Integer(listPoint.get(0)+"");
	}
	@Override
	public boolean isGetLoginPoint(Long memberId, String date) {
		String query = "from Point where memberid = ? and tag = ? and to_char(addtime,'yyyy-mm-dd')=?";
		List<Point> pointList = hibernateTemplate.find(query, memberId, PointConstant.TAG_LOGIN_ACTIVIRY, date);
		if(pointList.isEmpty()) return false;
		return true;
	}

	@Override
	public ErrorCode<Point> addLoginPoint(Member member, String type, Timestamp cur) {
		if(!member.isBindMobile()){
			return ErrorCode.getFailure(ApiConstant.CODE_UNBIND_MOBILE, "请先绑定手机再领取红包！");
		}

		String key = "getpoint" + member.getId();
		boolean allow = operationService.updateOperation(key, 10);
		if(!allow){
			return ErrorCode.getFailure("操作太频繁！");
		}
		boolean isGetPoint = this.isGetLoginPoint(member.getId(), DateUtil.formatDate(cur));
		if(isGetPoint) return ErrorCode.getFailure("你今天已经领取过红包！");
		MemberInfo info = baseDao.getObject(MemberInfo.class, member.getId());
		Integer pointValue = 2;
		if (type.equals("bit")) {
			if(info.getPointvalue()<5) return ErrorCode.getFailure("你的积分太低，不能冒险！");
			Integer speValue = RandomUtils.nextInt(333);
			if(speValue == 150){
				pointValue = 100;
			}else{
				pointValue = RandomUtils.nextInt(11) - 5;
				if(pointValue==0) pointValue=1;
			}
		}else if(StringUtils.equals(type, "brt")){
			Integer speValue = RandomUtils.nextInt(1000);
			if(speValue == 500){
				pointValue = 200;
			}else{
				pointValue = 5 + RandomUtils.nextInt(6);
			}
		}
		boolean isReward = isLoginPointRewards(member.getId(), cur);
		if(isReward){
			addPointInfo(member.getId(), PointConstant.SCORE_LOGIN_REWARDS, null, PointConstant.TAG_LOGIN_ACTIVIRY_REWARDS, null, null, cur);
		}
		Point point = addPointInfo(member.getId(), pointValue, null, PointConstant.TAG_LOGIN_ACTIVIRY, null, null, cur);
		return ErrorCode.getSuccessReturn(point);
	}
	@Override
	public ErrorCode<Map> addLoginPointInFestival(Member member){
		if(StringUtils.isBlank(member.getMobile())){
			return ErrorCode.getFailure(ApiConstant.CODE_UNBIND_MOBILE, "请先绑定手机再领取红包！");
		}
		Date curDate = new Date();
		Festival festival = commonPartService.getCurFestival(curDate);
		if(festival == null) {
			return ErrorCode.getFailure("今天不是节日，改天再来领取红包吧！");
		}
		if(!member.isBindMobile()){
			return ErrorCode.getFailure("请先绑定手机再领取红包！");
		}
		Map resultMap = new HashMap();
		boolean isSendPrize = drawActivityService.isSendPrize(member.getMobile(), member.getId(), festival.getDrawid(),new Timestamp(System.currentTimeMillis()));
		if(isSendPrize){
			return ErrorCode.getFailure("今天你已经领过红包了！");
		}else{
			try{
				Prize prize = drawActivityService.clickPointPrize(festival, member);
				resultMap.put("prize",prize);
				resultMap.put("festival", festival);
				if(prize != null){
					Timestamp cur = DateUtil.getCurFullTimestamp();
					boolean isReward = isLoginPointRewards(member.getId(), cur);
					//节日用判断连续领取积分
					Point point = addPointInfo(member.getId(), 0, prize.getRemark(), PointConstant.TAG_LOGIN_ACTIVIRY);
					if(isReward) addPointInfo(member.getId(), PointConstant.SCORE_LOGIN_REWARDS, "连续登陆奖励！", PointConstant.TAG_LOGIN_ACTIVIRY_REWARDS, null, null, cur);
					resultMap.put("isReward", isReward);
					resultMap.put("pointValue", point.getPoint());
					resultMap.put("point", point);
				}
			}catch (Exception e) {
				dbLogger.error(StringUtil.getExceptionTrace(e));
				resultMap.put("errorMsg",  "操作异常");
			}
		}
		return ErrorCode.getSuccessReturn(resultMap);
	}
	
	@Override
	public boolean isLoginPointRewards(Long memberid, Timestamp cur) {
		DetachedCriteria query = DetachedCriteria.forClass(Point.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("tag", PointConstant.TAG_LOGIN_ACTIVIRY_REWARDS));
		Integer spoint =  PointConstant.LOGIN_REWARDS_DAYNUM - 1;
		query.add(Restrictions.ge("addtime", DateUtil.getBeginningTimeOfDay(DateUtil.addDay(cur, -spoint))));
		query.setProjection(Projections.rowCount());
		List list = hibernateTemplate.findByCriteria(query);
		if(Integer.parseInt(list.get(0)+"")==0) {
			DetachedCriteria query2 = DetachedCriteria.forClass(Point.class);
			query2.add(Restrictions.eq("memberid", memberid));
			query2.add(Restrictions.eq("tag", PointConstant.TAG_LOGIN_ACTIVIRY));
			query2.add(Restrictions.ge("addtime", DateUtil.getBeginningTimeOfDay(DateUtil.addDay(cur, -spoint))));
			query2.setProjection(Projections.rowCount());
			List list2 = hibernateTemplate.findByCriteria(query2);
			return Integer.parseInt(list2.get(0)+"")>=spoint;
		}
		return false;
	}
	@Override
	public Integer getPointRewardsDay(Long memberid, Timestamp cur){
		DetachedCriteria query = DetachedCriteria.forClass(Point.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("tag", PointConstant.TAG_LOGIN_ACTIVIRY_REWARDS));
		Timestamp curtime = DateUtil.getBeginningTimeOfDay(DateUtil.addDay(cur, -PointConstant.LOGIN_REWARDS_DAYNUM));
		query.add(Restrictions.ge("addtime", curtime));
		query.addOrder(Order.desc("addtime"));
		List<Point> list = hibernateTemplate.findByCriteria(query, 0, 1);
		Timestamp curDate = DateUtil.getCurTruncTimestamp();
		if(!list.isEmpty()){
			Point point = list.get(0);
			Timestamp spointTime = DateUtil.getBeginningTimeOfDay(point.getAddtime());
			if(DateUtil.getDiffDay(spointTime, curDate) == 0){
				return 7;
			}
			curtime = DateUtil.getBeginningTimeOfDay(DateUtil.addDay(point.getAddtime(), 1));
		}
		DetachedCriteria query2 = DetachedCriteria.forClass(Point.class);
		query2.add(Restrictions.eq("memberid", memberid));
		query2.add(Restrictions.eq("tag", PointConstant.TAG_LOGIN_ACTIVIRY));
		query2.add(Restrictions.ge("addtime", curtime));
		query2.setProjection(Projections.property("addtime"));
		List<Timestamp> list2 = hibernateTemplate.findByCriteria(query2);
		int count = 0;
		if(list2.isEmpty()){
			return count;
		}
		Collections.sort(list2);
		Collections.reverse(list2);
		boolean flag = false;
		for(int i = 0, j=1; i< list2.size(); i++, j++){
			Timestamp tmp = DateUtil.getBeginningTimeOfDay(list2.get(i));
			if(DateUtil.getDiffDay(tmp, curDate) == 0){
				count ++;
				flag = true;
				continue;
			}
			if((!flag && DateUtil.getDiffDay(tmp, DateUtil.addDay(curDate, -j)) == 0) 
				|| (flag && DateUtil.getDiffDay(tmp, DateUtil.addDay(curDate, -i)) == 0)){
				count++;
			}else{
				break;
			}
		}
		return count;
	}
	
	public List getPointExpendDetail(String startTime,String endTime,String tag){
		DetachedCriteria query=DetachedCriteria.forClass(Point.class);
		if(StringUtils.isNotBlank(startTime)&&StringUtils.isNotBlank(endTime))query.add(Restrictions.between("addtime",Timestamp.valueOf(startTime), Timestamp.valueOf(endTime)));
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.lt("point", 0));
		query.setProjection(Projections.projectionList().add(Projections.sum("point"), "sumpoint")
														.add(Projections.groupProperty("tag"),"tagpoint"));
		List list=hibernateTemplate.findByCriteria(query);
		return list;
	}

	@Override
	public List<Diary> getDiaryList(String tag,String pointTag,int from, int maxnum) {
		String diaryHql = "from Diary c where c.addtime>=? and c.type=? and c.tag=? and (instr(c.flag,'point')<=0 or instr(c.flag,'point') is null)" +
				" and not exists(select p.id from Point p where p.tag=? and p.memberid=c.memberid and p.tagid=c.id) " +
				" order by c.addtime desc" ;
		Timestamp time = DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -60);
		List<Diary> diaryList = queryByRowsRange(diaryHql, from, maxnum, time, DiaryConstant.DIARY_TYPE_COMMENT,tag,pointTag);
		return diaryList;
	}

	@Override
	public Integer getDiaryCount(String tag,String pointTag) {
		String diaryHql = "select count(c) from Diary c where c.addtime>=? and c.type=? and c.tag=? and (instr(c.flag,'point')<=0 or instr(c.flag,'point') is null)" +
		" and not exists(select p.id from Point p where p.tag=? and p.memberid=c.memberid and p.tagid=c.id)";
		Timestamp time = DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -60);
		List list = hibernateTemplate.find(diaryHql, time, DiaryConstant.DIARY_TYPE_COMMENT,tag,pointTag);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}

	@Override
	public Point getPointByMemberiAndTagid(Long memberid, String tag, Long tagid){
		DetachedCriteria query = DetachedCriteria.forClass(Point.class);
		query.add(Restrictions.eq("memberid",memberid));
		query.add(Restrictions.eq("tag", tag));
		if(tagid!=null){
			query.add(Restrictions.eq("tagid", tagid));
		}
		query.addOrder(Order.desc("addtime"));
		List<Point> pointList=hibernateTemplate.findByCriteria(query, 0, 1);
		if(pointList.isEmpty()) return null;
		return pointList.get(0);
	}
	
	/**
	 *  根据 memberid查询 uniquetag标识
	 * */
	@Override
	public int countUniquetagByCombine(String combUniquetag){
		DetachedCriteria query = DetachedCriteria.forClass(Point.class);
		query.setProjection(Projections.rowCount());
		query.add(Restrictions.eq("uniquetag", combUniquetag));
		List list = hibernateTemplate.findByCriteria(query);
		return Integer.valueOf(list.get(0)+"");
	}
	
	/**
	 *  最近领取红包的用户
	 * */
	public List<Map> getRecentlyGetPointList(int maxnum){
		List<Map> recentlyGetPointList = (List<Map>) cacheService.get(CacheConstant.REGION_ONEDAY, CacheConstant.KEY_RECENTLYGETPOINT);
		if(recentlyGetPointList == null){
			recentlyGetPointList = new LinkedList<Map>();
			Timestamp addtime = DateUtil.addDay(DateUtil.getCurTruncTimestamp(), -2);	//从2天以前开始
			List<Point> newPointList = getPointListByMemberid(null, PointConstant.TAG_LOGIN_ACTIVIRY, addtime, null, null, 0, maxnum);
			if(!newPointList.isEmpty()){
				for(Point point : newPointList){
					Map cacheMemberInfo = memberService.getCacheMemberInfoMap(point.getMemberid());
					Map map = new HashMap();
					map.put("addtime", point.getAddtime());
					map.put("point", point.getPoint());
					map.put("reason", point.getReason());
					map.put("memberid", point.getMemberid());
					map.put("nickname", cacheMemberInfo.get("nickname"));
					map.put("headpic", cacheMemberInfo.get("headpicUrl"));
					recentlyGetPointList.add(map);
				}
				cacheService.set(CacheConstant.REGION_ONEDAY, CacheConstant.KEY_RECENTLYGETPOINT, recentlyGetPointList);
			}
		}
		return recentlyGetPointList;
	}
	
	public List<Map> getLuckGetPointList(int maxnum){
		List<Map> luckPointList = (List<Map>) cacheService.get(CacheConstant.REGION_ONEDAY, CacheConstant.KEY_LUCKGETPOINT);
		if(luckPointList == null){
			luckPointList = new LinkedList<Map>();
			Timestamp addtime = DateUtil.addDay(DateUtil.getCurTruncTimestamp(), -10); //从10天以前开始
			List<Point> newPointList = getPointListByMemberidAndPointValue(null, PointConstant.TAG_LOGIN_ACTIVIRY, 49, addtime, null, null, 0, maxnum);
			if(!newPointList.isEmpty()){
				for(Point point : newPointList){
					Map cacheMemberInfo = memberService.getCacheMemberInfoMap(point.getMemberid());
					Map map = new HashMap();
					map.put("addtime", point.getAddtime());
					map.put("point", point.getPoint());
					map.put("reason", point.getReason());
					map.put("memberid", point.getMemberid());
					map.put("nickname", cacheMemberInfo.get("nickname"));
					map.put("headpic", cacheMemberInfo.get("headpicUrl"));
					luckPointList.add(map);
				}
				cacheService.set(CacheConstant.REGION_ONEDAY, CacheConstant.KEY_LUCKGETPOINT, luckPointList);
			}
		}
		return luckPointList;
	}
	@Override
	public void addPointToInvite(Long memberid, Integer point){
		if(point<=0) return;
		MemberInfo mi = daoService.getObject(MemberInfo.class, memberid);
		if(mi != null){
			String otherinfo = mi.getOtherinfo();
			if(mi.getInviteid() != null && mi.getInviteid()>2000 && 
					!StringUtils.contains(otherinfo, PointConstant.TAG_INVITED_FRIEND_TICKET)){
				MemberInfo tomi = daoService.getObject(MemberInfo.class, mi.getInviteid());
				if(tomi != null){
					Point p = new Point(mi.getInviteid());
					p.setTagid(memberid);
					p.setPoint(point);
					p.setTag(PointConstant.TAG_INVITED_FRIEND_TICKET);
					p.setReason("邀请好友购票成功");
					daoService.saveObject(p);
					tomi.addPointValue(p.getPoint());
					daoService.saveObject(tomi);
					Map otherMap = VmUtils.readJsonToMap(otherinfo);
					otherMap.put(PointConstant.TAG_INVITED_FRIEND_TICKET,Status.Y);
					mi.setOtherinfo(JsonUtils.writeMapToJson(otherMap));
					daoService.saveObject(mi);
					String url = "http://www.gewara.com/register.xhtml?from=" + mi.getInviteid() + "&encode=" + StringUtil.md5WithKey("" + mi.getInviteid());
					String body = "你邀请的" + mi.getNickname() + "在格瓦拉消费，因此你获得等值积分" + p.getPoint() + "（100积分抵值1元），邀请更多好友赚积分：";
					body += "<a href='" + url + "'>url</a>";
					userMessageService.sendSiteMSG(mi.getInviteid(), SysAction.ACTION_GETPOINT, mi.getId(), body);
				}
			}
		}
	}
	
	@Override
	public void addPointStats(Timestamp curTimestamp) {
		GewaConfig cfg = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_POINTSTATS_UPDATE);
		
		Timestamp updateTimestamp = cfg.getUpdatetime();
		curTimestamp = DateUtil.getBeginTimestamp(curTimestamp);
		String sql1 = "insert into WEBDATA.point_stats select to_char(p.addtime,'yyyy-mm-dd') adddate, " +
				"sum(case when p.pointvalue>0 then p.pointvalue else 0 end) as addpoint, " +
				"sum(case when p.pointvalue<0 then p.pointvalue else 0 end) as reducepoint, " +
				"p.tag as tag,'310000' as applycity, " +
				"(case when p.tag in ('pointexchange','trade','"+PointConstant.TAG_REFUND_TRADE+"','commentcinema','addinfo','correct','commenttheatre','accusation') then '06' else '12' end) as applydept, " +
				"(case when p.tag in ('pointexchange','trade','"+PointConstant.TAG_REFUND_TRADE+"','commentcinema','addinfo','correct','commenttheatre','accusation') then '99' else '01' end) as applytype, " +
				"'system' as flag " +
				"from WEBDATA.point p where p.stat_flag is null and p.addtime>=? and p.addtime<? " +
				"group by to_char(p.addtime,'yyyy-mm-dd'),p.tag";
		String sql2 = "insert into WEBDATA.point_stats select to_char(p.addtime,'yyyy-mm-dd') adddate, " +
				"sum(case when p.pointvalue>0 then p.pointvalue else 0 end) as addpoint, " +
				"sum(case when p.pointvalue<0 then p.pointvalue else 0 end) as reducepoint, p.tag as tag, " +
				"(case when p.tag='content' then regexp_substr(p.stat_flag,'[^:]+',1,3) else '310000' end) as applycity, " +
				"(case when p.tag='content' then regexp_substr(p.stat_flag,'[^:]+',1) else '12' end) as applydept, " +
				"(case when p.tag='content' then regexp_substr(p.stat_flag,'[^:]+',1,2) else '01' end) as applytype, " +
				"'manual' as flag " +
				"from WEBDATA.point p where p.stat_flag is not null and p.addtime>=? and p.addtime<? " +
				"group by to_char(p.addtime,'yyyy-mm-dd'),p.tag,p.stat_flag";
		int row1 = jdbcTemplate.update(sql1, updateTimestamp, curTimestamp);
		int row2 = jdbcTemplate.update(sql2, updateTimestamp, curTimestamp);
		dbLogger.warn("更新了" + (row1 + row2) + "条数据");
		cfg.setUpdatetime(curTimestamp);
		daoService.saveObject(cfg);
	}
	
	@Override
	public List<Point> getPointListByTradeNo(String tradeNo){
		String qry = "from Point where tag = ? and reason = ?";
		List<Point> result = hibernateTemplate.find(qry, PointConstant.TAG_TRADE, tradeNo);
		return result;
	}
	@Override
	public ErrorCode<Point> addNewTaskPoint(Long memberid, String tag){
		if(!MemberConstant.TASK_LIST.contains(tag) && !StringUtils.equals(tag, MemberConstant.TASK_CONFIRMREG) && !StringUtils.equals(tag, MemberConstant.TASK_BUYED_TICKET)) {
			return ErrorCode.getFailure("参数错误！");
		}
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, memberid);
		if(memberInfo == null)  return ErrorCode.getFailure("参数错误！");
		if(!memberInfo.getNewTaskList().contains(tag)) {
			return ErrorCode.getFailure("任务未完成！");
		}
		if(memberInfo.getOtherNewtaskList().contains(tag)) {
			return ErrorCode.getFailure("你已领取过任务奖励，不能重复领取！");
		}
		
		Point point = getPointByMemberiAndTagid(memberid, tag, null);
		if(point != null){
			memberInfo.finishTaskOtherInfo(tag);
			daoService.updateObject(memberInfo);
			return ErrorCode.getFailure("你已领取过任务奖励，不能重复领取！");
		}
		
		int pointvalue = 0;
		String reason = "";
		if(StringUtils.equals(tag, MemberConstant.TASK_SENDWALA)){//发哇啦
			pointvalue = PointConstant.SCORE_USERSENDWALA;
			reason = "完成新手任务之发布一条哇啦奖励" + PointConstant.SCORE_USERSENDWALA + "积分！";
		}else if(StringUtils.equals(tag, MemberConstant.TASK_UPDATE_HEAD_PIC)){//更新头像
			pointvalue = PointConstant.SCORE_USERHEADPIC;
			reason = "完成新手任务之更新头像奖励" + PointConstant.SCORE_USERHEADPIC + "积分！";
		}else if(StringUtils.equals(tag, MemberConstant.TASK_BINDMOBILE)){//绑手机
			pointvalue = PointConstant.SCORE_USERBINDMOBILE;
			reason = "完成新手任务之绑定手机奖励" + PointConstant.SCORE_USERBINDMOBILE + "积分！";
		}else if(StringUtils.equals(tag, MemberConstant.TASK_MOVIE_COMMENT)){//完成影评
			pointvalue = PointConstant.SCORE_USERMOVIECOMMENT;
			reason = "完成新手任务之发表影评奖励" + PointConstant.SCORE_USERMOVIECOMMENT + "积分！";
		}else if(StringUtils.equals(tag, MemberConstant.TASK_FIVEFRIEND)){//关注5个朋友
			pointvalue = PointConstant.SCORE_USERFIVEFRIEND;
			reason = "完成新手任务之关注好友奖励" + PointConstant.SCORE_USERFIVEFRIEND + "积分！";
		}else if(StringUtils.equals(tag, MemberConstant.TASK_JOINCOMMU)){//加入一个圈子
			pointvalue = PointConstant.SCORE_USERJOINCOMMU;
			reason = "完成新手任务之加入一个圈子奖励" + PointConstant.SCORE_USERJOINCOMMU + "积分！";
		}
		memberInfo.finishTaskOtherInfo(tag);
		point = this.addPointInfo(memberid, pointvalue, reason, tag);
		return ErrorCode.getSuccessReturn(point);
	}
	@Override
	public ErrorCode<String> validUsePoint(Long memberid) {
		GewaConfig gcc = baseDao.getObject(GewaConfig.class, ConfigConstant.POINT_LIMIT);
		List<Long> memberidList = BeanUtil.getIdList(gcc.getContent(), ",");
		if(memberidList.contains(memberid)) return ErrorCode.getFailure("你的账户限制使用积分，如有疑问请联系客服");
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode validWeiboPoint(Member member, boolean isApp){
		List<ShareMember>  shareMemberList = shareService.getShareMemberByMemberid(Arrays.asList(MemberConstant.SOURCE_SINA, MemberConstant.SOURCE_QQ), member.getId());
		if(shareMemberList.isEmpty()){
			String msg = isApp?"你还没有绑定微博账户,请去网站绑定":"你还没有绑定微博账户，请先绑定微博账户";
			return ErrorCode.getFailure(ApiConstant.CODE_NOT_EXISTS, msg);
		}else{
			for(ShareMember shareMember : shareMemberList) {
				if(StringUtils.equals(shareMember.getSource(), MemberConstant.SOURCE_SINA)){
					Map<String,String> otherMap = JsonUtils.readJsonToMap(shareMember.getOtherinfo());
					if(StringUtils.equals(otherMap.get("accessrights"), "0") || shareMember.getAddtime() == null || otherMap.get("expires") == null){
						String msg = isApp?"新浪微博账号已过期,请到网站绑定":"新浪微博账号已过期，请重新绑定！";
						return ErrorCode.getFailure(ApiConstant.CODE_WEIBO_EXPRIES, msg);
					}
					Timestamp addtime = shareMember.getAddtime();
					int expires = Integer.parseInt(otherMap.get("expires")+"") - 60;
					Timestamp duetime = DateUtil.addSecond(addtime, expires);
					if(!DateUtil.isAfter(duetime)){
						shareService.updateShareMemberRights(shareMember);
						String msg = isApp?"新浪微博账号已过期,请到网站绑定":"新浪微博账号已过期，请重新绑定！";
						return ErrorCode.getFailure(ApiConstant.CODE_WEIBO_EXPRIES, msg);
					}
				}
			}
		}
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode validWeiboPoint(Member member){
		return validWeiboPoint(member, false);
	}
}
