package com.gewara.web.action.admin.content;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.sys.MongoData;
import com.gewara.model.common.City;
import com.gewara.model.movie.Cinema;
import com.gewara.mongo.MongoService;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;

@Controller
public class VoucherCardsAdminController extends AnnotationController {

	@Autowired
	@Qualifier("mongoService")
	private MongoService mongoService;

	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}

	// 团体销售
	@RequestMapping("/admin/content/sales/voucherCard.xhtml")
	public String voucherCard() {
		return "admin/content/sales/voucherCardIndex.vm";
	}

	// 查询当前的兑换券种类
	@RequestMapping("/admin/content/sales/voucherCardType.xhtml")
	public String voucherCardType(ModelMap model) {
		Map params = new HashMap();
		List<Map> voucherCardList = mongoService.find(MongoData.NS_VOUCHERCARD_TYPE, params, MongoData.DEFAULT_ID_NAME, true);
		model.put("voucherCardList", voucherCardList);
		return "admin/content/sales/voucherCardType.vm";
	}

	// 增加兑换券种类
	@RequestMapping("/admin/content/sales/addVoucherCard.xhtml")
	public String addVoucherCard(String name, Integer num, ModelMap model) {
		if (StringUtils.isBlank(name) || num == null)
			return showJsonError(model, "参数错误！");
		Map params = new HashMap();
		params.put(MongoData.SYSTEM_ID, System.currentTimeMillis() + StringUtil.getRandomString(5));
		params.put(MongoData.ACTION_ORDERNUM, num);
		params.put(MongoData.ACTION_NAME, name);
		mongoService.saveOrUpdateMap(params, MongoData.SYSTEM_ID, MongoData.NS_VOUCHERCARD_TYPE);
		return showJsonSuccess(model);
	}

	// 修改兑换券的排序num
	@RequestMapping("/admin/content/sales/changeCardOrderNum.xhtml")
	public String changeCardOrderNum(String id, Integer num, ModelMap model) {
		if (StringUtils.isBlank(id) || num == null)
			return showJsonError(model, "参数错误！");
		Map map = mongoService.findOne(MongoData.NS_VOUCHERCARD_TYPE, MongoData.SYSTEM_ID, id);
		if (map != null) {
			map.put(MongoData.ACTION_ORDERNUM, num);
			mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_VOUCHERCARD_TYPE);
			return showJsonSuccess(model);
		}
		return showJsonError(model, "参数错误！");
	}

	// 删除指定票券
	@RequestMapping("/admin/content/sales/doDelCard.xhtml")
	public String doDelCard(String id, ModelMap model) {
		if (StringUtils.isBlank(id))
			return showJsonError(model, "参数错误！");
		mongoService.removeObjectById(MongoData.NS_VOUCHERCARD_TYPE, MongoData.SYSTEM_ID, id);
		return showJsonSuccess(model);
	}

	// 查询支持影院的信息
	@RequestMapping("/admin/content/sales/getCinemInfo.xhtml")
	public String getCinemaInfo(Integer pageNo, ModelMap model) {
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 20;
		int forms = pageNo * rowsPerPage;
		Map params = new HashMap();
		List<Map> cinemaList = mongoService.find(MongoData.NS_VOUCHERCARD_CINEMA, params, MongoData.ACTION_CITYCODE, true, forms, rowsPerPage);
		if (cinemaList != null) {
			PageUtil pageUtil = new PageUtil(mongoService.getCount(MongoData.NS_VOUCHERCARD_CINEMA, params), rowsPerPage, pageNo,
					"admin/content/sales/getCinemInfo.xhtml");
			pageUtil.initPageInfo(params);
			Map cardPram = new HashMap();
			BasicDBObject query = new BasicDBObject();
			query.put(QueryOperators.GT, 0);
			cardPram.put(MongoData.ACTION_ORDERNUM, query);
			List<Map> voucherCardList = mongoService.find(MongoData.NS_VOUCHERCARD_TYPE, cardPram, MongoData.ACTION_CITYCODE, true);
			model.put("voucherCardList", voucherCardList);
			model.put("cinemaList", cinemaList);
			model.put("pageUtil", pageUtil);
		}
		return "admin/content/sales/voucherCardCinema.vm";
	}

	// 新增影院信息
	@RequestMapping("/admin/content/sales/addCardCinema.xhtml")
	public String addCardCinema(String id, String relatedid, ModelMap model) {
		Map dataMap = new HashMap();
		Cinema cinema = null;
		if (StringUtils.isNotBlank(relatedid))
			cinema = daoService.getObject(Cinema.class, Long.valueOf(relatedid));
		if (cinema == null)
			return showJsonError(model, "此场馆不存在！");
		if (StringUtils.isNotBlank(id)) {
			dataMap = mongoService.findOne(MongoData.NS_VOUCHERCARD_CINEMA, MongoData.SYSTEM_ID, id);
		} else {
			dataMap.put(MongoData.SYSTEM_ID, System.currentTimeMillis() + StringUtil.getRandomString(5));
			dataMap.put(MongoData.ACTION_ADDTIME, DateUtil.currentTime());
		}
		dataMap.put(MongoData.ACTION_COUNTYCODE, cinema.getCountycode());
		dataMap.put(MongoData.ACTION_CITYCODE, cinema.getCitycode());
		City city = daoService.getObject(City.class, cinema.getCitycode());
		dataMap.put(MongoData.ACTION_PROVINCE, city.getProvince().getProvincecode());
		dataMap.put(MongoData.ACTION_PROVINCE_NAME, city.getProvince().getProvincename());
		dataMap.put(MongoData.ACTION_TITLE, cinema.getName());
		dataMap.put(MongoData.ACTION_NEWSLOGO, cinema.getLimg());
		dataMap.put(MongoData.ACTION_ADDRESS, cinema.getAddress());
		dataMap.put(MongoData.ACTION_RELATEDID, relatedid);
		dataMap.put(MongoData.ACTION_ORDERNUM, 0);
		dataMap.put(MongoData.ACTION_OTHERINFO, null);
		mongoService.saveOrUpdateMap(dataMap, MongoData.SYSTEM_ID, MongoData.NS_VOUCHERCARD_CINEMA);
		return showJsonSuccess(model);
	}

	// 保存影院支持的兑换券
	@RequestMapping("/admin/content/sales/changeCardCinema.xhtml")
	public String changeCardCinema(String id, String stauts, ModelMap model) {
		if (StringUtils.isBlank(id))
			return showJsonError(model, "数据错误！");
		Map dataMap = mongoService.findOne(MongoData.NS_VOUCHERCARD_CINEMA, MongoData.SYSTEM_ID, id);
		if (dataMap != null)
			dataMap.put(MongoData.ACTION_OTHERINFO, stauts);
		mongoService.saveOrUpdateMap(dataMap, MongoData.SYSTEM_ID, MongoData.NS_VOUCHERCARD_CINEMA);
		return showJsonSuccess(model);
	}

	// 修改电影院的排序ordernum
	@RequestMapping("/admin/content/sales/changeCinemaOrderNum.xhtml")
	public String changeCinemaOrderNum(String id, Integer ordernum, ModelMap model) {
		if (StringUtils.isBlank(id) || ordernum == null)
			return showJsonError(model, "参数错误！");
		Map map = mongoService.findOne(MongoData.NS_VOUCHERCARD_CINEMA, MongoData.SYSTEM_ID, id);
		if (map != null) {
			map.put(MongoData.ACTION_ORDERNUM, ordernum);
			mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_VOUCHERCARD_CINEMA);
			return showJsonSuccess(model);
		}
		return showJsonError(model, "参数错误！");
	}

	// 删除指定影院信息
	@RequestMapping("/admin/content/sales/delCardCinema.xhtml")
	public String delCardCinema(String id, ModelMap model) {
		if (StringUtils.isBlank(id))
			return showJsonError(model, "数据错误！");
		mongoService.removeObjectById(MongoData.NS_VOUCHERCARD_CINEMA, MongoData.SYSTEM_ID, id);
		return showJsonSuccess(model);
	}
}
