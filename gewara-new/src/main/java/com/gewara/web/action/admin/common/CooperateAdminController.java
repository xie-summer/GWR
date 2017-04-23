/**
 * 
 */
package com.gewara.web.action.admin.common;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import com.gewara.constant.sys.MongoData;
import com.gewara.mongo.MongoService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.mongodb.DBObject;

@Controller
public class CooperateAdminController extends BaseAdminController{
	public static final Integer MAX = 24;
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("gewaMultipartResolver")
	private MultipartResolver gewaMultipartResolver;

	@RequestMapping("/admin/common/upLoadCCBcardbinList.xhtml")
	public String uploadCCBCardBin(HttpServletRequest request,ModelMap model){
		MultipartHttpServletRequest multipartRequest = gewaMultipartResolver.resolveMultipart(request);
		MultipartFile multipartFile = multipartRequest.getFileMap().get("ccbCardBinFile");
		if(multipartFile == null){
			return this.show404(model, "上传文件出错，请重新上传!");
		}
		try {
			byte[] bytes = multipartFile.getBytes();
			String[] cardBinList = new String(bytes).split("\r\n");
			for(String bin : cardBinList){
				String[] bins = StringUtils.split(bin, "****");
				if(bins.length == 2 && bins[0].trim().length() == 8 && bins[1].trim().length() == 4){
					Map<String, String> map = new HashMap<String, String>();
					map.put("_id", bins[0].trim() + bins[1].trim());
					map.put("prefixbin", bins[0].trim());
					map.put("suffixbin", bins[1].trim());
					mongoService.saveOrUpdateMap(map, "_id", MongoData.NS_CCBPOS_CARDBIN_2013);
				}
			}
		} catch (Exception e) {
			return this.show404(model, "上传文件出错，请检查文件格式重新上传!");
		}
		model.put("pageNo", 0);
		return "redirect:/admin/common/ccbCardbinList84.xhtml";
	}
	
	@RequestMapping("/admin/common/ccbCardbinList84.xhtml")
	public String ccbCardBinList(ModelMap model,Integer pageNo){
		if(pageNo==null) pageNo = 0;
		model.put("qryMapList",mongoService.find(MongoData.NS_CCBPOS_CARDBIN_2013,  new HashMap(), pageNo * 150, 150));
		PageUtil pageUtil = new PageUtil( mongoService.getCount(MongoData.NS_CCBPOS_CARDBIN_2013) ,150, pageNo,"admin/common/ccbCardbinList84.xhtml", true, true);
		pageUtil.initPageInfo(new HashMap());
		model.put("pageUtil", pageUtil);
		return "admin/cooperate/ccbCardbin84.vm";
	}
	@RequestMapping("/admin/common/removeCCBCardBin84.xhtml")
	public String removeunionpayFastCardbin(String id, ModelMap model){
		Map tmpMap = mongoService.findOne(MongoData.NS_CCBPOS_CARDBIN_2013, "_id", id);
		if(tmpMap==null){
			return showJsonError(model, "记录不存在，请不要进行删除操作！");
		}
		mongoService.removeObjectById(MongoData.NS_CCBPOS_CARDBIN_2013, "_id", id);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/common/ccbcardbinList.xhtml")
	public String ccbcardbinList(ModelMap model){
		List<Map> qryMapList = mongoService.find(MongoData.NS_CCBPOS_CARDBIN, new HashMap());
		model.put("qryMapList", qryMapList);
		return "admin/cooperate/ccbcardbin.vm";
	}
	@RequestMapping("/admin/common/saveCcbcardbin.xhtml")
	public String ccbcardbin(String cardbin, ModelMap model){
		Map tmpMap = mongoService.findOne(MongoData.NS_CCBPOS_CARDBIN, "cardbin", cardbin);
		if(tmpMap!=null){
			return showJsonError(model, "记录已经存在，请不要重复添加！");
		}
		String bins[] = StringUtils.split(cardbin, ",");
		for(String bin : bins){
			if(StringUtils.length(cardbin)==6){
				Map<String, String> map = new HashMap<String, String>();
				map.put("cardbin", bin);
				mongoService.saveOrUpdateMap(map, "cardbin", MongoData.NS_CCBPOS_CARDBIN);
			}
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/common/delccbposActivity.xhtml")
	public String delccbposActivity(Timestamp addtime, ModelMap model){
		Timestamp curtime = DateUtil.getMillTimestamp();
		Timestamp time1 = DateUtil.getBeginningTimeOfDay(curtime);
		if(addtime.after(time1)){
			return forwardMessage(model, "日期不能使今天！");
		}
		DBObject query = mongoService.queryAdvancedDBObject("addtime", new String[]{"<"}, new Object[]{DateUtil.formatTimestamp(addtime)});
		mongoService.removeObjectList(MongoData.NS_CCBPOS_ACTIVITY, query);
		return forwardMessage(model, "删除成功！");
	}
	@RequestMapping("/admin/common/unionpayFastCardbinList.xhtml")
	public String unionpayFastCardbinList(String unionPayBank,ModelMap model){
		if(StringUtils.isBlank(unionPayBank)){
			unionPayBank = "ICBC";
		}
		List<Map> qryMapList = mongoService.find(MongoData.NS_UNIONPAYFAST_CARDBIN + ":" + unionPayBank, new HashMap());
		model.put("qryMapList", qryMapList);
		model.put("unionPayBank", unionPayBank);
		model.put(unionPayBank, "selected='selected'");
		model.put("payName",mongoService.findOne(MongoData.NS_UNIONPAYFAST_CARDBIN + "Name:" + unionPayBank, "_id", "payName"));
		return "admin/cooperate/unionpayFastcardbin.vm";
	}
	@RequestMapping("/admin/common/saveunionpayFastCardbin.xhtml")
	public String saveunionpayFastCardbin(String cardbin,String unionPayBank, ModelMap model){
		Map tmpMap = mongoService.findOne(MongoData.NS_UNIONPAYFAST_CARDBIN + ":" + unionPayBank, "cardbin", cardbin);
		if(tmpMap!=null){
			return showJsonError(model, "记录已经存在，请不要重复添加！");
		}
		String bins[] = StringUtils.split(cardbin, ",");
		for(String bin : bins){			
			Map<String, String> map = new HashMap<String, String>();
			map.put("cardbin", bin.trim());
			mongoService.saveOrUpdateMap(map, "cardbin", MongoData.NS_UNIONPAYFAST_CARDBIN + ":" + unionPayBank);
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/common/removeunionpayFastCardbin.xhtml")
	public String removeunionpayFastCardbin(String cardbin,String unionPayBank, ModelMap model){
		Map tmpMap = mongoService.findOne(MongoData.NS_UNIONPAYFAST_CARDBIN + ":" + unionPayBank, "cardbin", cardbin);
		if(tmpMap==null){
			return showJsonError(model, "记录不存在，请不要进行删除操作！");
		}
		mongoService.removeObjectById(MongoData.NS_UNIONPAYFAST_CARDBIN + ":" + unionPayBank, "cardbin", cardbin);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/common/saveunionpayFastPayName.xhtml")
	public String saveunionpayFastPayName(String payName,String unionPayBank, ModelMap model){
		Map tmpMap = mongoService.findOne(MongoData.NS_UNIONPAYFAST_CARDBIN + "Name:" + unionPayBank, "_id", "payName");
		if(tmpMap == null){
			tmpMap = new HashMap<String, String>();
			tmpMap.put("_id", "payName");
		}
		tmpMap.put("payName", payName);
		mongoService.saveOrUpdateMap(tmpMap, "_id", MongoData.NS_UNIONPAYFAST_CARDBIN + "Name:" + unionPayBank);
		return showJsonSuccess(model);
	}
}
