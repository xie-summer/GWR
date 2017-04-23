package com.gewara.untrans.sitemap;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gewara.util.DateUtil;


/**
 *    @function 话剧模块(话剧, 剧院, 明星etc.) 站点地图 
 * 	@author bob.hu
 *		@date	2011-03-28 15:54:10
 */
@Service("dramaSitemapGenService")
public class DramaSitemapGenService extends AbstractSitemapGenService implements SitemapGenService{
	public DramaSitemapGenService(){
		this.setFileName("/genfile/dramasitemap.xml");
	}
	@Override
	public void writeUrlList(Writer writer) throws IOException{
		String lastmod = DateUtil.formatDate(new Date());
		String loc;
		//0)
		loc = siteUrl + "drama/dramaList.xhtml";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "drama/dramaList.xhtml?dramatype=other";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "theatre/theatreList.xhtml";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "drama/star";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "drama/star?type=star";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "news/theatre";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "blog?tag=theatre";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		
		// 1) drama
		List<Long> dramaList = hibernateTemplate.find("select id from Drama");
		String[] moudels = new String[]{"/introduce", "/diarylist", "/picturelist", "/newslist"};
		for(Long ptId : dramaList){
			loc = siteUrl + "drama/" + ptId;
			this.writerUrl(writer, loc, lastmod, "daily","0.8");

			for(String moudel: moudels){
				loc = siteUrl + "drama/" + ptId + moudel;
				this.writerUrl(writer, loc, lastmod, "daily","0.8");
			}
		}
		// 2) theatre 
		List<Long> theatreList = hibernateTemplate.find("select id from Theatre");
		String[] moudels2 = new String[]{"/introduce", "/newslist", "/pricetable", "/picturelist", "/commentlist"};
		for(Long ptId : theatreList){
			loc = siteUrl + "theatre/" + ptId;
			this.writerUrl(writer, loc, lastmod, "daily","0.8");

			for(String moudel: moudels2){
				loc = siteUrl + "theatre/" + ptId + moudel;
				this.writerUrl(writer, loc, lastmod, "daily","0.8");
			}
		}
		// 3) star 
		List<Long> starList = hibernateTemplate.find("select id from DramaStar");
		String[] moudels3 = new String[]{"/introduce", "/news", "/pricetable", "/picturelist", "/commentlist"};
		for(Long ptId : starList){
			loc = siteUrl + "drama/star/" + ptId;
			this.writerUrl(writer, loc, lastmod, "daily","0.8");

			for(String moudel: moudels3){
				loc = siteUrl + "drama/star/" + ptId + moudel;
				this.writerUrl(writer, loc, lastmod, "daily","0.8");
			}
		}
	}
}
