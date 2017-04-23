package com.gewara.web.action.subject;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.TagConstant;
import com.gewara.model.movie.Movie;
import com.gewara.service.bbs.MarkService;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;
import com.gewara.web.action.AnnotationController;

@Controller
public class JusticePKController extends AnnotationController{
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@RequestMapping("/admin/newsubject/justicePk.xhtml")
	public String spiderman(){
		return "admin/newsubject/justice.vm";
	}
	@RequestMapping("/subject/proxy/justice/getMovie.xhtml")
	public String getMovie(ModelMap model,Long movieId){
		Movie movie= daoService.getObject(Movie.class, movieId);
		Map map = new HashMap();
		map.put("id", movie.getId());
		map.put("boughtcount", movie.getBoughtcount());
		map.put("gmark", VmUtils.getLastMarkStar(movie, "general",markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, movieId),markService.getMarkdata(TagConstant.TAG_MOVIE)));
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(map));
	}
}
