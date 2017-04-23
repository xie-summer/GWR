package com.gewara.web.action.admin.sys;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.untrans.HisDataService;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class HisDataAdminController extends BaseAdminController{
	@Autowired@Qualifier("hisDataService")
	private HisDataService hisDataService;
	@RequestMapping("/admin/sysmgr/hisdata/backPoint.xhtml")
	@ResponseBody
	public synchronized String backPoint(Integer pages){
		int count = 0;
		if(pages==null){
			count = hisDataService.backupPointHist();
		}else{
			for(int i=0;i<pages;i++){
				count += hisDataService.backupPointHist();
			}
		}
		return "success:" + count;
	}
	@RequestMapping("/admin/sysmgr/hisdata/backSms.xhtml")
	@ResponseBody
	public synchronized String backSms(Integer pages){
		int count = 0;
		if(pages==null){
			count = hisDataService.backupSMSRecordHist();
		}else{
			for(int i=0;i<pages;i++){
				count +=hisDataService.backupSMSRecordHist();
			}
			dbLogger.warn("page:" + pages + "TOTAL:" + count);
		}
		return "success:" + count;
	}
	@RequestMapping("/admin/sysmgr/hisdata/qrySmsList.xhtml")
	@ResponseBody
	public String qrySmsList(String mobile){
		List<Map<String, String>> result = hisDataService.getHisSmsList(mobile);
		return result.size() + ":" + result;
	}
	
	@RequestMapping("/admin/sysmgr/hisdata/backOrder.xhtml")
	@ResponseBody
	public synchronized String backOrder(Integer days){
		int count = 0;
		if(days==null){
			count = hisDataService.backupOrder();
		}else{
			for(int i=0;i<days;i++){
				count += hisDataService.backupOrder();
			}
		}
		return "success:" + count;
	}
}
