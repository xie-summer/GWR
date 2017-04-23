package com.gewara.service.ticket;

import java.util.List;

import com.gewara.helper.ticket.UpdateMpiContainer;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.ticket.SynchPlayItem;

/**
 * 与Ticket项目做数据同步
 * @author gebiao(ge.biao@gewara.com)
 * @since Apr 15, 2013 4:27:41 PM
 */
public interface TicketSynchService {
	/**
	 * @param synchPlayItem
	 * @param mpi
	 * @param cinema
	 * @param room
	 * @param msgList
	 */
	void updateSynchPlayItem(UpdateMpiContainer container, SynchPlayItem synchPlayItem, MoviePlayItem mpi, Cinema cinema, CinemaRoom room, List<String> msgList);
	
	/**
	 * 更新影院影厅信息
	 * @param cinemaid		影院ID编号
	 * @param userid		管理员ID
	 * @return
	 */
	ErrorCode updateCinemaRoom(Long cinemaid, Long userid);
	
	/**
	 * 更新影厅座位信息
	 * @param room			影厅信息
	 * @param forceUpdate	是否强制更新
	 * @return
	 */
	List<String> updateRoomSeatList(CinemaRoom room, boolean forceUpdate);
	void updateOpenPlayItem(List<String> msgList);
	void updateOpenPlayItem(Long cinemaid, List<String> msgList);

	void updateSpiderPlayItem(SynchPlayItem synchPlayItem, MoviePlayItem mpi,
			Cinema cinema, List<String> msgList);
}
