package com.gewara.service.drama.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.hibernate.StaleObjectStateException;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.gewara.constant.DrawActicityConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PointConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.SysAction;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.common.VersionCtl;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Festival;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.member.PointService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.ticket.TicketDiscountService;
import com.gewara.support.ErrorCode;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;

@Service("drawActivityService")
public class DrawActivityServiceImpl extends BaseServiceImpl implements DrawActivityService {
	@Autowired@Qualifier("pointService")
	private PointService pointService;
	public void setPointService(PointService pointService) {
		this.pointService = pointService;
	}
	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;
	public void setElecCardService(ElecCardService elecCardService) {
		this.elecCardService = elecCardService;
	}
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	public void setUserMessageService(UserMessageService userMessageService) {
		this.userMessageService = userMessageService;
	}
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	
	@Autowired@Qualifier("ticketDiscountService")
	private TicketDiscountService ticketDiscountService;
	
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	
	@Override
	public Integer getDrawActivityCount() {
		DetachedCriteria query = DetachedCriteria .forClass(DrawActivity.class);
		query.add(Restrictions.eq("status", Status.Y_NEW));
		query.setProjection(Projections.rowCount());
		List list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}

	@Override
	public List<DrawActivity> getDrawActivityList(String order, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria .forClass(DrawActivity.class);
		query.add(Restrictions.eq("status", Status.Y_NEW));
		if(StringUtils.isNotBlank(order)){
			query.addOrder(Order.desc(order));
			query.addOrder(Order.asc("id"));
		} else query.addOrder(Order.desc("addtime"));
		List<DrawActivity> drawActivityList = hibernateTemplate.findByCriteria(query,from,maxnum);
		return drawActivityList;
	}

	@Override
	public List<Prize> getPrizeListByDid(Long did,String[] ptype) {
		DetachedCriteria query = DetachedCriteria.forClass(Prize.class);
		if(ptype!=null && ptype.length>0) query.add(Restrictions.in("ptype",ptype));
		query.add(Restrictions.eq("activityid", did));
		query.addOrder(Order.desc("addtime"));
		List<Prize> prizeList = hibernateTemplate.findByCriteria(query);
		return prizeList;
	}
	@Override
	public List<Prize> getAvailablePrizeList(Long did, int minleft) {
		String query = "from Prize where activityid = ? and pnumber - psendout >? order by addtime";
		List<Prize> prizeList = hibernateTemplate.find(query, did, minleft);
		return prizeList;
	}

	@Override
	public Integer getSendOutPrizeCount(Long activityid, Long prizeid) {
		DetachedCriteria query = DetachedCriteria.forClass(WinnerInfo.class);
		query.add(Restrictions.eq("activityid", activityid));
		query.add(Restrictions.eq("prizeid",prizeid));
		query.add(Restrictions.eq("tag", WinnerInfo.TAG_SYSTEM));
		query.setProjection(Projections.rowCount());
		List list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}

	@Override
	public Integer getWinnerCount(Long activityid, List<Long> prizeid,Timestamp startTime,Timestamp endTime,String tag,Long memberid,String mobile,String nickname) {
		DetachedCriteria query = DetachedCriteria.forClass(WinnerInfo.class,"w");
		if(activityid !=null)query.add(Restrictions.eq("w.activityid", activityid));
		if(prizeid !=null && !prizeid.isEmpty()){query.add(Restrictions.in("w.prizeid", prizeid));}
		if(memberid != null) query.add(Restrictions.eq("w.memberid",memberid));
		if(startTime!=null && endTime!=null){
			query.add(Restrictions.between("w.addtime", startTime, endTime));
		}else if(startTime !=null){
			query.add(Restrictions.ge("w.addtime", startTime));
		}else if(endTime != null){
			query.add(Restrictions.le("w.addtime", endTime));
		}
		if(StringUtils.isNotBlank(mobile)) query.add(Restrictions.eq("mobile",mobile));
		if(StringUtils.isNotBlank(nickname)) query.add(Restrictions.like("nickname", nickname,MatchMode.ANYWHERE));
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("w.tag", tag));
		query.setProjection(Projections.rowCount());
		List list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}

	@Override
	public List<WinnerInfo> getWinnerList(Long activityid,List<Long> prizeid,Timestamp startTime,Timestamp endTime,
			String tag,Long memberid,String mobile,String nickname,int from,int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(WinnerInfo.class,"w");
		if(activityid != null)query.add(Restrictions.eq("w.activityid", activityid));
		if(prizeid != null && !prizeid.isEmpty())query.add(Restrictions.in("w.prizeid", prizeid));
		if(memberid != null) query.add(Restrictions.eq("w.memberid",memberid));
		if(startTime!=null && endTime!=null){
			query.add(Restrictions.between("w.addtime", startTime, endTime));
		}else if(startTime !=null){
			query.add(Restrictions.ge("w.addtime", startTime));
		}else if(endTime != null){
			query.add(Restrictions.le("w.addtime", endTime));
		}
		if(StringUtils.isNotBlank(mobile)) query.add(Restrictions.eq("mobile",mobile));
		if(StringUtils.isNotBlank(nickname)) query.add(Restrictions.like("nickname", nickname,MatchMode.ANYWHERE));
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("w.tag", tag));
		query.addOrder(Order.desc("w.addtime"));
		List<WinnerInfo> winnerList = hibernateTemplate.findByCriteria(query,from,maxnum);
		return winnerList;
	}

	@Override
	public List<Map> getPrizeCountInfo(Long activityid,Timestamp startTime,Timestamp endTime) {
		List<Map> list = null;
		String hql = "select new map(w.prizeid as pid,count(w.prizeid) as pcount) from WinnerInfo w where w.tag = ? and w.activityid =? ";
		String group = " group by w.activityid,w.prizeid";
		if(startTime !=null && endTime !=null){
			hql += " and w.addtime >= ? and w.addtime <= ?";
			hql += group;
			list = hibernateTemplate.find(hql,WinnerInfo.TAG_SYSTEM,activityid,startTime,endTime);
			return list;
		}else if(startTime !=null){
			hql += " and w.addtime >= ?";
			hql += group;
			list = hibernateTemplate.find(hql,WinnerInfo.TAG_SYSTEM,activityid,startTime);
			return list;
		}else if(endTime !=null){
			hql += " and w.addtime <= ?";
			hql += group;
			list = hibernateTemplate.find(hql,WinnerInfo.TAG_SYSTEM,activityid,endTime);
			return list;
		}
		hql += group;
		list = hibernateTemplate.find(hql,WinnerInfo.TAG_SYSTEM,activityid);
		return list;
	}

	@Override
	public Integer getJoinDrawActivityCount(Long activityid) {
		DetachedCriteria query = DetachedCriteria.forClass(WinnerInfo.class);
		query.add(Restrictions.eq("activityid",activityid));
		query.setProjection(Projections.rowCount());
		List list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}

	@Override
	public Integer getCurDrawActivityNum(DrawActivity da, Long memberid, int totalChance){
		Timestamp from = DateUtil.getCurTruncTimestamp();
		Timestamp to = DateUtil.getMillTimestamp();
		Integer today = getMemberWinnerCount(memberid, da.getId(), from, to);
		int result = totalChance - today;
		return result>0?result:0;
	}
	
	@Override
	public Integer getCurChanceNum(DrawActivity da, Long memberid){
		Timestamp from = DateUtil.getCurTruncTimestamp();
		Timestamp to = DateUtil.getMillTimestamp();
		Integer today = getMemberWinnerCount(memberid, da.getId(), from, to);
		Integer extra = getExtraChanceNum(da, memberid);
		Integer used = getUsedChanceNum(da, memberid);
		Integer result = extra - used + (today==0?1:0);
		return result>0?result:(today==0?1:0);
	}
	@Override
	public Integer getExtraChanceNum(DrawActivity da, Long memberid) {
		DetachedCriteria toQuery = DetachedCriteria.forClass(Member.class,"m");
		toQuery.add(Restrictions.isNotNull("m.mobile"));
		DetachedCriteria opiQuery = DetachedCriteria.forClass(MemberInfo.class,"mi");
		opiQuery.add(Restrictions.eqProperty("mi.id", "m.id"));
		opiQuery.add(Restrictions.eq("mi.invitetype", da.getTag()));
		opiQuery.add(Restrictions.eq("mi.inviteid",memberid));
		opiQuery.setProjection(Projections.property("mi.id"));
		toQuery.add(Subqueries.propertyIn("m.id", opiQuery));
		toQuery.setProjection(Projections.rowCount());
		List<Long> list = hibernateTemplate.findByCriteria(toQuery);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	@Override
	public Integer getUsedChanceNum(DrawActivity da, Long memberid){
		String sql = "SELECT SUM(TOTAL) FROM (SELECT TO_CHAR(ADDTIME,'YYYYMMDD') UDATE, COUNT(*)-1 AS TOTAL " +
				"FROM WEBDATA.WINNERINFO WHERE ACTIVITYID=? AND MEMBERID=? GROUP BY TO_CHAR(ADDTIME,'YYYYMMDD')) TMP";
		Integer result = jdbcTemplate.queryForInt(sql, da.getId(), memberid);
		return result;
	}
	/**
	 * @param da
	 * @param vc
	 * @param scalper true表示黄牛
	 * @return
	 */
	@Override
	public Prize runDrawPrize(DrawActivity da, VersionCtl vc, boolean scalper){
		vc.setCtldata(StringUtil.getRandomString(20));
		baseDao.saveObject(vc);
		List<Prize> prizeList = getAvailablePrizeList(da.getId(), 5);
		Prize prize = runDrawPrize(prizeList);
		if(prize!=null && scalper && StringUtils.equals(prize.getTopPrize(), "Y")){//黄牛中大奖，重抽！
			prize = runDrawPrize(prizeList);
			if(prize!=null && StringUtils.equals(prize.getTopPrize(), "Y")){//黄牛又中大奖，再重抽！！
				prize = runDrawPrize(prizeList);
			}
		}
		return prize;
	}
	private Prize runDrawPrize(List<Prize> prizeList){
		Integer sumPrizeCount = 0;
		Map<Prize/*prizeid*/,Integer[]> m = new LinkedHashMap<Prize, Integer[]>();
		if(prizeList.isEmpty()) return null;
		if(prizeList.size()==1) return prizeList.get(0);
		int i = 0;
		int j = 0;
		for (Prize prize: prizeList){
			Integer count = prize.getChancenum();
			if(count <=0 || prize.getLeavenum() <=5 ) continue;
			Integer[] numberValue = new Integer[2];
			j = i + count;
			numberValue[0]=i+1;
			numberValue[1]=j;
			i = j;
			sumPrizeCount +=count;
			m.put(prize, numberValue);
		}
		Integer random = RandomUtils.nextInt(sumPrizeCount)+1;
		for(Prize prize : m.keySet()){
			Integer[] numberValue = m.get(prize);
			if(random>=numberValue[0]&&random<=numberValue[1]){
				return prize;
			}
		}
		return null;
	}
	private void updatePrize(Prize prize, VersionCtl vc) throws StaleObjectStateException{
		prize.addPsendout();
		baseDao.updateObject(prize); //更新奖品已出数量
		vc.setCtldata(StringUtil.getRandomString(20));
		baseDao.saveObject(vc);

	}
	@Override
	public Integer getMemberWinnerCount(Long memberid, Long activityid, Timestamp startTime, Timestamp endTime) {
		String query = "select count(id) from WinnerInfo where activityid =? and memberid =? and addtime>? and addtime<?";
		Integer count = new Integer("" + hibernateTemplate.find(query, activityid, memberid, startTime, endTime).get(0));
		return count;
	}
	@Override
	public WinnerInfo addWinner(Prize prize, Long memberId, String nickname, String mobile, String ip){
		//添加获奖人信息
		WinnerInfo winnerInfo = new WinnerInfo(prize.getActivityid(), prize.getId(), mobile,
				new Timestamp(System.currentTimeMillis()), WinnerInfo.TAG_SYSTEM);
		winnerInfo.setMemberid(memberId);
		winnerInfo.setNickname(nickname);
		winnerInfo.setIp(ip);
		baseDao.saveObject(winnerInfo);
		return winnerInfo;
	}
	//发送奖品信息
	@Override
	public SMSRecord sendPrize(Prize prize, WinnerInfo winner, boolean sendsms) {
		if(winner.getStatus().equals(Status.Y)) throw new IllegalArgumentException("奖品已送出！");
		String temp = "";
		if(PayConstant.CARDTYPE_A.equals(prize.getPtype()) || 
				PayConstant.CARDTYPE_D.equals(prize.getPtype())){//兑换券
			ErrorCode result = elecCardService.randomSendPrize(prize, winner);
			if(!result.isSuccess()){
				dbLogger.warn(result.getMsg());
				return null;
			}
		}else if(Prize.PRIZE_TYPE_SPDISCOUNT.equals(prize.getPtype())){//特价活动
			ErrorCode result = ticketDiscountService.randomSendPrize(prize, winner);
			if(!result.isSuccess()){
				dbLogger.warn(result.getMsg());
				return null;
			}
		}else if(PayConstant.CARDTYPE_POINT.equals(prize.getPtype())){//积分
			pointService.addPointInfo(winner.getMemberid(), Integer.valueOf(prize.getPvalue()+""),prize.getPlevel(), PointConstant.TAG_CONTENT);
		}else if(Prize.PRIZE_TYPE_DRAMA.equals(prize.getPtype())){//话剧票
			winner.setRemark("话剧票");
			temp = "Drama";
		}else if(Prize.PRIZE_REMARK.equals(prize.getPtype())){//其他
			winner.setRemark("其他");
			temp = "Remark";
		}else if(Prize.PRIZE_TYPE_WAIBI.equals(prize.getPtype())){//瓦币
			winner.setRemark("瓦币");
			temp = "Wabi";
			Member member=hibernateTemplate.get(Member.class, winner.getMemberid());
			if(null!=member){
				//增加充值记录,并为修改会员账户信息
				paymentService.addWaiBiByDrawActivity(String.valueOf(prize.getActivityid()), member, Integer.valueOf(prize.getPvalue()+""));
			}
		}
		winner.setStatus(Status.Y);
		baseDao.saveObject(winner);
		// 20130823短信可以发，但是获奖的卡号密码还是不要发出去，站内信也要发.....
	//	if(!PayConstant.CARDTYPE_A.equals(prize.getPtype()) && !PayConstant.CARDTYPE_D.equals(prize.getPtype())){
			Map<String,String> otherinfoMap = JsonUtils.readJsonToMap(prize.getOtherinfo());
			String sysMessage = otherinfoMap.get(DrawActicityConstant.TASK_SYS_MESSAGE);

			if(StringUtils.isNotBlank(sysMessage) && winner.getMemberid() != null) {
				userMessageService.sendSiteMSG(winner.getMemberid(),  SysAction.STATUS_RESULT, prize.getActivityid(), sysMessage);
			}
			if(sendsms && !PayConstant.CARDTYPE_POINT.equals(prize.getPtype()) && StringUtils.isNotBlank(prize.getMsgcontent()) && StringUtils.isNotBlank(winner.getMobile())){
				Timestamp cur = new Timestamp(System.currentTimeMillis());
				if (Prize.PRIZE_TYPE_SPDISCOUNT.equals(prize.getPtype())) {
					prize.setMsgcontent(prize.getMsgcontent() + winner.getRemark() );
				}
				SMSRecord sms = new SMSRecord(null, temp+DateUtil.format(cur, "yyyyMMddHHmmss"), winner.getMobile(), prize.getMsgcontent(),
						cur, DateUtil.addDay(cur, 2), SmsConstant.SMSTYPE_ACTIVITY);
				baseDao.saveObject(sms);
				return sms;
			}
	//	}
		return null;
	}
	
	@Override
	public ErrorCode sendNewTaskCardPrize(MemberInfo info, Member member) {
		if(!info.isFinishedTask(MemberConstant.TASK_CONFIRMREG) || !info.isFinishedTask(MemberConstant.TASK_BINDMOBILE)) return ErrorCode.getFailure("对不起，你必须完成绑定邮箱，和绑定手机才可以领取!");
		if(StringUtils.equals(JsonUtils.getJsonValueByKey(info.getOtherinfo(), MemberConstant.TASK_BUYED_TICKET), Status.Y)) return ErrorCode.getFailure("你已经领取优惠券,请不要重复领取!");
		if(!info.isFinishedTask(MemberConstant.TASK_BUYED_TICKET)) return ErrorCode.getFailure("任务未完成，优惠券领取失败!");
		DrawActivity da = baseDao.getObjectByUkey(DrawActivity.class, "tag", DrawActivity.TAG_NEWTASK, true);
		if(da==null) return ErrorCode.getFailure("奖品活动不存在，请联系客服！");
		WinnerInfo winner = getWinnerInfoByMemberid(da.getId(), member.getId());
		Prize prize = null;
		if(winner == null){
			List<Prize> prizeList = getAvailablePrizeList(da.getId(), 5);
			prize = runDrawPrize(prizeList);
			if(prize==null) return ErrorCode.getFailure("奖品不存在，请联系客服！");
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			winner = new WinnerInfo(prize.getActivityid(), prize.getId(), null, cur, WinnerInfo.TAG_SYSTEM);
			winner.setNickname(member.getNickname());
			winner.setMemberid(info.getId());
			winner.setRemark(DrawActivity.TAG_NEWTASK);
			baseDao.saveObject(winner);
		}else{
			prize = baseDao.getObject(Prize.class, winner.getPrizeid());
		}
		info.finishTaskOtherInfo(MemberConstant.TASK_BUYED_TICKET); 
		baseDao.saveObjectList(winner, info);
		sendPrize(prize, winner, false);
		return ErrorCode.SUCCESS;
	}

	@Override
	public int getWinnerCountByMobile(Long activityid, String mobile) {
		String query = "select count(id) from WinnerInfo where activityid=? and mobile=?";
		int result = Integer.parseInt(""+hibernateTemplate.find(query, activityid, mobile).get(0));
		return result;
	}

	@Override
	public WinnerInfo getWinnerInfoByMemberid(Long activityid, Long memberid) {
		List<WinnerInfo> list = getWinnerInfoByMemberid(activityid, memberid, 0, 1);
		return list.isEmpty()?null:list.get(0);
	}
	@Override
	public List<WinnerInfo> getWinnerInfoByMemberid(Long activityid, Long memberid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(WinnerInfo.class);
		query.add(Restrictions.eq("activityid",activityid));
		query.add(Restrictions.eq("memberid",memberid));
		List<WinnerInfo> list = hibernateTemplate.findByCriteria(query, from, maxnum);
		return list;
	}
	
	@Override
	public boolean isSendPrize(String mobile,Long memberid, Long activityid, Timestamp gainPrizeTime) {
		DetachedCriteria query = DetachedCriteria.forClass(WinnerInfo.class);
		query.add(Restrictions.or(Restrictions.eq("mobile",mobile),Restrictions.eq("memberid",memberid)));
		query.add(Restrictions.eq("activityid",activityid));
		if(gainPrizeTime != null){
			Timestamp startTime = DateUtil.getBeginningTimeOfDay(gainPrizeTime);
			Timestamp endTime = DateUtil.getLastTimeOfDay(gainPrizeTime);
			query.add(Restrictions.between("addtime", startTime, endTime));
		}
		query.setProjection(Projections.rowCount());
		List list = hibernateTemplate.findByCriteria(query);
		return Integer.parseInt(list.get(0)+"") > 0? true:false;
	}

	@Override
	public Integer getInviteMemberCount(Long memberId, String invitetype, boolean validMobile,
			Timestamp startTime, Timestamp endTime) {
		String hql = "";
		if(validMobile){
			hql += "select count(m.id) from Member m where exists( select mi.id from MemberInfo mi where mi.inviteid=? ";
		}else{
			hql += " select count(mi.id) from MemberInfo mi where mi.inviteid=? ";
		}
		List params = new ArrayList();
		params.add(memberId);
		if(startTime != null){
			hql += " and mi.addtime>? and mi.addtime<? ";
			params.add(startTime);
			if(endTime == null) endTime = DateUtil.getCurFullTimestamp();
			params.add(endTime);
		}
		if(StringUtils.isNotBlank(invitetype)){
			hql += " and mi.invitetype=? ";
			params.add(invitetype);
		}
		if(validMobile){
			hql += " and mi.id=m.id  ) and m.mobile is not null ";	
		}
		List count = hibernateTemplate.find(hql, params.toArray());
		return count.isEmpty()?0:Integer.valueOf(count.get(0)+"");
	}
	
	@Override
	public List<Long> getInviteMemberList(Long memberId, String invitetype, boolean validMobile, Timestamp starttime, Timestamp endtime){
		String hql = "";
		if(validMobile){
			hql += "select m.id from Member m where exists( ";
		}
		hql += " select mi.id from MemberInfo mi where mi.inviteid=? ";
		List params = new ArrayList();
		params.add(memberId);
		if(starttime != null){
			hql += " and mi.addtime>? and mi.addtime<? ";
			params.add(starttime);
			if(endtime == null) endtime = DateUtil.getCurFullTimestamp();
			params.add(endtime);
		}
		if(StringUtils.isNotBlank(invitetype)){
			hql += " and mi.invitetype=? ";
			params.add(invitetype);
		}
		if(validMobile){
			hql += " and mi.id=m.id ) and m.mobile is not null ";	
		}
		List<Long> idList = hibernateTemplate.find(hql, params.toArray());
		return idList;
	}

	@Override
	public Integer getInviteOrderNum(DrawActivity da, Long memberid) {
		String hql = "select mi.id from MemberInfo mi where exists (select m.id from Member m " +
				"where m.id = mi.id and mi.inviteid = ? and mi.addtime between ? and ?) and mi.invitetype = ?";
		List<Long> memberIdList = hibernateTemplate.find(hql,memberid,da.getStarttime(),da.getEndtime(),da.getTag());
		DetachedCriteria query = DetachedCriteria.forClass(GewaOrder.class);
		if(memberIdList.isEmpty()) query.add(Restrictions.eq("memberid", memberid));
		else query.add(Restrictions.or(Restrictions.in("memberid",memberIdList), Restrictions.eq("memberid",memberid)));
		query.add(Restrictions.between("addtime", da.getStarttime(), da.getEndtime()));
		query.add(Restrictions.like("status",OrderConstant.STATUS_PAID,MatchMode.START));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		return Integer.valueOf(result.get(0)+"");
	}
	/*@Override
	public ErrorCode<WinnerInfo> baseClickDraw(DrawActivity da, VersionCtl mvc, boolean scalper, Member member, BuziCallback callback){
		callback.call();
		return baseClickDraw(da, mvc, scalper, member.getId(), member.getNickname(), member.getMobile());
	}*/

	@Override
	public ErrorCode<WinnerInfo> baseClickDraw(DrawActivity da, VersionCtl mvc, boolean scalper, Member member){
		return baseClickDraw(da, mvc, scalper, member.getId(), member.getNickname(), member.getMobile());
	}
	@Override
	public ErrorCode<WinnerInfo> baseClickDraw(DrawActivity da, VersionCtl mvc, boolean scalper, Long memberId, String nickname, String mobile){
		return baseClickDraw(da, mvc, scalper, memberId, nickname, mobile, null);
	}
	/**
	 * @param da
	 * @param mvc
	 * @param scalper true 表示 黄牛
	 * @param memberId
	 * @param nickname
	 * @param mobile
	 * @param ip
	 * @return
	 */
	@Override
	public ErrorCode<WinnerInfo> baseClickDraw(DrawActivity da, VersionCtl mvc, boolean scalper, Long memberId, String nickname, String mobile, String ip){
		Map<String, String> otherinfoMap = VmUtils.readJsonToMap(da.getOtherinfo());
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_POINT))){
			MemberInfo info = baseDao.getObject(MemberInfo.class, memberId);
			if(info.getPointvalue() < Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_POINTVALUE))) {
				return ErrorCode.getFailure("nopoint", "您的积分不足！");
			}
			pointService.addPointInfo(memberId, -Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_POINTVALUE)),"参加活动扣去积分", PointConstant.TAG_CONTENT);
		}		
		
		Timestamp today = new Timestamp(System.currentTimeMillis());
		if(da.getStarttime().getTime() <= today.getTime() && today.getTime() <= da.getEndtime().getTime()){
			//计算概率
			Prize prize = runDrawPrize(da, mvc, scalper);//抽出奖品
			if(prize==null){
				//奖品已抽完
				return ErrorCode.getFailure("noprize", "本次抽奖活动奖品已经抽完！请关注下一期活动......");
			}
			VersionCtl prvc = gainPrizeVc(prize.getId());
			updatePrize(prize, prvc);
			WinnerInfo winner = addWinner(prize, memberId, nickname, mobile, ip);//给奖品
			return ErrorCode.getSuccessReturn(winner);
		}else{
			if(da.getStarttime().getTime()>today.getTime()){
				return ErrorCode.getFailure("noprize", "抽奖活动暂未开始！请耐心等候......");
			}else if(da.getEndtime().getTime()<today.getTime()){
				return ErrorCode.getFailure("noprize", "本次抽奖活动已结束！感谢您的参与！");
			}
		}
		return ErrorCode.getFailure("noprize", "抽奖已结束！");
	}
	
	@Override
	public VersionCtl gainMemberVc(String memberId){
		VersionCtl mvc = baseDao.getObject(VersionCtl.class, "mvc" + memberId);
		if(mvc == null) {
			mvc = new VersionCtl("mvc" + memberId);
			baseDao.saveObject(mvc);
		}
		return mvc;
	}
	@Override
	public VersionCtl gainPrizeVc(Long prizeId){
		VersionCtl vc = baseDao.getObject(VersionCtl.class, "pr" + prizeId);
		if(vc == null) {
			vc = new VersionCtl("pr" + prizeId);
			baseDao.saveObject(vc);
		}
		return vc;
	}

	@Override
	public Prize clickPointPrize(Festival festival, Member member) {
		DrawActivity da = baseDao.getObject(DrawActivity.class, festival.getDrawid());
		Prize prize = null;
		if(da != null){
			Timestamp today = new Timestamp(System.currentTimeMillis());
			if(da.getStarttime().getTime() <= today.getTime() && today.getTime() <= da.getEndtime().getTime()){
				try{
					VersionCtl mvc = baseDao.getObject(VersionCtl.class, "mvc" + member.getId());
					if(mvc == null) {
						mvc = new VersionCtl("mvc" + member.getId());
						baseDao.saveObject(mvc);
					}
					prize = runDrawPrize(da, mvc, false);//抽出奖品
					if(prize==null){
						//奖品已抽完
						dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_LOGIC, "奖品已经全部送出！请添加！");
						return null;
					}
					VersionCtl vc = baseDao.getObject(VersionCtl.class, "pr" + prize.getId());
					if(vc == null) {
						vc = new VersionCtl("pr" + prize.getId());
						baseDao.saveObject(vc);
					}
					updatePrize(prize, vc);
				}catch(StaleObjectStateException e){
					dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_LOGIC, StringUtil.getExceptionTrace(e, 200));
					return null;
				}catch(HibernateOptimisticLockingFailureException e){
					dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_LOGIC, StringUtil.getExceptionTrace(e, 200));
					return null;
				}
				WinnerInfo winner = addWinner(prize, member.getId(), member.getNickname(), member.getMobile(), null);//给奖品
				sendPrize(prize, winner, false);
				return prize;
			} else{
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_LOGIC, "积分活动设置时间有误，联系管理员重新设置");
				return null;
			}
		}else{
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_LOGIC, "抽奖活动不存在，联系管理员重新设置");
			return null;
		}
	}

	@Override
	public ErrorCode<SMSRecord> getColorBall(Long memberid, String mobile , String msgContent) {
		int movieCount = orderQueryService.getMemberTicketCountByMemberid(memberid, DateUtil.parseTimestamp("2013-11-30", "yyyy-MM-dd"),
				DateUtil.parseTimestamp("2013-12-25 23:59:59", "yyyy-MM-dd hh:mm:ss"), OrderConstant.STATUS_PAID_SUCCESS, null);
		if (movieCount == 0 ) {
			return ErrorCode.getFailure("noticket", "请先购票！");
		}
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		SMSRecord sms = new SMSRecord(null, "Remark"+DateUtil.format(cur, "yyyyMMddHHmmss"), mobile, msgContent,
				cur, DateUtil.addDay(cur, 2), SmsConstant.SMSTYPE_ACTIVITY);
		return ErrorCode.getSuccessReturn(sms);
	}

	@Override
	public ErrorCode<SMSRecord> getCouponCode(Long memberid, String mobile, String msgContent) {
		MemberInfo info = baseDao.getObject(MemberInfo.class, memberid);
		if(info.getPointvalue() < 100) {
			return ErrorCode.getFailure("nopoint", "您的积分不足！");
		}
		pointService.addPointInfo(memberid, -100,"Origins官网购物码领取-扣去积分", PointConstant.TAG_CONTENT);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		SMSRecord sms = new SMSRecord(null, "Remark"+DateUtil.format(cur, "yyyyMMddHHmmss"), mobile, msgContent,
				cur, DateUtil.addDay(cur, 2), SmsConstant.SMSTYPE_ACTIVITY);
		return ErrorCode.getSuccessReturn(sms);
	}
}
