package com.gewara.web.action.admin.cooperate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.sys.MongoData;
import com.gewara.json.cooperate.UnionPayFastCardbin;
import com.gewara.mongo.MongoService;
import com.gewara.util.BeanUtil;
import com.gewara.util.PinYinUtils;
import com.gewara.util.StringUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


/**
 * 银联活动卡BIN配置
 */
@Controller
public class UnionpayFastActiveController extends BaseAdminController {

	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	
	
	@RequestMapping("/admin/unionpayFast/modifyCardbin.xhtml")
	public String modifyCardbin(String id, ModelMap model) {
		if (StringUtils.isNotBlank(id)) {
			UnionPayFastCardbin unionPayFastCardbin = mongoService.getObject(UnionPayFastCardbin.class, "id", id);
			model.put("unionPayFastCardbin", unionPayFastCardbin);
		}
		return "admin/cooperate/unionpayFast/saveCardbin.vm";
	}
	
	@RequestMapping("/admin/unionpayFast/cardbinList.xhtml")
	public String cardbinList(UnionPayFastCardbin cardBin, Integer pageNo, ModelMap model) {
		int allCount = mongoService.getObjectCount(UnionPayFastCardbin.class);
		Map<String, String> params = new HashMap<String, String>();
		
		int ROWS_PER_PAGE = 10;
		if(pageNo == null)pageNo = 0;
		int rowsPerPage = ROWS_PER_PAGE;
		int firstPerPage = pageNo * rowsPerPage;
		
		DBObject queryCondition = new BasicDBObject();
		
		if (cardBin != null) {
			if (StringUtils.isNotBlank(cardBin.getCardbinUkey())) {
				DBObject cardbinUkeyDbObject = mongoService.queryBasicDBObject("cardbinUkey", "=", cardBin.getCardbinUkey());
				queryCondition.putAll(cardbinUkeyDbObject);
				params.put("cardbinUkey", cardBin.getCardbinUkey());
			}
			if (StringUtils.isNotBlank(cardBin.getRemark())) {
				DBObject remarkDbObject = new BasicDBObject("remark", Pattern.compile(cardBin.getRemark()));
				queryCondition.putAll(remarkDbObject);
				params.put("remark", cardBin.getRemark());
			}
			if (StringUtils.isNotBlank(cardBin.getRequirements())) {
				DBObject remarkDbObject = new BasicDBObject("requirements", Pattern.compile(cardBin.getRequirements()));
				queryCondition.putAll(remarkDbObject);
				params.put("requirements", cardBin.getRemark());
			}
		}
		List<UnionPayFastCardbin> unionPayFastCardbinList = mongoService.getObjectList(UnionPayFastCardbin.class, queryCondition, "cardbinUkey", true, firstPerPage, rowsPerPage);
		model.put("unionPayFastCardbinList", unionPayFastCardbinList);
		PageUtil pageUtil = new PageUtil(allCount, rowsPerPage, pageNo, "admin/unionpayFast/cardbinList.xhtml", true, true);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("cardbin", cardBin);
		return "admin/cooperate/unionpayFast/cardbinList.vm";
	}
	
	@RequestMapping("/admin/unionpayFast/saveCardbin.xhtml")
	public String addCardBin(String cardbins, UnionPayFastCardbin unionPayFastCardbin, ModelMap model) {
		if (StringUtils.isBlank(unionPayFastCardbin.getCardbinUkey())) {
			unionPayFastCardbin.setCardbinUkey("unionpayFast:" + PinYinUtils.getFirstSpell(unionPayFastCardbin.getRemark()) + StringUtil.getRandomString(4));
		}
		if (StringUtils.isNotBlank(cardbins)) {
			unionPayFastCardbin.setCardbinList(Arrays.asList(cardbins.split(",")));
		}
		if (StringUtils.isBlank(unionPayFastCardbin.getId())) {
			unionPayFastCardbin.setId(MongoData.buildId());
		}
		mongoService.saveOrUpdateObject(unionPayFastCardbin, "id");
		model.put("id", unionPayFastCardbin.getId());
		return "redirect:/admin/unionpayFast/cardbinDetail.xhtml";
	}
	
	
	@RequestMapping("/admin/unionpayFast/cardbinDetail.xhtml")
	public String cardbinDetail(String id, Integer pageNo, ModelMap model) {
		
		int ROWS_PER_PAGE = 50;
		if(pageNo == null)pageNo = 0;
		int rowsPerPage = ROWS_PER_PAGE;
		int firstPerPage = pageNo * rowsPerPage;
		
		UnionPayFastCardbin unionPayFastCardbin = mongoService.getObject(UnionPayFastCardbin.class, "id", id);
		if (unionPayFastCardbin != null && CollectionUtils.isNotEmpty(unionPayFastCardbin.getCardbinList())) {
			PageUtil pageUtil = new PageUtil(unionPayFastCardbin.getCardbinList().size(), rowsPerPage, pageNo, 
					"admin/unionpayFast/cardbinDetail.xhtml", true, true);
			model.put("pageUtil", pageUtil);
			Map<String, String> params = new HashMap<String, String>();
			params.put("id", id);
			pageUtil.initPageInfo(params);
			unionPayFastCardbin.setCardbinList(BeanUtil.getSubList(unionPayFastCardbin.getCardbinList(), firstPerPage, ROWS_PER_PAGE));
			model.put("unionPayFastCardbin", unionPayFastCardbin);
		}
		return "admin/cooperate/unionpayFast/cardbinDetail.vm";
	}
	
	@RequestMapping("/admin/unionpayFast/removeCardbin.xhtml")
	public String removeCardbin(String id, String cardbinNo, ModelMap model) {
		if (StringUtils.isBlank(id)) {
			return showJsonError(model, "id不能为空");
		}
		// 删除单个cardBin
		if (StringUtils.isNotBlank(cardbinNo)) {
			UnionPayFastCardbin unionPayFastCardbin = mongoService.getObject(UnionPayFastCardbin.class, "id", id);
			List<String> cardList = unionPayFastCardbin.getCardbinList();
			cardList.remove(cardbinNo.trim());
			if (CollectionUtils.isEmpty(cardList)) {
				mongoService.removeObjectById(UnionPayFastCardbin.class, "id", id);
			} else {
				unionPayFastCardbin.setCardbinList(cardList);
				mongoService.saveOrUpdateObject(unionPayFastCardbin, "id");
			}
		} else {
			// 删除在cardbinUkey下的所有cardBin
			mongoService.removeObjectById(UnionPayFastCardbin.class, "id", id);
		}
		return showJsonSuccess(model);
	}
	
	
	@RequestMapping("/admin/unionpayFast/checkCardbinUkey.xhtml")
	public String checkCardbin(String cardbinUkey, ModelMap model) {
		mongoService.getObject(UnionPayFastCardbin.class, "cardbinUkey", cardbinUkey);
		return showJsonSuccess(model);
	}
}
