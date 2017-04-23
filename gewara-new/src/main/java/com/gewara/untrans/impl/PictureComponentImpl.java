package com.gewara.untrans.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gewara.constant.Flag;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.model.BaseObject;
import com.gewara.model.common.Relationship;
import com.gewara.model.content.HeadInfo;
import com.gewara.model.content.Picture;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberPicture;
import com.gewara.service.DaoService;
import com.gewara.service.content.NewsService;
import com.gewara.service.content.PictureService;
import com.gewara.service.content.VideoService;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.PictureComponent;
import com.gewara.untrans.RelateService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.web.util.PageUtil;

@Service("pictureComponent")
public class PictureComponentImpl implements PictureComponent {
	public static final List<String> TAG_LIST = Arrays.asList("cinema", "movie", "theatre", "drama", "dramastar", "sport","gym", "gymcoach", "gymcourse", "agency");
	public static final Map<String, Class<? extends BaseObject>> clazzMap = new HashMap<String, Class<? extends BaseObject>>();

	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	@Autowired@Qualifier("pictureService")
	private PictureService pictureService;
	public void setPictureService(PictureService pictureService) {
		this.pictureService = pictureService;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	@Autowired@Qualifier("videoService")
	private VideoService videoService;
	public void setVideoService(VideoService videoService) {
		this.videoService = videoService;
	}
	@Autowired@Qualifier("relateService")
	private RelateService relateService;
	
	@Autowired@Qualifier("newsService")
	private NewsService newsService;
	public void setNewsService(NewsService newsService) {
		this.newsService = newsService;
	}
	private static final List<String> PICTURE_TYPE = Arrays.asList("apic", "mpic");
	@Override
	public void pictureList(ModelMap model,Integer pageNo, String tag, Long relatedid, String type, String url){
		if(!PICTURE_TYPE.contains(type) || StringUtils.isBlank(url)) throw new IllegalArgumentException("参数类型错误！");
		if(pageNo==null) pageNo=0;
		int rowsPerPage = 16;
		if(StringUtils.isNotBlank(tag)){
			if(StringUtils.equals(tag, "sport") || StringUtils.equals(tag, "movie")) rowsPerPage=18;
		}
		int firstPerPage = pageNo*rowsPerPage;
		int pictureCount = 0;
		Map<Long, Integer> repleyCountMap = new HashMap<Long, Integer>();
		if(StringUtils.equals("apic", type)){
			List<Picture> pictureList = pictureService.getPictureListByRelatedid(tag, relatedid, firstPerPage, rowsPerPage);
			for(Picture picture:pictureList){
				Integer count=commentService.getCommentCountByRelatedId("picture", picture.getId());
				repleyCountMap.put(picture.getId(), count);
			}
			pictureCount = pictureService.getPictureCountByRelatedid(tag, relatedid);
			model.put("pictureList", pictureList);
		}else if(StringUtils.equals("mpic", type)){
			List<MemberPicture> memberPictureList = pictureService.getMemberPictureList(relatedid, tag, null, TagConstant.FLAG_PIC, Status.Y,firstPerPage, rowsPerPage);
			Map<Long, String> memberMap=new HashMap<Long, String>();
			for(MemberPicture mpicture:memberPictureList){
				Integer count2=commentService.getCommentCountByRelatedId("picture", mpicture.getId());
				repleyCountMap.put(mpicture.getId(), count2);
				Member member2=daoService.getObject(Member.class, mpicture.getMemberid());
				memberMap.put(mpicture.getId(), member2.getNickname());
			}
			pictureCount = pictureService.getMemberPictureCount(relatedid, tag, null, TagConstant.FLAG_PIC, Status.Y);
			model.put("memberMap", memberMap);
			model.put("memberPictureList", memberPictureList);
		}
		model.put("type", type);
		model.put("tag", tag);
		PageUtil pageUtil = new PageUtil(pictureCount, rowsPerPage, pageNo, url, true, true);
		Map params = new HashMap();
		params.put("relatedid", relatedid);
		params.put("type", type);
		params.put("tag", tag);
		pageUtil.initPageInfo(params);
		model.put("repleyCountMap", repleyCountMap);
		model.put("pageUtil", pageUtil);
		model.put("relatedid", relatedid);
	}
	@Override
	public void pictureDetail(ModelMap model,String tag, Long relatedid, Long pid, String type) {
		if(!TAG_LIST.contains(tag)|| !PICTURE_TYPE.contains(type)) return ;//throw new IllegalArgumentException("参数类型错误！");
		List<Map> mapList = new ArrayList<Map>();
		Object baseEntity = null;
		long pictureid= 0L;
		if(StringUtils.equals(type, "apic")){//管理员添加的图片
			List<Picture> pictureList = new ArrayList<Picture>();
			if(pid != null){
				Picture picture=daoService.getObject(Picture.class, pid);
				if(picture==null) return;
				baseEntity = relateService.getRelatedObject(tag, picture.getRelatedid());
				if(baseEntity ==null) return;
				Long id=(Long)BeanUtil.get(baseEntity, "id");
				pictureList = pictureService.getPictureListByRelatedid(tag, id, 0, 200);
				pictureList.remove(picture);
				pictureList.add(0, picture);
				pictureid = pid;
			}else {
				baseEntity =relateService.getRelatedObject(tag, relatedid);
				if (baseEntity == null) return;
				Long id=(Long)BeanUtil.get(baseEntity, "id");
				pictureList = pictureService.getPictureListByRelatedid(tag, id, 0, 200);
				if(!pictureList.isEmpty())
					pictureid=pictureList.get(0).getId();
			}
			mapList = BeanUtil.getBeanMapList(pictureList, "id","picturename","posttime","memberid", "description");
			Long amebmerid=0L;
			for(Map map: mapList){
				if(map.get("memberid") == null) amebmerid=1L;
				else amebmerid= Long.parseLong(map.get("memberid")+"");
				Member member=daoService.getObject(Member.class, amebmerid);
				if(member == null) member=daoService.getObject(Member.class, 1L);
				map.put("membername", member.getNickname());
			}
		}else{//网友上传的图片
			List<MemberPicture> pictureList = new ArrayList<MemberPicture>();
			if(pid != null){
				MemberPicture mpicture=daoService.getObject(MemberPicture.class, pid);
				if(mpicture==null)return;
				baseEntity = relateService.getRelatedObject(tag, mpicture.getRelatedid());
				if(baseEntity == null)return;
				Long id=(Long)BeanUtil.get(baseEntity, "id");
				pictureList = pictureService.getMemberPictureList(id, tag, null, TagConstant.FLAG_PIC, Status.Y, 0, 200);
				pictureList.remove(mpicture);
				pictureList.add(0, mpicture);
				pictureid = pid;
			}else {
				baseEntity = relateService.getRelatedObject(tag, relatedid);
				if (baseEntity == null) return;
				Long id=(Long)BeanUtil.get(baseEntity, "id");
				pictureList = pictureService.getMemberPictureList(id, tag, null, TagConstant.FLAG_PIC, Status.Y, 0, 200);
				if(!pictureList.isEmpty())
					pictureid = pictureList.get(0).getId();
			}
			mapList = BeanUtil.getBeanMapList(pictureList, "id","picturename","addtime","memberid", "description");
			for(Map map: mapList){
				Member member=daoService.getObject(Member.class, new Long(map.get("memberid")+""));
				map.put("membername", member.getNickname());
				map.put("posttime", map.get("addtime"));
			}
		}
		model.put("type", type);
		model.put(tag, baseEntity);
		model.put("mapList", mapList);
		model.put("pictureid", pictureid);
	}
	//公用图片上传(电影，影院，剧院，话剧)
	@Override
	public Map attachRelatePicture(String tag, Long relatedid, String citycode){
		if(!TAG_LIST.contains(tag)) throw new IllegalArgumentException("参数类型错误！");
		HashMap model=new HashMap();
		if(StringUtils.equals(tag, TagConstant.TAG_MOVIE)){
			Movie movie=daoService.getObject(Movie.class, relatedid);
			model.put("movie", movie);
			model.putAll(getCommonData(TagConstant.TAG_MOVIE,citycode, relatedid));
			model.putAll(getHeadData(TagConstant.TAG_MOVIE, relatedid));
		}else if(StringUtils.equals(tag, TagConstant.TAG_CINEMA)){
			Cinema cinema=daoService.getObject(Cinema.class, relatedid);
			model.put("cinema", cinema);
			model.putAll(getHeadData(TagConstant.TAG_CINEMA, relatedid));
		}else if(StringUtils.equals(tag, TagConstant.TAG_THEATRE)){
			Theatre theatre=daoService.getObject(Theatre.class, relatedid);
			model.put("theatre", theatre);
		}else if(StringUtils.equals(tag, TagConstant.TAG_DRAMA)){
			Drama drama=daoService.getObject(Drama.class, relatedid);
			model.put("drama", drama);
		}
		return model;
	}
	@Override
	public Map getHeadData(String tag, Long relatedid){
		Relationship relationship = commonService.getRelationship(Flag.FLAG_HEAD, tag, relatedid, DateUtil.getCurFullTimestamp());
		Map headDataMap = new HashMap();
		HeadInfo headInfo = null;
		if(relationship!=null){
			headInfo = daoService.getObject(HeadInfo.class, relationship.getRelatedid1());
		}
		headDataMap.put("headInfo", headInfo);
		return headDataMap;
	}
	@Override
	public Map getCommonData(String tag,String citycode, Long relatedid){
		HashMap model=new HashMap();
		Integer videoCount = videoService.getVideoCountByTag(tag, relatedid, null);
		Integer pictureCount = pictureService.getPictureCountByRelatedid(tag, relatedid);
		Integer newsCount=newsService.getNewsCount(citycode, tag, "", relatedid, "");
		//网友上传信息
		Integer mVideoCount= pictureService.getMemberPictureCount(relatedid, tag, null, TagConstant.FLAG_VIDEO, Status.Y);
		Integer mPictureCount= pictureService.getMemberPictureCount(relatedid,tag, null, TagConstant.FLAG_PIC, Status.Y);
		model.put("allVideoCount", videoCount+mVideoCount);
		model.put("allPictureCount", pictureCount+mPictureCount);
		model.put("newsCount", newsCount);
		return model;
	}
}
