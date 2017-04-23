package com.gewara.web.action.admin.sys;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.model.common.UploadPic;
import com.gewara.service.DaoService;
import com.gewara.untrans.GewaPicService;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class HdfsFileAdminController extends BaseAdminController{
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	public void setGewaPicService(GewaPicService gewaPicService) {
		this.gewaPicService = gewaPicService;
	}
	
	
	
	@RequestMapping("/admin/sysmgr/save2hdfs.xhtml")
	public String save2hdfs(ModelMap model, Integer from){
		if(from == null) from = 0;
		DetachedCriteria query = DetachedCriteria.forClass(UploadPic.class);
		query.add(Restrictions.eq("status", "unsave"));
		query.addOrder(Order.asc("picname"));
		final List<UploadPic> uploadPicList = hibernateTemplate.findByCriteria(query, from, 3000);
		new CopyWorker(daoService, uploadPicList, gewaPicService, from).start();
		dbLogger.error("save2hdfs:" + from);
		return forwardMessage(model, "save working...." + from + ", count:" + uploadPicList.size());
	}
	@RequestMapping("/admin/sysmgr/uploadImgFile.xhtml")
	public String uploadImgFile(ModelMap model, String filename){
		String picname = "img/" + filename;
		String file = getRealPath(picname);
		boolean result = gewaPicService.saveToRemote(new File(file), picname, true);
		return forwardMessage(model, "result:" + file + ", " + result);
	}
	@RequestMapping("/picExists.xhtml")
	public String picExists(ModelMap model, String picname){
		return forwardMessage(model, "result: " + gewaPicService.exists(picname));
	}
	@RequestMapping("/admin/sysmgr/clearTemp.xhtml")
	public String clearTemp(ModelMap model) throws IOException{
		int count = gewaPicService.clearTempFiles();
		return forwardMessage(model, "clear result:" + count);
	}
	
	@RequestMapping("/admin/sysmgr/checkHdfs.xhtml")
	public String checkHdfs(ModelMap model, Integer from){
		if(from == null) from = 0;
		//检查hdfs中文件是否存在
		DetachedCriteria query = DetachedCriteria.forClass(UploadPic.class);
		query.add(Restrictions.eq("status", "saved"));
		query.addOrder(Order.asc("picname"));
		final List<UploadPic> uploadPicList = hibernateTemplate.findByCriteria(query, from, 5000);
		new CheckWorker(daoService, uploadPicList, gewaPicService, from).start();
		return forwardMessage(model, "check working...." + from);
	}
	public static class CheckWorker extends Thread{
		private GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
		private List<UploadPic> uploadPicList;
		private GewaPicService gewaPicService;
		private Integer from;
		private DaoService daoService;
		private String basePath = "/opt/lamp/apache/htdocs/shanghai";
		public CheckWorker(DaoService daoService, List<UploadPic> uploadPicList, GewaPicService gewaPicService, Integer from){
			this.uploadPicList = uploadPicList;
			this.gewaPicService = gewaPicService;
			this.from = from;
			this.daoService = daoService;
			this.setDaemon(true);
		}
		@Override
		public void run() {
			int i=0, j=0;
			for(UploadPic uploadPic: uploadPicList){
				if(gewaPicService.exists(uploadPic.getPicname().substring(basePath.length())) > 0){
					uploadPic.setStatus("checked");
				}else{
					uploadPic.setStatus("unsave");
					j++;
					dbLogger.error("not exists:" + j + "," + uploadPic.getPicname().substring(basePath.length()));
				}
				daoService.saveObject(uploadPic);
				i++;
				if(i % 50 == 0) dbLogger.error("check from:" + from + ", cur:" + i);
			}
		}
	}
	@RequestMapping("/admin/sysmgr/deleteInvalid.xhtml")
	public String save2hdfs(ModelMap model){
		return forwardMessage(model, "test");
	}
	@RequestMapping("/admin/sysmgr/reloadPicSize.xhtml")
	public String reloadPicSize(ModelMap model){
		List<String> sizeList = gewaPicService.reloadPicSize();
		model.put("msgList", sizeList);
		return "showResult.vm";
	}
	@RequestMapping("/admin/sysmgr/compress.xhtml")
	public String compressPics(String picpath, String recusive, ModelMap model) throws Exception{
		if(StringUtils.isNotBlank(picpath)){
			gewaPicService.compressFiles(picpath, StringUtils.equals("true", recusive));
		}
		model.put("successMsgs", "" + new Date());
		return "showResult.vm";
	}
	@RequestMapping("/admin/sysmgr/updateScalePic.xhtml")
	public String updateScalePic(int w, int h, String path, String recusive, ModelMap model) throws Exception{
		String searchPath = "/scale/sw" + w + "h" + h + "/";
		if(StringUtils.isNotBlank(path)) searchPath += path;
		else path="";
		boolean allow = operationService.updateOperation(searchPath, 60 * 10);
		List<String> msgList = new ArrayList<String>();
		if(allow){
			dbLogger.warn(searchPath);
			final List<String> files = gewaPicService.findFiles(searchPath, StringUtils.equals("true", recusive));
			final String tmpPath = path;
			final int width = w, height=h;
			Thread worker = new Thread(new Runnable(){
				@Override
				public void run() {
					for(String file: files){
						String picname = tmpPath + file;
						try {
							gewaPicService.genPicture(picname, width, height, true);
						} catch (Exception e) {
							dbLogger.error("", e);
						}
					}
				}
			});
			worker.setDaemon(true);
			worker.start();
			msgList.add("start working，共" + files.size() + "个......");
			msgList.addAll(files);
		}else{
			msgList.add("retry 10m later!");
		}
		model.put("msgList", msgList);
		return "showResult.vm";
	}
	public static class CopyWorker extends Thread{
		private GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
		private List<UploadPic> uploadPicList;
		private GewaPicService gewaPicService;
		private Integer from;
		private DaoService daoService;
		private String basePath = "/opt/lamp/apache/htdocs/shanghai";
		public CopyWorker(DaoService daoService, List<UploadPic> uploadPicList, GewaPicService gewaPicService, Integer from){
			this.uploadPicList = uploadPicList;
			this.gewaPicService = gewaPicService;
			this.from = from;
			this.daoService = daoService;
			this.setDaemon(true);
		}
		@Override
		public void run() {
			int i=0;
			for(UploadPic uploadPic: uploadPicList){
				gewaPicService.saveToRemote(new File(uploadPic.getPicname()), uploadPic.getPicname().substring(basePath.length()), false);
				uploadPic.setStatus("saved");
				daoService.saveObject(uploadPic);
				i++;
				if(i % 50 == 0) dbLogger.error("save from:" + from + ", cur:" + i);
			}
		}
	}
}
