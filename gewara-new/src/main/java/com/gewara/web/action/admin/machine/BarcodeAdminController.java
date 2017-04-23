package com.gewara.web.action.admin.machine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.machine.Barcode;
import com.gewara.model.machine.MachineSynch;
import com.gewara.model.machine.TakeTicket;
import com.gewara.service.BarcodeService;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
@Controller
public class BarcodeAdminController extends BaseAdminController {
	@Autowired@Qualifier("barcodeService")
	private BarcodeService barcodeService;
	@RequestMapping("/admin/machine/createBarcode.xhtml")
	public String addGewaMachine(Long placeid, ModelMap model){
		if(placeid==null) return forwardMessage(model, "请输入场馆id");
		int i = barcodeService.handCreateNewBarcodeByPlaceid(placeid);
		return forwardMessage(model, "本次创建条形码：" + i + "个");
	}
	@RequestMapping("/admin/machine/getBarcodeList.xhtml")
	public String addGewaMachine(String barcode, Long relatedid, Long placeid, Long itemid, String tradeno, Integer pageNo, ModelMap model){
		String vm = "admin/machine/barcodeList.vm";
		if(relatedid==null && placeid==null && StringUtils.isBlank(tradeno) && StringUtils.isBlank(barcode)){
			model.put("msg", "条件查询不能同时为空！");
			return vm;
		}
		if(pageNo==null) pageNo = 0;
		int rowsPerpage = 50;
		int count = barcodeService.getBarcodeCount(barcode, relatedid, placeid, itemid, tradeno);
		List<Barcode> barcodeList = barcodeService.getBarcodeList(barcode, relatedid, placeid, itemid, tradeno, pageNo*rowsPerpage, rowsPerpage);
		PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/machine/getBarcodeList.xhtml", true, true);
		Map params = new HashMap();
		if(StringUtils.isNotBlank(barcode))params.put("barcode", barcode);
		if(placeid!=null)params.put("placeid", placeid);
		if(relatedid!=null)params.put("relatedid", relatedid);
		if(itemid!=null)params.put("itemid", itemid);
		if(StringUtils.isNotBlank(tradeno)) params.put("tradeno", tradeno);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("barcodeList", barcodeList);
		return vm;
	}
	
	@RequestMapping("/admin/machine/moreMachineSynchList.xhtml")
	public String addGewaMachine(Long placeid, String macid, ModelMap model){
		DetachedCriteria query = DetachedCriteria.forClass(MachineSynch.class);
		if(placeid!=null){
			query.add(Restrictions.eq("placeid", placeid));
		}
		if(StringUtils.isNotBlank(macid)){
			query.add(Restrictions.eq("macid", macid));
		}
		query.addOrder(Order.desc("id"));
		List<MachineSynch> synchList = hibernateTemplate.findByCriteria(query);
		model.put("mcList", synchList);
		return "admin/machine/moreMachineSynchList.vm";
	}
	
	@RequestMapping("/admin/machine/takeTicketList.xhtml")
	public String takeTicketList(Long placeid, String macid, String tradeno, ModelMap model){
		DetachedCriteria query = DetachedCriteria.forClass(TakeTicket.class);
		if(placeid!=null){
			query.add(Restrictions.eq("placeid", placeid));
		}
		if(StringUtils.isNotBlank(macid)){
			query.add(Restrictions.eq("macid", macid));
		}
		if(StringUtils.isNotBlank(tradeno)){
			query.add(Restrictions.eq("tradeno", tradeno));
		}
		query.addOrder(Order.desc("id"));
		List<MachineSynch> ttList = hibernateTemplate.findByCriteria(query);
		model.put("ttList", ttList);
		return "admin/machine/takeTicketList.vm";
	}
}
