/**
 * 
 */
package com.gewara.web.action.admin.partner;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.partner.PartnerCinema;
import com.gewara.model.partner.PartnerMovie;
import com.gewara.util.ApiUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.StringUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.xmlbind.partner.PartnerCinemaList;
import com.gewara.xmlbind.partner.PartnerMovieList;
import com.gewara.xmlbind.partner.QQCinema;
import com.gewara.xmlbind.partner.QQCity;
import com.gewara.xmlbind.partner.QQMovie;

@Controller
public class PartnerQQAdminController extends BaseAdminController {
	public static final Long PARTNER_QQ = 50000180L;		//腾讯
	private static final String CINEMA_URL = "http://imgcache.qq.com/club/movie_channel/js/data/subs/dianying2/supplier_city_cinema.xml";
	private static final String MOVIE_URL = "http://imgcache.qq.com/club/movie_channel/js/data/subs/dianying2/supplier_movie.xml";
	//TODO:重构  /admin/ticket/outdata/qq/....
	@RequestMapping("/admin/qq/movies.xhtml")
	public String getMovieList(ModelMap model) throws Exception {
		synQQmovie();
		return showJsonSuccess(model);
	}

	@RequestMapping("/synch/qq/movies.xhtml")
	@ResponseBody
	public String synchMovieList(String check) throws Exception {
		String strdate = DateUtil.formatDate(new Date());
		strdate = StringUtil.md5(strdate);
		if (!StringUtils.equalsIgnoreCase(check, strdate))
			return "check error";
		synQQmovie();
		return "success";
	}

	private void synQQmovie() throws Exception {
		HttpResult code = HttpUtils.getUrlAsString(MOVIE_URL, null, "gbk");

		BeanReader beanReader = ApiUtils.getBeanReader("movies", PartnerMovieList.class);
		PartnerMovieList pmList = (PartnerMovieList) ApiUtils.xml2Object(beanReader, code.getResponse());
		for (QQMovie qm : pmList.getMovieList()) {
			PartnerMovie pm = addMovie(qm.getId() + "");
			if (pm == null) {
				pm = new PartnerMovie();
				pm.setPmid(qm.getId() + "");
				pm.setPartnerid(PARTNER_QQ);
			}
			pm.setPmname(qm.getName());
			daoService.saveObject(pm);
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_PARTNER, "同步腾讯电影：" + qm.getName());
		}
	}

	@RequestMapping("/admin/qq/cinemas.xhtml")
	public String getCinemaList(ModelMap model) throws Exception {
		synQQcinema();
		return showJsonSuccess(model);
	}

	@RequestMapping("/synch/qq/cinemas.xhtml")
	@ResponseBody
	public String synchCinemaList(String check) throws Exception {
		String strdate = DateUtil.formatDate(new Date());
		strdate = StringUtil.md5(strdate);
		if (!StringUtils.equalsIgnoreCase(check, strdate))
			return "check error";
		synQQcinema();
		return "success";
	}

	private void synQQcinema() throws Exception {
		HttpResult code = HttpUtils.getUrlAsString(CINEMA_URL, null, "gbk");
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_PARTNER, code.getResponse());
		BeanReader beanReader = ApiUtils.getBeanReader("cities", PartnerCinemaList.class);
		PartnerCinemaList pcList = (PartnerCinemaList) ApiUtils.xml2Object(beanReader, code.getResponse());
		for (QQCity qc : pcList.getCityList()) {
			for (QQCinema qcinema : qc.getCinemaList()) {
				PartnerCinema pc = addCinema(qcinema.getId() + "");
				if (pc == null) {
					pc = new PartnerCinema();
					pc.setPcid(qcinema.getId() + "");
					pc.setCityname(qc.getName());
					pc.setPartnerid(PARTNER_QQ);
					dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_PARTNER, "同步腾讯影院：" + qcinema.getName());
				}
				pc.setPcname(qcinema.getName());
				daoService.saveObject(pc);
			}
		}
	}

	private PartnerMovie addMovie(String pmid) {
		String qry = "from PartnerMovie m where m.pmid=? and m.partnerid=?";
		List<PartnerMovie> mList = hibernateTemplate.find(qry, pmid, PARTNER_QQ);
		if (mList.size() > 0)
			return mList.get(0);
		return null;
	}

	private PartnerCinema addCinema(String pcid) {
		String qry = "from PartnerCinema m where m.pcid=? and m.partnerid=?";
		List<PartnerCinema> cList = hibernateTemplate.find(qry, pcid, PARTNER_QQ);
		if (cList.size() > 0)
			return cList.get(0);
		return null;
	}

	@RequestMapping("/admin/qq/movieList.xhtml")
	public String getMovies(ModelMap model) {
		String qry = "from PartnerMovie m order by m.id desc";
		List<PartnerMovie> movieList = hibernateTemplate.find(qry);
		Map<Long, Movie> movieMap = new HashMap<Long, Movie>();
		for (PartnerMovie pm : movieList) {
			if (pm.getMovieid() != null) {
				Movie movie = daoService.getObject(Movie.class, pm.getMovieid());
				movieMap.put(pm.getId(), movie);
			}
		}
		model.put("movieMap", movieMap);
		model.put("movieList", movieList);
		return "admin/ticket/outdata/qqMovieList.vm";
	}

	@RequestMapping("/admin/qq/cinemaList.xhtml")
	public String getCinemas(ModelMap model) {
		String qry = "from PartnerCinema m order by m.cityname";
		List<PartnerCinema> cinemaList = hibernateTemplate.find(qry);
		Map<Long, Cinema> cinemaMap = new HashMap<Long, Cinema>();
		for (PartnerCinema pc : cinemaList) {
			if (pc.getCinemaid() != null) {
				Cinema cinema = daoService.getObject(Cinema.class, pc.getCinemaid());
				cinemaMap.put(pc.getId(), cinema);
			}
		}
		model.put("cinemaMap", cinemaMap);
		model.put("cinemaList", cinemaList);
		return "admin/ticket/outdata/qqCinemaList.vm";
	}

	@RequestMapping("/admin/qq/setMovieUnion.xhtml")
	public String setMovieUnion(Long id, Long movieid, ModelMap model) {
		String qry = "from PartnerMovie m where m.movieid=? and m.partnerid=?";
		List list = hibernateTemplate.find(qry, movieid, PARTNER_QQ);
		if (list.size() > 0)
			return showJsonError(model, "不能重复添加，请核对！");
		PartnerMovie pm = daoService.getObject(PartnerMovie.class, id);
		if (pm.getMovieid() != null)
			return showJsonError(model, "不能修改，请联系技术人员！");
		Movie movie = daoService.getObject(Movie.class, movieid);
		pm.setMovieid(movie.getId());
		daoService.saveObject(pm);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_PARTNER, getLogonUser().getId() + "对应影片：" + movieid + "<--->" + pm.getPmid());
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/qq/setCinemaUnion.xhtml")
	public String setCinemaUnion(Long id, Long cinemaid, ModelMap model) {
		String qry = "from PartnerCinema m where m.cinemaid=? and m.partnerid=?";
		List list = hibernateTemplate.find(qry, cinemaid, PARTNER_QQ);
		if (list.size() > 0)
			return showJsonError(model, "不能重复添加，请核对！");
		PartnerCinema pc = daoService.getObject(PartnerCinema.class, id);
		if (pc.getCinemaid() != null)
			return showJsonError(model, "不能修改，请联系技术人员！");
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		pc.setCinemaid(cinema.getId());
		daoService.saveObject(pc);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_PARTNER, getLogonUser().getId() + "对应影院：" + cinemaid + "<--->" + pc.getPcid());
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/qq/getObject.xhtml")
	public String getObject(Long relatedid, String tag, ModelMap model) {
		String name = "";
		if (StringUtils.equals(tag, "movie")) {
			Movie movie = daoService.getObject(Movie.class, relatedid);
			if (movie != null)
				name = movie.getMoviename();
		} else if (StringUtils.equals(tag, "cinema")) {
			Cinema cinema = daoService.getObject(Cinema.class, relatedid);
			if (cinema != null)
				name = cinema.getName();
		}
		if (StringUtils.isBlank(name))
			return showJsonError(model, "数据不存在，请核对id");
		return showJsonSuccess(model, name);
	}

	@RequestMapping("/admin/qq/delObject.xhtml")
	public String getObject(Long id, ModelMap model) {
		PartnerMovie pm = daoService.getObject(PartnerMovie.class, id);
		if (pm != null) {
			daoService.removeObject(pm);
		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/qq/delCinemaObject.xhtml")
	public String getCinemaObject(Long id, ModelMap model) {
		PartnerCinema pc = daoService.getObject(PartnerCinema.class, id);
		if (pc != null) {
			daoService.removeObject(pc);
		}
		return showJsonSuccess(model);
	}
}
