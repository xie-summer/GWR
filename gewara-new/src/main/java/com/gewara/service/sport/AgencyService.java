package com.gewara.service.sport;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.gewara.model.acl.User;
import com.gewara.model.agency.Agency;
import com.gewara.model.agency.AgencyToVenue;
import com.gewara.model.agency.Curriculum;
import com.gewara.model.agency.TrainingGoods;
import com.gewara.model.sport.SportItem;
import com.gewara.support.ErrorCode;


public interface AgencyService {
	
	List<Agency> getAgencyList(String name, String citycode, String orderField, boolean asc, int from, int maxnum);
	int getAgencyCount(String name, String citycode);
	/**
	 * 根据城市编码查询培训，根据goodssort顺序排序
	 * @param citycode		城市编码
	 * @param tag				培训机构类型
	 * @param relatedid		培训机构ID	
	 * @param category		培训项目类型
	 * @param categoryid		项日ID
	 * @param isTovaltime	是否有效
	 * @param isGtZero		是否大于0
	 * @param from			查询行
	 * @param maxnum		最大数据
	 * @return
	 */
	List<TrainingGoods> getTrainingGoodsList(String citycode, String tag, Long relatedid, String category, Long categoryid, Long placeid, String order, boolean asc, boolean isTovaltime, int from, int maxnum);
	int getTrainingGoodsCount(String citycode, String tag, Long relatedid, String category, Long categoryid, Long placeid, boolean isTovaltime);
	List<TrainingGoods> getTrainingGoodsList(String citycode, Long relatedid, Long categoryid, String fitcrowd, String timetype, List<Long> sportIdList,
			Integer fromprice, Integer toprice, String searchKey, String order, boolean asc, int from, int maxnum);
	int getTrainingGoodsCount(String citycode, Long relatedid, Long categoryid, String fitcrowd, String timetype, List<Long> sportIdList,
			Integer fromprice, Integer toprice, String searchKey);
	ErrorCode<TrainingGoods> saveTrainingGoods(Long gid, String citycode, String goodsname, String tag, Long relatedid, String category, 
			Long categoryid, Long placeid, Timestamp fromvalidtime, Timestamp tovalidtime, String summary, String description, String fitcrowd,
			String timetype, String seotitle, String seodescription, Integer quantity, String showtime, Integer minquantity, User user);
	//课程表
	List<Curriculum> getCurriculumList(Long relatedid, Date playDate);
	//得到培训机构的培训项目
	List<SportItem> getAgencySportItemList(Long agencyId, String citycode);
	//常驻场馆
	List<AgencyToVenue> getATVList(Long agencyId, Long venueId); 
	void clearTrainingGoodsPreferential(TrainingGoods goods);
}
