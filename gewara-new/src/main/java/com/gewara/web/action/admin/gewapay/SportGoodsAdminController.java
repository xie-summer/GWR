package com.gewara.web.action.admin.gewapay;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.GoodsConstant;
import com.gewara.constant.TagConstant;
import com.gewara.model.acl.User;
import com.gewara.model.goods.SportGoods;
import com.gewara.model.sport.Sport;
import com.gewara.service.order.GoodsService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class SportGoodsAdminController extends BaseAdminController {
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	public void setGoodsService(GoodsService goodsService) {
		this.goodsService = goodsService;
	}
	
	@RequestMapping("/admin/goods/goodsListToSport.xhtml")
	public String goodsListToSport(Long sid, String datetype, ModelMap model, HttpServletRequest request){
		Sport sport = daoService.getObject(Sport.class, sid);
		if(sport == null) return showError(model, "关联的运动场馆不存在！");
		String citycode = getAdminCitycode(request);
		String tag = GoodsConstant.GOODS_TYPE_SPORT;
		model.put("tag", tag);
		boolean isToTime = false;
		if(StringUtils.isBlank(datetype) || StringUtils.equals(datetype, "cur")) isToTime = true;
		List<SportGoods> sportgoodsList = goodsService.getSportGoodsList(citycode, tag, sid, isToTime, false, false, null, false);
		Map<Long, Sport> sportMap = new HashMap<Long, Sport>();
		for(SportGoods sportGoods : sportgoodsList){
			sportMap.put(sportGoods.getId(), daoService.getObject(Sport.class, sportGoods.getRelatedid()));
		}
		model.put("sportgoodsList", sportgoodsList);
		model.put("sportMap", sportMap);
		return "admin/goods/sportGoodsList.vm";
	}
	
	@RequestMapping("/admin/goods/addSportGoods.xhtml")
	public String addSportGoods(Long id, ModelMap model){
		SportGoods sportgoods = null;
		if(id != null) {
			sportgoods = daoService.getObject(SportGoods.class, id);
			model.put("goods", sportgoods);
		}
		return "admin/goods/addSportGoods.vm";
	}
	
	@RequestMapping("/admin/goods/ajax/saveSportGoods.xhtml")
	public String saveSportGoods(HttpServletRequest request, String goodsname, Integer unitprice, Long id, ModelMap model){
		User user = getLogonUser();
		SportGoods sportgoods = null;
		if(id != null){
			sportgoods =  daoService.getObject(SportGoods.class, new Long(id));
			sportgoods.setClerkid(user.getId());
		}else{
			sportgoods = new SportGoods(goodsname, unitprice, user.getId());
			String citycode = getAdminCitycode(request);
			sportgoods.setCitycode(citycode);
		}
		sportgoods.setManager(GoodsConstant.MANAGER_USER);
		BindUtils.bindData(sportgoods, request.getParameterMap());
		if(sportgoods.getFromtime() == null){
			sportgoods.setFromtime(sportgoods.getAddtime());
		}
		if(sportgoods.getAllowaddnum()==null){
			sportgoods.setAllowaddnum(0);
		}
		if(sportgoods.getMaxpoint() == null){
			sportgoods.setMaxpoint(0);
		}
		if(sportgoods.getMinpoint() == null){
			sportgoods.setMinpoint(0);
		}
		sportgoods.setFeetype(GoodsConstant.FEETYPE_T);
		sportgoods.setServicetype(TagConstant.TAG_SPORT);
		Timestamp time = sportgoods.getReleasetime();
		sportgoods.setReleasetime(DateUtil.getBeginningTimeOfDay(time));
		//验证内容
		String msg=ValidateUtil.validateNewsContent(null, sportgoods.getDescription());
		if(StringUtils.isNotBlank(msg)) return showJsonError(model, msg);
		daoService.saveObject(sportgoods);
		return showJsonSuccess(model, BeanUtil.getBeanMap(sportgoods));
	}
}
