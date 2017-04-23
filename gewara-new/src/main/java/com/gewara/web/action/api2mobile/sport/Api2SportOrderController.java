package com.gewara.web.action.api2mobile.sport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.SearchOrderCommand;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.OpenTimeItemConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.constant.TagConstant;
import com.gewara.helper.SportOrderHelper;
import com.gewara.helper.TimeItemHelper;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportField;
import com.gewara.model.user.Member;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.sport.SportUntransService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.web.filter.NewApiAuthenticationFilter;
@Controller
public class Api2SportOrderController extends BaseApiController{
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	@Autowired@Qualifier("sportOrderService")
	private SportOrderService sportOrderService;
	@Autowired@Qualifier("sportUntransService")
	private SportUntransService sportUntransService;
	@RequestMapping("/api2/sport/getOttList.xhtml")
	public String getOttList(Long sportid, Long itemid, Integer maxnum, ModelMap model){
		if(maxnum==null) maxnum = 7;
		Map<Long, Map<String, Integer>> ottPriceMap = new HashMap<Long, Map<String, Integer>>();
		List<OpenTimeTable> ottList = openTimeTableService.getOpenTimeTableList(sportid, itemid, DateUtil.currentTime(), null, null, false, 0, maxnum);
		Map<Long, Integer> remainMap = new HashMap<Long, Integer>();
		boolean isFiled = false;
		for(OpenTimeTable ott : ottList){
			Map<String, Integer> ottPrice = sportService.getSportPriceByOtt(sportid, itemid, ott.getId());
			ottPriceMap.put(ott.getId(), ottPrice);
			isFiled = ott.hasField();
			if(isFiled && ott.getRemain()<=3){
				int remain = 0;
				boolean validOver = false;
				if(ott.getPlaydate().compareTo(DateUtil.getBeginningTimeOfDay(new Date()))==0) validOver = true;
				Sport2Item sport2Item = null;
				if(validOver){
					sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
				}
				List<OpenTimeItem> otiList = openTimeTableService.getOpenItemList(ott.getId());
				List<SportField> fieldList = sportOrderService.getSportFieldList(ott.getId());
				List<Long> filedidList = BeanUtil.getBeanPropertyList(fieldList, Long.class, "id", true);
				for(OpenTimeItem oti : otiList){
					boolean isn = true;
					if(validOver && oti.hasOver(sport2Item.getLimitminutes())){
						isn = false;
					}
					if(isn)if(oti.hasAvailable() && oti.getFieldid()!=null && filedidList.contains(oti.getFieldid())) remain++;
				}
				ott.setRemain(remain);
				daoService.saveObject(ott);
			}
			if(isFiled){
				remainMap.put(ott.getId(), ott.getRemain());
			}else {
				remainMap.put(ott.getId(), 0);
			}
		}
		model.put("ottList", ottList);
		model.put("remainMap", remainMap);
		model.put("ottPriceMap", ottPriceMap);
		return getXmlView(model, "api2/sport/ottList.vm");
	}
	/**
	 * 运动预订排期表
	 * @param key 
	 * @param encryptCode
	 * @param model
	 * @param playdate 预订日期
	 * @param sportid 运动场馆id
	 * @param itemid 运动项目id
	 * @param citycode 城市id
	 * @return
	 */
	@RequestMapping("/api2/sport/getOtiList.xhtml")
	public String getOtiList(String playdate, Long sportid, Long itemid, String citycode, ModelMap model){
		List<OpenTimeTable> ottList = openTimeTableService.getOttList(sportid, itemid, playdate);
		Map<Long, List<OpenTimeItem>> otisMap = new HashMap<Long, List<OpenTimeItem>>();
		for(OpenTimeTable otts : ottList){
			List<OpenTimeItem> otis = openTimeTableService.getOpenItemList(otts.getId());
			otisMap.put(otts.getId(), otis);
		}
		model.put("ottList", ottList);
		model.put("otisMap", otisMap);
		return getXmlView(model, "api/sport/otiList.vm");
	}
	/**
	 * 获取场馆场次信息
	 * @param opendate
	 * @param itemid
	 * @param sportid
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/sport/ottInfo.xhtml")
	public String ottInfo(Date opendate, Long itemid, Long sportid, ModelMap model, HttpServletRequest request){
		if(sportid==null || itemid==null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "itemid和sportid不能为空");
		List<OpenTimeTable> openTimeTableList = openTimeTableService.getOpenTimeTableList(sportid, itemid, opendate, null);
		if(openTimeTableList.isEmpty()) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "该场馆在"+DateUtil.formatDate(opendate)+"暂无开放场次信息！");
		OpenTimeTable ott = openTimeTableList.get(0);
		List<SportField> fieldList = sportOrderService.getSportFieldList(ott.getId());
		List<OpenTimeItem> otiList2 = openTimeTableService.getOpenItemList(ott.getId());
		Collections.sort(otiList2, new PropertyComparator("hour", false, true));
		TimeItemHelper tih = new TimeItemHelper(otiList2);
		Map<String, OpenTimeItem> otiMap = tih.getOtiMap();
		List<String> hourList = tih.getPlayHourList();
		
		Map<Long,List<OpenTimeItem>> fieldOtiMap = new LinkedMap();
		boolean validOver = false;
		if(ott.getPlaydate().compareTo(DateUtil.getBeginningTimeOfDay(new Date()))==0) validOver = true;
		//Sport2Item sport2Item = sportService.getSport2Item(ott.getSportid(), ott.getItemid());
		List<OpenTimeItem> otiList = null;
		OpenTimeItem oti = null;
		List<SportField> sportfieldList = new ArrayList<SportField>();
		Set<String> removeHourList = new HashSet<String>();
		Map<Long, String> timesMap = new HashMap<Long, String>();
		Map<Long, String> minlenMap = new HashMap<Long, String>();
		boolean hasField = ott.hasField();
		Date playdate = ott.getPlaydate();
		for (SportField sf : fieldList) {
			otiList = new ArrayList<OpenTimeItem>();
			List<OpenTimeItem> remoteOtiList = new ArrayList<OpenTimeItem>();
			for (String hour : hourList) {
				/*if(validOver && isOver(ott, hour, sport2Item.getLimitminutes())){
					removeHourList.add(hour);
					continue;
				}*/
				oti = otiMap.get(sf.getId() + hour);
				if(oti != null && !oti.hasStatus(OpenTimeItemConstant.STATUS_DELETE)) {
					otiList.add(oti);
					if(!hasField){
						List<String> timeList = SportOrderHelper.getStarttimeList(playdate,oti);
						if(validOver && timeList.isEmpty()){
							remoteOtiList.add(oti);
						}
						if(oti.hasPeriod()){
							if(StringUtils.equals(oti.getUnitType(), OpenTimeTableConstant.UNIT_TYPE_TIME)){ //按分钟限时
								List<Integer> minlenList = SportOrderHelper.getPeriodList(playdate, oti);
								minlenMap.put(oti.getId(), StringUtils.join(minlenList, ","));
							}else {  //不限时
								timesMap.put(oti.getId(), StringUtils.join(timeList, ","));
							}
						}else if(oti.hasInning()){
							timesMap.put(oti.getId(), StringUtils.join(timeList, ","));
						}
					}
				}
			}
			otiList.removeAll(remoteOtiList);
			if(!otiList.isEmpty()){
				fieldOtiMap.put(sf.getId(), otiList);
				sportfieldList.add(sf);
			}
		}
		hourList.removeAll(removeHourList);
		model.put("ott", ott);
		model.put("hourList", StringUtils.join(hourList, ","));
		model.put("fieldOtiMap", fieldOtiMap);
		model.put("fieldList", sportfieldList);
		model.put("tih", tih);
		model.put("timesMap", timesMap);
		model.put("minlenMap", minlenMap);
		return getXmlView(model, "api2/sport/ottInfo.vm");
	}

	/*private boolean isOver(OpenTimeTable ott, String hour,int minutes){
		if(ott.hasPeriod() || ott.hasInning()) return false;
		Date curDate = DateUtil.currentTime();
		curDate = DateUtil.addMinute(curDate, minutes);
		String date =  DateUtil.format(curDate, "HH:mm");
		return hour.compareTo(date)<0;
	}*/
	
	/**
	 * 运动场馆下订单
	 * @param memberEncode
	 * @param ottid
	 * @param otiIdList
	 * @param mobile
	 * @param appSource
	 * @param osType
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/sport/addSportOrder.xhtml")
	public String addSportOrder(String memberEncode, Long ottid, String mobile, 
			String otiIdList, 
			Long otiid, String starttime, Integer quantity,
			Integer minlen,
			String appSource,String osType, ModelMap model, HttpServletRequest request){
		if(!ValidateUtil.isMobile(mobile)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "手机号有错误！");
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "用户不存在！");
		ApiUser partner = NewApiAuthenticationFilter.getApiAuth().getApiUser();
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, ottid);
		if(ott==null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "场次不存在！");
		ErrorCode<SportOrder> code = null;
		if(ott.hasField()){ //按场地预定
			try {
				code = sportUntransService.addSportOrder(ott, otiIdList, null, null, mobile, member, partner);
				if(!code.isSuccess()){
					dbLogger.error("订单错误：" + code.getMsg());
					return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, code.getMsg());
				}
			} catch (Exception e){
				dbLogger.error("订单错误：" + StringUtil.getExceptionTrace(e));
				return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "订单有错误，可能是时间段被别人占用，请刷新页面重新选择！");
			}
		}else if(ott.hasPeriod()){ //按时间段预定
			Integer minutes = 0;
			OpenTimeItem oti  = daoService.getObject(OpenTimeItem.class, otiid);
			if(StringUtils.isBlank(starttime)){
				List<String> timeList = SportOrderHelper.getStarttimeList(ott.getPlaydate(),oti);
				starttime = timeList.get(0);
			}
			if(StringUtils.equals(oti.getUnitType(), OpenTimeTableConstant.UNIT_TYPE_TIME)){
				minutes = minlen;
			}else {
				minutes = SportOrderHelper.getMinutes(ott, oti, starttime);
			}
			code = sportUntransService.addSportOrder(otiid, starttime, minutes, quantity, mobile, member, partner);
		}else if(ott.hasInning()){
			code = sportUntransService.addSportOrder(otiid, starttime, quantity, mobile, member, partner);
		}else {
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "场次类型错误！");
		}
		if(!code.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, code.getMsg());
		SportOrder order = code.getRetval();
		model.put("order", order);
		model.put("orderStatus", ApiConstant.getMappedOrderStatus(order.getFullStatus()));
		logAppSourceOrder(request, order, TagConstant.TAG_SPORT, null);
		return getXmlView(model, "api2/sport/sportOrder.vm");
	}
	
	@RequestMapping("/api2/sport/qryOrder.xhtml")
	public String qryOrder(String tradeno, String memberEncode, Long memberid, ModelMap model){
		if(StringUtils.isNotBlank(memberEncode)){
			Member member = this.memberService.getMemberByEncode(memberEncode);
			if(member == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "请先登录！");
		}else if(memberid==null){
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "缺少参数");
		}
		SportOrder order = daoService.getObjectByUkey(SportOrder.class, "tradeNo", tradeno, false);
		if(order==null) return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "订单不存在！");
		if(memberid!=null && !memberid.equals(order.getMemberid())){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "不能查看别人的订单");
		}
		OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, order.getOttid());
		if(ott == null) return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "场次不存在！");
		model.put("order", order);
		model.put("ott", ott);
		model.put("orderStatus", ApiConstant.getMappedOrderStatus(order.getFullStatus()));
		return getXmlView(model, "api2/sport/sportOrder.vm");
	}
	@RequestMapping("/api2/sport/orderList.xhtml")
	public String qryOrder(String memberEncode, int from, int maxnum, ModelMap model){
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "请先登录！");
		SearchOrderCommand soc = new SearchOrderCommand();
		soc.setMemberid(member.getId());
		List<SportOrder> orderList = sportOrderService.getValidSportOrderList(soc, from, maxnum);
		Map<Long, String> orderStatusMap = new HashMap<Long, String>();
		Map<Long, String> addressMap = new HashMap<Long, String>();
		for(SportOrder order : orderList){
			orderStatusMap.put(order.getId(), ApiConstant.getMappedOrderStatus(order.getFullStatus()));
			if(!addressMap.containsKey(order.getSportid())){
				Sport sport = daoService.getObject(Sport.class, order.getSportid());
				addressMap.put(order.getSportid(), sport.getAddress());
			}
		}
		model.put("orderStatusMap", orderStatusMap);
		model.put("orderList", orderList);
		model.put("addressMap", addressMap);
		return getXmlView(model, "api2/sport/sportOrderList.vm");
	}
}
