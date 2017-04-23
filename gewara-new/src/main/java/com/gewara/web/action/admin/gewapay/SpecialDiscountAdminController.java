package com.gewara.web.action.admin.gewapay;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.bank.BankConstant;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ManageConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.ConfigTag;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.discount.DramaSpecialDiscountHelper;
import com.gewara.helper.discount.GoodsSpecialDiscountHelper;
import com.gewara.helper.discount.GymSpecialDiscountHelper;
import com.gewara.helper.discount.MovieSpecialDiscountHelper;
import com.gewara.helper.discount.SpecialDiscountHelper;
import com.gewara.helper.discount.SportSpecialDiscountHelper;
import com.gewara.helper.sys.CachedScript.ScriptResult;
import com.gewara.json.pay.Scalper;
import com.gewara.model.acl.User;
import com.gewara.model.api.ApiUser;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.drama.Theatre;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.Cpcounter;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.SpecialDiscountExtra;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.pay.PayValidHelper;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.gewapay.ScalperService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.untrans.monitor.ConfigCenter;
import com.gewara.untrans.ticket.SpecialDiscountService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.gym.CardItem;

@Controller
public class SpecialDiscountAdminController extends BaseAdminController{
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;

	@Autowired@Qualifier("dramaOrderService")
	private DramaOrderService dramaOrderService;
	
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	
	@Autowired@Qualifier("sportOrderService")
	private SportOrderService sportOrderService;
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	
	@Autowired@Qualifier("scalperService")
	private ScalperService scalperService;
	
	@Autowired@Qualifier("configCenter")
	private ConfigCenter configCenter;

	@Autowired @Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	
	@Autowired@Qualifier("specialDiscountService")
	private SpecialDiscountService specialDiscountService;
	
	@RequestMapping("/admin/gewapay/spdiscount/spdiscountList.xhtml")
	public String spdiscountList(ModelMap model, String status){
		List<SpecialDiscount> spdiscountList = null;
		Timestamp cur = DateUtil.getCurTruncTimestamp();
		if(StringUtils.equals("all", status)){
			spdiscountList = hibernateTemplate.find("from SpecialDiscount order by sortnum desc");
		}else if(StringUtils.equals("timeout", status)) {
			spdiscountList = hibernateTemplate.find("from SpecialDiscount where timeto < ? order by sortnum desc", cur);
		}else{
			spdiscountList = hibernateTemplate.find("from SpecialDiscount where timeto >= ? order by sortnum desc", cur);
		}
		model.put("spdiscountList", spdiscountList);
		Map<Long, Spcounter> spcounterMap = daoService.getObjectMap(Spcounter.class, BeanUtil.getBeanPropertyList(spdiscountList, Long.class, "spcounterid", true));
		Map<Long, String> spidMap = new HashMap<Long, String>();
		
		for(SpecialDiscount sd:spdiscountList){
			spidMap.put(sd.getId(), PKCoderUtil.encryptString(""+sd.getId(), SpecialDiscount.ENCODE_KEY));
		}
		model.put("spidMap", spidMap);
		model.put("spcounterMap", spcounterMap);
		return "admin/gewapay/spdiscount/spdiscountList.vm";
	}
	@RequestMapping("/admin/gewapay/spdiscount/modifySpdiscount.xhtml")
	public String modifySpdiscount(ModelMap model, Long discountId, Long copyFrom){
		if(discountId!=null){
			SpecialDiscount spdiscount = daoService.getObject(SpecialDiscount.class, discountId);
			model.put("spdiscount", spdiscount);
			model.put("copy", false);
		}else if(copyFrom !=null ){
			SpecialDiscount source = daoService.getObject(SpecialDiscount.class, copyFrom);
			SpecialDiscount copy = new SpecialDiscount();
			try {
				BeanUtils.copyProperties(copy, source);
			} catch (Exception e) {
			}
			copy.setId(null);
			copy.setFlag("copy" + source.getFlag());
			copy.setSpcounterid(null);
			model.put("spdiscount", copy);
			model.put("copy", true);
			model.put("origdid", copyFrom);
		}
		String hql = "from ApiUser a where a.status=? order by id";
		List<ApiUser> apiUserList = hibernateTemplate.find(hql, ApiUser.STATUS_OPEN);
		model.put("paytextMap", PaymethodConstant.getPayTextMap());
		model.put("paybankMap", BankConstant.getPnrBankMap());
		model.put("apiUserList", apiUserList);
		return "admin/gewapay/spdiscount/spdiscount.vm";
	}
	@RequestMapping("/admin/gewapay/spdiscount/spdiscountExtra.xhtml")
	public String spdiscountExtra(ModelMap model, Long discountId){
		SpecialDiscount spdiscount = daoService.getObject(SpecialDiscount.class, discountId);
		SpecialDiscountExtra extra = daoService.getObject(SpecialDiscountExtra.class, discountId);
		model.put("spdiscount", spdiscount);
		model.put("extra", extra);
		model.put("paytextMap", PaymethodConstant.getPayTextMap());
		model.put("paybankMap", BankConstant.getPnrBankMap());
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		model.put("deptMap", ManageConstant.deptMap);
		model.put("applyMap", ManageConstant.applyMap);
		return "admin/gewapay/spdiscount/spdiscountExtra.vm";
	}
	
	@RequestMapping("/admin/gewapay/spdiscount/saveSpdiscountExtra.xhtml")
	public String spdiscountExtra(ModelMap model, Long id, HttpServletRequest request){
		SpecialDiscountExtra extra = daoService.getObject(SpecialDiscountExtra.class, id);
		if(extra==null){
			extra = new SpecialDiscountExtra(id);
		}
		BindUtils.bindData(extra, request.getParameterMap());
		daoService.saveObject(extra);
		SpecialDiscount spdiscount = daoService.getObject(SpecialDiscount.class, id);
		spdiscount.setUpdatetime(DateUtil.getMillTimestamp());
		spdiscount.setExtraInfo(Status.Y);
		daoService.saveObject(spdiscount);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/gewapay/spdiscount/spcounterList.xhtml")
	public String spcounterList(ModelMap model, String status){
		List<Spcounter> spcounterList = null;
		Timestamp cur = DateUtil.getCurTruncTimestamp();
		if(StringUtils.equals("all", status)){
			//TODO:分页？？
			spcounterList = this.daoService.getObjectList(Spcounter.class, "id", false, 0, 1000);
		}else if(StringUtils.equals("timeout", status)) {
			spcounterList = hibernateTemplate.find("from Spcounter c where exists(select s.id from SpecialDiscount s where s.spcounterid = c.id and timeto < ?) order by id desc", cur);
		}else{
			spcounterList = hibernateTemplate.find("from Spcounter c where not exists(select s.id from SpecialDiscount s where s.spcounterid = c.id) or exists(select s.id from SpecialDiscount s where s.spcounterid = c.id and timeto >= ?) order by id desc", cur);
		}
		model.put("spcounterList", spcounterList);
		Map<Long,String> sdMap = new HashMap<Long,String>();
		for (Spcounter sp : spcounterList) {
			List<String> sdList = getSpflagList(sp.getId());
			sdMap.put(sp.getId(), StringUtils.join(sdList,","));
		}
		model.put("sdMap", sdMap);
		return "admin/gewapay/spdiscount/spcounterList.vm";
	}
	private List<String> getSpflagList(Long spcounterid){
		DetachedCriteria query = DetachedCriteria.forClass(SpecialDiscount.class);
		query.add(Restrictions.eq("spcounterid", spcounterid));
		query.setProjection(Projections.property("flag"));
		List<String> sdList = this.hibernateTemplate.findByCriteria(query);
		return sdList;
	}
	@RequestMapping("/admin/gewapay/spdiscount/getSpcounter.xhtml")
	public String modifySpcounter(ModelMap model, Long spid){
		Spcounter sp = this.daoService.getObject(Spcounter.class, spid);
		if(sp != null){
			List<String> sdList = getSpflagList(sp.getId());
			model.put("sdList", StringUtils.join(sdList,","));
			model.put("sp", sp);
			model.put("periodDay", sp.getPeriodMinute()/1440);
		}
		return "admin/gewapay/spdiscount/spcounter.vm";
	}
	@RequestMapping("/admin/gewapay/spdiscount/modifySpcounter.xhtml")
	public String saveSpcounter(ModelMap model, Long id, Integer periodDay, String ctltype, HttpServletRequest request){
		Spcounter spcounter = daoService.getObject(Spcounter.class, id);
		if(spcounter == null){
			spcounter = new Spcounter(ctltype);
		}
		ChangeEntry changeEntry = new ChangeEntry(spcounter);
		Map<String,String> dataMap = WebUtils.getRequestMap(request);
		BindUtils.bindData(spcounter, dataMap);
		if(spcounter.getLimitmaxnum() == null) return showJsonError(model, "最大名额数量不能为空！");
		if(spcounter.getAllowaddnum() == null) return showJsonError(model, "最大下单数量不能为空！");
		if(spcounter.getBasenum() == null || spcounter.getBasenum()<1) return showJsonError(model, "基础数据错误！");
		if(spcounter.getAllquantity() == null) spcounter.setAllquantity(0);
		if(spcounter.getAllordernum() == null) spcounter.setAllordernum(0);
		if(spcounter.getPeriodtime() == null ) return showJsonError(model, "周期开始时间不能为空！");
		if(periodDay == null || periodDay<0) {
			return showJsonError(model, "周期时长错误！");
		}
		spcounter.setPeriodMinute(periodDay * 1440);
		daoService.saveObject(spcounter);
		monitorService.saveChangeLog(getLogonUser().getId(), Spcounter.class, spcounter.getId(),changeEntry.getChangeMap(spcounter));
		return showJsonSuccess(model,spcounter.getId()+"");
	}
	
	@RequestMapping("/admin/gewapay/spdiscount/resetSpcounter.xhtml")
	public String resetSpcounter(Long spid, ModelMap model){
		User user = getLogonUser();
		Spcounter spcounter = daoService.getObject(Spcounter.class, spid);
		if(spcounter == null) return showJsonError_NOT_FOUND(model);
		paymentService.resetSpcounter(spcounter, user.getId());
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/gewapay/spdiscount/cpcounterList.xhtml")
	public String cpcounterList(Long spid, ModelMap model){
		Spcounter spcounter = daoService.getObject(Spcounter.class, spid);
		if(spid != null){
			List<Cpcounter> cpcounterList = daoService.getObjectListByField(Cpcounter.class, "spcounterid", spid);
			Collections.sort(cpcounterList, new PropertyComparator("id", false, true));
			model.put("cpcounterList", cpcounterList);
		}
		model.put("spcounter", spcounter);
		return "admin/gewapay/spdiscount/cpcounterList.vm";
	}
	
	@RequestMapping("/admin/gewapay/spdiscount/getCpcounter.xhtml")
	public String getCpcounter(Long id, Long spcounterid, ModelMap model){
		Cpcounter cpcounter = daoService.getObject(Cpcounter.class, id);
		model.put("cpcounter", cpcounter);
		model.put("spcounterid", spcounterid);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		return "admin/gewapay/spdiscount/cpcounter.vm";
	}
	
	@RequestMapping("/admin/gewapay/spdiscount/updateCpcounter.xhtml")
	public String updateCpcounter(Long id, Long spcounterid, String flag, String cpcode, Integer basenum, HttpServletRequest request, ModelMap model){
		Cpcounter cpcounter = daoService.getObject(Cpcounter.class, id);
		Spcounter spcounter = daoService.getObject(Spcounter.class, spcounterid);
		if(spcounter == null) return showJsonError(model, "计数器不存在或被删除！");
		if(cpcounter == null){
			cpcounter = new Cpcounter(spcounterid, flag, cpcode, basenum);
		}else{
			cpcounter.setUpdatetime(DateUtil.getCurFullTimestamp());
		}
		ChangeEntry changeEntry = new ChangeEntry(cpcounter);
		Map<String, String> dataMap = WebUtils.getRequestMap(request);
		BindUtils.bindData(cpcounter, dataMap);
		if(StringUtils.isBlank(cpcounter.getFlag())) return showJsonError(model, "计数器类型不能为空！");
		if(StringUtils.isBlank(cpcode)) return showJsonError(model, "城市/商家代码不能为空！");
		if(cpcounter.getBasenum() == null) return showJsonError(model, "基础数据不能为空！");
		if(cpcounter.getAllownum() == null) return showJsonError(model, "下单总数不能为空！");
		if(cpcounter.getSellorder() == null) return showJsonError(model, "卖出下单数不能为空！");
		if(cpcounter.getSellquantity() == null) return showJsonError(model, "卖出数不能为空！");
		daoService.saveObject(cpcounter);
		monitorService.saveChangeLog(getLogonUser().getId(), Cpcounter.class, cpcounter.getId(), changeEntry.getChangeMap(cpcounter));
		return showJsonSuccess(model, spcounter.getId() + "");
	}
	@RequestMapping("/admin/gewapay/spdiscount/deleteCpcouter.xhtml")
	public String delCpcounter(Long id, ModelMap model){
		Cpcounter cpcounter = daoService.getObject(Cpcounter.class, id);
		if(cpcounter == null) return showJsonError_NOT_FOUND(model);
		daoService.removeObject(cpcounter);
		monitorService.saveDelLog(getLogonUser().getId(), id, cpcounter);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/gewapay/spdiscount/expression.xhtml")
	public String expression(ModelMap model, String tradeno){
		if(StringUtils.isNotBlank(tradeno)){
			GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno, true);
			if(order == null) return showError(model, "订单不存在！");
			model.put("order", order);
			if(order instanceof TicketOrder){
				OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", ((TicketOrder) order).getMpid(), true);
				model.put("opi", opi);
			}
		}
		
		return "admin/gewapay/spdiscount/expression.vm";
	}
	
	@RequestMapping("/admin/gewapay/spdiscount/specialRule.xhtml")
	public String specialRule(Long discountId, HttpServletRequest request, ModelMap model){
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, discountId);
		if(sd == null) return showMessageAndReturn(model, request, "该数据不存在或被删除！");
		model.put("sd", sd);
		return "admin/gewapay/spdiscount/specialRule.vm";
	}
	
	@RequestMapping("/admin/gewapay/spdiscount/saveSpecialRule.xhtml")
	public String saveSpecialRule(Long id, String specialrule, ModelMap model){
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, id);
		if(sd == null) return showJsonError(model, "该数据不存在或被删除！");
		ChangeEntry changeEntry = new ChangeEntry(sd);
		sd.setSpecialrule(specialrule);
		sd.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(sd);
		monitorService.saveChangeLog(getLogonUser().getId(), SpecialDiscount.class, sd.getId(), changeEntry.getChangeMap(sd));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/gewapay/spdiscount/testExpression.xhtml")
	public String testExpression(HttpServletRequest request, Long mpid, String expression, ModelMap model){
		ScriptResult<Integer> result = null;
		if(mpid!=null){
			TicketOrder order = new TicketOrder(0L);
			OpenPlayItem opi = new OpenPlayItem();
			Map<String, String> requestParam = WebUtils.getRequestMap(request);
			BindUtils.bindData(order, requestParam);
			BindUtils.bindData(opi, requestParam);
			
			result = MovieSpecialDiscountHelper.compute(order, opi, expression, false);
		}else{
			return showJsonError(model, "此订单暂不支持公式！"); 
		}
		if(result.hasError()){
			return showJsonError(model, "公式错误：" + result.getErrorMsg());
		}else{
			return showJsonSuccess(model, "" +  result.getRetval());
		}
	}

	
	@RequestMapping("/admin/gewapay/spdiscount/saveSpdiscount.xhtml")
	public String saveSpdiscount(HttpServletRequest request, String flag, String tag, Long discountId, 
			Long bindgoods, Integer bindnum, Long spcounterid, Long origdid, ModelMap model){
		if(bindgoods != null){
			if(bindnum == null) return showJsonError(model, "设置了“绑定套餐”，“使用数量”也必须设置！");
			Goods goods = daoService.getObject(Goods.class, bindgoods);
			if(goods==null) return showJsonError(model, "绑定的套餐不存在！");
		}
		if(spcounterid == null) return showJsonError(model, "记数器不存在！");
		SpecialDiscount spdiscount = null;
		User user = getLogonUser();
		ChangeEntry changeEntry = null;
		if(discountId!=null){
			spdiscount = daoService.getObject(SpecialDiscount.class, discountId);
			if(spdiscount.getSpcounterid() == null){
				Spcounter counter = daoService.getObject(Spcounter.class, spcounterid);
				if(counter==null){
					return showJsonError(model, "记数器不存在！");
				}
			}else if(!spcounterid.equals(spdiscount.getSpcounterid())){
				return showJsonError(model, "不可以更改计数器，请联系系统管理员！");
			}
			spdiscount.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			changeEntry = new ChangeEntry(spdiscount);
		}else{
			spdiscount = new SpecialDiscount(flag, tag);
			Spcounter counter = daoService.getObject(Spcounter.class, spcounterid);
			if(counter==null){
				return showJsonError(model, "记数器不存在！");
			}
		}
		BindUtils.bindData(spdiscount, request.getParameterMap());
		ErrorCode<String> validateSpdisCountCode = validate(spdiscount);
		if (!validateSpdisCountCode.isSuccess()) {
			return showJsonError(model, validateSpdisCountCode.getRetval());
		}
		//更改成本价
		if(StringUtils.isNotBlank(spdiscount.getCosttype())){
			if(spdiscount.getCostnum()==null) {
				return showJsonError(model, "“更改成本价”设置错误！");
			}
			//只能为特定的优惠方式
			if(!Arrays.asList("uprice", "order").contains(spdiscount.getDistype())){
				return showJsonError(model, "设置了“更改成本价”，优惠方式只能是“按单价折扣”、“按订单折扣”！");
			}
		}else if(spdiscount.getCostnum()!=null){
			return showJsonError(model, "“更改成本价”设置错误！");
		}
		//版本校验
		if(StringUtils.isNotBlank(spdiscount.getEdition())){
			List<String> edtionList = Arrays.asList(StringUtils.split(spdiscount.getEdition(), ","));
			for (String edition : edtionList) {
				if(!OpiConstant.isValidEdition(edition)) return showJsonError(model, "版本设置不对！");
			}
		}
		if(spdiscount.getDistype().equals(SpecialDiscount.DISCOUNT_TYPE_EXPRESSION) && StringUtils.isBlank(spdiscount.getExpression())){
			return showJsonError(model, "公式不能为空！"); 
		}
		daoService.saveObject(spdiscount);
		if(origdid!= null){
			SpecialDiscountExtra extra = daoService.getObject(SpecialDiscountExtra.class, discountId);
			if(extra==null){
				SpecialDiscountExtra origextra = daoService.getObject(SpecialDiscountExtra.class, origdid);
				try {
					extra = new SpecialDiscountExtra();
					BeanUtils.copyProperties(extra, origextra);
					extra.setId(spdiscount.getId());
					daoService.saveObject(extra);
				} catch (Exception e) {
				}
			}
		}
		if(discountId!=null){
			monitorService.saveChangeLog(user.getId(), SpecialDiscount.class, spdiscount.getId(),changeEntry.getChangeMap(spdiscount));
		}else{
			monitorService.saveAddLog(user.getId(), SpecialDiscount.class, spdiscount.getId(), spdiscount);
		}
		model.put("spdiscount", spdiscount);
		return showJsonSuccess(model, ""+spdiscount.getId());
	}
	@RequestMapping("/admin/gewapay/spdiscount/refreshSellnum.xhtml")
	public String refreshSpdiscountSellnum(Long sid, String uptype, ModelMap model){
		String sql = "select sum(t.quantity) as 影票数量 from WEBDATA.discount_item d, WEBDATA.ticket_order t " +
			"where d.orderid=t.recordid and t.status=? and t.addtime>=? and t.addtime<=? " +
			"and d.relatedid=? and d.tag=? and d.cardtype=? group by order_type";
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, sid);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		List<Map<String, Object>> qryMapList = jdbcTemplate.queryForList(sql, OrderConstant.STATUS_PAID_SUCCESS, sd.getCreatetime(), cur, 
				sid, PayConstant.DISCOUNT_TAG_PARTNER, PayConstant.CARDTYPE_PARTNER);
		int count = 0;
		if(qryMapList.size()>0) count = Integer.parseInt(""+qryMapList.get(0).get("影票数量"));
		if(count < sd.getSellcount()) return showJsonError(model, "卖出数量统计有问题，请联系系统管理员，数量：" + count);
		if(StringUtils.equals(uptype, "update") && count > sd.getSellcount()){
			sd.setSellcount(count);
			daoService.saveObject(sd);
		}
		return showJsonSuccess(model, ""+count);
	}
	@RequestMapping("/admin/gewapay/spdiscount/processSpdiscount.xhtml")
	public String processSpdiscount(Long sid, String citycode, ModelMap model){
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, sid);
		String sql = "select t.trade_no as 订单号, t.mobile as 手机号, t.membername as 用户名, t.memberid as 用户ID, t.addtime as 下单时间, t.amount as 订单金额, " +
				"t.quantity as 影票数量, (t.gewapaid) as 余额支付,(t.alipaid) as 网络支付, t.discount as 优惠金额 ,t.paymethod as 支付方式, t.otherinfo " +
				"from WEBDATA.discount_item d, WEBDATA.ticket_order t " +
				"where d.orderid=t.recordid and t.status=? and t.addtime>=? and t.addtime<=? ";
				if(StringUtils.isNotBlank(citycode)) sql = sql + " and citycode='" + citycode + "'";
				sql = sql + "and d.relatedid=? and d.tag=? and d.cardtype=? order by t.recordid";
		List<Map<String, Object>> qryMapList = jdbcTemplate.queryForList(sql, OrderConstant.STATUS_PAID_SUCCESS, sd.getTimefrom(), sd.getTimeto(), 
				sid, PayConstant.DISCOUNT_TAG_PARTNER, PayConstant.CARDTYPE_PARTNER);
		if(sd.getRebates() > 0 && StringUtils.equals(sd.getRebatestype(), SpecialDiscount.REBATES_CASH)){
			String flag = "rebates" + sd.getId();
			String qry = "select payseqno from Charge where paymethod=? and paybank = ? and status = ? and updatetime>= ? ";
			List<String> rebatesList = hibernateTemplate.find(qry, PaymethodConstant.PAYMETHOD_SYSPAY, flag, Charge.STATUS_PAID, sd.getTimefrom());
			model.put("rebatesList", rebatesList);
			Map<String, String> checkMap = new HashMap<String, String>();
			for(int i=0, max=Math.min(qryMapList.size(), sd.getRebatesmax()); i<max; i++ ){
				String tradeNo = (String) qryMapList.get(i).get("订单号");
				checkMap.put(tradeNo, StringUtil.md5(tradeNo + "dkeudke"));
			}
			for(String tradeNo: rebatesList){
				if(!checkMap.containsKey(tradeNo)) checkMap.put(tradeNo, StringUtil.md5(tradeNo + "dkeudke"));
			}
			model.put("checkMap", checkMap);
		}else if(sd.getDrawactivity() != null && SpecialDiscount.REBATES_CARDD.equals(sd.getRebatestype())){
			String qry = "select tradeNo from GewaOrder where otherinfo like ? and status = ?";
			List<String> rebatesList = hibernateTemplate.find(qry, "%rebatesCard" + sd.getId() + "%",OrderConstant.STATUS_PAID_SUCCESS);
			model.put("rebatesList", rebatesList);
			Map<String, String> checkMap = new HashMap<String, String>();
			for(int i=0, max=Math.min(qryMapList.size(), sd.getRebatesmax()); i<max; i++ ){
				String tradeNo = (String) qryMapList.get(i).get("订单号");
				checkMap.put(tradeNo, StringUtil.md5(tradeNo + "dkeudke"));
			}
			for(String tradeNo: rebatesList){
				if(!checkMap.containsKey(tradeNo)) checkMap.put(tradeNo, StringUtil.md5(tradeNo + "dkeudke"));
			}
			model.put("checkMap", checkMap);
		}
		model.put("qryMapList", qryMapList);
		model.put("paytextMap", PaymethodConstant.getPayTextMap());
		model.put("sd", sd);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		return "admin/gewapay/spdiscount/processSpdiscount.vm";
	}
	@RequestMapping("/admin/gewapay/spdiscount/failOrderList.xhtml")
	public String spFailOrderList(Long sid, ModelMap model){
		//TODO:效率太底，重写
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, sid);
		String qry = "from GewaOrder g where g.status=? and exists(select d.id from Discount d where d.orderid=g.id and d.relatedid=?) order by g.addtime desc";
		List<GewaOrder> orderList = hibernateTemplate.find(qry, OrderConstant.STATUS_PAID_FAILURE, sid);
		model.put("orderList", orderList);
		model.put("sd", sd);
		return "admin/gewapay/spdiscount/failOrderList.vm";
	}	
	@RequestMapping("/admin/gewapay/spdiscount/toggleRebates.xhtml")
	public String toggleRebates(Long sid, String tradeNo, String optype, String check, String supplement, ModelMap model){
		if(sid==null || StringUtils.isBlank(tradeNo) || StringUtils.isBlank(optype)) return showJsonError(model, "数据错误！");
		String mycheck = StringUtil.md5(tradeNo + "dkeudke");
		User user = getLogonUser();
		
		if(StringUtils.equals(mycheck, check)){
			SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, sid);
			GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
			ErrorCode code = null;
			if(StringUtils.equals(optype, "add")){
				boolean isSupplement = StringUtils.isNotBlank(supplement);
				if(SpecialDiscount.REBATES_CARDD.equals(sd.getRebatestype())){
					code = paymentService.addSpdiscountCard(order, sd, user.getId(), isSupplement);
				}else{
					code = paymentService.addSpdiscountCharge(order, sd, user.getId(), isSupplement);
				}
			}else{
				code = paymentService.removeSpdiscountCharge(order, sd, user.getId());
			}
			if(code.isSuccess()) return showJsonSuccess(model);
			return showJsonError(model, code.getMsg());
		}else{
			return showJsonError(model, "校验错误！");
		}
	}
	@RequestMapping("/admin/gewapay/spdiscount/checkSpdicount.xhtml")
	public String checkSpdicount(String tradeno, Long discountId, ModelMap model){
		if(StringUtils.isBlank(tradeno)) return showJsonError(model, "订单号不能为空！");
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno, true);
		if(order == null) return showJsonError(model, "订单不存在！");
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, discountId);
		if(sd == null) return showJsonError(model, "特价折扣不存在！");
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		SpecialDiscountHelper sdh = null;
		PayValidHelper pvh = null;
		if(order instanceof TicketOrder){
			TicketOrder tOrder = (TicketOrder)order;
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", tOrder.getMpid(), true);
			List<SellSeat> seatList = ticketOrderService.getOrderSeatList(tOrder.getId());
			sdh = new MovieSpecialDiscountHelper(opi, tOrder, seatList, discountList);
			pvh = new PayValidHelper(VmUtils.readJsonToMap(opi.getOtherinfo()));
		}else if(order instanceof DramaOrder){
			DramaOrder dOrder = (DramaOrder)order;
			OpenDramaItem item = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dOrder.getDpid(), false);
			List<SellDramaSeat> seatList = new ArrayList<SellDramaSeat>();
			if(item.isOpenseat()) seatList = dramaOrderService.getDramaOrderSeatList(order.getId());
			List<BuyItem> buyList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
			List<OpenDramaItem> itemList = dramaOrderService.getOpenDramaItemList(item, buyList);
			sdh = new DramaSpecialDiscountHelper(dOrder, itemList, buyList, discountList, seatList);
			pvh = new PayValidHelper(VmUtils.readJsonToMap(item.getOtherinfo()));
		}else if(order instanceof SportOrder){
			SportOrder sOrder = (SportOrder)order;
			OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, sOrder.getOttid());
			List<OpenTimeItem> otiList = sportOrderService.getMyOtiList(order.getId());
			sdh = new SportSpecialDiscountHelper(sOrder, ott, discountList, otiList);
			pvh = new PayValidHelper(VmUtils.readJsonToMap(ott.getOtherinfo()));
		}else if(order instanceof GymOrder){
			GymOrder gOrder = (GymOrder)order;
			ErrorCode<CardItem> code = synchGymService.getGymCardItem(gOrder.getGci(), true);
			if(code.isSuccess()){
				CardItem item = code.getRetval();
				sdh = new GymSpecialDiscountHelper(gOrder, item, discountList);
				pvh = new PayValidHelper(VmUtils.readJsonToMap(item.getOtherinfo()));
			}
		}else if(order instanceof GoodsOrder){
			GoodsOrder gOrder = (GoodsOrder)order;
			BaseGoods goods = daoService.getObject(BaseGoods.class, gOrder.getGoodsid());
			List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
			List<Long>	idList = BeanUtil.getBeanPropertyList(itemList, "relatedid", true);
			List<BaseGoods> goodsList = daoService.getObjectList(BaseGoods.class, idList);
			if(!goodsList.contains(goods)) goodsList.add(goods);
			sdh = new GoodsSpecialDiscountHelper(gOrder, goodsList, itemList);
			pvh = new PayValidHelper();
		}
		if(sdh == null || pvh == null) return showJsonError(model, "该订单不支持特价折扣！");
		List<String> limitPayList = paymentService.getLimitPayList();
		pvh.setLimitPay(limitPayList);
		Spcounter spcounter = paymentService.getSpdiscountCounter(sd);
		List<Cpcounter> cpcounterList = new ArrayList<Cpcounter>();
		if(spcounter != null){
			cpcounterList = daoService.getObjectListByField(Cpcounter.class, "spcounterid", spcounter.getId());
		}
		sdh.setShowMsg(true);
		String disable = sdh.getFullDisabledReason(sd, spcounter, cpcounterList);
		ErrorCode<Integer> code = sdh.validSpdiscountWithoutStatus(sd, spcounter, cpcounterList, pvh);
		return showJsonError(model, code.isSuccess()?"可用:优惠" + code.getRetval():"不可用：" + disable + code.getMsg());
	}

	@RequestMapping("/admin/gewapay/spdiscount/spotherOrderList.xhtml")
	public String spotherOrderList(Long sid, ModelMap model, String removeOther, String matchBank){
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, sid);
		if(sd.getRebates() > 0 && StringUtils.equals(sd.getRebatestype(), SpecialDiscount.REBATES_CASH)){
			DetachedCriteria query = DetachedCriteria.forClass(TicketOrder.class, "o");
			query.add(Restrictions.eq("status", OrderConstant.STATUS_PAID_SUCCESS));
			query.add(Restrictions.ge("addtime", sd.getTimefrom()));
			query.add(Restrictions.lt("addtime", sd.getTimeto()));
			String[] pay = StringUtils.split(sd.getPaymethod(), ":");
			if(pay.length >0) query.add(Restrictions.eq("paymethod", pay[0]));
			if(pay.length >1 && StringUtils.equals("true", matchBank)) query.add(Restrictions.eq("paybank", pay[1]));
			query.add(Restrictions.or(Restrictions.isNull("otherinfo"), Restrictions.or(
					Restrictions.not(Restrictions.like("otherinfo", "rebates", MatchMode.ANYWHERE)),
					Restrictions.and(Restrictions.like("otherinfo", "supplement", MatchMode.ANYWHERE), 
							Restrictions.like("otherinfo", "rebates" + sid, MatchMode.ANYWHERE)))));
			if(StringUtils.isNotBlank(removeOther)){
				query.add(Restrictions.eq("discount", 0));
				DetachedCriteria sub = DetachedCriteria.forClass(Discount.class, "d");
				sub.add(Restrictions.eqProperty("o.id", "d.orderid"));
				sub.setProjection(Projections.id());
				query.add(Subqueries.notExists(sub));
			}
			query.addOrder(Order.asc("addtime"));
			List<TicketOrder> orderList = hibernateTemplate.findByCriteria(query);

			String flag = "rebates" + sd.getId();
			String qry = "select payseqno from Charge where paymethod=? and paybank = ? and status = ? and updatetime>= ? ";
			List<String> rebatesList = hibernateTemplate.find(qry, PaymethodConstant.PAYMETHOD_SYSPAY, flag, Charge.STATUS_PAID, sd.getTimefrom());
			Map<String, String> checkMap = new HashMap<String, String>();
			Map<Long, String> disableReasonMap = new HashMap<Long, String>();
			Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
			for(TicketOrder order: orderList){
				OpenPlayItem opi = opiMap.get(order.getMpid());
				if(opi==null){
					opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
					opiMap.put(order.getMpid(), opi);
				}
				Spcounter spcounter = paymentService.getSpdiscountCounter(sd);
				List<Cpcounter> cpcounterList = new ArrayList<Cpcounter>();
				if(spcounter != null){
					cpcounterList = daoService.getObjectListByField(Cpcounter.class, "spcounterid", spcounter.getId());
				}
				String fullReason = MovieSpecialDiscountHelper.getFullDisabledReason(spcounter, cpcounterList, sd, opi, order);
				disableReasonMap.put(order.getId(), fullReason);
				checkMap.put(order.getTradeNo(), StringUtil.md5(order.getTradeNo() + "dkeudke"));
			}
			for(String tradeNo: rebatesList){
				if(!checkMap.containsKey(tradeNo)) checkMap.put(tradeNo, StringUtil.md5(tradeNo + "dkeudke"));
			}
			model.put("rebatesList", rebatesList);
			model.put("checkMap", checkMap);
			model.put("sd", sd);
			model.put("orderList", orderList);
			model.put("disableReasonMap", disableReasonMap);
			model.put("paytextMap", PaymethodConstant.getPayTextMap());
		}

		return "admin/gewapay/spdiscount/otherOrderList.vm";
	}	
	
	
	//TODO:移到订票规则设置
	@RequestMapping("/admin/gewapay/scalperList.xhtml")
	public String scalperList(ModelMap model){
		List<Scalper> scalperList = mongoService.getObjectList(Scalper.class);
		model.put("scalperList", scalperList);
		return "admin/gewapay/scalperList.vm";
	}
	@RequestMapping("/admin/gewapay/toEditScalper.xhtml")
	public String toEditScalper(Long scalperId, ModelMap model){
		Scalper scalper = mongoService.getObject(Scalper.class, "id", scalperId);
		if(scalper != null){
			model.put("scalper", scalper);
		}
		return "admin/gewapay/editScalper.vm";
	}
	
	@RequestMapping("/admin/gewapay/saveScalper.xhtml")
	public String saveScalper(Long id, String name, String description, String mobiles, ModelMap model){
		if(StringUtils.isBlank(name)){
			return showJsonError(model, "名称为空");
		}
		Scalper scalper = mongoService.getObject(Scalper.class, "id", id);
		if(scalper == null){
			scalper = new Scalper();
			scalper.setId(System.currentTimeMillis());
		}
		scalper.setName(name);
		scalper.setDescription(description);
		scalper.setMobiles(mobiles);
		
		mongoService.saveOrUpdateObject(scalper, "id");
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_SCALPER);
		
		return showJsonSuccess(model, scalper.getId() + "");
	}
	
	@RequestMapping("/admin/gewapay/suspectList.xhtml")
	public String getSuspectScalper(Integer hours, Integer count, ModelMap model){
		if(hours == null){
			hours = -12;
		}
		if(count == null){
			count = 10;
		}
		//hours = -12*365 * 2;
		model.put("suspectMap", this.scalperService.getSuspectScalperByIp(hours, count));
		return "admin/gewapay/suspectScalperList.vm";
	}
	
	@RequestMapping("/admin/gewapay/addScalper.xhtml")
	public String addScalper(String memberids, ModelMap model){

		if(StringUtils.isBlank(memberids)){
			return showJsonError(model, "请选择用户");
		}
		Scalper scalper = new Scalper();
		long id = System.currentTimeMillis();
		scalper.setId(id);
		scalper.setName("批量黄牛" + id);
		scalper.setDescription("批量添加");
		scalper.setMobiles(memberids);
		
		mongoService.saveOrUpdateObject(scalper, "id");
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_SCALPER);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/gewapay/delScalper.xhtml")
	public String delScalper(String scalperId, ModelMap model){
		if(StringUtils.isBlank(scalperId)){
			return showJsonError(model, "不能删除id为空的数据");
		}
		mongoService.removeObjectById(Scalper.class, "id", Long.valueOf(scalperId));
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_SCALPER);
		return showJsonSuccess(model);
	}
	/**
	 * 特价活动订单展示后台redmine #7038
	 */
	@RequestMapping("/admin/gewapay/spdiscount/orderList.xhtml")
	public String specialDisCountList(Integer pageNo, String discountIds, Date fromTime, Date endTime, HttpServletRequest request, ModelMap model) {
		model.put("discountIds", discountIds);
		model.put("fromTime", fromTime);
		model.put("endTime", endTime);
		if (StringUtils.isEmpty(discountIds)) {
			model.put("msg", "特价活动ID不能为空！");
			return "admin/gewapay/spdiscount/discountOrderList.vm";
		}
		List<Long> discountIdList = BeanUtil.getIdList(discountIds, ",");
		if (CollectionUtils.isEmpty(discountIdList)) {
			model.put("msg", "特价活动ID不能为空！");
			return "admin/gewapay/spdiscount/discountOrderList.vm";
		}
		if (fromTime == null || endTime == null) {
			model.put("msg", "查询开始日期，结束日期不能为空！");
			return "admin/gewapay/spdiscount/discountOrderList.vm";
		}
		if (DateUtil.addDay(fromTime, 60).before(endTime)) {
			model.put("msg", "查询日期间隔不能超过60天");
			return "admin/gewapay/spdiscount/discountOrderList.vm";
		}
		List<SpecialDiscount> specialDiscounts = daoService.getObjectList(SpecialDiscount.class, discountIdList);
		if (CollectionUtils.isEmpty(specialDiscounts)) {
			model.put("msg", "请输入正确的特价活动ID");
			return "admin/gewapay/spdiscount/discountOrderList.vm";
		}
		String tag = specialDiscounts.get(0).getTag();
		if (!StringUtils.equals(tag, "movie") && StringUtils.equals(tag, "sport") && StringUtils.equals(tag, "drama")) {
			model.put("msg", "请输入正确的特价活动ID");
			return "admin/gewapay/spdiscount/discountOrderList.vm";
		}
		model.put("tag", tag);
		String typeId = "movieid";
		String areaId = "cinemaid";
		Class movieClazz = Movie.class;
		Class cinemaClazz = Cinema.class;
		Class clazz = TicketOrder.class;
		if (StringUtils.equals(tag, "movie")) {
			clazz = TicketOrder.class;
			movieClazz = Movie.class;
			cinemaClazz = Cinema.class;
			typeId = "movieid";
			areaId = "cinemaid";
		} else if (StringUtils.equals(tag, "sport")) {
			clazz = SportOrder.class;
			movieClazz = SportItem.class;
			cinemaClazz = Sport.class;
			typeId = "itemid";
			areaId = "sportid";
		} else if (StringUtils.equals(tag, "drama")) {
			clazz = DramaOrder.class;
			movieClazz = Drama.class;
			cinemaClazz = Theatre.class;
			typeId = "dramaid";
			areaId = "theatreid";
		}
		
		if (pageNo == null) {
			pageNo = 0;
		}
		String maxnumStr = request.getParameter("maxnum");
		int maxnum = StringUtils.isNotBlank(maxnumStr) ? Integer.valueOf(maxnumStr) : 20;
		model.put("maxnum", String.valueOf(maxnum));
		int from = pageNo * maxnum;
		int orderCount = specialDiscountService.getOrderCountByDiscountIds(clazz, discountIdList, fromTime, endTime);
		List orderList = specialDiscountService.getOrderListByDiscountIds(clazz, discountIdList, fromTime, endTime, from, maxnum);
		
		PageUtil pageUtil = new PageUtil(orderCount, maxnum, pageNo, "admin/gewapay/spdiscount/orderList.xhtml", true, true);
		pageUtil.initPageInfo(request.getParameterMap());
		model.put("pageUtil", pageUtil);
		model.put("orderList", orderList);
		
		
		List<Long> movieIdList = BeanUtil.getBeanPropertyList(orderList, typeId,true);
		List<Long> cinemaIdList = BeanUtil.getBeanPropertyList(orderList, areaId,true);
		List<Long> memberIdList = BeanUtil.getBeanPropertyList(orderList, "memberid",true);
		List movieList = new LinkedList();
		List cinemaList = new LinkedList();
		List<Member> memberList = new LinkedList<Member>();
		for (Long movieid : movieIdList) {
			movieList.add(daoService.getObjectByUkey(movieClazz, "id", movieid, true));
		}
		for (Long cinemaid : cinemaIdList) {
			cinemaList.add(daoService.getObjectByUkey(cinemaClazz, "id", cinemaid, true));
		}
		for (Long memberid : memberIdList) {
			memberList.add(daoService.getObjectByUkey(Member.class, "id", memberid, true));
		}
		Map<Long, Movie> movieMap = BeanUtil.beanListToMap(movieList, "id");
		Map<Long, Cinema> cinemaMap = BeanUtil.beanListToMap(cinemaList, "id");
		Map<Long, Member> memberMap = BeanUtil.beanListToMap(memberList, "id");
		model.put("movieMap", movieMap);
		model.put("cinemaMap", cinemaMap);
		model.put("memberMap", memberMap);
		return "admin/gewapay/spdiscount/discountOrderList.vm";
	}
	
	//光棍节添加特价活动ID
	@RequestMapping("/admin/gewapay/spdiscount/saveDiscountId.xhtml")
	public String saveDiscountId(String ids, ModelMap model){
		GewaConfig config = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_SUBJECT_DOUBLE_ELEVEN);
		if(StringUtils.isNotBlank(ids)){
			String[] idList = StringUtils.split(ids, ",");
			for (String id : idList) {
				SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, Long.parseLong(id));
				if(sd == null) return showJsonError(model, id + "该ID不是特价活动ID！");
			}
			config.setContent(ids);
			daoService.saveObject(config);
		}
		return showJsonSuccess(model, config.getContent());
	}
	
	private static final List<String> SPECIALDISCOUNT_OPENTYPELIST = new ArrayList<String>();
	static {
		SPECIALDISCOUNT_OPENTYPELIST.add(SpecialDiscount.OPENTYPE_SPECIAL);
		SPECIALDISCOUNT_OPENTYPELIST.add(SpecialDiscount.OPENTYPE_WAP);
		SPECIALDISCOUNT_OPENTYPELIST.add(SpecialDiscount.OPENTYPE_PARTNER);
		SPECIALDISCOUNT_OPENTYPELIST.add(SpecialDiscount.OPENTYPE_GEWA);
	}
	private ErrorCode<String> validate(SpecialDiscount spdiscount) {
		if (StringUtils.isBlank(spdiscount.getFlag())) {
			return ErrorCode.getFailureReturn("[标识]不能为空！");
		} else if (strLength(spdiscount.getFlag()) > 30) {
			return ErrorCode.getFailureReturn("[标识]长度不能超出30个字节！");
		}
		if (StringUtils.isBlank(spdiscount.getTag())) {
			return ErrorCode.getFailureReturn("[订单类型]不能为空！");
		} else if (strLength(spdiscount.getTag()) > 10) {
			return ErrorCode.getFailureReturn("[订单类型]长度不能超出10个字节！");
		}
		if (spdiscount.getSortnum() == null) {
			return ErrorCode.getFailureReturn("[排序]不能为空！");
		} else if(spdiscount.getSortnum() > 10000) {
			return ErrorCode.getFailureReturn("[排序]不能大于9999！");
		}
		if (StringUtils.isBlank(spdiscount.getOpentype())) {
			return ErrorCode.getFailureReturn("[开放类型]不能为空！");
		} else if (!SPECIALDISCOUNT_OPENTYPELIST.contains(spdiscount.getOpentype())) {
			return ErrorCode.getFailureReturn("[开放类型]格式不对！");
		} else if (StringUtils.equals(spdiscount.getOpentype(), SpecialDiscount.OPENTYPE_PARTNER)
				&& StringUtils.isBlank(spdiscount.getPtnids())) {
			return ErrorCode.getFailureReturn("[开放类型]设置为渠道页面，商家ID不能为空！");
		}
		
		if (StringUtils.isBlank(spdiscount.getDistype())) {
			return ErrorCode.getFailureReturn("[折扣方式]不能为空！");
		}
		
		if (spdiscount.getLimitperiod() == null) {
			return ErrorCode.getFailureReturn("[限购周期]必须是数字！");
		}
		
		if (spdiscount.getExtdiscount() == null) {
			return ErrorCode.getFailureReturn("[外部折扣金额]必须是数字！");
		} else if (spdiscount.getExtdiscount() > 999) {
			return ErrorCode.getFailureReturn("[外部折扣金额]不能超过999！");
		}
		if (spdiscount.getLimitnum() == null) {
			return ErrorCode.getFailureReturn("[限购次数]必须是数字！");
		} else if (spdiscount.getLimitnum() > 999) {
			return ErrorCode.getFailureReturn("[限购次数]不能超过999！");
		}
		if (spdiscount.getMinbuy() == null || spdiscount.getBuynum() == null) {
			return ErrorCode.getFailureReturn("[每单限购数]必须是数字！");
		}
		if (StringUtils.isBlank(spdiscount.getPeriodtype())) {
			return ErrorCode.getFailureReturn("[限购周期]不能为空！");
		}
		if (StringUtils.isBlank(spdiscount.getBankname())) {
			return ErrorCode.getFailureReturn("[银行名称]不能为空！");
		} else if (strLength(spdiscount.getBankname()) > 50) {
			return ErrorCode.getFailureReturn("[银行名称]长度不能超过50个字节！");
		}
		if (StringUtils.isBlank(spdiscount.getUniqueby())) {
			return ErrorCode.getFailureReturn("[唯一标识]不能为空！");
		} else if (strLength(spdiscount.getUniqueby()) > 15) {
			return ErrorCode.getFailureReturn("[唯一标识]长度不能超过15个字节！");
		}
		
		if (StringUtils.isBlank(spdiscount.getDescription())) {
			return ErrorCode.getFailureReturn("[简要说明]不能为空！");
		} else if (strLength(spdiscount.getDescription()) > 50) {
			return ErrorCode.getFailureReturn("[简要说明]长度不能超过50个字节！");
		}
		if (spdiscount.getRebates() == null) {
			return ErrorCode.getFailureReturn("[返利数]必须是数字！");
		} else if (spdiscount.getRebates() > 999) {
			return ErrorCode.getFailureReturn("[返利数]不能超出999！");
		}
		if (spdiscount.getRebatesmax() == null) {
			return ErrorCode.getFailureReturn("[返利名额]必须是数字！");
		} else if (spdiscount.getRebatesmax() > 99999) {
			return ErrorCode.getFailureReturn("[返利数]不能超出99999！");
		}
		if (StringUtils.isBlank(spdiscount.getRebatestype())) {
			return ErrorCode.getFailureReturn("[返利类型]不能为空！");
		} else if (StringUtils.equals(spdiscount.getRebatestype(), SpecialDiscount.REBATES_CARDD)
				&& spdiscount.getDrawactivity() == null) {
			return ErrorCode.getFailureReturn("[返利类型]为送D券时，[抽奖活动id]必须填写数字！");
		}
		if (StringUtils.isBlank(spdiscount.getChannel())) {
			return ErrorCode.getFailureReturn("[活动类型]不能为空！");
		}
		if (spdiscount.getSpcounterid() == null) {
			return ErrorCode.getFailureReturn("[计数器]必须是数字！");
		}
		if (spdiscount.getTimefrom() == null) {
			return ErrorCode.getFailureReturn("[开始时间]不能为空！");
		}
		if(spdiscount.getTimeto() == null) {
			return ErrorCode.getFailureReturn("[结束时间]不能为空！");
		}
		if (StringUtils.isBlank(spdiscount.getTime1()) || StringUtils.isBlank(spdiscount.getTime2())) {
			return ErrorCode.getFailureReturn("[场次时段]不能为空！");
		}
		if (StringUtils.isBlank(spdiscount.getAddtime1()) || StringUtils.isBlank(spdiscount.getAddtime2())) {
			return ErrorCode.getFailureReturn("[下单时段]不能为空！");
		}
		if (spdiscount.getPrice1() == null || spdiscount.getPrice2() == null) {
			return ErrorCode.getFailureReturn("[卖价范围]必须是数字！");
		}
		if (spdiscount.getPricegap() == null) {
			return ErrorCode.getFailureReturn("[成本差额]必须是数字！");
		}
		if (spdiscount.getCostprice1() == null || spdiscount.getCostprice2() == null) {
			return ErrorCode.getFailureReturn("[成本范围]必须是数字！");
		}
		if (StringUtils.isBlank(spdiscount.getCitycode())) {
			return ErrorCode.getFailureReturn("[可用城市]不能为空！");
		}
		if (spdiscount.getDiscount() == null) {
			return ErrorCode.getFailureReturn("[折扣金额]必须是数字！");
		} else if (spdiscount.getDiscount() > 999) {
			return ErrorCode.getFailureReturn("[折扣金额]不能大于999！");
		}
		if (StringUtils.equals(spdiscount.getDistype(), SpecialDiscount.DISCOUNT_TYPE_EXPRESSION)
				&& StringUtils.isBlank(spdiscount.getExpression())) {
			return ErrorCode.getFailureReturn("[折扣方式]选择为公式，[公式]不能为空！");
		}
		return ErrorCode.SUCCESS;
	}
	/**
	 * 保存到数据库做计算，一个汉字算两个字节
	 */
	private int strLength(String str) {
		if (StringUtils.isBlank(str)) {
			return 0;
		}
		int length = StringUtils.length(str);
		int byteLength = 0;
		for(int i=0; i< length; i++) { 
			byteLength++;
			if(str.charAt(i) > 128) byteLength++;
		}
		return byteLength;
	}
}
