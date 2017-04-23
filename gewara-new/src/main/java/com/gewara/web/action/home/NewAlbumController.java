package com.gewara.web.action.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.TagConstant;
import com.gewara.json.MemberStats;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.commu.CommuMember;
import com.gewara.model.content.Picture;
import com.gewara.model.user.Album;
import com.gewara.model.user.AlbumComment;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.MemberPicture;
import com.gewara.service.bbs.AlbumService;
import com.gewara.service.bbs.CommuService;
import com.gewara.service.member.FriendService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.GewaPicService;
import com.gewara.untrans.ShareService;
import com.gewara.util.BeanUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.PictureUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.BaseHomeController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;

/**
 * 
 * 我的相册相关控制
 * 
 * @author taiqichao
 * 
 */
@Controller
public class NewAlbumController extends BaseHomeController {

	private static final List<String> specialTagList = new ArrayList<String>();
	static {
		specialTagList.add("member");
		specialTagList.add("commu");
	}
	@Autowired
	@Qualifier("friendService")
	private FriendService friendService;

	@Autowired
	@Qualifier("albumService")
	private AlbumService albumService;

	@Autowired
	@Qualifier("commuService")
	private CommuService commuService;

	@Autowired
	@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;

	@Autowired
	@Qualifier("config")
	private Config config;

	@Autowired
	@Qualifier("commentService")
	private CommentService commentService;

	@Autowired
	@Qualifier("shareService")
	private ShareService shareService;

	/**
	 * 我的相册
	 * 
	 * @param model
	 * @param pageNo
	 * @param type
	 * @param memberid
	 * @return
	 */
	@RequestMapping("/home/new/album/albumList.xhtml")
	public String position(ModelMap model, Integer pageNo, Long memberid) {
		Member mymember = getLogonMember();
		if (memberid == null) {// 自己
			memberid = mymember.getId();
		}
		// 判断访问权限
		if (memberid != null && !memberid.equals(mymember.getId())) {
			model.putAll(friendService.isPrivate(memberid));
		}
		model.putAll(controllerService.getCommonData(model, mymember, memberid));
		Member member = (Member) model.get("member");
		if (member == null)
			return show404(model, "请刷新重试！");
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 12;
		int start = pageNo * rowsPerPage;
		int count = 0;
		List<Album> albumList = null;
		Map<Long, Integer> imageNum = new HashMap<Long, Integer>();
		// 我的相册
		albumList = albumService.getAlbumListByMemberId(member.getId(), start, rowsPerPage);
		count = albumService.getAlbumListCountByMemberId(member.getId());
		for (Album album : albumList) {
			Integer num = albumService.getPictureountByAlbumId(album.getId());
			imageNum.put(album.getId(), num);
		}
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/home/new/album/albumList.xhtml", true, true);
		Map params = new HashMap();
		params.put("memberid", new String[] { memberid + "" });
		pageUtil.initPageInfo(params);
		model.put("albumMap", albumService.getAlbumListByMemberId(member.getId()));
		model.put("albumList", albumList);
		model.put("imageNum", imageNum);
		model.put("count", count);
		model.put("pageUtil", pageUtil);
		this.getHomeLeftNavigate(daoService.getObject(MemberInfo.class, memberid), model);
		return "sns/userAlbum/albumList.vm";
	}

	/**
	 * 上传图片
	 * 
	 * @param tag
	 * @param relatedid
	 * @param albumid
	 * @param model
	 * @return
	 */
	@RequestMapping("/home/new/uploadPicture.xhtml")
	public String uploadPicture(String tag, Long relatedid, Long albumid, ModelMap model) {
		Member member = getLogonMember();
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		model.put("url", "home/new/album/albumList.xhtml?memberid=" + member.getId());
		model.put("relatedtag", tag);
		model.put("relatedid", relatedid);
		model.put("albumid", albumid);
		if (specialTagList.contains(tag)) {
			if ("commu".equals(tag)) {// 上传圈子相册
				model.put("commu", daoService.getObject(Commu.class, relatedid));
				model.put("albumMap", BeanUtil.beanListToMap(commuService.getCommuAlbumById(relatedid, 0, 1000), "id", "subject", false));
			} else {// 上传用户相册
				model.put("albumMap", BeanUtil.beanListToMap(albumService.getAlbumListByMemberId(relatedid, 0, 1000), "id", "subject", false));
			}
		}
		this.getHomeLeftNavigate(daoService.getObject(MemberInfo.class, member.getId()), model);
		return "sns/userAlbum/photoUpload.vm";
	}

	/**
	 * 相册图片列表
	 * 
	 * @param albumid
	 * @param pageNo
	 * @param model
	 * @return
	 */
	@RequestMapping("/home/new/album/albumImageList.xhtml")
	public String albumImageList(Long albumid, Integer pageNo, ModelMap model) {
		Album album = daoService.getObject(Album.class, albumid);
		if (album == null)
			return showError(model, "您访问的页面不存在！");
		if (album.getCommuid() > 0)
			return showError(model, "你访问的相册不存在！");
		Member member = getLogonMember();
		Member albumMember = null;
		if (!album.getMemberid().equals(member.getId())) {
			model.put("isMyFriend", friendService.isFriend(member.getId(), album.getMemberid()));
			model.put("isMy", "no");
			albumMember = daoService.getObject(Member.class, album.getMemberid());
			if (albumMember == null)
				return showError(model, "该用户不存在！");
			model.putAll(friendService.isPrivate(album.getMemberid())); // 判断访问权限
		} else
			albumMember = member;
		model.putAll(controllerService.getCommonData(model, member, albumMember.getId()));
		isShowAlbumImage(albumMember, getLogonMember(), model, album);
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 20;
		int start = pageNo * rowsPerPage;
		int count = 0;
		List<Picture> albumImageList = albumService.getPictureByAlbumId(albumid, start, rowsPerPage);
		Map<Long, Long> imageNum = new HashMap<Long, Long>();
		count = albumService.getPictureountByAlbumId(albumid);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/home/new/album/albumImageList.xhtml", true, true);
		Map params = new HashMap();
		params.put("albumid", albumid);
		pageUtil.initPageInfo(params);
		model.put("albumMap", albumService.getAlbumListByMemberId(albumMember.getId()));
		model.put("imageNum", imageNum);
		model.put("pageUtil", pageUtil);
		model.put("albumid", albumid);
		model.put("albumImageList", albumImageList);
		model.put("album", album);
		this.getHomeLeftNavigate(daoService.getObject(MemberInfo.class, album.getMemberid()), model);
		return "sns/userAlbum/photoList.vm";
	}

	/**
	 * 单个相册访问权限
	 * 
	 * @param model
	 * @param albumid
	 * @param commuid
	 * @return
	 */
	private String isShowAlbumImage(Member mymember, Member loginMember, ModelMap model, Album album) {
		if (mymember == loginMember) {// 判断当前访问的是否是自己的相册
			model.put("isShowCommuAlbum", true);
		} else {
			model.put("isShowCommuAlbum", false);
		}
		boolean isFriend = Boolean.valueOf(model.get("isMyFriend") + "");
		if (!mymember.getId().equals(loginMember.getId())) {
			if (TagConstant.ALBUM_PRIVATE.equals(album.getRights())) {
				model.put("isCanShow", false);
			} else if (TagConstant.ALBUM_FRIEND.equals(album.getRights()) && isFriend) {
				model.put("isCanShow", true);
			} else if (TagConstant.ALBUM_PUBLIC.equals(album.getRights()) || album.getMemberid().equals(loginMember.getId())) {
				model.put("isCanShow", true);
			}
		} else {
			model.put("isCanShow", true);
		}
		return null;
	}

	/**
	 * 用户相册图片详细
	 * 
	 * @param model
	 * @param albumid
	 * @param curAlbumPicId
	 * @return
	 */
	@RequestMapping("/home/new/album/imageDetailList.xhtml")
	public String imageDetailList(ModelMap model, Long albumid, Long curAlbumPicId) {

		Album album = daoService.getObject(Album.class, albumid);
		if (album == null) {
			return showError(model, "你访问的页面不存在！");
		}
		if (album.getCommuid() > 0) {
			return showError(model, "你访问的相册不存在！");
		}
		Picture picture = daoService.getObject(Picture.class, curAlbumPicId);
		if (picture == null) {
			return showError(model, "你访问的图片不存在！");
		}

		Member member = getLogonMember();
		Member albumMember = null;
		if (!member.getId().equals(album.getMemberid())) {
			model.put("isMy", "no");
			model.put("isMyFriend", friendService.isFriend(member.getId(), album.getMemberid()));
			albumMember = daoService.getObject(Member.class, album.getMemberid());
			model.putAll(friendService.isPrivate(album.getMemberid())); // 判断访问权限
		} else {
			albumMember = member;
		}
		model.putAll(controllerService.getCommonData(model, member, album.getMemberid()));
		isShowAlbumImage(albumMember, getLogonMember(), model, album);

		// 图片列表
		Member curAlbumMember = daoService.getObject(Member.class, album.getMemberid());
		List<Map> mapList = new ArrayList<Map>();
		List<Picture> pictureList = albumService.getPictureByAlbumId(albumid, 0, 500);
		pictureList.remove(picture);
		pictureList.add(0, picture);
		mapList = BeanUtil.getBeanMapList(pictureList, "id", "picturename", "posttime", "memberid", "description");
		for (Map map : mapList) {
			map.put("membername", curAlbumMember.getNickname());
		}
		model.put("album", album);
		model.put("mapList", mapList);
		model.put("pictureid", curAlbumPicId);
		model.put("curAlbumMember", curAlbumMember);
		model.put("curAlbumImage", picture);
		model.put("albumid", albumid);
		model.put("curAlbum", album);
		model.put("logonMember", member);
		this.getHomeLeftNavigate(daoService.getObject(MemberInfo.class, album.getMemberid()), model);
		return "sns/userAlbum/photoShow.vm";
	}

	/**
	 * 上传图片回调转到修改页面
	 * 
	 * @param realName
	 * @param successFile
	 * @param tag
	 * @param relatedid
	 * @param redirecturl
	 * @param albumid
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/home/new/album/uploadAlbumImages.xhtml")
	public String uploadAlbumImages(String realName, String successFile, HttpServletRequest request,
			String tag, Long relatedid, String redirecturl,	Long albumid, ModelMap model) throws Exception {
		Member member = getLogonMember();
		Object obj = relateService.getRelatedObject(tag, relatedid);
		if (obj == null){
			return goBack(model, "数据有误！");
		}
		if ("commu".equals(tag) && relatedid != null) {
			CommuMember commuMember = commuService.getCommuMemberByMemberidAndCommuid(member.getId(), relatedid);
			if (CommuMember.FLAG_BLACK.equals(commuMember.getFlag())) {
				return goBack(model, "你被关入小黑屋，暂时不能上传图片！");
			}
		}

		String[] fileNames = StringUtils.split(successFile, "@@");
		String[] fileFields = StringUtils.split(realName, "@@");
		List<Picture> albumImageList = new ArrayList<Picture>();// 用户，圈子上传图片
		List<Picture> pictureList = new ArrayList<Picture>();// 除去圈子，用户相册上传图片
		List<MemberPicture> mpList = new ArrayList<MemberPicture>();
		int index = 0;
		String path = PictureUtil.getAlbumPicpath();
		Album album = new Album();
		List<Long> pictureIdList = new ArrayList<Long>();
		for (String fileName : fileNames) {
			if (TagConstant.TAGList.contains(tag)) {
				gewaPicService.moveRemoteTempTo(member.getId(), tag, relatedid, path, fileName);
				MemberPicture mp = new MemberPicture(tag, relatedid, member.getId(), member.getNickname(), path + fileName, TagConstant.FLAG_PIC);
				mp.setName(fileFields[index]);
				mp.setDescription(fileFields[index]);
				daoService.saveObject(mp);
				mpList.add(mp);
				pictureIdList.add(mp.getId());
			} else if (specialTagList.contains(tag)) { // 圈子相册或用户相册
				gewaPicService.moveRemoteTempTo(member.getId(), "album", albumid, path, fileName);
				Picture ai = new Picture("album", albumid, member.getId(), path + fileName);
				ai.setMemberType(GewaraUser.USER_TYPE_MEMBER);
				ai.setName(fileFields[index]);
				ai.setDescription(fileFields[index]);
				daoService.saveObject(ai);
				album = daoService.getObject(Album.class, albumid);

				if (StringUtils.isBlank(album.getLogo())) {
					album.setLogo(ai.getLogo());
					daoService.saveObject(album);
				}
				albumImageList.add(ai);
				pictureIdList.add(ai.getId());
			} else if ("activity".equals(tag)) { // 其他的情况
				gewaPicService.moveRemoteTempTo(member.getId(), tag, relatedid, path, fileName);
				Picture picture = new Picture(tag);
				picture.setRelatedid(relatedid);
				picture.setMemberid(member.getId());
				picture.setMemberType(GewaraUser.USER_TYPE_MEMBER);
				picture.setName(fileFields[index]);
				picture.setDescription(fileFields[index]);
				picture.setPicturename(path + fileName);
				daoService.saveObject(picture);
				pictureList.add(picture);
				pictureIdList.add(picture.getId());
			}
			index++;
		}
		model.put("redirecturl", redirecturl);
		model.put("relatedtag", tag);
		if (!albumImageList.isEmpty()) {
			model.put("albumImageList", albumImageList);
			List<Album> albumList = daoService.getObjectList(Album.class, BeanUtil.getBeanPropertyList(albumImageList, Long.class, "albumid", true));
			model.put("albumsMap", BeanUtil.beanListToMap(albumList, "id"));
		}
		if (!pictureList.isEmpty()) {
			model.put("pictureList", pictureList);
		} else if (!mpList.isEmpty()) {
			model.put("pictureList", mpList);
		}
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		// 产生哇啦
		// 给哇啦数+1
		memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_COMMENTCOUNT, 1, true);
		// 给哇啦数+1
		String link = config.getAbsPath() + config.getBasePath() + "home/new/album/albumImageList.xhtml?albumid=" + albumid;
		String linkStr = "上传了" + fileNames.length + "张新照片至" + "<a href=\"" + link + "\" target=\"_blank\">" + album.getSubject() + "</a>";
		if("activity".equals(tag)){
			Object object = relateService.getRelatedObject("activity", relatedid);
			RemoteActivity activity = (RemoteActivity)object;
			link = config.getAbsPath() + config.getBasePath() + "activity/" + activity.getId();
			linkStr = "上传了" + fileNames.length + "张新照片至" + "<a href=\"" + link + "\" target=\"_blank\">" + activity.getTitle() + "</a>";
		}
		Map otherinfoMap = new HashMap();
		otherinfoMap.put("albumid", albumid);
		otherinfoMap.put("imagecount", fileFields.length);
		if (fileNames.length > 0 && pictureIdList.size() > 0) {
			otherinfoMap.put("image1", path + fileNames[0]);
			otherinfoMap.put("imageid1", pictureIdList.get(0));
		}
		if (fileNames.length > 1 && pictureIdList.size() > 1) {
			otherinfoMap.put("image2", path + fileNames[1]);
			otherinfoMap.put("imageid2", pictureIdList.get(1));
		}
		if (fileNames.length > 2 && pictureIdList.size() > 2) {
			otherinfoMap.put("image3", path + fileNames[2]);
			otherinfoMap.put("imageid3", pictureIdList.get(2));
		}
		if (fileNames.length > 3 && pictureIdList.size() > 3) {
			otherinfoMap.put("image4", path + fileNames[3]);
			otherinfoMap.put("imageid4", pictureIdList.get(3));
		}
		String otherinfo = JsonUtils.writeObjectToJson(otherinfoMap);
		String ip = WebUtils.getRemoteIp(request);
		ErrorCode<Comment> ec = null;
		if (TagConstant.TAGList.contains(tag)) {
			ec = commentService.addMicroComment(member, TagConstant.TAG_MEMBERPICTURE_MEMBER, null, linkStr, null, null, null, false, null,
					otherinfo, null, null, WebUtils.getIpAndPort(ip, request), null);
		} else {
			ec = commentService.addMicroComment(member, TagConstant.TAG_PICTURE_MEMBER, null, linkStr, null, null, null, false, null, otherinfo,
					null, null, WebUtils.getIpAndPort(ip, request), null);
		}
		if (ec != null) {
			if (ec.isSuccess()) {
				shareService.sendShareInfo("wala", ec.getRetval().getId(), ec.getRetval().getMemberid(), null);
			}
		}

		return "sns/userAlbum/photoEdit.vm";
	}

	/**
	 * 发表照片评论
	 * 
	 * @param imageid
	 * @param body
	 * @param model
	 * @return
	 */
	@RequestMapping("/home/new/album/saveAlbumComment.xhtml")
	public String saveAlbumComment(Long imageid, String body, ModelMap model) {
		Member member = getLogonMember();
		if (member == null)
			return showError(model, "请先登录!");
		Picture albumImage = daoService.getObject(Picture.class, imageid);
		if (albumImage == null)
			return alertMessage(model, "图片信息不正确！");
		Album album = daoService.getObject(Album.class, albumImage.getRelatedid());
		if (album == null)
			return alertMessage(model, "相册信息不正确！");
		if (body.length() > 200)
			return alertMessage(model, "内容太长！");
		AlbumComment albumComment = new AlbumComment(member.getId());
		albumComment.setAlbumid(album.getId());
		albumComment.setImageid(imageid);
		albumComment.setBody(body);
		daoService.saveObject(albumComment);
		model.put("comment", albumComment);
		addCacheMember(model, albumComment.getMemberid());

		if (album.getMemberid().equals(member.getId()) || albumComment.getMemberid().equals(member.getId())) {
			model.put("isShowCommuAlbum", true);
		}
		Member commentMember = daoService.getObject(Member.class, albumComment.getMemberid());
		model.put("commentMember", commentMember);
		return "sns/userAlbum/loadAlbumComment.vm";
	}

	/**
	 * 加载图片评论
	 * 
	 * @param sessid
	 * @param request
	 * @param imageid
	 * @param model
	 * @return
	 */
	@RequestMapping("/quan/new/album/albumCommentList.xhtml")
	public String albumCommentList(@CookieValue(value = LOGIN_COOKIE_NAME, required = false) String sessid, HttpServletRequest request, Long imageid,
			ModelMap model) {
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		Picture albumImage = daoService.getObject(Picture.class, imageid);
		if (albumImage == null)
			return alertMessage(model, "错误图片id！");
		Album album = daoService.getObject(Album.class, albumImage.getRelatedid());
		List<AlbumComment> imageCommentList = albumService.getPictureComment(imageid, 0, 30);
		if (imageCommentList.size() > 0) {
			List<Long> commentMemberIdList = ServiceHelper.getMemberIdListFromBeanList(imageCommentList);
			addCacheMember(model, commentMemberIdList);
		}
		if (member != null) {
			if (album.getMemberid().equals(member.getId())) {
				model.put("isShowCommuAlbum", true);
			}
		}
		model.put("albumid", albumImage.getId());
		model.put("imageid", imageid);
		model.put("logonMember", member);
		model.put("imageCommentList", imageCommentList);
		return "sns/userAlbum/albumImageComment.vm";
	}

}
