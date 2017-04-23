package com.gewara.web.action.api2.bbs;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.web.action.api.BaseApiController;

/**
 * ÆÀ·ÖAPI
 * 
 * @author taiqichao
 * 
 */
@Controller
public class Api2MarkController extends BaseApiController {
	/**
	 * Ìí¼ÓÆÀ·Ö
	 * 
	 * @param memberEncode
	 * @param tag
	 * @param relatedid
	 * @param generalmark
	 * @param marks
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/mark/addMark.xhtml")
	public String addMark(ModelMap model) {
		return notSupport(model);
	}

}
