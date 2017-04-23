package com.gewara.api.service.common;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.api.vo.ResultCode;
import com.gewara.api.vo.common.GewaCityVo;
import com.gewara.model.common.GewaCity;
import com.gewara.service.GewaCityService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.util.VoCopyUtil;

public class CommonVoServiceImpl extends BaseServiceImpl implements CommonVoService {
	@Autowired@Qualifier("gewaCityService")
	private GewaCityService gewaCityService;
	
	@Override
	public ResultCode<List<GewaCityVo>> getAllGewaCity() {
		Map<GewaCity, List<GewaCity>> proMap = gewaCityService.getAdmCityMap();
		List<GewaCity> cityList = new LinkedList<GewaCity>();
		for(GewaCity pro : proMap.keySet()){
			cityList.addAll(proMap.get(pro));
		}
		return VoCopyUtil.copyListProperties(GewaCityVo.class, cityList);
	}
}
