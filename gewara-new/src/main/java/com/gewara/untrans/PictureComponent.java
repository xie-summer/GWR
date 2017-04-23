package com.gewara.untrans;

import java.util.Map;

import org.springframework.ui.ModelMap;


public interface PictureComponent {
	Map getHeadData(String tag, Long relatedid);
	void pictureDetail(ModelMap model,String tag, Long relatedid, Long pid, String type);
	Map attachRelatePicture(String tag, Long relatedid, String citycode);
	void pictureList(ModelMap model,Integer pageNo, String tag, Long relatedid, String type, String url);
	Map getCommonData(String tag,String citycode, Long relatedid);
}
