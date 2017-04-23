package com.gewara.web.action.user;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.sys.MongoData;
import com.gewara.json.MemberStats;
import com.gewara.model.user.Member;
import com.gewara.model.user.Treasure;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.content.RecommendService;
import com.gewara.untrans.GewaPicService;
import com.gewara.untrans.MemberCountService;
import com.gewara.untrans.WalaApiService;
import com.gewara.util.JsonUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

/**
 * 新版哇啦
 * @author lss
 */
@Controller
public class MicroBlogController extends AnnotationController {
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	@Autowired@Qualifier("memberCountService")
	private MemberCountService memberCountService;

	@Autowired@Qualifier("walaApiService")
	private WalaApiService walaApiService;
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	public void setGewaPicService(GewaPicService gewaPicService) {
		this.gewaPicService = gewaPicService;
	}
	@Autowired@Qualifier("recommendService")
	private RecommendService recommendService;
	public void setRecommendService(RecommendService recommendService) {
		this.recommendService = recommendService;
	}
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	
	// 微博图片删除
	@RequestMapping("/wala/delMicroBlogPic.xhtml")
	public String delpic(String picpath, ModelMap model) {
		//之前的方法, 根据picpath 直接获取路径进行删除, 现使用acerge HDFS图片删除方法
		// wala图片未存储在 Picture表中, 所以只用删除服务器对应的图片路径即可.
		try {
			boolean isSuc = gewaPicService.removePicture(picpath);
			if(isSuc) return showJsonSuccess(model);
		} catch (IOException e) {
			dbLogger.error("", e);
		}
		return showJsonError(model, "删除时出错!");
	}


	/**
	 * 功能描述 : 检测当前url是否可连接或是否有效, 最多连接网络 5 次, 如果 5 次都不成功说明该地址不存在或视为无效地址.
	 * 
	 * @param url
	 *           指定url网络地址
	 * 
	 * @return string
	 */
	@RequestMapping("/wala/isConnect.xhtml")
	public String isConnect(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)String sessid, 
			HttpServletRequest request, ModelMap model, String url) {
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError_NOT_LOGIN(model);
		int counts = 0;
		String succ = null;
		while (counts < 2) {
			try {
				URL urlstr = new URL(url);
				HttpURLConnection connection = (HttpURLConnection) urlstr.openConnection();
				connection.setConnectTimeout(3000);
				connection.setReadTimeout(2000);	// 设置读取时间, 不然会造成堵塞.
				int state = connection.getResponseCode();
				if (state == 200) {
					succ = connection.getURL().toString();
					if (StringUtils.isNotBlank(succ))
						return showJsonSuccess(model);
				}
				break;
			} catch (Exception ex) {
				counts++;
				continue;
			}
		}
		return showJsonError(model, "您输入的链接为无效链接！");
	}
	
	
	
	/**
	 * 移除粉丝关系
	 */
	@RequestMapping("/wala/cancelFans.xhtml")
	public String cancelFans(ModelMap model,Long fansid, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		Member meb = daoService.getObject(Member.class, fansid);
		if (meb == null)
			return showJsonError(model, "你想要移除的粉丝对象不存在！");
		boolean b = blogService.cancelTreasure(fansid, member.getId(), Treasure.TAG_MEMBER, Treasure.ACTION_COLLECT);
		if (b){
			memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_FANSCOUNT, 1, false);
			memberCountService.updateMemberCount(meb.getId(), MemberStats.FIELD_ATTENTIONCOUNT, 1, false);
			return showJsonSuccess(model, "移除粉丝成功！");
		}else{
			return showJsonError(model, "移除粉丝失败！");
		}
	}
	/**
	 * 添加关注
	 */
	@RequestMapping("/wala/addMicroAttention.xhtml")
	public String addAttention(Long memberid, ModelMap model, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request) {
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError_NOT_LOGIN(model);
		if ((member.getId() + "").equals(memberid + "")) {
			return showJsonError(model, "不能对自己进行关注！");
		}
		boolean b = blogService.isTreasureMember(member.getId(), memberid);
		if (b) {
			return showJsonError(model, "已添加到关注列表，请不要重复操作！");
		}
		Treasure treasure = new Treasure(member.getId());
		treasure.setRelatedid(memberid);
		treasure.setAction(Treasure.ACTION_COLLECT);
		treasure.setTag(Treasure.TAG_MEMBER);
		daoService.saveObject(treasure);
		walaApiService.addTreasure(treasure);
		//关注，粉丝数加一
		memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_ATTENTIONCOUNT, 1, true);
		memberCountService.updateMemberCount(memberid, MemberStats.FIELD_FANSCOUNT, 1, true);
		//关注，粉丝数加一
		
		//添加一条粉丝通知
		recommendService.memberAddFansCount(memberid, MongoData.MESSAGE_FANS_ADD, MongoData.MESSAGE_FANS, 1);
		
		//个人中心左边显示感兴趣的用户，当点击关注一个用户的时候，就删除掉这个兴趣用户
		Map params = memberCountService.getMemberInfoStats(member.getId());
		if(params != null){
			String jsonStr = (String)params.get("recommedPerson");
			List<Map> memberList = JsonUtils.readJsonToObject(List.class, jsonStr);
			for(Map map : memberList){
				String mid = map.get("memberid").toString();
				if(Long.valueOf(mid).equals(memberid)){
					memberList.remove(map);
					break;
				}
			}
			jsonStr = JsonUtils.writeObjectToJson(memberList);
			params.put("recommedPerson", jsonStr);
			
			mongoService.saveOrUpdateMap(params, "myid", MongoData.NS_MEMBER_INFO);
		}
		
		return showJsonSuccess(model, "添加关注成功！");
	}
	
	/**
	 * 取消关注用户
	 * @param memberid
	 * @param model
	 * @return
	 */
	@RequestMapping("/wala/cancelAttention.xhtml")
	public String cancelAttention(Long memberid, ModelMap model, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request) {
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		Member meb = daoService.getObject(Member.class, memberid);
		if (meb == null)
			return showJsonError(model, "取消关注的对象不存在！");
		boolean b = blogService.cancelTreasure(member.getId(), memberid, Treasure.TAG_MEMBER, Treasure.ACTION_COLLECT);
		if (b){
			//关注，粉丝数减一
			memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_ATTENTIONCOUNT, 1, false);
			memberCountService.updateMemberCount(memberid, MemberStats.FIELD_FANSCOUNT, 1, false);
			//关注，粉丝数减一
			walaApiService.delTreasure(member.getId(), memberid, Treasure.TAG_MEMBER, Treasure.ACTION_COLLECT);
			return showJsonSuccess(model, "取消关注成功！");
		}else{
			return showJsonError(model, "取消关注失败！");
		}
	}
	/**
	 * 取消关注,场馆，项目，人
	 * @param model
	 * @param tid
	 * @return
	 */
	@RequestMapping("/wala/cancelTreasure.xhtml")
	public String cancelTreasure(ModelMap model, Long tid, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request) {
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		Treasure treasure = daoService.getObject(Treasure.class, tid);
		if (treasure == null)
			return show404(model, "你访问的对象不存在！");
		boolean b = false;
		if (member.getId().equals(treasure.getMemberid())) {
			try {
				walaApiService.delTreasure(treasure.getMemberid(), treasure.getRelatedid(), treasure.getTag(), treasure.getAction());
				daoService.removeObject(treasure);
				b = true;
			} catch (Exception e) {
				b = false;
			}
		} else {
			return show404(model, "此对象不是你关注的！不能对其取消关注");
		}
		if (b){
			if(StringUtils.equals(treasure.getTag(), "member")){
				memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_ATTENTIONCOUNT, 1, false);
			}
			memberCountService.updateMemberCount(treasure.getMemberid(), MemberStats.FIELD_FANSCOUNT, 1, false);
			walaApiService.delTreasure(treasure.getMemberid(), treasure.getRelatedid(), treasure.getTag(), treasure.getAction());
			return showJsonSuccess(model, "取消关注成功!");
		}else{
			return showJsonError(model, "取消关注失败！");
		}
	}
}
