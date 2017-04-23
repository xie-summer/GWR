package com.gewara.service;

import java.util.List;

import com.gewara.model.machine.Machine;

public interface MachineService {
	
	/**
	 * 查询gewa机器列表
	 * @return
	 */
	List<Machine> getGewaMachineList(String citycode, String machinenumber, String machinename, Long cinemaid, String linkmethod, 
			String machineowner, Integer ticketcount, String machinetype, String machinestatus, int from, int maxnum);
	/**
	 * 查询数量
	 * @param machinenumber
	 * @param machinename
	 * @param cinemaid
	 * @param linkmethod
	 * @param touchtype
	 * @param ticketcount
	 * @param machinetype
	 * @param machinestatus
	 * @return
	 */
	Integer gewaMachineCount(String cicycode, String machinenumber, String machinename, Long cinemaid, String linkmethod, 
			String machineowner, Integer ticketcount, String machinetype, String machinestatus);
	/**
	 * 查询每种机器的最大编号
	 */
	Integer getMaxMachineNumber(String machinename,String machineprefix);
}
