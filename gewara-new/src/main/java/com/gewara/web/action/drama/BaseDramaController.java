package com.gewara.web.action.drama;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.Config;
import com.gewara.service.PlaceService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.bbs.QaService;
import com.gewara.service.content.PictureService;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.DramaService;
import com.gewara.service.drama.DramaStarService;
import com.gewara.service.drama.DramaToStarService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.service.member.TreasureService;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.PictureComponent;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.web.action.AnnotationController;

public class BaseDramaController extends AnnotationController {
	@Autowired@Qualifier("commentService")
	protected CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	@Autowired@Qualifier("placeService")
	protected PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}

	@Autowired@Qualifier("commonService")
	protected CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	@Autowired@Qualifier("qaService")
	protected QaService qaService;
	public void setQaService(QaService qaService) {
		this.qaService = qaService;
	}
	@Autowired@Qualifier("dramaService")
	protected DramaService dramaService;
	public void setDramaService(DramaService dramaService) {
		this.dramaService = dramaService;
	}
	@Autowired@Qualifier("diaryService")
	protected DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("synchActivityService")
	protected SynchActivityService synchActivityService;
	
	@Autowired@Qualifier("synchGymService")
	protected SynchGymService synchGymService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	@Autowired@Qualifier("dramaStarService")
	protected DramaStarService dramaStarService;
	public void setDramaStarService(DramaStarService dramaStarService) {
		this.dramaStarService = dramaStarService;
	}

	@Autowired@Qualifier("treasureService")
	protected TreasureService treasureService;
	@Autowired@Qualifier("dramaToStarService")
	protected DramaToStarService dramaToStarService;
	public void setDramaTOStarService(DramaToStarService dramaToStarService) {
		this.dramaToStarService = dramaToStarService;
	}
	@Autowired@Qualifier("pictureService")
	protected PictureService pictureService;
	public void setPictureService(PictureService pictureService) {
		this.pictureService = pictureService;
	}
	@Autowired@Qualifier("pictureComponent")
	protected PictureComponent pictureComponent = null;
	public void setPictureComponent(PictureComponent pictureComponent) {
		this.pictureComponent = pictureComponent;
	}
	@Autowired@Qualifier("config")
	protected Config config;
	@Autowired@Qualifier("openDramaService")
	protected OpenDramaService openDramaService;
	@Autowired@Qualifier("dramaPlayItemService")
	protected DramaPlayItemService dramaPlayItemService;
}
