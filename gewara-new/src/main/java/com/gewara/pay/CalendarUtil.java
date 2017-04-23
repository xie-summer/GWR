package com.gewara.pay;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.gewara.util.DateUtil;

public class CalendarUtil implements Serializable {
	
	private static final long serialVersionUID = 5265606621670779299L;
	public static final List<String> WEEKLIST = Arrays.asList("日", "一", "二", "三", "四", "五", "六");
	protected Calendar cal = Calendar.getInstance(); 
	protected int today = cal.get(Calendar.DAY_OF_MONTH); 
	protected int month = cal.get(Calendar.MONTH);
	protected int year = cal.get(Calendar.YEAR);
	
	public CalendarUtil(){}
	public CalendarUtil(int year, int month){
		cal.set(year, month-1, 1, 0, 0, 0);
	}

	public String getMonthName() { 
		return DateUtil.format(cal.getTime(), "M月");
   }
	public int getToday(){
		return today;
   }
	
	public int getYear(){
		return cal.get(Calendar.YEAR);
	}

	public int getMonth(){
		return cal.get(Calendar.MONTH);
	}
	public int getStartCell() {
		Calendar beginOfMonth=Calendar.getInstance();    
		beginOfMonth.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 0); 
		int startCell = beginOfMonth.get(Calendar.DAY_OF_WEEK);
		if(startCell == 7) startCell = 0;
		return startCell;
	} 
	
	public int getEndCell() { 
     int endCell = cal.getActualMaximum(Calendar.DAY_OF_MONTH)+ getStartCell();  
     if(month == Calendar.FEBRUARY && ((GregorianCalendar)cal).isLeapYear(year)){      
          endCell++;  
     } 
     return endCell;
	}
	
	public Date getDate(){
		return cal.getTime();
	}
	
	public Date getPreviousMonthDate(){
		Calendar calendar = (Calendar) cal.clone();
		int years = cal.get(Calendar.YEAR);
		int months = cal.get(Calendar.MONTH);
		calendar.set(years, months-1, 1, 0, 0, 0);
		return calendar.getTime();
	}
	public Date getNextMonthDate(){
		Calendar calendar = (Calendar) cal.clone();
		int years = cal.get(Calendar.YEAR);
		int months = cal.get(Calendar.MONTH);
		calendar.set(years, months+1, 1, 0, 0, 0);
		return calendar.getTime();
	}
	public int getAllCell(){
		int endCell = getEndCell();
		int index = endCell/7;
		if(endCell%7 == 0) return endCell;
		return (index + 1)*7;
	}
	
	public Date getMonthDayDate(int day){
		Calendar calendar = (Calendar) cal.clone();
		calendar.set(Calendar.DAY_OF_MONTH, day);
		return DateUtil.getBeginningTimeOfDay(calendar.getTime());
	}
	
	public void update()  { 
	   cal.set(this.year, this.month, this.today);
	}  
	
	public List<String> getWeekList(){
		return WEEKLIST;
	}
 }
