/**
 * 
 */
package com.gewara.web.action.partner;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.pay.TicketOrder;
import com.gewara.pay.JFTUtil;

@Controller
public class PartnerJiFutongController extends BasePartnerController{
	private ApiUser getJft(){
		return daoService.getObject(ApiUser.class, PartnerConstant.PARTNER_JIFUTONG);
	}
	@RequestMapping("/partner/jifutong/qryOrder.xhtml")
	@ResponseBody
	public String qryOrder(String tradeno, String payseqno){
		ApiUser apiUser = getJft();
		TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", tradeno, false);
		String result = JFTUtil.getQryResult(order, payseqno, apiUser);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_PARTNER, "集付通查询结果：" + result);
		return result;
	}
}
