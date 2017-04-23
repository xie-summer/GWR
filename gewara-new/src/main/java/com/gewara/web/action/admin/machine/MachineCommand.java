package com.gewara.web.action.admin.machine;

public class MachineCommand {

		public String machinenumber;
		public String machinename;
		public Long cinemaid;
		public String linkmethod;
		public String machineowner;
		public Integer ticketcount;
		public String machinetype;
		public String machinestatus;
		public String citycode;
		public int pageNo=0;
		public int rowsPerpage=50;
		public String getMachinenumber() {
			return machinenumber;
		}
		public void setMachinenumber(String machinenumber) {
			this.machinenumber = machinenumber;
		}
		public String getMachinename() {
			return machinename;
		}
		public void setMachinename(String machinename) {
			this.machinename = machinename;
		}
		public Long getCinemaid() {
			return cinemaid;
		}
		public void setCinemaid(Long cinemaid) {
			this.cinemaid = cinemaid;
		}
		public String getLinkmethod() {
			return linkmethod;
		}
		public void setLinkmethod(String linkmethod) {
			this.linkmethod = linkmethod;
		}
		public String getMachineowner() {
			return machineowner;
		}
		public void setMachineowner(String machineowner) {
			this.machineowner = machineowner;
		}
		public Integer getTicketcount() {
			return ticketcount;
		}
		public void setTicketcount(Integer ticketcount) {
			this.ticketcount = ticketcount;
		}
		public String getMachinetype() {
			return machinetype;
		}
		public void setMachinetype(String machinetype) {
			this.machinetype = machinetype;
		}
		public String getMachinestatus() {
			return machinestatus;
		}
		public void setMachinestatus(String machinestatus) {
			this.machinestatus = machinestatus;
		}
		
		public String getCitycode() {
			return citycode;
		}
		public void setCitycode(String citycode) {
			this.citycode = citycode;
		}
		public int getPageNo() {
			return pageNo;
		}
		public void setPageNo(int pageNo) {
			this.pageNo = pageNo;
		}
		public int getRowsPerpage() {
			return rowsPerpage;
		}
		public void setRowsPerpage(int rowsPerpage) {
			this.rowsPerpage = rowsPerpage;
		}
}
