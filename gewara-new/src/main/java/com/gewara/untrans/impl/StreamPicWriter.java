package com.gewara.untrans.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.io.IOUtils;

import com.gewara.untrans.GewaPicService;

public class StreamPicWriter implements GewaPicService.StreamWriter{
	private OutputStream os;
	public StreamPicWriter(OutputStream os){
		this.os = os;
	}

	@Override
	public void write(InputStream is, Long lastModifyTime, Integer maxage, Long expireTime) throws IOException {
		IOUtils.copyBytes(is, os, 4096, false);
		os.flush();
		os.close();
	}

	@Override
	public void write(InputStream is, Long lastModifyTime) throws IOException {
		IOUtils.copyBytes(is, os, 4096, false);
		os.flush();
		os.close();
	}
}

