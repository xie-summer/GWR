package com.gewara.untrans.drama.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gewara.constant.OdiConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.Status;
import com.gewara.helper.DramaSeatStatusUtil;
import com.gewara.helper.TspHelper;
import com.gewara.helper.discount.DramaSpecialDiscountHelper;
import com.gewara.helper.discount.SpecialDiscountHelper;
import com.gewara.helper.order.OrderOther;
import com.gewara.json.PageView;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.DiaryHist;
import com.gewara.model.drama.DisQuantity;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.OpenTheatreSeat;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreProfile;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.drama.TheatreSeatPrice;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.MemberUsefulAddress;
import com.gewara.pay.PayValidHelper;
import com.gewara.service.DaoService;
import com.gewara.service.drama.DpiManageService;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.member.MemberService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.drama.TheatreOperationService;
import com.gewara.untrans.drama.TheatreOrderService;
import com.gewara.untrans.order.impl.SpdiscountService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;


@Service("dramaControllerService")
public class DramaControllerService {
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	
	@Autowired@Qualifier("dpiManageService")
	private DpiManageService dpiManageService;
	
	@Autowired@Qualifier("dramaOrderService")
	private DramaOrderService dramaOrderService;
	
	@Autowired@Qualifier("openDramaService")
	private OpenDramaService openDramaService;
	
	@Autowired@Qualifier("theatreOperationService")
	private TheatreOperationService theatreOperationService;
	
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	
	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;
	
	@Autowired@Qualifier("memberService")
	private MemberService memberService;
	
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	
	@Autowired@Qualifier("theatreOrderService")
	private TheatreOrderService theatreOrderService;
	
	@Autowired@Qualifier("spdiscountService")
	private SpdiscountService spdiscountService;

	/**
	 * 获取场次座位(缓存)
	 * @param model
	 * @param odi
	 * @param areaid
	 * @param response
	 * @param request
	 * @return
	 */
	public ErrorCode addSeatData(ModelMap model, OpenDramaItem odi, Long areaid, HttpServletResponse response, HttpServletRequest request, String spkey){
		if(odi==null) return ErrorCode.getFailure("该场次不存在！");
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, odi.getDpid());
		if(item == null) return ErrorCode.getFailure("该场次不存在！");
		if(!odi.isOpenseat()) return ErrorCode.getFailure("非选座场次！");
		if(!odi.isBooking()) return  ErrorCode.getFailure("本场次不接受预定！");
		if(odi.hasUnOpenToGewa()) return ErrorCode.getFailure("本场次已停止售票！");
		Theatre theatre = daoService.getObject(Theatre.class, odi.getTheatreid());
		WebUtils.setCitycode(request, theatre.getCitycode(), response);
		TheatreField field = daoService.getObject(TheatreField.class, odi.getRoomid());
		if(field==null) return  ErrorCode.getFailure("该场区不存在！");
		if(!odi.isOpenseat()) return  ErrorCode.getFailure("非选座场次！");
		List<TheatreSeatArea> seatAreaList = daoService.getObjectListByField(TheatreSeatArea.class, "dpid", item.getId());
		Collections.sort(seatAreaList, new MultiPropertyComparator(new String[]{"roomnum"}, new boolean[]{true}));
		model.put("seatAreaList", seatAreaList);
		Map<Long, String> areaZoneMap = BeanUtil.beanListToMap(seatAreaList, "id", "hotzone", true);
		model.put("areaZoneMap", JsonUtils.writeObjectToJson(areaZoneMap));
		TheatreSeatArea seatArea = null;
		if(areaid != null){
			seatArea = daoService.getObject(TheatreSeatArea.class, areaid);
		}else{
			for (TheatreSeatArea theatreSeatArea : seatAreaList) {
				if(theatreSeatArea.hasStatus(Status.Y)){
					seatArea = theatreSeatArea;
					break;
				}
			}
		}
		if(seatArea == null) return ErrorCode.getFailure("场区不存在或被删除！");
		if(!seatArea.hasStatus(Status.Y)) return ErrorCode.getFailure("场区不接受预定！");
		//先展览座位图
		String[] seatMap = dpiManageService.getAreaSeatMap(seatArea.getId());
		if(seatMap==null) {
			model.put("seatMap", seatArea.getSeatmap());
		}else{
			model.put("seatMap", seatMap[0]);
		}
		List<DisQuantity> disquanList = daoService.getObjectListByField(DisQuantity.class, "areaid", seatArea.getId());
		List<TheatreSeatPrice> tspList2 = dramaPlayItemService.getTspList(item.getId(), seatArea.getId());
		TspHelper tspHelper = new TspHelper(tspList2, disquanList);
		
		String spid = null;
		if(StringUtils.isNotBlank(spkey)){
			spid = PKCoderUtil.decryptString(spkey, SpecialDiscount.ENCODE_KEY);
		}
		if(StringUtils.isNotBlank(spid)){
			SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, new Long(spid));
			PayValidHelper pvh = new PayValidHelper(VmUtils.readJsonToMap(odi.getOtherinfo()));
			if(sd != null && DramaSpecialDiscountHelper.isEnabled(sd, odi, pvh).isSuccess()){
				model.put("adspdiscount", sd);
				model.put("spkey", spkey);
			}
		}
		Drama drama = daoService.getObject(Drama.class, odi.getDramaid());
		List<TheatreSeatPrice> tspList = new ArrayList<TheatreSeatPrice>(tspHelper.getTspList());
		Collections.sort(tspList, new PropertyComparator("price", false, true));
		model.put("odi", odi);
		model.put("field", field);
		model.put("drama", drama);
		model.put("item", item);
		model.put("theatre", theatre);
		model.put("tspList", tspList);
		model.put("tspHelper", tspHelper);
		model.put("seatArea", seatArea);
		return ErrorCode.SUCCESS;
	}
	
	/**
	 * 获取场次区域座位(及时)
	 * @param model
	 * @param odi
	 * @param areaid
	 * @param response
	 * @param request
	 * @return
	 */
	public ErrorCode<Map> getSeatPage(TheatreSeatArea seatArea){
		if(seatArea == null || !seatArea.hasStatus(Status.Y)) return ErrorCode.getFailure("该场次暂定售票！");
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", seatArea.getDpid());
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, seatArea.getDpid());
		if(odi == null || item == null || !odi.isBooking()) return ErrorCode.getFailure("该场次暂定售票！");
		if(odi.hasUnOpenToGewa()) return ErrorCode.getFailure("本场次已停止售票！");
		Map model = new HashMap(); 
		Map<Integer, String> rowMap = new HashMap<Integer, String>();
		Map<String, OpenTheatreSeat> seatMap = new HashMap<String, OpenTheatreSeat>();
		List<OpenTheatreSeat> openSeatList = openDramaService.getOpenTheatreSeatListByDpid(odi.getDpid(), seatArea.getId());
		List<SellDramaSeat> selleatList = dramaOrderService.getSellDramaSeatList(odi.getDpid(), seatArea.getId());
		ErrorCode<List<String>> remoteLockList = theatreOperationService.updateRemoteLockSeat(seatArea, OdiConstant.SECONDS_UPDATE_SEAT, true);
		if(!remoteLockList.isSuccess()) return ErrorCode.getFailure("场馆服务器连接不正常，请稍候再试！");
		model.put("rLockList", remoteLockList.getRetval());
		DramaSeatStatusUtil seatStatusUtil = new DramaSeatStatusUtil(selleatList);
		model.put("seatStatusUtil", seatStatusUtil);
		for(OpenTheatreSeat seat:openSeatList){
			rowMap.put(seat.getLineno(), seat.getSeatline());
			seatMap.put("row" + seat.getLineno() + "rank" + seat.getRankno(), seat);
		}
		List<DisQuantity> disquanList = dramaPlayItemService.getDisQuantityListByDpid(item.getId());
		List<TheatreSeatPrice> tspList2 = dramaPlayItemService.getTspList(item.getId(), seatArea.getId());
		TspHelper tspHelper = new TspHelper(tspList2, disquanList);
		
		
		Drama drama = daoService.getObject(Drama.class, odi.getDramaid());
		Theatre theatre = daoService.getObject(Theatre.class, odi.getTheatreid());
		
		List<TheatreSeatPrice> tspList = new ArrayList<TheatreSeatPrice>(tspHelper.getTspList());
		Collections.sort(tspList, new PropertyComparator("price", false, true));
		List<TheatreSeatArea> seatAreaList = daoService.getObjectListByField(TheatreSeatArea.class, "dpid", item.getId());
		model.put("seatAreaList", seatAreaList);
		model.put("odi", odi);
		model.put("drama", drama);
		model.put("rowMap", rowMap);
		model.put("seatMap", seatMap);
		model.put("theatre", theatre);
		model.put("item", item);
		model.put("openSeatList", openSeatList);
		model.put("tspList", tspList);
		model.put("tspHelper", tspHelper);
		model.put("seatArea", seatArea);
		dpiManageService.updateAreaSeatMap(seatArea, openSeatList, remoteLockList.getRetval(), seatStatusUtil);
		return ErrorCode.getSuccessReturn(model);
	}
	
	/**
	 * 获取场次数据数据
	 * @param dramaid
	 * @param fieldid
	 * @param request
	 * @param response
	 * @param cache
	 * @return
	 */
	public ErrorCode<Map> getItemList(Long dramaid, Long fieldid, HttpServletRequest request, HttpServletResponse response, final boolean cache){
		Map model = new HashMap();
		Drama drama = daoService.getObject(Drama.class, dramaid);
		if (drama == null) return ErrorCode.getFailure("话剧不存在或已经删除！");
		String citycode = drama.getCitycode();
		if(StringUtils.isBlank(citycode)){
			citycode = WebUtils.getAndSetDefault(request, response);
		}
		List<Long> idList = dramaPlayItemService.getTheatreFieldIdList(citycode, dramaid, false);
		if(fieldid == null && !idList.isEmpty() && !idList.contains(fieldid)){
			fieldid = idList.get(0);
		}
		if(idList.isEmpty()) return ErrorCode.getSuccessReturn(model);
		TheatreField field = daoService.getObject(TheatreField.class, fieldid);
		if(field == null) return ErrorCode.getSuccessReturn(model);
		List<TheatreField> fieldList = daoService.getObjectList(TheatreField.class, idList);
		model.put("fieldList", fieldList);
		if(cache && pageCacheService.isUseCache(request)) {
			PageParams params = new PageParams();
			params.addLong("dramaid", dramaid);
			params.addLong("fieldid", fieldid);
			final String pageUrl = request.getServletPath();
			PageView pageView = pageCacheService.getPageView(request, pageUrl, params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return ErrorCode.getSuccessReturn(model);
			}
		}
		model.put("drama", drama);
		List<Long> theatreIdList = BeanUtil.getBeanPropertyList(fieldList, "theatreid", true);
		Map<Long,Theatre> theatreMap = daoService.getObjectMap(Theatre.class, theatreIdList);
		model.put("theatreMap", theatreMap);
		model.put("curField", field);
		if(!theatreMap.isEmpty()){
			List<DramaPlayItem> dpiList = dramaPlayItemService.getDramaPlayItemList(citycode, field.getTheatreid(), dramaid, DateUtil.getCurFullTimestamp(), null, null);
			Collections.sort(dpiList, new MultiPropertyComparator(new String[]{"sortnum","playtime"}, new boolean[]{true,true}));
			Theatre curTheatre = daoService.getObject(Theatre.class, field.getTheatreid());
			model.put("curTheatre", curTheatre);
			model.put("dpiList", dpiList);
			Map<Long, OpenDramaItem> odiMap = new HashMap<Long, OpenDramaItem>();
			for (Iterator iterator = dpiList.iterator(); iterator.hasNext();) {
				DramaPlayItem item = (DramaPlayItem) iterator.next();
				OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", item.getId());
				if(odi != null){
					if(odi.hasUnOpenToGewa()){
						iterator.remove();
						continue;
					}
					odiMap.put(item.getId(), odi);
				}
			}
			model.put("odiMap", odiMap);
		}
		return ErrorCode.getSuccessReturn(model);	
	}
	
	/**
	 * 获取场次价格列表
	 * @param odi
	 * @return
	 */
	public ErrorCode<Map> getPriceList(OpenDramaItem odi){
		Map model = new HashMap();
		if(odi==null) return ErrorCode.getFailure("该场次不存在！");
		if(!odi.isBooking()) return ErrorCode.getFailure("该场次不接受预定！");
		final Long itemid = odi.getDpid();
		DramaPlayItem item = daoService.getObject(DramaPlayItem.class, itemid);
		if(item==null) return ErrorCode.getFailure("该场次不存在！");
		TheatreField section = daoService.getObject(TheatreField.class, odi.getRoomid());
		if(section==null) return ErrorCode.getFailure("该场区不存在！");
		List<DisQuantity> disquanList = dramaPlayItemService.getDisQuantityListByDpid(item.getId());
		List<TheatreSeatPrice> tspList2 = dramaPlayItemService.getTspList(item.getId());
		List<Long> areaIdList = BeanUtil.getBeanPropertyList(tspList2, "areaid", true);
		Map<Long, TheatreSeatArea> areaMap = daoService.getObjectMap(TheatreSeatArea.class, areaIdList);
		TspHelper tspHelper = new TspHelper(tspList2, disquanList);
		List<TheatreSeatPrice> tspList = new ArrayList<TheatreSeatPrice>(tspHelper.getTspList());
		Collections.sort(tspList, new MultiPropertyComparator(new String[]{"areaid","price"}, new boolean[]{false, true}));
		model.put("tspList", tspList);
		model.put("openType", odi.getOpentype());
		model.put("itemid", itemid);
		model.put("odi", odi);
		model.put("tspHelper", tspHelper);
		model.put("areaMap", areaMap);
		for (TheatreSeatArea seatArea : areaMap.values()) {
			theatreOperationService.updateRemoteLock(odi, seatArea, OdiConstant.SECONDS_ADDORDER, false);
		}
		return ErrorCode.getSuccessReturn(model);
	}
	
	public void putDramaOrderData(DramaOrder order, OpenDramaItem item, ModelMap model) {
		model.put("order", order);
		Member member = daoService.getObject(Member.class, order.getMemberid());
		Theatre theatre = daoService.getObject(Theatre.class, order.getTheatreid());
		TheatreProfile profile = daoService.getObject(TheatreProfile.class, theatre.getId());
		Drama drama = daoService.getObject(Drama.class, order.getDramaid());
		if(profile!=null && profile.getTopicid()!=null){
			DiaryBase diary = daoService.getObject(Diary.class, profile.getTopicid());
			if(diary == null) diary = daoService.getObject(DiaryHist.class, profile.getTopicid());
			model.put("topic", diary);
		}
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		List<MemberUsefulAddress> addressList = new ArrayList<MemberUsefulAddress>();
		if(member != null){
			if (item.isOpenCardPay()) {
				List<ElecCard> cardList = elecCardService.getAvailableCardList(order, discountList, item, member.getId()).getAvaliableList();
				model.put("cardList", cardList);
			}
			MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
			model.put("memberInfo", memberInfo);
			addressList = memberService.getMemberUsefulAddressByMeberid(order.getMemberid(), 0, 10);
		}else{
			addressList = daoService.getObjectListByField(MemberUsefulAddress.class, "mobile", order.getMobile());
		}
		if(!addressList.isEmpty()){
			model.put("usefulAddress", addressList.get(0));
		}
		model.put("addressList", addressList);
		TheatreField section = daoService.getObject(TheatreField.class, item.getRoomid());
		model.put("item", item);
		model.put("drama", drama);
		model.put("section", section);
		model.put("theatre", theatre);
		model.put("profile", profile);
		
		Map<String, String> orderOtherinfo = VmUtils.readJsonToMap(order.getOtherinfo());
		model.put("orderOtherinfo", orderOtherinfo);
		List<SellDramaSeat> seatList = null;
		if(item.isOpenseat()) seatList = dramaOrderService.getDramaOrderSeatList(order.getId());
		List<String> limitPayList = paymentService.getLimitPayList();
		List<BuyItem> buyList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
		List<OpenDramaItem> itemList = dramaOrderService.getOpenDramaItemList(item, buyList);
		model.put("buyList", buyList);
		Map<Long, OpenDramaItem> odiMap = BeanUtil.beanListToMap(itemList, "dpid");
		model.put("odiMap", odiMap);
		OrderOther orderOther = theatreOrderService.getDramaOrderOtherData(order, buyList, odiMap, model);
		Map<String, String> otherInfo = dramaOrderService.getOtherInfoMap(itemList);
		PayValidHelper valHelp = new PayValidHelper(otherInfo);
		valHelp.setLimitPay(limitPayList);
		SpecialDiscountHelper sdh = new DramaSpecialDiscountHelper(order, itemList, buyList, discountList, seatList);
		boolean openSpdiscount = StringUtils.contains(orderOther.getElecard(), PayConstant.CARDTYPE_PARTNER);
		Map discountData = spdiscountService.getSpecialDiscountData(sdh, valHelp, order, openSpdiscount, item.getSpflag(), 
				discountList, SpecialDiscount.OPENTYPE_GEWA, PayConstant.APPLY_TAG_DRAMA);
		model.putAll(discountData);
	}

}
