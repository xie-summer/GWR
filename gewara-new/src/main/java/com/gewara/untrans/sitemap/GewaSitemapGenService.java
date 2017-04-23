package com.gewara.untrans.sitemap;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gewara.util.DateUtil;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
@Service("gewaSitemapGenService")
public class GewaSitemapGenService extends AbstractSitemapGenService implements SitemapGenService{
	public GewaSitemapGenService(){
		this.setFileName("/genfile/gewarasitemap.xml");
	}
	@Override
	public void writeUrlList(Writer writer) throws IOException{
		String lastmod = DateUtil.formatDate(new Date());
		String loc;
		//0)
		loc = siteUrl + "index.xhtml";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "movie/index.xhtml";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "sport/index.xhtml";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "movie/searchMovie.xhtml";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "movie/futureMovie.xhtml";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "movie/searchCinema.xhtml";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		
		//1)影院
		List<Long> idList = hibernateTemplate.find("select c.id from Cinema c");
		for(Long id: idList){
			loc = siteUrl + "cinema/" + id;
			this.writerUrl(writer, loc, lastmod, "hourly","0.8");
		}
		List<Long> movieIdList = hibernateTemplate.find("select m.id from Movie m");
		for(Long movieId:movieIdList){
			loc = siteUrl + "movie/" + movieId;
			this.writerUrl(writer, loc, lastmod, "hourly","1.0");
		}
		//2)运动场所
		idList = hibernateTemplate.find("select c.id from Sport c");
		for(Long id: idList){
			loc = siteUrl + "sport/" + id;
			this.writerUrl(writer, loc, lastmod, "hourly","0.8");
		}
	}
}
