package com.gewara.web.action.admin.album;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.ManagerCheckConstant;
import com.gewara.helper.sys.RelateClassHelper;
import com.gewara.json.ManageCheck;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.acl.User;
import com.gewara.model.common.UploadPic;
import com.gewara.model.content.Picture;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.sport.SportItem;
import com.gewara.model.user.Album;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberPicture;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.AlbumService;
import com.gewara.service.content.PictureService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;

@Controller
public class AlbumAdminController extends BaseAdminController {
	@Autowired@Qualifier("pictureService")
	private PictureService pictureService;

	public void setPictureService(PictureService pictureService) {
		this.pictureService = pictureService;
	}

	@Autowired
	@Qualifier("mongoService")
	private MongoService mongoService;

	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}

	@Autowired
	@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;

	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	
	@Autowired@Qualifier("albumService")
	private AlbumService albumService;
	
	
	@RequestMapping("/admin/audit/pictureList.xhtml")
	public String getPictureList(ModelMap model, boolean check, String tag, Long relatedid, Integer pageNo) {
		List<Picture> pictureList = new ArrayList<Picture>();
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 30;
		if (relatedid == null) {
			model.put("lg", "Y");
			if (StringUtils.isBlank(tag))
				tag = "movie";
			model.put("vtag", tag);
			String mongotag = getCheckMongoTag(tag);
			Criteria criter1 = new Criteria("tag").is(mongotag);
			Query query = new Query(criter1);
			List<ManageCheck> manageCheckList = mongoService.getObjectList(ManageCheck.class, query.getQueryObject(), "modifytime", false, 0, 1);
			Date operationtime = null;
			if (!manageCheckList.isEmpty()) {
				ManageCheck manageCheck = manageCheckList.get(0);
				model.put("manageCheck", manageCheck);
				operationtime = new Date(manageCheck.getModifytime());
			}
			int checkCount = 0;
			if (operationtime != null) {
				checkCount = pictureService.getPictureCountCheck(tag, operationtime, true);
			}
			int uncheckCount = pictureService.getPictureCountCheck(tag, operationtime, false);
			int checkPage = mongoService.getObjectCount(ManageCheck.class, query.getQueryObject());
			int uncheckPage = 0;
			if ((uncheckCount % rowsPerPage) == 0) {
				uncheckPage = uncheckCount / rowsPerPage;
			} else {
				uncheckPage = uncheckCount / rowsPerPage + 1;
			}

			if (check) {
				List<ManageCheck> manageCheckUsers = mongoService.getObjectList(ManageCheck.class, query.getQueryObject(), "modifytime", true,
						pageNo, 1);
				if (!manageCheckUsers.isEmpty()) {
					ManageCheck userCheck = manageCheckUsers.get(0);
					model.put("userCheck", userCheck);
					pictureList = pictureService.getPictureListCheck(tag, relatedid, null, null, null, new Date(userCheck.getModifytime()), new Date(
							userCheck.getUnmodifytime().getTime()), check, 0, rowsPerPage + 20);
				}
			} else {
				pictureList = pictureService.getPictureListCheck(tag, relatedid, null, null, null, operationtime, null, check, 0, rowsPerPage);
			}
			Map<Long, Object> mapPic = new HashMap<Long, Object>();
			for (Picture picture : pictureList) {
				if ("cinemaroom".equals(picture.getTag())) {
					CinemaRoom cr = daoService.getObject(CinemaRoom.class, picture.getRelatedid());
					mapPic.put(picture.getId(), daoService.getObject(Cinema.class, cr.getCinemaid()));
				} else if ("sportItem".equals(picture.getTag())) {
					SportItem sportitem = daoService.getObject(SportItem.class, picture.getRelatedid());
					mapPic.put(picture.getId(), sportitem);
				} else if (RelateClassHelper.getRelateClazz(picture.getTag()) != null) {
					mapPic.put(picture.getId(), relateService.getRelatedObject(picture.getTag(), picture.getRelatedid()));
				}
				operationtime = new Date(picture.getPosttime().getTime());
			}
			model.put("pageNo", pageNo);
			model.put("mapPic", mapPic);
			model.put("time", DateUtil.getCurDateMills(operationtime));
			model.put("pictureList", pictureList);
			model.put("checkPage", checkPage);
			model.put("uncheckPage", uncheckPage);
			model.put("checkCount", checkCount);
			model.put("check", check);
			model.put("uncheckCount", uncheckCount);
			model.put("tag", mongotag);
		} else {
			int from = rowsPerPage * pageNo;
			Map<Long, Object> mapPic = new HashMap<Long, Object>();
			pictureList = pictureService.getPictureList(tag, relatedid, null, null, null, from, rowsPerPage);
			for (Picture picture : pictureList) {
				if ("cinemaroom".equals(picture.getTag())) {
					CinemaRoom cr = daoService.getObject(CinemaRoom.class, picture.getRelatedid());
					mapPic.put(picture.getId(), daoService.getObject(Cinema.class, cr.getCinemaid()));
				} else if ("sportItem".equals(picture.getTag())) {
					SportItem sportitem = daoService.getObject(SportItem.class, picture.getRelatedid());
					mapPic.put(picture.getId(), sportitem);
				} else if (RelateClassHelper.getRelateClazz(picture.getTag()) != null) {
					mapPic.put(picture.getId(), relateService.getRelatedObject(picture.getTag(), picture.getRelatedid()));
				}
			}
			int count = pictureService.getPictureCount(tag, relatedid, null, null, null);
			Map params = new HashMap();
			params.put("relatedid", relatedid);
			params.put("tag", tag);
			PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/audit/pictureList.xhtml");
			pageUtil.initPageInfo(params);
			model.put("mapPic", mapPic);
			model.put("pictureList", pictureList);
			model.put("pageUtil", pageUtil);
		}
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(pictureList));
		return "admin/audit/picture.vm";
	}

	@RequestMapping("/admin/audit/otherPictureList.xhtml")
	public String getOtherUploadPicList(Long memberid, boolean check, Integer pageNo, ModelMap model) {
		if (pageNo == null)
			pageNo = 0;
		int pageRow = 50;
		Criteria criter1 = new Criteria("tag").is(ManagerCheckConstant.USER_UPLOADPIC);
		Query query = new Query(criter1);
		List<ManageCheck> manageCheckList = mongoService.getObjectList(ManageCheck.class, query.getQueryObject(), "modifytime", false, 0, 1);
		Long operationtime = null;
		if (!manageCheckList.isEmpty()) {
			ManageCheck manageCheck = manageCheckList.get(0);
			model.put("manageCheck", manageCheck);
			operationtime = manageCheckList.get(0).getModifytime();
		}
		String status = "del";
		List<UploadPic> upList = new ArrayList<UploadPic>();
		Long time = null;
		Map<String, Member> mapMember = new HashMap<String, Member>();
		if (!check) {
			upList = uploadPicList(operationtime, null, check, memberid, status, 0, pageRow);
			for (UploadPic uploadPic : upList) {
				mapMember.put(uploadPic.getPicname(), daoService.getObject(Member.class, uploadPic.getMemberid()));
				time = uploadPic.getModifytime();
			}
		}
		int checkCount = 0;
		if (operationtime != null) {
			checkCount = hqlCount(true, operationtime, memberid, status);
		}
		int uncheckCount = hqlCount(false, operationtime, memberid, status);
		int checkPage = 0;
		if ((checkCount % pageRow) == 0) {
			checkPage = checkCount / pageRow;
		} else {
			checkPage = checkCount / pageRow + 1;
		}
		int uncheckPage = 0;
		if ((uncheckCount % pageRow) == 0) {
			uncheckPage = uncheckCount / pageRow;
		} else {
			uncheckPage = uncheckCount / pageRow + 1;
		}
		if (check) {
			List<ManageCheck> manageCheckUsers = mongoService.getObjectList(ManageCheck.class, query.getQueryObject(), "modifytime", true, pageNo, 1);
			if (!manageCheckUsers.isEmpty()) {
				ManageCheck userCheck = manageCheckUsers.get(0);
				model.put("userCheck", userCheck);
				upList = uploadPicList(userCheck.getModifytime(), userCheck.getUnmodifytime().getTime(), check, memberid, status, 0, pageRow);
				for (UploadPic uploadPic : upList) {
					mapMember.put(uploadPic.getPicname(), daoService.getObject(Member.class, uploadPic.getMemberid()));
					time = uploadPic.getModifytime();
				}
			}
			PageUtil pageUtil = new PageUtil(checkCount, pageRow, pageNo, "admin/audit/otherPictureList.xhtml");
			Map parames = new HashMap();
			parames.put("check", check);
			pageUtil.initPageInfo(parames);
			model.put("pageUtil", pageUtil);
		}
		model.put("checkCount", checkCount);
		model.put("uncheckCount", uncheckCount);
		model.put("checkPage", checkPage);
		model.put("uncheckPage", uncheckPage);
		model.put("tag", ManagerCheckConstant.USER_UPLOADPIC);
		model.put("upList", upList);
		model.put("mapMember", mapMember);
		model.put("time", time);
		model.put("check", check);
		List<Long> memberidList = ServiceHelper.getMemberIdListFromBeanList(upList);
		addCacheMember(model, memberidList);
		return "admin/audit/otherPicture.vm";
	}

	private int hqlCount(boolean check, Long operationtime, Long memberid, String status) {
		String hql = "select count(*) from UploadPic t where not exists(select p.picturename from Picture p where p.picturename = substr(t.picname,2)) and not exists(select m.picturename from MemberPicture m where m.picturename=substr(t.picname,2)) ";
		List<Long> result = new ArrayList<Long>();
		List params = new ArrayList();
		if (operationtime != null) {
			if (check)
				hql += " and modifytime <= ? ";
			else
				hql += " and modifytime > ?";
			params.add(operationtime);
		}
		if (memberid != null) {
			hql += " and t.memberid= ?";
			params.add(memberid);
		}
		if (StringUtils.isNotBlank(status)) {
			hql += " and t.status!= ?";
			params.add(status);
		}
		result = hibernateTemplate.find(hql, params.toArray());
		if (result.isEmpty())
			return 0;
		return result.get(0).intValue();
	}

	private List uploadPicList(Long operationtime, Long updatetime, boolean check, Long memberid, String status, int formnum, int maxnum) {
		List params = new ArrayList();
		String hql = "from UploadPic t where not exists(select p.picturename from Picture p where p.picturename = substr(t.picname,2)) and not exists(select m.picturename from MemberPicture m where m.picturename=substr(t.picname,2)) ";
		if (operationtime != null) {
			if (check) {
				if (operationtime.equals(updatetime)) {
					hql += " and modifytime <=? ";
				} else {
					hql += " and modifytime > ? and modifytime <= ? ";
					params.add(updatetime);
				}
			} else
				hql += " and modifytime >? ";
			params.add(operationtime);
		}
		if (memberid != null) {
			params.add(memberid);
			hql += " and t.memberid=? ";
		}
		if (StringUtils.isNotBlank(status)) {
			params.add(status);
			hql += " and t.status!=? ";
		}
		hql += " order by t.modifytime asc";
		List<UploadPic> upList = daoService.queryByRowsRange(hql, formnum, maxnum, params.toArray());
		return upList;
	}

	@RequestMapping("/admin/audit/otherDeletePicture.xhtml")
	public String otherDeleteUploadPic(ModelMap model, String picname) {
		User user = getLogonUser();
		if (StringUtils.isBlank(picname))
			return showJsonError(model, "参数错误！");
		UploadPic uploadPic = daoService.getObjectByUkey(UploadPic.class, "picname", picname, false);
		if (uploadPic == null)
			return showJsonError(model, "该图片不在存或被删除！");
		ChangeEntry changeEntry = new ChangeEntry(uploadPic);
		uploadPic.setStatus(UploadPic.STATUS_DEL);
		daoService.saveObject(uploadPic);
		monitorService.saveChangeLog(user.getId(), UploadPic.class, uploadPic.getPicname(), changeEntry.getChangeMap(uploadPic));
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/audit/checkPicture.xhtml")
	public String checkPictureId(ModelMap model, Long id) {
		try {
			Picture picture = daoService.getObject(Picture.class, id);
			daoService.saveObject(picture);
		} catch (Exception e) {
			return showJsonError(model, e.getMessage());
		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/audit/deletePicture.xhtml")
	public String deletePictureId(ModelMap model, Long id) {
		try {
			Picture picture = daoService.getObject(Picture.class, id);
			daoService.removeObject(picture);
			monitorService.saveDelLog(getLogonUser().getId(), id, picture);
		} catch (Exception e) {
			return showJsonError(model, e.getMessage());
		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/audit/activityPictureList.xhtml")
	public String getActivityPictureList(ModelMap model, boolean check, Integer pageNo, Long memberid, Timestamp starttime, Timestamp endtime) {
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPages = 30;
		String tag = "activity";
		if (memberid == null && starttime == null && endtime == null) {
			model.put("lg", "Y");
			Date operationtime = null;
			Criteria criter1 = new Criteria("tag").is(ManagerCheckConstant.USER_ACTIVITY_PICTURE);
			Query query = new Query(criter1);
			List<ManageCheck> manageCheckList = mongoService.getObjectList(ManageCheck.class, query.getQueryObject(), "modifytime", false, 0, 1);
			if (!manageCheckList.isEmpty()) {
				ManageCheck manageCheck = manageCheckList.get(0);
				model.put("manageCheck", manageCheck);
				operationtime = new Date(manageCheck.getModifytime());
			}
			int checkCount = 0;
			if (operationtime != null) {
				checkCount = pictureService.getPictureCountCheck(tag, operationtime, true);
			}
			int uncheckCount = pictureService.getPictureCountCheck(tag, operationtime, false);
			int checkPage = mongoService.getObjectCount(ManageCheck.class, query.getQueryObject());
			int uncheckPage = 0;
			if ((uncheckCount % rowsPerPages) == 0) {
				uncheckPage = uncheckCount / rowsPerPages;
			} else {
				uncheckPage = uncheckCount / rowsPerPages + 1;
			}
			List<Picture> pictureList = new ArrayList<Picture>();
			if (!check) {
				pictureList = pictureService
						.getPictureListCheck(tag, null, memberid, starttime, endtime, operationtime, null, check, 0, rowsPerPages);
			} else {
				List<ManageCheck> manageCheckUsers = mongoService.getObjectList(ManageCheck.class, query.getQueryObject(), "modifytime", true,
						pageNo, 1);
				if (!manageCheckUsers.isEmpty()) {
					ManageCheck userCheck = manageCheckUsers.get(0);
					model.put("userCheck", userCheck);
					pictureList = pictureService.getPictureListCheck(tag, null, null, null, null, new Date(userCheck.getModifytime()), new Date(
							userCheck.getUnmodifytime().getTime()), check, 0, rowsPerPages);
				}
			}
			Map<Long, Object> pictureMap = new HashMap<Long, Object>();
			Map<Long, Member> memberMap = new HashMap<Long, Member>();
			for (Picture picture : pictureList) {
				if ("activity".equals(picture.getTag())) {
					ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(picture.getRelatedid());
					if (code.isSuccess()) {
						pictureMap.put(picture.getId(), code.getRetval());
					}
				} else {
					pictureMap.put(picture.getId(), "暂时没关联!");
				}
				if (picture.hasMemberType(GewaraUser.USER_TYPE_MEMBER)) {
					memberMap.put(picture.getId(), daoService.getObject(Member.class, picture.getMemberid()));
				}
				operationtime = new Date(picture.getPosttime().getTime());
			}
			model.put("time", DateUtil.getCurDateMills(operationtime));
			model.put("pictureMap", pictureMap);
			model.put("memberMap", memberMap);
			model.put("pictureList", pictureList);
			model.put("checkPage", checkPage);
			model.put("uncheckPage", uncheckPage);
			model.put("checkCount", checkCount);
			model.put("check", check);
			model.put("uncheckCount", uncheckCount);
			model.put("pageNo", pageNo);
			model.put("tag", ManagerCheckConstant.USER_ACTIVITY_PICTURE);
			addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(pictureList));
		} else {
			int from = rowsPerPages * pageNo;
			Map<Long, Object> pictureMap = new HashMap<Long, Object>();
			List<Picture> pictureList = pictureService.getPictureList(tag, null, memberid, starttime, endtime, from, rowsPerPages);
			for (Picture picture : pictureList) {
				if ("activity".equals(picture.getTag())) {
					ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(picture.getRelatedid());
					if (code.isSuccess()) {
						pictureMap.put(picture.getId(), code.getRetval());
					}

				} else {
					pictureMap.put(picture.getId(), "暂时没关联!");
				}
			}
			Integer pictureCount = pictureService.getPictureCount(tag, null, memberid, starttime, endtime);
			Map params = new HashMap();
			params.put("tag", tag);
			params.put("memberid", memberid);
			params.put("starttime", starttime);
			params.put("endtime", endtime);
			PageUtil pageUtil = new PageUtil(pictureCount, rowsPerPages, pageNo, "admin/audit/activityPictureList.xhtml");
			pageUtil.initPageInfo(params);
			model.put("pictureMap", pictureMap);
			model.put("pictureList", pictureList);
			model.put("pageUtil", pageUtil);
			addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(pictureList));
		}
		return "admin/audit/activityPicture.vm";
	}

	@RequestMapping("/admin/audit/attachPictureList.xhtml")
	public String auditMPPictureList(ModelMap model, Integer pageNo, String astatus, Long memberid, String tag) {
		if (StringUtils.isBlank(astatus))
			astatus = Status.N;
		if (StringUtils.isBlank(tag))
			tag = "movie";
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 10;
		int firstRow = pageNo * rowsPerPage;
		int count = pictureService.getMemberPictureCount(null, tag, memberid, TagConstant.FLAG_PIC, astatus);// 网友上传视频，在memberpicture
		List<MemberPicture> mVideoList = pictureService.getMemberPictureList(null, tag, memberid, TagConstant.FLAG_PIC, astatus, firstRow,
				rowsPerPage);
		Map<Long, Object> tagMap = new HashMap<Long, Object>();
		for (MemberPicture mp : mVideoList) {
			if (tag.equals("movie"))
				tagMap.put(mp.getId(), daoService.getObject(Movie.class, mp.getRelatedid()));
			else if (tag.equals("cinema"))
				tagMap.put(mp.getId(), daoService.getObject(Cinema.class, mp.getRelatedid()));
		}
		model.put("tagMap", tagMap);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(mVideoList));
		Map params = new HashMap();
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/admin/audit/pictureList.xhtml");
		params.put("memberid", memberid);
		params.put("tag", tag);
		params.put("astatus", astatus);
		pageUtil.initPageInfo(params);
		model.put("astatus", astatus);
		model.put("tag", tag);
		model.put("pageUtil", pageUtil);
		model.put("mVideoList", mVideoList);
		List<Long> memberidList = ServiceHelper.getMemberIdListFromBeanList(mVideoList);
		addCacheMember(model, memberidList);
		return "admin/audit/attachMPPictureList.vm";
	}

	private String getCheckMongoTag(String key) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("movie", ManagerCheckConstant.VENUE_MOVIE);
		map.put("cinema", ManagerCheckConstant.VENUE_CINEMA);
		map.put("ktv", ManagerCheckConstant.VENUE_KTV);
		map.put("bar", ManagerCheckConstant.VENUE_BAR);
		map.put("gym", ManagerCheckConstant.VENUE_GYM);
		map.put("sport", ManagerCheckConstant.VENUE_SPORT);
		map.put("news", ManagerCheckConstant.VENUE_NEWS);
		map.put("gymcourse", ManagerCheckConstant.VENUE_GYMCOURSE);
		map.put("sportItem", ManagerCheckConstant.VENUE_SPORTITEM);
		map.put("gymroom", ManagerCheckConstant.VENUE_GYMROOM);
		map.put("commu", ManagerCheckConstant.VENUE_COMMU);
		map.put("drama", ManagerCheckConstant.VENUE_DRAMA);
		map.put("theatre", ManagerCheckConstant.VENUE_THEATRE);
		return map.get(key);
	}
	
	@RequestMapping("/admin/audit/albumList.xhtml")
	public String albumList(Long memberid, Long commuid, String searchKey, Integer pageNo, ModelMap model){
		if (pageNo == null) pageNo = 0;
		int rowsPerPage = 50;
		int firstRow = pageNo * rowsPerPage;
		int count = albumService.getAlbumCountByMemberIdOrCommuId(memberid, commuid, searchKey);
		List<Album> albumList = albumService.getAlbumListByMemberIdOrCommuId(memberid, commuid, searchKey, firstRow, rowsPerPage);
		if(count > rowsPerPage){
			PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/audit/albumList.xhtml");
			Map params = new HashMap();
			params.put("memberid", memberid);
			params.put("commuid", commuid);
			params.put("searchKey", searchKey);
			pageUtil.initPageInfo(params);
			model.put("pageUtil", pageUtil);
		}
		model.put("albumList", albumList);
		addCacheMember(model, BeanUtil.getBeanPropertyList(albumList, Long.class, "memberid", true));
		return "admin/audit/albumList.vm";
	}
	@RequestMapping("/admin/audit/deleteAlbumById.xhtml")
	public String deleteAlbumById(Long id, ModelMap model){
		if(id == null) return showJsonError(model, "参数错误！");
		Album album = daoService.getObject(Album.class, id);
		if(album == null) return showJsonError(model, "未找到该相册！");
		User user = getLogonUser();
		List<Picture> pictureList = albumService.getPictureByAlbumId(album.getId(), 0, 1000);
		daoService.removeObjectList(pictureList);
		daoService.removeObject(album);
		dbLogger.warn("管理员["+user.getNickname()+"("+user.getId()+")]:删除了用户ID["+album.getMemberid()+"]的相册ID["+album.getId()+"]所属圈子ID["+album.getCommuid()+"]该相册下的所有照片也被级联删除！");
		return showJsonSuccess(model);
	}
	
}