package com.gewara.web.action.drama;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.UnmodifiableMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.SearchOdiCommand;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.DramaConstant;
import com.gewara.constant.content.SignName;
import com.gewara.helper.DramaHelper;
import com.gewara.json.PageView;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.WebUtils;
import com.gewara.web.util.PageUtil;

@Controller
public class SearchOdiController extends BaseDramaController {

	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	private static final Map<Integer, List<Integer>> priceMap;
	static{
		Map<Integer, List<Integer>> tmp = new HashMap<Integer, List<Integer>>();
		tmp.put(1, Arrays.asList(100, null));
		tmp.put(2, Arrays.asList(101,200));
		tmp.put(3, Arrays.asList(201,300));
		tmp.put(4, Arrays.asList(301,500));
		tmp.put(5, Arrays.asList(501,800));
		tmp.put(6, Arrays.asList(801,1000));
		tmp.put(7, Arrays.asList(null,1001));
		priceMap = UnmodifiableMap.decorate(tmp);
	}
	@RequestMapping("/drama/searchOdi.xhtml")
	public String searchOdi(Integer pageNo, SearchOdiCommand command, HttpServletRequest request, HttpServletResponse response, ModelMap model){
		if(StringUtils.isBlank(command.citycode) 
			|| StringUtils.isBlank(AdminCityContant.getCityNameByCode(command.citycode))){
			command.citycode = WebUtils.getAndSetDefault(request, response);
		}
		if(StringUtils.isBlank(DramaConstant.getDramaTypeText(command.dramatype))){
			command.dramatype = "";
		}
		Date curDate = DateUtil.getCurDate();
		Date curMonth = DateUtil.getMonthFirstDay(curDate);
		if(!DateUtil.isValidDate(command.playdate)){
			command.playdate = "";
		}else{
			Date date = DateUtil.parseDate(command.playdate);
			if(curMonth.after(date)){
				command.playdate = "";
			}
		}
		if(pageNo == null){
			pageNo = 0;
		}
		PageParams pageParams = new PageParams();
		if (pageCacheService.isUseCache(request)) {// 先使用缓存
			pageParams.addInteger("pageNo", pageNo);
			pageParams.addSingleString("citycode", command.citycode);
			pageParams.addSingleString("dramatype", command.dramatype);
			pageParams.addSingleString("dramaname", command.dramaname);
			pageParams.addDateStr("playdate", command.getPlaydate());
			pageParams.addLong("starid", command.starid);
			pageParams.addInteger("price", command.getPrice());
			pageParams.addSingleString("order", command.getOrder());
			PageView pageView = pageCacheService.getPageView(request, "drama/searchOdi.xhtml", pageParams, command.citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		int rowsPerPage = 5;
		int firstPer = pageNo * rowsPerPage;
		List<String> citycodeList = openDramaService.getCitycodeList();
		model.put("citycodeList", citycodeList);
		List<Long> dramaidList = openDramaService.getCurDramaidList(command.citycode);
		List<Drama> dramaList = new ArrayList<Drama>();
		List<Drama> bookingDramaList = daoService.getObjectList(Drama.class, dramaidList);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		List<GewaCommend> dramaStarList = commonService.getGewaCommendList(null, SignName.DRAMA_SEARCH_DRAMASTAR, null, null, true, 0, -1);
		commonService.initGewaCommendList("dramaStarList", rh, dramaStarList);
		model.put("dramaStarList", dramaStarList);
		model.put("dramaTypeMap", DramaConstant.dramaTypeMap);
		Map<String,List<Drama>> dramaListMap = BeanUtil.groupBeanList(bookingDramaList, "dramatype", DramaConstant.TYPE_OTHER);
		model.put("dramaListMap", dramaListMap);
		//名称搜索
		if(StringUtils.isNotBlank(command.dramaname)){
			command.citycode = "";
			List<Drama> dramaNameList = dramaPlayItemService.getDramaListByName(command.dramaname);
			dramaList.addAll(CollectionUtils.intersection(bookingDramaList, dramaNameList));
		}else{
			List<Drama> tmpList = new ArrayList<Drama>();
			//筛选类型
			if(StringUtils.isNotBlank(command.dramatype)){
				List<Drama> dramatypeList = dramaListMap.get(command.dramatype);
				if(!CollectionUtils.isEmpty(dramatypeList)){
					tmpList.addAll(dramatypeList);
				}
			}else{
				tmpList = bookingDramaList;
			}
			//筛选日期
			Date date = null;
			if(StringUtils.isNotBlank(command.playdate)){
				date = DateUtil.parseDate(command.playdate);
				//tmpList = DramaHelper.dateFilter(tmpList, date);
			}
			/*//筛选明星
			if(command.starid != null){
				tmpList = DramaHelper.dramaStarFilter(tmpList, command.starid);
			}*/
			//价格筛选
			Integer minprice = null, maxprice = null;
			if(command.getPrice() != null){
				List<Integer> priceList = priceMap.get(command.getPrice());
				if(!CollectionUtils.isEmpty(priceList) && priceList.size() == 2){
					minprice = priceList.get(0);
					maxprice = priceList.get(1);
				}
			}
			tmpList = DramaHelper.dramaListFilter(tmpList, date, command.starid , minprice, maxprice);
			dramaList.addAll(tmpList);
		}
		int count = dramaList.size();
		Collections.sort(dramaList, new MultiPropertyComparator<Drama>(new String[]{command.getOrder()}, new boolean[]{command.gainAsc()}));
		Map<Long, List<Integer>> dramaPriceMap = new HashMap<Long, List<Integer>>();
		Map<Long, List<DramaStar>> dramaDirectorListMap= new HashMap<Long, List<DramaStar>>();
		for (Drama drama : dramaList) {
			List<Integer> priceList = BeanUtil.getIntgerList(drama.getPrices(), ",");
			Collections.sort(priceList);
			dramaPriceMap.put(drama.getId(), priceList);
			if(StringUtils.isNotBlank(drama.getDirector())){
				List<Long> directorIdList = BeanUtil.getIdList(drama.getDirector(), ",");
				List<DramaStar> directorsList = daoService.getObjectList(DramaStar.class, directorIdList);
				dramaDirectorListMap.put(drama.getId(), directorsList);
			}
		}
		model.put("dramaDirectorListMap", dramaDirectorListMap);
		model.put("dramaPriceMap", dramaPriceMap);
		dramaList = BeanUtil.getSubList(dramaList, firstPer, rowsPerPage);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "drama/searchOdi.xhtml", true, true);
		Map params  = BeanUtil.getSimpleStringMap(command);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("dramaList", dramaList);
		model.put("count", count);
		model.put("command", command);
		return "drama/odiList.vm";
	}
}
