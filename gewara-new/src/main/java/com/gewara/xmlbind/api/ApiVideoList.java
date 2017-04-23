package com.gewara.xmlbind.api;

import java.util.ArrayList;
import java.util.List;

import com.gewara.model.content.Video;

public class ApiVideoList {
	
	private  List<Video> videoList = new ArrayList<Video>();

	public List<Video> getVideoList() {
		return videoList;
	}

	public void setVideoList(List<Video> videoList) {
		this.videoList = videoList;
	}
	
	public void addVideo(Video video)
	{
		this.videoList.add(video);
	}

}
