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
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
@Service("everyDaySitemapGenService")
public class EveryDaySitemapGenService extends AbstractSitemapGenService implements SitemapGenService{
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public EveryDaySitemapGenService(){
		this.setFileName("/genfile/newsitemap.xml");
	}
	@Override
	public void writeUrlList(Writer writer) throws IOException{
		String lastmod = DateUtil.formatDate(new Date());
		String loc;
		Timestamp firstDay = new Timestamp(DateUtil.getMonthFirstDay(new Date()).getTime());
		//1)影院
		List<Long> idList = hibernateTemplate.find("select c.id from Cinema c where c.addtime > ? ", firstDay);
		for(Long id:idList){
			loc = siteUrl + "cinema/" + id;
			this.writerUrl(writer, loc, lastmod, "hourly","0.8");
		}
		//5)运动场所
		idList = hibernateTemplate.find("select c.id from Sport c where c.addtime > ? ", firstDay);
		for(Long id:idList){
			loc = siteUrl + "sport/" + id;
			this.writerUrl(writer, loc, lastmod, "hourly","0.8");
		}
		//6)电影
		idList = hibernateTemplate.find("select c.id from Movie c where c.addtime > ? ", firstDay);
		for(Long id:idList){
			loc = siteUrl + "movie/" + id;
			this.writerUrl(writer, loc, lastmod, "hourly","0.8");
		}
		//7)Diary
		List<Long> topicList = hibernateTemplate.find("select id from Diary where addtime > ? ", firstDay);
		for(Long did:topicList){
			loc = siteUrl + "blog/t" + did;
			this.writerUrl(writer, loc, lastmod, "daily","0.8");
		}
		//8)Activity
		List<String> activityIdList = synchActivityService.getActivityIdList(firstDay, null);
		for(String aid:activityIdList){
			loc = siteUrl + "activity/" + aid;
			this.writerUrl(writer, loc, lastmod, "daily","0.8");
		}
	}
}
