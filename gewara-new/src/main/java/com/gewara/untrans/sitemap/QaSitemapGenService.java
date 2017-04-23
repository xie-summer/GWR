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
@Service("qaSitemapGenService")
public class QaSitemapGenService extends AbstractSitemapGenService implements SitemapGenService{
	public QaSitemapGenService(){
		this.setFileName("/genfile/qasitemap.xml");
	}
	@Override
	public void writeUrlList(Writer writer) throws IOException{
		String lastmod = DateUtil.formatDate(new Date());
		String loc;
		//0)
		loc = siteUrl + "qa";
		this.writerUrl(writer, loc, lastmod, "always","1.0");
		//1)QA
		List<Long> qaIdList = hibernateTemplate.find("select c.id from GewaQuestion c");
		for(Long qaId:qaIdList){
			loc = siteUrl + "qa/q" + qaId;
			this.writerUrl(writer, loc, lastmod, "hourly","0.8");
		}
	}
}
