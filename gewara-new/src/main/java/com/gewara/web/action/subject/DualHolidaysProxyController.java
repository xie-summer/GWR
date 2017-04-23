package com.gewara.web.action.subject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.GoodsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.GoodsFilterHelper;
import com.gewara.model.goods.Goods;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.service.OperationService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.order.GoodsService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.MailService;
import com.gewara.untrans.ShareService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ObjectId;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

// 双节专题代理(中秋、10.1) author:wk
@Controller
public class DualHolidaysProxyController  extends AnnotationController {
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	@Autowired@Qualifier("mailService")
	private MailService mailService;
	//前台部分star
	//得到积分兑换物品信息集合
	@RequestMapping("/subject/proxy/getPointGoodsList")
	public String getPointGoodsList(Integer formnum, Integer maxnum, String para, ModelMap model){
		if(StringUtils.isBlank(para)) return showJsonSuccess(model);
		if(formnum == null || formnum < 0) formnum = 0;
		if(maxnum == null || maxnum < 1) maxnum = 5;
		List<Goods> goodsList = goodsService.getCurGoodsList(Goods.class, GoodsConstant.GOODS_TAG_POINT, null, formnum, maxnum);
		GoodsFilterHelper.goodsFilter(goodsList, PartnerConstant.GEWA_SELF);
		List<Map> result = BeanUtil.getBeanMapList(goodsList, para.split(","));
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(result));
	}
	//得到积分兑换物品被兑换数集合
	@RequestMapping("/subject/proxy/getBuyGoodsCount.xhtml")
	public String getBuyGoodsCount(Long goodsid, ModelMap model){
		return showJsonSuccess(model, goodsOrderService.getGoodsOrderQuantity(goodsid, OrderConstant.STATUS_PAID_SUCCESS)+"");
	}
	//上传图片
	@RequestMapping("/subject/proxy/uploadPicture.xhtml")
	public String uploadPicture(Long memberid, String check, String type, String newslogo, String title, String summary, String content, String url, ModelMap model){
		String checkcode = StringUtil.md5(memberid + "njmk5678");
		if(!StringUtils.equals(check, checkcode)) return showJsonSuccess(model, "请先登录。");
		Member member = daoService.getObject(Member.class, memberid);
		if(member == null) return showJsonSuccess(model, "请先登录。");
		if(StringUtils.isBlank(type)) return showJsonSuccess(model, "参数错误。");
		if(StringUtils.isBlank(newslogo)) return showJsonSuccess(model, "图片不能为空！");
		boolean allow = operationService.updateOperation("uploadSubjectPicture" + member.getId(), OperationService.HALF_MINUTE, 1);
		if(!allow) return showJsonSuccess(model, "你操作过于频繁，请稍后再试！");
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, type);
		params.put(MongoData.ACTION_MEMBERID, member.getId());
		params.put(MongoData.ACTION_STATUS, Status.Y);
		int count = mongoService.getCount(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params);
		if(count > 0) return showJsonSuccess(model, "你的图片已通过审核，请勿重复操作！");
		if(StringUtils.isBlank(title)) return showJsonSuccess(model, "昵称不能为空！");
		if (StringUtils.length(title) > 20) return showJsonSuccess(model, "昵称不能超过20个字符！");
		if(WebUtils.checkString(title)) return showJsonSuccess(model, "昵称不能出现非法字符！");
		if (StringUtils.isBlank(summary)) return showJsonSuccess(model, "简介的内容不能为空！");
		if (StringUtils.length(summary) > 200) return showJsonSuccess(model, "简介的内容不能超过200个字符！");
		if(WebUtils.checkString(summary))return showJsonSuccess(model, "简介的内容不能出现非法字符！");
		Map map = new HashMap();
		map.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestamp());
		map.put(MongoData.ACTION_STARTTIME, DateUtil.format(DateUtil.getCurFullTimestamp(), "yyyy-MM-dd HH:mm:ss"));
		map.put(MongoData.SYSTEM_ID, ObjectId.uuid());
		map.put(MongoData.GEWA_CUP_MEMBERID, member.getId());
		map.put(MongoData.ACTION_MEMBERNAME, member.getNickname());
		map.put(MongoData.ACTION_PICTRUE_URL, newslogo);
		map.put(MongoData.ACTION_SUPPORT, 0);
		map.put(MongoData.ACTION_TYPE, type);
		map.put(MongoData.ACTION_STATUS, Status.Y_NEW);
		map.put(MongoData.ACTION_TITLE, title);
		map.put(MongoData.ACTION_CONTENT, summary);
		mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_PICTRUE);
		if(StringUtils.isNotBlank(content) && StringUtils.isNotBlank(url)){
			shareService.sendShareInfo(MongoData.DRAMA_REDCAT, null, member.getId(), content+url, newslogo);
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/subject/proxy/supportPicture.xhtml")
	public String supportPicture(Long memberid, String check, String type, String id, ModelMap model){
		String checkcode = StringUtil.md5(memberid + "njmk5678");
		if(!StringUtils.equals(check, checkcode)) return showJsonSuccess(model, "请先登录。");
		Member member = daoService.getObject(Member.class, memberid);
		if(member == null) return showJsonSuccess(model, "请先登录。");
		if(!member.isBindMobile()) return showJsonSuccess(model, "请先绑定手机。");
		if(id == null) return showJsonSuccess(model, "参数错误。");
		Map picMap = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_PICTRUE, MongoData.SYSTEM_ID, id);
		if(picMap == null) return showJsonSuccess(model, "不存在此图片！");
		if(StringUtils.equals(member.getId()+"", picMap.get("memberid")+"")) return showJsonSuccess(model, "不可以给自己的照片投票！");
		boolean allow = operationService.updateOperation("supportPicture" + member.getId(), OperationService.HALF_MINUTE, 1);
		if(!allow) return showJsonSuccess(model, "你操作过于频繁，请稍后再试！");
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, type);
		params.put(MongoData.ACTION_MEMBERID, member.getId());
		int count = mongoService.getCount(MongoData.NS_ACTIVITY_COMMON_MEMBER, params);
		if(count >= 1) return showJsonSuccess(model, "投票次数已用完，感谢你的参与！");
		Map memberMap = new HashMap();
		memberMap.put(MongoData.SYSTEM_ID, ObjectId.uuid());
		memberMap.put(MongoData.ACTION_TYPE, type);
		memberMap.put(MongoData.ACTION_RELATEDID, id);
		memberMap.put(MongoData.ACTION_ADDTIME, System.currentTimeMillis());
		memberMap.put(MongoData.ACTION_MEMBERID, member.getId());
		memberMap.put(MongoData.ACTION_TITLE, picMap.get(MongoData.ACTION_TITLE));
		memberMap.put(MongoData.ACTION_MEMBERNAME, member.getNickname());
		picMap.put(MongoData.ACTION_SUPPORT, Integer.parseInt(picMap.get("support")+"") + 1);
		mongoService.saveOrUpdateMap(picMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_PICTRUE);
		mongoService.saveOrUpdateMap(memberMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
		return showJsonSuccess(model);
	}
	//获取审核成功图片
	@RequestMapping("/subject/proxy/getSupportMember.xhtml")
	public String getSupportMember(String type, Integer fromnum, Integer maxnum, ModelMap model){
		if(fromnum == null || maxnum == null || StringUtils.isBlank(type)) return showJsonError(model, "参数错误！");
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, type);
		List<Map> supportMemberList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_MEMBER, params, MongoData.ACTION_ADDTIME, false, fromnum, maxnum);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(supportMemberList));
	}
	
	//获取审核成功图片
	@RequestMapping("/subject/proxy/getCheckPicture.xhtml")
	public String getRedCatPic(String type, Integer fromnum, Integer maxnum, String orderField, ModelMap model){
		if(fromnum == null || maxnum == null || StringUtils.isBlank(type)) return showJsonError(model, "参数错误！");
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, type);
		params.put(MongoData.ACTION_STATUS, Status.Y);
		List<Map> joinCatList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params, orderField, false, fromnum, maxnum);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(joinCatList));
	}
	
	//获取审核成功图片数量
	@RequestMapping("/subject/proxy/getCheckPictureCount.xhtml")
	public String getCheckPictureCount(String type, ModelMap model){
		if(StringUtils.isBlank(type)) return showJsonError(model, "参数错误！");
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, type);
		params.put(MongoData.ACTION_STATUS, Status.Y);
		int count = mongoService.getCount(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params);
		return showJsonSuccess(model, count+"");
	}
	
	//发送邮件
	@RequestMapping("/subject/proxy/sendEmail.xhtml")
	public String sendEmail(Long memberid, String check, String nickname, String email, String title,
			String content, String type, String tag, String template, ModelMap model){
		String checkcode = StringUtil.md5(memberid + "njmk5678");
		if(!StringUtils.equals(check, checkcode)) return showJsonSuccess(model, "请先登录。");
		Member member = daoService.getObject(Member.class, memberid);
		if(member == null) return showJsonSuccess(model, "请先登录。");
		if(StringUtils.isBlank(type) || StringUtils.isBlank(tag) || StringUtils.isBlank(template)) return showJsonSuccess(model, "参数错误！");
		if(StringUtils.isBlank(nickname)) return showJsonSuccess(model, "姓名不能为空！");
		if (StringUtils.length(nickname) > 10) return showJsonSuccess(model, "姓名不能超过10个字符！");
		if(WebUtils.checkString(nickname)) return showJsonSuccess(model, "姓名不能出现非法字符！");
		if(StringUtils.isBlank(email)) return showJsonSuccess(model, "邮箱不能为空！");
		if(!ValidateUtil.isEmail(email)) return showJsonSuccess(model, "邮箱格式不正确！");
		if(StringUtils.isBlank(title)) return showJsonSuccess(model, "标题不能为空！");
		if (StringUtils.length(title) > 60) return showJsonSuccess(model, "标题不能超过60个字符！");
		if(WebUtils.checkString(title)) return showJsonSuccess(model, "标题不能出现非法字符！");
		if(StringUtils.isBlank(content)) return showJsonSuccess(model, "邮件内容不能为空！");
		if (StringUtils.length(content) > 400) return showJsonSuccess(model, "邮件内容不能超过400个字符！");
		if(WebUtils.checkString(content)) return showJsonSuccess(model, "邮件内容不能出现非法字符！");
		String key =  "sendZhuantiEmail" + memberid;
		boolean allow = operationService.isAllowOperation(key, 1, OperationService.HALF_DAY, 5);
		if(!allow) return showJsonSuccess(model, "每人每天限发送5封邮件！");
		Map contentMap = new HashMap();
		contentMap.put("nickname", nickname);
		contentMap.put("content", content);
		contentMap.put("type", type);
		contentMap.put("tag", tag);
		mailService.sendTemplateEmail(nickname, title, template, contentMap, email);
		operationService.updateOperation(key, 1, OperationService.HALF_DAY, 5);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/subject/proxy/memberDynamic.xhtml")
	public String memberDynamic(ModelMap model){
		return showJsonSuccess(model, JsonUtils.writeMapToJson(commonService.getCurIndexDataSheet()));
	}
	//前台部分end
	
	//后台部分star
	@RequestMapping("/admin/newsubject/dualHolidays.xhtml")
	public String dualHolidays(){
		return "admin/newsubject/after/dualHolidays.vm";
	} 
	//后台部分end
	
}