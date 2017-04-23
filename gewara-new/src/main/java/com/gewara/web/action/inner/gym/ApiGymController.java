package com.gewara.web.action.inner.gym;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.user.Member;
import com.gewara.service.OpenGymService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.web.component.ShLoginService;
import com.gewara.xmlbind.gym.CardItem;

@Controller
public class ApiGymController extends BaseApiController {
	
	@Autowired@Qualifier("loginService")
	private ShLoginService loginService;
	
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	
	@Autowired@Qualifier("openGymService")
	private OpenGymService openGymService;
	
	@RequestMapping("/inner/gym/lockOrder.xhtml")
	public String  lockOrder(@CookieValue(required=false, value="origion")String origin, Long cid, String speciallist, Integer quantity, String mobile, String sessid, String ip, Date startdate, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "请先登录！");
		if(cid == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "健身卡编号错误！");
		if(quantity == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "数量不能为空！");
		if(!ValidateUtil.isMobile(mobile)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "手机号格式错误！");
		if(startdate == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "激活日期不能为空！");
		ErrorCode<CardItem> code = synchGymService.getGymCardItem(cid, true);
		if(!code.isSuccess()) return getErrorXmlView(model, code.getErrcode(), code.getMsg());
		CardItem cardItem = code.getRetval();
		if(cardItem.getCostprice() == null || cardItem.getPrice() == null || cardItem.getCostprice()<=0) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "价格错误！");
		ErrorCode<GymOrder> orderCode = openGymService.addGymOrder(code.getRetval(), quantity, mobile, speciallist, startdate, member, origin);
		if(!orderCode.isSuccess()) return getErrorXmlView(model, orderCode.getErrcode(), orderCode.getMsg());
		model.put("order", orderCode.getRetval());
		return getXmlView(model, "inner/gym/orderInfo.vm");
	}
	
}
