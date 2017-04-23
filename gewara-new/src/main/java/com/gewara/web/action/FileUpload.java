package com.gewara.web.action;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28ÏÂÎç02:05:17
 */
public class FileUpload {
	private String uploadPath;
	private Long relatedid;
	private String tag;
	private MultipartFile file;
	private MultipartFile file1;
	private MultipartFile file2;
	private MultipartFile file3;
	private String callback;
	//processor=CINEMAPICTURE,MOVIEPICTURE
	private String processor;
	public String getProcessor() {
		return processor;
	}

	public void setProcessor(String processor) {
		this.processor = processor;
	}

	public String getUploadPath() {
		if(StringUtils.isBlank(this.uploadPath)) return "/resources";
		return uploadPath;
	}
	public void setUploadPath(String uploadPath) {
		if(uploadPath == null || uploadPath.endsWith("/") || uploadPath.endsWith("\\")) this.uploadPath = uploadPath;
		else this.uploadPath = uploadPath + "/";
	}

	public MultipartFile getFile3() {
		return file3;
	}

	public void setFile3(MultipartFile file3) {
		this.file3 = file3;
	}

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public MultipartFile getFile1() {
		return file1;
	}

	public void setFile1(MultipartFile file1) {
		this.file1 = file1;
	}

	public MultipartFile getFile2() {
		return file2;
	}

	public void setFile2(MultipartFile file2) {
		this.file2 = file2;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}
}
