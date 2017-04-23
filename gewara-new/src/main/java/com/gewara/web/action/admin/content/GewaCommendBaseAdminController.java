package com.gewara.web.action.admin.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.gewara.mongo.MongoService;
import com.gewara.web.action.admin.BaseAdminController;

public class GewaCommendBaseAdminController extends BaseAdminController{
	@Autowired@Qualifier("mongoService")
	protected MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
}
