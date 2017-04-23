package com.gewara.web.action.admin.gewapay;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.GoodsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.acl.User;
import com.gewara.model.agency.Agency;
import com.gewara.model.agency.AgencyToVenue;
import com.gewara.model.agency.Curriculum;
import com.gewara.model.agency.TrainingGoods;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.DramaToStar;
import com.gewara.model.goods.GoodsPrice;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.pay.CalendarUtil;
import com.gewara.service.MessageService;
import com.gewara.service.drama.DramaToStarService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.sport.AgencyService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.ticket.OrderProcessService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class TrainingGoodsAdminController extends BaseAdminController {
	
	@Autowired@Qualifier("agencyService")
	private AgencyService agencyService;
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	@Autowired@Qualifier("dramaToStarService")
	private DramaToStarService dramaToStarService;
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	@Autowired@Qualifier("orderProcessService")
	private OrderProcessService orderProcessService;
	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	
	@RequestMapping("/admin/training/trainingGoodsList.xhtml")
	public String trainingGoodsList(Long agencyId, HttpServletRequest request, ModelMap model){
		String citycode = getAdminCitycode(request);
		List<Agency> agencyList = agencyService.getAgencyList(null, citycode, "hotvalue", false, 0, 100);
		if(agencyList.isEmpty()) return show404(model, "没有培训机构！");
		model.put("agencyList", agencyList);
		Agency agency = null;
		if(agencyId == null){
			agency = agencyList.get(0);
		}else{
			agency = daoService.getObject(Agency.class, agencyId);
			if(agency == null) return show404(model, "未找到此培训机构！");
		}
		List<TrainingGoods> trainingGoodsList = agencyService.getTrainingGoodsList(citycode, TagConstant.TAG_AGENCY, agency.getId(), null, null, null, "goodssort", true, false, 0, 500);
		Map<Long, List<GoodsPrice>> goodsPriceMap = new HashMap<Long, List<GoodsPrice>>();
		for (TrainingGoods trainingGoods : trainingGoodsList) {
			List<GoodsPrice> goodsPriceList = daoService.getObjectListByField(GoodsPrice.class, "goodsid", trainingGoods.getId());
			goodsPriceMap.put(trainingGoods.getId(), goodsPriceList);
		}
		model.put("goodsPriceMap", goodsPriceMap);
		List<Long> itemidList = BeanUtil.getBeanPropertyList(trainingGoodsList, "itemid", true);
		Map<Long, SportItem> SportItemMap = daoService.getObjectMap(SportItem.class, itemidList);
		model.put("SportItemMap", SportItemMap);
		List<Long> siteIdList = BeanUtil.getBeanPropertyList(trainingGoodsList, "siteid", true);
		Map<Long, Sport> SportMap = daoService.getObjectMap(Sport.class, siteIdList);
		model.put("SportMap", SportMap);
		model.put("agency", agency);
		model.put("trainingGoodsList", trainingGoodsList);
		return "admin/goods/training/trainingGoodsList.vm";
	}
	@RequestMapping("/admin/training/getTrainingGoods.xhtml")
	public String getTrainingGoods(Long id, Long agencyId, ModelMap model){
		Agency agency = daoService.getObject(Agency.class, agencyId);
		if(agency == null) return show404(model, "未找到此机构！");
		if(id != null){
			TrainingGoods trainingGoods = daoService.getObject(TrainingGoods.class, id);
			List<DramaToStar> tcDtsList = dramaToStarService.getDramaToStarListByDramaid(GoodsConstant.GOODS_TYPE_TRAINING, trainingGoods.getId(), false);
			model.put("tcDtsList", tcDtsList);
			model.put("trainingGoods", trainingGoods);
		}
		List<AgencyToVenue> atvList = daoService.getObjectListByField(AgencyToVenue.class, "agencyId", agencyId);
		List<Long> idList = BeanUtil.getBeanPropertyList(atvList, Long.class, "venueId", true);
		List<Sport> sportList = daoService.getObjectList(Sport.class, idList);
		Map<Long, Sport> sportMap = BeanUtil.groupBeanList(sportList, "id");
		List<SportItem> sportItemList = sportService.getSubSportItemList(0L, null);
		model.put("sportItemList", sportItemList);
		model.put("sportMap", sportMap);
		model.put("atvList", atvList);
		model.put("agency", agency);
		List<DramaToStar> agencyDtsList = dramaToStarService.getDramaToStarListByDramaid(TagConstant.TAG_AGENCY, agency.getId(), false);
		List<Long> staridList = BeanUtil.getBeanPropertyList(agencyDtsList, "starid", true);
		Map<Long, DramaStar> starMap = daoService.getObjectMap(DramaStar.class, staridList);
		model.put("starMap", starMap);
		model.put("agencyDtsList", agencyDtsList);
		return "admin/goods/training/trainingGoods.vm";
	}
	//保存课程
	@RequestMapping("/admin/training/saveTrainingGoods.xhtml")
	public String saveTrainingGoods(Long id, String goodsname, String tag, Long relatedid, String itemtype, Long itemid, Long placeid,
			Timestamp fromvalidtime, Timestamp tovalidtime, String summary, String description, String fitcrowd, String timetype, Integer quantity,
			String seotitle, String seodescription, String showtime, Integer minquantity, HttpServletRequest request, ModelMap model){
		String citycode = getAdminCitycode(request);
		ErrorCode<TrainingGoods> code = agencyService.saveTrainingGoods(id, citycode, goodsname, tag, relatedid, itemtype, itemid, placeid, fromvalidtime, tovalidtime, summary, description, fitcrowd, timetype, seotitle, seodescription, quantity, showtime, minquantity, getLogonUser());
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model, code.getRetval().getId() + "");
	}
	
	@RequestMapping("/admin/training/goodsOrderList.xhtml")
	public String goodsOrderList(Long gid, Long placeid, String status, String tradeNo, String mobile, Timestamp timeFrom, Timestamp timeTo,  String ctype, ModelMap model, HttpServletResponse response){
		if(gid == null && placeid == null) return show404(model, "参数错误！");
		if(gid != null){
			TrainingGoods trainingGoods = daoService.getObject(TrainingGoods.class, gid);
			if(trainingGoods == null) return showJsonError_NOT_FOUND(model);
			model.put("trainingGoods", trainingGoods);
		}else if(placeid != null){
			Agency agency = daoService.getObject(Agency.class, placeid);
			if(agency == null) return showJsonError_NOT_FOUND(model);
			model.put("agency", agency);
		}
		Map<Long, List<OrderNote>> noteMap = new HashMap<Long, List<OrderNote>>();
		List<GoodsOrder> orderList = goodsOrderService.getGoodsOrderList(gid, placeid, status, tradeNo, mobile, timeFrom, timeTo);
		for (GoodsOrder order : orderList) {
			List<OrderNote> noteList = daoService.getObjectListByField(OrderNote.class, "orderid", order.getId());
			noteMap.put(order.getId(), noteList);
		}
		model.put("orderList", orderList);
		model.put("noteMap", noteMap);
		List<Long> memberidList = ServiceHelper.getMemberIdListFromBeanList(orderList);
		addCacheMember(model, memberidList);
		if(StringUtils.isNotBlank(ctype)){
			download("xls", response);
			return "admin/goods/exportGoodsOrder.vm";
		}
		return "admin/goods/training/goodsOrderList.vm";
	}
	
	@RequestMapping("/admin/training/failConfirm.xhtml")
	public String failConfirm(String tradeNo, ModelMap model){
		GoodsOrder gorder = daoService.getObjectByUkey(GoodsOrder.class, "tradeNo", tradeNo, false);
		if(gorder == null) return showJsonError(model, "未找到此订单！");
		if(!StringUtils.startsWith(gorder.getStatus(), OrderConstant.STATUS_PAID_FAILURE)) return showJsonError(model, "非待处理的订单，不能确认！");
		User user = getLogonUser();
		ErrorCode result = orderProcessService.processOrder(gorder, "重新确认", null);
		if(result.isSuccess()) {
			dbLogger.warn(user.getUsername() + "("+user.getId()+")转换订单状态为交易成功：" + gorder.getTradeNo());	
			return showJsonSuccess(model);
		}else{
			return showJsonError(model, "转换失败：" + result.getMsg());
		}
	}
	@RequestMapping("/admin/training/sendOrderNoteSms.xhtml")
	public String sendOrderNoteSms(Long id, ModelMap model){
		OrderNote orderNote = daoService.getObject(OrderNote.class, id);
		if(orderNote == null) return showJsonError_NOT_FOUND(model);
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", orderNote.getTradeno());
		if(!StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)) return showJsonError(model, "不是成功订单不能发短信！");
		if(order instanceof GoodsOrder){
			SMSRecord sms = messageService.addTrainingOrderSms((GoodsOrder)order, orderNote, DateUtil.getCurFullTimestamp());
			if(sms != null){
				untransService.sendMsgAtServer(sms, false);
				return showJsonSuccess(model);
			}
		}
		return showJsonError(model, "发送短信错误！");
	}
	//课程表
	@RequestMapping("/admin/training/getCurriculumCalendar.xhtml")
	public String getCurriculumCalendar(Long tid, Date playDate, ModelMap model){
		TrainingGoods trainingGoods = daoService.getObject(TrainingGoods.class, tid);
		if(trainingGoods == null) return show404(model, "未找到此课程！");
		if(playDate == null) playDate = DateUtil.currentTime();
		int year = DateUtil.getYear(playDate);
		int month = DateUtil.getMonth(playDate);
		CalendarUtil calendarUtil = new CalendarUtil(year, month);
		List<Curriculum> curriculumList = agencyService.getCurriculumList(tid, playDate);
		model.put("curriculumList", curriculumList);
		model.put("calendarUtil", calendarUtil);
		model.put("playDate", playDate);
		model.put("trainingGoods", trainingGoods);
		return "/admin/agency/curriculumCalendar.vm";
	}
	@RequestMapping("/admin/training/getCurriculum.xhtml")
	public String getCurriculum(Long id, Long tid, ModelMap model){
		TrainingGoods trainingGoods = daoService.getObject(TrainingGoods.class, tid);
		if(trainingGoods == null) return showJsonError(model, "未找到此课程！");
		if(id != null){
			Curriculum curriculum = daoService.getObject(Curriculum.class, id);
			model.put("curriculum", curriculum);
		}
		model.put("trainingGoods", trainingGoods);
		return "/admin/agency/curriculumForm.vm";
	}
	@RequestMapping("/admin/training/saveCurriculum.xhtml")
	public String saveCurriculum(Long id, Long tid, String title, String remark, Date fromdate, Date todate, String classtime,
			String categoryids, String cycletype, ModelMap model){
		if(fromdate == null) return showJsonError(model, "开始时间不能为空！");
		if(todate == null) return showJsonError(model, "结束时间不能为空！");
		if(fromdate.after(todate)) return showJsonError(model, "开始时间不能在结束时间之后！");
		if(StringUtils.isBlank(classtime)) return showJsonError(model, "上课时间不能为空！");
		if(StringUtils.isBlank(cycletype)) return showJsonError(model, "循环类型不能为空！");
		if(StringUtils.isBlank(title)) return showJsonError(model, "课程题目不能为空！");
		if(StringUtils.isBlank(remark)) return showJsonError(model, "课程介绍不能为空！");
		TrainingGoods trainingGoods = daoService.getObject(TrainingGoods.class, tid);
		if(trainingGoods == null) return showJsonError(model, "未找到此课程！");
		Curriculum curriculum = null;
		if(id == null){
			curriculum = new Curriculum();
			curriculum.setRelatedid(tid);
		}else{
			curriculum = daoService.getObject(Curriculum.class, id);
		}
		curriculum.setTitle(title);
		curriculum.setRemark(remark);
		curriculum.setFromdate(fromdate);
		curriculum.setTodate(todate);
		curriculum.setClasstime(classtime);
		curriculum.setCategoryids(categoryids);
		curriculum.setCycletype(cycletype);
		daoService.saveObject(curriculum);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/training/isSure.xhtml")
	public String isSure(String tradeNo, ModelMap model){
		GoodsOrder gorder = daoService.getObjectByUkey(GoodsOrder.class, "tradeNo", tradeNo, false);
		if(gorder == null) return showJsonError(model, "未找到此订单！");
		if(!StringUtils.equals(gorder.getStatus(), OrderConstant.STATUS_PAID_SUCCESS)) return showJsonError(model, "非待处理的订单，不能确认！");
		Map descMap = JsonUtils.readJsonToMap(gorder.getDescription2());
		descMap.put(OrderConstant.TRAINING_ORDER_IS_SURE, Status.Y);
		gorder.setDescription2(JsonUtils.writeMapToJson(descMap));
		daoService.saveObject(gorder);
		User user = getLogonUser();
		dbLogger.warn(user.getUsername() + "("+user.getId()+")确定了此订单被商户确认：" + gorder.getTradeNo());
		return showJsonSuccess(model);
	}
}
