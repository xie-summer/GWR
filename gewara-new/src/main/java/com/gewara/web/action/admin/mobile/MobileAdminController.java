package com.gewara.web.action.admin.mobile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.Config;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.MongoData;
import com.gewara.helper.sys.AdminCityHelper;
import com.gewara.json.MobileApp;
import com.gewara.json.MobileLoadImage;
import com.gewara.json.MobileQrCode;
import com.gewara.json.MobileUpGrade;
import com.gewara.json.PhoneActivity;
import com.gewara.json.mobile.MobileGrabTicketEvent;
import com.gewara.json.mobile.MobileGrabTicketMpi;
import com.gewara.json.mobile.MobileUpGradeCount;
import com.gewara.json.mobile.SpShare;
import com.gewara.json.mobile.WeixinActivity;
import com.gewara.model.acl.User;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.PhoneAdvertisement;
import com.gewara.model.mobile.ApiConfig;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.mongo.MongoService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.GewaPicService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.PictureUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * 手机wap与手机客户端后台管理功能
 * @author shusong.liu@gewara.com
 */
//TODO:所有datacount相关的报表数据移入报表系统 
@Controller
public class MobileAdminController extends BaseAdminController {
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	@Autowired@Qualifier("config")
	private Config config;
	//添加或首页广告活动信息
	@RequestMapping("/admin/mobile/saveOrUpdateActivity.xhtml")
	public String saveOrUpdateActivity(String pid,String title,String logo,Timestamp starttime,Timestamp endtime,
			String address,String type,String content,String opiinfo,String apptype,String ostype,
			String citycode,String status,Integer rank,ModelMap model,String contentLogo){
		PhoneActivity pa = mongoService.getObject(PhoneActivity.class, MongoData.DEFAULT_ID_NAME, pid);
		if(pa == null){
			pa = new PhoneActivity();
			String id = System.currentTimeMillis()+"";
			pa.setAddtime(new Date());
			pa.setId(id);
		}
		pa.setTitle(title);
		pa.setAddress(address);
		pa.setLogo(logo);
		pa.setStarttime(starttime);
		pa.setEndtime(endtime);
		pa.setType(type);
		pa.setContent(content);
		pa.setCitycode(citycode);
		pa.setOpiinfo(opiinfo);
		pa.setApptype(apptype);
		pa.setOstype(ostype);
		pa.setStatus(status);
		pa.setContentLogo(contentLogo);
		if(null==rank){
			rank=0;
		}
		pa.setRank(rank);
		mongoService.saveOrUpdateObject(pa, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/mobile/phoneActivitylist.xhtml")
	public String searchPhoneActivityList(ModelMap model){
		DBObject params = new BasicDBObject();
		params.put("status", new BasicDBObject("$ne",PhoneActivity.STATUS_DELETE));
		List<PhoneActivity> phoneActivityList = mongoService.getObjectList(PhoneActivity.class, params,"rank",true,0,1000);
		model.put("phoneActivityList", phoneActivityList);
		model.put("citynameMap", AdminCityContant.getCitycode2CitynameMap());
		List<AdminCityHelper> province2CityList = AdminCityHelper.province2CityListMap();
		model.put("province2CityList", province2CityList);
		return "admin/datacount/wap/phoneActivitylist.vm";
	}
	@RequestMapping("/admin/mobile/weixinActivitylist.xhtml")
	public String weixinActivitylist(ModelMap model){
		DBObject params = new BasicDBObject();
		Map<String, List<PhoneActivity>> activityMap = new HashMap<String, List<PhoneActivity>>();
		List<WeixinActivity> weixinActivityList = mongoService.getObjectList(WeixinActivity.class, params, "rank", true, 0, 500);
		for(WeixinActivity activity : weixinActivityList){
			String[] aids = activity.getActivityid().split(",");
			List<PhoneActivity> paList = new ArrayList<PhoneActivity>();
			for(String activityid : aids){
				PhoneActivity pa = mongoService.getObject(PhoneActivity.class, "id", activityid);
				if(pa!=null){ 
					paList.add(pa);
				}
			}
			activityMap.put(activity.getId(), paList);
		}
		Map map = mongoService.findOne(MongoData.NS_WEIXIN, "id", WeixinActivity.TEMPLATE_ID);
		if(map!=null)model.put("content", map.get("content"));
		model.put("activityMap", activityMap);
		model.put("weixinActivityList", weixinActivityList);
		return "admin/datacount/wap/weixinActivitylist.vm";
	}
	@RequestMapping("/admin/mobile/setWeixinText.xhtml")
	public String setText(String id, String type, Integer num, ModelMap model){
		WeixinActivity activity = mongoService.getObject(WeixinActivity.class, "id", id);
		if(StringUtils.equals(type, "reply"))activity.setReplynum(num+"");
		else if(StringUtils.equals(type, "rank"))activity.setRank(num);
		mongoService.saveOrUpdateObject(activity, "id");
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/mobile/delWeixin.xhtml")
	public String setText(String id, ModelMap model){
		mongoService.removeObjectById(WeixinActivity.class, "id", id);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/mobile/saveWeixin.xhtml")
	public String saveWeixin(String replynum, Integer rank, String activityid, ModelMap model){
		if(StringUtils.isBlank(replynum) || rank==null || StringUtils.isBlank(activityid)){
			return showJsonError(model, "设置参数不能为空！");
		}
		WeixinActivity activity = new WeixinActivity();
		activity.setId(System.currentTimeMillis()+"");
		activity.setActivityid(activityid);
		activity.setRank(rank);
		activity.setReplynum(replynum);
		mongoService.saveOrUpdateObject(activity, "id");
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/mobile/weixinTemplate.xhtml")
	public String weixinTemplate(String content, ModelMap model){
		Map map = new HashMap();
		map.put("id", WeixinActivity.TEMPLATE_ID);
		map.put("content", content);
		mongoService.saveOrUpdateMap(map, "id", MongoData.NS_WEIXIN);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/mobile/phoneActivity.xhtml")
	public String searchPhoneActivity(String id,ModelMap model){
		PhoneActivity phoneActivity = mongoService.getObject(PhoneActivity.class, MongoData.DEFAULT_ID_NAME, id);
		Map result = BeanUtil.getBeanMap(phoneActivity);
		result.put("starttime", DateUtil.format(phoneActivity.getStarttime(), "yyyy-MM-dd HH:mm:ss"));
		result.put("endtime", DateUtil.format(phoneActivity.getEndtime(), "yyyy-MM-dd HH:mm:ss"));
		return showJsonSuccess(model, result);
	}
	@RequestMapping("/admin/mobile/deletePhoneActivity.xhtml")
	public String deletePhoneActivity(ModelMap model,String id){
		if(id==null) this.showError(model, "ID不可以为空");
		PhoneActivity pa = mongoService.getObject(PhoneActivity.class, MongoData.DEFAULT_ID_NAME, id);
		pa.setStatus(PhoneActivity.STATUS_DELETE);
		mongoService.saveOrUpdateObject(pa, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	//查询广告列表
	@RequestMapping("/admin/mobile/searchAdvertisementList.xhtml")
	public String searchAdvertisementList(ModelMap model){
		List<AdminCityHelper> province2CityList = AdminCityHelper.province2CityListMap();
		model.put("province2CityList", province2CityList);
		Map<String,String> citynameMap=AdminCityContant.getCitycode2CitynameMap();
		List<PhoneAdvertisement> phoneAdvertisementlist=commonService.getNewPhoneAdvertisementList(PhoneAdvertisement.STATUS_NEW);
		model.put("citynameMap", citynameMap);
		model.put("phoneAdvertisementlist", phoneAdvertisementlist);
		return "admin/datacount/wap/advertisementlist.vm";
	}
	
	//添加或修改广告信息
	@RequestMapping("/admin/mobile/addOrUpdateAdvertisement.xhtml")
	public String addOrUpdateAdvertisement(ModelMap model,HttpServletRequest request,Long adid){
		PhoneAdvertisement phoneadvertisement=null;
		if(adid==null){
			phoneadvertisement=new PhoneAdvertisement();
			phoneadvertisement.setAddtime(DateUtil.getCurFullTimestamp());
			phoneadvertisement.setStatus(PhoneAdvertisement.STATUS_NEW);
		}else{
			phoneadvertisement=daoService.getObject(PhoneAdvertisement.class, adid);
		}
		BindUtils.bindData(phoneadvertisement, request.getParameterMap());
		if(null==phoneadvertisement.getRank()){
			phoneadvertisement.setRank(0);
		}
		daoService.saveObject(phoneadvertisement);
		return showJsonSuccess(model);
	}
	
	//删除广告
	@RequestMapping("/admin/mobile/deleteAdvertisementbyid.xhtml")
	public String deleteAdvertisementById(ModelMap model,Long adid){
		if(adid==null) this.showError(model, "ID不可以为空");
		PhoneAdvertisement phoneAdvertisement=daoService.getObject(PhoneAdvertisement.class, adid);
		phoneAdvertisement.setStatus(PhoneAdvertisement.STATUS_DELETE);
		daoService.updateObject(phoneAdvertisement);
		return showJsonSuccess(model);
	}
	//获取要修改的广告信息
	@RequestMapping("/admin/mobile/toUpdateAdvertisementPage.xhtml")
	public String toUpdateAdvertisementPage(ModelMap model, Long id){
		if(id==null)return this.showError(model, "id为空错误");
		PhoneAdvertisement phoneAdvertisement=daoService.getObject(PhoneAdvertisement.class, id);
		Map result = BeanUtil.getBeanMap(phoneAdvertisement);
		return showJsonSuccess(model, result);
	}
	
	
	/**
	 * 手机wap版首页推荐
	 */
	@RequestMapping("/admin/mobile/wapIndex.xhtml")
	public String wapIndex(ModelMap model){
		List<GewaCommend> gcList = commonService.getGewaCommendListByid(null, null, SignName.WAP_INDEX, false);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		commonService.initGewaCommendList("gcList", rh, gcList);
		model.put("gcList", gcList);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		List<AdminCityHelper> province2CityList = AdminCityHelper.province2CityListMap();
		model.put("province2CityList", province2CityList);
		return "admin/recommend/phone/wapIndex.vm";
	}
	
	/**
	 * 手机wap版首页推荐添加
	 */
	@RequestMapping("/admin/mobile/addWapIndex.xhtml")
	public String addWapIndex(Long id,String title,String link,String citycode,ModelMap model){
		GewaCommend gc = null;
		if(id == null){
			gc = new GewaCommend(SignName.WAP_INDEX);
		}else{
			gc = daoService.getObject(GewaCommend.class, id);
		}
		gc.setTitle(title);
		gc.setLink(link);
		gc.setCitycode(citycode);
		daoService.saveObject(gc);
		return showJsonSuccess(model);
	}
	
	/**
	 * 手机wap版首页推荐删除
	 */
	@RequestMapping("/admin/mobile/delWapIndex.xhtml")
	public String delWapIndex(Long id,ModelMap model){
		GewaCommend gc = daoService.getObject(GewaCommend.class, id);
		daoService.removeObject(gc);
		monitorService.saveDelLog(getLogonUser().getId(), id, gc);
		return showJsonSuccess(model);
	}
	
	
	/**
	 * 手机版活动推荐列表
	 */
	@RequestMapping("/admin/mobile/wapActivityList.xhtml")
	public String wapActivityList(ModelMap model){
		List<GewaCommend> gcList = commonService.getGewaCommendListByid(null, null, SignName.WAP_ACTIVITY, false);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		commonService.initGewaCommendList("gcList", rh, gcList);
		model.put("gcList", gcList);
		return "admin/mobile/wapActivityList.vm";
	}
	
	/**
	 * 添加，修改手机活动跳转
	 */
	@RequestMapping("/admin/mobile/addWapActivity.xhtml")
	public String addWapActivity(Long gid,ModelMap model){
		GewaCommend gc = null;
		if(gid != null){
			gc = daoService.getObject(GewaCommend.class,gid);
		}
		model.put("gc", gc);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		List<AdminCityHelper> province2CityList = AdminCityHelper.province2CityListMap();
		model.put("province2CityList", province2CityList);
		return "admin/mobile/wapModifyActivity.vm";
	}
	
	/**
	 * 保存手机活动
	 */
	@RequestMapping("/admin/mobile/saveWapActivity.xhtml")
	public String saveWapActivity(ModelMap model,Long gid,String link,String title,String content,String logo,
			Timestamp startTime,Timestamp endTime,Timestamp stopTime,String tag,String citycode){
		GewaCommend gc = null;
		if(gid != null){
			gc = daoService.getObject(GewaCommend.class, gid);
		}else{
			gc = new GewaCommend(SignName.WAP_ACTIVITY);
		}
		gc.setTitle(title);
		gc.setSummary(content);
		gc.setStarttime(startTime);
		gc.setEndtime(endTime);
		gc.setStoptime(stopTime);
		gc.setLink(link);
		gc.setTag(tag);
		gc.setLogo(logo);
		gc.setCitycode(citycode);
		String msg=ValidateUtil.validateNewsContent(null, gc.getSummary());
		if(StringUtils.isNotBlank(msg)) return showJsonError(model, msg);
		daoService.saveObject(gc);
		return showJsonSuccess(model);
	}
	/**
	 * 电影环节列表
	 */
	@RequestMapping("/admin/mobile/movieLinkList.xhtml")
	public String movieLinkList(ModelMap model){
		List<GewaCommend> gcList = commonService.getGewaCommendListByParentid(SignName.FILM_MOVIE_LINK, 0l,true);
		Map<Long,List<GewaCommend>> childrenGcMap = new HashMap<Long, List<GewaCommend>>();
		if(!gcList.isEmpty())
		for (GewaCommend gc : gcList) {
			List<GewaCommend> childrenMovieLink = commonService.getGewaCommendListByParentid(SignName.FILM_MOVIE_LINK, gc.getId(),true);
			childrenGcMap.put(gc.getId(),childrenMovieLink);
		}
		model.put("gcList", gcList);
		model.put("childrenGcMap", childrenGcMap);
		return "admin/recommend/phone/movieLinkList.vm";
	}
	
	/**
	 * 添加或修改电影环节
	 */
	@RequestMapping("/admin/mobile/saveOrUpdateMovieLink.xhtml")
	public String addMovieLink(Long id,String title,Long parentid,Integer ordernum,String tag,ModelMap model){
		GewaCommend gc = null;
		if(id != null) gc = daoService.getObject(GewaCommend.class, id);
		else gc = new GewaCommend(SignName.FILM_MOVIE_LINK);
		gc.setTitle(title);
		gc.setParentid(parentid);
		gc.setOrdernum(ordernum);
		gc.setTag(tag);
		daoService.saveObject(gc);
		return showJsonSuccess(model);
	}
	
	/**
	 * 删除电影环节信息
	 */
	@RequestMapping("/admin/mobile/deleteMovieLink.xhtml")
	public String deleteMovieLink(Long id,ModelMap model){
		GewaCommend ml = daoService.getObject(GewaCommend.class, id);
		daoService.removeObject(ml);
		monitorService.saveDelLog(getLogonUser().getId(), id, ml);
		return showJsonSuccess(model);
	}
	
	/**
	 * 手机客户端首页广告推荐
	 */
	@RequestMapping("/admin/mobile/phoneIndexAdvertList.xhtml")
	public String phoneIndexAdvertList(ModelMap model,Integer pageNo){
		if(pageNo == null) pageNo = 0;
		Integer maxnum = 20;
		Integer from = pageNo *maxnum;
		List<GewaCommend> gcList = commonService.getGewaCommendList(null, SignName.PHONE_INDEX_ADVERT, null,null,false,from, maxnum);
		Integer count = commonService.getGewaCommendCount(null, SignName.PHONE_INDEX_ADVERT,null,null, false);
		PageUtil pageUtil = new PageUtil(count,maxnum,pageNo,"admin/mobile/phoneIndexAdvertList.xhtml");
		pageUtil.initPageInfo();
		model.put("gcList", gcList);
		model.put("pageUtil", pageUtil);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		List<AdminCityHelper> province2CityList = AdminCityHelper.province2CityListMap();
		model.put("province2CityList", province2CityList);
		return "admin/recommend/phone/phoneIndexAdvertList.vm";
	}
	
	/**
	 * 保存广告信息
	 */
	@RequestMapping("/admin/mobile/savePhoneAdvert.xhtml")
	public String savePhoneAdvert(Long id,String title,String logo,Long relatedid,String link,String tag,String citycode,ModelMap model){
		GewaCommend gc = null;
		if(id != null) gc = daoService.getObject(GewaCommend.class, id);
		else gc = new GewaCommend(SignName.PHONE_INDEX_ADVERT);
		gc.setTitle(title);
		gc.setLogo(logo);
		gc.setTag(tag);
		gc.setRelatedid(relatedid);
		gc.setLink(link);
		gc.setCitycode(citycode);
		daoService.saveObject(gc);
		return showJsonSuccess(model);
	}
	
	/**
	 * 手机客户端升级信息列表 
	 */
	@RequestMapping("/admin/mobile/upGradeList.xhtml")
	public String upGradeList(ModelMap model){
		//TODO:move to nosqlService
		List<MobileUpGrade> upGradeList = mongoService.getObjectList(MobileUpGrade.class, "addTime", false, 0, 100);
		model.put("upGradeList", upGradeList);
		model.put("appSourcesMap", getAppSourceMap());
		GewaConfig gc = daoService.getObject(GewaConfig.class, config.getLong("imgServer"));
		Map<String, String> gcMap = JsonUtils.readJsonToMap(gc.getContent());
		model.put("uploadPath", gcMap.keySet().iterator().next());
		return "admin/recommend/phone/upGradeList.vm";
	}
	
	/**
	 * 手机客户端升级信息列表 
	 */
	@RequestMapping("/admin/mobile/upGradeCountList.xhtml")
	public String upGradeCountList(ModelMap model){
		List<MobileUpGradeCount> upGrades = mongoService.getObjectList(MobileUpGradeCount.class, "addTime", false, 0, 200);
		model.put("upGrades", upGrades);
		model.put("appSourcesMap", getAppSourceMap());
		return "admin/recommend/phone/upGradeCountList.vm";
	}
	
	/**
	 * 手机客户端升级信息删除测试遗留的垃圾数据 
	 */
	@RequestMapping("/admin/mobile/deleteUpGradeCount.xhtml")
	public String deleteUpGradeCount(String id,ModelMap model){
		MobileUpGradeCount mug = mongoService.getObject(MobileUpGradeCount.class,MongoData.DEFAULT_ID_NAME , id);
		mongoService.removeObject(mug, MongoData.DEFAULT_ID_NAME);
		return this.showJsonSuccess(model, "删除成功！");
	}
	/**
	 * 手机客户端升级信息删除测试遗留的垃圾数据 
	 */
	@RequestMapping("/admin/mobile/getUpGrade.xhtml")
	public String getUpGrade(String uid, ModelMap model){
		//TODO:move to nosqlService
		MobileUpGrade mug = mongoService.getObject(MobileUpGrade.class, MongoData.DEFAULT_ID_NAME, uid);
		return showJsonSuccess(model, BeanUtil.getBeanMap(mug));
	}
	/**
	 * 保存手机客户端升级信息
	 */
	@RequestMapping("/admin/mobile/saveUpGrade.xhtml")
	public String saveUpGrade(String uid,String tag,Integer code,String name,String upgradeUrl,Integer status,String apptype,String foceversion,String specificversion,String remark,String appsource,
			String downloadName,
			ModelMap model){
		//TODO:move to nosqlService
		MobileUpGrade mug = mongoService.getObject(MobileUpGrade.class, MongoData.DEFAULT_ID_NAME, uid);
		if(mug == null){
			mug = new MobileUpGrade();
			String id = System.currentTimeMillis()+StringUtil.getRandomString(5);
			mug.setAddTime(new Timestamp(System.currentTimeMillis()));
			mug.setId(id);
		}
		mug.setModifytime(System.currentTimeMillis());
		mug.setTag(tag);
		mug.setUpgradeStatus(status);
		mug.setUpgradeUrl(upgradeUrl);
		mug.setVersionCode(code);
		mug.setVersionName(name);
		mug.setApptype(apptype);
		mug.setAppsource(appsource);
		mug.setFoceversion(foceversion);
		mug.setSpecificversion(specificversion);
		mug.setRemark(remark);
		mug.setDownloadName(downloadName);
		mongoService.saveOrUpdateObject(mug, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	/**
	 * 删除手机客户端升级信息
	 */
	@RequestMapping("/admin/mobile/delUpGrade.xhtml")
	public String delUpGrade(ModelMap model,String uid){
		MobileUpGrade mug = mongoService.getObject(MobileUpGrade.class, MongoData.DEFAULT_ID_NAME, uid);
		mongoService.removeObject(mug, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	
	/**
	 * 手机客户端海报列表
	 */
	@RequestMapping("/admin/mobile/mobileImageLoad.xhtml")
	public String mobileImageLoadList(ModelMap model){
		List<MobileLoadImage> mobileImageLoadList = mongoService.getObjectList(MobileLoadImage.class, "addTime", false, 0, 30);
		model.put("imageLoadList", mobileImageLoadList);
		return "admin/recommend/phone/imageLoadList.vm";
	}
	
	/**
	 * 保存手机客户端升级信息
	 */
	@RequestMapping("/admin/mobile/saveImageLoad.xhtml")
	public String saveImageLoad(ModelMap model,String imageSrc,String id,Integer status,String apptype, Date starttime, Date endtime){
		if(starttime==null || endtime==null){
			return showJsonError(model, "开始日期和结束日期不能为空！");
		}
		MobileLoadImage mli = mongoService.getObject(MobileLoadImage.class, MongoData.DEFAULT_ID_NAME, id);
		if(mli == null){
			mli = new MobileLoadImage(imageSrc,status,apptype);
			mli.setId(System.currentTimeMillis()+StringUtil.getRandomString(5));
		}else{
			mli.setImagesrc(imageSrc);
			mli.setStatus(status);
			mli.setApptype(apptype);
			mli.setAddTime(new Timestamp(System.currentTimeMillis()));
		}
		mli.setStarttime(starttime);
		mli.setEndtime(endtime);
		mongoService.saveOrUpdateObject(mli, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	/**
	 * 删除手机客户端升级信息
	 */
	@RequestMapping("/admin/mobile/delImageLoad.xhtml")
	public String delImageLoad(ModelMap model,String id){
		MobileLoadImage mli = mongoService.getObject(MobileLoadImage.class, MongoData.DEFAULT_ID_NAME, id);
		mongoService.removeObject(mli, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	
	
	/**
	 * 手机二维码列表
	 */
	@RequestMapping("/admin/mobile/qrCodeList.xhtml")
	public String qrCodeList(ModelMap model){
		List<MobileQrCode> mobileQrCodeList = mongoService.getObjectList(MobileQrCode.class, "addTime", false, 0, 30);
		model.put("qrCodeList", mobileQrCodeList);
		return "admin/recommend/phone/qrCodeList.vm";
	}
	
	/**
	 * 保存二维码信息
	 */
	@RequestMapping("/admin/mobile/saveQrCode.xhtml")
	public String saveQrCode(ModelMap model,String id,String title,String url,String flag,String waterPath,Integer width,Integer height){
		if(StringUtils.isBlank(url)) return showJsonError(model, "请输入链接地址！");
		MobileQrCode mobileQrCode = mongoService.getObject(MobileQrCode.class, MongoData.DEFAULT_ID_NAME, id);
		if(mobileQrCode == null){
			mobileQrCode = new MobileQrCode();
			mobileQrCode.setId(System.currentTimeMillis()+StringUtil.getRandomString(5));
		}
		mobileQrCode.setWaterPath(waterPath);
		mobileQrCode.setFlag(flag);
		mobileQrCode.setHeight(height);
		mobileQrCode.setWidth(width);
		mobileQrCode.setTitle(title);
		mobileQrCode.setUrl(url);
		String str ="URL:"+url;
		String path = "images/qrcode/";
		Hashtable hints= new Hashtable(); 
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		BitMatrix byteMatrix;
		String filepath="";
		try {
			byteMatrix= new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, width, height,hints);
			ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
			BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(byteMatrix);
			ImageIO.write(bufferedImage, "png", pngOutputStream);
			String filename = gewaPicService.saveToTempPic(new ByteArrayInputStream(pngOutputStream.toByteArray()), "png");
			String qrCodePath = gewaPicService.getTempFilePath(filename);
			if(StringUtils.isNotBlank(waterPath)){
				String realWaterPath = applicationContext.getServletContext().getRealPath(waterPath);
				PictureUtil.addWarterMarkUsingJava(qrCodePath, realWaterPath, width/2, height/2, 0.5F);
			}
			dbLogger.warn("path:"+path+"  filename:"+filename);
			gewaPicService.saveTempFileToRemote(filename);
			filepath = gewaPicService.moveRemoteTempTo(getLogonUser().getId(), "qrcode",null, path, filename);
			mobileQrCode.setQrCodePath(filepath);
		} catch (Exception e) {
			dbLogger.error("", e);
			dbLogger.warn("二维码生成错误："+e.getMessage());
			return showJsonError(model, "二维码生成有误，错误信息："+e.getMessage());
		}
		mongoService.saveOrUpdateObject(mobileQrCode, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model,filepath);
	}
	/**
	 * 删除二维码信息
	 */
	@RequestMapping("/admin/mobile/delQrCode.xhtml")
	public String delQrCode(ModelMap model,String id){
		MobileQrCode mqc = mongoService.getObject(MobileQrCode.class, MongoData.DEFAULT_ID_NAME, id);
		mongoService.removeObject(mqc, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	
	
	/**
	 * 手机触屏版->首页->活动广告推荐列表
	 */
	@RequestMapping("/admin/mobile/touchActivityAdvert.xhtml")
	public String touchActivityAdvert(ModelMap model){
		List<GewaCommend> gcList = commonService.getGewaCommendListByid(null, null, SignName.TOUCH_INDEX_ADVERT, false);
		model.put("touchAdvertList",gcList);
		List<AdminCityHelper> province2CityList = AdminCityHelper.province2CityListMap();
		model.put("province2CityList", province2CityList);
		return "admin/recommend/phone/touchActivityAdvert.vm";
	}
	
	/**
	 * 手机触屏版->首页->活动广告推荐添加
	 */
	@RequestMapping("/admin/mobile/saveTouchAdvert.xhtml")
	public String touchSaveTouchAdvert(Long id,String title,String link,String logo,String citycode,ModelMap model){
		GewaCommend gc = null;
		if(id != null){
			gc = daoService.getObject(GewaCommend.class, id);
		}else{
			gc = new GewaCommend();
			gc.setAddtime(new Timestamp(System.currentTimeMillis()));
			gc.setOrdernum(0);
		}
		gc.setSignname(SignName.TOUCH_INDEX_ADVERT);
		gc.setTitle(title);
		gc.setLink(link);
		gc.setLogo(logo);
		gc.setCitycode(citycode);
		daoService.saveObject(gc);
		return showJsonSuccess(model);
	}

	/**
	 * 手机触屏版->首页->活动广告推荐删除
	 */
	@RequestMapping("/admin/mobile/delTouchAdvert.xhtml")
	public String touchDelAdvert(Long id,ModelMap model){
		GewaCommend gc = daoService.getObject(GewaCommend.class, id);
		daoService.removeObject(gc);
		monitorService.saveDelLog(getLogonUser().getId(), id, gc);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/mobile/appList.xhtml")
	public String appList(ModelMap model){
		List<MobileApp> mobileAppList = mongoService.getObjectList(MobileApp.class, "sortFlag", true);
		model.put("mobileAppList", mobileAppList);
		model.put("appSourcesMap", getAppSourceMap());
		return "admin/recommend/phone/appList.vm";
	}
	
	@RequestMapping("/admin/mobile/addApp.xhtml")
	public String addApp(String uid, String otherAppName,int sortFlag,String ostype,String appurl, String name, String logo, String appsize, String appversion, String status,String appdesc,String coverapps, ModelMap model){
		if(StringUtils.isBlank(name))return showJsonError(model,"应用名称不能为空!");
		if(StringUtils.isBlank(logo))return showJsonError(model,"logo不能为空!");
		if(StringUtils.isBlank(appurl))return showJsonError(model,"下载路径不能为空!");
		MobileApp mobileapp = mongoService.getObject(MobileApp.class, "id", uid);
		if(mobileapp == null){
			mobileapp = new MobileApp();
			mobileapp.setId(ServiceHelper.assignID("mobile")+StringUtil.getRandomString(4));
			mobileapp.setAddtime(DateUtil.getCurFullTimestamp());
		}
		String[] coverapp=null;
		if(StringUtils.isNotBlank(coverapps)){
			coverapp=coverapps.split(";");
		}
		mobileapp.setCoverapp(Arrays.asList(coverapp));
		mobileapp.setOstype(ostype);
		mobileapp.setAppurl(appurl);
		mobileapp.setName(name);
		mobileapp.setLogo(logo);
		mobileapp.setAppsize(appsize);
		mobileapp.setAppversion(appversion);
		mobileapp.setStatus(status);
		mobileapp.setOtherAppName(otherAppName);
		mobileapp.setSortFlag(sortFlag);
		mobileapp.setAppdesc(appdesc);
		mongoService.saveOrUpdateObject(mobileapp, "id");
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/mobile/delApp.xhtml")
	public String delApp(String uid, ModelMap model){
		if(StringUtils.isBlank(uid)) return showJsonError(model,"数据错误！");
		mongoService.removeObjectById(MobileApp.class, "id", uid);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/mobile/searchWapInfo.xhtml")
	public String searchInfo(ModelMap model){
		List<GewaCommend> gcList = commonService.getGewaCommendListByid(null, null, SignName.WAP_INFO, false); 
		model.put("gcList", gcList);
		return "admin/recommend/phone/wapInfo.vm";
	}
	@RequestMapping("/admin/mobile/addWapInfo.xhtml")
	public String addWapInfo(Long id, String title, String link, ModelMap model){
		GewaCommend gc = null;
		if(id == null){
			gc = new GewaCommend(SignName.WAP_INFO);
		}else{
			gc = daoService.getObject(GewaCommend.class, id);
		}
		gc.setTitle(title);
		gc.setLink(link);
		daoService.saveObject(gc);
		return showJsonSuccess(model);
	}

	/**
	 * 抢票活动列表
	 * @return
	 */
	@RequestMapping(value = "/admin/mobile/grabTicketEventList.xhtml")
	public String grabTicketEventList(@RequestParam(defaultValue = "0", required = false, value = "pageNo") Integer pageNo,
			ModelMap model){
		int pageSize = 20;
		Map<String, Integer> mpiMap=new HashMap<String, Integer>();
		List<MobileGrabTicketEvent> eventList=mongoService.getObjectList(MobileGrabTicketEvent.class,"starttime",true,pageNo,pageSize);
		for (MobileGrabTicketEvent event : eventList) {
			DBObject params=new BasicDBObject();
			params.put("gtid", event.getId());
			mpiMap.put(event.getId(), mongoService.getObjectCount(MobileGrabTicketMpi.class, params));
		}
		model.put("eventList", eventList);
		model.put("mpiMap", mpiMap);
		int totalCount = mongoService.getObjectCount(MobileGrabTicketEvent.class);
		PageUtil pageUtil = new PageUtil(totalCount, pageSize, pageNo,"admin/mobile/grabTicketEventList.xhtml");
		pageUtil.initPageInfo();
		model.put("pageUtil", pageUtil);
		return "admin/mobile/grabTicketEventList.vm";
	}
	
	/**
	 * 转到抢票活动创建页
	 * @return
	 */
	@RequestMapping(value = "/admin/mobile/modifyGrabTicketEvent.xhtml",method=RequestMethod.GET)
	public String modifyGrabTicketEvent(String id,ModelMap model){
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		if(StringUtils.isNotBlank(id)){
			MobileGrabTicketEvent event=mongoService.getObject(MobileGrabTicketEvent.class, MongoData.DEFAULT_ID_NAME, id);
			model.put("event", event);
		}
		return "admin/mobile/grabTicketEventForm.vm";
	}
	
	/**
	 * 保存抢票活动信息
	 * @return
	 */
	@RequestMapping(value = "/admin/mobile/saveGrabTicketEvent.xhtml",method=RequestMethod.POST)
	public String saveGrabTicketEvent(
			MobileGrabTicketEvent event,
			ModelMap model){
		//输入校验
		if(StringUtils.isBlank(event.getTitle())){
			return showJsonSuccess(model, "请输入活动名称");
		}
		if(null==event.getStarttime()){
			return showJsonSuccess(model,"请输入抢票时间");
		}
		if(null==event.getPrice()){
			return showJsonSuccess(model,"请输入秒杀价");
		}
		
		//状态默认设置
		if(StringUtils.isBlank(event.getStatus())){
			event.setStatus(MobileGrabTicketEvent.GRAB_STATUS_C);
		}
		
		if(StringUtils.isBlank(event.getId())){//新增,设置属性
			event.setId(MongoData.buildId());
			event.setAddtime(DateUtil.getCurFullTimestampStr());
		}else{
			MobileGrabTicketEvent dbEvent=mongoService.getObject(MobileGrabTicketEvent.class, MongoData.DEFAULT_ID_NAME, event.getId());
			if(null==dbEvent){
				return showJsonSuccess(model,"抢票活动不存在！");
			}
			event.setAddtime(dbEvent.getAddtime());
		}
		event.setUpdatetime(DateUtil.getCurFullTimestampStr());
		mongoService.saveOrUpdateObject(event, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model,"保存成功！");
	}
	
	/**
	 * 删除抢票活动信息
	 * @return
	 */
	@RequestMapping(value = "/admin/mobile/removeGrabTicketEvent.xhtml",method=RequestMethod.POST)
	public String saveGrabTicketEvent(
			String id,
			ModelMap model){
		//删除抢票活动
		mongoService.removeObjectById(MobileGrabTicketEvent.class, MongoData.DEFAULT_ID_NAME, id);
		//删除抢票场次
		DBObject params=new BasicDBObject();
		params.put("gtid", id);
		List<MobileGrabTicketMpi> list=mongoService.getObjectList(MobileGrabTicketMpi.class, params);
		mongoService.removeObjectList(list, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model,"删除成功！");
	}
	
	
	/**
	 * 转到抢票场次列表页
	 * @param id
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/admin/mobile/grabTicketMpiList.xhtml",method=RequestMethod.GET)
	public String grabTicketMpiList(String id,ModelMap model){
		if(StringUtils.isBlank(id)){
			return showError(model, "请选择抢票活动");
		}
		MobileGrabTicketEvent dbEvent=mongoService.getObject(MobileGrabTicketEvent.class, MongoData.DEFAULT_ID_NAME, id);
		if(null==dbEvent){
			return showError(model,"抢票活动不存在！");
		}
		DBObject params=new BasicDBObject();
		params.put("gtid", id);
		List<OpenPlayItem> openPlayItemList=new ArrayList<OpenPlayItem>();
		List<MobileGrabTicketMpi> list=mongoService.getObjectList(MobileGrabTicketMpi.class, params);
		Map<Long, String> mpiIdMap=new HashMap<Long, String>();
		for (MobileGrabTicketMpi mpi : list) {
			OpenPlayItem openPlayItem=daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getMpid(), true);
			if(null!=openPlayItem){
				openPlayItemList.add(openPlayItem);
				mpiIdMap.put(mpi.getMpid(), mpi.getId());
			}
			
		}
		model.put("openPlayItemList", openPlayItemList);
		model.put("mpiIdMap", mpiIdMap);
		model.put("grabEvent", dbEvent);
		return "admin/mobile/grabTicketMpiList.vm";
	}
	
	
	/**
	 * 保存抢票活动场次信息
	 * @return
	 */
	@RequestMapping(value = "/admin/mobile/saveGrabTicketMpi.xhtml",method=RequestMethod.POST)
	public String saveGrabTicketMpi(
			MobileGrabTicketMpi mpi,
			ModelMap model){
		if(StringUtils.isBlank(mpi.getGtid())){
			return showJsonSuccess(model,"未知的抢票活动！");
		}
		MobileGrabTicketEvent dbEvent=mongoService.getObject(MobileGrabTicketEvent.class, MongoData.DEFAULT_ID_NAME, mpi.getGtid());
		if(null==dbEvent){
			return showError(model,"抢票活动不存在！");
		}
		if(null==mpi.getMpid()){
			return showJsonSuccess(model,"请输入场次信息");
		}
		//校验场次是否存在
		OpenPlayItem openPlayItem=daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getMpid(), false);
		if(null==openPlayItem){
			return showJsonSuccess(model,"输入的场次不存在！");
		}
		//校验是否已经添加
		DBObject params=new BasicDBObject();
		params.put("gtid", mpi.getGtid());
		params.put("mpid", mpi.getMpid());
		int count=mongoService.getObjectCount(MobileGrabTicketMpi.class, params);
		if(count>0){
			return showJsonSuccess(model,"该场次已经添加过了，不允许重复！");
		}
		mpi.setId(MongoData.buildId());
		mongoService.saveOrUpdateObject(mpi, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model,"保存成功！");
	}
	
	/**
	 * 删除抢票活动场次
	 * @param gmpid
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/admin/mobile/removeGrabTicketMpi.xhtml")
	public String removeGrabTicketMpi(String gmpid,ModelMap model){
		mongoService.removeObjectById(MobileGrabTicketMpi.class, MongoData.DEFAULT_ID_NAME, gmpid);
		return showJsonSuccess(model,"删除成功！");
	}
	
	
	@RequestMapping("/admin/mobile/configList.xhtml")
	public String configList(Long cid, ModelMap model) {
		String url = "admin/mobile/config.vm";
		if(cid==null) return url;
		ApiConfig cfg = daoService.getObject(ApiConfig.class, cid);
		model.put("config", cfg);
		return url;
	}
	@RequestMapping("/admin/mobile/setConfigContent.xhtml")
	public String setConfigContent(Long cid, String content, ModelMap model) {
		User user = getLogonUser();
		ApiConfig cfg = daoService.getObject(ApiConfig.class, cid);
		ChangeEntry changeEntry = new ChangeEntry(cfg);
		cfg.setContent(content);
		daoService.saveObject(cfg);
		monitorService.saveChangeLog(user.getId(), ApiConfig.class, cid, changeEntry.getChangeMap(cfg));
		return showJsonSuccess(model);
	}
	

	
	@RequestMapping("/admin/mobile/spShareList.xhtml")
	public String spShareList(ModelMap model) {
		List<SpShare> spList = mongoService.getObjectList(SpShare.class, "addtime", false);
		model.put("spList", spList);
		return "admin/recommend/phone/spShareList.vm";
	}
	@RequestMapping("/admin/mobile/getSpShare.xhtml")
	public String spShareList(String id, ModelMap model) {
		SpShare share = mongoService.getObject(SpShare.class, "id", id);
		return showJsonSuccess(model, BeanUtil.getBeanMap(share));
	}
	/**
	 * 保存分享级信息
	 */
	@RequestMapping("/admin/mobile/saveSpShare.xhtml")
	public String saveSpShare(String id, String starttime, String endtime, Long spid, String content, ModelMap model, HttpServletRequest request){
		if(spid==null || content==null || starttime==null || endtime==null){
			return showJsonError(model, "开始日期和结束日期不能为空！");
		}
		SpecialDiscount discount = daoService.getObject(SpecialDiscount.class, spid);
		if(discount==null){
			return showJsonError(model, "特价活动不存在，请核实活动id");
		}
		SpShare share = mongoService.getObject(SpShare.class, "id", id);
		if(share==null){
			share = new SpShare();
		}
		BindUtils.bindData(share, request.getParameterMap());
		if(StringUtils.isBlank(id)){
			id = System.currentTimeMillis()+StringUtil.getRandomString(5);
			share.setAddtime(DateUtil.getCurTimeStr());
			share.setId(id);
			share.setAddtime(DateUtil.getCurFullTimestampStr());
		}
		mongoService.saveOrUpdateObject(share, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/mobile/reSpShare.xhtml")
	public String reSpShare(ModelMap model){
		List<SpShare> spList = mongoService.getObjectList(SpShare.class);
		int i = 0;
		for(SpShare sp : spList){
			Timestamp time = DateUtil.parseTimestamp(sp.getId());
			if(time!=null){
				i++;
				sp.setAddtime(sp.getId());
				sp.setId(time.getTime()+StringUtil.getRandomString(5));
				mongoService.saveOrUpdateObject(sp, "id");
			}
		}
		return forwardMessage(model, "更新数据：" + i);
	}
	@RequestMapping("/admin/mobile/delSpShare.xhtml")
	public String delSpShare(String id, ModelMap model){
		if(id!=null){
			SpShare share = mongoService.getObject(SpShare.class, "id", id);
			if(share!=null){
				mongoService.removeObject(share, "id");
			}
			return showJsonSuccess(model);
		}
		List<SpShare> spList = mongoService.getObjectList(SpShare.class);
		int i = 0;
		List<SpShare> list = new ArrayList<SpShare>();
		for(SpShare sp : spList){
			Timestamp time = DateUtil.parseTimestamp(sp.getId());
			if(time!=null){
				i++;
				list.add(sp);
			}
		}
		mongoService.removeObjectList(list, "id");
		return forwardMessage(model, "删除数据：" + i);
	}
}
