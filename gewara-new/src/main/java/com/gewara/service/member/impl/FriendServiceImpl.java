/**
 * 
 */
package com.gewara.service.member.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.SysAction;
import com.gewara.model.bbs.BlackMember;
import com.gewara.model.bbs.commu.CommuMember;
import com.gewara.model.user.EmailInvite;
import com.gewara.model.user.Friend;
import com.gewara.model.user.FriendInfo;
import com.gewara.model.user.HiddenMember;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.SysMessageAction;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.member.FriendService;
import com.gewara.support.ReadOnlyTemplate;
/**
 * @author hxs(ncng_2006@hotmail.com)
 * @since Jan 27, 2010 1:50:33 PM
 */
@Service("friendService")
public class FriendServiceImpl extends BaseServiceImpl implements FriendService {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public List<Friend> getFriendList(Long memberid, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Friend.class, "f");
		query.add(Restrictions.eq("f.memberfrom", memberid));
		query.addOrder(Order.desc("f.addtime"));
		
		DetachedCriteria subquery=DetachedCriteria.forClass(BlackMember.class, "bm");
		subquery.setProjection(Projections.property("bm.id"));
		subquery.add(Restrictions.eqProperty("f.memberto", "bm.memberId"));
		query.add(Subqueries.notExists(subquery));
		List<Friend> result = readOnlyTemplate.findByCriteria(query, from ,maxnum);
		return result;
	}
	
	@Override
	public List<Long> getFriendIdList(Long memberid, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Friend.class);
		query.add(Restrictions.eq("memberfrom", memberid));
		query.setProjection(Projections.property("memberto"));
		List<Long> list  = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}
	@Override
	public boolean isInvitedFriend(Long frommemberid,  Long tomemberid){
		DetachedCriteria query = DetachedCriteria.forClass(SysMessageAction.class);
		query.add(Restrictions.eq("frommemberid", frommemberid));
		query.add(Restrictions.eq("tomemberid", tomemberid));
		query.add(Restrictions.eq("status", SysAction.STATUS_APPLY));
		query.add(Restrictions.eq("action", SysAction.ACTION_APPLY_FRIEND_ADD));
		List<SysMessageAction> list = readOnlyTemplate.findByCriteria(query);
		return list.size()>0;
	}
	
	@Override 
	public boolean isFriend(Long memberidfrom, Long memberidto){
		DetachedCriteria query = DetachedCriteria.forClass(Friend.class);
		query.add(Restrictions.eq("memberfrom", memberidfrom));
		query.add(Restrictions.eq("memberto", memberidto));
		List<Friend> list = readOnlyTemplate.findByCriteria(query);
		return list.size()>0;
	}
	
	@Override
	public Integer getFriendCount(Long memberid){
		DetachedCriteria query = DetachedCriteria.forClass(Friend.class, "f");
		query.setProjection(Projections.rowCount());
		query.add(Restrictions.eq("f.memberfrom", memberid));
		
		DetachedCriteria subquery=DetachedCriteria.forClass(BlackMember.class, "bm");
		subquery.setProjection(Projections.property("bm.id"));
		subquery.add(Restrictions.eqProperty("f.memberto", "bm.memberId"));
		query.add(Subqueries.notExists(subquery));
		
		List result = readOnlyTemplate.findByCriteria(query);
		if(result.size() > 0) return Integer.parseInt(""+result.get(0));
		return 0;
	}
	@Override
	public void deleteCommueMember(Long memberid, Long commuid) {
		String hql="delete CommuMember where commuid=? and memberid=?";
		hibernateTemplate.bulkUpdate(hql,commuid,memberid);
	}
	@Override 
	public List<Long> getNotJoinCommuFriendIdList(Long memberid, Long commuid, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(Friend.class, "f");
		query.add(Restrictions.eq("memberfrom", memberid));
		
		DetachedCriteria subQuery=DetachedCriteria.forClass(CommuMember.class, "c");
		subQuery.add(Restrictions.eq("c.commuid", commuid));
		subQuery.add(Restrictions.eqProperty("f.memberto", "c.memberid"));
		subQuery.setProjection(Projections.property("c.memberid"));

		query.add(Subqueries.notExists(subQuery));
		query.setProjection(Projections.property("memberto"));
		List<Long> list  = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}
	@Override 
	public Integer getNotJoinCommuFriendCount(Long memberid, Long commuid){
		DetachedCriteria query = DetachedCriteria.forClass(Friend.class, "f");
		query.add(Restrictions.eq("memberfrom", memberid));
		
		DetachedCriteria subQuery=DetachedCriteria.forClass(CommuMember.class, "c");
		subQuery.add(Restrictions.eq("c.commuid", commuid));
		subQuery.add(Restrictions.eqProperty("f.memberto", "c.memberid"));
		subQuery.setProjection(Projections.property("c.memberid"));

		query.add(Subqueries.notExists(subQuery));
		query.setProjection(Projections.rowCount());
		List result  = readOnlyTemplate.findByCriteria(query);
		return new Integer(result.get(0)+"");
	}
	
	@Override 
	public Map isPrivate(Long friendid){
		Map map=new HashMap();
		//判断当前用户是否是所访问好友的朋友
		//boolean isfriend=isFriend(mymember.getId(),friendid);
		//加载所访问的好友的权限信息
		MemberInfo memberinfo=baseDao.getObject(MemberInfo.class, friendid);
		if(memberinfo!=null && StringUtils.isNotBlank(memberinfo.getRights())){
			String[] rights=memberinfo.getRights().split(",");
			map.put("rights", rights);
		}
		//map.put("isfriend",isfriend);
		return map;
	}
	@Override
	public List<HiddenMember> getHiddenMemberListByMemberid(Long memberid){
		DetachedCriteria query = DetachedCriteria.forClass(HiddenMember.class);
		query.add(Restrictions.eq("inviteid", memberid));
		List<HiddenMember> hiddenMemberList = readOnlyTemplate.findByCriteria(query);
		return hiddenMemberList;
	}
	
	@Override
	public List<FriendInfo> getFriendInfoListByAddMemberidAndMemberid(Long addmemberid, Long memberid){
		DetachedCriteria query = DetachedCriteria.forClass(FriendInfo.class);
		if(addmemberid!=null)query.add(Restrictions.eq("addmemberid", addmemberid));
		if(memberid!=null)query.add(Restrictions.eq("memberid", memberid));
		List<FriendInfo> friendInfoList = readOnlyTemplate.findByCriteria(query);
		return friendInfoList;
	}
	@Override
	public boolean isExistsEmail(Long memberid, String email) {
		DetachedCriteria query = DetachedCriteria.forClass(EmailInvite.class);
		query.add(Restrictions.eq("memberid",memberid));
		query.add(Restrictions.eq("email", email));
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty()) return false;
		return Integer.parseInt(""+list.get(0)) > 0;
	}
	@Override
	public Member checkUserName(String nickname) {
		String hql = "select m from Member m where m.nickname = ?";
		List list = readOnlyTemplate.find(hql,nickname);
		if(list.isEmpty()) return null;
		return (Member) list.get(0);
	}
	@Override
	public void deleteFriend(Long memberid1, Long memberid2) {
		String query = "delete Friend where (memberfrom=? and memberto=?) or (memberfrom=? and memberto=?)";
		hibernateTemplate.bulkUpdate(query, memberid1, memberid2, memberid2, memberid1);
	}
	@Override
	public List<Member> getFriendMemberList(Long memberid, int from, int maxnum) {
		String query = "from Member m where m.id in (select memberto from Friend f where memberfrom = ?)";
		List<Member> result = queryByRowsRange(query, from, maxnum, memberid);
		return result;
	}
	
}