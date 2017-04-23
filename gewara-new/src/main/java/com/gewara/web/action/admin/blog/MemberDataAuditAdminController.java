package com.gewara.web.action.admin.blog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.PointConstant;
import com.gewara.model.acl.User;
import com.gewara.model.bbs.Correction;
import com.gewara.model.common.County;
import com.gewara.model.common.Place;
import com.gewara.model.common.Subwayline;
import com.gewara.model.common.Subwaystation;
import com.gewara.model.movie.TempMovie;
import com.gewara.model.user.Member;
import com.gewara.service.member.PointService;
import com.gewara.support.ServiceHelper;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class MemberDataAuditAdminController extends BaseAdminController {
	
	@Autowired@Qualifier("pointService")
	private PointService pointService;
	
	@RequestMapping("/admin/audit/tempMovieList.xhtml")
	public String tempMovieList(ModelMap model, String tag, String sdatefrom, String sdateto,
			@RequestParam(required = false, value = "pageNo") Integer pageNo) {
		if (tag == null)
			tag = "untreated";
		Timestamp starttime = DateUtil.parseTimestamp(sdatefrom);
		Timestamp endtime = DateUtil.parseTimestamp(sdateto);
		if (endtime == null && starttime != null) {
			endtime = DateUtil.getCurFullTimestamp();
			sdateto = DateUtil.format(endtime, "yyyy-MM-dd HH:mm:ss");
		}
		if (starttime == null && endtime != null) {
			starttime = DateUtil.getMonthFirstDay(endtime);
			sdatefrom = DateUtil.format(starttime, "yyyy-MM-dd HH:mm:ss");
		}
		Integer rowsCount = commonService.getTempMovieCount(tag, TempMovie.STATUS_PASSED, starttime, endtime);
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 10;
		int first = rowsPerPage * pageNo;
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, pageNo, "admin/audit/tempMovieList.xhtml");
		Map params = new HashMap();
		params.put("tag", tag);
		params.put("sdatefrom", sdatefrom);
		params.put("sdateto", sdateto);
		pageUtil.initPageInfo(params);
		List<TempMovie> tempMovieList = commonService.getTempMovieList(tag, TempMovie.STATUS_PASSED, starttime, endtime, null, null, first,
				rowsPerPage);
		Map<Long, Member> lmMap = new HashMap<Long, Member>();
		for (TempMovie tm : tempMovieList) {
			Member m = daoService.getObject(Member.class, tm.getMemberid());
			lmMap.put(tm.getId(), m);
		}
		model.put("pageUtil", pageUtil);
		model.put("lmMap", lmMap);
		model.put("tag", tag);
		model.put("sdatefrom", sdatefrom);
		model.put("sdateto", sdateto);
		model.put("tempMovieList", tempMovieList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(tempMovieList));
		return "admin/audit/tempMovieList.vm";
	}
	@RequestMapping("/admin/audit/placeList.xhtml")
	public String placeList(ModelMap model, String status, String sdatefrom, String sdateto,
			@RequestParam(required = false, value = "pageNo") Integer pageNo) {
		if (status == null)
			status = "untreated";
		Timestamp starttime = DateUtil.parseTimestamp(sdatefrom);
		Timestamp endtime = DateUtil.parseTimestamp(sdateto);
		if (endtime == null && starttime != null) {
			endtime = DateUtil.getCurFullTimestamp();
			sdateto = DateUtil.format(endtime, "yyyy-MM-dd HH:mm:ss");
		}
		if (starttime == null && endtime != null) {
			starttime = DateUtil.getMonthFirstDay(endtime);
			sdatefrom = DateUtil.format(starttime, "yyyy-MM-dd HH:mm:ss");
		}
		Integer rowsCount = commonService.getPlaceCount(status, starttime, endtime);
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 10;
		int first = rowsPerPage * pageNo;
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, pageNo, "admin/audit/placeList.xhtml");
		Map params = new HashMap();
		params.put("status", status);
		params.put("sdatefrom", sdatefrom);
		params.put("sdateto", sdateto);
		pageUtil.initPageInfo(params);
		List<Place> placeList = commonService.getPlaceList(status, starttime, endtime, first, rowsPerPage);
		Map<Long, Member> lmMap = new HashMap<Long, Member>();
		for (Place place : placeList) {
			Member m = daoService.getObject(Member.class, place.getUserid());
			lmMap.put(place.getId(), m);
		}
		model.put("pageUtil", pageUtil);
		model.put("lmMap", lmMap);
		model.put("status", status);
		model.put("sdatefrom", sdatefrom);
		model.put("sdateto", sdateto);
		model.put("placeList", placeList);
		addCacheMember(model, BeanUtil.getBeanPropertyList(placeList, Long.class, "userid", true));
		return "admin/audit/placeList.vm";
	}

	@RequestMapping("/admin/audit/copyPlace.xhtml")
	public String copyPlace(ModelMap model, @RequestParam("tag") String tag, @RequestParam("id") Long id, HttpServletRequest request) {
		Place place = daoService.getObject(Place.class, id);
		String target = null;
		if ("cinema".equals(tag)) {
			model.put("cinema", place);
			target = "admin/cinema/cinemaForm.vm";
		} else if ("ktv".equals(tag)) {
			model.put("ktv", place);
			target = "admin/ktv/ktvDetailForm.vm";
		} else if ("bar".equals(tag)) {
			model.put("bar", place);
			target = "admin/bar/barDetailForm.vm";
		} else if ("sport".equals(tag)) {
			model.put("sport", place);
			target = "admin/sport/sportDetailForm.vm";
		}
		String citycode = getAdminCitycode(request);
		List<Subwayline> lineList = placeService.getSubwaylinesByCityCode(citycode);
		List<Subwaystation> stationList = new ArrayList<Subwaystation>();
		model.put("lineList", lineList);
		model.put("stationList", stationList);
		return target;
	}
	
	// ÍøÓÑÌí¼Ó³¡¹Ý
	@RequestMapping("/admin/audit/ajax/getPlaceById.xhtml")
	public String getPlaceById(Long placeId, ModelMap model) {
		Place place = daoService.getObject(Place.class, placeId);
		Map result = BeanUtil.getBeanMap(place);
		if (StringUtils.isNotBlank(place.getCountycode())) {
			County county = daoService.getObject(County.class, place.getCountycode());
			result.put("countyname", county.getCountyname());
		}
		return showJsonSuccess(model, result);
	}

	@RequestMapping("/admin/audit/ajax/removePlaceById.xhtml")
	public String removePlaceById(Long placeId, ModelMap model) {
		Place tm = daoService.getObject(Place.class, placeId);
		tm.setIsdel(Place.DELED);
		daoService.saveObject(tm);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/audit/ajax/passPlace.xhtml")
	public String passPlace(HttpServletRequest request, ModelMap model) throws Exception {
		Map<String, String[]> dataMap = request.getParameterMap();
		Set<String> keySet = dataMap.keySet();
		for (String string : keySet) {
			Long placeId = Long.parseLong(string);
			String value = ServiceHelper.get(dataMap, string);
			if (StringUtils.isNotBlank(value)) {
				String[] values = value.split(",");
				String point = "-1";
				if (ArrayUtils.isNotEmpty(values)) {
					Map<String, String> map = new HashMap<String, String>();
					point = String.valueOf(values[0]);
					if ("-1".equals(point))
						continue;
					map.put("point", point);
					if (values.length > 1) {
						String reason = String.valueOf(values[1]);
						map.put("reason", reason);
					}
					map.put("tag", "content");
					String otherinfo = JsonUtils.writeMapToJson(map);
					Place place = daoService.getObject(Place.class, placeId);
					place.setIspass(Place.PASSED);
					place.setOtherinfo(otherinfo);
					daoService.saveObject(place);
					User user = getLogonUser();
					pointService.addPointInfo(place.getUserid(), Integer.parseInt(point), place.getName(), PointConstant.TAG_ADD_INFO, null,	user.getId());
				}
			}
		}

		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/audit/correctionList.xhtml")
	public String correctionList(ModelMap model, String status, String sdatefrom, String sdateto, Integer pageNo) {
		if (status == null)
			status = "untreated";
		Timestamp starttime = DateUtil.parseTimestamp(sdatefrom);
		Timestamp endtime = DateUtil.parseTimestamp(sdateto);
		if (endtime == null && starttime != null) {
			endtime = DateUtil.getCurFullTimestamp();
			sdateto = DateUtil.format(endtime, "yyyy-MM-dd HH:mm:ss");
		}
		if (starttime == null && endtime != null) {
			starttime = DateUtil.getMonthFirstDay(endtime);
			sdatefrom = DateUtil.format(starttime, "yyyy-MM-dd HH:mm:ss");
		}
		Integer rowsCount = commonService.getCorrectionCount(status, starttime, endtime);
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 10;
		int first = rowsPerPage * pageNo;
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, pageNo, "admin/audit/correctionList.xhtml");
		Map params = new HashMap();
		params.put("status", status);
		params.put("sdatefrom", sdatefrom);
		params.put("sdateto", sdateto);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		List<Correction> corrList = commonService.getCorrectionList(status, starttime, endtime, first, rowsPerPage);
		Map<Long, Member> lmMap = new HashMap<Long, Member>();
		for (Correction corr : corrList) {
			Member m = daoService.getObject(Member.class, corr.getMemberid());
			lmMap.put(corr.getId(), m);
		}
		model.put("corrList", corrList);
		model.put("lmMap", lmMap);
		model.put("sdatefrom", sdatefrom);
		model.put("sdateto", sdateto);
		model.put("status", status);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(corrList));
		return "admin/audit/correctionList.vm";
	}
	
	@RequestMapping("/admin/audit/ajax/checkCorrection.xhtml")
	public String checkCorrection(HttpServletRequest request, ModelMap model) throws Exception {
		Map<String, String[]> dataMap = request.getParameterMap();
		Set<String> keySet = dataMap.keySet();
		for (String string : keySet) {
			Long corrid = Long.parseLong(string);
			String value = ServiceHelper.get(dataMap, string);
			if (StringUtils.isNotBlank(value)) {
				String[] values = value.split(",");
				if (ArrayUtils.isNotEmpty(values)) {
					String point = String.valueOf(values[0]);
					if ("-1".equals(point))
						continue;
					Map<String, String> map = new HashMap<String, String>();
					map.put("point", point);
					if (values.length > 1) {
						String reason = String.valueOf(values[1]);
						map.put("reason", reason);
					}
					map.put("tag", "content");
					String otherinfo = JsonUtils.writeMapToJson(map);
					Correction corre = daoService.getObject(Correction.class, corrid);
					corre.setCheck(Correction.CHECKED);
					corre.setOtherinfo(otherinfo);
					daoService.saveObject(corre);
					User user = getLogonUser();
					if (corre.getMemberid() != null) {
						pointService.addPointInfo(corre.getMemberid(), Integer.parseInt(point), corre.getReferer(), PointConstant.TAG_CORRECT, null, user.getId());
					}
				}
			}
		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/audit/ajax/removeCorrection.xhtml")
	public String removeCorrection(Long corrid, ModelMap model) throws Exception {
		Correction corre = daoService.getObject(Correction.class, corrid);
		daoService.removeObject(corre);
		return showJsonSuccess(model);
	}
}
