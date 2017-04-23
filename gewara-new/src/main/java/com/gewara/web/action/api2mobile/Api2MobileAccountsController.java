package com.gewara.web.action.api2mobile;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.web.action.api.BaseApiController;

/**
 * 手机客户端账户相关业务
 * @author taiqichao
 * @deprecated
 */
@Controller
public class Api2MobileAccountsController extends BaseApiController {
	/**
	 * 发送手机绑定动态验证码
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/mobile/accounts/getBindingCaptcha.xhtml")
	public String getBindingCaptcha(ModelMap model){
		return notSupport(model);
	}
	
	
	/**
	 * 绑定手机号
	 * @param key
	 * @param encryptCode
	 * @param mobile
	 * @param dynamicNumber
	 * @param version
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/mobile/accounts/bindingMobile.xhtml")
	public String bindingMobile(ModelMap model){
		return notSupport(model);
	}
	
	/**
	 * 贵宾卡充值
	 * @param key
	 * @param encryptCode
	 * @param memberEncode
	 * @param cardpass
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/mobile/accounts/ccardPayCharge.xhtml")
	public String ccardPayCharge(ModelMap model){
		return notSupport(model);
	}
	
	/**
	 * 新增账户信息
	 * @param realname
	 * @param password
	 * @param confirmPassword
	 * @param idcard
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/mobile/accounts/saveAccount.xhtml")
	public String saveAccount(ModelMap model){
		return notSupport(model);
	}
	
}
