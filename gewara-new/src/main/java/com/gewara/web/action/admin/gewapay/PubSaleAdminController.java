package com.gewara.web.action.admin.gewapay;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import com.gewara.constant.Status;
import com.gewara.model.common.VersionCtl;
import com.gewara.model.pay.PubMember;
import com.gewara.model.pay.PubSale;
import com.gewara.model.pay.PubSaleOrder;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.MessageService;
import com.gewara.service.order.PubSaleService;
import com.gewara.support.ErrorCode;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class PubSaleAdminController extends BaseAdminController{
	@Autowired @Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate template) {
		jdbcTemplate = template;
	}
	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	@Autowired@Qualifier("pubSaleService")
	private PubSaleService pubSaleService;
	public void setPubSaleService(PubSaleService pubSaleService) {
		this.pubSaleService = pubSaleService;
	}
	@RequestMapping("/admin/pubsale/saleList.xhtml")
	public String saleList(Integer pageNo, String name, String status, ModelMap model){
		if(pageNo == null) pageNo = 0;
		int rowsPages = 20;
		int firstPage = pageNo * rowsPages;
		int count = pubSaleService.getPubSaleCount(name, status);
		List<PubSale> saleList = pubSaleService.getPubSaleList(name, status, firstPage, rowsPages);
		PageUtil pageUtil = new PageUtil(count, rowsPages, pageNo, "/admin/pubsale/saleList.xhtml");
		Map params = new HashMap();
		params.put("name", name);
		params.put("status", status);
		pageUtil.initPageInfo(params);
		model.put("saleList", saleList);
		model.put("pageUtil", pageUtil);
		return "admin/gewapay/pubsale/saleList.vm";
	}
	@RequestMapping("/admin/pubsale/setSaleStatus.xhtml")
	public String setSaleStatus(Long pid, String status, ModelMap model){
		if(StringUtils.isBlank(status)) return showJsonError(model, "参数错误！");
		PubSale sale = daoService.getObject(PubSale.class, pid);
		if(sale == null) return showJsonError(model, "该竞拍不存在或被删除！");
		if(sale.isJoin()) return showJsonError(model, "该竞拍正在进行不能修改状态！");
		ChangeEntry changeEntry = new ChangeEntry(sale);
		sale.setStatus(status);
		daoService.saveObject(sale);
		monitorService.saveChangeLog(getLogonUser().getId(), PubSale.class, sale.getId(), changeEntry.getChangeMap(sale));
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/pubsale/pmList.xhtml")
	public String saleList(Long sid, ModelMap model){
		PubSale sale = daoService.getObject(PubSale.class, sid);
		String qry = "from PubMember m where m.pubid=? order by m.addtime desc";
		List<PubMember> pmList = hibernateTemplate.find(qry, sale.getId());
		String sql = "select count(distinct p.pubid) from WEBDATA.pubmember p where p.memberid=?";
		Map<Long,Integer> memberCount = new HashMap<Long, Integer>();
		Map<Long, MemberInfo> memberInfoMap = new HashMap<Long, MemberInfo>();
		for(PubMember pm : pmList){
			if(!memberInfoMap.containsKey(pm.getMemberid())){
				memberCount.put(pm.getMemberid(), jdbcTemplate.queryForInt(sql, pm.getMemberid()));
				memberInfoMap.put(pm.getMemberid(), daoService.getObject(MemberInfo.class, pm.getMemberid()));
			}
		}
		PubSaleOrder order = daoService.getObjectByUkey(PubSaleOrder.class, "pubid", sale.getId(), false);
		model.put("sale", sale);
		model.put("order", order);
		model.put("pmList", pmList);
		model.put("memberCount", memberCount);
		model.put("memberInfoMap", memberInfoMap);
		return "admin/gewapay/pubsale/pmList.vm";
	}
	@RequestMapping("/admin/pubsale/salePubMember.xhtml")
	public String salePubMember(Long pubid,Timestamp startTime, Timestamp endTime,Integer startPoint, Integer endPoint, ModelMap model){
		List<Object> params = new ArrayList<Object>();
		String sql = "select p.memberid as memberid," +
				"(select m.nickname from WEBDATA.member m where m.recordid=p.memberid) as nickname," +
				"(select s.name from WEBDATA.pubsale s where s.recordid=p.pubid) as psname," +
				"p.pubid as pubid,max(p.addtime) as addtime,max(p.price)/100.00 as price,min(p.pointvalue) as pointvalue," +
				"(select count(distinct c.pubid) from WEBDATA.pubmember c where memberid=p.memberid) as num, " +
				"count(memberid)*(select s.needpoint from WEBDATA.pubsale s where s.recordid=p.pubid) as pnum " +
				"from WEBDATA.pubmember p where p.addtime >=? ";
		StringBuilder paramSql= new StringBuilder();
		Timestamp curTime = DateUtil.getCurFullTimestamp();
		if(startTime==null&&endTime==null){
			startTime = DateUtil.addDay(curTime,-30);
		}else if(startTime==null&&!(endTime==null)){
			startTime = DateUtil.addDay(endTime, -30);
		}
		params.add(startTime);
		if(endTime!=null){
			paramSql.append("and p.addtime <=? ");
			params.add(endTime);
		}
		if(startPoint!=null){
			paramSql.append("and p.pointvalue >=? ");
			params.add(startPoint);
		}
		if(endPoint!=null){
			paramSql.append("and p.pointvalue<=? ");
			params.add(endPoint);
		}	
		if(pubid!=null){
			paramSql.append("and p.pubid=? ");
			params.add(pubid);
		}
		
		sql =sql+ paramSql.toString()+" group by p.pubid,p.memberid order by min(p.pointvalue)";
		List<Map<String,Object>> result = jdbcTemplate.queryForList(sql, params.toArray());
		model.put("pmList", result);
		return "admin/gewapay/pubsale/salePubMember.vm";
	}

	@RequestMapping("/admin/pubsale/getSale.xhtml")
	public String saleForm(Long id, ModelMap model){
		PubSale sale = null;
		if(id!=null) sale = daoService.getObject(PubSale.class, id);
		model.put("sale", sale);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		return "admin/gewapay/pubsale/saleForm.vm";
	}
	@RequestMapping("/admin/pubsale/addSale.xhtml")
	public String addSale(Long id, HttpServletRequest request, ModelMap model){
		PubSale sale = null;
		if(id!=null) sale = daoService.getObject(PubSale.class, id);
		else sale = new PubSale("");
		BindUtils.bindData(sale, request.getParameterMap());
		if(StringUtils.isBlank(sale.getCitycode())) return showJsonError(model, "请选择城市！");
		if(StringUtils.isBlank(sale.getSaletype())) return showJsonError(model, "请选择类型！");
		if(StringUtils.contains(sale.getCardpass(), "，")) return showJsonError(model, "卡密码含有特殊字符！");
		if(sale.isCard()){
			/*if(StringUtils.isBlank(sale.getCardpass())) return showJsonError(model, "卡密码必须填写！");
			String[] pass = StringUtils.split(sale.getCardpass(), ",");
			for(String p : pass){
				ElecCard card = elecCardService.getElecCardByPass(p);
				if(card==null) return showJsonError(model, "密码:" + p + "对应的卡不存在！");
			}*/
		}
		Double dlowerprice = Double.parseDouble(request.getParameter("dlowerprice"));
		String dupprice = request.getParameter("dupprice");
		sale.setLowerprice(doubleToInt(dlowerprice*100));
		if(StringUtils.isNotBlank(dupprice)){
			List<String> tmpList = Arrays.asList(StringUtils.split(dupprice,","));
			sale.setDupprice(doubleToString(tmpList));
		}else{
			sale.setDupprice("0");
		}
		if(id==null || sale.getMemberid()==null) {
			sale.setStatus(Status.N);
			sale.setCurprice(sale.getLowerprice());
			sale.setLasttime(DateUtil.addSecond(sale.getBegintime(), sale.getCountdown()));
		}
		daoService.saveObjectList(sale);
		String strId = "pub" + sale.getId();
		VersionCtl vc = daoService.getObject(VersionCtl.class, strId);
		if(sale.isSoon() && vc==null){
			vc = new VersionCtl(strId);
			vc.setCtldata("unsale");
			daoService.saveObjectList(vc);
		}
		return showJsonSuccess(model);
	}
	public Integer doubleToInt(Double d) {
		if(d==null) return 0;
		String x = StringUtils.split(d.toString(), ".")[0];
		return Integer.valueOf(x);
	}
	public String doubleToString(List<String> duppriceList) {
		if(CollectionUtils.isEmpty(duppriceList)) return "";
		List<Integer> tmpList = new ArrayList<Integer>();
		for (String tmp : duppriceList) {
			Double dupprice = Double.parseDouble(tmp);
			if(dupprice != null && dupprice>=0){
				Integer tmpInt = Double.valueOf(dupprice*100).intValue();
				tmpList.add(tmpInt);
			}
		}
		return StringUtils.join(tmpList, ",");
	}
	@RequestMapping("/admin/pubsale/setOrdernum.xhtml")
	public String setOrdernum(Long id, Integer ordernum, ModelMap model){
		PubSale  sale = daoService.getObject(PubSale.class, id);
		sale.setOrdernum(ordernum);
		daoService.saveObject(sale);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/pubsale/postMsg.xhtml")
	public String setOrdernum(Long orderid, String company, String sno, ModelMap model){
		PubSaleOrder  porder = daoService.getObject(PubSaleOrder.class, orderid);
		ErrorCode code = messageService.addPostPubSaleMessage(porder, company, sno);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
}
