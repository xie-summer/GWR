/**
 * 
 */
package com.gewara.service.bbs;

import java.util.List;
import java.util.Map;

import com.gewara.model.content.Picture;
import com.gewara.model.user.Album;
import com.gewara.model.user.AlbumComment;


/**
 * @author chenhao(sky_stefanie@hotmail.com)
 */
public interface AlbumService {

	/**通过用户id查询相册列表
	 * @param id
	 * @return
	 */
	List<Album> getAlbumListByMemberId(Long id, int from, int maxnum);
	/**通过用户id查询相册数量
	 * @param id
	 * @return
	 */
	int getAlbumListCountByMemberId(Long id);
	/**
	 * 通过用户id查询相册列表(id, subject)
	 * @param memberid
	 * @return
	 */
	List<Map> getAlbumListByMemberId(Long memberid);

	/**
	 * 根据albumid查询所属图片 + 数量
	 */
	List<Picture> getPictureByAlbumId(Long albumid, int from, int maxnum);
	Integer getPictureountByAlbumId(Long albumid);
	
	/**
	 * 根据图片id查询图片回复列表
	 * @param imageid
	 * @return
	 */
	List<AlbumComment> getPictureComment(Long imageid, int from, int maxnum);
	/**
	 * 根据圈子id查询所属圈子的照片信息
	 */
	List<Picture> getPicturesByCommuidList(Long commuid,int from,int maxnum);
	
	/**
	 * 通过用户ID查询该用户好友的相册信息
	 */
	List<Album> getFriendAlbumListByMemberId(Long memberid,int from,int maxnum);
	Integer getFriendAlbumCountByMemberId(Long memberid);
	
	List<Long> getMemberIdListByAlbumComment(Long imageid, int from, int maxnum);
	//根据用户ID或圈子ID得到相册列表
	List<Album> getAlbumListByMemberIdOrCommuId(Long memberid, Long commuid, String searchKey, int from, int maxnum);
	//根据用户ID或圈子ID得到相册数量
	Integer getAlbumCountByMemberIdOrCommuId(Long memberid, Long commuid, String searchKey);
}
