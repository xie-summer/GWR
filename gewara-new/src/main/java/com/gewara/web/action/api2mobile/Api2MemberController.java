package com.gewara.web.action.api2mobile;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.SellSeat;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.SysMessageAction;
import com.gewara.model.user.UserMessage;
import com.gewara.model.user.UserMessageAction;
import com.gewara.service.MessageService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.GewaPicService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.PictureUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.api.BaseApiController;

/**
 * 会员信息
 * @author taiqichao
 *
 */
@Controller
public class Api2MemberController extends BaseApiController{
	
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	@Autowired@Qualifier("messageService")
	private MessageService messageService;
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	/**
	 * 修改用户信息
	 * @param memberEncode
	 * @param logoHex
	 * @param filetype
	 * @param provincecode
	 * @param citycode
	 * @param countycode
	 * @param birthday
	 * @param sex
	 * @param sign
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/member/updateMemberInfo.xhtml")
	public String updateMemberInfo(String memberEncode,String logoHex,String filetype, String nickname,
			String provincecode,String citycode,String countycode,String birthday,String sex, ModelMap model, HttpServletRequest request){
		if(StringUtils.isBlank(memberEncode)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "memberEncode不能为空！");
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "用户不存在！");
		MemberInfo memberInfo = this.daoService.getObject(MemberInfo.class, member.getId());
		if(StringUtils.isNotBlank(sex))memberInfo.setSex(sex);
		if(StringUtils.isNotBlank(birthday))memberInfo.setBirthday(birthday);
		if(StringUtils.isNotBlank(provincecode))memberInfo.setLiveprovince(provincecode);
		if(StringUtils.isNotBlank(citycode))memberInfo.setLivecity(citycode);
		if(StringUtils.isNotBlank(countycode))memberInfo.setLivecounty(countycode);
		if(StringUtils.isNotBlank(citycode) && AdminCityContant.zxsList.contains(citycode)) memberInfo.setLiveprovince(citycode);
		if(StringUtils.isNotBlank(logoHex)){
			ErrorCode<String> returnCode = this.uploadPic(member.getId(), logoHex, "member",filetype,PictureUtil.getHeadPicpath());
			if(!returnCode.isSuccess())return getErrorXmlView(model,returnCode.getErrcode(),returnCode.getMsg());
			memberInfo.setHeadpic(returnCode.getMsg());
		}
		memberInfo.setUpdatetime(DateUtil.getCurFullTimestamp());
		if(StringUtils.isNotBlank(nickname)){
			boolean matchNickname = ValidateUtil.isCNVariable(nickname, 2, 15);
			if(!matchNickname) {
				return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "用户昵称格式不正确，不能包含特殊符号！");
			}
			boolean bNickname=memberService.isMemberExists(nickname, member.getId());
			if(bNickname){
				return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "您输入的昵称已被占用！");
			} 
			memberInfo.setNickname(nickname);
			member.setNickname(nickname);
		}
		daoService.saveObject(member);
		daoService.saveObject(memberInfo);
		return getXmlView(model, "api/mobile/result.vm");
	}
	
	@SuppressWarnings("unchecked")
	private ErrorCode<String> uploadPic(long memberid, String pic, String tag, String filetype, String path){
		if(pic ==null) return ErrorCode.getFailure("上传图片的十六进制流不能为空");
		try {
			if(StringUtils.isBlank(filetype)) return ErrorCode.getFailure(ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
			ByteArrayInputStream is = new ByteArrayInputStream(Hex.decodeHex(pic.toCharArray()));
			String filename = gewaPicService.saveToTempPic(is, filetype);
			if(!PictureUtil.isValidPicType(StringUtil.getFilenameExtension(filename))) {
				return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR,  "上传图片格式不合法！只支持jpg,png,gif,jpeg格式");
			}
			gewaPicService.saveTempFileToRemote(filename);
			String filepath =  gewaPicService.moveRemoteTempTo(memberid, tag, memberid, path, filename);
			return ErrorCode.getSuccess(filepath.replaceFirst("/",""));
		} catch (Exception e) {
			return ErrorCode.getFailure(ApiConstant.CODE_PARAM_ERROR,  "上传图片错误，请重试!");
		}
	}
	
	
	
	@RequestMapping("/api2/home/orderList.xhtml")
	public String orderList(String memberEncode,int from ,int maxnum,ModelMap model){
		if(maxnum > 15 ){
			maxnum = 15;
		}
		Member member = this.memberService.getMemberByEncode(memberEncode);
		if(member == null) {
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "请先登录！");
		}
		List<TicketOrder> orderList = orderQueryService.getOrderListByMemberId(TicketOrder.class, member.getId(), OrderConstant.STATUS_PAID, 180, from, maxnum);
		GewaOrder lastUnpaidOrder = orderQueryService.getLastUnpaidOrder(member.getId());
		if(lastUnpaidOrder!=null) {
			model.put("lastUnpaidOrder", lastUnpaidOrder);
		}
		model.put("orderList", orderList);
		return getXmlView(model, "api/order/wapHomeOrderList.vm");
	}
	@RequestMapping("/api2/mobile/member/orderList.xhtml")
	public String ticketOrderList(String memberEncode,int from ,int maxnum,ModelMap model){
		return orderList(memberEncode, from, maxnum, model);
	}
	@RequestMapping("/api2/home/getOrderById.xhtml")
	public String getOrderById(ModelMap model,long orderId,String memberEncode){
		Member member = this.memberService.getMemberByEncode(memberEncode);
		if(member == null) {
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "请先登录！");
		}
		GewaOrder order = daoService.getObject(GewaOrder.class, orderId);
		if(order == null){
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "订单不存在");
		}
		if(!order.getMemberid().equals(member.getId())){
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "订单不属于你不能进行查看!");
		}
		if(member.isBindMobile()){
			model.put("isShowCheckpass",true);
		}else{
			model.put("isShowCheckpass",false);
		}
		if(order.isPaidSuccess()&&"ticket".equals(order.getOrdertype())){
			String passMsg = getOrderPassword((TicketOrder)order);
			model.put("passMsg", passMsg);
		}
		model.put("order", order);
		model.put("itemList", daoService.getObjectListByField(BuyItem.class, "orderid", order.getId()));
		return getXmlView(model, "api/order/orderDetail.vm");
	}
	@RequestMapping("/api2/mobile/getOrderById.xhtml")
	public String getOrderById2(ModelMap model,long orderId,String memberEncode){
		return getOrderById(model, orderId, memberEncode);
	}
	private String getOrderPassword(TicketOrder ticketOrder){
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", ticketOrder.getMpid(), false);
		if(opi==null || opi.getPlaytime().before(DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -1))) {
			dbLogger.warn("查询过期场次，不需密码:" + ticketOrder.getMpid());
			return null;
		}
		String msgTemplate = messageService.getCheckpassTemplate(opi);
		List<SellSeat> seatList = ticketOrderService.getOrderSeatList(ticketOrder.getId());
		ErrorCode<String> msg = messageService.getCheckpassMsg(msgTemplate, ticketOrder, seatList, opi);
		String password = msg.getRetval();
		List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", ticketOrder.getId());
		if(itemList.size() >0 ) {
			for(BuyItem item : itemList){
				password = password + "\n套餐：" + item.getGoodsname()+"\n套餐密码：" + item.getCheckpass();
			}
		}
		return password;
	}
	/**
	 * 私信列表（与某个用户的私信列表）详细
	 * @param mid 私信id
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/member/userMessageDetail.xhtml")
	public String userMessageDetail(String memberEncode, Long memberid, Timestamp addtime, Integer from, Integer maxnum, ModelMap model){
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "请先登录！");
		if(from == null) from = 0;
		if(maxnum == null || maxnum > 20) maxnum = 20;
		if(addtime!=null) addtime = DateUtil.addSecond(addtime, 1);
		List<UserMessageAction> umaList = userMessageService.getUserMessageActionByFromidToid(member.getId(), memberid, addtime, from, maxnum);
		Map<Long, UserMessage> userMessageMap=new HashMap<Long, UserMessage>();
		for(UserMessageAction ua : umaList){
			if(ua.getIsread().equals(TagConstant.READ_NO)) {
				ua.setIsread(TagConstant.READ_YES);
				daoService.saveObject(ua);
			}
			userMessageMap.put(ua.getId(), daoService.getObject(UserMessage.class, ua.getUsermessageid()));
		}
		model.put("userMessageMap", userMessageMap);
		model.put("umaList", umaList);
		addCacheMember(model, BeanUtil.getBeanPropertyList(umaList, Long.class, "frommemberid", true));
		addCacheMember(model, BeanUtil.getBeanPropertyList(umaList, Long.class, "tomemberid", true));
		return getXmlView(model, "api2/member/userMessageDetail.vm");
	}
	@RequestMapping("/api2/mobile/member/userMessageDetail.xhtml")
	public String userMessageDetail2(String memberEncode, Long memberid, Timestamp addtime, Integer from, Integer maxnum, ModelMap model){
		return userMessageDetail(memberEncode, memberid, addtime, from, maxnum, model);
	}
	/**
	 * 发私信
	 * @param memberEncode
	 * @param tomemberid
	 * @param content
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/member/saveUserMessage.xhtml")
	public String saveUserMessage(String memberEncode, Long groupid, Long tomemberid, String subject, String content, ModelMap model){
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "请先登录！");
		Member toMember = null;
		String key = blogService.filterContentKey(content);
		if(StringUtils.isNotBlank(key)){
			return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "内容含有非法关键字符！");
		}
		List<UserMessageAction> userMessageActionList = new ArrayList<UserMessageAction>();
		toMember = daoService.getObject(Member.class, tomemberid);
		if(toMember != null){
			UserMessage userMessage = new UserMessage(""); 
			userMessage.setContent(content);
			userMessage.setSubject(subject);
			if(StringUtils.isBlank(userMessage.getSubject())) 
				userMessage.setSubject(TagConstant.DEFAULT_SUBJECT);
			daoService.saveObject(userMessage);
			
			UserMessageAction uma = new UserMessageAction(toMember.getId());
			uma.setFrommemberid(member.getId());
			uma.setUsermessageid(userMessage.getId());
			if(groupid != null) uma.setGroupid(groupid);
			if(uma.getGroupid()==null) { //新发表的情况
				uma.setGroupid(userMessage.getId());
			}
			userMessageActionList.add(uma);
		}
		if(userMessageActionList.isEmpty()) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "接收人为空，请确认输入的格式为Email、用户昵称或手机号！");
		daoService.saveObjectList(userMessageActionList);
		return getXmlView(model, "api/mobile/result.vm");
	}
	@RequestMapping("/api2/mobile/member/saveUserMessage.xhtml")
	public String saveUserMessage2(String memberEncode, Long groupid, Long tomemberid, String subject, String content, ModelMap model){
		return saveUserMessage(memberEncode, groupid, tomemberid, subject, content, model);
	}
	/**
	 * 我的系统消息
	 * @param memberEncode
	 * @param from
	 * @param maxnum
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/member/receiveSystemMessageList.xhtml")
	public String receiveSystemMessageList(String memberEncode, Integer from, Integer maxnum, ModelMap model){
		Member member = memberService.getMemberByEncode(memberEncode);
		if(from == null) from = 0;
		if(maxnum == null) maxnum = 20;
		//系统消息
		List<SysMessageAction> sysMsgList = userMessageService.getSysMsgListByMemberid(member.getId(),null, from, maxnum);
		model.put("sysMsgList", sysMsgList);
		return getXmlView(model, "api2/member/systemMessageList.vm");
	}
	@RequestMapping("/api2/mobile/member/receiveSystemMessageList.xhtml")
	public String receiveSystemMessageList2(String memberEncode, Integer from, Integer maxnum, ModelMap model){
		return receiveSystemMessageList(memberEncode, from, maxnum, model);
	}
	/**
	 * 系统消息未读数量
	 * @param memberEncode
	 * @param model
	 * @return
	 */
	@RequestMapping("/api2/member/receiveSystemMessageCount.xhtml")
	public String receiveSystemMessageCount(String memberEncode, ModelMap model){
		Member member = memberService.getMemberByEncode(memberEncode);
		//未读系统消息数
		Integer syscount = memberService.getMemberNotReadSysMessageCount(member.getId());
		//未读私信消息数
		Integer lettercount =   memberService.getMemberNotReadNormalMessageCount(member.getId());
		//未读粉丝数量
		Integer fanscount = 0;
		Map data = mongoService.getMap(MongoData.SYSTEM_ID, MongoData.NS_PROMPT_INFO, member.getId());
		if(data != null && data.get("fans") != null) fanscount = Integer.parseInt(data.get("fans")+"");
		model.put("count", syscount + fanscount + lettercount);
		return getXmlView(model, "api2/count.vm");
	}
}