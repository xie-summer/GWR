package com.gewara.untrans.ticket;

import java.util.List;

import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.ticket.AutoSetter;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.support.ErrorCode;
import com.gewara.xmlbind.ticket.MpiSeat;

public interface MpiOpenService extends BaseOpenService {
	ErrorCode asynchOpenMpi(MoviePlayItem mpi, Long userid, String opentype, String tempid);
	/**
	 * @param mpi
	 * @param userid
	 * @param setter
	 */
	ErrorCode asynchAutoOpenMpi(MoviePlayItem mpi, Long userid, AutoSetter setter);
	
	void asynchAutoOpenMpiList(List<MoviePlayItem> mpiList);
	void asynchAutoOpenMpiList(List<MoviePlayItem> mpiList, AutoSetter setter);
	
	void refreshMpiRelatePage(List<OpenPlayItem> opiList);
	void refreshMpiRelatePage(OpenPlayItem opi);

	ErrorCode<List<MpiSeat>> refreshOpiSeat(Long mpid, Long userid, boolean refresh, List<String> msgList);
}
