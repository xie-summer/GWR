/**
 * 
 */
package com.gewara.service.bbs.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.model.content.Picture;
import com.gewara.model.user.Album;
import com.gewara.model.user.AlbumComment;
import com.gewara.service.bbs.AlbumService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;

/**
 * @author chenhao(sky_stefanie@hotmail.com)
 */
@Service("albumService")
public class AlbumServiceImpl extends BaseServiceImpl implements AlbumService {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public List<Album> getAlbumListByMemberId(Long id, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Album.class);
		query.add(Restrictions.eq("memberid", id));
		query.add(Restrictions.eq("commuid",0l));
		query.addOrder(Order.desc("addtime"));
		List<Album> albumList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return albumList;
	}
	@Override
	public List<Map> getAlbumListByMemberId(Long memberid) {
		String hql = "select new map(a.id as id, a.subject as subject) from Album a where a.memberid=? and a.commuid=0";
		List<Map> albumList = readOnlyTemplate.find(hql, memberid);
		return albumList;
	}
	
	@Override
	public int getAlbumListCountByMemberId(Long id) {
		DetachedCriteria query = DetachedCriteria.forClass(Album.class);
		query.add(Restrictions.eq("memberid", id));
		query.add(Restrictions.eq("commuid",0l));
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	@Override
	public List<Picture> getPictureByAlbumId(Long albumid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Picture.class);
		query.add(Restrictions.eq("relatedid", albumid));
		query.add(Restrictions.eq("tag", "album"));
		query.addOrder(Order.desc("posttime"));
		List<Picture> result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}
	@Override
	public Integer getPictureountByAlbumId(Long albumid){
		DetachedCriteria query = DetachedCriteria.forClass(Picture.class);
		query.add(Restrictions.eq("relatedid", albumid));
		query.add(Restrictions.eq("tag", "album"));
		query.setProjection(Projections.rowCount());
		List<Picture> pictureList = readOnlyTemplate.findByCriteria(query);
		if(pictureList.size()>0) return new Integer(pictureList.get(0)+"");
		return 0;
	}
	@Override
	public List<AlbumComment> getPictureComment(Long imageid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(AlbumComment.class);
		query.add(Restrictions.eq("imageid", imageid));
		query.addOrder(Order.desc("addtime"));
		List<AlbumComment> albumCommentList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return albumCommentList;
	}
	@Override
	public List<Long> getMemberIdListByAlbumComment(Long imageid, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(AlbumComment.class);
		query.add(Restrictions.eq("imageid", imageid));
		query.setProjection(Projections.distinct(Projections.property("memberid")));
		List<Long> memberidList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return memberidList;
	}
	@Override
	public List<Picture> getPicturesByCommuidList(Long commuid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(Picture.class,"ai");
		query.add(Restrictions.eq("tag", "album"));
		DetachedCriteria subQuery = DetachedCriteria.forClass(Album.class,"a");
		subQuery.add(Restrictions.eq("a.commuid", commuid));
		subQuery.add(Restrictions.eqProperty("a.id","ai.relatedid"));
		subQuery.setProjection(Projections.property("a.id"));
		query.add(Subqueries.exists(subQuery));
		query.addOrder(Order.desc("ai.posttime"));
		List<Picture> albumImageList = readOnlyTemplate.findByCriteria(query,from, maxnum);
		return albumImageList;
	}
	@Override
	public Integer getFriendAlbumCountByMemberId(Long memberid) {
		String hql = "select count(a) from Album a where memberid in(select memberfrom from Friend f " +
				"where f.memberto= ?) and a.commuid =0";
		List list = readOnlyTemplate.find(hql,memberid);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	@Override
	public List<Album> getFriendAlbumListByMemberId(Long memberid, int from,
			int maxnum) {
		String hql = "from Album a where exists (select id from Friend f " +
				"where f.memberfrom = ? and a.memberid = f.memberto) and a.commuid =0";
		List<Album> albumList = queryByRowsRange(hql, from, maxnum, memberid);
		return albumList;
	}
	
	@Override
	public List<Album> getAlbumListByMemberIdOrCommuId(Long memberid, Long commuid, String searchKey, int from, int maxnum) {
		DetachedCriteria query = getAlbumQuery(memberid, commuid, searchKey);
		query.addOrder(Order.desc("addtime"));
		List<Album> albumList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return albumList;
	}
	@Override
	public Integer getAlbumCountByMemberIdOrCommuId(Long memberid, Long commuid, String searchKey){
		DetachedCriteria query = getAlbumQuery(memberid, commuid, searchKey);
		query.setProjection(Projections.rowCount());
		List result = readOnlyTemplate.findByCriteria(query);
		if(result.isEmpty()) return 0;
		else return Integer.parseInt(result.get(0)+"");
	}
	private DetachedCriteria getAlbumQuery(Long memberid, Long commuid, String searchKey){
		DetachedCriteria query = DetachedCriteria.forClass(Album.class);
		if(memberid != null) query.add(Restrictions.eq("memberid", memberid));
		if(commuid != null){
			query.add(Restrictions.eq("commuid",commuid));
		}else{
			query.add(Restrictions.eq("commuid",0l));
		}
		if(StringUtils.isNotBlank(searchKey)){
			query.add(Restrictions.or(Restrictions.like("subject", searchKey, MatchMode.ANYWHERE), Restrictions.like("description", searchKey, MatchMode.ANYWHERE)));
		}
		return query;
	}
}
