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
import com.gewara.model.user.Member;
import com.gewara.untrans.CommonService;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.AnnotationController;
/**
 * 世界末日专题
 * @author user
 *
 */
@Controller
public class Doomsday2012ProxyController extends AnnotationController{
	@Autowired@Qualifier("commonService")
	protected CommonService commonService;
	
	/**
	 * 
	 * 获取网友添加的最新电影
	 * 
	 * */
	@RequestMapping("/subject/proxy/getMoviesList.xhtml")
	public String getTempmoviesList(ModelMap model,int fromnum,int maxnum){
		List<TempMovie> dataMap =commonService.getTempMovieList("treated", "passed", null, null,null,null,fromnum, maxnum);
		List<Map> list = new ArrayList<Map>();
		for(TempMovie tmovie : dataMap){
			Movie movie = daoService.getObjectByUkey(Movie.class, "moviename", tmovie.getMoviename(), true);
			Map<String,String> movieMap = new HashMap<String,String>();
			if(movie != null){
				movieMap.put("moviename", movie.getMoviename());
				movieMap.put("memberid",tmovie.getMemberid()+"");
				movieMap.put("logo",movie.getLogo());
				movieMap.put("id", movie.getId()+"");
				movieMap.put("memberNickName", daoService.getObject(Member.class, tmovie.getMemberid()).getNickname());
				list.add(movieMap);
			}
			if(list.size() == 4){
				break;
			}
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(list));
	}
}
