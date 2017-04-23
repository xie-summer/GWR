package com.gewara.web.action.inner.mobile.member;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.helper.api.GewaApiMemberHelper;
import com.gewara.helper.api.GewaApiOrderHelper;
import com.gewara.json.SeeMovie;
import com.gewara.model.common.City;
import com.gewara.model.common.County;
import com.gewara.model.common.Province;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.user.Jobs;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.MemberUsefulAddress;
import com.gewara.model.user.OpenMember;
import com.gewara.model.user.Treasure;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.GewaPicService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.PictureUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.inner.OpenApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;

@Controller
public class OpenApiMobileMemberController extends BaseOpenApiMobileController{
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	private int getMyCinemaCount(Member member, String citycode, List<Long> cidList){
		if(StringUtils.isBlank(citycode)) return 0;
		List<Cinema> myCinemaList = orderQueryService.getMemberOrderCinemaList(member.getId(), 100);
		List<Long> orderList = new ArrayList<Long>();
		for(Cinema cinema : myCinemaList){
			if(StringUtils.equals(cinema.getCitycode(), citycode) && !orderList.contains(cinema.getId())){
				orderList.add(cinema.getId());
			}
		}
		Set<Long> set1 = new HashSet<Long>(orderList);
		set1.addAll(cidList);
		return set1.size();
	}
	/**
	 * 用户信息
	 */
	@RequestMapping("/openapi/mobile/member/memberInfo.xhtml")
	public String memberInfo(String citycode, ModelMap model, HttpServletRequest request){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if(member==null){
			dbLogger.warn("qry memberEncode:" + WebUtils.getRequestMap(request).toString());
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "获取登录信息失败，请刷新重试或重新登录！");
		}
		Long memberid = member.getId();
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, memberid);
		Map<String, Object> resMap = getMemberMap(member, memberInfo);
		MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", memberid, false);
		resMap.put("banlance", memberAccount==null?0:memberAccount.getBanlance());
		resMap.put("bankcharge", memberAccount==null?0:memberAccount.getBankcharge());
		resMap.put("wabi", memberAccount==null?0:memberAccount.getOthercharge());
		resMap.put("realname", memberAccount==null?"":memberAccount.getRealname());
		Jobs jobs = memberService.getMemberPosition(memberInfo.getExpvalue());
		Integer treasureMovieCount = blogService.getTreasureCountByMemberId(memberid, new String[]{TagConstant.TAG_MOVIE}, null, Treasure.ACTION_COLLECT);
		List<Long> cidList = blogService.getTreasureCinemaidList(citycode, memberid, Treasure.ACTION_COLLECT);
		Integer treasureCinemaCount = getMyCinemaCount(member, citycode, cidList);
		int accountintegrity = 1;
		//账户信息完整性
		if (memberAccount == null || memberAccount.isNopassword()) accountintegrity = 0;
		resMap.put("position", jobs.getPosition());
		resMap.put("accountintegrity", accountintegrity);
		resMap.put("memberEncode", request.getParameter("memberEncode"));
		resMap.put("collCinemaCount", treasureCinemaCount);
		resMap.put("collMovieCount", treasureMovieCount);
		ErrorCode<OpenMember> omCode = getOpenMember(memberInfo);
		if(omCode.isSuccess()){
			OpenMember om = omCode.getRetval();
			if(StringUtils.isNotBlank(om.getNickname())) resMap.put("nickname", om.getNickname());
		}
		
		initField(model, request);
		model.put("resMap", resMap);
		model.put("root", "member");
		return getOpenApiXmlDetail(model);
	}
	
	/**
	 * 其他的用户信息
	 */
	@RequestMapping("/openapi/mobile/member/otherMemberInfo.xhtml")
	public String memberInfo(Long memberid, ModelMap model, HttpServletRequest request){
		Member otherMember = daoService.getObject(Member.class, memberid);
		if(otherMember==null){
			return getErrorXmlView(model, ApiConstant.CODE_NOT_EXISTS, "用户不存在！"); 
		}
		Member selfMember = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		MemberInfo otherInfo = daoService.getObject(MemberInfo.class, memberid);
		int iscollect = 0;
		if(selfMember!=null){
			boolean res = blogService.isTreasureMember(selfMember.getId(), otherMember.getId());
			if(res) iscollect = 1;
		}
		Map<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("memberid", memberid);
		resMap.put("iscollect", iscollect);
		resMap.put("nickname", otherMember.getNickname());
		resMap.put("headpic", getMobilePath() + otherInfo.getHeadpicUrl());
		String sex = otherInfo.getSex();
		if(StringUtils.isBlank(sex)) sex = "女";
		resMap.put("sex", sex);
		initField(model, request);
		model.put("resMap", resMap);
		model.put("root", "member");
		return getOpenApiXmlDetail(model);
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
	
	/**
	 * 修改用户信息
	 */
	@RequestMapping("/openapi/mobile/member/updateMemberInfo.xhtml")
	public String updateMemberInfo(String logoHex,String filetype, String nickname,
			String provincecode,String citycode,String countycode,String birthday,String sex, ModelMap model){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
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
		return getSuccessXmlView(model);
	}
	
	/**
	 * 绑定手机号
	 */
	@RequestMapping("/openapi/mobile/member/bindingMobile.xhtml")
	public String bindingMobile(String memberEncode,
			@RequestParam(required = true, value = "mobile") String mobile,
			@RequestParam(required = true, value = "dynamicNumber") String dynamicNumber,
			ModelMap model){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		ErrorCode result = memberService.bindMobile(member, mobile, dynamicNumber, auth.getRemoteIp());
		if(result.isSuccess()){
			member = daoService.getObject(Member.class, member.getId());
			memberService.updateMemberByMemberEncode(memberEncode, member);
			return getSuccessXmlView(model);
		}else{
			return getErrorXmlView(model, result.getErrcode(), result.getMsg());
		}
	}
	
	/**
	 * 新增账户信息
	 * @param realname
	 * @param password
	 * @param confirmPassword
	 * @param idcard
	 * @param model
	 * @return
	 */
	@RequestMapping("/openapi/mobile/member/saveAccount.xhtml")
	public String saveAccount(
			@RequestParam(required = true, value = "realname") String realname, 
			@RequestParam(required = true, value = "password") String password, 
			@RequestParam(required = true, value = "confirmPassword") String confirmPassword, 
			@RequestParam(required = true, value = "idcard") String idcard, 
			ModelMap model){
		
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		
		ErrorCode<MemberAccount> account = paymentService.createOrUpdateAccount(member, realname, password, confirmPassword, idcard);
		if(!account.isSuccess()) {
			return getErrorXmlView(model, account.getErrcode(), account.getMsg());
		}
		return getSuccessXmlView(model);
	}
	
	/**
	 * 贵宾卡充值
	 */
	@RequestMapping("/openapi/mobile/member/ccardPayCharge.xhtml")
	public String ccardPayCharge(@RequestParam(required = true, value = "cardpass") String cardpass, ModelMap model){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		// 检查账户信息是否完整
		MemberAccount account = daoService.getObjectByUkey(MemberAccount.class,"memberid", member.getId(), false);
		if (account == null || account.isNopassword()) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "请您先设置支付密码！");
		}
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		ErrorCode<ElecCard> code = elecCardService.chargeByCard(member, account, cardpass, auth.getRemoteIp());
		if (code.isSuccess()) {
			return getMsgResult(model, SUCCESS,"充值成功，充值金额为" + code.getRetval().getEbatch().getAmount());
		} else {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "充值错误:" + code.getMsg());
		}
	}
	
	
	/**
	 * 我的票券
	 */
	@RequestMapping("/openapi/mobile/member/cardList.xhtml")
	public String cardList(String tag,Integer from,Integer maxnum, String validItem, ModelMap model, HttpServletRequest request) {
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if(maxnum != null && maxnum >20) maxnum = 20;
		if(tag == null )tag = TagConstant.TAG_MOVIE;
		List<ElecCard> cardList = elecCardService.getCardListByMemberid(member.getId(), tag, from, maxnum);
		Collections.sort(cardList,new PropertyComparator("status", true, false));
		List<ElecCard> cardList1 = new ArrayList<ElecCard>();
		List<ElecCard> cardList2 = new ArrayList<ElecCard>();
		List<ElecCard> newCardList = new ArrayList<ElecCard>();
		for(ElecCard card : cardList){
			if(card.available()){
				cardList1.add(card);
			}else {
				cardList2.add(card);
			}
		}
		Collections.sort(cardList1,new PropertyComparator("endtime", true, true));
		newCardList.addAll(cardList1);
		newCardList.addAll(cardList2);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		Map<Long, String> movieMap = new HashMap<Long, String>();
		for (ElecCard card : newCardList) {
			Map<String, Object> params = GewaApiOrderHelper.getCardMap(card);
			resMapList.add(params);
			if (StringUtils.isBlank(validItem)) {
				continue;
			}
			String movienames = "";
			String tmp = card.getEbatch().getValidmovie();
			if(StringUtils.isNotBlank(tmp) && card.available()){
				if(!movieMap.containsKey(card.getEbatch().getId())){
					List<Long> movieIdList = BeanUtil.getIdList(tmp, ",");
					List<Movie> movieList = daoService.getObjectList(Movie.class, movieIdList);
					List<String> nameList = BeanUtil.getBeanPropertyList(movieList, String.class, "moviename", true);
					movienames = StringUtils.join(nameList, ",");
					movieMap.put(card.getEbatch().getId(), movienames);
				}else {
					movienames = movieMap.get(card.getEbatch().getId());
				}
			}
			params.put("itemnames", movienames);
		}
		putList(resMapList, model, request);
		model.put("root", "cardList");
		model.put("nextroot", "card");
		return getOpenApiXmlList(model);
	}
	
	/**
	 * 绑定票券
	 */
	@RequestMapping("/openapi/mobile/member/bindCard.xhtml")
	public String bindCardInfo(String cardPass,ModelMap model){
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		Member member = auth.getMember();
		ErrorCode code = elecCardService.registerCard(member, cardPass, auth.getRemoteIp());
		if(code.isSuccess()) return getSuccessXmlView(model);
		else return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR,code.getMsg());
	}
	//充值记录
	@RequestMapping("/openapi/mobile/member/chargeList.xhtml")
	public String chargeList(int from, int maxnum ,ModelMap model){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		List<Charge> chargeList = paymentService.getChargeListByMemberId(member.getId(), null, null, from, maxnum);
		model.put("chargeList", chargeList);
		return getXmlView(model, "api2/mobile/chargeList.vm");
	}
	//用户的常用联系地址
	@RequestMapping("/openapi/mobile/member/addressList.xhtml")
	public String addressList(int from, int maxnum, ModelMap model, HttpServletRequest request){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		List<MemberUsefulAddress> memberAddressList = memberService.getMemberUsefulAddressByMeberid(member.getId(), from, maxnum);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(MemberUsefulAddress address : memberAddressList){
			Map<String, Object> resMap = GewaApiMemberHelper.getMemberAddressData(address);
			resMapList.add(resMap);
		}
		return getOpenApiXmlList(resMapList, "memberAddressList,memberAddress", model, request);
	}
	
	//用户的常用联系地址
	@RequestMapping("/openapi/mobile/member/addMemberAddress.xhtml")
	public String addressList(Long addressid, 
			String provincecode, String citycode, String countycode, 
			String address, String realname, String mobile, ModelMap model){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		MemberUsefulAddress memAddress = null;
		if(addressid!=null){
			memAddress = daoService.getObject(MemberUsefulAddress.class, addressid);
			if(memAddress==null) {
				return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR, "该信息地址不存在！"); 
			}
			if(!member.getId().equals(memAddress.getMemberid())){
				return getErrorXmlView(model,ApiConstant.CODE_DATA_ERROR, "操作非法"); 
			}
		}else {
			memAddress = new MemberUsefulAddress();
			memAddress.setMemberid(member.getId());
			memAddress.setAddtime(DateUtil.getMillTimestamp());
		}
		Province pro = daoService.getObject(Province.class, provincecode);
		City city = daoService.getObject(City.class, citycode);
		County county = daoService.getObject(County.class, countycode);
		memAddress.setAddress(address);
		memAddress.setRealname(realname);
		memAddress.setProvincecode(provincecode);
		memAddress.setCitycode(citycode);
		memAddress.setCountycode(countycode);
		memAddress.setMobile(mobile);
		memAddress.setProvincename(pro.getProvincename());
		memAddress.setCityname(city.getCityname());
		memAddress.setCountyname(county.getCountyname());
		daoService.saveObject(memAddress);
		return getSingleResultXmlView(model, memAddress.getId());
	}
	
	/**
	 * 是否首次购票
	 */
	@RequestMapping("/openapi/mobile/member/firstConsume.xhtml")
	public String isFirstConsume(String tradeNo, ModelMap model, HttpServletRequest request) {
		OpenApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		Member member = auth.getMember();
		Map param = new HashMap();
		param.put("memberid", member.getId());
		param.put("tag", TagConstant.TAG_MOVIE);
		List<SeeMovie> seeOrderList = mongoService.getObjectList(SeeMovie.class, param, "adddate", false, 0, 2);
		if (CollectionUtils.isEmpty(seeOrderList)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "还没购票！");
		}
		if (seeOrderList.size() > 1 || !StringUtils.equals(tradeNo, seeOrderList.get(0).getTradeNo())) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "非首次购票！");
		}
		Map<String, Object> resMap = new HashMap<String, Object>();
		resMap.put("tradeNo", tradeNo);
		initField(model, request);
		model.put("resMap", resMap);
		model.put("root", "result");
		return getOpenApiXmlDetail(model);
	}
}
