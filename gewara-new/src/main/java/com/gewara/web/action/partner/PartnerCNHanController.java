package com.gewara.web.action.partner;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.api.ApiUser;
import com.gewara.model.movie.Movie;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.util.BeanUtil;
import com.gewara.util.StringUtil;

/**
 * 热映购票排行榜 其它购票页面走通用的partnerMovieCommon
 * @author gang.liu
 *
 */
@Controller
public class PartnerCNHanController extends BasePartnerController{
	private ApiUser getApiUser(String key){
		return daoService.getObjectByUkey(ApiUser.class, "partnerkey", key, true);
	}
	
	private boolean validPartner(String sign,ApiUser partner){
		if(partner == null){
			return false;
		}
		if(StringUtils.equals(sign, StringUtil.md5("key=" + partner.getPartnerkey() + "&privateKey=" + partner.getPrivatekey()))){
			return true;
		}
		return false;
	}
	
	@RequestMapping("/partner/common/hotSaleList.xhtml")
	public String index( ModelMap model,String sign, String key){
		if(StringUtils.isBlank(key)){
			return forwardMessage(model, "请确保正确的来源渠道！");
		}
		ApiUser partner = getApiUser(key);
		if(!validPartner(sign, partner)){
			return forwardMessage(model, "请确保正确的来源渠道！");
		}
		List<Movie> rankMovieList= mcpService.getHotPlayMovieList(partner.getDefaultCity());
		Collections.sort(rankMovieList, new MultiPropertyComparator(new String[]{"boughtcount"}, new boolean[]{false}));
		rankMovieList =BeanUtil.getSubList(rankMovieList, 0, 6);
		model.put("rankMovieList", rankMovieList);
		model.put("sign", sign);
		model.put("key", key);
		return "partner/cnhan/hotSaleList.vm";
	}
}
