package com.gewara.untrans.sitemap;

import java.io.IOException;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.DateUtil;

/**
 * 电影模块站点地图, 包含影院的各个信息页面、增加日期小于今天的影片页面
 * @author acerge(acerge@163.com)
 * @since 11:44:47 AM Jul 30, 2009
 */
@Service("blogSitemapGenService")
public class BlogSitemapGenService extends AbstractSitemapGenService implements SitemapGenService{
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public BlogSitemapGenService(){
		this.setFileName("/genfile/blogsitemap.xml");
	}
	@Override
	public void writeUrlList(Writer writer) throws IOException{
		String lastmod = DateUtil.formatDate(new Date());
		String loc;
		//0)
		loc = siteUrl + "blog";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "blog/cinema";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "blog/gym";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "blog/sport";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		loc = siteUrl + "activity";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		
		Timestamp enddate = DateUtil.getCurFullTimestamp();
		Timestamp begindate = DateUtil.addDay(enddate, -180);
		//1)Diary
		List<Long> topicList = hibernateTemplate.find("select id from Diary where addtime > ? and addtime <= ? order by addtime desc", begindate, enddate);
		for(Long did:topicList){
			loc = siteUrl + "blog/t" + did;
			this.writerUrl(writer, loc, lastmod, "hourly","0.8");
		}
		//2)Activity
		List<String> activityIdList = synchActivityService.getActivityIdList(begindate, enddate);
		for(String aid:activityIdList){
			loc = siteUrl + "activity/" + aid;
			this.writerUrl(writer, loc, lastmod, "hourly","0.8");
		}
	}
}
