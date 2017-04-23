package com.gewara.service.bbs.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.list.UnmodifiableList;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import com.gewara.constant.DiaryConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.helper.sys.RelateClassHelper;
import com.gewara.model.bbs.Accusation;
import com.gewara.model.bbs.Bkmember;
import com.gewara.model.bbs.BlackMember;
import com.gewara.model.bbs.BlogData;
import com.gewara.model.bbs.BlogDataEveryDay;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.movie.Cinema;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.Treasure;
import com.gewara.mongo.MongoService;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.XSSFilter;

@Service("blogService")
public class BlogServiceImpl extends BaseServiceImpl implements BlogService, InitializingBean {
	private int night_begin = 23;	//晚上23点
	private int night_end = 9;		//早上9点
	private int frequency = 1800;	//默认30分钟
	private int times = 30;			//默认30次
	
	private Integer unbindUsrLimitEgg = 1;
	private Integer bindUsrLimitEgg = 50;
	@Autowired@Qualifier("cacheService")
	public CacheService cacheService;
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	
	/****************************************************************************
	 * 版主、成员
	 ***************************************************************************/
	@Override
	public List<Bkmember> getBkmemberList(String tag, Long relatedid, int role, boolean includeSub, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Bkmember.class);
		if (StringUtils.isBlank(tag)) {
			query.add(Restrictions.isNull("tag"));
			query.add(Restrictions.isNull("relatedid"));
		} else {
			query.add(Restrictions.like("tag", tag, MatchMode.ANYWHERE));
			if (relatedid != null)
				query.add(Restrictions.eq("relatedid", relatedid));
			else if (!includeSub)
				query.add(Restrictions.isNull("relatedid"));
		}
		query.add(Restrictions.ge("role", role));
		query.addOrder(Order.desc("addtime"));
		List<Bkmember> result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}

	@Override
	public List<Bkmember> getBkmemberListByMemberId(Long memberId) {
		final String qstr = "from Bkmember b where b.memberid = ? ";
		List<Bkmember> result = readOnlyTemplate.find(qstr, memberId);
		return result;
	}

	@Override
	public List<Bkmember> getBanzhuList(String tag, Long relatedid) {
		DetachedCriteria query = DetachedCriteria.forClass(Bkmember.class);
		if (StringUtils.isBlank(tag)) {
			query.add(Restrictions.isNull("tag"));
			query.add(Restrictions.isNull("relatedid"));
		} else {
			query.add(Restrictions.eq("tag", tag));
			if (relatedid != null)
				query.add(Restrictions.or(Restrictions.eq("relatedid", relatedid), Restrictions.isNull("relatedid")));
			else
				query.add(Restrictions.isNull("relatedid"));
		}
		query.add(Restrictions.ge("role", Bkmember.ROLE_BANZHU));
		List<Bkmember> result = readOnlyTemplate.findByCriteria(query);
		return result;
	}

	@Override
	public int getMaxRights(String tag, Long relatedid, Long ownerId, Member member) {
		if (member == null)
			return 0;
		if (ownerId != null && member.getId().equals(ownerId))
			return Bkmember.ROLE_OWNER;
		int maxRights = 1;
		// if(StringUtils.isBlank(tag)) return maxRights;
		List<Bkmember> bkmemberList = getBkmemberListByMemberId(member.getId());
		for (Bkmember bkmember : bkmemberList) {
			if (bkmember.hasManagerRights(tag, relatedid))
				return Bkmember.ROLE_MANAGER;
			if (bkmember.hasRights(tag, relatedid))
				maxRights = Bkmember.ROLE_BANZHU;
		}
		return maxRights;
	}

	@Override
	public Bkmember getBkmember(Member member, String tag, Long relatedid) {
		List<Bkmember> result = null;
		if (relatedid == null) {
			String query = "from Bkmember b where b.memberid = ? and b.tag = ? and b.relatedid is null";
			result = readOnlyTemplate.find(query, member.getId(), tag);
		} else {
			String query = "from Bkmember b where b.memberid = ? and b.tag = ? and b.relatedid = ? ";
			result = readOnlyTemplate.find(query, member.getId(), tag, relatedid);
		}
		if (result == null || result.isEmpty())
			return null;
		return result.get(0);
	}

	@Override
	public List<BlackMember> getBlackMemberList(Long memberId, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(BlackMember.class);
		if (memberId != null)
			query.add(Restrictions.eq("memberId", memberId));
		else
			query.addOrder(Order.desc("addtime"));
		return readOnlyTemplate.findByCriteria(query, from, maxnum);
	}

	@Override
	public List<BlackMember> getBlackMemberList(Long memberId, String nickname, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(BlackMember.class, "bl");

		DetachedCriteria subquery = DetachedCriteria.forClass(Member.class, "m");
		if (memberId != null)
			subquery.add(Restrictions.eq("m.id", memberId));
		if (nickname != null)
			subquery.add(Restrictions.like("m.nickname", nickname, MatchMode.ANYWHERE));
		subquery.setProjection(Projections.property("m.id"));
		subquery.add(Restrictions.eqProperty("bl.memberId", "m.id"));
		query.add(Subqueries.exists(subquery));
		query.addOrder(Order.desc("bl.addtime"));

		return readOnlyTemplate.findByCriteria(query, from, maxnum);
	}

	@Override
	public boolean isBlackMember(Long memberId) {
		List<BlackMember> blackList = getBlackMemberList(memberId, -1, -1);
		if (blackList.isEmpty())
			return false;
		return true;
	}

	@Override
	public Integer getBlackMembertCount(String nickname) {
		DetachedCriteria query = DetachedCriteria.forClass(BlackMember.class, "bl");
		if(StringUtils.isNotBlank(nickname)){
			DetachedCriteria subquery = DetachedCriteria.forClass(Member.class, "m");
			subquery.add(Restrictions.like("m.nickname", nickname, MatchMode.ANYWHERE));
			subquery.setProjection(Projections.property("m.id"));
			subquery.add(Restrictions.eqProperty("bl.memberId", "m.id"));
			query.add(Subqueries.exists(subquery));
		}
		query.setProjection(Projections.rowCount());
		List<Integer> count = readOnlyTemplate.findByCriteria(query);
		return count.size() == 1 ? new Integer(count.get(0) + "") : 0;
	}

	@Override
	public List<Commu> getCommunityList(String order, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Commu.class);
		query.addOrder(Order.desc(order));
		query.addOrder(Order.desc("updatetime"));
		List<Commu> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}

	@Override
	public Integer getCommunityCount() {
		return baseDao.getObjectCount(Commu.class);
	}

	@Override
	public Integer getAccusationCount() {
		DetachedCriteria query = DetachedCriteria.forClass(Accusation.class);
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty())
			return 0;
		return Integer.parseInt(list.get(0) + "");
	}

	@Override
	public List<Accusation> getAccusationList(int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Accusation.class);
		query.addOrder(Order.desc("addtime"));
		List<Accusation> accList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return accList;
	}
	private List<String> filterList;
	private List<String> manualFilterList;
	private List<String> memberRegisterFilterList;

	@Override
	public boolean rebuildFilterKey() {
		String keywords = (String)mongoService.getPrimitiveObject(ConfigConstant.KEY_FIXEDKEYWORDS);
		if(StringUtils.length(keywords) < 2000) throw new IllegalArgumentException("rebuildFilterKey Error!!");
		String[] tmps = keywords.split(",");
		List<String> tmpFilters = new ArrayList<String>();
		for (int i = 0; i < tmps.length; i++) {
			tmpFilters.add("(" + tmps[i].trim() + ")");
		}
		filterList = UnmodifiableList.decorate(tmpFilters);
		return true;
	}

	@Override
	public boolean rebuildManualFilterKey() {
		List<String> filters = new ArrayList<String>();
		String keywords = (String)mongoService.getPrimitiveObject(ConfigConstant.KEY_MANUKEYWORDS);
		if(StringUtils.isBlank(keywords))  throw new IllegalArgumentException("rebuildManualFilterKey Error!!");
		String[] filterarrs = keywords.split(",");
		for (int i = 0; i < filterarrs.length; i++) {
			filters.add("(" + filterarrs[i].trim() + ")");
		}
		manualFilterList = UnmodifiableList.decorate(filters);
		return true;
	}
	
	@Override
	public boolean rebuildMemberRegisterFilterKey(){
		List<String> filters = new ArrayList<String>();
		String keywords = (String)mongoService.getPrimitiveObject(ConfigConstant.KEY_MEMBERKEYWORDS);
		if(StringUtils.isBlank(keywords))  throw new IllegalArgumentException("rebuildMemberRegisterFilterKey Error!!");
		String[] filterarrs2 = keywords.split(",");
		for (int i = 0; i < filterarrs2.length; i++) {
			filters.add("(" + filterarrs2[i].trim() + ")");
		}
		memberRegisterFilterList = UnmodifiableList.decorate(filters);
		return true;
	}
	private String filterJsKey(String html){
		String key = "";
		if (StringUtils.containsIgnoreCase(html, "<script")){
			key = "<script";// 过滤Js
		}
		if (StringUtils.containsIgnoreCase(html,"<iframe")){
			key = key + ", <iframe";// 过滤iframe
		}
		return key;
	}
	@Override
	public String filterAllKey(String html) {
		String filterJsKey = filterJsKey(html);
		if(StringUtils.isNotBlank(filterJsKey)) return filterJsKey;
		
		String text = getTextContent(html);
		StringBuilder sbkey =  new StringBuilder();
		filterContentKey(text, sbkey, memberRegisterFilterList);
		filterContentKey(text, sbkey, filterList);
		filterContentKey(text, sbkey, manualFilterList);
		return sbkey.toString();
	}

	@Override
	public String filterContentKey(String html) {
		String filterJsKey = filterJsKey(html);
		if(StringUtils.isNotBlank(filterJsKey)) return filterJsKey;
		html = XSSFilter.filterSpecStr(html);
		String text = getTextContent(html);
		StringBuilder sbkey =  new StringBuilder();
		filterContentKey(text, sbkey, filterList);
		filterContentKey(text, sbkey, manualFilterList);
		return sbkey.toString();
	}
	private String getTextContent(String html){
		String text = StringUtil.getHtmlText(html);
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(text)) {
			for (int i = 0; i < text.length(); i++) {// 过滤空白字符
				if (text.charAt(i) >= 48)
					sb.append(text.charAt(i));
			}
		}
		text = sb.toString();
		return text;
	}
	private void filterContentKey(String text, StringBuilder result, List<String> keys){
		for (String fkey : keys) {
			String k = StringUtil.findFirstByRegex(text, fkey);
			if (StringUtils.isNotBlank(k)) {
				result.append("," + k);
			}
		}
	}
	@Override
	public List<Long> getTreasureRelatedidList(String citycode, Long memberid, String tag, String action) {
		String hql = "select distinct t.relatedid from Treasure t where t.memberid=? and t.tag=? and t.action = ? and exists( select c.id from Cinema c where t.relatedid=c.id and c.citycode=? and c.booking=? )";
		List<Long> list = readOnlyTemplate.find(hql, memberid, tag, action, citycode, Cinema.BOOKING_OPEN);
		return list;
	}
	@Override
	public List<Long> getTreasureCinemaidList(String citycode, Long memberid, String action) {
		String hql = "select distinct t.relatedid from Treasure t where t.memberid=? and t.tag=? and t.action = ? and exists( select c.id from Cinema c where t.relatedid=c.id and c.citycode=?)";
		List<Long> list = readOnlyTemplate.find(hql, memberid, TagConstant.TAG_CINEMA, action, citycode);
		return list;
	}
	private final static String treasure_tag = "member";

	@Override
	public boolean isTreasureMember(Long fromMemberid, Long toMemberid) {
		if (StringUtils.equals(fromMemberid + "", toMemberid + ""))
			return true;
		DetachedCriteria query = DetachedCriteria.forClass(Treasure.class);
		query.add(Restrictions.eq("relatedid", toMemberid));
		query.add(Restrictions.eq("memberid", fromMemberid));
		query.add(Restrictions.eq("tag", treasure_tag));
		query.add(Restrictions.eq("action", Treasure.ACTION_COLLECT));
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty())
			return false;
		return Integer.parseInt("" + list.get(0)) > 0;
	}

	@Override
	public boolean cancelTreasure(Long fromMemberid, Long toMemberid, String tag, String action) {
		DetachedCriteria query = DetachedCriteria.forClass(Treasure.class);
		query.add(Restrictions.eq("relatedid", toMemberid));
		query.add(Restrictions.eq("memberid", fromMemberid));
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.eq("action", action));
		List list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty())
			return false;
		try {
			hibernateTemplate.delete(list.get(0));
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	@Override
	public List<Treasure> getTreasureListByMemberId(Long memberId, String[] tag, String[] removieTag, Long relatedid, int from, int maxnum, String... action) {
		DetachedCriteria query = this.getTreasureList(memberId, tag, removieTag, relatedid, action);
		List<Treasure> treasureList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return treasureList;
	}
	@Override
	public List<Long> getTreasureListByMemberIdList(Long relatedid, String tag, int from, int maxnum, String action){
		DetachedCriteria query = DetachedCriteria.forClass(Treasure.class, "t");
		DetachedCriteria subquery = DetachedCriteria.forClass(Treasure.class, "tt");
		subquery.add(Restrictions.eq("tt.relatedid", relatedid));
		subquery.add(Restrictions.eq("tt.tag", tag));
		subquery.add(Restrictions.eqProperty("tt.memberid", "t.memberid"));
		subquery.setProjection(Projections.property("tt.memberid"));
		query.add(Restrictions.ne("t.relatedid", relatedid));
		query.add(Restrictions.eq("t.tag", tag));
		query.add(Subqueries.exists(subquery));
		query.setProjection(Projections.distinct(Projections.property("t.relatedid")));
		List<Long> list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return list;
	}
	private DetachedCriteria getTreasureList(Long memberId, String[] tag, String[] removieTag, Long relatedid, String... action) {
		DetachedCriteria query = DetachedCriteria.forClass(Treasure.class, "t");
		if (memberId!=null)
			query.add(Restrictions.eq("t.memberid", memberId));
		if (!ArrayUtils.isEmpty(tag)) {
			if (ArrayUtils.getLength(tag) == 1)
				query.add(Restrictions.eq("t.tag", tag[0]));
			else
				query.add(Restrictions.in("t.tag", tag));
		}
		if (!ArrayUtils.isEmpty(removieTag)) {
			if (ArrayUtils.getLength(removieTag) == 1)
				query.add(Restrictions.not(Restrictions.eq("t.tag", removieTag[0])));
			else
				query.add(Restrictions.not(Restrictions.in("t.tag", removieTag)));
		}
		if (action != null && !ArrayUtils.isEmpty(action)) {
			if (ArrayUtils.getLength(action) == 1)
				query.add(Restrictions.eq("t.action", action[0]));
			else
				query.add(Restrictions.in("t.action", action));
		}
		if (relatedid != null)
			query.add(Restrictions.eq("relatedid", relatedid));
		query.add(Restrictions.ne("t.tag", "baractivity"));
		query.addOrder(Order.desc("t.addtime"));
		return query;
	}

	@Override
	public Integer getTreasureCountByMemberId(Long memberId, String[] tag, String[] removieTag, String... action) {
		DetachedCriteria query = DetachedCriteria.forClass(Treasure.class, "t");
		if (memberId != null)
			query.add(Restrictions.eq("t.memberid", memberId));
		if (!ArrayUtils.isEmpty(tag)) {
			if (ArrayUtils.getLength(tag) == 1)
				query.add(Restrictions.eq("t.tag", tag[0]));
			else
				query.add(Restrictions.in("t.tag", tag));
		}
		if (!ArrayUtils.isEmpty(removieTag)) {
			if (ArrayUtils.getLength(removieTag) == 1)
				query.add(Restrictions.not(Restrictions.eq("t.tag", removieTag[0])));
			else
				query.add(Restrictions.not(Restrictions.in("t.tag", removieTag)));
		}
		if (action != null && !ArrayUtils.isEmpty(action)) {
			if (ArrayUtils.getLength(action) == 1)
				query.add(Restrictions.eq("t.action", action[0]));
			else
				query.add(Restrictions.in("t.action", action));
		}
		query.add(Restrictions.ne("t.tag", "baractivity"));
		query.setProjection(Projections.rowCount());
		List treasureList = readOnlyTemplate.findByCriteria(query);
		return new Integer(treasureList.get(0) + "");
	}


	@Override
	public List<Long> getFanidListByMemberId(Long memberid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Treasure.class, "t");
		query.add(Restrictions.eq("t.action", Treasure.ACTION_COLLECT));
		query.add(Restrictions.eq("t.tag", Treasure.TAG_MEMBER));
		query.add(Restrictions.eq("t.relatedid", memberid));
		query.addOrder(Order.desc("t.addtime"));
		List list = readOnlyTemplate.findByCriteria(query, from, maxnum);
		List<Long> memberIdList = ServiceHelper.getMemberIdListFromBeanList(list);
		return memberIdList;
	}

	@Override
	public Treasure getTreasure(Long memberid, Long relatedid, String tag, String action) {
		DetachedCriteria query = DetachedCriteria.forClass(Treasure.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.eq("relatedid", relatedid));
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.eq("action", action));
		List<Treasure> treasureList = readOnlyTemplate.findByCriteria(query);
		if (treasureList.isEmpty())
			return null;
		return treasureList.get(0);
	}

	@Override
	public List<Treasure> getTreasureListByMemberId(Long memberid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Treasure.class);
		query.add(Restrictions.eq("memberid", memberid));
		query.add(Restrictions.isNotNull("actionlabel"));
		query.addOrder(Order.desc("addtime"));
		List<Treasure> treasureList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return treasureList;
	}
	@Override
	public String getDiaryBody(long diaryid) {
		Map map = new HashMap();
		map.put(MongoData.SYSTEM_ID, diaryid);
		Map diaryBodyMap = mongoService.findOne(MongoData.NS_DIARY, map);
		if (diaryBodyMap == null)
			return "";
		return diaryBodyMap.get("body") + "";
	}

	@Override
	public void saveDiaryBody(long diaryid, Timestamp updatetime, String body) {
		Map map = new HashMap();
		map.put(MongoData.SYSTEM_ID, diaryid);
		map.put("recordid", diaryid);
		if (updatetime != null)
			map.put("updatetime", DateUtil.format(updatetime, "yyyy-MM-dd HH:mm:ss"));
		map.put("body", body);
		mongoService.saveOrUpdateMap(map, "recordid", MongoData.NS_DIARY);
	}

	@Override
	public boolean isNight() {
		Integer curHour = DateUtil.getCurrentHour(DateUtil.currentTime());
		int before = Math.min(night_end, night_begin);
		int after = Math.max(night_end, night_begin);
		return curHour>=after || curHour<=before;
	}
	@Override
	public boolean allowAddContent(String flag, Long memberid) {
		return operationService.updateOperation(flag+memberid, frequency, times);
	}
	
	public void addBlogData(Long userid, String tag, Long relatedid){
		String ukey = relatedid + tag;
		BlogData blogData = baseDao.getObject(BlogData.class, ukey);
		if(blogData != null) return ;
		blogData = new BlogData(tag, relatedid);
		baseDao.saveObject(blogData);
		monitorService.saveAddLog(userid, BlogData.class, blogData.getUkey(), blogData);
	}
	
	@Override
	public void saveOrUpdateBlogData(Long userid, String tag, Long relatedid, Map<String/*propertyname*/,Integer> keyValueMap) {
		String ukey = relatedid + tag;
		BlogData blogData = baseDao.getObject(BlogData.class, ukey);
		if(blogData == null){
			blogData = new BlogData(tag, relatedid);
		}
		ChangeEntry changeEntry = new ChangeEntry(blogData);
		BindUtils.bind(blogData, keyValueMap, false, BlogData.disallowBindField);
		if(!changeEntry.getChangeMap(blogData).isEmpty()){
			blogData.setUpdatetime(DateUtil.getCurFullTimestamp());
		}
		baseDao.saveObject(blogData);
		monitorService.saveChangeLog(userid, BlogData.class, blogData.getUkey(), changeEntry.getChangeMap(blogData));
	}
	
	
	@Override
	public void saveOrUpdateBlogDateEveryDay(Long userid, String tag, Long relatedid, String blogtype, Date blogdate, int blogcount) {
		BlogDataEveryDay blogDataEveryDay = getBlogDataEveryDay(tag, relatedid, blogtype, blogdate);
		if(blogDataEveryDay == null){
			blogDataEveryDay = new BlogDataEveryDay(tag, relatedid, blogtype, blogdate);
		}
		ChangeEntry changeEntry = new ChangeEntry(blogDataEveryDay);
		blogDataEveryDay.setBlogcount(blogcount);
		if(!changeEntry.getChangeMap(blogDataEveryDay).isEmpty()){
			blogDataEveryDay.setUpdatetime(DateUtil.getCurFullTimestamp());
		}
		baseDao.saveObject(blogDataEveryDay);
		monitorService.saveChangeLog(userid, BlogData.class, blogDataEveryDay.getId(), changeEntry.getChangeMap(blogDataEveryDay));
	}
	
	@Override
	public BlogDataEveryDay getBlogDataEveryDay(String tag, Long relatedid, String blogtype, Date blogdate){
		DetachedCriteria query = DetachedCriteria.forClass(BlogDataEveryDay.class, "d");
		query.add(Restrictions.eq("d.tag", tag));
		query.add(Restrictions.eq("d.relatedid", relatedid));
		query.add(Restrictions.eq("d.blogtype", blogtype));
		query.add(Restrictions.eq("d.blogdate", blogdate));
		query.addOrder(Order.asc("d.addtime"));
		List<BlogDataEveryDay> blogDataEveryDayList = hibernateTemplate.findByCriteria(query, 0, 1);
		if(blogDataEveryDayList.isEmpty()) return null;
		return blogDataEveryDayList.get(0);
	}
	@Override
	public List<Map> getDiaryMapList(Timestamp starttime, Timestamp endtime){
		DetachedCriteria query = DetachedCriteria.forClass(Diary.class, "d");
		query.add(Restrictions.isNotNull("d.category"));
		query.add(Restrictions.isNotNull("d.categoryid"));
		query.add(Restrictions.ge("d.addtime", starttime));
		query.add(Restrictions.le("d.addtime", endtime));
		query.add(Restrictions.eq("d.type", DiaryConstant.DIARY_TYPE_COMMENT));
		query.add(Restrictions.like("d.status", Status.Y, MatchMode.START));
		query.add(Restrictions.ne("d.status", Status.Y_TREAT));
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.count("categoryid"), "rowcount");
		Projection pro = Projections.sqlGroupProjection(
				"to_char({alias}.addtime,'yyyy-mm-dd') as adddate, category, categoryid", 
				"to_char({alias}.addtime,'yyyy-mm-dd'), category, categoryid", new String[]{"adddate", "category", "categoryid"}, 
				new Type[]{new StringType(), new StringType(), new StringType()});
		projectionList.add(pro);
		query.setProjection(projectionList);
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		return 	hibernateTemplate.findByCriteria(query);
	}
	
	
	private DetachedCriteria queryBlogData(String citycode, String tag, String searchName, String searchKey){
		DetachedCriteria query = DetachedCriteria.forClass(BlogData.class, "b");
		query.add(Restrictions.eq("b.tag", tag));
		Class clazz = RelateClassHelper.getRelateClazz(tag);
		if(clazz != null){
			boolean isSearch = StringUtils.isNotBlank(searchName) && StringUtils.isNotBlank(searchKey) && ClassUtils.hasMethod(clazz, "get" + StringUtils.capitalize(searchName));
			boolean isCitycode = StringUtils.isNotBlank(citycode) && ClassUtils.hasMethod(clazz, "get" + StringUtils.capitalize("citycode"));
			if(isSearch || isCitycode){
				DetachedCriteria subQuery = DetachedCriteria.forClass(clazz, "c");
				if(isCitycode){
					subQuery.add(Restrictions.eq("c.citycode", citycode));
				}
				if(isSearch){
					subQuery.add(Restrictions.like("c."+searchName, searchKey, MatchMode.ANYWHERE));
				}
				subQuery.add(Restrictions.eqProperty("c.id", "b.relatedid"));
				subQuery.setProjection(Projections.property("c.id"));
				query.add(Subqueries.exists(subQuery));
			}
		}
		return query;
	}
	
	@Override
	public List<Long> getIdListBlogDataByTag(String citycode, String tag, String searchName, String searchKey, boolean asc, String order, int from, int maxnum){
		DetachedCriteria query = queryBlogData(citycode, tag, searchName, searchKey);
		query.setProjection(Projections.property("b.relatedid"));
		if(StringUtils.isNotBlank(order) && ClassUtils.hasMethod(BlogData.class, "get" + StringUtils.capitalize(order))){
			if(asc) query.addOrder(Order.asc(order));
			else query.addOrder(Order.desc(order));
		}else{
			if(asc) query.addOrder(Order.asc("diarycount"));
			else query.addOrder(Order.desc("diarycount"));
		}
		query.addOrder(Order.desc("b.ukey"));
		List<Long> idList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return idList;
	}
	
	@Override
	public Integer getIdCountBlogDataByTag(String citycode, String tag, String searchName, String searchKey){
		DetachedCriteria query = queryBlogData(citycode, tag, searchName, searchKey);
		query.setProjection(Projections.rowCount());
		List<Long> idList = hibernateTemplate.findByCriteria(query, 0, 1);
		if(idList.isEmpty()) return 0;
		return idList.get(0).intValue();
	}
	
	private DetachedCriteria queryBlogDataEveryDay(String citycode, String tag, String searchName, String searchKey, String blogtype, Date startdate, Date enddate){
		DetachedCriteria query = DetachedCriteria.forClass(BlogDataEveryDay.class, "b");
		query.add(Restrictions.eq("b.tag", tag));
		query.add(Restrictions.eq("b.blogtype", blogtype));
		query.add(Restrictions.ge("b.blogdate", startdate));
		query.add(Restrictions.le("b.blogdate", enddate));
		Class clazz = RelateClassHelper.getRelateClazz(tag);
		if(clazz != null){
			boolean isSearch = StringUtils.isNotBlank(searchName) && StringUtils.isNotBlank(searchKey) && ClassUtils.hasMethod(clazz, "get" + StringUtils.capitalize(searchName));
			boolean isCitycode = StringUtils.isNotBlank(citycode) && ClassUtils.hasMethod(clazz, "get" + StringUtils.capitalize("citycode"));
			if(isSearch || isCitycode){
				DetachedCriteria subQuery = DetachedCriteria.forClass(clazz, "c");
				if(isCitycode){
					subQuery.add(Restrictions.eq("c.citycode", citycode));
				}
				if(isSearch){
					subQuery.add(Restrictions.like("c."+searchName, searchKey, MatchMode.ANYWHERE));
				}
				subQuery.add(Restrictions.eqProperty("c.id", "b.relatedid"));
				subQuery.setProjection(Projections.property("c.id"));
				query.add(Subqueries.exists(subQuery));
			}
		}
		return query;
	}
	
	@Override
	public List<Long> getIdListEveryDayByTag(String citycode, String tag, String searchName, String searchKey, String blogtype, Date startdate, Date enddate, int from, int maxnum){
		DetachedCriteria query = queryBlogDataEveryDay(citycode, tag, searchName, searchKey, blogtype, startdate, enddate);
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.rowCount(), "rowcount");
		projectionList.add(Projections.groupProperty("relatedid"), "relatedid");
		query.setProjection(projectionList);
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		query.addOrder(Order.desc("rowcount"));
		query.addOrder(Order.desc("relatedid"));
		List<Map> mapList = hibernateTemplate.findByCriteria(query, from, maxnum);
		List<Long> idList = new ArrayList<Long>();
		for (Map map : mapList) {
			idList.add(Long.parseLong(map.get("relatedid")+""));
		}
		return idList;
	}
	
	@Override
	public Integer getIdCountEveryDayByTag(String citycode, String tag, String searchName, String searchKey, String blogtype, Date startdate, Date enddate){
		DetachedCriteria query = queryBlogDataEveryDay(citycode, tag, searchName, searchKey, blogtype, startdate, enddate);
		query.setProjection(Projections.countDistinct("relatedid"));
		List<Long> idList = hibernateTemplate.findByCriteria(query, 0, 1);
		if(idList.isEmpty()) return 0;
		return idList.get(0).intValue();
	}

	@Override
	public Integer isBadEgg(Member member) {
		String key = CacheConstant.buildKey("badUsrCheckInterface", member.getId());
		Object obj = cacheService.get(CacheConstant.REGION_ONEDAY, key);
		if (obj != null) {
			int totalEggs = (Integer)obj;
			MemberInfo memberInfo = baseDao.getObject(MemberInfo.class, member.getId());
			if (memberInfo!=null && !memberInfo.isBindSuccess()) {
				if ( totalEggs >= unbindUsrLimitEgg ) {
					return 1;//otherparty usr or dny usr,unbind account 
				}
			}else if(memberInfo!=null && StringUtils.isNotBlank(memberInfo.getNewtask())){
				if(!StringUtils.contains(memberInfo.getNewtask(), "confirmreg") && !StringUtils.contains(memberInfo.getNewtask(), "bindmobile")){
					if (totalEggs >= unbindUsrLimitEgg) {
						return 7;//unbind mobile,bindeml,uncheck eml
					}
				}
			}else if(memberInfo == null ){
				return 0;//xxx usr
			}
			if (totalEggs >= bindUsrLimitEgg) {
				return 51;//limit eggs
			}
			totalEggs++;
			cacheService.set(CacheConstant.REGION_ONEDAY, key,totalEggs);
		}else {			
			cacheService.set(CacheConstant.REGION_ONEDAY, key,1);
		}
		return 777;
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		rebuildFilterKey();
		rebuildManualFilterKey();
		rebuildMemberRegisterFilterKey();
		initEgg();
		initRegEx();
	}
	public void initEgg(){
		Map map1 = mongoService.findOne(MongoData.NS_BADEGG, MongoData.DEFAULT_ID_NAME, "BIND_USR_LIMIT_EGG");
		Map map2 = mongoService.findOne(MongoData.NS_BADEGG, MongoData.DEFAULT_ID_NAME, "UNBIND_USR_LIMIT_EGG");
		if (map1 != null) {
			bindUsrLimitEgg = (Integer) map1.get("val");
		}else if (map1 == null) {
			map1 = new HashMap();
			map1.put(MongoData.DEFAULT_ID_NAME, "BIND_USR_LIMIT_EGG");
			map1.put("val", bindUsrLimitEgg);
			mongoService.saveOrUpdateMap(map1, MongoData.DEFAULT_ID_NAME, MongoData.NS_BADEGG);
		}
		if (map2 != null) {
			unbindUsrLimitEgg = (Integer) map2.get("val");
		}else if (map2 == null) {
			map2 = new HashMap();
			map2.put(MongoData.DEFAULT_ID_NAME, "UNBIND_USR_LIMIT_EGG");
			map2.put("val", unbindUsrLimitEgg);
			mongoService.saveOrUpdateMap(map2, MongoData.DEFAULT_ID_NAME, MongoData.NS_BADEGG);
		}
	}
	public void initRegEx(){
		Map map1 = mongoService.findOne(MongoData.NS_REGEXP, MongoData.DEFAULT_ID_NAME, "RexEx");
		if (map1 != null) {
			XSSFilter.regExp = (String) map1.get("val");
		}
	}
	@Override
	public void rebuildAllFilterKeys(){
		rebuildFilterKey();
		rebuildManualFilterKey();
		rebuildMemberRegisterFilterKey();
	}
	@Override
	public void refreshCurrent(String newConfig) {
		rebuildAllFilterKeys();
		GewaConfig cfg = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_BBS_TIME);
		//夜间贴
		if(cfg!=null){
			Map<String, Integer> gewaConMap =	JsonUtils.readJsonToMap(cfg.getContent());
			if(gewaConMap!=null){
				if(gewaConMap.get("nightStart")!=null){
					night_begin = gewaConMap.get("nightStart");
				}
				if(gewaConMap.get("nightEnd")!=null){
					night_end = gewaConMap.get("nightEnd");
				}
				if(gewaConMap.get("frequency")!=null){
					frequency = gewaConMap.get("frequency");
					frequency = frequency*OperationService.HALF_HOUR;
				}
				if(gewaConMap.get("times")!=null){
					times = gewaConMap.get("times");
				}
			}
		}
		initEgg();
		initRegEx();
	}

}
