package com.gewara.untrans.sitemap;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.springframework.stereotype.Service;

import com.gewara.model.bbs.commu.Commu;
import com.gewara.util.DateUtil;



/**
 *    @function È¦×Ó Õ¾µãµØÍ¼ 
 * 	@author bob.hu
 *		@date	2011-07-11 16:04:08
 */
@Service("commuSitemapGenService")
public class CommuSitemapGenService extends AbstractSitemapGenService implements SitemapGenService{
	public CommuSitemapGenService(){
		this.setFileName("/genfile/commusitemap.xml");
	}
	@Override
	public void writeUrlList(Writer writer) throws IOException{
		String lastmod = DateUtil.formatDate(new Date());
		String loc;
		//0)
		loc = siteUrl + "quan/";
		this.writerUrl(writer, loc, lastmod, "always", "1.0");
		// 1) Commu
		DetachedCriteria query = DetachedCriteria.forClass(Commu.class);
		query.setProjection(Projections.id());
		query.addOrder(Order.desc("clickedtimes"));
		List<Long> commuList = hibernateTemplate.findByCriteria(query, 0, 45000);
		for(Long commuid : commuList){
			loc = siteUrl + "quan/" + commuid;
			this.writerUrl(writer, loc, lastmod, "daily", "0.8");
		}
	}
}
