package com.gewara.web.action.admin.ticket;

import java.sql.Timestamp;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.sys.ConfigTag;
import com.gewara.model.partner.PartnerCloseRule;
import com.gewara.service.partner.PartnerService;
import com.gewara.untrans.monitor.ConfigCenter;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.web.action.admin.BaseAdminController;
@Controller
public class PartnerAdminController extends BaseAdminController implements InitializingBean{
	@Autowired@Qualifier("partnerService")
	private PartnerService partnerService;

	@Autowired@Qualifier("configCenter")
	private ConfigCenter configCenter;
	@RequestMapping("/admin/ticket/partner/closeRuleList.xhtml")
	public String closeRoleList(ModelMap model){
		//TODO:分有效、无效
		List<PartnerCloseRule> closeRuleList = daoService.getObjectList(PartnerCloseRule.class, "updatetime", false, 0, 5000);
		model.put("closeRuleList", closeRuleList);
		return "admin/ticket/partner/closeRuleList.vm";
	}
	@RequestMapping("/admin/ticket/partner/modifyCloseRule.xhtml")
	public String modifyCloseRule(Long ruleId, ModelMap model){
		PartnerCloseRule rule = null;
		if(ruleId !=null) {
			rule = daoService.getObject(PartnerCloseRule.class, ruleId);
			model.put("rule", rule);
		}
		return "admin/ticket/partner/closeRule.vm";
	}
	@RequestMapping("/admin/ticket/partner/saveCloseRule.xhtml")
	public String saveCloseRule(Long ruleId, HttpServletRequest request, ModelMap model){
		PartnerCloseRule rule = null;
		if(ruleId !=null ) rule = daoService.getObject(PartnerCloseRule.class, ruleId);
		else rule = new PartnerCloseRule();
		ChangeEntry changeEntry = new ChangeEntry(rule);
		BindUtils.bindData(rule, request.getParameterMap());
		monitorService.saveChangeLog(getLogonUser().getId(), PartnerCloseRule.class, rule.getId(), changeEntry.getChangeMap(rule));
		rule.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(rule);
		configCenter.refresh(Config.SYSTEMID, ConfigTag.KEY_PARTNER_CLOSE_RULE);
		return showJsonSuccess(model, ""+rule.getId());
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		configCenter.register(Config.SYSTEMID, ConfigTag.KEY_PARTNER_CLOSE_RULE, partnerService);
	}
}
