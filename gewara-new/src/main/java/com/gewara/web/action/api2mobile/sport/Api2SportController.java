package com.gewara.web.action.api2mobile.sport;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.json.MemberSign;
import com.gewara.json.SeeSport;
import com.gewara.model.content.Picture;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportPrice;
import com.gewara.model.sport.SportProfile;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.Treasure;
import com.gewara.service.member.TreasureService;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.LongitudeAndLatitude;
import com.gewara.util.OuterSorter;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;

/**
 * 运动API
 * @author taiqichao
 *
 */
@Controller
public class Api2SportController extends BaseApiController {
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	@Autowired@Qualifier("sportOrderService")
	private SportOrderService sportOrderService;
	@Autowired@Qualifier("treasureService")
	private TreasureService treasureService;
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	class ValueComparator implements Comparator<Map.Entry<Map<Object, String>, Integer>> {
		public int compare(Map.Entry<Map<Object, String>, Integer> arg0, Map.Entry<Map<Object, String>, Integer> arg1) {
			return (arg1.getValue() - arg0.getValue());
		}
	}
	/**
	 * 查询运动项目列表
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/sport/sportItemList.xhtml")
	public String sportItemList(String citycode, ModelMap model){
		if(StringUtils.isBlank(citycode)) citycode = AdminCityContant.CITYCODE_SH;
		List<SportItem> itemList=sportService.getAllSportItem();
		Collections.sort(itemList, new PropertyComparator("clickedtimes", false, false));
		Map<Long, Integer> sportCountMap=new HashMap<Long, Integer>();
		Map<Long, Integer> buySportCountMap=new HashMap<Long, Integer>();
		for (SportItem sportItem : itemList) {
			int sportCount=sportService.getSportCountBySportItem(sportItem.getId(), citycode, true);
			sportCountMap.put(sportItem.getId(), sportCount);
			List<Long>	sportIdList = openTimeTableService.getCurOttSportIdList(sportItem.getId(), citycode);
			int csport = 0;
			if(sportIdList!=null) csport = sportIdList.size();
			buySportCountMap.put(sportItem.getId(), csport);
		}
		List<Entry<Long, Integer>> list = new ArrayList<Entry<Long, Integer>>(buySportCountMap.entrySet());
		Collections.sort(list, new Comparator() {
	        public int compare(Object o1, Object o2) {
	            Map.Entry<Long, Integer> obj1 = (Map.Entry) o1;
	            Map.Entry<Long, Integer> obj2 = (Map.Entry) o2;
	            return (obj2.getValue()-obj1.getValue());
	        }
	    });
		List<SportItem> newItemList = new ArrayList<SportItem>();
		List<SportItem> sortItemList = new ArrayList<SportItem>();
		for(Entry<Long, Integer> res : list){
			SportItem item  = daoService.getObject(SportItem.class, res.getKey());
			sortItemList.add(item);
		}
		newItemList.addAll(sortItemList);
		itemList.removeAll(sortItemList);
		newItemList.addAll(itemList);
		model.put("itemList", newItemList);
		model.put("sportCountMap", sportCountMap);
		model.put("buySportCountMap", buySportCountMap);
		return getXmlView(model, "api2/sport/sportItemList.vm");
	}
	/**
	 * 获取运动场馆详情
	 * @param sportid
	 * @param memberEncode
	 * @param returnField
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/sport/sportDetail.xhtml")
	public String sportDetail(Long sportid, String memberEncode, Integer commentCount, Integer memberCount, ModelMap model){
		Sport sport = daoService.getObject(Sport.class, sportid);
		if(sport == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "场馆不存在！");
		boolean collectstatus = false;
		if(StringUtils.isNotBlank(memberEncode)){
			Member member = memberService.getMemberByEncode(memberEncode);
			Treasure treasure = null;
			if(member != null)treasure = treasureService.getTreasureByTagMemberidRelatedid(TagConstant.TAG_SPORT, member.getId(), sportid, "collect");
			collectstatus = treasure != null?true:false;
		}
		List<SportItem> itemList = sportService.getSportItemListBySportId(sportid, SportProfile.STATUS_OPEN);
		Map<Long, Boolean> itemBooingMap = new HashMap<Long, Boolean>();
		boolean booking = false;
		for(SportItem item : itemList){
			Sport2Item s2i = sportService.getSport2Item(sportid, item.getId());
			boolean itembooking = false;
			if(s2i != null){
				if(StringUtils.equals(s2i.getBooking(),Sport.BOOKING_OPEN)) {
					int count = openTimeTableService.getOpenTimeTableCount(sportid, item.getId(), new Date(), null, null);
					if(count>0) itembooking = true;
				}
			}
			if(!booking && itembooking) booking = true;
			itemBooingMap.put(s2i.getItemid(), itembooking);
		}
		//获取场馆两条最新哇啦
		if(commentCount!=null){
			List<Comment> commentList = commentService.getCommentListByRelatedId(TagConstant.TAG_SPORT,null, sportid, "addtime", 0, commentCount);
			List<Long> memberids = ServiceHelper.getMemberIdListFromBeanList(commentList);
			addCacheMember(model, memberids);
			model.put("commentList", commentList);
		}
		//获取场馆增来过的20个用户
		List<Long> memberidList = new ArrayList<Long>();
		List<MemberInfo> infoList = new ArrayList<MemberInfo>();
		if(memberCount!=null){
			//场馆订过票的用户id
			Timestamp curtime = DateUtil.getMillTimestamp();
			memberidList.addAll(sportService.getMemberListByOrder(sportid, DateUtil.addDay(curtime, -60), 0, memberCount));
			//该场馆下的哇啦
			if(memberidList.size() < memberCount){
				List<Comment> commList = commentService.getCommentListByRelatedId(TagConstant.TAG_SPORT,null, sportid, "addtime", 0, memberCount-memberidList.size());
				memberidList.addAll(ServiceHelper.getMemberIdListFromBeanList(commList));
			}
		}
		for (Long memberid : memberidList) {
			MemberInfo info = daoService.getObject(MemberInfo.class, memberid);
			if(info != null) infoList.add(info);
		}
		model.put("collectstatus", collectstatus);
		model.put("sport", sport);
		model.put("itemList", itemList);
		model.put("booking", booking);
		model.put("infoList", infoList);
		model.put("itemBooingMap", itemBooingMap);
		return getXmlView(model, "api2/sport/sportDetail.vm");
	}
	@RequestMapping("/api2/sport/sportDetailMemberList.xhtml")
	public String sportDetailMemberList(Long sportid, Integer from, Integer maxnum, ModelMap model){
		if(from==null) from = 0;
		if(maxnum==null) maxnum = 20;
		List<Long> memberidList = new ArrayList<Long>();
		List<MemberInfo> infoList = new ArrayList<MemberInfo>();
			//场馆订过票的用户id
		Timestamp curtime = DateUtil.getMillTimestamp();
		memberidList.addAll(sportService.getMemberListByOrder(sportid, DateUtil.addDay(curtime, -60), 0, 1000));
		//该场馆下的哇啦
		List<Comment> commList = commentService.getCommentListByRelatedId(TagConstant.TAG_SPORT,null, sportid, "addtime", 0, 1000);
		memberidList.addAll(ServiceHelper.getMemberIdListFromBeanList(commList));
		memberidList = BeanUtil.getSubList(memberidList, from, maxnum);
		for (Long memberid : memberidList) {
			MemberInfo info = daoService.getObject(MemberInfo.class, memberid);
			if(info != null) infoList.add(info);
		}
		model.put("infoList", infoList);
		return getXmlView(model, "api2/sport/sportDetailMemberList.vm");
	}
	/**
	 * 获取运动场馆信息
	 */
	@RequestMapping("/api2/sport/partner/sport.xhtml")
	public String sportInfo(Long sportid,ModelMap model){
		if(sportid == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		Sport sport = daoService.getObject(Sport.class, sportid);
		if(sport == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "运动场馆不存在！");
		model.put("sport",sport);
		return getXmlView(model, "api/info/sport/partner/sport.vm");
	}
	@RequestMapping("/api2/sport/commentList.xhtml")
	public String commentList(Long sportid, String orderField, String haveface, Integer from, Integer maxnum, ModelMap model){
		if(sportid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "缺少参数");
		Sport sport = daoService.getObject(Sport.class, sportid);
		if(sport == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "场馆不存在！");
		List<Comment> commentList = commentService.getCommentListByRelatedId(TagConstant.TAG_SPORT,null, sportid, orderField, from, maxnum);
		getCommCommentData(model, commentList, haveface);
		return getXmlView(model, "api2/comment/commentList.vm");
	}
	private List<Long> getSportidListByDistance(Long itemid, String citycode, double pointx, double pointy, Double distance){
		List<Long> sportidList = new ArrayList();
		if(distance==null) return sportidList;
		double maxLd =  LongitudeAndLatitude.getLongitude(pointx, pointy, distance);
		double minLd = LongitudeAndLatitude.getLongitude(pointx, pointy, -distance);
		double maxLa = LongitudeAndLatitude.getLatitude(pointy, distance);
		double minLa = LongitudeAndLatitude.getLatitude(pointy, -distance);
		sportidList = sportService.getNearSportList(maxLd, minLd, maxLa, minLa,itemid, citycode);
		return sportidList;
	}
	private List<Sport> getSportListByItemid(Long itemid){
		String query = "select distinct sportid from Sport2Item where itemid=? and booking=?";
		List<Long> sportidList = hibernateTemplate.find(query, itemid, SportProfile.STATUS_OPEN);
		List<Sport> sportList = daoService.getObjectList(Sport.class, sportidList);
		return sportList;
	}
	/**
	 * 获取运动场馆列表
	 * @param citycode
	 * @param memberEncode
	 * @param subwayid
	 * @param name
	 * @param type
	 * @param orderField
	 * @param returnField
	 * @param pointx
	 * @param pointy
	 * @param from
	 * @param maxnum
	 * @param model
	 * @param request
	 * @param countycode
	 * @return
	 * 
	 * 1.默认排序（修改变化）
			1.1支持在线预订的场馆优先往前排，且支持在线预订的场馆部分中的排序，
			1.2优先考虑我感兴趣的场馆和我曾经有过消费的场馆，且在这个维度（我感兴趣+我曾经消费过的）上距离最近的往前排
			1.3剩余不支持预订的场馆按照距离进行排序
	 * 2.按照距离最近排序（没有改变）
			就是按照距离排序，没有其他的逻辑
	 * 3.支持在线预订优先排序（没有改变）
	 		同城支持在线预订的优先排在最前面，且这个维度按照距离排序
	 */
	@RequestMapping("/api2/sport/sportList.xhtml")
	public String sportList(String citycode,String countycode,String memberEncode,Long subwayid,
			Long itemid, Double distance, String name, String type, Double pointx, Double pointy, 
			String orderField, Integer from, Integer maxnum,ModelMap model){
		Member member = null;
		if(StringUtils.isNotBlank(memberEncode)) {
			member = memberService.getMemberByEncode(memberEncode);
			if(member==null) getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "用户不存在！");
		}
		if(StringUtils.isBlank(citycode)) citycode = AdminCityContant.CITYCODE_SH;
		//Double defDis = 5000.00D;
		List<Sport> sportList = new ArrayList<Sport>();
		if(StringUtils.equals(type, "near")){ //附件的场馆  【默认进来】
			if(pointy == null || pointx == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "坐标参数错误！");
			if(itemid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "请选择项目！");
			sportList = getSportListByItemid(itemid);
			sportList = getSortSportList(sportList, pointx, pointy);
		}else if(StringUtils.equals(type, "booking")) { //能够预定的场馆
			//可预定的场馆id
			List<Long> bookingSportidList = openTimeTableService.getCurOttSportIdList(itemid, citycode);
			List<Long> retainSportidList = new ArrayList<Long>(bookingSportidList);
			if(distance!=null){
				List<Long> disIdList = getSportidListByDistance(itemid, citycode, pointx, pointy, distance);
				retainSportidList.retainAll(disIdList);
			}
			sportList = daoService.getObjectList(Sport.class, retainSportidList);
			sportList = getSortSportList(sportList, pointx, pointy);
		}else {
			List<Long> bookingSportidList = openTimeTableService.getCurOttSportIdList(itemid, citycode);
			List<Long> retainSportidList = new ArrayList<Long>(bookingSportidList);
			if(distance!=null){
				List<Long> disIdList = getSportidListByDistance(itemid, citycode, pointx, pointy, distance);
				retainSportidList.retainAll(disIdList);
			}
			sportList = daoService.getObjectList(Sport.class, retainSportidList);
			sportList = getSortSportList(sportList, pointx, pointy);
			if(member != null) {
				List<Long> collSportidList = treasureService.getTreasureIdList(member.getId(), TagConstant.TAG_SPORT, Treasure.ACTION_COLLECT, 0, 100);
				List<Long> orderSportidList = sportService.getSportListByOrder(member.getId(), 0, 20);
				List<Sport> collSportList = daoService.getObjectList(Sport.class, collSportidList);
				List<Sport> orderSportList = daoService.getObjectList(Sport.class, orderSportidList);
				List<Sport> retainCollSportList = new ArrayList<Sport>(collSportList);
				List<Sport> retainOrderSportList = new ArrayList<Sport>(orderSportList);
				retainCollSportList.retainAll(sportList);
				retainOrderSportList.retainAll(sportList);
				retainCollSportList = getSortSportList(retainCollSportList, pointx, pointy);
				retainOrderSportList = getSortSportList(retainOrderSportList, pointx, pointy);
				int i = 0;
				for(Sport sport : retainCollSportList){
					if(sportList.contains(sport)){
						sportList.remove(sport);
						sportList.add(i, sport);
						i++;
					}
				}
				for(Sport sport : retainOrderSportList){
					if(sportList.contains(sport)){
						sportList.remove(sport);
						sportList.add(i, sport);
						i++;
					}
				}
			}
			if(sportList.size()>0){
				List<Long> sportidList = BeanUtil.getBeanPropertyList(sportList, Long.class, "id", true);
				DetachedCriteria query = DetachedCriteria.forClass(Sport2Item.class);
				query.add(Restrictions.not(Restrictions.in("sportid", sportidList)));
				query.add(Restrictions.eq("itemid", itemid));
				query.add(Restrictions.eq("booking", SportProfile.STATUS_OPEN));
				query.setProjection(Projections.distinct(Projections.property("sportid")));
				List<Long> sportidList2 = hibernateTemplate.findByCriteria(query);
				retainSportidList = new ArrayList<Long>(sportidList2);
				if(distance!=null){
					List<Long> disIdList = getSportidListByDistance(itemid, citycode, pointx, pointy, distance);
					retainSportidList.retainAll(disIdList);
				}
				List<Sport> sportList2 = daoService.getObjectList(Sport.class, retainSportidList);
				sportList2 = getSortSportList(sportList2, pointx, pointy);
				sportList.addAll(sportList2);
			}else {
				sportList = getSportListByItemid(itemid);
				List<Long> sportidList = BeanUtil.getBeanPropertyList(sportList, Long.class, "id", true);
				retainSportidList = new ArrayList<Long>(sportidList);
				if(distance!=null){
					List<Long> disIdList = getSportidListByDistance(itemid, citycode, pointx, pointy, distance);
					retainSportidList.retainAll(disIdList);
				}
				sportList = daoService.getObjectList(Sport.class, retainSportidList);
				sportList = getSortSportList(sportList, pointx, pointy);
			}
		}
		filterSportList(citycode, countycode, name, sportList);
		if(StringUtils.equals(orderField, "mark")){//mark
			Collections.sort(sportList, new PropertyComparator("general", false, false));
		}
		sportList = BeanUtil.getSubList(sportList, from, maxnum);
		bookingData(sportList, member, model);
		model.put("sportList", sportList);
		//场馆列表4个相关活动
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getCurrActivityList(citycode, null, TagConstant.TAG_SPORT, null, TagConstant.TAG_SPORTITEM, itemid, null, null, 0, 4);
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		if(code.isSuccess()) activityList = code.getRetval();
		ErrorCode<Integer> result = synchActivityService.getCurrActivityCount(citycode, null, null, TagConstant.TAG_SPORT, null, TagConstant.TAG_SPORTITEM, itemid, null, null);
		int count = 0;
		if(result.isSuccess()) count=result.getRetval();
		model.put("activityList", activityList);
		model.put("count", count);
		return getXmlView(model, "api2/sport/sportList.vm");
	}
	private void filterSportList(String citycode, String countycode, String name, List<Sport> sportList){
		List<Sport> rList = new ArrayList<Sport>();
		if(sportList!=null){
			for(Sport sport : sportList){
				if(!citycode.equals(sport.getCitycode())){
					rList.add(sport);
				}else {
					if(StringUtils.isNotBlank(countycode) && !countycode.equals(sport.getCountycode())){
						rList.add(sport);
					}
				}
			}
		}
		if(StringUtils.isNotBlank(countycode) && sportList!=null) {
			for(Sport sport : sportList){
				if(!citycode.equals(sport.getCitycode()) || !countycode.equals(sport.getCountycode())){
					rList.add(sport);
				}
			}
		}
		if(StringUtils.isNotBlank(name)){
			for(Sport sport : sportList){
				if(sport.getName().indexOf(name)==-1){
					rList.add(sport);
				}
			}
		}
		sportList.removeAll(rList);
	}
	private void bookingData(List<Sport> sportList, Member member, ModelMap model){
		Map<Long,Boolean> bookingMap = new HashMap<Long, Boolean>();
		Map<Long,Boolean> collectMap = new HashMap<Long, Boolean>();
		Treasure treasure = null;
		for (Sport sport : sportList) {
			if(member != null) { 
				treasure = treasureService.getTreasureByTagMemberidRelatedid(TagConstant.TAG_SPORT, member.getId(), sport.getId(), "collect");
				collectMap.put(sport.getId(), treasure != null);
			}else {
				collectMap.put(sport.getId(), false);
			}
			boolean booking = false;
			if(StringUtils.equals(sport.getBooking(),Sport.BOOKING_OPEN)) {
				int count = openTimeTableService.getOpenTimeTableCount(sport.getId(), null, new Date(), null, null);
				if(count>0) booking = true;
			}
			bookingMap.put(sport.getId(), booking);
		}
		model.put("bookingMap", bookingMap);
		model.put("collectMap", collectMap);
	}
	private List<Sport> getSortSportList(List<Sport> list,Double pointx,Double pointy){
		OuterSorter sorter = new OuterSorter<Sport>(false);
		for(Sport sport : list){
			if(StringUtils.isNotBlank(sport.getPointx()) && StringUtils.isNotBlank(sport.getPointy())){
				sorter.addBean(Math.round(LongitudeAndLatitude.getDistance(pointx, pointy, Double.parseDouble(sport.getPointx()), Double.parseDouble(sport.getPointy()))), sport);
			}else{
				sorter.addBean(null, sport);
			}
		}
		return sorter.getAscResult();
	}
	/**
	 * 我的足迹场馆列表
	 * @param memberEncode
	 * @param from
	 * @param maxnum
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/sport/footSportList.xhtml")
	public String zjSportList(String memberEncode, Long memberid, Integer from, Integer maxnum, ModelMap model){
		Member member = null;
		if(memberid!=null){
			 member = daoService.getObject(Member.class, memberid);
		}else{
			 member = memberService.getMemberByEncode(memberEncode);
		}
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "用户不存在！");
		if(from == null) from = 0;
		if(maxnum == null) maxnum = 20;
		List<Long> memberids = new ArrayList<Long>();
		memberids.add(member.getId());
		List<Long> sportidList = null;
		//我订过票的场馆id
		sportidList = sportService.getSportListByOrder(member.getId(), 0, 200);
		//我在运动里发表的哇啦
		List<Comment> commentList = commentService.getCommentListByTags(new String[]{"sport"}, member.getId(), true, 0, 200);
		for (Comment comment : commentList) {
			if(comment.getRelatedid()!=null && !sportidList.contains(comment.getRelatedid())) sportidList.add(comment.getRelatedid());
		}
		//我发表关联运动的活动
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getMemberActivityListByMemberid(member.getId(), null, RemoteActivity.TIME_ALL, "sport", null, 0, 200);
		if(!code.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, code.getMsg());
		activityList = code.getRetval();
		for (RemoteActivity remoteActivity : activityList) {
			if(!sportidList.contains(remoteActivity.getRelatedid())&&remoteActivity.getRelatedid()!=null) sportidList.add(remoteActivity.getRelatedid());
		}
		//得到我的足迹场馆
		List<Sport> sportList = new ArrayList<Sport>();
		for (Long long1 : sportidList) {
			Sport sport = daoService.getObject(Sport.class, long1);
			if(sport != null) sportList.add(sport);
		}
		sportList = BeanUtil.getSubList(sportList, from, maxnum);
		model.put("sportList", sportList);
		return getXmlView(model, "api2/sport/zjSportList.vm");
	}
	
	/**
	 * 返回此场馆曾来过人的列表（可分页）
	 * @param sportid 场馆id
	 * @param from 初始值
	 * @param maxnum 每页显示数
	 * @param returnField 
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/sport/sportMemberList.xhtml")
	public String sportUserList(Long sportid, Integer from, Integer maxnum, ModelMap model){
		Sport sport = daoService.getObject(Sport.class, sportid);
		if(sport == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "场馆不存在！");
		List<Long> memberidList = new ArrayList<Long>();
		List<MemberInfo> infoList = new ArrayList<MemberInfo>();
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		if(from == null) from = 0 ;
		if(maxnum == null) maxnum = 20 ;
		//场馆订单
		Timestamp curtime = DateUtil.addDay(DateUtil.getMillTimestamp(), -180);
		List<Long> ordermemberidList = sportOrderService.getMemberidListBySportid(sportid, curtime, 0, maxnum);
		for (Long mid : ordermemberidList) {
			if(!memberidList.contains(mid)){
				memberidList.add(mid);
			}
		}
		if(memberidList.size()<maxnum){
			//场馆哇啦
			List<Comment> commentList = commentService.getCommentListByRelatedId(TagConstant.TAG_SPORT,null, sportid, null, 0, maxnum-memberidList.size());
			for (Comment comment : commentList) {
				if(!memberidList.contains(comment.getMemberid())){
					memberidList.add(comment.getMemberid());
				}
			}
		}
		if(memberidList.size()<maxnum){
			//场馆活动
			ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByTag(sport.getCitycode(), null, null, TagConstant.TAG_SPORT, sport.getId(), null, null, null, null, null, null, 0, maxnum-memberidList.size());
			if(code.isSuccess()) activityList = code.getRetval();
			for (RemoteActivity activity : activityList) {
				if(!memberidList.contains(activity.getMemberid())){
					memberidList.add(activity.getMemberid());
				}
			}
		}
		infoList = daoService.getObjectList(MemberInfo.class, memberidList);
		Integer year = DateUtil.getCurrentYear();
		Integer infoyear = year; 
		Map<Long, Integer> ageMap = new HashMap<Long, Integer>();
		for (MemberInfo info : infoList) {
			if(StringUtils.isNotBlank(info.getBirthday()) && DateUtil.isValidDate(info.getBirthday())){
				infoyear = Integer.parseInt((info.getBirthday()).substring(0, 4));
				ageMap.put(info.getId(), year - infoyear);
			}
		}
		model.put("ageMap", ageMap);
		model.put("infoList", infoList);
		return getXmlView(model, "api2/sport/sportMemberList.vm");
	}

	@RequestMapping("/api2/sport/interestSportList.xhtml")
	public String interestBarList(String memberEncode, Integer from, Integer maxnum, ModelMap model){
		if(maxnum > 30)maxnum = 30;
		if(StringUtils.isBlank(memberEncode)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "memberEncode不能为空！");
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "用户不存在！");
		List<Long> sportidList = treasureService.getTreasureIdList(member.getId(), TagConstant.TAG_SPORT, Treasure.ACTION_COLLECT, from, maxnum);
		List<Sport> sportList = daoService.getObjectList(Sport.class, sportidList);
		model.put("sportList", sportList);
		return getXmlView(model, "api2/sport/interestSportList.vm");
	}
	

	
	
	/**
	 * 获取项目场馆场次日期列表
	 * @param sportid
	 * @param itemid
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/sport/openDateList.xhtml")
	public String openDateList(Long sportid,Long itemid,ModelMap model, HttpServletRequest request){
		List<OpenTimeTable> itemList = openTimeTableService.getOpenTimeTableList(sportid, itemid, new Date(), null, null, true, 0, 7);
		model.put("itemList", itemList);
		return getXmlView(model, "api/sport/openDateList.vm");
	}
	
	
	
	/**
	 * 获取运动场馆价目表
	 * @param sportid
	 * @param itemid
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/sport/itempriceList.xhtml")
	public String itempriceList(Long sportid,Long itemid, ModelMap model, HttpServletRequest request){
		List<SportPrice> sportPriceList = sportService.getPriceList(sportid,itemid);
		model.put("sportPriceList", sportPriceList);
		return getXmlView(model, "api/sport/itempriceList.vm");
	}
	
	@RequestMapping("/api2/sport/getSportPictureList.xhtml")
	public String getActivityAndCommentPictureList(Long sportid, Integer from, Integer maxnum, ModelMap model){
		if(sportid==null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "用户不存在！" );
		if(from==null) from = 0;
		List<Picture> pictureList = pictureService.getPictureListByRelatedid(TagConstant.TAG_SPORT, sportid, from, maxnum);
		model.put("pictureList", pictureList);
		return getXmlView(model, "api2/sport/pictureList.vm");
	}
	@RequestMapping("/api2/sport/getExploreActivityList.xhtml")
	public String getExploreActivityList(String memberEncode, String citycode,  Integer maxnum, ModelMap model){
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member==null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "用户不存在,或重新登录");
		if(StringUtils.isBlank(citycode)) citycode = AdminCityContant.CITYCODE_SH;
		List<Long> sportidList = getSportidList(member);
		if(maxnum==null) maxnum = 20;
		Set<RemoteActivity> activityList = new HashSet<RemoteActivity>();
		 //有消费的场馆的活动
		if(sportidList.size()>0){
			ErrorCode<List<RemoteActivity>> code = synchActivityService.getCurrActivityListByRelatedidList(citycode, null, TagConstant.TAG_SPORT, sportidList, 0, maxnum);
			if(code.isSuccess()) activityList.addAll(code.getRetval());
		}
		MemberSign myms = nosqlService.getMemberSign(member.getId());
		//周边场馆发布的、尚未结束的召集活动
		if(activityList.size()<maxnum && myms!=null){ 
			double distance = 5000.0d;
			sportidList = getSportidListByDistance(null, citycode, myms.getPointx(), myms.getPointy(), distance);
			if(sportidList.size()>0){
				ErrorCode<List<RemoteActivity>> code = synchActivityService.getCurrActivityListByRelatedidList(citycode, null, TagConstant.TAG_SPORT, sportidList, 0, maxnum-activityList.size());
				if(code.isSuccess()) activityList.addAll(code.getRetval());
			}
		}
		//格瓦拉运动，同城范围内用户参与度最高的、尚未结束的召集内容
		if(activityList.size()<maxnum){
			ErrorCode<List<RemoteActivity>> code = synchActivityService.getCommendActivityList(citycode, TagConstant.TAG_SPORT, null, null, null, 0, maxnum-activityList.size());
			if(code.isSuccess())activityList.addAll(code.getRetval());
		}
		if(activityList.size()<maxnum){
			ErrorCode<List<RemoteActivity>> code = synchActivityService.getHotActivityList(citycode, TagConstant.TAG_SPORT, null, null, null, 0, maxnum-activityList.size());
			if(code.isSuccess())activityList.addAll(code.getRetval());
		}
		putRelateMap(new ArrayList<RemoteActivity>(activityList), model);
		model.put("activityList", activityList);
		return getXmlView(model, "api2/activity/activityList.vm");
	}
	@RequestMapping("/api2/sport/getExploreMemberList.xhtml")
	public String getExploreMemberList(String memberEncode, Integer maxnum, ModelMap model){
		//探索条件1：正在和我在相同场馆的人、即将和我去相同场馆的人
		if(maxnum==null) maxnum = 50;
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member==null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "用户不存在！");
		MemberSign myms = nosqlService.getMemberSign(member.getId());
		List<Long> sportidList = getSportidList(member);
		List<Map> memberList = new ArrayList<Map>();
		if(sportidList.size()>0){
			memberList = getMemberidList(sportidList, member.getId(), maxnum);
		}
		//探索条件2：距离我比较近的朋友
		if(memberList.size()<maxnum && myms!=null){
			List<MemberSign> msList = nosqlService.getMemberSignListByPointR(myms.getPointx(), myms.getPointy(), 5000, 0, maxnum-memberList.size());
			for(MemberSign ms: msList){
				MemberInfo info = daoService.getObject(MemberInfo.class, ms.getMemberid());
				if(info!=null)memberList.add(getExplorMap(info, "distance"));
			}
		}
		//探索条件3:运动和我同城用户中，发布哇啦数、发布召集数最多
		if(memberList.size() < maxnum){
			int x = maxnum - memberList.size();
			Random r = new Random();
			int y = r.nextInt(x)+1;
			List<Long> memberidList1 = synchActivityService.getTopAddMemberidList(AdminCityContant.CITYCODE_SH, TagConstant.TAG_SPORT, y);
			for(Long mid : memberidList1){
				MemberInfo info = daoService.getObject(MemberInfo.class, mid);
				if(info!=null)memberList.add(getExplorMap(info, "topmember"));
			}
			List<Long> memberidList2 = commentService.getTopAddMemberidList(TagConstant.TAG_SPORT, x-y);
			for(Long mid : memberidList2){
				MemberInfo info = daoService.getObject(MemberInfo.class, mid);
				if(info!=null)memberList.add(getExplorMap(info, "topmember"));
			}
		}
		memberList = BeanUtil.getSubList(memberList, 0, maxnum);
		model.put("memberList", memberList);
		return getXmlView(model, "api2/sport/exploreMemberList.vm");
	}
	private Map getExplorMap(MemberInfo info, String reason){
		Map em = new HashMap();
		em.put("reason", reason);
		em.put("sex", info.getSex());
		em.put("id", info.getId());
		em.put("birthday", info.getBirthday());
		em.put("nickname", info.getNickname());
		em.put("headpicUrl", info.getHeadpicUrl());
		return em;
	}
	private List<Long> getSportidList(Member member){
		Criteria c1 = Criteria.where("memberid").is(member.getId());
		Criteria c2 = Criteria.where("tag").is(TagConstant.TAG_SPORT);
		Date curdate = new Date();
		Criteria c3 = Criteria.where("playDate").gte(curdate);
		List<SeeSport> seeSportList = getObjectListByCriteria(SeeSport.class, "playDate", true, 0, 3, c1,c2,c3);
		List<Long> sportidList = BeanUtil.getBeanPropertyList(seeSportList, Long.class, "relatedid", true);
		return sportidList;
	}
	private List<Map> getMemberidList(List<Long> sportidList, Long mid, int maxnum){
		List<Map> result = new ArrayList<Map>();
		if(sportidList==null || sportidList.size()==0) return result;
		Date curdate = new Date();
		Criteria c0 = Criteria.where("memberid").ne(mid);
		Criteria c1 = Criteria.where("tag").is(TagConstant.TAG_SPORT);
		Criteria c2 = Criteria.where("relatedid").is(sportidList.get(0));
		Criteria c3 = Criteria.where("playDate").gte(curdate);
		List<SeeSport> seeSportList = getObjectListByCriteria(SeeSport.class, "playDate", true, 0, maxnum, c0, c1, c2, c3);
		List<Long> memberidList = BeanUtil.getBeanPropertyList(seeSportList, Long.class, "memberid", true);
		for(Long memberid : memberidList){
			MemberInfo info = daoService.getObject(MemberInfo.class, memberid);
			if(info!=null)result.add(getExplorMap(info, "samesport"));
		}
		return result;
	}
	private <T extends Serializable> List<T> getObjectListByCriteria(Class<T> clazz, String orderField, boolean asc, int from, int maxnum, Criteria... criteria){
		Query query = new Query(); 
		for(Criteria c : criteria){
			query.addCriteria(c);
		}
		List result = mongoService.getObjectList(clazz, query.getQueryObject(), orderField, asc, from, maxnum);
		return result;
	}
	
	@RequestMapping("/api2/sport/getActivityAndCommentPictureList.xhtml")
	public String getActivityAndCommentPictureList(String memberEncode, Long memberid, Integer from, Integer maxnum, ModelMap model){
		Member member = null;
		if(memberid!=null){
			member = daoService.getObject(Member.class, memberid);
		}else {
			member = memberService.getMemberByEncode(memberEncode);
		}
		if(member==null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "用户不存在！" );
		if(from==null) from = 0;
		if(maxnum==null) maxnum = 15;
		//List<Picture> pictureList = pictureService.getPictureListByMemberid(member.getId(), TagConstant.TAG_ACTIVITY, 0, 50);
		List<Picture> pictureList = new ArrayList<Picture>();
		List<Comment> commList = commentService.getCommentListByMemberid(member.getId(), 0, 0);
		for (Comment comment : commList) {
			if(comment.getPicturename() != null&&StringUtils.isNotBlank(comment.getPicturename())){ 
				Picture picture = new Picture();
				picture.setId(comment.getId());
				picture.setPicturename(comment.getPicturename());
				pictureList.add(picture);
			}
		}
		pictureList = BeanUtil.getSubList(pictureList, from, maxnum);
		model.put("pictureList", pictureList);
		return getXmlView(model, "api2/sport/pictureList.vm");
	}
}
