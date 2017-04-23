package com.gewara.untrans.sitemap;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gewara.util.DateUtil;

/**
 * 电影模块站点地图, 包含影院的各个信息页面、增加日期小于今天的影片页面
 * @author acerge(acerge@163.com)
 * @since 11:44:47 AM Jul 30, 2009
 */
@Service("sportSitemapGenService")
public class SportSitemapGenService extends AbstractSitemapGenService implements SitemapGenService{
	public SportSitemapGenService(){
		this.setFileName("/genfile/sportsitemap.xml");
	}
	@Override
	public void writeUrlList(Writer writer) throws IOException{
		String lastmod = DateUtil.formatDate(new Date());
		String loc;
		//0)
		loc = siteUrl + "sport/sportList.xhtml";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "sport/itemList.xhtml";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "sport/itemDiaryList.xhtml";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		List<Long> ptList = hibernateTemplate.find("select id from Sport");
		String[] moudels = new String[]{"/picturelist"};
		for(Long ptId:ptList){
			for(String moudel: moudels){
				loc = siteUrl + "sport/" + ptId + moudel;
				this.writerUrl(writer, loc, lastmod, "daily","0.8");
			}
		}
		List<Long> itemList = hibernateTemplate.find("select t.id from SportItem t");
		moudels = new String[]{"/rookielist", "/masterlist", "/pricetable", "/diarylist", "/librarylist"};
		for(Long itemId:itemList){
			loc = siteUrl + "sport/item/" + itemId;
			this.writerUrl(writer, loc, lastmod, "daily","0.8");
			for(String moudel: moudels){
				loc = siteUrl + "sport/item/" + itemId + moudel;
				this.writerUrl(writer, loc, lastmod, "daily","0.8");
			}
		}
	}
}
