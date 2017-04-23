package com.gewara.web.action.inner.movie;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.movie.Movie;
import com.gewara.service.movie.MCPService;
import com.gewara.web.action.api.BaseApiController;
@Controller
public class ApiMoiveController extends BaseApiController{
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	@RequestMapping("/inner/movie/getMoiveByName.xhtml")
	public String getMoiveByName(String moviename, ModelMap model) {
		if(StringUtils.isBlank(moviename)) return getXmlView(model, "inner/movie/movieList.vm");
		List<Long> movieidList = mcpService.getMovieIdByMoviename(moviename);
		List<Movie> movieList = daoService.getObjectList(Movie.class, movieidList);
		model.put("movieList", movieList);
		return getXmlView(model, "inner/movie/movieList.vm");
	}
}
