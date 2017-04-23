package com.gewara.untrans.ticket;

import java.util.List;

import com.gewara.json.TempRoomSeat;
import com.gewara.model.acl.User;
import com.gewara.support.ErrorCode;
/**
 * 
 * @author john.zhou@gewara.com
 *
 */
public interface TempRoomSeatService {
	/**
	 * 匹配座位数据，格式: "1:1,1:2,2:1,2:2,15:14"
	 */
	public static final String BATCH_PEX = "^[0-9a-zA-Z]{1,3}:[0-9a-zA-Z]{1,3}(,[0-9a-zA-Z]{1,3}:[0-9a-zA-Z]{1,3})*$";
	public static final String SINGLE_PEX = "^[0-9a-zA-Z]{1,3}:[0-9a-zA-Z]{1,3}$";
	/**
	 * 通过影厅ID、模板名称增加锁定座位情况
	 * @param roomid 影厅ID
	 * @param tmpname 模板名称
	 * @param user 管理员信息
	 * @return 成功返回 模板数据对象，反之返回错误信息
	 */
	ErrorCode<TempRoomSeat> addRoomSeat(Long roomid, String tmpname, User user);
	/**
	 * 通过影厅ID、模板名称查询影厅锁定座位情况
	 * @param roomid 影厅ID
	 * @param tmpname 模板名称
	 * @return 影厅锁定座位情况
	 */
	TempRoomSeat getRoomSeat(Long roomid, String tmpname);
	
	/**
	 * 通过影厅ID、模板名称修改影厅锁定座位情况
	 * @param roomid 影厅ID
	 * @param tmpname 模板名称
	 * @param seatbody 座位数据,格式: "1:1 或12:13" 等字符串。:前数字代表座位行号，:后数字代表座位列号
	 * @param add 为true增加座位数据,flase为删除数据
	 * @param user 管理员信息
	 * @return 成功返回 "success"，反之返回错误信息
	 */
	ErrorCode<String> updateRoomSeat(Long roomid, String tmpname, String seatbody, boolean add, User user);
	
	/**
	 * 通过影厅ID、模板名称批量修改影厅锁定座位情况
	 * @param roomid 影厅ID
	 * @param tmpname 模板名称
	 * @param seatbody 座位数据,格式: "1:1,1:2,2:1,2:2,15:14" 等字符串。:前数字代表座位行号，:后数字代表座位列号
	 * @param add 为true增加座位数据,flase为删除数据
	 * @param user 管理员信息
	 * @return 成功返回 "success"，反之返回错误信息
	 */
	ErrorCode<String> batchUpdateRoomSeat(Long roomid, String tmpname, String seatbody, boolean add, User user);
	
	/**
	 * 通过影厅ID查询影厅模板数据
	 * @param roomid 影厅ID
	 * @return 影厅模板数据集合
	 */
	List<TempRoomSeat> getRoomSeatList(Long roomid);



}
