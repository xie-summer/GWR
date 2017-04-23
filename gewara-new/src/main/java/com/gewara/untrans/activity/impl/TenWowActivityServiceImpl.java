package com.gewara.untrans.activity.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.untrans.activity.TenWowActivityService;
import com.gewara.untrans.hbase.HBaseService;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;

@Service("tenWowActivityService")
public class TenWowActivityServiceImpl implements TenWowActivityService {

	private final String TABLE_TENWOW = "TENWOW";
	private final String FILEPATH_TENWOW = "/opt/TENWOW/";//
	private static final transient GewaLogger dbLogger = LoggerUtils.getLogger(TenWowActivityServiceImpl.class, Config.getServerIp(), Config.SYSTEMID);


	@Autowired
	@Qualifier("hbaseService")
	private HBaseService hbaseService;

	@Override
	public boolean isAuthenticode(String authenticode) {
		if (authenticode == null || authenticode.length() < 16) {
			return false;
		}

		Map<String, String> rowMap = hbaseService.getRow(TABLE_TENWOW, authenticode.getBytes());
		if (rowMap == null) {
			return false;
		}
		String status = rowMap.get("authentic_status");
		if (status == null || status.equals("0")) {
			return true;
		}

		return false;
	}

	@Override
	public boolean useAuthenticode(String authenticode, String memberid, String memberCode, String memberName) {
		if (!isAuthenticode(authenticode)) {
			return false;
		}
		Map<String, String> rowdata = new HashMap<String, String>(4);
		rowdata.put("authentic_status", "1");
		rowdata.put("memberid", memberid);
		rowdata.put("memberCode", memberCode);
		rowdata.put("memberName", memberName);
		hbaseService.saveRow(TABLE_TENWOW, authenticode.getBytes(), rowdata);
		return true;
	}

	@Override
	public boolean loadTinWowTxt(String fileName,boolean isLoad) {
		if(fileName==null||fileName.indexOf("..")>0||fileName.indexOf("//")>0||fileName.indexOf("\\")>0){
			return false;
		}
		
		FileReader reader=null;
		BufferedReader br=null;
		try {
			reader = new FileReader(FILEPATH_TENWOW + fileName);
			br = new BufferedReader(reader);
			String s1 = null;
			String idName="idName";
			List<Map<String, String>> rowList=new ArrayList<Map<String,String>>();
			dbLogger.error("开始导入数据："+fileName);
			int rowSize=0;
			long start=System.currentTimeMillis();
			while ((s1 = br.readLine()) != null) {
				Map<String,String> map=new HashMap();
				String[] s=s1.split("\t");
				
				String authenticode=null;
				
				if(s.length>=2){
					authenticode=s[1];
				}else{
					authenticode=s[0];
				}
				if (authenticode == null || authenticode.length() < 16) {
					if(isLoad){
						dbLogger.error(fileName+"数据有问题:"+" s.length="+s.length+" authenticode:"+authenticode.length() );
					}
				}else{
					map.put(idName, authenticode);
					map.put("rowNo", s[0]);
					rowList.add(map);
				}
				
				if(rowList.size()>=5000){
					if(isLoad){
						hbaseService.saveRowListByString(TABLE_TENWOW, idName, rowList);
						dbLogger.error(fileName+"已经导入："+rowSize);
					}
					rowSize=rowSize+rowList.size();
					rowList.clear();
				}
			}
			
			if(rowList.size()>0){
				if(isLoad){
					hbaseService.saveRowListByString(TABLE_TENWOW, idName, rowList);
				}
				rowSize=rowSize+rowList.size();
				rowList.clear();
			}
			dbLogger.error("导入数据完成："+fileName+" 导入数量:"+rowSize+" 耗时:"+(System.currentTimeMillis()-start)/1000);
		} catch (Exception e) {
			dbLogger.error("导入错误：",e);
		} finally {
			try {
				br.close();
			} catch (Exception e) {
			}
			try {
				reader.close();
			} catch (Exception e) {
			}
		}

		return false;
	}

}
