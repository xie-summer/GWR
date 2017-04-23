package com.gewara.service.content;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.gewara.model.content.Picture;
import com.gewara.model.user.MemberPicture;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-10-9上午08:58:27
 */
public interface PictureService {
	/**
	 * 得到关联对象的图片列表
	 * @param tag
	 * @param relatedid
	 * @param from
	 * @param num
	 * @return
	 */
	List<Picture> getPictureListByRelatedid(String tag, Long relatedid, int from, int maxnum);
	/**
	 * 获取图片列表
	 * @param tag
	 * @param relatedid
	 * @param orderField
	 * @param asc
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<Picture> getPictueList(String tag, Long relatedid, String orderField, boolean asc, int from, int maxnum);
	Integer getPictureCountByRelatedid(String tag, Long relatedid);
	List<String> getSinglePictureListByRelatedid(String tag, Long relatedid, int from, int maxnum);

	Integer getPictureCount(String tag, Long relatedid, Long memberid, Timestamp starttime, Timestamp endtime);
	List<Picture> getPictureList(String tag, Long relatedid, Long memberid, Timestamp starttime, Timestamp endtime, int from, int maxnum);
	//后台审核专用
	//begin
	List<Picture> getPictureListCheck(String tag, Long relatedid,Long memberid, Timestamp starttime, Timestamp endtime,Date modifytime,Date updatetime,boolean check, int from, int maxnum);
	Integer getPictureCountCheck(String tag,Date datetime,boolean check);
	//end
	
	/**
	 * 获取用户上传图片或视频信息
	 * @param relatedid
	 * @param tag
	 * @return
	 */
	Integer getMemberPictureCount(Long relatedid, String tag, Long memberid, String flag, String status);
	List<MemberPicture> getMemberPictureList(Long relatedid, String tag, Long memberid, String flag, String status, int from, int maxnum);
}
