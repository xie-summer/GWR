package com.gewara.web.action.inner.mobile.diary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.content.SignName;
import com.gewara.helper.api.GewaApiBbsHelper;
import com.gewara.json.Weixin2Wala;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.movie.Movie;
import com.gewara.model.user.Member;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.untrans.CommonService;
import com.gewara.util.RelatedHelper;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
@Controller
public class OpenApiMobileDiaryController extends BaseOpenApiMobileController{
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	/**
	 * 获取影片长影评
	 * @param mid 影片id
	 * @param citycode 城市代码
	 * @param order 排序,可选值:poohnum,addtime
	 * @param pageNo 当前页码
	 * @param model 
	 * @param request
	 * @return
	 */
	@RequestMapping("/openapi/mobile/diary/movieDiaryList.xhtml")
	public String movieDiaryList(Long movieid, String citycode,
			@RequestParam(defaultValue="poohnum",required=false,value="order") String order, 
			@RequestParam(defaultValue="0",required=false,value="from")Integer from,
			@RequestParam(defaultValue="20",required=false,value="maxnum")Integer maxnum, 
			ModelMap model, HttpServletRequest request) {
		if(maxnum>100){
			maxnum=100;
		}
		List<Diary> diaryList = diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, "movie", movieid, from, maxnum, order);
		getMovieDiaryListMap(diaryList, model, request);
		putDiaryListNode(model);
		return getOpenApiXmlList(model);
	}
	
	//帖子详细
	@RequestMapping("/openapi/mobile/diary/diaryDetail.xhtml")
	public String movieDiaryDetail(Long diaryid, ModelMap model, HttpServletRequest request) {
		Diary diary = daoService.getObject(Diary.class, diaryid);
		if(diary==null){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "影评不存在！"); 
		}
		String content = blogService.getDiaryBody(diaryid);
		getMovieDiaryMap(diary, content, model, request);
		putDiaryNode(model);
		return getOpenApiXmlDetail(model);
	}
	
	/**
	 * 帖子增加顶的功能
	 * @param diaryIds 帖子id集合
	 * @param model
	 * @return
	 */
	@RequestMapping("/openapi/mobile/diary/addFlower.xhtml")
	public String getDiaryList(Long diaryid, String tag, ModelMap model){
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		String opkey = "adf" + member.getId() + diaryid;
		if(!operationService.updateOperationOneDay(opkey, true)) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "不能重复操作");
		Diary diary = daoService.getObject(Diary.class, diaryid);
		if(diary == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "数据不存在");
		if(StringUtils.equals(tag, "oppose")){ //反对人数
			diary.addPoohnum();
		}else {
			diary.addFlowernum();
		}
		daoService.saveObject(diary);
		return getSingleResultXmlView(model, diary.getFlowernum());
	}
	@RequestMapping("/openapi/mobile/diary/commendDiaryList.xhtml")
	public String sportList(String citycode,int from, int maxnum, ModelMap model, HttpServletRequest request){
		if(maxnum > 15) maxnum = 15;
		ApiAuth auth = OpenApiMobileAuthenticationFilter.getOpenApiAuth();
		if(StringUtils.isBlank(citycode)){
			return getErrorXmlView(model, auth.getCode(),"citycode is not null");
		}
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return recommentMovieDiary(rh, citycode, from, maxnum, model, request);
	}
	private String recommentMovieDiary(RelatedHelper rh ,String citycode, int from,int maxnum,ModelMap model, HttpServletRequest request){
		List<GewaCommend> gcSportitemlist = commonService.getGewaCommendList(citycode, SignName.INDEX_MOVIEDIARY, null,null, true,from,maxnum);
		commonService.initGewaCommendList("gcMovieDiaryList", rh, gcSportitemlist);
		List<Object> diarys = new LinkedList<Object>();
		Map<Long,Movie> moiveMap=new HashMap<Long, Movie>();
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		if(gcSportitemlist != null){
			for(GewaCommend gCommend:gcSportitemlist){
				DiaryBase diary=(DiaryBase) rh.getR1("gcMovieDiaryList",gCommend.getId());
				if(null!=diary && StringUtils.equals(diary.getCategory(), "movie")){
					Movie movie=daoService.getObject(Movie.class, diary.getCategoryid());
					moiveMap.put(diary.getId(), movie);
					diarys.add(diary);
					Map<String, Object> params = GewaApiBbsHelper.getMovieDiary(diary, null, null);
					params.put("moviename", movie.getMoviename());
					resMapList.add(params);
				}
			}
		}
		initField(model, request);
		putDiaryListNode(model);
		model.put("resMapList", resMapList);
		return getOpenApiXmlList(model);
	}
	
	//微信
	@RequestMapping("/openapi/mobile/weixin/weixin2Wala.xhtml")
	public String weixin2Wala(String id, ModelMap model, HttpServletRequest request) {
		Weixin2Wala wala = mongoService.getObject(Weixin2Wala.class, "id", id);
		Map<String, Object> resMap = getWeixin2WalaMap(wala);
		return getOpenApiXmlDetail(resMap, "weixin2Wala", model, request);
	}
}
