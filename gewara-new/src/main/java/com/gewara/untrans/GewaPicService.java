package com.gewara.untrans;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.gewara.support.ErrorCode;

public interface GewaPicService{
	void init();
	List<String> reloadPicSize();
	boolean isValidSize(int width, int height);
	/**
	 * 返回文件最后修改时间，小于等于0，文件不存在
	 * @param picname
	 * @return
	 */
	long exists(String picname);
	/**
	 * 保存图片
	 * @param picStream
	 * @param picname
	 * @return
	 * @throws IOException 
	 */
	boolean saveToRemote(InputStream inputStream, String dstpath, boolean update) throws IOException;
	boolean saveToRemote(File localFile, String dstpath, boolean update);
	/**
	 * 获取图片，写入writer
	 * @param writer
	 * @param picname
	 * @return 文件最后更改时间，0表示文件不存在
	 * @throws IOException
	 */
	long getFileFromRemote(StreamWriter writer, String picname) throws IOException;
	/**
	 * @param os 
	 * @param picname
	 * @param width
	 * @param height
	 * @param crop 是否从中截取
	 * @return 文件最后更改时间，0表示文件不存在
	 */
	long getPicture(StreamWriter writer, String picname, int width, int height, boolean crop) throws IOException;
	void genPicture(String picname, int width, int height, boolean crop);
	/**
	 * 返回文件最后修改时间，小于等于0，文件不存在
	 * @param picname
	 * @param width
	 * @param height
	 * @param crop
	 * @return 
	 */
	long exists(String picname, int width, int height, boolean crop);
	boolean saveToLocal(File file, String picname) throws IOException;
	void compressPic(String picname, int width, int height, boolean crop) throws IOException;
	void compressPic(String picname) throws IOException;
	boolean removePicture(String picname) throws IOException;
	
	/**
	 * 
	 * @param file
	 * @param extname
	 * @return 保存后的文件名
	 * @throws IOException
	 */
	String saveToTempFile(MultipartFile file, String extname) throws IOException;
	String saveToTempPic(InputStream is, String extname) throws IOException;
	String saveToTempPic(MultipartFile file, String extname) throws IOException;
	
	void saveTempFileToRemote(String filename);
	void saveTempFileListToRemote(List<String> successFile);
	/**
	 * 从RemoteTmp文件夹移走文件到dstDir
	 * @param memberid
	 * @param tag
	 * @param relatedid
	 * @param dstDir like images/xxxx/201112/
	 * @param tmpFilename
	 * @return
	 * @throws IOException
	 */
	String moveRemoteTempTo(Long memberid, String tag, Long relatedid, String dstDir, String tmpFilename) throws IOException;
	String moveRemoteTempTo(Long memberid, String tag, Long relatedid, String dstDir, String tmpFilename, String dstFilename) throws IOException;
	List<String> moveRemoteTempListTo(Long memberid, String tag, Long relatedid, String dstDir, String[] tmpList) throws IOException;
	/**
	 * 限制临时图片大小
	 * @param tmpFileList
	 */
	void limitTempFileSize(List<String> tmpFileList, int width, int height);
	void limitTempFileSize(String tmpFile, int width, int height);
	void addWaterMark(String tmpFile);
	void addWaterMark(List<String> tmpFile);
	String getTempFilePath(String filename);
	void compressFiles(String path, boolean recusive) throws IOException;
	List<String> findFiles(String path, boolean recusive) throws IOException;
	OutputStream createTempFile(String filename) throws IOException;

	public static interface StreamWriter{
		void write(InputStream is, Long lastModifyTime, Integer maxage, Long expireTime) throws IOException;
		void write(InputStream is, Long lastModifyTime) throws IOException;
	}
	boolean addToRemoteFile(File file, Long memberid, String tag, Long relatedid, String dstFile) throws IOException;
	int clearTempFiles() throws IOException;
	//String moveRemotePicToMongo(MultipartFile file, String partnerid, String date, String dstDir) throws IOException;
	//String moveRemotePicStreamToMongo(InputStream stream, String partnerid, String date, String dstDir) throws IOException;
	ErrorCode removeScalePic(String path);
}

