package com.gewara.xmlbind.api;

import java.util.ArrayList;
import java.util.List;

import com.gewara.model.content.Picture;

public class ApiPictureList {
	
	private  List<Picture> pictureList = new ArrayList<Picture>();

	public List<Picture> getPictureList() {
		return pictureList;
	}

	public void setPictureList(List<Picture> pictureList) {
		this.pictureList = pictureList;
	}
	
	public void addPicture(Picture picture)
	{
		this.pictureList.add(picture);
	}

}
