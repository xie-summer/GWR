package com.gewara.service.bbs.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.json.SeeDrama;
import com.gewara.json.SeeMovie;
import com.gewara.json.SeeOrder;
import com.gewara.json.SeeSport;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.BaseObject;
import com.gewara.model.bbs.MarkCount;
import com.gewara.model.bbs.MemberMark;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.common.JsonData;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.RelateService;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.MarkHelper;

@Service("markService")
public class MarkServiceImpl extends BaseServiceImpl implements MarkService {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	
	@Autowired@Qualifier("relateService")
	private RelateService relateService;
	private static final Map<String, Class<? extends SeeOrder>> orderTag = new HashMap<String, Class<? extends SeeOrder>>();
	static {
		orderTag.put("movie", SeeMovie.class);
		orderTag.put("cinema", SeeMovie.class);
		orderTag.put("drama", SeeDrama.class);
		orderTag.put("theatre", SeeDrama.class);
		orderTag.put("sport", SeeSport.class);
	}
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService){
		this.mongoService = mongoService;
	}
	@Override
	public MemberMark getLastMemberMark(String tag, Long relatedid, String markname, Long memberId) {
		String query = " from MemberMark t where t.tag = ? and t.relatedid = ? and t.markname = ? and t.memberid = ? order by t.addtime desc ";
		// String query = "from MemberMark m where m.id = (select max(t.id) from
		// MemberMark t where t.tag = ? and t.relatedid = ? and t.markname = ? and
		// t.memberid = ?)";
		List<MemberMark> result = hibernateTemplate.find(query, tag, relatedid, markname, memberId);
		if (result.isEmpty())
			return null;
		return result.get(0);
	}

	@Override
	public MemberMark getCurMemberMark(String tag, Long relatedid, String markname, Long memberId) {
		String query = "from MemberMark t where t.tag = ? and t.relatedid = ? and t.markname = ? and t.memberid = ? and t.addtime>=?";
		List<MemberMark> result = readOnlyTemplate.find(query, tag, relatedid, markname, memberId, DateUtil.addHour(new Timestamp(System.currentTimeMillis()), -12));
		if (result.isEmpty())
			return null;
		return result.get(0);
	}

	@Override
	public List<MemberMark> getMarkList(String tag, Long relatedid, String markname, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(MemberMark.class);
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.eq("relatedid", relatedid));
		query.add(Restrictions.eq("markname", markname));
		query.add(Restrictions.ge("markvalue", 7)); // 20110630统一加入评分>=7条件
		query.addOrder(Order.desc("addtime"));
		List<MemberMark> mmList = readOnlyTemplate.findByCriteria(query, 0, maxnum);
		return mmList;
	}
	
	public List<MemberMark> getMarkList(String tag,Long relatedid, String markname ,String flag){
		DetachedCriteria query = DetachedCriteria.forClass(MemberMark.class);
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.eq("relatedid", relatedid));
		query.add(Restrictions.eq("markname", markname));
		if(StringUtils.isNotBlank(flag)){
			query.add(Restrictions.eq("flag", flag));
		}
		query.addOrder(Order.desc("addtime"));
		List<MemberMark> mmList = readOnlyTemplate.findByCriteria(query);
		return mmList;
	}

	private Map<String, Map> markdataMap = new HashMap<String, Map>();
	@Override
	public Map getMarkdata(String tag) {
		Map result = markdataMap.get(tag);
		if(result==null){
			initMarkdata(tag);
		}
		return markdataMap.get(tag);
	}
	private void initMarkdata(String tag){
		JsonData data = baseDao.getObject(JsonData.class, tag + JsonDataKey.KEY_MARKDATA);
		Map result = new HashMap();
		if(data != null) {
			result.putAll(JsonUtils.readJsonToMap(data.getData()));	
		}
		GewaConfig gewaConfig = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_MARKCONSTANT);
		int markConstant = 5;
		if(gewaConfig != null) {
			markConstant = Integer.valueOf(gewaConfig.getContent());
		}
		result.put("markConstant", markConstant);
		markdataMap.put(tag, result);
	}
	@Override
	public List<Map> getMarkRelatedidByAddtime(String tag, Timestamp starttime, Timestamp endtime) {
		DetachedCriteria query = DetachedCriteria.forClass(MemberMark.class);
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.ge("addtime", starttime));
		query.add(Restrictions.le("addtime", endtime));
		query.add(Restrictions.eq("markname", "generalmark"));
		query.setProjection(Projections.projectionList().add(Projections.property("relatedid"), "relatedid")
				.add(Projections.sum("markvalue"), "marks").add(Projections.count("relatedid"), "times")
				.add(Projections.property("flag"),"flag").add(Projections.groupProperty("relatedid"))
				.add(Projections.groupProperty("flag")));
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		List<Map> relatedList = readOnlyTemplate.findByCriteria(query);
		return relatedList;
	}
	@Override
	public MarkCountData getMarkCountByTagRelatedid(String tag, Long relatedid) {
		MarkCount markCount = baseDao.getObject(MarkCount.class, tag+relatedid);
		if(markCount==null) return null;
		if(StringUtils.equals("movie", tag) && relatedid.equals(18038859L)){//神奇hack
			GewaConfig config = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_MARK_SCALE);
			if(config !=null){
				Map<String, String> scaleMap = JsonUtils.readJsonToMap(config.getContent());
				if(scaleMap.containsKey(markCount.getMkey())){
					return new MarkCountData(markCount, new Double(scaleMap.get(markCount.getMkey()))); 
				}
			}
		}
		return new MarkCountData(markCount, null);
	}
	private MarkCount getMarkCount(String tag, Long relatedid) {
		MarkCount markCount = baseDao.getObject(MarkCount.class, tag+relatedid);
		return markCount;
	}
	@Override
	public Timestamp updateMarkCount(String tag) {
		JsonData data = baseDao.getObject(JsonData.class, tag + JsonDataKey.KEY_MARKCOUNT);
		String strData = data.getData();
		Timestamp starttime = DateUtil.parseTimestamp(strData);
		Timestamp endtime = new Timestamp(System.currentTimeMillis());

		List<Map> relatedList = getMarkRelatedidByAddtime(tag, starttime, endtime);
		for (Map map : relatedList) {
			Long relatedid = Long.parseLong(map.get("relatedid") + "");
			MarkCount markCount = getMarkCount(tag, relatedid);
			String flag = map.get("flag").toString();
			if(Status.Y.equals(flag)) {
				if (markCount == null) {
					markCount = new MarkCount(tag, relatedid);
					markCount.setBookingmarks(Integer.parseInt(map.get("marks") + ""));
					markCount.setBookingtimes(Integer.parseInt(map.get("times") + ""));
					baseDao.addObject(markCount);
				}else{
					markCount.setBookingmarks(Integer.parseInt(map.get("marks") + ""));
					markCount.setBookingtimes(Integer.parseInt(map.get("times") + ""));
					baseDao.updateObject(markCount);
				}
			}else if(Status.N.equals(flag)){
				if (markCount == null) {
					markCount = new MarkCount(tag, relatedid);
					markCount.setUnbookingmarks(Integer.parseInt(map.get("marks") + ""));
					markCount.setUnbookingtimes(Integer.parseInt(map.get("times") + ""));
					baseDao.addObject(markCount);
				}else{
					markCount.setUnbookingmarks(Integer.parseInt(map.get("marks") + ""));
					markCount.setUnbookingtimes(Integer.parseInt(map.get("times") + ""));
					baseDao.updateObject(markCount);
				}
			}
		}
		data.setData(DateUtil.formatTimestamp(endtime));
		data.setValidtime(DateUtil.addDay(endtime, 180));
		baseDao.updateObject(data);
		return endtime;
	}

	@Override
	public void updateAvgMarkTimes(String tag, Timestamp starttime, Timestamp endtime) {
		Timestamp curTime = DateUtil.getCurFullTimestamp();
		Map markMap = getAllMarktimes(tag, DateUtil.addDay(curTime, -30), curTime);
		Integer avgMarktimes = Integer.parseInt(markMap.get("allMarktimes") + "") / Integer.parseInt(markMap.get("allCount") + "");
		JsonData data = baseDao.getObject(JsonData.class, tag + JsonDataKey.KEY_MARKDATA);
		Integer maxTimes = getMaxMarktimes(tag, starttime);
		if (data == null) {
			data = new JsonData();
			data.setDkey(tag + JsonDataKey.KEY_MARKDATA);
			data.setValidtime(DateUtil.addDay(DateUtil.getCurFullTimestamp(), 30));
			data.setTag(JsonDataKey.KEY_MARKDATA);
		}
		data.setData("{\"maxtimes\":\""+ (maxTimes < 1000 ? 1000 : maxTimes) + "\",\"avgmarktimes\":\""+avgMarktimes+"\"}");
		baseDao.saveObject(data);
	}

	private Map getAllMarktimes(String tag, Timestamp starttime, Timestamp endtime) {
		DetachedCriteria query = DetachedCriteria.forClass(MemberMark.class);
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.eq("markname", "generalmark"));
		query.add(Restrictions.gt("addtime", starttime));
		query.add(Restrictions.lt("addtime", endtime));
		query.setProjection(Projections.projectionList().add(Projections.count("id"), "allMarktimes").add(Projections.countDistinct("relatedid"), "allCount"));
		query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
		List<Map> relatedList = readOnlyTemplate.findByCriteria(query, 0, 1);
		return relatedList.get(0);
	}
	@Override
	public Integer getMaxMarktimes(String tag, Timestamp starttime) {
		String query = "select new map(to_char(max(count(relatedid))) as maxmarktimes) from MemberMark where tag = ? and addtime >= ? group by relatedid";
		List<Map<String, String>> relatedList = readOnlyTemplate.find(query, tag, starttime);
		return Integer.valueOf(relatedList.get(0).get("maxmarktimes"));
	}

	@Override
	public List<Map> getMarkDetail(String tag, Long relatedid, String markname) {
		String query = "select new map(m.markvalue as markvalue, count(*) as markcount) from MemberMark m where m.tag = ? and m.relatedid = ? and m.markname = ? group by m.markvalue order by m.markvalue";
		List<Map> result = readOnlyTemplate.find(query, tag, relatedid, markname);
		return result;
	}

	@Override
	public Integer getMarkValueCount(String tag, Long relatedid, String markname, int fromValue, int maxValue) {
		String query = "select new map(to_char(count(*)) as markcount) from MemberMark m where m.tag = ? and m.relatedid = ? and m.markname = ? and m.markvalue>=? and m.markvalue<=?";
		List<Map<String, String>> result = readOnlyTemplate.find(query, tag, relatedid, markname, fromValue, maxValue);
		return Integer.valueOf(result.get(0).get("markcount"));
	}	
	
	@Override
	public Map getPercentCount(String tag, Long relatedid) {
		if (StringUtils.isBlank(tag) || relatedid == null)
			throw new IllegalArgumentException("参数错误！");
		Map model = new HashMap();
		int goodCount = getMarkValueCount(tag, relatedid, "generalmark", 5, 10);
		int sumCount = getMarkValueCount(tag, relatedid, "generalmark", 1, 10);
		if(sumCount < 50) {
			goodCount = 50;
			sumCount = 100;
		}
		model.put("goodPerc", goodCount *100 / sumCount);
		return model;
	}
	// 评分统计
	@Override
	public Map getGradeCount(String tag, Long relatedid) {
		if (StringUtils.isBlank(tag) || relatedid == null)
			throw new IllegalArgumentException("参数错误！");

		Map model = new HashMap();
		int mtCount = getMarkValueCount(tag, relatedid, "generalmark", 1, 3);
		int geCount = getMarkValueCount(tag, relatedid, "generalmark", 4, 6);
		int okCount = getMarkValueCount(tag, relatedid, "generalmark", 7, 8);
		int neCount = getMarkValueCount(tag, relatedid, "generalmark", 9, 10);
		int sumCount = mtCount + geCount + okCount + neCount;
		if (sumCount < 5) {
			mtCount = 25;
			geCount = 25;
			okCount = 25;
			neCount = 25;
			sumCount = 100;
		}
		model.put("mtCount", mtCount);
		model.put("geCount", geCount);
		model.put("okCount", okCount);
		model.put("neCount", neCount);
		model.put("sumCount", sumCount);
		return model;
	}
	
	@Override
	public List<Map> getGradeDetail(String tag, Long relatedid){
		String key = CacheConstant.buildKey("getGradeDetail", tag, relatedid);
		List<Map> list = (List<Map>)cacheService.get(CacheConstant.REGION_TENMIN, key);
		if(list != null){
			return list;
		}
		if (StringUtils.isBlank(tag) || relatedid == null){
			throw new IllegalArgumentException("参数错误！");
		}
		int mtCount = getMarkValueCount(tag, relatedid, "generalmark",Status.Y, 1, 4);
		int geCount = getMarkValueCount(tag, relatedid, "generalmark",Status.Y, 4, 6);
		int okCount = getMarkValueCount(tag, relatedid, "generalmark",Status.Y, 6, 8);
		int goodCount = getMarkValueCount(tag, relatedid, "generalmark",Status.Y, 8, 9);
		int neCount = getMarkValueCount(tag, relatedid, "generalmark",Status.Y, 9, 11);
		//未购票用户
		int mtCountN = getMarkValueCount(tag, relatedid, "generalmark",Status.N, 1, 4);
		int geCountN = getMarkValueCount(tag, relatedid, "generalmark",Status.N, 4, 6);
		int okCountN = getMarkValueCount(tag, relatedid, "generalmark",Status.N, 6, 8);
		int goodCountN = getMarkValueCount(tag, relatedid, "generalmark",Status.N, 8, 9);
		int neCountN = getMarkValueCount(tag, relatedid, "generalmark",Status.N, 9, 11);
		int sumCount = mtCount + geCount + okCount + neCount + goodCount + mtCountN + geCountN + okCountN + goodCountN + neCountN;
		list = new LinkedList<Map>();
		list.add(this.newMarkGradeModel("9-10分", (neCount + neCountN), neCount, neCountN, sumCount));
		list.add(this.newMarkGradeModel("8-9分", (goodCount + goodCountN), goodCount,goodCountN, sumCount));
		list.add(this.newMarkGradeModel("6-8分", (okCount + okCountN), okCount, okCountN, sumCount));
		list.add(this.newMarkGradeModel("4-6分", (geCount + geCountN), geCount, geCountN, sumCount));
		list.add(this.newMarkGradeModel("1-4分", (mtCount + mtCountN), mtCount, mtCountN, sumCount));
		cacheService.set(CacheConstant.REGION_TENMIN, key, list);
		return list;
	}
	
	private Map newMarkGradeModel(String name,int count,int y,int n,int sumCount){
		Map model = new HashMap();
		model.put("name", name);//等级描述
		model.put("value", count);//等级评分总数
		if(sumCount != 0){
			model.put("percent",Math.round(count * 100.0/sumCount));//占比
		}else{
			model.put("percent", 0);//占比
		}
		model.put("pointY", y);//购票用户评分数
		model.put("pointN", n);//非购票用户评分总数
		if(count != 0){
			model.put("percenY",Math.round(y * 100.0/count));//购票用户评分占比
			model.put("percenN",Math.round(n * 100.0/count));//购票用户评分占比
		}else{
			model.put("percenY", 1);//购票用户评分占比
			model.put("percenN",1);//购票用户评分占比
		}
		return model;
	}
	
	private Integer getMarkValueCount(String tag, Long relatedid, String markname,String flag, int fromValue, int maxValue) {
		String query = "select new map(to_char(count(*)) as markcount) from MemberMark m where m.tag = ? and m.relatedid = ? and m.markname = ? and m.markvalue>=? and m.markvalue<? and flag = ?";
		List<Map<String, String>> result = readOnlyTemplate.find(query, tag, relatedid, markname, fromValue, maxValue,flag);
		return Integer.valueOf(result.get(0).get("markcount"));
	}

	private void saveOrUpdateMarkCount(String tag, Long relatedid, Integer newmark, String newflag, Integer oldmark, String oldflag) {
		MarkCount markCount = getMarkCount(tag, relatedid);
		if(markCount==null) {
			markCount = new MarkCount(tag, relatedid);
		}
		int diffvalue = newmark - oldmark;
		boolean update = (oldmark==0);
		if(Status.Y.equals(newflag)) {
			if(update) {//第一次
				markCount.setBookingtimes(markCount.getBookingtimes() + 1);
				markCount.setBookingmarks(markCount.getBookingmarks() + diffvalue);
			}else {
				if(StringUtils.equals(Status.N, oldflag)) {
					markCount.setUnbookingtimes(markCount.getUnbookingtimes() - 1);
					markCount.setUnbookingmarks(markCount.getUnbookingmarks() - oldmark);
					markCount.setBookingtimes(markCount.getBookingtimes() + 1);
					markCount.setBookingmarks(markCount.getBookingmarks() + newmark);
				}else {
					markCount.setBookingmarks(markCount.getBookingmarks() + diffvalue);
				}
				baseDao.updateObject(markCount);
			}
		}else {
			markCount.setUnbookingmarks(markCount.getUnbookingmarks() + diffvalue);
			if(update) {
				markCount.setUnbookingtimes(markCount.getUnbookingtimes() + 1);
			}
		}
		baseDao.saveObject(markCount);
	}
	@Override
	public MemberMark saveOrUpdateMemberMark(String tag, Long relatedid, String markname, Integer markvalue, Member member) {
		if(markvalue == null || markvalue <1 || markvalue >10) throw new IllegalArgumentException("评分有错误！");
		Object obj = relateService.getRelatedObject(tag, relatedid);
		if(obj == null) throw new IllegalArgumentException("评分对象不存在！");
		boolean update = false;
		String newflag = "N";
		Integer diffmark = markvalue;
		Integer oldmark = 0;
		String oldflag = "Y";
		MemberMark memberMark = getLastMemberMark(tag, relatedid, markname, member.getId());
		if(memberMark == null){
			memberMark = new MemberMark(tag, relatedid, markname, markvalue, member.getId(), member.getNickname());
			update = true;
		}else{
			diffmark -= memberMark.getMarkvalue();
			oldmark = memberMark.getMarkvalue();
			oldflag = memberMark.getFlag();
			newflag = memberMark.getFlag();
		}
		if(StringUtils.equals(Status.N,newflag)){
			Class<? extends SeeOrder> clazz = orderTag.get(tag);
			if (clazz != null){
				boolean ticket = false;
				Date curDate = DateUtil.currentTime();
				Criteria criter1 = new Criteria("playDate").lte(curDate);
				Criteria criter2= new Criteria("tag").is(tag);
				Criteria criter3 = new Criteria("relatedid").is(relatedid);
				Criteria criter4 = new Criteria("memberid").is(member.getId());
				Query query = new Query(criter1).addCriteria(criter2).addCriteria(criter3).addCriteria(criter4);
				List orderList = mongoService.getObjectList(clazz, query.getQueryObject(), "playDate", false, 0, 1);
				if(!orderList.isEmpty()){
					SeeOrder order = (SeeOrder) orderList.get(0);
					if((order.getPlayDate()!= null && curDate.after(order.getPlayDate())) || order.getPlayDate() == null) ticket = true;
				}
				newflag = ticket?Status.Y:Status.N;
			}
		}
		saveOrUpdateMarkCount(tag, relatedid, markvalue, newflag, oldmark, oldflag);
		memberMark.setMarkvalue(markvalue);
		memberMark.setFlag(newflag);
		if(obj instanceof BaseObject){
			BaseObject bean = (BaseObject) obj;
			MarkHelper.updateMark(bean, markname, diffmark, update);
			baseDao.saveObject(bean);
		}
		baseDao.saveObject(memberMark);
		return memberMark;
	}
	@Override
	public boolean saveOrUpdateMemberMarkMap(String tag, Long relatedid, Member member, Map<String, Integer> memberMarkMap) {
		if(memberMarkMap == null || memberMarkMap.isEmpty() || member == null) return false;
		Object obj = relateService.getRelatedObject(tag, relatedid);
		if(obj == null) return false;
		BaseObject bean = null;
		if((obj instanceof BaseObject)) bean = (BaseObject) obj;
		Set<String> memberMarkKeySet = memberMarkMap.keySet();
		List<MemberMark> memberMarkList = new ArrayList<MemberMark>();
		Long memberid = member.getId();
		String nickname = member.getNickname();
		for (String markname : memberMarkKeySet) {
			Integer markvalue = memberMarkMap.get(markname);
			if(markvalue == null || markvalue <1 || markvalue >10) throw new IllegalArgumentException("评分有错误");
		}
		boolean ticket = false;
		Class<? extends SeeOrder> clazz = orderTag.get(tag);
		if (clazz != null){
			Date curDate = DateUtil.currentTime();
			Criteria criter1= new Criteria("tag").is(tag);
			Criteria criter2 = new Criteria("relatedid").is(relatedid);
			Criteria criter3 = new Criteria("memberid").is(member.getId());
			Criteria criter4 = new Criteria("playDate").lte(curDate);
			Query query = new Query(criter1).addCriteria(criter2).addCriteria(criter3).addCriteria(criter4);
			List orderList = mongoService.getObjectList(clazz, query.getQueryObject(), "paidtime", false, 0, 1);
			if(!orderList.isEmpty()){
				SeeOrder order = (SeeOrder) orderList.get(0);
				if((order.getPlayDate()!= null && curDate.after(order.getPlayDate())) || order.getPlayDate() == null) ticket = true;
			}
		}
		String newflag = ticket?Status.Y:Status.N;
		for (String markname : memberMarkKeySet) {
			boolean update = false;
			Integer markvalue = memberMarkMap.get(markname);
			Integer diffmark = markvalue; 
			String oldflag = "Y";
			Integer oldmark = 0;
			MemberMark memberMark = getLastMemberMark(tag, relatedid, markname, memberid);
			if(memberMark == null){
				memberMark = new MemberMark(tag, relatedid, markname, markvalue, memberid, nickname);
				update = true;
			}else{
				diffmark -= memberMark.getMarkvalue();
				oldmark = memberMark.getMarkvalue();
				oldflag = memberMark.getFlag();
				if(StringUtils.equals(newflag, "N")) newflag = memberMark.getFlag();
			}
			if(StringUtils.equals(markname, "generalmark")) {
				saveOrUpdateMarkCount(tag, relatedid, markvalue, newflag, oldmark, oldflag);
			}
			memberMark.setMarkvalue(markvalue);
			memberMark.setFlag(newflag);
			memberMarkList.add(memberMark);
			if(bean != null) MarkHelper.updateMark(bean, markname, diffmark, update);
		}
		baseDao.saveObjectList(memberMarkList);
		if(bean != null) baseDao.saveObject(bean);
		return true;
	}
	@Override
	public List<MarkCount> getMarkCountListByTag(String tag) {
		DetachedCriteria query = DetachedCriteria.forClass(MarkCount.class);
		query.add(Restrictions.eq("tag", TagConstant.TAG_MOVIE));
		List<MarkCount> result = hibernateTemplate.findByCriteria(query);
		return result;
	}
}
