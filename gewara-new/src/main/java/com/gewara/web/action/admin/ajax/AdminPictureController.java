package com.gewara.web.action.admin.ajax;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.BaseObject;
import com.gewara.model.content.Bulletin;
import com.gewara.model.content.Picture;
import com.gewara.model.movie.Cinema;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.admin.BaseAdminController;
@Controller
public class AdminPictureController extends BaseAdminController {
	
	@RequestMapping("/admin/common/ajax/removePictureById.xhtml")
	public String removePictureById(Long pictureId, ModelMap model) {
		Picture picture = daoService.getObject(Picture.class, pictureId);
		if (picture == null)	return showJsonSuccess(model, "数据不存在！");
		daoService.removeObject(picture);
		monitorService.saveDelLog(getLogonUser().getId(), pictureId, picture);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/setFirstPicture.xhtml")
	public String setFirstPicture(String picPath, String tag, Long relatedid, ModelMap model){
		BaseObject object = (BaseObject)relateService.getRelatedObject(tag, relatedid);
		if(object == null) return showJsonError_DATAERROR(model); 
		ChangeEntry changeEntry = new ChangeEntry(object);
		BeanUtil.set(object, "firstpic", picPath);
		daoService.saveObject(object);
		monitorService.saveChangeLog(getLogonUser().getId(), BaseObject.class, relatedid, changeEntry.getChangeMap(object));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/common/ajax/updatePictureDesc.xhtml")
	public String updatePictureDesc(Long pictureId, String desc, ModelMap model) {
		Picture picture = daoService.getObject(Picture.class, pictureId);
		if (picture == null) return showJsonSuccess(model, "数据不存在！");
		ChangeEntry changeEntry = new ChangeEntry(picture);
		if(!StringUtils.equals(picture.getTag(), "characterroom")){
			picture.setName(desc);
		}
		picture.setDescription(desc);
		daoService.saveObject(picture);
		monitorService.saveChangeLog(getLogonUser().getId(), Picture.class, pictureId,changeEntry.getChangeMap(picture));
		return showJsonSuccess(model);
	}
	
	// ~~~~~~~~~~~~~~~~~公告~~~~~~~~~~~~~~~~~~
	@RequestMapping("/admin/common/ajax/getBulletinById.xhtml")
	public String getBulletinById(Long bulletinId, ModelMap model) {
		Bulletin bulletin = daoService.getObject(Bulletin.class, bulletinId);
		if (bulletin == null) return showJsonError_NOT_FOUND(model);
		Map result = BeanUtil.getBeanMap(bulletin);
		return showJsonSuccess(model, result);
	}
	
	@RequestMapping("/admin/common/ajax/saveOrUpdateBulletin.xhtml")
	public String saveOrUpdateBulletin(Long id, HttpServletRequest request, ModelMap model) {
		Bulletin bulletin = new Bulletin("");
		if (id!=null) {
			bulletin = daoService.getObject(Bulletin.class, id);
			bulletin.setPosttime(new Date());
		}
		ChangeEntry changeEntry = new ChangeEntry(bulletin);
		String type = bulletin.getBulletintype();
		BindUtils.bindData(bulletin, request.getParameterMap());
		if(StringUtil.getByteLength(bulletin.getContent())>20000) return showJsonError(model, "内容字符过长！");
		//验证内容
		String msg=ValidateUtil.validateNewsContent(null, bulletin.getContent());
		if(StringUtils.isNotBlank(msg)) return showJsonError(model, msg);
		String citycode = "310000";
		if(bulletin.getRelatedid()!=null && StringUtils.isNotBlank(bulletin.getTag())) {
			BaseObject object = (BaseObject)relateService.getRelatedObject(bulletin.getTag(), bulletin.getRelatedid());
			if(object!=null) {
				String countycode = (String)BeanUtil.get(object, "countycode");
				if(StringUtils.isNotBlank(countycode)) bulletin.setCountycode(countycode);
				if(object instanceof Cinema) {
					Cinema cinema = (Cinema)object;
					citycode = cinema.getCitycode();
				}
			}
		}
		bulletin.setCitycode(citycode);
		daoService.saveObject(bulletin);
		monitorService.saveChangeLog(getLogonUser().getId(), Bulletin.class, bulletin.getId(),changeEntry.getChangeMap( bulletin));
		if (Bulletin.BULLETION_COUPON.equals(bulletin.getBulletintype()) || Bulletin.BULLETION_COUPON.equals(type))
			commonService.updateCoupon(null, bulletin.getTag(), bulletin.getRelatedid());
		Map result = BeanUtil.getBeanMap(bulletin);
		return showJsonSuccess(model, result);
	}
	
	@RequestMapping("/admin/common/ajax/removeBulletinById.xhtml")
	public String removeBulletinById(Long bulletinId, ModelMap model) {
		Bulletin bulletin = daoService.getObject(Bulletin.class, bulletinId);
		if (bulletin == null) return showJsonError(model, "数据不存在！");
		ChangeEntry changeEntry = new ChangeEntry(bulletin);
		daoService.removeObject(bulletin);
		monitorService.saveChangeLog(getLogonUser().getId(), Bulletin.class, bulletin.getId(),changeEntry.getChangeMap( bulletin));
		commonService.updateCoupon(null, bulletin.getTag(), bulletin.getRelatedid());
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/common/ajax/updateBulletinHotValue.xhtml")
	public String updateBulletinHotValue(Long id, Integer value, ModelMap model) {
		commonService.updateBulletinHotValue(id, value);
		return showJsonSuccess(model);
	}
}
