package com.gewara.web.action.admin.pay;

import java.sql.Timestamp;
import java.util.ArrayList;
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

import com.gewara.api.pay.request.GatewayGetAllRequest;
import com.gewara.api.pay.response.GatewayGetAllResponse;
import com.gewara.api.pay.service.GatewayApiService;
import com.gewara.model.acl.User;
import com.gewara.model.common.City;
import com.gewara.model.common.Province;
import com.gewara.model.pay.PayCityMerchant;
import com.gewara.model.pay.PayGateway;
import com.gewara.model.pay.PayGatewayBank;
import com.gewara.model.pay.PayInterfaceSwitch;
import com.gewara.model.pay.PayMerchant;
import com.gewara.service.pay.GatewayService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class PayGatewayController extends BaseAdminController {
	
	@Autowired
	private GatewayService gatewayService;
	
	@Autowired@Qualifier("gatewayApiService")
	private GatewayApiService gatewayApiService;


	/**
	 * 同步支付网关数据
	 * 
	 * @param model
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Oct 17, 2013 6:12:27 PM
	 */
	@RequestMapping("/admin/pay/gateway/syn.xhtml")
	public String synGateway(ModelMap model){		
		GatewayGetAllRequest request = new GatewayGetAllRequest();
		GatewayGetAllResponse response = gatewayApiService.getAll(request);
		try {
			if(!response.isSuccess()){
				return showJsonError(model, "同步API返回失败：" + response.getMsg());
			}
			gatewayService.synAllGateway(response.getGatewayList());			
			return showJsonSuccess(model);
		} catch (Exception e) {
			return showJsonError(model, "同步失败，发生异常！");
        }
	}
	
	@RequestMapping("/admin/pay/gateway/list.xhtml")
	public String getGatewayList(ModelMap model){		
		dbLogger.warn("start getGatewayList...");
		List<PayGateway> gatewayList = daoService.getObjectList(PayGateway.class, "id", true, 0, 100);		
		model.put("gatewayList", gatewayList);
		return "admin/pay/gateway/gatewayList.vm";
	}
	
	/**
	 * 设置商户号路由状态
	 * 
	 * @param gwid
	 * @param routeStatus
	 * @param model
	 * @return
	 *
	 * @author leo.li
	 * Modify Time Oct 17, 2013 1:58:55 PM
	 */
	@RequestMapping("/admin/pay/gateway/setRouteStatus.xhtml")
	public String setRouteStatus(Long gwid,String routeStatus,ModelMap model){
		if(gwid == null){
			return showJsonError(model, "缺少参数！");
		}
		
		PayGateway gateway = daoService.getObject(PayGateway.class, gwid);
		if(gateway == null){
			return showJsonError(model, "您要查询的支付网关不存在！");
		}
		if(StringUtils.equals(PayGateway.ROUTE_TYPE_NONE, routeStatus) || StringUtils.equals(PayGateway.ROUTE_TYPE_CITY, routeStatus) || StringUtils.equals(PayGateway.ROUTE_TYPE_MERCODE, routeStatus)){
			gateway.setRouteStatus(routeStatus);
			User user = getLogonUser();
			gateway.setModifyTime(new Timestamp(System.currentTimeMillis()));
			gateway.setModifyUser(user.getUsername());
			daoService.saveObject(gateway);
		}else{
			return showJsonError(model, "参数不正确！");
		}
		
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/pay/gateway/merchant/list.xhtml")
	public String getMerchantList(Long gwid,ModelMap model){
		if(gwid == null){
			return forwardMessage(model, "缺少参数！");
		}
		
		PayGateway gateway = daoService.getObject(PayGateway.class, gwid);
		if(gateway == null){
			return forwardMessage(model, "您要查询的支付网关不存在");
		}
		
		List<PayMerchant> merchantList = daoService.getObjectListByField(PayMerchant.class, "gatewayId", gwid);
		model.put("gateway", gateway);
		model.put("merchantList", merchantList);
		
		return "admin/pay/gateway/merchantList.vm";
	}
	
	@RequestMapping("/admin/pay/gateway/merchant/setDefault.xhtml")
	public String setMerchantDefault(Long id,String defautlt,ModelMap model){
		if(id == null){
			return showJsonError(model, "缺少参数！");
		}
		
		PayMerchant merchant = daoService.getObject(PayMerchant.class, id);
		if(merchant == null){
			return showJsonError(model, "您要查询的商户不存在！");
		}
		if(StringUtils.equals(PayMerchant.DEFAULT_Y, defautlt) || StringUtils.equals(PayMerchant.DEFAULT_N, defautlt)){
			merchant.setIsDefault(defautlt);
			User user = getLogonUser();
			merchant.setModifyTime(new Timestamp(System.currentTimeMillis()));
			merchant.setModifyUser(user.getUsername());
			daoService.saveObject(merchant);
		}else{
			return showJsonError(model, "参数不正确！");
		}
		
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/pay/gateway/bank/list.xhtml")
	public String getBankList(Long gwid,ModelMap model){
		if(gwid == null){
			return forwardMessage(model, "缺少参数！");
		}
		
		PayGateway gateway = daoService.getObject(PayGateway.class, gwid);
		if(gateway == null){
			return forwardMessage(model, "您要查询的支付网关不存在");
		}
		Map<String,String> bankTypeKeyMap = StringUtils.isBlank(gateway.getBankTypeKey()) ? new HashMap<String,String>() : JsonUtils.readJsonToMap(gateway.getBankTypeKey());
		
		
		List<PayGatewayBank> bankList = daoService.getObjectListByField(PayGatewayBank.class, "gatewayId", gwid);
		model.put("gateway", gateway);
		model.put("bankList", bankList);
		model.put("bankTypeKeyMap", bankTypeKeyMap);
		
		return "admin/pay/gateway/bankList.vm";
	}
	
	@RequestMapping("/admin/pay/gateway/route/city/list.xhtml")
	public String getCityMerchantList(Long gwid,ModelMap model){
		if(gwid == null){
			return forwardMessage(model, "缺少参数！");
		}
		
		PayGateway gateway = daoService.getObject(PayGateway.class, gwid);
		if(gateway == null){
			return forwardMessage(model, "您要查询的支付网关不存在");
		}		
		
		List<PayCityMerchant> cityMerchantList = daoService.getObjectListByField(PayCityMerchant.class, "gatewayId", gwid);
		model.put("gateway", gateway);
		model.put("cityMerchantList", cityMerchantList);
		
		List<String> provinceCodeList = new ArrayList<String>();
		List<String> cityCodeList = new ArrayList<String>();
		for(PayCityMerchant t : cityMerchantList){
			if(StringUtils.equals(PayCityMerchant.AREATYPE_C, t.getAreaType())){
				cityCodeList.add(t.getAreaCode());
			}else if(StringUtils.equals(PayCityMerchant.AREATYPE_P, t.getAreaType())){
				provinceCodeList.add(t.getAreaCode());
			}
		}		
		List<Province> provinceList = daoService.getObjectList(Province.class, provinceCodeList);
		List<City> cityList = daoService.getObjectList(City.class, cityCodeList);
		model.put("provinceNameMap", BeanUtil.beanListToMap(provinceList, "provincecode", "provincename", true));
		model.put("cityNameMap", BeanUtil.beanListToMap(cityList, "citycode", "cityname", true));
		
		return "admin/pay/gateway/cityMerchantList.vm";
	}
	
	@RequestMapping("/admin/pay/gateway/route/city/addPage.xhtml")
	public String citymerAddPage(Long gwid,ModelMap model){
		if(gwid == null){
			return showJsonError(model, "缺少参数");
		}		
		PayGateway gateway = daoService.getObject(PayGateway.class, gwid);
		if(gateway == null){
			return showJsonError(model, "您要查询的支付网关不存在");
		}
		
		List<Province> provinceList = daoService.getAllObjects(Province.class);
		List<PayMerchant> merchantList = daoService.getObjectListByField(PayMerchant.class, "gatewayId", gwid);
		
		model.put("gateway", gateway);
		model.put("merchantList", merchantList);
		model.put("provinceList", provinceList);		
		
		return "admin/pay/gateway/citymer.vm";
	}
	
	@RequestMapping("/admin/pay/gateway/route/city/get.xhtml")
	public String getCitymer(Long id,ModelMap model){
		if(id == null){
			return showJsonError(model, "缺少参数");
		}
		PayCityMerchant citymer = daoService.getObject(PayCityMerchant.class, id);
		if(citymer == null){
			return showJsonError(model, "您要查看的银行不存在");
		}		
		PayGateway gateway = daoService.getObject(PayGateway.class, citymer.getGatewayId());
		if(gateway == null){
			return showJsonError(model, "您要查询的支付网关不存在");
		}

		List<Province> provinceList = daoService.getAllObjects(Province.class);
		List<PayMerchant> merchantList = daoService.getObjectListByField(PayMerchant.class, "gatewayId", citymer.getGatewayId());

		model.put("currentProvinceCode", citymer.getAreaCode());
		if(StringUtils.equals(PayCityMerchant.AREATYPE_C, citymer.getAreaType())){
			City city = daoService.getObject(City.class, citymer.getAreaCode());
			List<City> cityList = daoService.getObjectListByField(City.class, "province", city.getProvince());
			model.put("cityList", cityList);
			model.put("currentCityCode", city.getCitycode());
			model.put("currentProvinceCode", city.getProvince().getProvincecode());
		}
				
		model.put("gateway", gateway);
		model.put("citymer", citymer);
		model.put("merchantList", merchantList);
		model.put("provinceList", provinceList);

		return "admin/pay/gateway/citymer.vm";
	}
	
	@RequestMapping("/admin/pay/gateway/route/city/save.xhtml")
	public String saveCitymer(Long id,HttpServletRequest request,ModelMap model){
		PayCityMerchant citymer = null;
		User user = getLogonUser();
		if(id == null){
			citymer = new PayCityMerchant(user.getUsername());
		}else{
			citymer = daoService.getObject(PayCityMerchant.class, id);
			if(citymer == null){
				return showJsonError(model, "您要修改的银行不存在");
			}
			citymer.setModifyTime(new Timestamp(System.currentTimeMillis()));
			citymer.setModifyUser(user.getUsername());
		}
		BindUtils.bindData(citymer, request.getParameterMap());
		String provinceCode = request.getParameter("provinceCode");
		String cityCode = request.getParameter("cityCode");
		if(StringUtils.equals(PayCityMerchant.AREATYPE_C, citymer.getAreaType())){
			citymer.setAreaCode(cityCode);
		}else if(StringUtils.equals(PayCityMerchant.AREATYPE_P, citymer.getAreaType())){
			citymer.setAreaCode(provinceCode);
		}
		
		if(citymer.getGatewayId() == null){
			return showJsonError(model, "支付网关不可以为空");
		}
		if(StringUtils.isBlank(citymer.getAreaType())){
			return showJsonError(model, "区域类型不可以为空");
		}
		if(StringUtils.isBlank(citymer.getAreaCode())){
			return showJsonError(model, "省或城市不可以为空");
		}
		if(StringUtils.isBlank(citymer.getMerchantCode())){
			return showJsonError(model, "商户号标识不可以为空");
		}
		
		PayGateway gateway = daoService.getObject(PayGateway.class, citymer.getGatewayId());
		if(gateway == null){
			return showJsonError(model, "您要查询的支付网关不存在");
		}
		daoService.saveObject(citymer);
				
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/pay/gateway/route/city/del.xhtml")
	public String delCitymer(Long id,ModelMap model){
		PayCityMerchant citymer = daoService.getObject(PayCityMerchant.class, id);
		if(citymer == null){
			return showJsonError(model, "您要删除的信息不存在！");
		}
		daoService.removeObject(citymer);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/pay/gateway/city/list.xhtml")
	public String getCityList(String provinceCode,ModelMap model){
		List<City> cityList = daoService.getObjectListByField(City.class, "province", new Province(provinceCode));
		String json = JsonUtils.writeObjectToJson(cityList);
		return showJsonSuccess(model, json, "data");
	}
	
	
	@RequestMapping("/admin/pay/gateway/switch/list.xhtml")
	public String listSwitch(ModelMap model){
		List<PayInterfaceSwitch> switchList = daoService.getAllObjects(PayInterfaceSwitch.class);
		List<PayGateway> gatewayList = daoService.getAllObjects(PayGateway.class);
		Map<String,PayGateway> gatewayMap = BeanUtil.beanListToMap(gatewayList, "gatewayCode");
		
		model.put("switchList", switchList);
		model.put("gatewayMap", gatewayMap);
		
		return "admin/pay/gateway/switchList.vm";
	}
	
	@RequestMapping("/admin/pay/gateway/switch/addPage.xhtml")
	public String switchAddPage(ModelMap model){
		List<PayGateway> gatewayList = daoService.getObjectListByField(PayGateway.class, "status", PayGateway.STATUS_IN_USE);
		List<PayInterfaceSwitch> switchList = daoService.getAllObjects(PayInterfaceSwitch.class);
		for(PayInterfaceSwitch s : switchList){
			for(PayGateway p : gatewayList){
				if(StringUtils.equals(s.getGatewayCode(), p.getGatewayCode())){
					gatewayList.remove(p);
					break;
				}
			}
		}
		
		model.put("gatewayList", gatewayList);
		return "admin/pay/gateway/switch.vm";
	}
	
	@RequestMapping("/admin/pay/gateway/switch/save.xhtml")
	public String saveSwitch(String gatewayCode,ModelMap model){
		if(StringUtils.isBlank(gatewayCode)){
			return showJsonError(model, "参数不可以为空");
		}
		List<PayGateway> gatewayList = daoService.getObjectListByField(PayGateway.class, "gatewayCode", gatewayCode);
		if(gatewayList == null || gatewayList.isEmpty()){
			return showJsonError(model, "该支付网关不存在！");
		}
		
		PayInterfaceSwitch interfaceSwitch = daoService.getObject(PayInterfaceSwitch.class, gatewayCode);
		if(interfaceSwitch != null){
			return showJsonError(model, "已存在！");
		}

		User user = getLogonUser();
		interfaceSwitch = new PayInterfaceSwitch(gatewayCode, user.getUsername());
		daoService.saveObject(interfaceSwitch);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/pay/gateway/switch/del.xhtml")
	public String delSwitch(String gatewayCode,ModelMap model){
		PayInterfaceSwitch interfaceSwitch = daoService.getObject(PayInterfaceSwitch.class, gatewayCode);
		if(interfaceSwitch == null){
			return showJsonError(model, "您要删除的信息不存在！");
		}
		daoService.removeObject(interfaceSwitch);
		return showJsonSuccess(model);
	}
}
