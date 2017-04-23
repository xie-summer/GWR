package com.gewara.helper.ticket;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.gewara.helper.sys.CachedScript;
import com.gewara.helper.sys.CachedScript.ScriptResult;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.ticket.AutoSetter;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;

public abstract class AutoSetterHelper {
	/**
	 * 获取匹配的场次
	 * @param setter
	 * @param mpiList
	 * @return
	 */
	public static List<MoviePlayItem> getMatchMpi(AutoSetter setter, List<MoviePlayItem> mpiList,CachedScript cs){
		List<MoviePlayItem> result = new ArrayList<MoviePlayItem>(mpiList.size());
		for(MoviePlayItem mpi: mpiList){
			if(isMatch(setter, mpi,null,cs)) result.add(mpi);
		}
		return result;
	}
	/**
	 * 检测场次是否匹配
	 * @param setter
	 * @param mpi
	 * @return
	 */
	public static boolean isMatch(AutoSetter setter, MoviePlayItem mpi,Map<String,String> limit,CachedScript cs){
		if(!setter.getCinemaid().equals(mpi.getCinemaid())) return false;
		if(StringUtils.isNotBlank(setter.getRoomnum())){
			if(!ArrayUtils.contains(StringUtils.split(setter.getRoomnum(), ","),mpi.getRoomnum())) return false;
		}
		if(mpi.getPrice()==null) return false;
		if(!priceBetween(setter.getPrice1(),setter.getPrice2(), mpi.getPrice())) return false;
		if(!timeBetween(setter.getPlaytime1(), setter.getPlaytime2(), Timestamp.valueOf(mpi.getFullPlaytime()))) return false;
		if(!timeBetween(setter.getTimescope().split(","), mpi.getPlaytime().replace(":", ""))) return false;
		if(!StringUtils.contains(setter.getWeektype(), ""+DateUtil.getWeek(mpi.getPlaydate()))) return false;
		if(!ArrayUtils.contains(setter.getEdition().split(","), mpi.getEdition())) return false;
		if(StringUtils.isNotBlank(setter.getMovies()) && !BeanUtil.getIdList(setter.getMovies(), ",").contains(mpi.getMovieid())) return false;
		if(limit != null) return checkLimit(limit,mpi);
		if(cs != null){
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("mpi", mpi);
			ScriptResult<Boolean> result = cs.run(context);
			if(result.hasError() || result.getRetval()==null) return false;
			return result.getRetval();
		}
		return true;
	}
	
	/**
	 * 检测设置的播放时段，和播放日期，排除节假日里面的排片不进行自动开放
	 * @param limit
	 * @return
	 */
	private static boolean checkLimit(Map<String,String> limit, MoviePlayItem mpi){
		String[] playDateS = StringUtils.split(limit.get("playDate"),",");
		List<String> playDate = new ArrayList<String>();
		if(playDateS != null){
			playDate = Arrays.asList(playDateS);
			if(playDate.contains(DateUtil.format(mpi.getPlaydate(), "yyyy-MM-dd"))){
				return false;
			}
		}
		String[] timeScopeS = StringUtils.split(limit.get("timescope"),",");
		if(timeScopeS != null){
			for(String s : timeScopeS){
				String[] scopes =  StringUtils.split(s,"~");
				if(scopes != null && scopes.length == 2 && mpi.getPlaytime().compareTo(scopes[0]) >= 0
						&& mpi.getPlaytime().compareTo(scopes[1]) <= 0){
					return false;
				}
			}
			
		}
		return true;
	}
	/**
	 * 检测规则冲突
	 * @param setterList
	 * @return
	 */
	public static boolean isCollision(AutoSetter setter1, AutoSetter setter2){
		if(!setter1.getCinemaid().equals(setter2.getCinemaid())) return false;//影院不相等
		if(!priceBetween(setter1.getPrice1(), setter1.getPrice2(), setter2.getPrice1()) && 
				!priceBetween(setter1.getPrice1(), setter1.getPrice2(), setter2.getPrice2())) {
			//价格无交集
			return false;
		}
		if(!timeBetween(setter1.getPlaytime1(), setter1.getPlaytime2(), setter2.getPlaytime1()) && 
				!timeBetween(setter1.getPlaytime1(), setter1.getPlaytime2(), setter2.getPlaytime1())){
			//时间无交集
			return false;
		}
		boolean timescopeMatch = false;
		String[] scopeList1 = setter1.getTimescope().split(",");
		String[] scopeList2 = setter2.getTimescope().split(",");
		for(String scope: scopeList1){
			String[] pair=scope.split("~");
			if(timeBetween(scopeList2, pair[0]) || timeBetween(scopeList2, pair[1])){
				//时间有交集
				timescopeMatch = true;
				break;
			}
		}
		if(!timescopeMatch) return false;
		//星期无交集
		if(!StringUtils.containsAny(setter1.getWeektype(), setter2.getWeektype())) return false;
		//版本无交集
		if(Collections.disjoint(Arrays.asList(StringUtils.split(setter1.getEdition(), ",")), 
				Arrays.asList(StringUtils.split(setter2.getEdition(), ",")))){
			return false;
		}
		if(StringUtils.isNotBlank(setter1.getRoomnum()) && StringUtils.isNotBlank(setter2.getRoomnum())){
			if(Collections.disjoint(Arrays.asList(StringUtils.split(setter1.getRoomnum(), ",")), 
					Arrays.asList(StringUtils.split(setter2.getRoomnum(), ",")))){
				return false;
			}
		}
		//影片无交集
		if(StringUtils.isNotBlank(setter1.getMovies()) && StringUtils.isNotBlank(setter2.getMovies()) && 
				Collections.disjoint(BeanUtil.getIdList(setter1.getMovies(), ","), BeanUtil.getIdList(setter2.getMovies(), ","))){
			return false;
		}
		return true;
	}
	private static boolean timeBetween(Timestamp timefrom, Timestamp timeto, Timestamp testtime){
		return timefrom.getTime()<=testtime.getTime() && timeto.getTime()>testtime.getTime();
	}
	private static boolean timeBetween(String/*0000~2400*/[] scopeList, String time){
		for(String scope: scopeList){
			String[] timepair = scope.split("~");
			if(time.compareTo(timepair[0])>=0 && time.compareTo(timepair[1])<=0) return true;
		}
		return false;
	}
	private static boolean priceBetween(int price1, int price2, int testprice){
		return testprice>=price1 && testprice<=price2;
	}
}
