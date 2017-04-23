package com.gewara.web.action.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.sys.MongoData;
import com.gewara.model.common.City;
import com.gewara.model.common.County;
import com.gewara.model.common.Province;
import com.gewara.mongo.MongoService;
import com.gewara.service.DaoService;
import com.gewara.util.BeanUtil;
import com.gewara.util.WebUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;
@Controller
public class AdvertController {

	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService){
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService){
		this.daoService = daoService;
	}
	//兑换券首页
	@RequestMapping("/subject/advertIndex.xhtml")
	public String advertIndex(HttpServletRequest request, HttpServletResponse response, ModelMap model){
		String citycode = WebUtils.getAndSetDefault(request, response);
		City curCity = daoService.getObject(City.class, citycode);
		model.put("curCity", curCity);
		Map params = new HashMap();
		BasicDBObject query = new BasicDBObject();
		query.put(QueryOperators.GT, 0);
		params.put(MongoData.ACTION_ORDERNUM, query);
		List citycodeList = mongoService.getDistinctPropertyList(MongoData.NS_VOUCHERCARD_CINEMA, params, "citycode"); 
		Collections.sort(citycodeList);
		List<City> cityList = daoService.getObjectList(City.class, citycodeList);
		model.put("cityList", cityList);
		List<Province> provinceList = BeanUtil.getBeanPropertyList(cityList, Province.class, "province", true);
		model.put("provinceList", provinceList);
		//票券种类
		List<Map> voucherCardList = mongoService.find(MongoData.NS_VOUCHERCARD_TYPE,params,MongoData.DEFAULT_ID_NAME,true);
		model.put("voucherCardList", voucherCardList);
		//查询区
		Map countyPram = new HashMap();
		countyPram.put(MongoData.ACTION_ORDERNUM, query);
		List countycodeList = mongoService.getDistinctPropertyList(MongoData.NS_VOUCHERCARD_CINEMA, countyPram, "countycode");
		List<County> countyList = daoService.getObjectList(County.class, countycodeList);
		model.put("countyList", countyList);
		//查询影院
		Map cinemaPram = new HashMap();
		cinemaPram.put(MongoData.ACTION_ORDERNUM, query);
		List<Map> cinemaList = mongoService.find(MongoData.NS_VOUCHERCARD_CINEMA, cinemaPram, MongoData.ACTION_ORDERNUM, true);
		model.put("cinemaList", cinemaList);
		model.put("citycode", citycode);
		return "footer/quanmaket.vm";
	}
	@RequestMapping("/subject/groupmaket.xhtml")
	public String groupmaket(){
		return "footer/groupmaket.vm";
	}
}
