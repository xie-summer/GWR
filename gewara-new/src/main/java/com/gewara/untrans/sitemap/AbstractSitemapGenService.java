package com.gewara.untrans.sitemap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.gewara.untrans.GewaPicService;

public abstract class AbstractSitemapGenService implements SitemapGenService, ApplicationContextAware{
	@Autowired@Qualifier("hibernateTemplate")
	protected HibernateTemplate hibernateTemplate;
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	public void setGewaPicService(GewaPicService gewaPicService) {
		this.gewaPicService = gewaPicService;
	}
	private WebApplicationContext ctx; 
	public void setApplicationContext(ApplicationContext ctx){
		this.ctx = (WebApplicationContext)ctx;
	}
	public String getRealPath(String filename){
		return ctx.getServletContext().getRealPath(filename);
	}
	protected static final String siteUrl = "http://www.gewara.com/";
	protected void writerHeader(Writer writer) throws IOException{
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.write("<urlset xmlns=\"http://www.google.com/schemas/sitemap/0.84\">");
	}
	protected void writerUrl(Writer writer, String loc, String lastmod, String changefreq, String priority) throws IOException{
		writer.append("<url><loc>").append(loc).append("</loc>")
			.append("<lastmod>").append(lastmod).append("</lastmod>") 
			.append("<changefreq>").append(changefreq).append("</changefreq>")
			.append("<priority>").append(priority).append("</priority>")
			.append("</url>");
	}
	protected void writeEnd(Writer writer) throws IOException{
		writer.write("</urlset>");
	}
	protected String fileName;
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public void genSitemap(){
		File xmlFile = new File(getRealPath(this.getFileName()));
		try {
			if(xmlFile.exists()) xmlFile.delete();//奇怪，为何追加内容？
			Writer writer = new BufferedWriter(new FileWriter(xmlFile));
			writerHeader(writer);//1. 写头
			writeUrlList(writer);
			writeEnd(writer);//写尾
			writer.close();
			gewaPicService.saveToRemote(xmlFile, this.getFileName(), true);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	public abstract void writeUrlList(Writer writer) throws IOException;
}
