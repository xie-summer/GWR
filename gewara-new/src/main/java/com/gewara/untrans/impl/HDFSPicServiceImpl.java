package com.gewara.untrans.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.multipart.MultipartFile;

import com.gewara.Config;
import com.gewara.model.common.UploadPic;
import com.gewara.service.DaoService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.GewaPicService;
import com.gewara.util.CompressCallback;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.util.PictureUtil;
import com.gewara.util.StringUtil;
public class HDFSPicServiceImpl implements GewaPicService, InitializingBean {
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	private String scalePre = "/scale/";
	private String tempPre = "/temp/";
	private String remoteTmpPath = "/images/temp/";
	private FileSystem fs;
	private String hdfsUrl;
	private String tempPath;			//临时文件
	private String tempCompressPath;
	private String waterMarkFile;		//水印文件
	private Set<String> sizeList = new TreeSet<String>();
	private Set<Integer> widthList = new TreeSet<Integer>();
	private Set<Integer> heightList = new TreeSet<Integer>();
	private int maxsize = 2000;
	private Map<String, Long> recentResizeFiles = new LinkedHashMap<String, Long>();
	public void setHdfsUrl(String hdfsUrl) {
		this.hdfsUrl = hdfsUrl;
	}
	@Override
	public boolean isValidSize(int width, int height){
		return sizeList.isEmpty() || sizeList.contains("w" + width + "h" + height) 
				|| widthList.contains(width) && height== maxsize || heightList.contains(height) && width==maxsize;
	}
	@Override
	public List<String> reloadPicSize() {
		sizeList.clear();
		try{
			List size = org.apache.commons.io.IOUtils.readLines(
					new FileReader(getClass().getClassLoader().getResource("/picsize.txt").getFile()));
			sizeList.addAll(size);
			for(String pic: sizeList){
				try{
					Integer w = Integer.valueOf(pic.substring(1, pic.indexOf('h')));
					Integer h = Integer.valueOf(pic.substring(pic.indexOf('h') + 1));
					widthList.add(w);
					heightList.add(h);
				}catch(Exception e){
				}
			}
		}catch(Exception e){
			dbLogger.warn("", e);
		}
		return new ArrayList<String>(sizeList);
	}
	@Override
	public long exists(String picname){
		try {
			if(!picname.startsWith("/")) picname = "/" + picname;
			Path path = new Path(picname);
			if(fs.exists(path) && fs.isFile(path)) return fs.getFileStatus(path).getModificationTime();
		} catch (Exception e) {
			dbLogger.error("", e);
		} finally {
		}
		return 0L;
	}
	@Override
	public long getFileFromRemote(StreamWriter pw, String picname) throws IOException {
		if(!picname.startsWith("/")) picname = "/" + picname;
		Path path = new Path(picname);
		return getFileFromRemote(pw, path);
	}
	private long getFileFromRemote(StreamWriter pw, Path path) throws IOException {
		InputStream in = null;
		try {
			if(fs.exists(path) && fs.isFile(path)){
				in = fs.open(path);
				if(in.available()< 50){//少于50byte，不能保存
					fs.delete(path, false);
					dbLogger.warn("file is empty:" + path.toString());
					return 0L;
				}else{
					FileStatus status = fs.getFileStatus(path);
					if(pw!=null) pw.write(in, status.getModificationTime(), 5184000, DateUtil.addDay(new Date(), 60).getTime());
					return status.getModificationTime();
				}
			}
		} catch (MalformedURLException e) {
			dbLogger.error("", e);
		} finally {
			if(in!=null) IOUtils.closeStream(in);
		}
		dbLogger.warn("file is empty:" + path.toString());
		return 0L;
	}
	
	@Override
	public long getPicture(StreamWriter wp, String picname, int width, int height, boolean crop) throws IOException {
		if(!picname.startsWith("/")) picname = "/" + picname;
		String filename = StringUtil.getFilename(picname);
		String realname = scalePre + (crop?"c":"s") + "w" + width + "h" + height + picname;
		long result = 0L;
		if(fs.exists(new Path(realname))){
			result = getFileFromRemote(wp, realname);
		}else{
			dbLogger.warn("first tmp get:" + realname);
			Long time = recentResizeFiles.get(realname);
			Long cur = System.currentTimeMillis();
			
			File/*原始图片*/ localFile = new File(getTempFilePath((time==null?cur:time) + filename.replaceAll("/", "_")));
			File/*调整大小后的图片*/ resizedFile = new File(getTempFilePath((time==null?cur:time) + realname.replaceAll("/", "_")));
			if(time == null){
				if(recentResizeFiles.size()>2000) recentResizeFiles = new LinkedHashMap<String, Long>();
				recentResizeFiles.put(realname, cur);
			}else{//第二次，不生成，直接返回文件
				if(time + DateUtil.m_minute > cur) {
					if(resizedFile.exists()){
						InputStream tmp = new FileInputStream(resizedFile);
						wp.write(tmp, resizedFile.lastModified(), 300, DateUtil.addMinute(new Date(), 5).getTime());
						result = resizedFile.lastModified();
						return result;
					}
				}
			}
			InputStream tmp = null;
			try {
				boolean resized = resizedFile.exists();
				if(!resized){
					boolean tmpExists = saveToLocal(localFile, picname);
					if(tmpExists){
						resized = PictureUtil.resizeOrCrop(localFile, resizedFile, width, height, crop);
					}else{
						dbLogger.warn("pic not exists:" + picname);
					}
				}
				if(resized){
					tmp = new FileInputStream(resizedFile);
					wp.write(tmp, resizedFile.lastModified(), 300, DateUtil.addMinute(new Date(), 5).getTime());
					result = resizedFile.lastModified();
					genPicture(picname, width, height, crop, resizedFile);
				}else{
					dbLogger.warn("pic not resize:" + picname);
				}
			} catch (Exception e) {
				dbLogger.error("", e);
			} finally{
				if(tmp!=null) try{tmp.close();}catch(Exception e){}
				localFile.delete();
			}
		}
		return result;
	}
	@Override
	public void genPicture(String picname, int width, int height, boolean crop){
		picname = picname.startsWith("/")? picname: ("/" + picname);
		String realname = scalePre + (crop?"c":"s") + "w" + width + "h" + height + picname;
		File tmpFile2 = new File(getTempFilePath(System.currentTimeMillis() + realname.replaceAll("/", "_")));
		genPicture(picname, width, height, crop, tmpFile2);
	}
	private void genPicture(String picname, int width, int height, boolean crop, File resizedFile){
		picname = picname.startsWith("/")? picname: ("/" + picname);
		String filename = StringUtil.getFilename(picname);
		String realname = scalePre + (crop?"c":"s") + "w" + width + "h" + height + picname;
		File localFile = new File(getTempFilePath(System.currentTimeMillis() + filename.replaceAll("/", "_")));
		boolean genLocal = false;
		try {
			boolean resized = resizedFile.exists();
			if(!resized){
				boolean tmpExists = saveToLocal(localFile, picname);
				genLocal = true;
				if(tmpExists){
					resized = PictureUtil.resizeOrCrop(localFile, resizedFile, width, height, crop);
				}
			}
			if(resized && resizedFile.exists()){
				Path dstPath = new Path(realname);
				saveToRemote(resizedFile, realname, true);//先写入
				if(StringUtils.endsWithIgnoreCase(picname, "jpg") || 
							StringUtils.endsWithIgnoreCase(picname, "jpeg")) {//压缩
					compressPic(resizedFile, dstPath);//再压缩
				}
			}else{
				dbLogger.warn("pic not exists:" + picname);
			}
		} catch (Exception e) {
			dbLogger.error("", e);
		} finally{
			if(genLocal) localFile.delete();
		}
	}
	@Override
	public long exists(String picname, int width, int height, boolean crop){
		if(!picname.startsWith("/")) picname = "/" + picname;
		String realname = scalePre + (crop?"c":"s") + "w" + width + "h" + height + picname;
		try {
			Path path = new Path("/", realname);
			if(fs.exists(path) && fs.isFile(path)) return fs.getFileStatus(path).getModificationTime();
		} catch (IOException e) {
			dbLogger.error("", e);
		}
		return 0L;
	}
	@Override
	public boolean saveToLocal(File file, String picname) throws IOException{
		Path original = new Path("/", picname);
		if(!fs.exists(original)) return false;
		FileOutputStream tmpos1 = new FileOutputStream(file);
		try{
			getFileFromRemote(new StreamPicWriter(tmpos1), picname);
			return true;
		}catch(Exception e){
			return false;
		}finally{
			try{tmpos1.close();}catch(Exception e){}
		}
	}
	private boolean saveToLocal(File file, Path original) throws IOException{
		if(!fs.exists(original)) return false;
		FileOutputStream tmpos1 = new FileOutputStream(file);
		try{
			getFileFromRemote(new StreamPicWriter(tmpos1), original);
			return true;
		}catch(Exception e){
			dbLogger.error(StringUtil.getExceptionTrace(e, 5));
			return false;
		}finally{
			try{tmpos1.close();}catch(Exception e){}
		}
	}
	@Override
	public void init() {
		Configuration conf = new Configuration();
		try {
			fs = FileSystem.get(URI.create(hdfsUrl), conf);
		} catch (IOException e) {
			dbLogger.error("", e);
		}
	}
	@Override
	public boolean removePicture(String picname) {
		if(!picname.startsWith("/")) picname = "/" + picname;
		UploadPic uploadPic = daoService.getObjectByUkey(UploadPic.class, "picname", picname, false);
		if(uploadPic != null){
			uploadPic.setStatus(UploadPic.STATUS_DEL);
			daoService.saveObject(uploadPic);
			return true;
		}
		return false;
	}

	@Override
	public boolean saveToRemote(InputStream inputStream, String dstpath, boolean update) throws IOException {
		if(!dstpath.startsWith("/")) dstpath = "/" + dstpath;
		String picpath = StringUtil.getFilepath(dstpath);
		Path realpath = new Path(picpath);
		if(!fs.exists(realpath)){
			fs.mkdirs(realpath);
		}
		Path remoteFile = new Path(dstpath);
		return saveToRemote(inputStream, remoteFile, update);
	}
	@Override
	public OutputStream createTempFile(String filename) throws IOException{
		if(StringUtils.indexOfAny(filename, "/\\") >=0 ) throw new IllegalArgumentException();
		String tmpPath = tempPre + DateUtil.format(new Date(), "yyyyMM") + "/";
		Path realpath = new Path(tmpPath);
		if(!fs.exists(realpath)){
			fs.mkdirs(realpath);
		}
		return fs.create(new Path(tmpPath+filename));
		
	}
	private boolean saveToRemote(InputStream inputStream, Path remoteFile, boolean update) throws IOException {
		if(inputStream.available() < 50) return false;//不能保存
		if(!update && fs.exists(remoteFile)) return true;
		OutputStream os = fs.create(remoteFile);
		try{
			IOUtils.copyBytes(inputStream, os, 4096, false);
			return true;
		}catch(Exception e){
			return false;
		}finally{
			try{os.close();}catch(Exception e){}
		}
	}
	@Override
	public boolean saveToRemote(File localFile, String dstpath, boolean update) {
		InputStream is = null;
		try {
			is = new FileInputStream(localFile);
			boolean result = saveToRemote(is, dstpath, update);
			return result;
		} catch (IOException e) {
			dbLogger.error("", e);
			return false;
		}finally{
			try{if(is !=null) is.close();}catch(Exception e){}
		}
	}
	@Override
	public void saveTempFileListToRemote(List<String> tmpFileList) {
		for(String tmpFile: tmpFileList){
			saveTempFileToRemote(tmpFile);
		}
	}
	@Override
	public void saveTempFileToRemote(String tmpFile) {
		try{
			File file = new File(getTempFilePath(tmpFile));
			saveToRemote(file, remoteTmpPath + tmpFile, false);
			file.delete(); 
		}catch(Exception e){
			dbLogger.error("", e);
		}
	}

	@Override
	public String saveToTempFile(MultipartFile file, String extname) throws IOException {
		String fileName = StringUtil.getUID() + "." + extname;
		File upfile = new File(tempPath, fileName);
		file.transferTo(upfile);
		return fileName;
	}
	@Override
	public String saveToTempPic(InputStream is, String extname) throws IOException {
		String fileName = StringUtil.getUID();
		File upfile = new File(tempPath, fileName + "." + extname);
		OutputStream os = new FileOutputStream(upfile);
		org.apache.commons.io.IOUtils.copy(is, os);
		os.close();
		return processPic(fileName, extname, upfile);
	}
	@Override
	public String saveToTempPic(MultipartFile file, String extname) throws IOException {
		if(StringUtils.equals("jpeg", extname)) extname = "jpg";
		String fileName = StringUtil.getUID();
		File upfile = new File(tempPath, fileName + "." + extname);
		file.transferTo(upfile);
		return processPic(fileName, extname, upfile);
	}
	private String getRemoteTempPath(String fileName){
		return "/images/temp/" + fileName;
	}
	private String processPic(String filename, String extname, File upfile){
		String realExtname = PictureUtil.getPicFormat(upfile);
		String real = filename + "." + realExtname;
		if(!PictureUtil.isValidExtension(real, PictureUtil.UPLOADTYPE_PIC)){
			upfile.delete();
			return "";
		}
		if(!StringUtils.equals(realExtname, extname)){
			upfile.renameTo(new File(tempPath, real));
		}
		return real;
	}
	@Override
	public String moveRemoteTempTo(Long memberid, String tag, Long relatedid, String dstDir, String tmpFilename) throws IOException {
		return moveRemoteTempTo(memberid, tag, relatedid, dstDir, tmpFilename, tmpFilename);
	}
	@Override
	public String moveRemoteTempTo(Long memberid, String tag, Long relatedid, String dstDir, String tmpFilename, String dstFilename) throws IOException {
		if(StringUtils.contains(dstDir, ".")){
			throw new IllegalArgumentException("上传路径不合法");
		}
		if(!StringUtils.endsWith(dstDir, "/")) dstDir += "/";
		if(!StringUtils.startsWith(dstDir, "/")) dstDir = "/" + dstDir;
		
		Path tmp = new Path(getRemoteTempPath(tmpFilename));
		Path dstPath = new Path(dstDir);
		if(!fs.exists(dstPath)){
			fs.mkdirs(dstPath);
		}
		int length = (int) fs.getFileStatus(tmp).getLen();
		boolean success = fs.rename(tmp, new Path(dstPath, dstFilename));
		if(success) {
			UploadPic upload = new UploadPic(dstDir + dstFilename, System.currentTimeMillis(), length);
			upload.setStatus(UploadPic.STATUS_CHECKED);
			upload.setMemberid(memberid);
			upload.setTag(tag);
			upload.setRelatedid(relatedid);
			daoService.saveObject(upload);
		}
		return dstDir + dstFilename;
	}
	@Override
	public boolean addToRemoteFile(File file, Long memberid, String tag, Long relatedid, String dstFile) throws IOException {
		String picpath = StringUtil.getFilepath(dstFile);
		if(StringUtils.contains(picpath, ".")){
			throw new IllegalArgumentException("上传路径不合法");
		}
		if(!StringUtils.startsWith(dstFile, "/")) dstFile = "/" + dstFile;
		boolean success = saveToRemote(file, dstFile, true);
		if(success){
			int length = (int) fs.getFileStatus(new Path(dstFile)).getLen();
			UploadPic upload = new UploadPic(dstFile, System.currentTimeMillis(), length);
			upload.setStatus(UploadPic.STATUS_CHECKED);
			upload.setMemberid(memberid);
			upload.setTag(tag);
			upload.setRelatedid(relatedid);
			daoService.saveObject(upload);
		}
		return true;
	}
	@Override
	public List<String> moveRemoteTempListTo(Long memberid, String tag, Long relatedid, String dstDir, String[] tmpList) throws IOException {
		if(StringUtils.contains(dstDir, ".")){
			throw new IllegalArgumentException("上传路径不合法");
		}
		if(!StringUtils.endsWith(dstDir, "/")) dstDir += "/";
		if(!StringUtils.startsWith(dstDir, "/")) dstDir = "/" + dstDir;
		
		Path dstPath = new Path(dstDir);
		if(!fs.exists(dstPath)){
			fs.mkdirs(dstPath);
		}
		List<String> result = new ArrayList<String>();
		for(String tmp: tmpList){
			try{
				Path tmpPath = new Path(getRemoteTempPath(tmp));
				int length = (int) fs.getFileStatus(tmpPath).getLen();
				boolean success = fs.rename(tmpPath, new Path(dstPath, tmp));
				if(success) {
					UploadPic upload = new UploadPic(dstDir + tmp, System.currentTimeMillis(), length);
					upload.setStatus(UploadPic.STATUS_CHECKED);
					upload.setMemberid(memberid);
					upload.setTag(tag);
					upload.setRelatedid(relatedid);
					daoService.saveObject(upload);
				}
				result.add(dstPath + tmp);
			}catch(Exception e){
				dbLogger.warn("", e);
			}
		}
		return result;
	}
	
	@Override
	public void limitTempFileSize(List<String> tmpFileList, int width, int height) {
		for(String file: tmpFileList){
			try{
				File upfile = new File(tempPath, file);
				PictureUtil.limitSize(upfile.getCanonicalPath(), width, height);
			}catch(Exception e){
				dbLogger.warn(StringUtil.getExceptionTrace(e));
			}
		}
	}
	@Override
	public void limitTempFileSize(String tmpFile, int width, int height) {
		try{
			File upfile = new File(tempPath, tmpFile);
			PictureUtil.limitSize(upfile.getCanonicalPath(), width, height);
		}catch(Exception e){
			dbLogger.warn(StringUtil.getExceptionTrace(e));
		}
	}
	@Override
	public void addWaterMark(String tmpFile) {
		File upfile = new File(tempPath, tmpFile);
		PictureUtil.addWaterMark(upfile, upfile, new File(waterMarkFile));
	}
	@Override
	public void addWaterMark(List<String> tmpFileList) {
		for(String tmpFile: tmpFileList){
			File upfile = new File(tempPath, tmpFile);
			if(!tmpFile.endsWith(".gif")) {
				PictureUtil.addWaterMark(upfile, upfile, new File(waterMarkFile));
			}
		}
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		init();
		reloadPicSize();
		File file = File.createTempFile("gewapic", "tmp");
		File tmpFile = new File(file.getParent(),"/temp/compress/");
		if(!tmpFile.exists()) {
			tmpFile.mkdirs();
		}
		tempPath = tmpFile.getParent();
		tempCompressPath = tmpFile.getCanonicalPath();
		waterMarkFile = getClass().getClassLoader().getResource("watermark.png").getFile();
		dbLogger.warn("temp compress path:" + tempCompressPath);
	}

	@Override
	public String getTempFilePath(String filename) {
		try {
			return new File(tempPath, filename).getCanonicalPath();
		} catch (IOException e) {
			dbLogger.warn(StringUtil.getExceptionTrace(e));
		}
		return null;
	}
	@Override
	public void compressPic(String picname, int width, int height, boolean crop) throws IOException {
		if(!picname.startsWith("/")) picname = "/" + picname;
		final String realname = scalePre + (crop?"c":"s") + "w" + width + "h" + height + picname;
		compressPic(realname);
	}
	@Override
	public void compressPic(final String picname) throws IOException {
		Path path = new Path("/", picname);
		compressPic(path);
	}
	private void compressPic(Path path) throws IOException {
		if(!fs.exists(path)) return;
		String tmpname = StringUtil.getRandomString(15, true, true, false) + "." + StringUtil.getFilenameExtension(path.getName());
		final File tmpFile = new File(tempPath, tmpname);
		saveToLocal(tmpFile, path);
		String picname = path.getName();
		if(!StringUtils.endsWithIgnoreCase(picname, "jpg") && 
				!StringUtils.endsWithIgnoreCase(picname, "jpeg")) return; //非jpg,不压缩
		compressPic(tmpFile, path);
	}
	private void compressPic(final File tmpFile, final Path dstPath) throws IOException {
		final String key = dstPath.toString();
		dbLogger.warn("save to tmp file:" + tmpFile.getCanonicalPath());
		CompressCallback callback = new CompressCallback(){
			@Override
			public void onComplete(File destFile, long oldsize, long newsize) {
				if(!destFile.exists()){
					//可能部分图片不需要压缩，如：3587 --> 3587 bytes (0.00%), skipped
					return;
				}
				InputStream tmp2 = null;
				try{
					tmp2 = new FileInputStream(destFile);
					saveToRemote(tmp2, dstPath, true);
					dbLogger.warn("compress complete" + key + ": from " + oldsize + "-->" + newsize);
				} catch (IOException e) {
					dbLogger.warn(StringUtil.getExceptionTrace(e, 5));
				} finally{
					if(tmp2!=null) try{tmp2.close();}catch(Exception e){}
					destFile.delete();
					tmpFile.delete();
				}
			}

			@Override
			public void onFailure() {
				dbLogger.warn("compress failure" + key);
			}
		};
		PictureUtil.compressPic(tmpFile.getCanonicalPath(), tempCompressPath, 90, callback);
	}
	@Override
	public void compressFiles(String path, boolean recusive) throws IOException {
		String[] pathList = StringUtils.split(path, ",");
		for(String p:pathList){
			if(!p.startsWith("/")) p = "/" + p;
			Path picPath = new Path(p);
			compressFiles(picPath, recusive);
		}
	}
	private void compressFiles(Path path, boolean recusive) throws IOException{
		if(!fs.exists(path)) return;
		if(fs.isFile(path)){
			compressPic(path);
			return;
		}
		FileStatus[] result = fs.listStatus(path);
		dbLogger.warn("compress " + path.toString() + ", have " + result.length + " files...");
		List<Path> subPathList = new ArrayList<Path>();
		Path tmp = null;
		for(FileStatus status: result){
			tmp = status.getPath();
			if(fs.isFile(tmp)) compressPic(tmp);
			else subPathList.add(tmp);
		}
		if(!recusive) return;
		dbLogger.warn("compress " + path.toString() + ", have " + subPathList.size() + " sub path...");
		for(Path subPath:subPathList){
			compressFiles(subPath, recusive);
		}
	}
	@Override
	public List<String> findFiles(String path, boolean recusive) throws IOException{
		Path picPath = new Path(path);
		List<String> files = new ArrayList<String>();
		findFiles(picPath, path, files, recusive);
		return files;
	}
	private void findFiles(Path path, String prefix, List<String> files, boolean recusive) throws IOException{
		if(fs.getFileStatus(path).isDir()){
			FileStatus[] result = fs.listStatus(path);
			dbLogger.warn("compress " + path.toString() + ", have " + result.length + " files...");
			List<Path> subPathList = new ArrayList<Path>();
			Path tmp = null;
			String fullPath;
			for(FileStatus status: result){
				tmp = status.getPath();
				if(fs.isFile(tmp)) {
					fullPath = tmp.toString();
					files.add(fullPath.substring(StringUtils.indexOf(fullPath, prefix) + prefix.length()));
				}
				else subPathList.add(tmp);
			}
			if(!recusive) return;
			for(Path subPath:subPathList){
				findFiles(subPath, prefix, files, recusive);
			}
		}
	}
	@Override
	public int clearTempFiles() throws IOException {
		Path tmpPath = new Path(remoteTmpPath);
		FileStatus[] result = fs.listStatus(tmpPath);
		dbLogger.warn("start clear temp files: " + tmpPath.toString() + ", have " + result.length + " files...");
		Long endtime = System.currentTimeMillis() - DateUtil.m_hour;
		Path tmp = null;
		int count = 0;
		for(FileStatus status: result){
			try{
				tmp = status.getPath();
				if(fs.isFile(tmp) && status.getModificationTime() < endtime) {
					fs.delete(tmp, false);
					count ++;
				}
			}catch(Exception e){
				dbLogger.warn("error", e);
			}
		}
		return count;
	}
	@Override
	public ErrorCode removeScalePic(String path) {
		if(StringUtils.isBlank(path)) return ErrorCode.getFailure("path is empty!");
		Path tmpPath = new Path(scalePre, path);
		try {
			boolean result = fs.delete(tmpPath, false);
			if(result) return ErrorCode.SUCCESS;
			return ErrorCode.getFailure("path not exists:" + path);
		} catch (IOException e) {
			dbLogger.warn("error", e);
			return ErrorCode.getFailure("error:" + path + ", " + e.getMessage());
		}
	}
}
