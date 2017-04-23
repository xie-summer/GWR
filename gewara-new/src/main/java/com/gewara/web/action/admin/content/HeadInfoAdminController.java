package com.gewara.web.action.admin.content;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.CityData;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.HeadInfo;
import com.gewara.util.BeanUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class HeadInfoAdminController extends BaseAdminController {
	//查询头部信息列表
	@RequestMapping("/admin/site/header/headInfoList.xhtml")
	public String getHeadInfoList(String board, ModelMap model,Integer pageNo, HttpServletRequest request){
		if(pageNo==null) pageNo=0;
		int rowsPerPage=20;
		String signName = SignName.INDEX_HEADINFO;
		if(StringUtils.equals(board, TagConstant.TAG_MOVIE + "_new")){
			signName = SignName.INDEX_HEADINFO_NEW;
		}else if(StringUtils.equals(board, TagConstant.TAG_DRAMA)){
			signName = SignName.DRAMA_HEADINFO;
		}else if(StringUtils.equals(board, SignName.MOVIE_HEADINFO)){
			signName = board;
		}else if(StringUtils.equals(board, TagConstant.TAG_SPORT)){
			signName = SignName.SPORT_HEADINFO;
		}else if(StringUtils.equals(board, TagConstant.TAG_MOVIE + "_auto")){
			signName = SignName.INDEX_HEADINFO_AUTO;
		}
		Integer iNum = commonService.getGewaCommendCount(getAdminCitycode(request), signName, null, HeadInfo.TAG, false);
		List<HeadInfo> headInfoList=commonService.getHeadInfoList(board, getAdminCitycode(request), pageNo*rowsPerPage, rowsPerPage);
		List<GewaCommend> gcHeadList = commonService.getGewaCommendList(getAdminCitycode(request), signName, null, HeadInfo.TAG, true, 0, iNum);
		if(!gcHeadList.isEmpty()){
			HeadInfo headInfo = daoService.getObject(HeadInfo.class, gcHeadList.get(0).getRelatedid());
			model.put("headInfo", headInfo);
		}
		List gcHeadStrList = new ArrayList();
		for(GewaCommend gewaObj : gcHeadList){
			gcHeadStrList.add(gewaObj.getRelatedid());
		}
		Map gcHeadMap = BeanUtil.beanListToMap(gcHeadList, "relatedid");
		int count=commonService.getHeadInfoCount(board);
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"admin/site/header/headInfoList.xhtml");
		pageUtil.initPageInfo();
		model.put("gcHeadStrList", gcHeadStrList);
		model.put("gcHeadMap", gcHeadMap);
		model.put("pageUtil", pageUtil);
		model.put("headInfoList", headInfoList);
		model.put("admincitycode", this.getAdminCitycode(request));
		return "admin/site/header/headInfoList.vm";
	}
	
	//修改排序
	@RequestMapping("/admin/site/header/updateHeadInfoOrderNum.xhtml")
	public String updateHeadInfoOrderNum(ModelMap model,Long hid, String tag, String board,Timestamp starttime, Timestamp stoptime, HttpServletRequest request){
		String citycode = getAdminCitycode(request);
		String signName = SignName.INDEX_HEADINFO;
		if(StringUtils.equals(board, TagConstant.TAG_MOVIE + "_new")){
			signName = SignName.INDEX_HEADINFO_NEW;
		}else if(StringUtils.equals(board, TagConstant.TAG_DRAMA)){
			signName = SignName.DRAMA_HEADINFO;
		}else if(StringUtils.equals(board, SignName.MOVIE_HEADINFO)){
			signName = board;
		}else if(StringUtils.equals(board, TagConstant.TAG_SPORT)){
			signName = SignName.SPORT_HEADINFO;
		}else if(StringUtils.equals(board, TagConstant.TAG_MOVIE + "_auto")){
			signName = SignName.INDEX_HEADINFO_AUTO;
		}
		if(StringUtils.equals(tag, "1")){
			HeadInfo headInfo = daoService.getObject(HeadInfo.class, hid);
			if(headInfo==null) return showJsonError(model, "对象不存在,请核实对象标识、ID");
			if(stoptime == null) return showJsonError(model, "结束时间不能为空！");
			if(starttime == null) return showJsonError(model,"开始时间不能为空！");
			if(starttime.after(stoptime)) return showJsonError(model,"开始时间需小于结束时间！"); 
			Integer iNum = commonService.getGewaCommendCount(getAdminCitycode(request), signName, null, HeadInfo.TAG, false);
			List<GewaCommend> gcHeadList = commonService.getGewaCommendList(getAdminCitycode(request), signName, null, HeadInfo.TAG, true, 0, iNum);
			Timestamp endTime = null;
			Timestamp gewaStarttime = null;
			for(GewaCommend gewaObj : gcHeadList){
				endTime = gewaObj.getStoptime();
				gewaStarttime = gewaObj.getStarttime();
				if((endTime.after(starttime)&&starttime.after(gewaStarttime))||starttime.equals(gewaStarttime)
					){
					return showJsonError(model,"开始时间设置有冲突，请检查其它使用中数据的时间设置是否有问题！");
				}
			}
			GewaCommend gewaCommend = new GewaCommend(signName, null, hid, 1);
			gewaCommend.setCitycode(citycode);
			gewaCommend.setTag(HeadInfo.TAG);
			gewaCommend.setStoptime(stoptime);
			gewaCommend.setStarttime(starttime);
			daoService.saveObject(gewaCommend);
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/site/header/cancleOp.xhtml")
	public String cancleOp(ModelMap model,Long hid){
		if(hid==null) return showJsonError(model,"未选择记录！");
		List<GewaCommend> gcHeadList = daoService.getObjectListByField(GewaCommend.class, "relatedid", hid);
		daoService.removeObjectList(gcHeadList);
		return showJsonSuccess(model);
	}
	
	
	//删除头部信息
	@RequestMapping("/admin/site/header/deleteHeaderInfo.xhtml")
	public String deleteHeaderInfo(ModelMap model,Long hiid, String board){
		if(hiid==1l||hiid==2l){
			return showJsonError(model,"默认头图不能删除！");
		}
		String signName = SignName.INDEX_HEADINFO;
		if(StringUtils.equals(board, TagConstant.TAG_DRAMA)){
			signName = SignName.DRAMA_HEADINFO;
		}else if(StringUtils.equals(board, SignName.MOVIE_HEADINFO)){
			signName = board;
		}else if(StringUtils.equals(board, TagConstant.TAG_SPORT)){
			signName = SignName.SPORT_HEADINFO;
		}
		HeadInfo hi=daoService.getObject(HeadInfo.class, hiid);		
		if(hi!=null){
			GewaCommend gc = commonService.getGewaCommendByRelatedid(signName, hi.getId());
			if(gc!=null) return showJsonError(model, "头图正在使用不能删除！");
			daoService.removeObject(hi);
			return showJsonSuccess(model,"删除成功！");
		}else{
			return showJsonError(model, "删除失败！");
		}
	}
	
	@RequestMapping("/admin/site/header/headinfoShareCitys.xhtml")
	public String dataShareCitys(Long hid, String board, ModelMap model, HttpServletRequest request){
		model.put("hid", hid);
		model.put("cityMap", CityData.getOtherCityNames());
		model.put("board", board);
		
		List<GewaCommend> gcHeadList = commonService.getGewaCommendListByRelatedid(null, SignName.INDEX_HEADINFO, hid, HeadInfo.TAG, true, 0, 100);
		Set<String> tmpSet = new HashSet<String>();
		for(GewaCommend commend : gcHeadList){
			tmpSet.add(commend.getCitycode());
		}
		List<String> selcitycode = new ArrayList<String>(tmpSet);
		model.put("selcitycode", selcitycode);
		model.put("citycode", getAdminCitycode(request));
		
		return "admin/site/header/headinfoShareCitys.vm";
	}
	@RequestMapping("/admin/site/header/saveheadinfoShareCitys.xhtml")
	public String saveheadinfoShareCitys(Long hid, String relatecityAll, String relatecity, String board, Timestamp stoptime, ModelMap model){
		HeadInfo headInfo = daoService.getObject(HeadInfo.class, hid);
		if(headInfo==null) return showJsonError(model, "对象不存在,请核实对象标识、ID");
		GewaCommend gewaCommend = new GewaCommend(SignName.INDEX_HEADINFO, null, hid, 1);
		gewaCommend.setCitycode(headInfo.getCitycode());
		gewaCommend.setStoptime(stoptime);
		gewaCommend.setTag(HeadInfo.TAG);
		daoService.saveObject(gewaCommend);
		List<String> citycodes = null;
		if(StringUtils.isBlank(relatecity)){
			Map<String, String> otherCityNamesMap = CityData.getOtherCityNames();
			citycodes = new ArrayList<String>(otherCityNamesMap.keySet());
		}else citycodes = Arrays.asList(StringUtils.split(relatecity, ","));
		if(StringUtils.equals(relatecityAll, "320600")){
			for(String citycode : citycodes){
				List<GewaCommend> gcHeadList = commonService.getGewaCommendList(citycode, SignName.INDEX_HEADINFO, null, HeadInfo.TAG, true, 0, 1);
				daoService.removeObjectList(gcHeadList);
			}
		}else if(StringUtils.equals(relatecityAll, AdminCityContant.CITYCODE_ALL)){
			if(stoptime == null) return showJsonError(model, "结束时间不能为空！");
			for(String citycode : citycodes){
				HeadInfo headInfo2 = new HeadInfo(headInfo.getTitle());
				headInfo2.setLogobig(headInfo.getLogobig());
				headInfo2.setLogosmall(headInfo.getLogosmall());
				headInfo2.setCss(headInfo.getCss());
				headInfo2.setLink(headInfo.getLink());
				headInfo2.setCitycode(citycode);
				headInfo2.setBoard(board);
				daoService.saveObject(headInfo2);
			}
		}else return showJsonError(model, "请选择操作类型！");
		return showJsonSuccess(model);
	}
}
