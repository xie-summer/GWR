package com.gewara.web.action.subject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.movie.Movie;
import com.gewara.model.movie.TempMovie;
import com.gewara.untrans.CommonService;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.AnnotationController;
/**
 * 代理查询万圣节专题
 * @author Bruce(liuyunxin)
 * @date 2012/10/22
 * */
@Controller
public class HalloweenProxyController extends AnnotationController {
	@Autowired@Qualifier("commonService")
	protected CommonService commonService;
	
	@RequestMapping("/admin/newsubject/hallowsDay.xhtml")
	public String hallowsDay(){
		return "/admin/newsubject/hallowsDay.vm";
	}
		
	/**
	 * 
	 * 获取网友添加电影前6部
	 * 
	 * */
	@RequestMapping("/subject/proxy/getTempmoviesList.xhtml")
	public String getTempmoviesList(ModelMap model, Integer point, String type, int fromnum, int maxnum){
		List<TempMovie> dataMap =commonService.getTempMovieList("treated", "passed", null, null, type, point, fromnum, maxnum);
//		List<String> movieNamels = BeanUtil.getBeanPropertyList(dataMap, String.class, "moviename", true);
		List<Map> list = new ArrayList<Map>();
		List<Long> movieIdList = new ArrayList<Long>();
		for(TempMovie tmovie : dataMap){
			Movie movie = daoService.getObjectByUkey(Movie.class, "moviename", tmovie.getMoviename(), true);
			Map<String,String> movieMap = new HashMap<String,String>();
			if(movie != null){
				if(movieIdList.contains(movie.getId())) continue;
				movieMap.put("moviename", movie.getMoviename());
				movieMap.put("memberid",tmovie.getMemberid()+"");
				movieMap.put("actors",movie.getActors());
				movieMap.put("director",movie.getDirector());
				movieMap.put("logo",movie.getLimg());
				if(movie.getReleasedate()!=null){
					movieMap.put("releasedate",movie.getReleasedate()+"");
				}
				movieMap.put("content",movie.getContent());
				movieMap.put("state", tmovie.getState());
				movieMap.put("id", movie.getId()+"");
				movieIdList.add(movie.getId());
				list.add(movieMap);
			}
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(list));
	}
}
