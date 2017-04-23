package com.gewara.web.action.common;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.content.Link;
import com.gewara.untrans.CommonService;

@Controller
public class FooterController {
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	@RequestMapping("/link.dhtml")
	public String link(ModelMap model){
		List<Link> pictureLinkList = commonService.getLinkListByType(Link.TYPE_PICTURE);
		List<Link> textLinkList = commonService.getLinkListByType(Link.TYPE_TEXT);
		model.put("pictureLinkList", pictureLinkList);
		model.put("textLinkList", textLinkList);
		return "common/link.vm";
	}
	
	@RequestMapping("/service/groupBuy.dhtml")
	public String groupBuy(){
		return "footer/groupBuy.vm";
	}
	
	@RequestMapping("/service/about.dhtml")
	public String about(){
		return "footer/about.vm";
	}
	
	@RequestMapping("/service/buss.dhtml")
	public String buss(){
		return "footer/buss.vm";
	}
	
	@RequestMapping("/service/weekly.dhtml")
	public String weekly(){
		return "footer/weekly.vm";
	}
	
	@RequestMapping("/service/contribute.dhtml")
	public String contribute(){
		return "footer/contribute.vm";
	}
	
	@RequestMapping("/service/integral.dhtml")
	public String integral(){
		return "footer/integral.vm";
	}
	
	@RequestMapping("/service/marketing.dhtml")
	public String marketing(){
		return "footer/marketing.vm";
	}
	
	@RequestMapping("/service/link.dhtml")
	public String link(){
		return "footer/link.vm";
	}
	
	@RequestMapping("/service/law.dhtml")
	public String law(){
		return "footer/law.vm";
	}
	
	@RequestMapping("/service/treaty.dhtml")
	public String treaty(){
		return "footer/treaty.vm";
	}
	
	@RequestMapping("/service/communityHelp.dhtml")
	public String communityHelp(){
		return "footer/communityHelp.vm";
	}
	
	@RequestMapping("/service/commuProtocol.dhtml")
	public String commuProtocol(){
		return "footer/commuProtocol.vm";
	}
	
	@RequestMapping("/service/degree.dhtml")
	public String degree(){
		return "footer/degree.vm";
	}
	@RequestMapping("/service/ticket.dhtml")
	public String ticket(){
		return "footer/ticket.vm";
	}
	
	@RequestMapping("/service/post.dhtml")
	public String post(){
		return "footer/post.vm";
	}
	
	@RequestMapping("/service/fee.dhtml")
	public String fee(){
		return "footer/fee.vm";
	}
	
	@RequestMapping("/service/global/header.dhtml")
	public String header(){
		return "footer/global/header.vm";
	}
	
	@RequestMapping("/service/global/footer.dhtml")
	public String footer(){
		return "footer/global/footer.vm";
	}
	
	@RequestMapping("/service/center.dhtml")
	public String center(){
		return "footer/center.vm";
	}
	//ÓªÒµÖ´ÕÕ
	@RequestMapping("/service/license.dhtml")
	public String license(){
		return "footer/license.vm";
	}
}
