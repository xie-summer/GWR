package com.gewara.web.action.subject;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.MemberConstant;
import com.gewara.model.user.ShareMember;
import com.gewara.service.OperationService;
import com.gewara.untrans.ShareService;
import com.gewara.util.RandomUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.AnnotationController;

/**
 * May 18, 2012 4:21:45 PM
 * @author bob
 * @desc 黑衣人专题 代理
 */
@Controller
public class BlackMenProxyController  extends AnnotationController {
	
	private static final List<String> stringList = Arrays.asList("你是来自《ET》的大眼睛外星人","你是来自《第五元素》的蓝色歌姬",
			"你是来自《独立日》的外星俘虏","你是来自《阿凡达》的蓝色自由战士","你是来自《异形》的粘液怪物","你是来自《保罗》的绿色外星人",
			"你是来自《星际迷航》的长耳朵将军","你是来自《超级战舰》的钢铁怪物","你是来自《变形金刚》的超级战士","你是来自《关键第四号》的帅哥外星客");
	
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	public void setShareService(ShareService shareService) {
		this.shareService = shareService;
	}
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}

	@RequestMapping("/subject/proxy/blackmen/sendsinafriendMsg.xhtml")
	public String blackmensendsinafriendMsg(Long memberid, ModelMap model){
		if(memberid == null) return showJsonError(model, "请先登录！");
		List<ShareMember> shareMemberList = shareService.getShareMemberByMemberid(Arrays.asList(MemberConstant.SOURCE_SINA),memberid);
		if(VmUtils.isEmptyList(shareMemberList)) return showJsonError(model, "请先绑定新浪微博！");
		String opkey = "blackmenActivity" + memberid;
		if(!operationService.isAllowOperation(opkey, OperationService.ONE_HOUR, 3)){
			return showJsonError(model, "操作过于频繁！");
		}
		List<String> friendsList = shareService.getSinaFriendList(memberid, 5);
		String resultString = "";
		if(friendsList.isEmpty()){
			resultString = "@格瓦拉生活网 "+RandomUtil.getRandomObjectList(stringList, 1).get(0);
		}else{
			List tempList = RandomUtil.getRandomObjectList(stringList, friendsList.size());
			for(int i = 0; i<friendsList.size(); i++){
				resultString += "@" + friendsList.get(i) + " "+ tempList.get(i);
			}
		}
		operationService.updateOperation(opkey, OperationService.ONE_DAY, 3);
		return showJsonSuccess(model, resultString);
	}
	
}