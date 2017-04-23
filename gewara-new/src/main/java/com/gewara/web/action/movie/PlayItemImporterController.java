package com.gewara.web.action.movie;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import com.gewara.service.movie.PlayItemImporter;
import com.gewara.util.DateUtil;
import com.gewara.web.action.AnnotationController;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
@Controller
public class PlayItemImporterController extends AnnotationController {
	@Autowired@Qualifier("playItemImporter")
	private PlayItemImporter playItemImporter;
	public void setPlayItemImporter(PlayItemImporter playItemImporter) {
		this.playItemImporter = playItemImporter;
	}
	@Autowired@Qualifier("gewaMultipartResolver")
	private MultipartResolver gewaMultipartResolver;
	public void setGewaMultipartResolver(MultipartResolver gewaMultipartResolver) {
		this.gewaMultipartResolver = gewaMultipartResolver;
	}
	@RequestMapping(value="/admin/cinema/importPlayItems.xhtml", method=RequestMethod.GET)
	public String showForm(){
		return "admin/cinema/importXLSForm.vm";
	}
	
	@RequestMapping(value="/admin/cinema/importPlayItems.xhtml", method=RequestMethod.POST)
	public String handleFormUpload(ModelMap model, HttpServletRequest req) throws Exception {
		List<String> msgList = new ArrayList<String>();
		MultipartHttpServletRequest multipartRequest = gewaMultipartResolver.resolveMultipart(req);
		MultipartFile file = multipartRequest.getFile("file");
		if(file == null){
			msgList.add("上传文件为空!");
			return forwardMessage(model, msgList);
		}
		// 检测文件后缀bob.20120611
		String orifilename = file.getOriginalFilename();
		if(!(StringUtils.endsWithIgnoreCase(orifilename, "xls") || StringUtils.endsWithIgnoreCase(orifilename, "xlsx"))){
			msgList.add("请确认上传文件为excel文件(xls,xlsx)!");
			return forwardMessage(model, msgList); 
		}
		
		String uploadDir = getRealPath("/resources/movieplaytime/");
		File dirPath = new File(uploadDir);
		if (!dirPath.exists()) {
			dirPath.mkdirs();
		}
		String fileName = DateUtil.format(new Date(), "MM_dd_HH_mm");
		String userid = SecurityContextHolder.getContext().getAuthentication().getName();
		String fullFileName = uploadDir + userid +fileName + ".xls";
		InputStream stream = null;
		OutputStream fos = null;
		
		try {
			stream = file.getInputStream();
			fos = new BufferedOutputStream(new FileOutputStream(fullFileName));
			IOUtils.copy(stream, fos);
			stream.close();
		} catch (IOException e) {
			msgList.add("上传文件时出现错误");
		} finally{
			if(fos!=null){
				try{
					fos.close();
				}catch(Exception e){/*ignore*/}
			}
			if(stream != null){
				try{
					stream.close();
				}catch(Exception e){/*ignore*/}
			}
		}
		String tag = multipartRequest.getParameter("tag");
		playItemImporter.importPlayTime(fullFileName, msgList, tag);
		return forwardMessage(model, msgList);
	}
}
