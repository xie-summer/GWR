package com.gewara.web.action.admin.gewapay;

import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.pay.SpecialDiscount;
import com.gewara.service.ticket.TicketDiscountService;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class SpCodeAdminController extends BaseAdminController{
	@Autowired@Qualifier("ticketDiscountService")
	private TicketDiscountService ticketDiscountService;
	
	@RequestMapping("/admin/gewapay/spdiscount/spcode/manage.xhtml")
	public String spCodeList(Long sid, ModelMap model){
		SpecialDiscount spdiscount = daoService.getObject(SpecialDiscount.class, sid);
		Map row = ticketDiscountService.getSpCodeCountStats(sid);
		model.put("stats", row);
		model.put("spdiscount", spdiscount);
		return "admin/gewapay/spdiscount/spcodeManage.vm";
	}
	@RequestMapping("/admin/gewapay/spdiscount/spcode/genPass.xhtml")
	public String genPass(Long sid, int max, ModelMap model){
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, sid);
		ticketDiscountService.genSpCode(sd, max);
		return showMessage(model, "成功生成！");
	}
	@RequestMapping("/admin/gewapay/spdiscount/spcode/exportPass.xhtml")
	public void exportPass(HttpServletResponse response, Long sid) throws Exception{
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, sid);
		response.setContentType("application/x-download");
		response.addHeader("Content-Disposition", "attachment;filename=spcode" + sid + ".txt");
		Writer writer = response.getWriter();
		ticketDiscountService.exportSpCodePassBySd(sd, writer, getLogonUser().getId());
		writer.close();
	}

}
