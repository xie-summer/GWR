package com.gewara.web.action.admin.member;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.DramaConstant;
import com.gewara.constant.ManageConstant;
import com.gewara.constant.PointConstant;
import com.gewara.constant.SysAction;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.sys.AdminCityHelper;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.acl.User;
import com.gewara.model.bbs.Diary;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Movie;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.Point;
import com.gewara.model.user.PointHist;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.member.PointService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CacheDataService;
import com.gewara.untrans.CommentService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.ObjectId;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.bbs.Comment;

@Controller
public class PointAdminController extends BaseAdminController {
	@Autowired
	@Qualifier("pointService")
	private PointService pointService;

	public void setPointService(PointService pointService) {
		this.pointService = pointService;
	}

	@Autowired
	@Qualifier("userMessageService")
	private UserMessageService userMessageService;

	public void setUserMessageService(UserMessageService userMessageService) {
		this.userMessageService = userMessageService;
	}

	@Autowired
	@Qualifier("cacheDataService")
	private CacheDataService cacheDataService;

	@Autowired
	@Qualifier("commentService")
	private CommentService commentService;

	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}

	@Autowired
	@Qualifier("mongoService")
	private MongoService mongoService;

	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}

	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	/**
	 * 后台查询积分列表
	 * 
	 * @param model
	 * @param pageNo
	 * @param id
	 * @param nickname
	 * @return
	 */
	@RequestMapping("/admin/point/pointList.xhtml")
	public String queryPontList(ModelMap model, Long id, String nickname, Integer pageNo) {
		if(id!=null || StringUtils.isNotBlank(nickname)){
			List<Member> memberList = new ArrayList<Member>();
			if(id!=null){
				Member member = daoService.getObject(Member.class, id);
				memberList.add(member);
			}else{
				memberList = memberService.searchMember(nickname, 0, 1000);
			}
			List<Long> idList = BeanUtil.getBeanPropertyList(memberList, Long.class, "id", true);
			Map<Long, Integer> pointMap = daoService.getObjectPropertyMap(MemberInfo.class, "id", "pointvalue", idList);
			model.put("memberList", memberList);
			model.put("pointMap", pointMap);
		}
		return "admin/point/pointList.vm";
	}

	@RequestMapping("/admin/point/freeback.xhtml")
	public String freeback() {
		return "admin/point/freeback.vm";
	}

	/**
	 * 根据用户id查询这个用户的全部积分信息
	 */
	@RequestMapping("/admin/point/pointDetailList.xhtml")
	public String queryPointListById(ModelMap model, Long id, Integer pageNo) {
		Timestamp addtime = cacheDataService.getHistoryUpdateTime(CacheConstant.KEY_POINTUPDATE);
		if (pageNo == null)
			pageNo = 0;
		int pointCount = pointService.getPointCountByMemberid(id, null, addtime, null);
		int pointHistCount = pointService.getPointHistCountByCondition(id, null, 0, addtime, null);
		int rowsPerPage = 30;
		int firstPerPage = pageNo * rowsPerPage;
		PageUtil pageUtil = new PageUtil(pointCount + pointHistCount, rowsPerPage, pageNo, "admin/point/pointDetailList.xhtml");
		Map params = new HashMap();
		params.put("id", id);
		pageUtil.initPageInfo(params);
		List<Point> pointList = pointService.getPointListByMemberid(id, null, addtime, null, null, firstPerPage, rowsPerPage);
		if (pointList.size() < 30 && pointList.size() > 0 ) {
			rowsPerPage = rowsPerPage - pointList.size();
			firstPerPage = 0;
		}else if (pointList.size() == 0) {
			firstPerPage = firstPerPage - pointCount; 
		}
		List<PointHist> pointHists = new ArrayList<PointHist>();
		if (pointList.size() < 30) {
			pointHists = pointService.getPointHistListByMemberidAndPointValue(id, null, 0,addtime, null, null, firstPerPage, rowsPerPage);
		}
		Map<Long, User> memberMap = new HashMap<Long, User>();
		for (Point point : pointList) {
			User user = daoService.getObject(User.class, point.getAdminid());
			if (user != null)
				memberMap.put(point.getId(), user);
		}
		for (PointHist point : pointHists) {
			User user = daoService.getObject(User.class, point.getAdminid());
			if (user != null)
				memberMap.put(point.getId(), user);
		}
		Member memberPoint = daoService.getObject(Member.class, id);
		model.put("memberPoint", memberPoint);
		model.put("userMap", memberMap);
		model.put("pointList", pointList);
		model.put("pointHists", pointHists);
		model.put("pageUtil", pageUtil);
		return "admin/point/pointDetailList.vm";
	}

	/**
	 * 转发的添加积分信息页面
	 */
	@RequestMapping("/admin/point/addPointInfo.xhtml")
	public String forwardAddPointInfo(ModelMap model) {
		model.put("deptMap", ManageConstant.deptMap);
		model.put("applyMap", ManageConstant.applyMap);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		return "admin/point/addPointInfo.vm";
	}

	// 获取当前系统中的总积分
	private Map getSumPoint() {
		Map map = new HashMap();
		Integer sumPoint = pointService.getSumPoint();
		List listTag = pointService.getPointTagList();
		map.put("listTag", listTag);
		map.put("sumPoint", sumPoint);
		return map;
	}

	/**
	 * 查询积分信息
	 */
	@RequestMapping("/admin/point/searchPointInfo.xhtml")
	public String searchPointList(ModelMap model, Timestamp startTime, Timestamp endTime, String tag, Integer valueStart, Integer valueEnd,
			Integer pageNo, String type) {
		Map params = new HashMap();
		Map mrParams = new HashMap();
		if ("para".equals(type)) {
			mrParams.put("valueStart", valueStart);
			mrParams.put("valueEnd", valueEnd);
			mrParams.put("startTime", startTime);
			mrParams.put("endTime", endTime);
			mrParams.put("tag", tag);
		}
		if (valueStart == null)
			valueStart = 0;
		if (valueEnd == null)
			valueEnd = 5000000;
		if (startTime == null)
			startTime = DateUtil.parseTimestamp("2007-01-01 00:00:00");
		if (endTime == null)
			endTime = new Timestamp(System.currentTimeMillis());
		params.put("tag", tag);
		params.put("valueStart", valueStart);
		params.put("valueEnd", valueEnd);
		params.put("startTime", startTime);
		params.put("endTime", endTime);
		if (pageNo == null)
			pageNo = 0;
		int maxNum = 50;
		Map<Long, Integer> memberPointMap = new HashMap<Long, Integer>();
		List<Map> variableList = pointService.getPointVariableList(startTime, endTime, tag, valueStart, valueEnd, pageNo * maxNum, maxNum);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(variableList));
		for (Map map : variableList) {
			memberPointMap.put(new Long(map.get("memberid") + ""), daoService.getObject(MemberInfo.class, new Long(map.get("memberid") + ""))
					.getPointvalue());
		}
		model.put("memberPointMap", memberPointMap);
		model.put("variableList", variableList);
		Map map = pointService.getPointVariableMap(startTime, endTime, tag, valueStart, valueEnd);
		Integer paypoint = 0;
		if (map.get("paypoint") != null)
			paypoint = Math.abs(Integer.parseInt(map.get("paypoint") + ""));
		NumberFormat nfPercent = NumberFormat.getPercentInstance();
		double sum = Integer.parseInt(getSumPoint().get("sumPoint") + "");
		model.put("sleepLv", nfPercent.format((sum - paypoint) / sum));
		model.put("pointMap", map);
		Integer count = pointService.getPointVariableCount(startTime, endTime, tag, valueStart, valueEnd);
		model.put("param", params);
		model.put("mrParams", mrParams);
		PageUtil pageUtil = new PageUtil(count, maxNum, pageNo, "admin/point/searchPointInfo.xhtml");
		pageUtil.initPageInfo(params);
		model.putAll(getSumPoint());
		model.put("pageUtil", pageUtil);
		return "admin/point/searchPointList.vm";
	}

	@RequestMapping("/admin/point/pointSearchDetailList.xhtml")
	public String searchDetailPointList(ModelMap model, Long memberid, Timestamp startTime, Timestamp endTime, String tag, String type, Integer pageNo) {
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 30;
		List<Point> pointList = pointService.getPointListByIdAndType(memberid, startTime, endTime, tag, type, pageNo * rowsPerPage, rowsPerPage);
		Integer count = pointService.getPointByIdAndTypeCount(memberid, startTime, endTime, tag, type);
		Map<Long, Member> memberMap = new HashMap<Long, Member>();
		for (Point point : pointList) {
			Member member = daoService.getObject(Member.class, point.getAdminid());
			memberMap.put(point.getId(), member);
		}
		Member memberPoint = daoService.getObject(Member.class, memberid);
		model.put("memberPoint", memberPoint);
		model.put("memberMap", memberMap);
		Map params = new HashMap();
		params.put("memberid", memberid);
		params.put("startTime", startTime);
		params.put("endTime", endTime);
		params.put("tag", tag);
		params.put("type", type);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/point/pointSearchDetailList.xhtml");
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("pointList", pointList);
		return "admin/point/pointDetailList.vm";
	}

	@RequestMapping("/admin/point/freebackcomment.xhtml")
	public String freebackcomment(ModelMap model, Integer pageNo, String tag) {
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 30;
		List<Comment> commentList = commentService.pointByFreeBackCommentList(tag, pageNo * rowsPerPage, rowsPerPage);
		Integer count = commentService.pointByFreeBackCommentCount(tag);
		Map params = new HashMap();
		params.put("tag", tag);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/point/freebackcomment.xhtml");
		pageUtil.initPageInfo(params);
		model.put("commentList", commentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		model.put("pageUtil", pageUtil);
		return "admin/message/commentdiary.vm";
	}

	@RequestMapping("/admin/point/freebackdiary2.xhtml")
	public String freeBackList2(ModelMap model) {
		Timestamp t = new Timestamp(System.currentTimeMillis());
		Timestamp dtime = DateUtil.addDay(t, -90);// 在3个月之内点评
		String diaryHql = "from Diary c where c.category=? and c.categoryid>0 and c.addtime>=? and instr(c.flag,'point')<=0 order by c.addtime desc";
		List<Diary> dList = hibernateTemplate.find(diaryHql, "movie", dtime);
		String orderHql = null;
		List list = null;
		Map<Long, Long> mpiMap = new HashMap<Long, Long>();
		List<Diary> diaryList = new ArrayList<Diary>();
		for (Diary diary : dList) {
			orderHql = "select t.mpid from TicketOrder t where t.status=? and t.memberid=? "
					+ "and exists(select opi.id from OpenPlayItem opi where opi.movieid=? and opi.mpid=t.mpid) "
					+ "and not exists(select p.id from Point p where p.tag=? and p.memberid=? and p.tagid=t.mpid)";
			list = hibernateTemplate.find(orderHql, OrderConstant.STATUS_PAID_SUCCESS, diary.getMemberid(), diary.getCategoryid(),
					PointConstant.FREEBACK_DIARYMOVIE, diary.getMemberid());
			if (!list.isEmpty() && list.size() > 0) {
				diaryList.add(diary);
				mpiMap.put(diary.getId(), new Long(list.get(0) + ""));
			}
		}
		model.put("diaryList", diaryList);
		model.put("mpiMap", mpiMap);
		return "admin/message/freebackdiary.vm";
	}

	@RequestMapping("/admin/point/freebackdiary.xhtml")
	public String freeBackList(ModelMap model, Integer pageNo, String tag) {
		String pointType = "";
		String tagType = "";
		if (TagConstant.TAG_MOVIE.equals(tag)) {
			tagType = "cinema";
			pointType = PointConstant.FREEBACK_DIARYMOVIE;
		} else if (DramaConstant.TYPE_DRAMA.equals(tag)) {
			pointType = PointConstant.FREEBACK_DIARYDRAMA;
			tagType = Theatre.TAG_THEATRE;
		}
		if (pageNo == null || pageNo < 0)
			pageNo = 0;
		int rowsPerPage = 30;
		int from = pageNo * rowsPerPage;
		Integer count = pointService.getDiaryCount(tagType, pointType);
		if (pageNo > count)
			pageNo = count;
		List<Diary> diaryList = pointService.getDiaryList(tagType, pointType, from, rowsPerPage);
		Map params = new HashMap();
		params.put("tag", tag);
		List<Long> movieIdList = BeanUtil.getBeanPropertyList(diaryList, Long.class, "categoryid", true);
		if (TagConstant.TAG_MOVIE.equals(tag)) {
			List<Movie> movieList = daoService.getObjectList(Movie.class, movieIdList);
			Map<Long, Movie> movieMap = BeanUtil.beanListToMap(movieList, "id");
			model.put("movieMap", movieMap);
		} else if (DramaConstant.TYPE_DRAMA.equals(tag)) {
			List<Drama> movieList = daoService.getObjectList(Drama.class, movieIdList);
			Map<Long, Drama> movieMap = BeanUtil.beanListToMap(movieList, "id");
			model.put("movieMap", movieMap);
		}
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/point/freebackdiary.xhtml");
		pageUtil.initPageInfo(params);
		model.put("diaryList", diaryList);
		model.put("pageUtil", pageUtil);
		return "admin/message/freebackdiary.vm";
	}


	@RequestMapping("/admin/point/searchexpendpoint.xhtml")
	public String searchExpendPoint(ModelMap model, String startTime, String endTime, String tag) {
		List list = pointService.getPointExpendDetail(startTime, endTime, tag);
		Map jsonReturn = new HashMap();
		jsonReturn.put("list", list);
		return showJsonSuccess(model, jsonReturn);
	}

	@RequestMapping("/admin/point/goodsOrderToPointByInvite.xhtml")
	public String goodsOrderToPointByInvite(ModelMap model, Timestamp beginTime, Timestamp endTime) {
		if (beginTime == null)
			beginTime = DateUtil.parseTimestamp("2011-01-01 00:00:00");
		if (endTime == null)
			endTime = new Timestamp(System.currentTimeMillis());
		String hql = "select new map(count(p) as count,sum(p.point) as sumpoint) from Point p where (p.tag = ? or p.tag = ?) and p.addtime between ? and ?";
		List<Map> pointList = hibernateTemplate.find(hql, PointConstant.TAG_INVITED_FRIEND_TICKET, PointConstant.TAG_INVITED_FRIEND_GOODS, beginTime,
				endTime);
		model.put("count", pointList.get(0).get("count"));
		model.put("sumpoint", pointList.get(0).get("sumpoint") == null ? 0 : pointList.get(0).get("sumpoint"));
		return "admin/point/ticketOrderToPointByInvite.vm";
	}

	@RequestMapping("/admin/point/ajax/addPointInfo.xhtml")
	public String addPointInfo(String statflag, HttpServletRequest request, ModelMap model) {
		GewaraUser user = getLogonUser();
		List list = new ArrayList();
		list.add("update_place");
		list.add("perfect_place");
		list.add("add_place");
		list.add("corr_place");
		list.add("update_item");
		list.add("perfect_item");
		list.add("add_item");
		list.add("corr_item");
		for (int i = 1; i <= 10; i++) {
			if (StringUtils.isNotBlank(request.getParameter("memberid" + i)) && StringUtils.isNotBlank(request.getParameter("point1" + i + ""))) {
				try {
					Integer.parseInt(request.getParameter("point1" + i));
					Integer.parseInt(request.getParameter("memberid" + i));

					if (list.contains(request.getParameter("tag" + i + "").toString())) {
						if (Long.parseLong(request.getParameter("point1" + i)) > 50
								|| Long.parseLong(request.getParameter("point1" + i + "") + "") < 0) {
							return showJsonError(model, "您违反了注意事项第1项！");
						}
					} else {
						if ("exchange".equals(request.getParameter("tag" + i).toString())
								&& (Long.parseLong(request.getParameter("point1" + i + "") + "") > 0 || Long.parseLong(request.getParameter("point1"
										+ i + "")
										+ "") < -10000)) {
							return showJsonError(model, "您违反了注意事项第3项！");
						}
						if ("content".equals(request.getParameter("tag" + i).toString())
								&& (Long.parseLong(request.getParameter("point1" + i + "") + "") > 10000 || Long.parseLong(request
										.getParameter("point1" + i + "") + "") < -10000)) {
							return showJsonError(model, "您违反了注意事项第4项！");
						}
						if (!list.contains(request.getParameter("tag" + i + "").toString())
								&& !"content".equals(request.getParameter("tag" + i).toString())
								&& !"exchange".equals(request.getParameter("tag" + i).toString())
								&& (Long.parseLong(request.getParameter("point1" + i + "") + "") > 1000 || Long.parseLong(request
										.getParameter("point1" + i + "") + "") < 0)) {
							return showJsonError(model, "您违反了注意事项第5项！");
						}
					}
				} catch (Exception e) {
					return showJsonError(model, "用户ID和积分只能输入数字！");
				}
			}
		}
		String errMsg = "";
		Timestamp cur = DateUtil.getCurFullTimestamp();
		for (int i = 1; i <= 10; i++) {
			String strMemberid = request.getParameter("memberid" + i);
			String strPoint = request.getParameter("point1" + i);
			if (StringUtils.isNotBlank(strMemberid) && StringUtils.isNotBlank(strPoint)) {
				MemberInfo info = daoService.getObject(MemberInfo.class, new Long((request.getParameter("memberid" + i))));
				if (info != null) {
					dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "管理员【" + user.getUsername() + "】在【" + DateUtil.formatDate(new Date())
							+ "】给用户：【" + info.getId() + "】,添加积分:【" + request.getParameter("point1" + i + "】"));
					int pointValue = new Integer((request.getParameter("point1" + i + "")));
					String reason = request.getParameter("reason" + i + "");
					String tag = request.getParameter("tag" + i);
					pointService.addPointInfo(info.getId(), pointValue, reason, tag, null, user.getId(), cur, null, statflag);
					// 送积分, 发送站内信
					String body = "恭喜您，管理员为您的账户添加了" + request.getParameter("point1" + i) + "个积分，原因：【"
							+ request.getParameter("reason" + i + "").toString()
							+ "】 已为您添加到账户&nbsp;&nbsp;<a href='http://www.gewara.com/home/acct/pointList.xhtml'>点此查看</a>";
					if (pointValue < 0) {
						body = body.substring(4);
					}
					userMessageService.sendSiteMSG(info.getId(), SysAction.ACTION_GETPOINT, user.getId(), body);
				} else {
					errMsg += "用户[" + strMemberid + "]不存在;";
				}

			}
		}
		return showJsonSuccess(model, errMsg);
	}

	@RequestMapping("/admin/point/ajax/addExPointInfo.xhtml")
	public String addExPointInfo(String uniquetag, String exmemberid, Integer expoint, String exreason, String extag, Long relatedid,
			String statflag, ModelMap model) {
		GewaraUser user = getLogonUser();
		List<String> idList = Arrays.asList(StringUtils.split(exmemberid, "[ ,，]+"));
		if (idList.size() > 500)
			return showJsonError(model, "一次不能添加超过500条记录!");
		List<String> idList2 = new ArrayList<String>(new HashSet(idList));
		List<String> diffList = (List<String>) CollectionUtils.disjunction(idList, idList2);
		String diffstr = VmUtils.printList(diffList);
		if (diffList != null && diffList.size() > 0)
			return showJsonError(model, "用户ID有重复, 核实后再添加! 重复ID为: " + diffstr);

		List<String> errors = new ArrayList<String>();
		int succount = 0;
		for (String memberid : idList) {
			String combUniquetag = uniquetag + memberid;
			int count = pointService.countUniquetagByCombine(combUniquetag);
			if (count > 0) {
				errors.add(memberid + ":已增加过！");
			} else {
				boolean success = true;
				try {
					//TODO:用户ID不存在
					pointService.addPointInfo(new Long(memberid), expoint, exreason, extag, relatedid, user.getId(), DateUtil.getCurFullTimestamp(), combUniquetag, statflag);
					succount++;
				} catch (Exception e) {
					success = false;
					errors.add(memberid + "错误：" + e.getMessage());
					dbLogger.warn("", e);
				}
				if (success) {
					dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "管理员【" + user.getUsername() + "】在【" + DateUtil.formatDate(new Date())
							+ "】给用户：【" + memberid + "】,添加积分:【" + expoint + "】");
					// 送积分, 发送站内信
					String body = "恭喜您，管理员为您的账户添加了" + expoint + "个积分，原因：【" + exreason
							+ "】 已为您添加到账户&nbsp;&nbsp;<a href='http://www.gewara.com/home/acct/pointList.xhtml'>点此查看</a>";
					if (expoint < 0) {
						body = body.substring(4);
					}
					userMessageService.sendSiteMSG(Long.valueOf(memberid), SysAction.ACTION_GETPOINT, user.getId(), body);
				}
			}
		}
		if (errors.size() > 0) {
			String alertMsg = "以下用户积分充值失败: " + errors.size() + " 【" + errors.toString() + "】 ";
			dbLogger.warn(alertMsg);
			return showJsonError(model, alertMsg);
		}
		return showJsonSuccess(model, "" + succount);
	}

	@RequestMapping("/admin/point/recharge.xhtml")
	public String recharge(ModelMap model) {
		Map param = new HashMap();
		param.put(MongoData.ACTION_TYPE, "recharge");
		param.put(MongoData.ACTION_TAG, "recharge");
		Map map = mongoService.findOne(MongoData.NS_INTEGRAL, param);
		model.put("map", map);
		return "admin/point/recharge.vm";
	}

	@RequestMapping("/admin/point/saveRecharge.xhtml")
	public String saveRecharge(String isSend, Timestamp starttime, Timestamp endtime, Double multiple, String rechargeType, String content,
			ModelMap model) {
		Map param = new HashMap();
		param.put(MongoData.ACTION_TYPE, "recharge");
		param.put(MongoData.ACTION_TAG, "recharge");
		Map map = mongoService.findOne(MongoData.NS_INTEGRAL, param);
		if (map == null) {
			map = new HashMap();
			map.put(MongoData.SYSTEM_ID, ObjectId.uuid());
			map.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestampStr());
			map.put("starttime", DateUtil.formatTimestamp(starttime));
			map.put("endtime", DateUtil.formatTimestamp(endtime));
			map.put("isSend", isSend);
			map.put("multiple", multiple);
			map.put("rechargeType", rechargeType);
			map.put(MongoData.ACTION_CONTENT, content);
			map.put(MongoData.ACTION_TAG, "recharge");
			map.put(MongoData.ACTION_TYPE, "recharge");
			mongoService.addMap(map, MongoData.SYSTEM_ID, MongoData.NS_INTEGRAL);
		} else {
			map.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestampStr());
			map.put("starttime", DateUtil.formatTimestamp(starttime));
			map.put("endtime", DateUtil.formatTimestamp(endtime));
			map.put("isSend", isSend);
			map.put("multiple", multiple);
			map.put("rechargeType", rechargeType);
			map.put(MongoData.ACTION_CONTENT, content);
			mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_INTEGRAL);
		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/searchPointList.xhtml")
	public String searchPointList(Timestamp starttime, Timestamp endtime, String applycity, String applydept, String applytype,HttpServletRequest request, ModelMap model) {
		String url = "admin/point/pointCount.vm";
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		model.put("deptMap", ManageConstant.deptMap);
		model.put("applyMap", ManageConstant.applyMap);
		model.put("pointTagMap", PointConstant.pointTagMap);
		model.put("applycity", applycity);
		model.put("applydept", applydept);
		model.put("applytype", applytype);
		model.put("starttime", starttime);
		model.put("endtime", endtime);
		String provincename = request.getParameter("province");
		if (StringUtils.isNotBlank(provincename)) {
			model.put("provincename", provincename);
		}
		List<AdminCityHelper> province2CityList = AdminCityHelper.province2CityListMap();
		model.put("province2CityList", province2CityList);
		if (starttime == null || endtime == null)
			return url;
		String sql = "select applycity as 申请区域, applydept as 申请部门, applytype as 申请类型, tag as 原因, sum(addpoint) as 增加积分, sum(reducepoint) as 减少积分 "
				+ "from WEBDATA.point_stats where adddate>=? and adddate<=? ";
		if (StringUtils.isNotBlank(applycity))
			sql = sql + " and applycity='" + applycity + "' ";
		if (StringUtils.isNotBlank(applydept))
			sql = sql + " and applydept='" + applydept + "' ";
		if (StringUtils.isNotBlank(applytype))
			sql = sql + " and applytype='" + applytype + "' ";
		sql = sql + "group by applycity, applydept, applytype, tag";
		List<Map<String, Object>> pointMapList = jdbcTemplate.queryForList(sql, DateUtil.format(starttime, "yyyy-MM-dd"),
				DateUtil.format(endtime, "yyyy-MM-dd"));
		model.put("pointMapList", pointMapList);
		return "admin/point/pointCount.vm";
	}
}
