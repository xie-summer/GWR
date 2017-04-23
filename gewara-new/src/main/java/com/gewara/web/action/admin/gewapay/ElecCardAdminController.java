package com.gewara.web.action.admin.gewapay;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.bank.BankConstant;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ManageConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.order.ElecCardConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.sys.SpecialRights;
import com.gewara.model.acl.User;
import com.gewara.model.api.ApiUser;
import com.gewara.model.common.JsonData;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.Theatre;
import com.gewara.model.goods.Goods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.ElecCardBatch;
import com.gewara.model.pay.ElecCardExtra;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.monitor.SysLogType;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class ElecCardAdminController extends BaseAdminController {
	@Autowired @Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate template) {
		jdbcTemplate = template;
	}

	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;
	public void setElecCardService(ElecCardService elecCardService) {
		this.elecCardService = elecCardService;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired@Qualifier("untransService")
	private UntransService untransService;

	// 电子影票列表
	@RequestMapping("/admin/eticket/batchList.xhtml")
	public String elecTicketList(String status, String applydept, String applytype, 
			Long adduserid, Timestamp addfrom, Timestamp addto, ModelMap model){
		if(StringUtils.isBlank(status)) status = ElecCardConstant.DATA_NOW;
		if(addfrom==null) addfrom = DateUtil.addDay(DateUtil.getBeginningTimeOfDay(DateUtil.getMillTimestamp()), -90);
		Map<Long, List<ElecCardExtra>> subMap = new HashMap<Long, List<ElecCardExtra>>();
		List<ElecCardExtra> topList = elecCardService.getTopCardExtraList(status, applydept, applytype);
		List<Long> topidList = BeanUtil.getBeanPropertyList(topList, Long.class, "batchid", false);
		List<ElecCardExtra> allsubList = elecCardService.getSubCardExtraList(status, applydept, applytype, adduserid, addfrom, addto);
		List<Long> idList = new ArrayList<Long>();
		idList.addAll(topidList);
		idList.addAll(BeanUtil.getBeanPropertyList(allsubList, Long.class, "batchid", false));
		Map<Long, User> userMap = new HashMap<Long, User>();
		for(ElecCardExtra extra : topList){
			if(!userMap.containsKey(extra.getAdduserid())){
				userMap.put(extra.getAdduserid(), daoService.getObject(User.class, extra.getAdduserid()));
			}
		}
		for(ElecCardExtra batch: allsubList){
			List<ElecCardExtra> subList = subMap.get(batch.getPid());
			if(subList == null) {
				subList = new ArrayList<ElecCardExtra>();
				subMap.put(batch.getPid(), subList);
			}
			subList.add(batch);
			if(!userMap.containsKey(batch.getAdduserid())){
				userMap.put(batch.getAdduserid(), daoService.getObject(User.class, batch.getAdduserid()));
			}
		}
		List<Long> newidList = new ArrayList<Long>(subMap.keySet());
		newidList.removeAll(topidList);
		topList.addAll(daoService.getObjectList(ElecCardExtra.class, newidList));
		Collections.sort(topList, new PropertyComparator("addtime", false, false));
		idList.addAll(newidList);
		model.put("topList", topList);
		model.put("subMap", subMap);
		model.put("addfrom", addfrom);
		model.put("addto", addto);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		model.put("deptMap", ManageConstant.deptMap);
		model.put("applyMap", ManageConstant.applyMap);
		model.put("userMap", userMap);
		model.put("cardtypeMap", ElecCardConstant.CARDTYPEMAP);
		model.put("userList", elecCardService.getAddBatchUserList());
		Map<Long, ElecCardBatch> batchMap = daoService.getObjectMap(ElecCardBatch.class, idList);
		model.put("batchMap", batchMap);
		
		return "admin/eticket/batchList.vm";
	}
	
	@RequestMapping("/admin/eticket/updateStats.xhtml")
	public String updateStats(Long bid, ModelMap model){
		if(bid != null){
			ElecCardExtra extra = daoService.getObject(ElecCardExtra.class, bid);
			elecCardService.updateBatchExtra(extra);
		}else{
			List<ElecCardExtra> batchList = elecCardService.getSubCardExtraListByStatus(ElecCardConstant.DATA_NOW);
			for(ElecCardExtra extra: batchList){
				elecCardService.updateBatchExtra(extra);
			}
		}
		return showMessage(model, "成功执行：" + new Timestamp(System.currentTimeMillis()));
	}
	@RequestMapping("/admin/eticket/frozenBatch.xhtml")
	public String setToHis(Long bid, ModelMap model){
		ErrorCode code = elecCardService.frozenBatch(bid);
		if(code.isSuccess()) return forwardMessage(model, "成功");
		return forwardMessage(model, code.getMsg());
	}
	// 电子影票列表
	@RequestMapping("/admin/eticket/issuerBatchList.xhtml")
	public String issuerBatchList(ModelMap model){
		User user = getLogonUser();
		List<ElecCardExtra> batchList = elecCardService.getSubCardExtraListByIssuerId(user.getId());
		Map<Long, ElecCardBatch> elecCardBatchMap = new HashMap<Long, ElecCardBatch>();
		for(ElecCardExtra eCardExtra : batchList){
			elecCardBatchMap.put(eCardExtra.getBatchid(), daoService.getObject(ElecCardBatch.class, eCardExtra.getBatchid()));
		}
		model.put("elecCardBatchMap", elecCardBatchMap);
		model.put("batchList", batchList);
		return "admin/eticket/issuerBatchList.vm";
	}
	// 电子影票列表
	@RequestMapping("/admin/eticket/viewBatchList.xhtml")
	public String viewBatchList(ModelMap model){
		List<ElecCardExtra> batchList = elecCardService.getAllSubCardExtraList();
		model.put("batchList", batchList);
		return "admin/eticket/viewBatchList.vm";
	}
	// 编辑批次
	@RequestMapping("/admin/eticket/modifyBatch.xhtml")
	public String modifyBatch(Long bid, ModelMap model){
		ElecCardBatch batch = new ElecCardBatch();
		if(bid != null){
			batch = daoService.getObject(ElecCardBatch.class, bid);
		}
		List<ApiUser> partnerList = hibernateTemplate.find("from ApiUser a where a.status=? order by a.id", ApiUser.STATUS_OPEN);
		model.put("batch", batch);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		model.put("partnerList", partnerList);
		model.put("paytextMap", PaymethodConstant.getPayTextMap());
		model.put("paybankMap", BankConstant.getPnrBankMap());
		return "admin/eticket/modifyBatch.vm";
	}
	// 保存批次
	@RequestMapping(value="/admin/eticket/saveBatch.xhtml", method=RequestMethod.POST)
	public String saveBatch(ElecCardBatch batchBean, Long bid, ModelMap model){
		String msg = "保存成功！";
		if(StringUtils.isNotBlank(batchBean.getCosttype())){
			if(!StringUtils.equals(batchBean.getCardtype(), PayConstant.CARDTYPE_A) &&
					!StringUtils.equals(batchBean.getCardtype(), PayConstant.CARDTYPE_B)) {
				return showJsonError(model, "设置了“更改成本价”，“卡类型”也必须为A卡或B卡！");
			}
			if(batchBean.getCardtype().equals(PayConstant.CARDTYPE_B)){
				msg += "设置了“更改成本价”，卡类型为“B卡”，注意限价条件：用户购买金额+补差金额>=成本价！";
			}
			if(batchBean.getCostnum()==null) return showJsonError(model, "设置了“更改成本价”，“成本价增量”也必须设置！");
		}
		if(batchBean.getBindgoods()!=null){
			if(batchBean.getBindratio()==null) return showJsonError(model, "设置了“绑定套餐”，“使用数量”也必须设置！");
			Goods goods = daoService.getObject(Goods.class, batchBean.getBindgoods());
			if(goods==null) return showJsonError(model, "绑定的套餐不存在！");
			//if(goods.getUnitprice() !=0) return showError(model, "绑定的套餐必须是赠品！");
		}
		if(StringUtils.isBlank(batchBean.getChannelinfo())){
			return showJsonError(model, "渠道说明必须填写");
		}
		ElecCardBatch batch = null;
		User user = getLogonUser();
		ChangeEntry batchChange = null;
		if(bid != null){//修改现有的, 批次改不了。
			batch = daoService.getObject(ElecCardBatch.class, bid);
			ElecCardExtra batchExtra = daoService.getObject(ElecCardExtra.class, bid);
			if(!batchExtra.getAdduserid().equals(user.getId()) && !user.isRole(SpecialRights.ELECCARD)) {
				return showJsonError(model, "只有加入者才能修改！");
			}
			if(StringUtils.equals(batchExtra.getStatus(), ElecCardConstant.DATA_HIS)){
				return showJsonError(model, "已经冻结的批次不能修改！");
			}
			if(!StringUtils.equals(batch.getCardtype(), batchBean.getCardtype()) || !batch.getAmount().equals(batchBean.getAmount())){
				if(elecCardService.hasUsed(batch)){
					return showJsonError(model, "该批次已被使用过，不能修改卡类型、金额！");
				}
			}
			batchChange = new ChangeEntry(batch);
			batch.copyFrom(batchBean);
			if(batch.getDaynum()==null) batch.setDaynum(0);
			if(StringUtils.equals(batch.getActivation(), ElecCardBatch.ACTIVATION_Y) && batch.getAppoint() == null)
				return showJsonError(model, "该批次设置了需要激活，指定数量不能为空！");
			daoService.saveObject(batch);
			monitorService.saveChangeLog(user.getId(), ElecCardExtra.class, batch.getId(), batchChange.getChangeMap(batch));
		}else{//新卡
			batch = new ElecCardBatch();
			batch.copyFrom(batchBean);
			if(batch.getDaynum()==null) batch.setDaynum(0);
			if(!StringUtils.equals(batch.getCardtype(), "A")){
				batch.setExchangetype("");
			}
			if(StringUtils.equals(batch.getActivation(), ElecCardBatch.ACTIVATION_Y) && batch.getAppoint() == null)
				return showJsonError(model, "该批次设置了需要激活，指定数量不能为空！");
			elecCardService.addCardBatch(batch, user.getId());
			monitorService.saveAddLog(user.getId(), ElecCardBatch.class, batch.getId(), batch);
		}
		
		return showJsonSuccess(model, msg);
	}
	@RequestMapping("/admin/eticket/modifyExtra.xhtml")
	public String modifyExtra(Long bid, ModelMap model){
		if(bid != null){
			ElecCardExtra extra = daoService.getObject(ElecCardExtra.class, bid);
			model.put("extra", extra);
		}
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		List<User> userList = daoService.getObjectList(User.class, "username", true, 0, 1000);
		model.put("userList", userList);
		model.put("deptMap", ManageConstant.deptMap);
		model.put("applyMap", ManageConstant.applyMap);
		Map tmpMap = mongoService.findOne(MongoData.NS_VOUCHERCARD_ISSUERID, "_id", "issuerid");
		if(tmpMap != null){
			model.put("userIdList",Arrays.asList(StringUtils.split((String)tmpMap.get("userIdList"), ",")));
		}
		return "admin/eticket/modifyExtra.vm";
	}
	/**
	 * 设置发行人
	 * @param request
	 * @param bid
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/eticket/setExtraUser.xhtml")
	public String setExtraUser(String[] userIds, ModelMap model){
		if(userIds == null){
			return showJsonError(model, "请选择用户！");
		}
		Map tmpMap = mongoService.findOne(MongoData.NS_VOUCHERCARD_ISSUERID, "_id", "issuerid");
		if(tmpMap == null){
			tmpMap = new HashMap<String, String>();
			tmpMap.put("_id", "issuerid");
		}
		tmpMap.put("userIdList", StringUtils.join(userIds, ","));
		mongoService.saveOrUpdateMap(tmpMap, "_id", MongoData.NS_VOUCHERCARD_ISSUERID);
		return showJsonSuccess(model);
	}
	@RequestMapping(value="/admin/eticket/saveExtra.xhtml", method=RequestMethod.POST)
	public String saveExtra(HttpServletRequest request, Long bid, ModelMap model){
		ElecCardExtra extra = daoService.getObject(ElecCardExtra.class, bid);
		User user = getLogonUser();
		if(!extra.getAdduserid().equals(user.getId()) && !user.isRole(SpecialRights.ELECCARD)) {
			return showJsonError(model, "只有加入者才能修改！");
		}
		ChangeEntry change = new ChangeEntry(extra);
		BindUtils.bindData(extra, request.getParameterMap());
		daoService.saveObject(extra);
		monitorService.saveChangeLog(user.getId(), ElecCardExtra.class, extra.getBatchid(), change.getChangeMap(extra));
		return showJsonSuccess(model, "保存成功！");
	}
	@RequestMapping("/admin/eticket/preSellBatch.xhtml")
	public String preSellBatch(Long bid, ModelMap model){
		User user = getLogonUser();
		ErrorCode<ElecCardBatch> batch = elecCardService.preSellBatch(bid, user.getId());
		if(batch.isSuccess()){
			model.put("bid", batch.getRetval().getId());
			return "redirect:/admin/eticket/modifyBatch.xhtml";
		}else{
			return forwardMessage(model, batch.getMsg());
		}
	}
	
	// 生成电子影票
	@RequestMapping("/admin/eticket/genElecCard.xhtml")
	public String genElecCard(Long bid, int num, ModelMap model) {
		User user = getLogonUser();
		boolean allowGen = operationService.updateOperation("genElecCard", 300);
		ErrorCode<ElecCardExtra> code = null;
		if(allowGen) {
			try{
				code = elecCardService.genElecCard(bid, num, user.getId());
			}finally{
				operationService.resetOperation("genElecCard", 300);
			}
		}else{
			return showMessage(model, "还有其他人在生成，请等待1~5分钟！");
		}
		if(code.isSuccess()){
			ElecCardExtra extra = code.getRetval();
			elecCardService.updateBatchExtra(extra);
			return showMessage(model, "生成了" + num + "张卡, 请再次确认此批次的信息。");
		}else{
			return showError(model, code.getMsg());
		}
	}
	@RequestMapping("/admin/eticket/manageBatch.xhtml")
	public String manageBatch(Long bid, Integer pageNo, ModelMap model){
		ElecCardBatch batch = daoService.getObject(ElecCardBatch.class, bid);
		ElecCardExtra batchExtra = daoService.getObject(ElecCardExtra.class, bid);
		ElecCardExtra parentExtra = daoService.getObject(ElecCardExtra.class, batchExtra.getPid());
		Integer count = elecCardService.getElecCardCountByBatchId(bid, null);
		if(pageNo==null) pageNo=0;
		int rowsPerpage = 1000;
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/eticket/manageBatch.xhtml");
		Map params = new HashMap();
		params.put("bid", ""+bid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);		
		List<ElecCard> cardList = elecCardService.getElecCardByBatchId(bid, null, false, pageNo*rowsPerpage, rowsPerpage);
		List<Long> possessorList = BeanUtil.getBeanPropertyList(cardList, Long.class, "possessor", true);
		Map<Long, Member> possessorMap = daoService.getObjectMap(Member.class, possessorList);
		model.put("possessorMap", possessorMap);
		model.put("batch", batch);
		model.put("batchExtra", batchExtra);
		model.put("parentExtra", parentExtra);
		model.put("cardList", cardList);
		return "admin/eticket/manageBatch.vm";
	}
	@RequestMapping("/admin/eticket/viewCardList.xhtml")
	public String viewCardList(Long bid, Integer pageNo, String status, String cw, ModelMap model){
		User user = getLogonUser();
		ElecCardBatch batch = daoService.getObject(ElecCardBatch.class, bid);
		ElecCardExtra batchExtra = daoService.getObject(ElecCardExtra.class, bid);
		if(!user.getId().equals(batchExtra.getIssuerid()) && StringUtils.isBlank(cw)) return showError(model, "无权限访问！");
		Integer count = elecCardService.getElecCardCountByBatchId(bid, status);
		if(pageNo==null) pageNo=0;
		int rowsPerpage = 1000;
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/eticket/viewCardList.xhtml");
		Map params = new HashMap();
		params.put("bid", ""+bid);
		params.put("status", status);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);		
		List<ElecCard> cardList = elecCardService.getElecCardByBatchId(bid, status, false, pageNo*rowsPerpage, rowsPerpage);
		List<Long> possessorList = BeanUtil.getBeanPropertyList(cardList, Long.class, "possessor", true);
		Map<Long, Member> possessorMap = daoService.getObjectMap(Member.class, possessorList);
		model.put("possessorMap", possessorMap);
		model.put("batch", batch);
		model.put("cardList", cardList);
		return "admin/eticket/viewCardList.vm";
		
	}
	@RequestMapping(method=RequestMethod.GET, value="/admin/eticket/assignMobile.xhtml")
	public String assignMobile(Long bid, Integer pageNo, ModelMap model){
		if (pageNo==null) pageNo=0;
		Integer rowsPerPage = 10000;
		Integer from = pageNo * rowsPerPage;
		Integer count = elecCardService.getElecCardCountByBatchId(bid,null);
		ElecCardBatch batch = daoService.getObject(ElecCardBatch.class, bid);
		List<ElecCard> cardList = elecCardService.getElecCardByBatchId(bid, null, true, from, rowsPerPage);
		Map<Object, List> mobileGroup = BeanUtil.groupBeanProperty(cardList, "mobile", "cardno");
		Map<String, String> repeatMap = new HashMap<String, String>();
		for(Object mobile: mobileGroup.keySet()){
			if(mobileGroup.get(mobile).size() > 1) repeatMap.put(""+mobile, StringUtils.join(mobileGroup.get(mobile), ","));
		}
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/eticket/assignMobile.xhtml");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("bid", ""+bid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("batch", batch);
		model.put("repeatMap", repeatMap);
		model.put("cardList", cardList);
		return "admin/eticket/assignMobile.vm";
	}
	@RequestMapping(method=RequestMethod.POST, value="/admin/eticket/assignMobile.xhtml")
	public String assignMobile(Long bid, String mobiles, Integer num, 
			@RequestParam String cardFrom, @RequestParam String cardTo, ModelMap model){
		User user = getLogonUser();
		List<String> mobileList = new ArrayList<String>(Arrays.asList(mobiles.split(",")));
		ErrorCode<String> code = elecCardService.assignMobile(bid, mobileList, num, cardFrom, cardTo);
		if(code.isSuccess()){
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, user.getRealname() + "分配卡号：" + code.getMsg());
			return showJsonSuccess(model, code.getMsg());
		}
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping(method=RequestMethod.GET, value="/admin/eticket/bind2Member.xhtml")
	public String bindToMember(Long bid, ModelMap model){
		ElecCardBatch batch = daoService.getObject(ElecCardBatch.class, bid);
		List<ElecCard> cardList = elecCardService.getElecCardByBatchId(bid, null, false, 0, 10000);
		Map<Object, List> gainerGroup = BeanUtil.groupBeanProperty(cardList, "gainer", "cardno");
		
		Map<String, String> repeatMap = new HashMap<String, String>();
		for(Object gainer: gainerGroup.keySet()){
			if(gainerGroup.get(gainer).size() > 1) repeatMap.put(""+gainer, StringUtils.join(gainerGroup.get(gainer), ","));
		}
		model.put("batch", batch);
		model.put("repeatMap", repeatMap);
		model.put("cardList", cardList);
		return "admin/eticket/bind2Member.vm";
	}
	@RequestMapping(method=RequestMethod.POST, value="/admin/eticket/bind2Member.xhtml")
	public String bind2Member(Long bid, String flag, String memberids, Integer num, 
			@RequestParam String cardFrom, @RequestParam String cardTo, ModelMap model){
		if(StringUtils.isBlank(flag)) return showJsonError(model, "标记不能为空！");
		User user = getLogonUser();
		List<Long> memberidList = BeanUtil.getIdList(memberids, ",");
		ErrorCode<String> code = elecCardService.bind2Member(bid, flag.trim(), memberidList, num, cardFrom, cardTo);
		if(code.isSuccess()){
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, user.getRealname() + "绑定卡号：" + code.getMsg());
			return showJsonSuccess(model, code.getMsg());
		}
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/eticket/unbindMember.xhtml")
	public String unbindMember(Long bid, String flag, String cardFrom, String cardTo, ModelMap model){
		if(StringUtils.isBlank(flag)) return showJsonError(model, "标记不能为空！");
		ErrorCode<String> code = elecCardService.unbindMember(bid, flag.trim(), cardFrom, cardTo);
		User user = getLogonUser();
		if(code.isSuccess()){
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, user.getRealname() + "取消绑定卡号：" + code.getMsg());
			return showJsonSuccess(model, code.getRetval());
		}
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/eticket/unassignMobile.xhtml")
	public String unassignMobile(Long bid, String cardFrom, String cardTo, ModelMap model){
		ErrorCode<String> code = elecCardService.unassignMobile(bid, cardFrom, cardTo);
		User user = getLogonUser();
		if(code.isSuccess()){
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_ORDER_PAY, user.getRealname() + "取消分配卡号：" + code.getMsg());
			return showJsonSuccess(model, code.getRetval());
		}
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/eticket/addAssignMsg.xhtml")
	public String addAssignMsg(@RequestParam(required=true) Long bid, 
			@RequestParam(required=true)Timestamp sendtime, 
			@RequestParam(required=true)String notifymsg, ModelMap model){
		List<ElecCard> cardList = elecCardService.getElecCardByBatchId(bid, ElecCardConstant.STATUS_SOLD, false, 0, 30000);
		int count = 0;
		for(ElecCard card: cardList){
			if(StringUtils.isNotBlank(card.getMobile()) && ValidateUtil.isMobile(card.getMobile())){
				String content = notifymsg.replace("cardno", card.getCardno())
						.replace("shortno", card.getCardno().substring(card.getCardno().length() - 5));
				
				SMSRecord sms = new SMSRecord(bid, card.getCardno(), card.getMobile(), content,
						sendtime, DateUtil.addDay(sendtime, 2), SmsConstant.SMSTYPE_ECARD);
				untransService.addMessage(sms);
				count++;
			}
		}
		ElecCardBatch batch = daoService.getObject(ElecCardBatch.class, bid);
		batch.setNotifymsg(notifymsg);
		daoService.saveObject(batch);
		return showJsonSuccess(model, "共加入" + count + "个短信");
	}
	
	/**
	 * 使用情况列表
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/eticket/useStat.xhtml")
	public String useStat(ModelMap model, Timestamp from, Timestamp to){
		String sql = "SELECT T.CARDTYPE, TO_CHAR(S.ADDTIME, 'yyyy-mm-dd') USEDATE, COUNT(T.RECORDID) USECOUNT, SUM(T.AMOUNT) USEAMOUNT " +
				"FROM WEBDATA.DISCOUNT_ITEM T INNER JOIN WEBDATA.TICKET_ORDER S ON T.ORDERID = S.RECORDID " +
				"WHERE S.STATUS='paid_success' AND S.ADDTIME >= ? AND S.ADDTIME <? " +
				"GROUP BY T.CARDTYPE, TO_CHAR(s.ADDTIME, 'yyyy-mm-dd') ";
		if(from == null) from = DateUtil.addDay(DateUtil.getCurTruncTimestamp(), -7);
		if(to == null) to = new Timestamp(System.currentTimeMillis());
		List<Map<String, Object>> useList = jdbcTemplate.queryForList(sql, from, to);
		Map<String/*date*/, Map/*Stat.*/> countMap = new HashMap<String, Map>();
		Map<String/*date*/, Map/*Stat.*/> amountMap = new HashMap<String, Map>();
		Set<String> cardTypes = new HashSet<String>();
		for(Map<String, Object> row: useList){
			String date = (String) row.get("USEDATE");
			String cardtype = (String) row.get("CARDTYPE");
			cardTypes.add(cardtype);
			Map cuse = countMap.get(date);
			Map suse = amountMap.get(date);
			if(cuse==null){
				cuse = new HashMap();
				countMap.put(date, cuse);
				suse = new HashMap();
				amountMap.put(date, suse);
			}
			cuse.put(cardtype, row.get("USECOUNT"));
			suse.put(cardtype, row.get("USEAMOUNT"));
		}
		List<String> dateList = new ArrayList<String>(countMap.keySet());
		Collections.sort(dateList);
		model.put("from", from);
		model.put("to", to);
		model.put("dateList", dateList);
		model.put("countMap", countMap);
		model.put("cardTypes", countMap);
		model.put("amountMap", amountMap);
		return "admin/eticket/useStat.vm";
	}
	@RequestMapping(value="/admin/eticket/batchDiscard.xhtml", method=RequestMethod.GET)
	public String batchDiscard(){
		return "admin/eticket/batchDiscard.vm";
	}
	@RequestMapping(value="/admin/eticket/batchDiscard.xhtml", method=RequestMethod.POST)
	public String batchDiscard(String bytype, String cardtext, ModelMap model){
		String[] cardList = StringUtils.split(cardtext, ",");
		
		Long userId = getLogonUser().getId();
		List<String> msgList = elecCardService.batchDiscard(userId, cardList, StringUtils.equals(bytype, "byno"));
		return forwardMessage(model, msgList);
	}
	@RequestMapping("/admin/eticket/queryCard.xhtml")
	public String queryCard(String cardno, String type, ModelMap model){
		User user = getLogonUser();
		if(StringUtils.isNotBlank(cardno)){
			ElecCard card = null;
			if(StringUtils.equals(type, "1")){
				if(cardno.startsWith("G") && cardno.length()==17) card = elecCardService.queryCardByNo(cardno);
				else card = elecCardService.getElecCardByPass(cardno);
			}else{
				if(cardno.startsWith("G") && cardno.length()==17) card = elecCardService.getHistElecCardByNo(cardno);
				else card = elecCardService.getHistElecCardByPass(cardno);
			}
			if(card != null) {
				if(user.isRole(SpecialRights.ELECCARD)){
					Map<String, String> entry = new LinkedHashMap<String, String>();
					entry.put("userid", "" + user.getId());
					entry.put("cardno", cardno);
					entry.put("tag", "elecCard");
					entry.put("category", "qryPass");
					monitorService.addSysLog(SysLogType.userOp, entry);
					dbLogger.warn("用户查询卡密码：" + user.getId() + ":" + cardno);
					model.put("cardpass", elecCardService.getElecCardpass(card));
				}
				model.put("card", card);
				ElecCardBatch batch = card.getEbatch();
				model.put("batch", batch);
				ElecCardExtra extra = daoService.getObject(ElecCardExtra.class, batch.getId());
				model.put("extra", extra);
				if(StringUtils.isNotBlank(batch.getValidcinema())){
					List<Long> idList = BeanUtil.getIdList(batch.getValidcinema(), ",");
					if(StringUtils.equals(batch.getTag(), "movie")){
						Map<Long, Cinema> placeMap = daoService.getObjectMap(Cinema.class, idList);
						model.put("placeMap", placeMap);
					}if(StringUtils.equals(batch.getTag(), "drama")){
						Map<Long, Theatre> placeMap = daoService.getObjectMap(Theatre.class, idList);
						model.put("placeMap", placeMap);
					}if(StringUtils.equals(batch.getTag(), "drama")){
						Map<Long, Theatre> placeMap = daoService.getObjectMap(Theatre.class, idList);
						model.put("placeMap", placeMap);
					}
				}
				if(StringUtils.isNotBlank(batch.getValidmovie())){
					List<Long> idList = BeanUtil.getIdList(batch.getValidmovie(), ",");
					if(StringUtils.equals(batch.getTag(), "movie")){
						Map<Long, Movie> categoryMap = daoService.getObjectMap(Movie.class, idList);
						model.put("categoryMap", categoryMap);
					}if(StringUtils.equals(batch.getTag(), "drama")){
						Map<Long, Drama> categoryMap = daoService.getObjectMap(Drama.class, idList);
						model.put("categoryMap", categoryMap);
					}
				}
				if(card.getPossessor() != null){
					Member member = daoService.getObject(Member.class, card.getPossessor());
					model.put("member", member);
				}
				if(card.getOrderid()!=null){
					TicketOrder order = daoService.getObject(TicketOrder.class, card.getOrderid());
					model.put("order", order);
				}
			}
		}
		return "admin/eticket/queryCard.vm";
	}
	
	
	@RequestMapping("/admin/eticket/getCardListByUser.xhtml")
	public String getCardListByUser(String tag,String ukeyName,String ukeyValue, ModelMap model, Integer pageNo,HttpServletRequest request){
		if(pageNo == null) pageNo = 0;
		int rowsPerPage = 20;
		int firstRow = pageNo*rowsPerPage;
		String keys = "idnicknameemailmobile";
		if(StringUtils.isNotBlank(ukeyName) && StringUtils.isNotBlank(ukeyValue) && keys.contains(ukeyName)){
			Member member = null;
			if(StringUtils.equals("id", ukeyName) && ValidateUtil.isNumber(ukeyValue)){
				member = this.daoService.getObject(Member.class, Long.valueOf(ukeyValue));
			}else{
				member = this.daoService.getObjectByUkey(Member.class, ukeyName, ukeyValue, false);
			}
			List<ElecCard> cardList = elecCardService.getCardListByMemberid(member.getId(), tag, firstRow, rowsPerPage);
			Integer count = elecCardService.getCardCountByMemberid(member.getId(), tag);
			PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"/admin/eticket/getCardListByUser.xhtml");
			pageUtil.initPageInfo(request.getParameterMap());
			model.put("pageUtil", pageUtil);
			model.put("cardList", cardList);
			model.put("cardtag", tag);
			tag = tag.replace("movie", "电影").replace("drama", "话剧").replace("sport", "运动");
			model.put("msg", "此用户一共有"+count+"张"+tag+"票券");
		}
		return "admin/sns/cardList.vm";
	}

	@RequestMapping("/admin/eticket/batchBalance.xhtml")
	public String batchBalance(Long bid, ModelMap model){
		DetachedCriteria qry = DetachedCriteria.forClass(ElecCard.class, "e");
		qry.createAlias("e.ebatch", "batch");
		qry.add(Restrictions.eq("batch.id", bid));
		List<ElecCard> cardList = hibernateTemplate.findByCriteria(qry);
		model.put("batchList", cardList);
		JsonData data = daoService.getObject(JsonData.class, JsonDataKey.KEY_BATCHBALANCE);
		int balance = 0;
		if(data!=null) {
			String strbalance = VmUtils.getJsonValueByKey(data.getData(), bid+"");
			if(StringUtils.isNotBlank(strbalance)) balance = Integer.valueOf(strbalance);
		}
		model.put("balance", balance);
		model.put("cardList", cardList);
		return "admin/eticket/batchBalance.vm";
	}
	@RequestMapping("/admin/eticket/ajax/setBalance.xhtml")
	public String setBalance(Long bid, Integer balance, ModelMap model){
		JsonData data = daoService.getObject(JsonData.class, JsonDataKey.KEY_BATCHBALANCE);
		if(data==null) data = new JsonData(JsonDataKey.KEY_BATCHBALANCE, "");
		String result = JsonUtils.addJsonKeyValue(data.getData(), bid+"", balance+"");
		data.setData(result);
		daoService.saveObject(data);
		return showJsonSuccess(model);
	}	
	/*
	 * 电子票业务操作
	 */
	@RequestMapping("/admin/eticket/manu/discardElecCard.xhtml")
	public String discardElecCard(Long cardId, ModelMap model){
		User user = getLogonUser();
		ErrorCode code = elecCardService.discardElecCard(cardId, user.getId());
		if(code.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/eticket/manu/lockElecCard.xhtml")
	public String lockcardElecCard(Long cardId, ModelMap model){
		User user = getLogonUser();
		ErrorCode code = elecCardService.lockElecCard(cardId, user.getId());
		if(code.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/eticket/manu/batchLockElecCard.xhtml")
	public String batchLockElecCard(Long batchId, String cardFrom, String cardTo, ModelMap model){
		User user = getLogonUser();
		ErrorCode  code = elecCardService.batchLockElecCard(batchId, cardFrom, cardTo, user.getId());
		if(code.isSuccess()) return showJsonSuccess(model, code.getRetval()+"");
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/eticket/manu/unlockcardElecCard.xhtml")
	public String unlockcardElecCard(Long cardId, ModelMap model){
		User user = getLogonUser();
		ErrorCode code = elecCardService.unlockcardElecCard(cardId, user.getId());
		if(code.isSuccess()) return showJsonSuccess(model);
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/eticket/manu/batchUnLockElecCard.xhtml")
	public String batchUnLockElecCard(Long batchId, String cardFrom, String cardTo, ModelMap model){
		User user = getLogonUser();
		ErrorCode code = elecCardService.batchUnLockElecCard(batchId, cardFrom, cardTo, user.getId());
		if(code.isSuccess()) return showJsonSuccess(model, code.getRetval()+"");
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/eticket/manu/lockElecCardByMemberid.xhtml")
	public String lockElecCard(Long memberid, Long batchid, ModelMap model){
		if(memberid == null) return showJsonError(model, "用户ID不能为空！");
		User user = getLogonUser();
		ErrorCode code = elecCardService.batchLockElecCard(batchid, memberid,  user.getId());
		if(code.isSuccess()) return showJsonSuccess(model, code.getRetval()+"");
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/eticket/manu/unLockElecCardByMemberid.xhtml")
	public String unlockElecCard(Long memberid, Long batchid, ModelMap model){
		if(memberid == null) return showJsonError(model, "用户ID不能为空！");
		User user = getLogonUser();
		ErrorCode code = elecCardService.batchUnLockElecCard(batchid, memberid,  user.getId());
		if(code.isSuccess()) return showJsonSuccess(model, code.getRetval()+"");
		return showJsonError(model, code.getMsg());
	}
	
	@RequestMapping("/admin/eticket/manu/unsellElecCard.xhtml")
	public String unsellElecCard(Long cardId, ModelMap model){
		User user = getLogonUser();
		ErrorCode<String> code = elecCardService.unsellElecCard(cardId, user.getId());
		if(code.isSuccess()) return showJsonSuccess(model, code.getRetval()+"");
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/eticket/manu/returnElecCard.xhtml")
	public String returnElecCard(Long cardId, ModelMap model){
		User user = getLogonUser();
		ErrorCode<String> code = elecCardService.returnElecCard(cardId, user.getId());
		if(code.isSuccess()) return showJsonSuccess(model, code.getRetval()+"");
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/eticket/manu/addElecCardFromParent.xhtml")
	public String addElecCardFromParent(Long batchId, String cardFrom, String cardTo, ModelMap model){
		User user = getLogonUser();
		ErrorCode<String> code = elecCardService.addCardFromParent(batchId, cardFrom, cardTo, user.getId());
		if(code.isSuccess()) {
			ElecCardExtra extra = daoService.getObject(ElecCardExtra.class, batchId);
			elecCardService.updateBatchExtra(extra);
			ElecCardExtra parentExtra = daoService.getObject(ElecCardExtra.class, extra.getPid());
			elecCardService.updateBatchExtra(parentExtra);
			return showJsonSuccess(model, code.getRetval()+"");
		}
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/eticket/manu/returnBatchCard.xhtml")
	public String returnBatchCard(Long batchId, String cardFrom, String cardTo, ModelMap model){
		User user = getLogonUser();
		ErrorCode<String> code = elecCardService.returnElecCard(batchId, cardFrom, cardTo, user.getId());
		if(code.isSuccess()) return showJsonSuccess(model, code.getRetval());
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/eticket/manu/batchUnsellCard.xhtml")
	public String batchUnsellElecCard(Long batchId, String cardFrom, String cardTo, ModelMap model){
		User user = getLogonUser();
		ErrorCode<String> code = elecCardService.unsellElecCard(batchId, cardFrom, cardTo, user.getId());
		if(code.isSuccess()) return showJsonSuccess(model, code.getRetval()+"");
		return showJsonError(model, code.getMsg());
	}
	@RequestMapping("/admin/eticket/manu/soldElecBatch.xhtml")
	public String soldElecBatch(Long batchId, ModelMap model){
		User user = getLogonUser();
		ErrorCode<String> code = elecCardService.soldElecBatch(batchId, user.getId());
		if(code.isSuccess()) return showJsonSuccess(model, code.getRetval()+"");
		return showJsonError(model, code.getMsg());
	}
}
