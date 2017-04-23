package com.gewara.untrans;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gewara.json.MemberSign;
import com.gewara.json.MobileUpGrade;
import com.gewara.json.MovieMpiRemark;
import com.gewara.json.PlayItemMessage;
import com.gewara.json.ViewFilmSchedule;
import com.gewara.json.WDOrderContrast;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.pay.TicketOrder;
import com.gewara.xmlbind.ticket.WdOrder;

/**
 * no sql相关业务方法封装
 * @author gebiao(ge.biao@gewara.com)
 * @since Dec 28, 2012 3:36:46 PM
 */
public interface NosqlService {
	/**
	 * 用户签到，经纬度
	 * @param id
	 * @param x  
	 * @param y
	 */
	void memberSign(Long memberid, Double pointx, Double pointy);
	void memberSignBaiDu(Long memberid, Double bpointx, Double bpointy);
	void memberSign(Long memberid, Double pointx, Double pointy, String address);
	/**
	 * 按经纬度最大最小值查询用户列表
	 * @param pointx
	 * @param pointy
	 * @param r
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<MemberSign> getMemberSignListByPointR(double pointx, double pointy, long r, int from, int maxnum);
	MemberSign getMemberSign(Long memberid);
	MobileUpGrade getLastMobileUpGrade(String tag, String apptype, String appsource);
	MobileUpGrade getLastMobileUpGradeById(String appid);
	void saveSurveyList(List<Map> mapList);
	List<Map> getSurveyByMemberid(Long memberid);
	PlayItemMessage getPlayItemMessage(Long memberid, String tag, Long relatedid, Date playdate, Long categoryid);
	List<PlayItemMessage> getSendPlayItemMessageList(String tag, String status, String type, int from, int maxnum);
	/**
	 * 获取提醒短信
	 * @param memberid
	 * @param categoryid
	 * @return
	 */
	PlayItemMessage addPlayItemMessage(Long memberid, String tag, Long relatedid, Date playdate, Long categoryid, String mobile, String type, String msg);
	PlayItemMessage addPlayItemMessage(Long memberid, String tag, Long relatedid, Date playdate, Long categoryid, String mobile, String flag, String type, String msg);
	/**
	 * 电影场次描述
	 * @param movieid
	 * @param citycode
	 * @param maxnum
	 * @return
	 */
	List<MovieMpiRemark> getMovieMpiRemarkList(Long movieid, String citycode, int maxnum);
	
	/**
	 * 获取自动设置器通用假日等时间限制
	 * @return
	 */
	Map<String,String> getAutoSetterLimit();
	/**
	 * 获取影厅走廊，过道、墙体位置
	 * @param roomId
	 * @return
	 */
	Map<String,String> getOuterRingSeatByRoomId(Long roomId);
	
	/**
	 * 格瓦拉每日购票排行
	 * @return
	 */
	List<Map> getBuyTicketRanking();
	/**
	 * 16届电影节 添加片单或日程
	 * @param mpi 排片
	 * @param tag
	 * @param movieId 电影id
	 * @return
	 */
	ViewFilmSchedule addViewFilmSchedule(MoviePlayItem mpi,String tag,Long movieId,long memberId,String source);
	
	/**
	 * 保存每天万达同步的订单和格瓦拉订单匹配不上的订单
	 * @param gewaOrderList
	 * @param wdOrderList
	 */
	List<WDOrderContrast> saveWDOrderContrast(List<TicketOrder> gewaOrderList,List<WdOrder> wdOrderList,Date addDate);
}
