package com.gewara.web.action.api2mobile.sport;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.helper.sys.RelateClassHelper;
import com.gewara.json.MemberSign;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.SysMessageAction;
import com.gewara.model.user.Treasure;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.LongitudeAndLatitude;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;

/**
 * 运动API
 * @author taiqichao
 *
 */
@Controller
public class Api2SportTwoController extends BaseApiController {
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	/**
	 * 请求某个场馆的活动、哇啦列表 
	 * @param categoryid 当前请求项目的ID
	 * @param from 开始位置
	 * @param maxnum 每次读取的条数
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/sport/activityAndCommentListByRelatedid.xhtml")
	public String activityAndCommentListByRelatedid(String citycode, String tag, Long relatedid, String category, Long categoryid, Timestamp starttime, Timestamp endtime, String orderField, String asc, int from, int maxnum, ModelMap model){
		if(StringUtils.isBlank(tag) || relatedid==null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "缺少参数！");
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		List<ActivityComment> acList = new ArrayList<ActivityComment>();
		Map<Long, Sport> sportMap = new HashMap<Long, Sport>();
		Map<Long, SportItem> itemMap = new HashMap<Long, SportItem>();
		Map<Long, MemberInfo> infoMap = new HashMap<Long, MemberInfo>();
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByTag(citycode, null, null, tag, relatedid, category, categoryid, starttime, endtime, orderField, asc, from, maxnum);
		if(code.isSuccess()) activityList = code.getRetval();
		for (RemoteActivity remoteActivity : activityList) {
			if(remoteActivity.getRelatedid() != null){
				Sport sport = daoService.getObject(Sport.class, remoteActivity.getRelatedid());
				sportMap.put(remoteActivity.getId(), sport);
			}
			if(remoteActivity.getCategoryid() != null){
				SportItem item = daoService.getObject(SportItem.class, remoteActivity.getCategoryid());
				itemMap.put(remoteActivity.getId(), item);
			}
			MemberInfo info = daoService.getObject(MemberInfo.class, remoteActivity.getMemberid());
			if(info != null) infoMap.put(remoteActivity.getId(), info);
			ActivityComment ac = new ActivityComment(remoteActivity);
			acList.add(ac);
		}
		Map<Long, Object> relateMap = new HashMap<Long, Object>();
		List<Comment> commentList = commentService.getCommentListByRelatedId(tag,null, relatedid, null, from, maxnum);
		for (Comment comment : commentList) {
			MemberInfo info = daoService.getObject(MemberInfo.class, comment.getMemberid());
			if(info != null) infoMap.put(comment.getId(), info);
			if(StringUtils.isNotBlank(comment.getTag()) && comment.getRelatedid()!=null){
				Object relate = relateService.getRelatedObject(comment.getTag(), comment.getRelatedid());
				if(relate!=null){ 
					if(relate instanceof RemoteActivity){
						RemoteActivity ra = (RemoteActivity)relate;
						relate = relateService.getRelatedObject(ra.getTag(), ra.getRelatedid());
						if(relate!=null) relateMap.put(comment.getId(), relate);
					}else if(ServiceHelper.isTag(comment.getTag())){
						relateMap.put(comment.getId(), relate);
					}
				}
			}
			ActivityComment ac = new ActivityComment(comment);
			acList.add(ac);
		}
		Collections.sort(acList, new MultiPropertyComparator(new String[]{"addtime"}, new boolean[]{false}));
		acList = BeanUtil.getSubList(acList, from, maxnum);
		model.put("sportMap", sportMap);
		model.put("relateMap", relateMap);
		model.put("itemMap", itemMap);
		model.put("infoMap", infoMap);
		model.put("acList", acList);
		return getXmlView(model, "api2/sport/commentAndActivityList.vm");
	}
	
	/**
	 * 根据用户查询哇啦和活动
	 * @param memberEncode 用户
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/sport/commentAndActivityList.xhtml")
	public String memberidComment(String memberEncode, String tag, Integer from, Integer maxnum, ModelMap model){
		if(StringUtils.isBlank(memberEncode)){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "memberEncode不能为空！");
		}
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member == null){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "用户不存在！");
		}
		if(from == null) from = 0 ;
		if(maxnum == null) maxnum = 20 ;
		List<ActivityComment> acList = new ArrayList<ActivityComment>();
		Map<Long, MemberInfo> infoMap = new HashMap<Long, MemberInfo>();
		//根据用户查询哇啦
		List<Comment> commentList = commentService.getCommentListByTags(new String[]{tag, TagConstant.TAG_ACTIVITY}, member.getId(), true, from, maxnum);
		Map<Long, Object> relateMap = new HashMap<Long, Object>();
		for (Comment comment : commentList) {
			MemberInfo info = daoService.getObject(MemberInfo.class, comment.getMemberid());
			if(info != null) infoMap.put(comment.getId(), info);
			if(StringUtils.isNotBlank(comment.getTag()) && comment.getRelatedid()!=null){
				Object relate = relateService.getRelatedObject(comment.getTag(), comment.getRelatedid());
				if(relate!=null){ 
					if(relate instanceof RemoteActivity){
						RemoteActivity ra = (RemoteActivity)relate;
						relate = relateService.getRelatedObject(ra.getTag(), ra.getRelatedid());
						if(relate!=null) relateMap.put(comment.getId(), relate);
					}else if(ServiceHelper.isTag(comment.getTag())){
						relateMap.put(comment.getId(), relate);
					}
				}
			}
			ActivityComment ac = new ActivityComment(comment);
			acList.add(ac);
		}
		model.put("relateMap", relateMap);
		//根据用户查询活动
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getRemoteActivityByMemberid(member.getId(), tag, from, maxnum);
		if(code.isSuccess()) activityList = code.getRetval();
		Map<Long, Sport> sportMap = new HashMap<Long, Sport>();
		Map<Long, SportItem> itemMap = new HashMap<Long, SportItem>();
		for (RemoteActivity activity : activityList) {
			if(activity.getRelatedid() != null){
				Sport sport = daoService.getObject(Sport.class, activity.getRelatedid());
				sportMap.put(activity.getId(), sport);
			}
			if(activity.getCategoryid() != null){
				SportItem item = daoService.getObject(SportItem.class, activity.getCategoryid());
				itemMap.put(activity.getId(), item);
			}
			MemberInfo info = daoService.getObject(MemberInfo.class, activity.getMemberid());
			if(info != null) infoMap.put(activity.getId(), info);
			ActivityComment ac = new ActivityComment(activity);
			acList.add(ac);
		}
		model.put("sportMap", sportMap);
		model.put("itemMap", itemMap);
		model.put("infoMap", infoMap);
		Collections.sort(acList, new MultiPropertyComparator(new String[]{"addtime"}, new boolean[]{false}));
		acList = BeanUtil.getSubList(acList,from,maxnum);
		model.put("acList", acList);
		return getXmlView(model, "api2/sport/commentAndActivityList.vm");
	}
	
	/**
	 * 根据用户关注的人查询哇啦和活动
	 * @param memberEncode 用户
	 * @param model
	 * @param memberId 指定用户ID modified by liuyunxin on 2012-11-06
	 * @return
	 */
	@RequestMapping("/api2/sport/treasureCommentAndActivityList.xhtml")
	public String treasureCommentAndActivityList(String citycode, String memberEncode, Long memberid, String tag, Integer from, Integer maxnum, ModelMap model){
		if(StringUtils.isBlank(memberEncode)){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "memberEncode不能为空！");
		}
		if(StringUtils.isBlank(citycode)) citycode = AdminCityContant.CITYCODE_SH;
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member == null){
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "用户不存在,或重新登录");
		}
		if(from == null) from = 0 ;
		if(maxnum == null) maxnum = 20 ;
		//我关注过的人
		List<Long> memberidList=new ArrayList<Long>();
		List<Treasure> treasureList = blogService.getTreasureListByMemberId(member.getId(), new String[]{Treasure.TAG_MEMBER},null, null, 0, 100, Treasure.ACTION_COLLECT);
		for (Treasure treasure : treasureList) {
			if (RelateClassHelper.getRelateClazz(treasure.getTag()) != null) {
				MemberInfo memberInfo = daoService.getObject(MemberInfo.class, treasure.getRelatedid());
				if(memberInfo != null){ 
					if(memberid==null){
						memberidList.add(memberInfo.getId());
					}else {
						if(memberInfo.getId().equals(memberid)){
							memberidList.add(memberInfo.getId());
						}
					}
				}
			}
		}
		if(memberid==null) memberidList.add(member.getId());
		List<ActivityComment> acList = new ArrayList<ActivityComment>();
		Map<Long, MemberInfo> infoMap = new HashMap<Long, MemberInfo>();
		Map<Long, Sport> sportMap = new HashMap<Long, Sport>();
		Map<Long, SportItem> itemMap = new HashMap<Long, SportItem>();
		Map<Long, Object> relateMap = new HashMap<Long, Object>();
		Timestamp curtime = DateUtil.getMillTimestamp();
		List<Comment> commentList = commentService.getCommentListByTagMemberids(new String[]{tag, TagConstant.TAG_ACTIVITY}, memberidList, DateUtil.addDay(curtime, -360), curtime, 0, 150);
		for (Comment comment : commentList) {
			MemberInfo info = daoService.getObject(MemberInfo.class, comment.getMemberid());
			if(info != null) infoMap.put(comment.getId(), info);
			boolean isAdd = true;
			if(StringUtils.isNotBlank(comment.getTag()) && comment.getRelatedid()!=null){
				Object relate = relateService.getRelatedObject(comment.getTag(), comment.getRelatedid());
				if(relate!=null){
					if(StringUtils.equals(comment.getTag(), TagConstant.TAG_ACTIVITY)){
						RemoteActivity ra = (RemoteActivity)relate;
						relate = relateService.getRelatedObject(ra.getTag(), ra.getRelatedid());
						if(relate!=null) relateMap.put(comment.getId(), relate);
						if(StringUtils.isNotBlank(ra.getTag()) && !StringUtils.equals(ra.getTag(),tag)) isAdd = false;
					}else if(ServiceHelper.isTag(comment.getTag())){
						relateMap.put(comment.getId(), relate);
					}
				}
			}
			if(isAdd){
				ActivityComment ac = new ActivityComment(comment);
				acList.add(ac);
			}
		}
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByMemberidList(citycode, null, null, memberidList, "N", tag, null, null, null, 0, 100);
		if(code.isSuccess()) activityList = code.getRetval();
		for (RemoteActivity activity : activityList) {
			if(activity.getRelatedid() != null){
				Sport sport = daoService.getObject(Sport.class, activity.getRelatedid());
				sportMap.put(activity.getId(), sport);
			}
			if(activity.getCategoryid() != null){
				SportItem item = daoService.getObject(SportItem.class, activity.getCategoryid());
				itemMap.put(activity.getId(), item);
			}
			MemberInfo info = daoService.getObject(MemberInfo.class, activity.getMemberid());
			if(info != null) infoMap.put(activity.getId(), info);
			ActivityComment ac = new ActivityComment(activity);
			acList.add(ac);
		}
		model.put("relateMap", relateMap);
		model.put("sportMap", sportMap);
		model.put("itemMap", itemMap);
		model.put("infoMap", infoMap);
		Collections.sort(acList, new MultiPropertyComparator(new String[]{"addtime"}, new boolean[]{false}));
		acList = BeanUtil.getSubList(acList,from, maxnum);
		model.put("acList", acList);
		return getXmlView(model, "api2/sport/commentAndActivityList.vm");
	}
	
	/**
	 * 我附近的活动和用户
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/sport/nearActivityAndMemberList.xhtml")
	public String nearActivityAndMemberList( String citycode, Double pointx, Double pointy, ModelMap model){
		if(StringUtils.isBlank(citycode)) citycode = AdminCityContant.CITYCODE_SH;
		List<ActivityMemberInfo> amList = new ArrayList<ActivityMemberInfo>();
		//我附近的场馆
		List<Long> sportidList = null;
		double distance = 5000.00;
		if(pointy == null || pointx == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "坐标参数错误！");
		double maxLd =  LongitudeAndLatitude.getLongitude(pointx, pointy, distance);
		double minLd = LongitudeAndLatitude.getLongitude(pointx, pointy, -distance);
		double maxLa = LongitudeAndLatitude.getLatitude(pointy, distance);
		double minLa = LongitudeAndLatitude.getLatitude(pointy, -distance);
		sportidList = sportService.getNearSportList(maxLd, minLd, maxLa, minLa, null, citycode);
		List<Sport> sportList = daoService.getObjectList(Sport.class, sportidList);
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		for (Sport sport : sportList) {
			ActivityMemberInfo am = new ActivityMemberInfo(sport);
			amList.add(am);
		}
		//我附近的用户
		List<MemberSign> memberSignList = nosqlService.getMemberSignListByPointR(pointx, pointy, (long)distance, 0, 20);
		Integer year = DateUtil.getCurrentYear();
		Integer infoyear = year;
		Map<Long, Integer> ageMap = new HashMap<Long, Integer>();
		for (MemberSign memberSign : memberSignList) {
			MemberInfo memberinfo = daoService.getObject(MemberInfo.class, memberSign.getMemberid());
			ActivityMemberInfo am = new ActivityMemberInfo(memberinfo, memberSign);
			amList.add(am);
			if(StringUtils.isNotBlank(memberinfo.getBirthday()) && DateUtil.isValidDate(memberinfo.getBirthday())){
				infoyear = Integer.parseInt((memberinfo.getBirthday()).substring(0, 4));
				ageMap.put(memberinfo.getId(), year - infoyear);
			}
		}
		getSortList(amList,pointx,pointy);
		//给RemoteActivity添加活动
		Map<Long, SportItem> itemMap = new HashMap<Long, SportItem>();
		Map<Long, MemberInfo> infoMap = new HashMap<Long, MemberInfo>();
		Map<Long, Sport> sportMap = new HashMap<Long, Sport>();
		ErrorCode<List<RemoteActivity>> acode = synchActivityService.getCurrActivityListByRelatedidList(citycode, null, TagConstant.TAG_SPORT, sportidList, 0, 20);
		if(acode.isSuccess()){
			activityList = acode.getRetval();
		}
		for (RemoteActivity remoteActivity : activityList) {
			SportItem item = daoService.getObject(SportItem.class, remoteActivity.getCategoryid());
			if(item != null) itemMap.put(remoteActivity.getId(), item);
			MemberInfo info = daoService.getObject(MemberInfo.class, remoteActivity.getMemberid());
			if(info != null)infoMap.put(remoteActivity.getId(), info);
			Sport sport = daoService.getObject(Sport.class, remoteActivity.getRelatedid());
			if(item != null) sportMap.put(remoteActivity.getId(), sport);
		}
		model.put("itemMap", itemMap);
		model.put("infoMap", infoMap);
		model.put("ageMap", ageMap);
		model.put("amList", amList);
		model.put("sportMap", sportMap);
		model.put("activityList", activityList);
		model.put("sportidLilst", StringUtils.join(sportidList, ","));
		return getXmlView(model, "api2/sport/nearActivityAndMemberList.vm");
	}
	
	private void getSortList(List<ActivityMemberInfo> list,Double pointx, Double pointy){
		for(ActivityMemberInfo am : list){
			if(StringUtils.isNotBlank(am.getPointx()) && StringUtils.isNotBlank(am.getPointy())){
				am.setOrderField(Math.round(LongitudeAndLatitude.getDistance(pointx, pointy, Double.parseDouble(am.getPointx()), Double.parseDouble(am.getPointy()))));
			}
		}
		Collections.sort(list, new PropertyComparator("orderField", false, true));
	}
	
	
	
	public class ActivityComment{
		private RemoteActivity activity;
		private Comment comment;
		private SysMessageAction sysMessage;
		private Timestamp addtime;
		private String clazztype;
		public ActivityComment(RemoteActivity activity){
			this.activity = activity;
			this.addtime = activity.getAddtime();
			this.clazztype = "activity";
		}
		public ActivityComment(Comment comment){
			this.comment = comment;
			this.addtime = comment.getAddtime();
			this.clazztype = "comment";
		}
		public ActivityComment(SysMessageAction sysMessage){
			this.sysMessage = sysMessage;
			this.addtime = sysMessage.getAddtime();
			this.clazztype = "sysmessage";
		}
		public RemoteActivity getActivity() {
			return activity;
		}

		public void setActivity(RemoteActivity activity) {
			this.activity = activity;
		}

		public Comment getComment() {
			return comment;
		}
		public void setComment(Comment comment) {
			this.comment = comment;
		}
		public Timestamp getAddtime() {
			return addtime;
		}
		public void setAddtime(Timestamp addtime) {
			this.addtime = addtime;
		}
		public String getClazztype() {
			return clazztype;
		}
		public void setClazztype(String clazztype) {
			this.clazztype = clazztype;
		}
		public SysMessageAction getSysMessage() {
			return sysMessage;
		}
		public void setSysMessage(SysMessageAction sysMessage) {
			this.sysMessage = sysMessage;
		}
	}
	
	public class ActivityMemberInfo{
		private Sport sport;
		private RemoteActivity activity;
		private MemberSign memberSign;
		private MemberInfo memberinfo;
		private String clazztype;
		private String pointx;
		private String pointy;
		private String address;
		private Comparable orderField; //作为临时排序字段使用
		public ActivityMemberInfo(MemberInfo memberinfo, MemberSign memberSign){
			this.memberinfo = memberinfo;
			this.pointx = memberSign.getPointx()+"";
			this.pointy = memberSign.getPointy()+"";
			this.clazztype = "memberinfo";
			this.address = memberSign.getAddress();
		}
		public ActivityMemberInfo(Sport sport){
			this.sport = sport;
			this.pointx = sport.getPointx();
			this.pointy = sport.getPointy();
			this.clazztype = "sport";
		}
		public MemberInfo getMemberinfo() {
			return memberinfo;
		}
		public void setMemberinfo(MemberInfo memberinfo) {
			this.memberinfo = memberinfo;
		}
		public Sport getSport() {
			return sport;
		}
		public void setSport(Sport sport) {
			this.sport = sport;
		}
		public String getPointx() {
			return pointx;
		}
		public void setPointx(String pointx) {
			this.pointx = pointx;
		}
		public String getPointy() {
			return pointy;
		}
		public void setPointy(String pointy) {
			this.pointy = pointy;
		}
		public Comparable getOrderField() {
			return orderField;
		}
		public void setOrderField(Comparable orderField) {
			this.orderField = orderField;
		}
		public MemberSign getMemberSign() {
			return memberSign;
		}
		public void setMemberSign(MemberSign memberSign) {
			this.memberSign = memberSign;
		}
		public RemoteActivity getActivity() {
			return activity;
		}
		public void setActivity(RemoteActivity activity) {
			this.activity = activity;
		}
		public String getClazztype() {
			return clazztype;
		}
		public void setClazztype(String clazztype) {
			this.clazztype = clazztype;
		}
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
	}
	
	
	public class RemoteActivityCount{
		private RemoteActivity activity;
		private Integer count;
		public RemoteActivityCount(RemoteActivity activity, Integer count){
			this.activity = activity;
			this.count = count;
		}
		public Integer getCount() {
			return count;
		}
		public void setCount(Integer count) {
			this.count = count;
		}
		public RemoteActivity getActivity() {
			return activity;
		}
		public void setActivity(RemoteActivity activity) {
			this.activity = activity;
		}
	}
	
	
}