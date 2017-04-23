package com.gewara.service.ticket;

import java.sql.Timestamp;
import java.util.List;

import com.gewara.json.TempRoomSeat;
import com.gewara.model.acl.User;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.ticket.AutoSetter;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.ticket.MpiSeat;

public interface OpiManageService {
	/**
	 * 清除类型中的座位
	 * @param mpid
	 * @param seatid
	 */
	ErrorCode removePriceSeat(Long mpid, Long seatid);
	/**
	 * 将座位加入到某种类型中
	 * @param mpid
	 * @param seattype
	 * @param seatid
	 */
	ErrorCode<String> addPriceSeat(Long mpid, String seattype, Long seatid);
	/**
	 * 开放第一步：设置场次信息
	 * @param mpid
	 * @param opentype 开放类型：gewa或火凤凰
	 * @return
	 */
	ErrorCode<OpenPlayItem> openMpi(MoviePlayItem mpi, Long userid, String opentype, TempRoomSeat tempRoomSeat);
	/**
	 * 开放东票场次
	 * @param mpi
	 * @param userid
	 * @param mpiSeatList
	 * @return
	 */
	ErrorCode<OpenPlayItem> openPnxMpi(MoviePlayItem mpi, Long userid, List<MpiSeat> mpiSeatList);
	/**
	 * 开放万达场次，无需插入座位
	 * @param mpi
	 * @param userid
	 * @return
	 */
	ErrorCode<OpenPlayItem> openWdMpi(MoviePlayItem mpi, Long userid);
	
	/**
	 * 重新刷新座位信息
	 * @param opi
	 * @param userid
	 * @param mpiSeatList
	 * @param refresh
	 * @return
	 */
	ErrorCode<OpenPlayItem> refreshOpiSeat(Long mpid, Long userid, List<MpiSeat> mpiSeatList);
	
	/**
	 * 改变状态
	 * @param mpid
	 * @param status
	 * @param userid 
	 * @return
	 */
	ErrorCode<OpenPlayItem> changeMpiStatus(Long mpid, String status, Long userid);
	/**
	 * 更改统一售价
	 * @param mpid
	 * @param gewaprice
	 * @return
	 */
	ErrorCode<OpenPlayItem> updateGewaPrice(Long mpid, Integer gewaprice, User user);
	/**
	 * 更改成本价
	 * @param mpid
	 * @param costprice
	 * @return
	 */
	ErrorCode updateCostPrice(Long mpid, Integer costprice, User user);
	ErrorCode<OpenPlayItem> updateOpenPlayItem(Long opid);
	/**
	 * 修改可用卡
	 * @param mpid
	 * @param cardtype
	 * @return
	 */
	ErrorCode<String> updateElecard(Long mpid, String cardtype, User user);
	/**
	 * 批量锁定座位
	 * @param mpid
	 * @param locktype
	 * @param lockReason
	 * @param lockline
	 * @param lockrank
	 * @return
	 */
	ErrorCode batchLockSeat(Long mpid, String locktype, String lockReason, String lockline, String lockrank);
	/**
	 * 批量解锁座位
	 * @param mpid
	 * @param lockline
	 * @param lockrank
	 * @return
	 */
	ErrorCode batchUnLockSeat(Long mpid, String lockline, String lockrank);
	/**
	 * 手工释放座位
	 * @param mpid
	 * @param seatId
	 * @return
	 */
	ErrorCode<OpenSeat> releaseSeat(Long mpid, Long seatId);
	/**
	 * 废弃某场次：必须是无订单的场次
	 * @param mpid
	 * @return
	 */
	ErrorCode<OpenPlayItem> discardOpiByMpid(Long mpid, Long userid);
	/**
	 * 设置座位初始状态
	 * @param seatid
	 * @param initstatus
	 * @return
	 */
	ErrorCode updateSeatInitStatus(Long seatid, String initstatus);
	/**
	 * 更新座位图实时卖出数量统计
	 * @param opid
	 * @return
	 */
	void updateOpiStats(Long opid, List<String> hfhLockList, boolean isFinished);
	
	/**
	 * 设置情侣座
	 * @param seatid
	 * @param loveInd
	 * @return
	 */
	ErrorCode<Long> updateSeatLoveInd(Long seatid, String loveInd);
	ErrorCode<String> batchAddPriceSeat(Long mpid, String seattype, String rows, String ranks);
	ErrorCode<String> batchRemovePriceSeat(Long mpid, String seattype, String rows, String ranks);
	/**
	 * 第一次开放场次，记录版、或语言、价格原始信息，便于后期同步排片做对比
	 * @param mpi
	 */
	void updateOriginInfo(MoviePlayItem mpi);
	/**
	 * 自动开放场次
	 * @param mpid
	 * @param setter
	 * @param tempRoomSeat
	 * @param userid
	 * @return
	 */
	ErrorCode<OpenPlayItem> autoOpenMpi(MoviePlayItem mpi, AutoSetter setter, TempRoomSeat tempRoomSeat, Long userid);
	/**
	 * 从废弃场次中重建MoviePlayItem
	 * 同步数据时，MoviePlayItem删除（影院删除排片），则OpenPlayItem废弃，后来影院又恢复排片，则
	 * @param opi
	 * @return
	 */
	boolean restoreMpiFromHisData(String seqNo) ;
	void saveMpiToHisData(MoviePlayItem mpi);
	/**
	 * 批量插入座位
	 * @param opi
	 * @param tempRoomSeat
	 * @return
	 */
	ErrorCode batchInsertOpenSeat(OpenPlayItem opi, TempRoomSeat tempRoomSeat);
	/**
	 * 检验锁定卖出座位（超时付款的座位锁定后又支付卖出，统计数量问题）
	 * @return
	 */
	int verifyOpiSeatLock(Long mpid);
	void updateLocknum(Long mpid, int total);
	/**
	 * @param mpid
	 * @param fee
	 * @param userid 
	 * @return
	 */
	ErrorCode<OpenPlayItem> updateOpiFee(Long mpid, Integer fee, Long userid);
	ErrorCode<OpenPlayItem> changePartnerStatus(Long mpid, String status);
	ErrorCode<OpenPlayItem> changeOpentime(Long mpid, Timestamp opentime, Long userid);
	ErrorCode<OpenPlayItem> changeClosetime(Long mpid, Timestamp closetime, Long userid);
	/**
	 * 更改积分限制
	 * @param opid
	 * @param minpoint
	 * @param maxpoint
	 * @param id
	 * @return
	 */
	ErrorCode<OpenPlayItem> updatePointLimit(Long opid, Integer minpoint, Integer maxpoint, Long userid);
	ErrorCode<OpenPlayItem> updateGivepoint(Long opid, Integer point, Long userid);
	ErrorCode<OpenPlayItem> updateSpflag(Long opid, String spflag, Long userid);
	ErrorCode<OpenPlayItem> updateOpiRemark(Long opid, String remark, Long userid);
	void updateOpiStatsByMtx(Long opid, int sellNum, boolean isFinished);
}
