package com.gewara.web.action.api2.bbs;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.web.action.api.BaseApiController;

@Controller
public class Api2MessageController extends BaseApiController {
	/**
	 * 发生站内信
	 * @param tomemberid
	 * @param action
	 * @param actionid
	 * @param body
	 */
	@RequestMapping("/api2/message/sendSiteMSG.xhtml")
	public String sendSiteMSG(ModelMap model){
		return notSupport(model);
	}
	
	
	/**
	 * 私信用户ID与PIC
	 * @author liuyunxin
	 * @date 2012-11-06
	 * 
	 * */
	@RequestMapping("/api2/message/sendUserMsgList.xhtml")
	public String sendUserMsgList(ModelMap model){
		return notSupport(model);
	}
}
