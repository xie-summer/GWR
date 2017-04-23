package com.gewara.web.action.subject;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.activity.RemoteActivity;


@Controller
public class HimalayaSummerProxyController extends AnnotationController {
	@Autowired@Qualifier("controllerService")
	protected ControllerService controllerService;
	public void setControllerService(ControllerService controllerService) {
		this.controllerService = controllerService;
	}
	@Autowired@Qualifier("diaryService")
	protected DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("blogService")
	protected BlogService blogService;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	@RequestMapping("/subject/proxy/getHimalaya.xhtml")
	public String getTime(Long aid, ModelMap model){
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(aid);
		RemoteActivity activity = code.getRetval();
		if(activity == null) return showJsonError(model, "暂无此活动相关信息！");
		Long robTime = DateUtil.parseTimestamp("2012-07-31 13:30:00").getTime();
		Long startTime = activity.getFromtime().getTime();
		Long endTime = activity.getDuetime().getTime();
		Long curTime = System.currentTimeMillis();
		Map map = new HashMap();
		map.put("remain",robTime - curTime);
		map.put("startTime",startTime);
		map.put("curTime",curTime);
		map.put("endTime", endTime);
		map.put("robTime", robTime);
		return showJsonSuccess(model,map);
	}
}
