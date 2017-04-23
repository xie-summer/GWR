package com.gewara.web.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.io.IOUtils;

import com.gewara.untrans.GewaPicService;
import com.gewara.util.DateUtil;

public class ResponseStreamWriter implements GewaPicService.StreamWriter{
	private HttpServletResponse res;
	private String etag;
	private String contentType;
	public ResponseStreamWriter(HttpServletResponse res, String etag, String contentType){
		this.res = res;
		this.etag = etag;
		this.contentType = contentType;
	}

	@Override
	public void write(InputStream is, Long lastModifyTime, Integer maxage, Long expireTime) throws IOException {
		res.resetBuffer();
		res.setContentType(contentType);
		res.addHeader("Cache-Control", "max-age=" + maxage);
		res.addHeader("Etag", "f" + etag.hashCode() + "-" + lastModifyTime);
		res.setDateHeader("Last-Modified", lastModifyTime);
		res.setDateHeader("Expires", expireTime);
		res.setHeader("Content-Length", ""+is.available());
		OutputStream os = res.getOutputStream();
		IOUtils.copyBytes(is, os, 4096, false);
		os.flush();
		os.close();
	}

	@Override
	public void write(InputStream is, Long lastModifyTime) throws IOException {
		Integer maxage = 5184000;
		Long expireTime = DateUtil.addDay(new Date(), 60).getTime();
		write(is, lastModifyTime, maxage, expireTime);
	}
}
