package com.gewara.web.action.admin.content;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.Config;
import com.gewara.model.acl.User;
import com.gewara.model.content.AdPosition;
import com.gewara.model.content.Advertising;
import com.gewara.service.content.AdService;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.untrans.GewaPicService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class AdAdminController extends BaseAdminController {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	public void setGewaPicService(GewaPicService gewaPicService) {
		this.gewaPicService = gewaPicService;
	}
	@Autowired@Qualifier("adService")
	private AdService adService;
	public void setAdService(AdService adService) {
		this.adService = adService;
	}
	@RequestMapping("/admin/site/ad/adPositionList.xhtml")
	public String qaList(ModelMap model, String tag, Integer pageNo) throws Exception {
		if (pageNo == null)
			pageNo = 0;
		Integer rowsPerPage = 15;
		Integer count = adService.getAdPositionCountByTag(tag);
		PageUtil pageUtil = new PageUtil(count, 15, pageNo, "admin/site/ad/adPositionList.xhtml");
		pageUtil.initPageInfo();
		List<AdPosition> adPositionList = adService.getAdPositionListByTag(tag, pageNo * rowsPerPage, rowsPerPage);
		model.put("pageUtil", pageUtil);
		model.put("adPositionList", adPositionList);
		return "admin/site/ad/adPositionList.vm";
	}

	@RequestMapping("/admin/site/ad/adversitingList.xhtml")
	public String qaList(ModelMap model, HttpServletRequest request, @RequestParam("adpid")Long adpid){
		List<Advertising> adList = adService.getAdvertisingListByAdPositionid(getAdminCitycode(request),adpid, "ordernum");
		model.put("adList", adList);
		return "admin/site/ad/adList.vm";
	}

	@RequestMapping("/admin/site/ad/export.xhtml")
	public void export(Long adpid, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String[] str = { "所属版块","广告标题", "广告类型", "广告链接", "开始时间", "结束时间", "逻辑位置" };
		List<Advertising> adList = new ArrayList<Advertising>();
		if (adpid != null) {
			adList = adService.getAdvertisingListByAdPositionid(getAdminCitycode(request),adpid,"ordernum");
		} else {
			DetachedCriteria query = DetachedCriteria.forClass(Advertising.class);
			query.addOrder(Order.asc("ordernum"));
			adList = readOnlyTemplate.findByCriteria(query);
		}
		// 创建工作本
		HSSFWorkbook wb = new HSSFWorkbook();
		// 创建表
		HSSFSheet sheet = wb.createSheet("广告数据");
		// 设置表格的宽度
		sheet.setDefaultColumnWidth(15);
		// 创建表头
		HSSFHeader header = sheet.getHeader();
		// 设置标题
		header.setCenter("广告表");
		HSSFRow row = sheet.createRow(0);
		// 逐一设置Title的值
		for (int i = 0; i < str.length; i++) {
			HSSFCell headerCell = row.createCell(i);
			headerCell.setCellValue(new HSSFRichTextString(str[i]));
		}
		int rownum = 1;
		for (Advertising ad : adList) {
			AdPosition pos = daoService.getObject(AdPosition.class, ad.getAdpositionid());
			row = sheet.createRow(rownum);
			rownum++;
			HSSFCell cell = row.createCell(0);
			cell.setCellValue(pos.getTag());
			cell = row.createCell(1);
			cell.setCellValue(ad.getTitle());
			cell = row.createCell(2);
			cell.setCellValue(ad.getAdtype());
			cell = row.createCell(3);
			cell.setCellValue(ad.getLink());
			cell = row.createCell(4);
			cell.setCellValue(DateUtil.format(ad.getStarttime(),"yyyy-MM-dd"));
			cell = row.createCell(5);
			cell.setCellValue(DateUtil.format(ad.getEndtime(),"yyyy-MM-dd"));
			cell = row.createCell(6);
			cell.setCellValue(ad.getLogicaldir());
		}
		wb.write(response.getOutputStream());
	}
	@RequestMapping("/admin/site/ad/upload.xhtml")
	public String qaList() throws Exception {
		return "admin/site/ad/upload.vm";
	}
	@RequestMapping("/admin/site/ad/uploadfile.xhtml")
	public String saveCompany(String paramchk, String invalidFile, String successFile, ModelMap model) throws IOException {
		String mycheck = StringUtil.md5(successFile + config.getString("uploadKey"));
		if(!mycheck.equals(paramchk)) return showError(model, "校验错误");
		String[] fileList = StringUtils.split(invalidFile, "@@");
		if(fileList.length>0) return showMessage(model, "上传文件不合法");
		fileList = StringUtils.split(successFile, "@@");
		String uploadPath = "images/ad/";
		User user = getLogonUser();
		gewaPicService.moveRemoteTempListTo(user.getId(), "ad", null, uploadPath, fileList);//将文件移动到正式文件夹
		return showMessage(model, "上传文件成功,文件路径: " +"images/ad/"+fileList[0]);
	}
	public static boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	@RequestMapping("/admin/site/ad/popupload.xhtml")
	public String processFile(String paramchk, String successFile, String adname, ModelMap model) throws IOException{
		String mycheck = StringUtil.md5(successFile + config.getString("uploadKey"));
		if(!mycheck.equals(paramchk)) return showError(model, "校验错误");
		String[] fileList = StringUtils.split(successFile, "@@");
		String uploadPath = "images/ad/" ; //上传路径
		User user = getLogonUser();
		gewaPicService.moveRemoteTempTo(user.getId(), "ad", null, uploadPath, fileList[0], adname + ".gif");//将文件移动到正式文件夹
		return "redirect:/admin/site/ad/pop.xhtml";
	}
	
	@RequestMapping("/admin/site/ad/saveAdPosition.xhtml")
	public String saveAdPosition(Long adpid,HttpServletRequest request, ModelMap model) {
		AdPosition adposition = new AdPosition("");
		if (adpid!=null) {
			adposition = daoService.getObject(AdPosition.class, adpid);
		}
		Map dataMap=request.getParameterMap();
		BindUtils.bindData(adposition,dataMap);
		if(adpid == null){
			AdPosition  ad = daoService.getObjectByUkey(AdPosition.class, "pid", adposition.getPid(), false);
			if(ad != null) return showJsonError(model, "标识PID已存在");
		}
		daoService.saveObject(adposition);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/site/ad/getAdPosition.xhtml")
	public String getAdPosition(Long id, ModelMap model) {
		AdPosition position = daoService.getObject(AdPosition.class, id);
		Map result = BeanUtil.getBeanMap(position);
		return showJsonSuccess(model, result);
	}

	@RequestMapping("/admin/site/ad/removeAdPosition.xhtml")
	public String removeAdPosition(Long id, ModelMap model) {
		AdPosition position = daoService.getObject(AdPosition.class, id);
		daoService.removeObject(position);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/site/ad/saveAd.xhtml")
	public String saveAd(Long adpid, Long id, HttpServletRequest request, ModelMap model) {
		AdPosition adposition = daoService.getObject(AdPosition.class, adpid);
		Advertising ad = new Advertising("");
		ad.setAdpositionid(adposition.getId());
		String citycode = null;
		if (id!=null) {
			ad = daoService.getObject(Advertising.class, id);
			citycode = ad.getCitycode();
		}else{
			citycode = getAdminCitycode(request);
		}
		ad.setCitycode(citycode);
		BindUtils.bindData(ad, request.getParameterMap());
		if(ad.getOrdernum() == null) ad.setOrdernum(0);
		daoService.saveObject(ad);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/site/ad/getAd.xhtml")
	public String getAd(Long id, Long adpid, ModelMap model) {
		if(id != null){
			Advertising ad = daoService.getObject(Advertising.class, id);
			model.put("ad", ad);
		}
		model.put("adpid", adpid);
		return "admin/site/ad/adForm.vm";
	}

	@RequestMapping("/admin/site/ad/removeAd.xhtml")
	public String removeAd(Long id, ModelMap model) {
		Advertising ad = daoService.getObject(Advertising.class, id);
		ad.setStatus(Advertising.STATUS_DELETED);
		daoService.saveObject(ad);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/site/ad/changOrdernum.xhtml")
	public String changOrdernum(Long id, Integer ordernum, ModelMap model) {
		Advertising ad = daoService.getObject(Advertising.class, id);
		ad.setOrdernum(ordernum);
		daoService.saveObject(ad);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/site/ad/udAd.xhtml")
	public String udAd(Long adpid, Long id, ModelMap model) {
		Advertising curad = daoService.getObject(Advertising.class, id);
		if(Advertising.STATUS_UP.equals(curad.getStatus()))
			curad.setStatus(Advertising.STATUS_DOWN);
		else if(Advertising.STATUS_DOWN.equals(curad.getStatus()))
			curad.setStatus(Advertising.STATUS_UP);
		daoService.saveObject(curad);
		adService.changRaterang(curad.getCitycode(), adpid);
		return showJsonSuccess(model);
	}
	
}
