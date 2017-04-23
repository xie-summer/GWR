package com.gewara.web.action.inner.mobile.order;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.app.AppConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.api.FindSeatPt;
import com.gewara.helper.api.FindSeatUtilHelper;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.user.Member;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.inner.OpenApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileMovieController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
import com.gewara.xmlbind.bbs.Comment;

@Controller
public class OpenApiMobileOrderScheduleController extends BaseOpenApiMobileMovieController {
	@RequestMapping("/openapi/mobile/order/orderSchedule.xhtml")
	public String orderSchedule(String tradeNo, String memberEncode, ModelMap model, HttpServletRequest request) {
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		Member member = auth.getMember();
		Timestamp ptime = DateUtil.addHour(DateUtil.getCurFullTimestamp(), -6);
		List<TicketOrder> orderList = new ArrayList<TicketOrder>();
		if (StringUtils.isNotBlank(tradeNo)) {
			TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo);
			if (order != null && order.isPaidSuccess() && order.getMemberid().equals(member.getId())) {
				orderList.add(order);
			}
		} else {
			DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class);
			query.add(Restrictions.eq("memberid", member.getId()));
			query.add(Restrictions.ge("playtime", ptime));
			query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
			query.addOrder(Order.asc("playtime"));
			orderList = hibernateTemplate.findByCriteria(query, 0, 1);
		}
		orderSchedule(memberEncode, false, orderList, model, request);
		model.put("share", true);
		return getXmlView(model, "inner/mobile/movie/orderSchedule.vm");
	}

	@RequestMapping("/openapi/mobile/order/orderScheduleShare.xhtml")
	public String orderScheduleShare(String tradeNo, String findCheck, ModelMap model, HttpServletRequest request) {
		List<TicketOrder> orderList = new ArrayList<TicketOrder>();
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo);
		String str = StringUtil.md5(order.getTradeNo() + order.getMemberid() + order.getMovieid());
		if (!StringUtils.equals(findCheck, str)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "校验错误！");
		}
		if (order != null && order.isPaidSuccess()) {
			orderList.add(order);
		}
		orderSchedule(null, true, orderList, model, request);
		return getXmlView(model, "inner/mobile/movie/orderSchedule.vm");
	}

	private String getTransdes(Cinema cinema, Map<String, String> otherMap) {
		String transdes = "";
		String transtool = otherMap.get("transtool");
		if (StringUtils.equals(transtool, "drive")) {
			Map<String, String> otherCinemaMap = VmUtils.readJsonToMap(cinema.getOtherinfo());
			transdes = otherCinemaMap.get("park");
		} else if (StringUtils.equals(transtool, "subway")) {
			if (StringUtils.isNotBlank(cinema.getStationname())) {
				Map<String, List<Map<String, String>>> subwayTransportMap = JsonUtils.readJsonToMap(cinema.getSubwayTransport());
				List<String> subwayList = new ArrayList<String>();
				transdes = StringUtils.join(subwayList, ",");
				for (String key : subwayTransportMap.keySet()) {
					List<Map<String, String>> list = subwayTransportMap.get(key);
					for (Map<String, String> map : list) {
						String lines = map.get("lines");
						String exitnumber = map.get("exitnumber");
						if (StringUtils.isNotBlank(lines)) {
							if (lines.indexOf("号线") == -1) {
								lines = lines + "号线";
							}
						}
						if (StringUtils.isNotBlank(exitnumber)) {
							if (exitnumber.indexOf("出口") == -1) {
								exitnumber = exitnumber + "出口";
							}
						}
						subwayList.add(lines + cinema.getStationname() + exitnumber);
					}
				}
				transdes = StringUtils.join(subwayList, ",");
			}
		} else if (StringUtils.equals(transtool, "transit")) {
			List<String> transList = Arrays.asList(StringUtils.split(cinema.getTransport(), "@"));
			transdes = StringUtils.join(transList, ",");
		}
		return transdes;
	}

	@RequestMapping("/openapi/mobile/order/setOrderSchedule.xhtml")
	public String schedulePlan(String tradeNo, Integer beforemin, String tanstool, ModelMap model) {
		if (beforemin == null) {
			beforemin = 180;
		}
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo, false);
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		ErrorCode vcode = validGewaOrder(order, member);
		if (!vcode.isSuccess()) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, vcode.getMsg());
		}
		if (!AppConstant.transToolList.contains(tanstool)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "出行方式错误！");
		}
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid());
		Map<String, String> otherMap = JsonUtils.readJsonToMap(order.getOtherinfo());
		otherMap.put("leavetime", DateUtil.formatTimestamp(DateUtil.addMinute(opi.getPlaytime(), -beforemin)));
		otherMap.put("transtool", tanstool);
		order.setOtherinfo(JsonUtils.writeMapToJson(otherMap));
		daoService.saveObject(order);
		return getSuccessXmlView(model);
	}

	private void orderSchedule(String memberEncode, boolean isshare, List<TicketOrder> orderList, ModelMap model, HttpServletRequest request) {
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		List<TicketOrder> neworderList = new ArrayList<TicketOrder>();
		if (orderList.size() > 1) {
			List<TicketOrder> curOrderList = new ArrayList<TicketOrder>();
			List<TicketOrder> afterOrderList = new ArrayList<TicketOrder>();
			for (TicketOrder order : orderList) {
				if (curtime.after(order.getPlaytime())) {
					afterOrderList.add(order);
				} else {
					curOrderList.add(order);
				}
			}
			Collections.sort(curOrderList, new PropertyComparator("playtime", false, false));
			Collections.sort(afterOrderList, new PropertyComparator("playtime", false, true));
			neworderList.addAll(curOrderList);
			neworderList.addAll(afterOrderList);
		} else {
			neworderList.addAll(orderList);
		}
		Map<String, TicketOrder> orderMap = new HashMap<String, TicketOrder>();
		Map<String, List<Map<String, Object>>> commentMap = new HashMap<String, List<Map<String, Object>>>();
		Map<Long, Movie> movieMap = new HashMap<Long, Movie>();
		Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
		Map<Long, Cinema> cinemaMap = new HashMap<Long, Cinema>();
		Map<String, String> transdesMap = new HashMap<String, String>();
		Map<String, Timestamp> leavetimeMap = new HashMap<String, Timestamp>();
		Map<String, Long> diaryidMap = new HashMap<String, Long>();
		Map<String, String> transtoolMap = new HashMap<String, String>();
		Map<String, Timestamp> endtimeMap = new HashMap<String, Timestamp>();
		Map<String, Boolean> showlinkMap = new HashMap<String, Boolean>();
		Map<String, String> findCheckMap = new HashMap<String, String>();
		Map<String, CinemaProfile> profileMap = new HashMap<String, CinemaProfile>();
		Map<String, String> checkpassMap = new HashMap<String, String>();
		for (TicketOrder order : neworderList) {
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid());
			String msgTemplate = messageService.getCheckpassTemplate(opi);
			String checkpass = msgTemplate.indexOf("hfhpass") >= 0 ? order.getHfhpass(): order.getCheckpass();
			checkpassMap.put(order.getTradeNo(), checkpass);
			Cinema cinema = daoService.getObject(Cinema.class, opi.getCinemaid());
			CinemaRoom room = daoService.getObject(CinemaRoom.class, opi.getRoomid());
			boolean showlink = false;
			if (StringUtils.isNotBlank(room.getOtherinfo())) {
				if (JsonUtils.readJsonToMap(room.getOtherinfo()).get("outerRingseat") != null
						&& StringUtils.equals("true", (String) JsonUtils.readJsonToMap(room.getOtherinfo()).get("outerRingseat"))) {
					showlink = true;
				}
			}
			CinemaProfile profile = daoService.getObject(CinemaProfile.class, opi.getCinemaid());
			Movie movie = daoService.getObject(Movie.class, opi.getMovieid());
			Timestamp leavetime = null;
			Timestamp playtime = order.getPlaytime();
			Map<String, String> otherMap = VmUtils.readJsonToMap(order.getOtherinfo());
			if (otherMap.containsKey("leavetime")) {
				leavetime = DateUtil.parseTimestamp(otherMap.get("leavetime"));
				leavetimeMap.put(order.getTradeNo(), leavetime);
				transtoolMap.put(order.getTradeNo(), otherMap.get("transtool"));
			} else {
				if (otherMap.containsKey("openleavetime")) {
					leavetimeMap.put(order.getTradeNo(), DateUtil.parseTimestamp(otherMap.get("openleavetime")));
				} else {
					String letime = DateUtil.format(DateUtil.getCurFullTimestamp(), "yyyy-MM-dd HH:mm:00");
					otherMap.put("openleavetime", letime);
					order.setOtherinfo(JsonUtils.writeMapToJson(otherMap));
					daoService.saveObject(order);
					Timestamp openleavetime = DateUtil.parseTimestamp(letime);
					leavetimeMap.put(order.getTradeNo(), openleavetime);
				}
			}
			String transdes = getTransdes(cinema, otherMap);
			int videolen = 90;
			if (movie.getVideolen() != null) {
				videolen = movie.getVideolen();
			}
			playtime = DateUtil.addMinute(playtime, videolen);
			Timestamp endtime = DateUtil.addMinute(playtime, 15);
			endtimeMap.put(order.getTradeNo(), endtime);

			List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
			List<Comment> commentList = commentService.getHotCommentListByRelatedId(TagConstant.TAG_MOVIE, null, opi.getMovieid(), null, null, 0, 1);
			resMapList = getCommentResMapList(commentList);
			initField(model, request);

			commentMap.put(order.getTradeNo(), resMapList);
			movieMap.put(order.getMovieid(), movie);
			cinemaMap.put(order.getCinemaid(), cinema);
			opiMap.put(order.getMpid(), opi);
			transdesMap.put(order.getTradeNo(), transdes);
			diaryidMap.put(order.getTradeNo(), profile.getTopicid());
			profileMap.put(order.getTradeNo(), profile);
			showlinkMap.put(order.getTradeNo(), showlink);
			findCheckMap.put(order.getTradeNo(), StringUtil.md5(order.getTradeNo() + order.getMemberid() + order.getMovieid()));
		}
		model.put("transdesMap", transdesMap);
		model.put("checkpassMap", checkpassMap);
		model.put("showlinkMap", showlinkMap);
		model.put("transtoolMap", transtoolMap);
		model.put("leavetimeMap", leavetimeMap);
		model.put("opiMap", opiMap);
		model.put("cinemaMap", cinemaMap);
		model.put("movieMap", movieMap);
		model.put("orderMap", orderMap);
		model.put("commentMap", commentMap);
		model.put("diaryidMap", diaryidMap);
		model.put("endtimeMap", endtimeMap);
		model.put("orderList", neworderList);
		model.put("memberEncode", memberEncode);
		model.put("findCheckMap", findCheckMap);
		model.put("isshare", isshare);
		model.put("profileMap", profileMap);
	}

	@RequestMapping("/openapi/mobile/order/findSeat.xhtml")
	public String findSeat(String tradeNo, ModelMap model) {
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo);
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if (!order.getMemberid().equals(member.getId())) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能查看他人信息！");
		}
		return findSeat(order, model);
	}

	@RequestMapping("/openapi/mobile/order/findSeatShare.xhtml")
	public String findSeat(String tradeNo, String findCheck, ModelMap model) {
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeNo);
		String str = StringUtil.md5(order.getTradeNo() + order.getMemberid() + order.getMovieid());
		if (!StringUtils.equals(findCheck, str)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "校验错误！");
		}
		return findSeat(order, model);
	}

	private String findSeat(TicketOrder order, ModelMap model) {
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid());
		CinemaRoom room = daoService.getObject(CinemaRoom.class, opi.getRoomid());
		if (room == null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有查找到厅数据！");
		}
		Map<String, String> outerRingseatMap = nosqlService.getOuterRingSeatByRoomId(room.getId());
		if (outerRingseatMap.isEmpty()) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有查找到相关数据！");
		}
		Map<String, List<String>> outMap = getOutMap(outerRingseatMap);
		int[] sxy = getStartXY(room, outMap);
		if (sxy == null) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有查找到相关数据！");
		}
		List<OpenSeat> openSeatList = openPlayService.getOpenSeatList(opi.getMpid());
		Map<String, OpenSeat> seatMap = BeanUtil.beanListToMap(openSeatList, "position");
		Map<Integer, String> rowMap = new HashMap<Integer, String>();
		OpenSeat oseat = null;
		Map<Integer, String> pointMap = new HashMap<Integer, String>();
		Map<String, String> descMap = JsonUtils.readJsonToMap(order.getDescription2());
		String seats = descMap.get("影票");
		seats = seats.replaceAll("\\d+元", "");
		List<String> seatList = Arrays.asList(seats.split(","));
		int linenum = room.getLinenum();
		int ranknum = room.getRanknum();
		int L2 = linenum + 1;
		int R2 = ranknum + 1;
		Map<String, SeatPoint> seatPointMap = new HashMap<String, SeatPoint>();
		List<SeatPoint> spList = new ArrayList<SeatPoint>();
		int startX = sxy[0];
		int startY = sxy[1];
		List<int[]> outxyList = getOutXY(room, outMap);
		List<int[]> guodaoxyList = getGuoDaoXY(room, outMap);
		List<int[]> myPointList = new ArrayList<int[]>(); // 我的座位座位
		//记录当前用户已经选择的座位，座位的下一行的如果为走道，则下一行封死（如果封死这一行的对应的上一列为走道，则开启）
		Map<Integer, List<SeatPoint>> myColumnsMap = new HashMap<Integer, List<SeatPoint>>();
		Map<Integer, List<SeatPoint>> columnsMap = new HashMap<Integer, List<SeatPoint>>();
		Map<String, SeatPoint> ukeyMap = new HashMap<String, SeatPoint>();
		for (int i = 0; i <= L2; i++) {
			List<SeatPoint> pointList = new ArrayList<SeatPoint>();
			boolean isfind = false;
			for (int j = 0; j <= R2; j++) {
				oseat = seatMap.get(i + ":" + j);
				String s = ""; // 标识座位类型
				String rowid = "";
				int pointx = j;
				int pointy = -i + linenum + 1;
				String ukey = pointx + ":" + pointy;
				if (oseat == null) { // 走廊
					if (i == 0 || j == 0 || i > linenum || j > ranknum) {
						s = "out";
					} else {
						s = "z";
					}
					if (pointx == startX && pointy == startY) {
						s = "inDoor";
					}
					if (StringUtils.equals(s, "out")) {
						if (outxyList != null) {
							for (int[] oxy : outxyList) {
								int outDoorX = oxy[0];
								int outDoorY = oxy[1];
								if (pointx == outDoorX && pointy == outDoorY) {
									s = "outDoor";
								}
							}
						}
					}
					if (s.indexOf("Door") == -1) {
						for (int[] hall : guodaoxyList) {
							int hallX = hall[0];
							int hallY = hall[1];
							if (pointx == hallX && pointy == hallY) {
								s = "guodao";
							}
						}
					}
				} else {
					String seatlabel = oseat.getSeatLabel();
					rowid = oseat.getSeatline();
					rowMap.put(i, rowid);
					if (seatList.contains(seatlabel)) { // 我自己的座位
						s = "ms";
						isfind = true;
						myPointList.add(new int[] { pointx, pointy });
					} else {
						s = "s"; // 普通座位
					}
				}
				SeatPoint point = new SeatPoint(pointx, pointy, s);
				seatPointMap.put(ukey, point);
				pointList.add(point);
			}
			List<String> strList = new ArrayList<String>();
			for (SeatPoint sp : pointList) {
				if (isfind) {
					if (sp.getType().equals("s")) {
						sp.setType("ls");
					}
				}
				strList.add(sp.getPointData2());
				ukeyMap.put(sp.getPointx()+":" + sp.getPointy(), sp);
			}
			if(isfind){
				myColumnsMap.put(i, pointList);
			}
			columnsMap.put(i, pointList);
			spList.addAll(pointList);
			pointMap.put(i, StringUtils.join(strList, ","));
		}
		for (Integer num : myColumnsMap.keySet()) {
			List<String> strTmpList = new ArrayList<String>();
			List<SeatPoint> tmpList = myColumnsMap.get(num);
			for(SeatPoint tmp : tmpList){
				SeatPoint sp = ukeyMap.get(tmp.getPointx()+":"+(tmp.getPointy()-1));
				if(!tmp.getType().equals("guodao") && sp.getType().equals("guodao")){
					sp.setType("z");
				}
				strTmpList.add(sp.getPointData2());
			}
			pointMap.put(num+1, StringUtils.join(strTmpList, ","));
		}
		int[][] map = new int[ranknum + 2][linenum + 2];// 初始化数组的大小
		initMap(spList, map);
		List<String> pathList = new ArrayList<String>();
		int endX = -1, endY = -1;
		for (int[] myPoint : myPointList) { // 多个入口选择一个入口
			FindSeatUtilHelper findHelper = new FindSeatUtilHelper(map, startX, startY, myPoint[0], myPoint[1]);
			FindSeatPt[] pts = findHelper.getResult();
			if (pts != null) {
				for (FindSeatPt pt : pts) {
					pathList.add(pt.getX() + ":" + pt.getY());
				}
				endX = myPoint[0];
				endY = myPoint[1];
				break;
			}
		}
		if (endX == -1 || endY == -1) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有查找到相关线路！");
		}
		Collections.reverse(pathList);
		model.put("room", room);
		model.put("rowMap", rowMap);
		model.put("pointMap", pointMap);
		model.put("startXY", sxy[0] + ":" + sxy[1]);
		model.put("endXY", endX + ":" + endY);
		model.put("pathList", StringUtils.join(pathList, ","));
		model.put("l2", L2);
		model.put("r2", R2);
		model.put("opi", opi);
		model.put("direction", FindSeatUtilHelper.getPathDirection(startX, startY, pathList));
		return getXmlView(model, "inner/mobile/movie/findSeat.vm");
	}

	private Map<String, List<String>> getOutMap(Map<String, String> outMap) {
		if (outMap == null) {
			return null;
		}
		Map<String, List<String>> resMap = new HashMap<String, List<String>>();
		for (String key : outMap.keySet()) {
			String value = outMap.get(key);
			List<String> tmpList = new ArrayList<String>();
			if (resMap.containsKey(value)) {
				tmpList = resMap.get(value);
			}
			tmpList.add(key);
			resMap.put(value, tmpList);
		}
		return resMap;
	}

	private int[] getStartXY(CinemaRoom room, Map<String, List<String>> outMap) {
		List<String> strList = outMap.get("startPoint");
		if(strList!=null && strList.size()>0){
			String str = strList.get(0);
			String s[] = str.split("L|R|=");
			int startX = Integer.valueOf(s[2]);
			int startY = room.getLinenum() + 1 - Integer.valueOf(s[1]);
			return new int[] { startX, startY };
		}
		return null;
	}

	private List<int[]> getOutXY(CinemaRoom room, Map<String, List<String>> outMap) {
		if (outMap == null) {
			return null;
		}
		List<int[]> resList = new ArrayList<int[]>();
		List<String> strList = outMap.get("outDoor");
		if (strList != null) {
			for (String str : strList) {
				int startX = 0, startY = 0;
				String s[] = str.split("L|R|=");
				startX = Integer.valueOf(s[2]);
				startY = room.getLinenum() + 1 - Integer.valueOf(s[1]);
				resList.add(new int[] { startX, startY });
			}
		}
		return resList;
	}

	public List<int[]> getGuoDaoXY(CinemaRoom room, Map<String, List<String>> outMap) {
		List<String> strList = new ArrayList<String>();
		List<String> hAisletList = outMap.get("hAislet");
		List<String> hAisleList = outMap.get("hAisle");
		List<String> vAisletList = outMap.get("vAislet");
		List<String> vAisleList = outMap.get("vAisle");
		
		if (hAisletList != null) {
			strList.addAll(hAisletList);
		}
		if (hAisleList != null) {
			strList.addAll(hAisleList);
		}
		if (vAisletList != null) {
			strList.addAll(vAisletList);
		}
		if (vAisleList != null) {
			strList.addAll(vAisleList);
		}
		List<int[]> resList = new ArrayList<int[]>();
		for (String str : strList) {
			String s[] = str.split("L|R|=");
			int startX = Integer.valueOf(s[2]);
			int startY = room.getLinenum() + 1 - Integer.valueOf(s[1]);
			resList.add(new int[] { startX, startY });
		}
		return resList;
	}
	private void initMap(List<SeatPoint> spList, int[][] map) {
		List<String> canList = new ArrayList(Arrays.asList("guodao", "ls", "ms", "inDoor"));
		for (SeatPoint sp : spList) {
			int z = 1;
			String type = sp.getType();
			if (canList.contains(type)) { // 可走的点
				z = 0;
			}
			map[sp.getPointx()][sp.getPointy()] = z;
		}
	}

	class SeatPoint {
		private Integer pointx; //
		private Integer pointy;
		private String type; // 普通座位，订单的座位，走廊，同一行

		public SeatPoint(Integer pointx, Integer pointy, String type) {
			this.pointx = pointx;
			this.pointy = pointy;
			this.type = type;
		}

		public Integer getPointx() {
			return pointx;
		}

		public void setPointx(Integer pointx) {
			this.pointx = pointx;
		}

		public Integer getPointy() {
			return pointy;
		}

		public void setPointy(Integer pointy) {
			this.pointy = pointy;
		}

		public String getPointData() {
			return pointx + "@" + pointy + "@" + type;
		}

		public String getPointData2() {
			String tmp = type;
			if (StringUtils.equals(tmp, "guodao")) {
				tmp = "z";
			}
			return pointx + "@" + pointy + "@" + tmp;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}
}
