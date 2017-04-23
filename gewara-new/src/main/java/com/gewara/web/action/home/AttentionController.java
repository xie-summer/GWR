package com.gewara.web.action.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.common.BaseEntity;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.Treasure;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.movie.MCPService;
import com.gewara.untrans.WalaApiService;
import com.gewara.web.action.BaseHomeController;
import com.gewara.web.util.PageUtil;

@Controller
public class AttentionController extends BaseHomeController {
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@Autowired@Qualifier("walaApiService")
	private WalaApiService walaApiService;
	@RequestMapping("/home/attentioninfo.xhtml")
	public String getAttentionInfo(ModelMap model, Long memberid){
		Member mymember = getLogonMember();
		if(memberid==null){//自己
			memberid = mymember.getId();
		}
		model.putAll(controllerService.getCommonData(model, mymember, memberid));
		return "home/acct/interest.vm";
	}
	//新版感兴趣入口
	@RequestMapping("/home/newAttentioninfo.xhtml")
	public String getNewAttentionInfo(ModelMap model, Long memberid){
		Member logonMember = getLogonMember();
		Member member = null;
		if(memberid==null){//自己
			member = logonMember;
		}else {
			member = daoService.getObject(Member.class, memberid);
		}
		model.putAll(controllerService.getCommonData(model, logonMember, memberid));
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		this.getHomeLeftNavigate(memberInfo, model);
		model.put("memberInfo", memberInfo);
		return "sns/userInterest/index.vm";
	}
	//新版感兴趣
	@RequestMapping("/home/newInterestInfo.xhtml")
	public String newInterestInfo(ModelMap model, Long memberid, Integer pageNo, String type, String action){
		Member logonMember = getLogonMember();
		Member member = null;
		if(memberid==null){//自己
			member = logonMember;
		}else {
			member = daoService.getObject(Member.class, memberid);
		}
		if (pageNo == null) pageNo = 0;
		int rowsPerPage = 10;
		int first = rowsPerPage * pageNo;
		int treasureCount=0;
		List<Treasure> treasureList=new ArrayList<Treasure>();
		Map<Long, List<Movie>> movieListMap = new HashMap<Long, List<Movie>>();
		String[] atag=new String[20];
		String[] actionArray = new String[]{"collect","xiangqu","quguo","palyed","together"};
		if(action == null)	action="";
		if(action.equals("all") && action != null){
			treasureCount=blogService.getTreasureCountByMemberId(member.getId(), new String[]{}, new String[]{"member"}, actionArray);
			treasureList=blogService.getTreasureListByMemberId(member.getId(), new String[]{}, new String[]{"member"}, null, first, rowsPerPage, actionArray);
		}else{
			if(type.equals("hd")){
				atag=new String[]{"activity"};
			}else if(type.equals("dy")){
				atag=new String[]{"movie"};
			}else if(type.equals("hj")){
				atag=new String[]{"drama"};
			}else if(type.equals("xm")){
				atag=new String[]{"gymcourse","sportservice"};
			}else{
				atag=new String[]{"cinema", "ktv", "bar", "gym", "sport", "theatre"};
			}
			treasureCount=blogService.getTreasureCountByMemberId(member.getId(), atag, new String[]{}, actionArray);
			treasureList=blogService.getTreasureListByMemberId(member.getId(), atag, new String[]{}, null, first, rowsPerPage, actionArray);
		}
		
		List<MoviePlayItem> playItemList = null;
		for (Treasure treasure : treasureList) {
			if(treasure.getTag().equals("cinema")){
				List<Date> playdateList = mcpService.getCurCinemaPlayDate(treasure.getRelatedid());
				if(!playdateList.isEmpty()){
					Date fyrq = playdateList.get(0);
					playItemList = mcpService.getCinemaMpiList(treasure.getRelatedid(), fyrq);
					Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
					Map<Long, List<MoviePlayItem>> mpiMap = new HashMap<Long, List<MoviePlayItem>>();
					for (MoviePlayItem mpi : playItemList) {
						List<MoviePlayItem> mpiList = mpiMap.get(mpi.getMovieid());
						if (mpiList == null) {
							mpiList = new ArrayList<MoviePlayItem>();
							mpiMap.put(mpi.getMovieid(), mpiList);
						}
						OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
						if (opi != null) {
							if (!opi.isClosed()) {
								opiMap.put(mpi.getId(), opi);
								mpiList.add(mpi);
							}
						} else
							mpiList.add(mpi);
					}
					List<Movie> movieList = new ArrayList<Movie>();
					Iterator iter = mpiMap.keySet().iterator();
					while (iter.hasNext()) {
						Long id = (Long) iter.next();
						Movie movie = daoService.getObject(Movie.class, id);
						if (!mpiMap.get(id).isEmpty()){
							movieList.add(movie);
						}
					}
					movieListMap.put(treasure.getRelatedid(), movieList);
				}
			}
		}
		
		model.put("movieListMap", movieListMap);
		Map<Long, Object> treasureMap = new HashMap<Long, Object>();
		for (Treasure treasure : treasureList) {
			Object obj = null;
			if(!StringUtils.equals(treasure.getTag(), "member")){
				obj = relateService.getRelatedObject(treasure.getTag(), treasure.getRelatedid());
			}
			if(obj!=null){
				treasureMap.put(treasure.getId(), obj);
			}
		}
		PageUtil pageUtil = new PageUtil(treasureCount, rowsPerPage, pageNo, "/home/newInterestInfo.xhtml", true, true);
		Map params = new HashMap();
		params.put("action", action);
		params.put("type", type);
		params.put("memberid", member.getId());
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("treasureList", treasureList);
		model.put("treasureMap", treasureMap);
		model.put("logonMember", logonMember);
		model.put("member", member);
		return "sns/userInterest/interest.vm";
	}
	@RequestMapping("/home/interestInfo.xhtml")
	public String interestInfo(ModelMap model, Long memberid, Integer pageNo, String action, String type){
		String[] actionArray=new String[]{};
		if("all".equals(action)) actionArray = new String[]{"collect","xiangqu","quguo","palyed","together"};
		else if(StringUtils.equals(action, "xingqu")){//我感兴趣
			actionArray=new String[]{"collect"};
		} else actionArray=new String[]{action};
		Member mymember = getLogonMember();
		if(memberid==null){//自己
			memberid = mymember.getId();
		}
		if (pageNo == null) pageNo = 0;
		int rowsPerPage = 10;
		int first = rowsPerPage * pageNo;
		int treasureCount=0;
		List<Treasure> treasureList=new ArrayList<Treasure>();
		String[] atag=new String[20];
		model.putAll(controllerService.getCommonData(model, mymember, memberid));
		if(!"all".equals(action)){
			if(StringUtils.isBlank(type)){
				if(Treasure.ACTION_XIANGQU.equals(action) || Treasure.ACTION_QUGUO.equals(action)){
					atag=new String[]{"cinema", "ktv", "bar", "gym", "sport", "theatre"};
					treasureCount=blogService.getTreasureCountByMemberId(memberid, atag, new String[]{"movie"}, actionArray);
					treasureList=blogService.getTreasureListByMemberId(memberid, atag, new String[]{"movie"}, null, first, rowsPerPage, actionArray);
				}else if(Treasure.ACTION_PLAYED.equals(action) || Treasure.ACTION_TOGETHER.equals(action)){
					atag= new String[]{"sportservice", "gymcourse"};
					treasureCount=blogService.getTreasureCountByMemberId(memberid,atag, new String[]{}, actionArray);
					treasureList=blogService.getTreasureListByMemberId(memberid, atag, new String[]{}, null, first, rowsPerPage, actionArray);
				}
			}else {//(我感兴趣) 电影、话剧
				atag=new String[]{"movie","drama"};
				treasureCount=blogService.getTreasureCountByMemberId(memberid, atag, new String[]{}, actionArray);
				treasureList=blogService.getTreasureListByMemberId(memberid, atag, new String[]{}, null, first, rowsPerPage, actionArray);
			}
		}else {//全部
			treasureCount=blogService.getTreasureCountByMemberId(memberid, new String[]{}, new String[]{}, actionArray);
			treasureList=blogService.getTreasureListByMemberId(memberid, new String[]{}, new String[]{}, null, first, rowsPerPage, actionArray);
		}
		
		Map<Long, Object> treasureMap = new HashMap<Long, Object>();
		for (Treasure treasure : treasureList) {
			Object obj = null;
			if(StringUtils.equals(treasure.getTag(), "member")){
				obj = daoService.getObject(MemberInfo.class, treasure.getRelatedid());
			}else{
				obj = relateService.getRelatedObject(treasure.getTag(), treasure.getRelatedid());
			}
			if(obj!=null){
				treasureMap.put(treasure.getId(), obj);
			}
		}
		PageUtil pageUtil = new PageUtil(treasureCount, rowsPerPage, pageNo, "/home/interestInfo.xhtml", true, true);
		Map params = new HashMap();
		params.put("action", action);
		params.put("type", type);
		params.put("memberid", memberid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		//model.put("member", mymember);
		model.put("action", action);
		model.put("type", type);
		model.put("treasureMap", treasureMap);
		model.put("treasureList", treasureList);	
		return "home/acct/interestData.vm";
	}
	@RequestMapping("/home/delInterestInfo.xhtml")
	public String delInterest(ModelMap model, Long interestid){
		Member member = getLogonMember();
		Treasure treasure=daoService.getObject(Treasure.class, interestid);
		if(treasure==null) return showJsonError(model, "参数出错");
		if(!member.getId().equals(treasure.getMemberid()))  return showJsonError(model, "你没权限删除");
		walaApiService.delTreasure(treasure.getMemberid(), treasure.getRelatedid(), treasure.getTag(), treasure.getAction());
		daoService.removeObject(treasure);
		Object obj = relateService.getRelatedObject(treasure.getTag(),  treasure.getRelatedid());
		if(obj instanceof BaseEntity){
			BaseEntity relate = (BaseEntity) obj;
			int collectedtimes = 0;
			if(relate.getCollectedtimes() != null && relate.getCollectedtimes() > 0){
				collectedtimes = relate.getCollectedtimes() - 1;
			}
			relate.setCollectedtimes(collectedtimes);
			daoService.updateObject(relate);
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/home/modifyInterestLabel.xhtml")
	public String modifyInterestLabel(ModelMap model, Long treasureid){
		Member member=getLogonMember();
		Treasure treasure=daoService.getObject(Treasure.class, treasureid);
		if(treasure==null) return showJsonError(model, "参数出错！");
		List<Treasure> myTreasureList=blogService.getTreasureListByMemberId(member.getId(), 0,5);
		Map jsonMap=new HashMap();
		List<String> aList = new ArrayList<String>();
		for(Treasure treasure2:myTreasureList){
			if(StringUtils.isNotBlank(treasure2.getActionlabel()))
				aList.add(treasure2.getActionlabel());
		}
		jsonMap.put("aList", aList);
		jsonMap.put("aLabel", treasure.getActionlabel());
		return showJsonSuccess(model, jsonMap);
	}
	@RequestMapping("/home/saveInterestLabel.xhtml")
	public String saveInterestLabel(ModelMap model, Long treasureid, String actionlabel){
		Treasure treasure=daoService.getObject(Treasure.class, treasureid);
		if(treasure==null) return showJsonError(model, "参数出错！");
		treasure.setActionlabel(actionlabel);
		daoService.saveObject(treasure);
		return showJsonSuccess(model, treasure.getActionlabel());
	}
}
