package com.gewara.web.action.admin.gewapay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.goods.ActivityGoods;
import com.gewara.service.order.GoodsService;
import com.gewara.support.ErrorCode;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class ActivityGoodsAdminController extends BaseAdminController {
	
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	public void setGoodsService(GoodsService goodsService){
		this.goodsService = goodsService;
	}
	
	@RequestMapping("/admin/goods/activityGoodsList.xhtml")
	public String goodsList(Integer pageNo, Long relatedid, ModelMap model){
		if(pageNo == null) pageNo = 0;
		int rowsPerPage = 50;
		int firstPre = pageNo * rowsPerPage;
		int count = goodsService.getGoodsCount(ActivityGoods.class, null, relatedid, false, false, false);
		if(count > 0){
			List<ActivityGoods> goodsList = goodsService.getGoodsList(ActivityGoods.class, null, null, relatedid, false, false, false, null, false, firstPre, rowsPerPage);
			model.put("goodsList", goodsList);
		}
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/admin/goods/activityGoodsList.xhtml");
		Map params = new HashMap();
		params.put("relatedid", relatedid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		return "admin/goods/activityGoodsList.vm";
	}
	
	@RequestMapping("/admin/goods/getActivityGoods.xhtml")
	public String getActivityGoods(Long gid, HttpServletRequest request, ModelMap model){
		ActivityGoods goods = daoService.getObject(ActivityGoods.class, gid);
		if(goods == null) return showMessageAndReturn(model, request, "该商品不存在或被删除！");
		model.put("goods", goods);
		return "admin/goods/activityGoods.vm";
	}
	
	@RequestMapping("/admin/goods/ajax/updateActivityGoods.xhtml")
	public String updateActivityGoods(Long id, HttpServletRequest request, ModelMap model){
		ActivityGoods goods = daoService.getObject(ActivityGoods.class, id);
		if(goods == null) return showJsonError(model, "该商品不存在或被删除！");
		Map<String, String> dataMap = WebUtils.getRequestMap(request);
		ErrorCode<ActivityGoods> code = goodsService.saveOrUpdateActivityGoods(getLogonUser().getId(), id, dataMap);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}

}

