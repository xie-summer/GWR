package com.gewara.web.action.inner.drama;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.model.content.Picture;
import com.gewara.model.content.Video;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.service.drama.DramaService;
import com.gewara.service.drama.DramaStarService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.web.action.api.BaseApiController;

@Controller
public class ApiDramaController extends BaseApiController {

	@Autowired@Qualifier("dramaStarService")
	private DramaStarService dramaStarService;
	@Autowired@Qualifier("dramaService")
	private DramaService dramaService;
	
	@RequestMapping("/inner/drama/troupeList.xhtml")
	public String getStarList(ModelMap model){
		List<DramaStar> starList = dramaStarService.getStarList(null, null, DramaStar.TYPE_TROUPE, 0, 1000);
		model.put("starList", starList);
		return getXmlView(model, "inner/drama/troupeList.vm");
	}
	@RequestMapping("/inner/drama/qryDrama.xhtml")
	public String qryOrder(String lastTime, Long tid, ModelMap model){
		if(StringUtils.isBlank(lastTime) || tid == null) return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "参数错误！");
		DramaStar star = daoService.getObject(DramaStar.class, Long.valueOf(tid));
		if(star == null) return getErrorXmlView(model, ApiConstant.CODE_OPI_NOT_EXISTS, "此社团不合法，请联系gewara相关人员！");
		List<Drama> dramaList = dramaService.getDramaListByTroupeCompany("310000", DateUtil.parseTimestamp(lastTime), star.getName());
		Map<Long,String> troupeIdMap = new HashMap<Long,String>();
		Map<Long, List<Picture>> pictureMap = new HashMap<Long, List<Picture>>();
		Map<Long, List<Video>> videoMap = new HashMap<Long, List<Video>>();
		if(dramaList != null){
			for(Drama drama : dramaList){
				if(StringUtils.isNotBlank(drama.getTroupecompany())){
					List<Long> companyIdList = BeanUtil.getIdList(drama.getTroupecompany(), ",");
					List<DramaStar> dramaStarList = daoService.getObjectList(DramaStar.class, companyIdList);
					troupeIdMap.put(drama.getId(), BeanUtil.getBeanPropertyList(dramaStarList, Long.class, "id", true).toString().replace("[", "").replace("]", "").replace(" ", ""));
				}
				List<Picture> pictureList = daoService.getObjectListByField(Picture.class, "relatedid", drama.getId());
				pictureMap.put(drama.getId(), pictureList);
				List<Video> videoList = daoService.getObjectListByField(Video.class, "relatedid", drama.getId());
				videoMap.put(drama.getId(), videoList);
			}
		}
		model.put("pictureMap", pictureMap);
		model.put("videoMap", videoMap);
		model.put("dramaList", dramaList);
		model.put("troupeIdMap", troupeIdMap);
		return getXmlView(model, "api2/drama/dramaList.vm");
	}
}
