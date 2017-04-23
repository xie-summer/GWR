package com.gewara.untrans.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.model.common.JsonData;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SMSRecordBase;
import com.gewara.model.pay.SMSRecordHis;
import com.gewara.model.user.PointHist;
import com.gewara.service.DaoService;
import com.gewara.untrans.HisDataService;
import com.gewara.untrans.hbase.HBaseService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.util.ObjectId;
import com.gewara.util.StringUtil;

@Service("hisDataService")
public class HisDataServiceImpl implements HisDataService {
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	@Autowired@Qualifier("hbaseService")
	private HBaseService hbaseService;
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	
	private static final String TABLE_POINTHIS = "point";
	private static final String TABLE_SMSRECORDHIS = "smsrecord";
	private static final String TABLE_ORDERHIS = "orderhis";
	private String getIdHex(Long id){
		return new String(Hex.encodeHex(Bytes.toBytes(id)));
	}
	@Override
	public int backupPointHist() {
		JsonData config = daoService.getObject(JsonData.class, JsonDataKey.KEY_HISDATA_TIME_POINT);
		Long maxid = Long.parseLong(config.getData());
		String query = "from PointHist where id > ? order by id";
		List<PointHist> pointList = daoService.queryByRowsRange(query, 0, 5000, maxid);
		if(pointList.size()>0){
			List<Map<String,String>> rowList = new ArrayList<Map<String, String>>(pointList.size());
			for(PointHist point: pointList){
				Map<String, String> map = BeanUtil.getSimpleStringMap(point);
				map.put("id", getIdHex(point.getId()));
				rowList.add(map);
			}
			PointHist last = pointList.get(pointList.size()-1);
			config.setData(""+last.getId());
			daoService.saveObject(config);
			hbaseService.saveRowListByHex(TABLE_POINTHIS, "id", rowList);
			dbLogger.warn("backupPointSuccess:" + last.getId() + "," + last.getAddtime());
		}
		return pointList.size();
	}
	@Override
	public int createPointIndex(){
		JsonData config = daoService.getObject(JsonData.class, JsonDataKey.KEY_HISDATA_POINT_IDX);
		Long maxid = Long.parseLong(config.getData());
		String query = "from PointHist where id > ? order by id";
		List<PointHist> pointList = daoService.queryByRowsRange(query, 0, 5000, maxid);
		if(pointList.size()>0){
			List<Map<String,String>> rowList = new ArrayList<Map<String, String>>(pointList.size());
			for(PointHist point: pointList){
				Map<String, String> map = BeanUtil.getSimpleStringMap(point);
				map.put("id", getIdHex(point.getId()));
				rowList.add(map);
			}
			PointHist last = pointList.get(pointList.size()-1);
			config.setData(""+last.getId());
			daoService.saveObject(config);
			hbaseService.saveRowListByHex(TABLE_POINTHIS, "id", rowList);
			dbLogger.warn("backupPointSuccess:" + last.getId() + "," + last.getAddtime());
		}
		return pointList.size();
	}

	@Override
	public int backupSMSRecordHist() {
		JsonData config = daoService.getObject(JsonData.class, JsonDataKey.KEY_HISDATA_TIME_SMSRECORD);
		Long maxid = Long.parseLong(config.getData());
		String query = "from SMSRecordHis where id > ? order by id";
		List<SMSRecordHis> smsList = daoService.queryByRowsRange(query, 0, 5000, maxid);
		saveSMSRecordList(smsList);
		SMSRecordHis last = smsList.get(smsList.size()-1);
		
		dbLogger.warn("backupSMSRecordHist:" + last.getId() + "," + last.getSendtime());
		config.setData(""+last.getId());
		daoService.saveObject(config);
		return smsList.size();
	}
	@Override
	public <T extends SMSRecordBase> int saveSMSRecordList(List<T> smsList){
		List<Map<String,String>> rowList = new ArrayList<Map<String, String>>(smsList.size());
		for(SMSRecordBase sms: smsList){
			try{
				Map<String, String> map = BeanUtil.getSimpleStringMap(sms);
				String day = DateUtil.format(sms.getSendtime(),"yyyyMMddHHmmss");
				String content = SmsConstant.filterContent(sms);
				map.put("content", content);
				String hex = new ObjectId(Long.parseLong(StringUtils.trim(sms.getContact())), Integer.parseInt(day.substring(0,8)), Integer.parseInt(day.substring(8)) + content.hashCode()).toString();
				map.put("genid", hex);
				rowList.add(map);
			}catch(Exception e){
				dbLogger.warn(StringUtil.getExceptionTrace(e, 10));
			}
		}
		if(rowList.size()>0){
			hbaseService.saveRowListByHex(TABLE_SMSRECORDHIS, "genid", rowList);
		}
		return rowList.size();
	}
	@Override
	public List<Map<String, String>> getHisSmsList(String mobile){
		byte[] startRowId = new ObjectId(Long.valueOf(mobile), 0, 0).toByteArray();
		byte[] endRowId = new ObjectId(Long.valueOf(mobile), 20200101, 0).toByteArray();
		List<Map<String, String>> rowList = hbaseService.getRowListByIdRange(TABLE_SMSRECORDHIS, null, null, startRowId, endRowId, 1000);
		return rowList;
	}
	@Override
	public int backupOrder() {
		JsonData config = daoService.getObject(JsonData.class, JsonDataKey.KEY_HISDATA_TIME_ORDER);
		Timestamp from = DateUtil.parseTimestamp(config.getData());
		Timestamp to = DateUtil.addDay(from, 1);
		String query = "from GewaOrder where createtime>=? and createtime<?";
		List<GewaOrder> orderList = daoService.queryByRowsRange(query, 0, 500000, from, to);
		List<Map<String,String>> rowList = new ArrayList<Map<String, String>>(orderList.size());
		for(GewaOrder order: orderList){
			Map<String, String> map = BeanUtil.getSimpleStringMap(order);
			map.put("id", getIdHex(order.getId()));
			rowList.add(map);
		}
		hbaseService.saveRowListByHex(TABLE_ORDERHIS, "id", rowList);
		if(orderList.size()>0){
			GewaOrder last = orderList.get(orderList.size()-1);
			dbLogger.warn("backupOrderCount:" + config.getData()+ "," + last.getId() + "," + last.getCreatetime());
		}
		config.setData(DateUtil.formatTimestamp(to));
		daoService.saveObject(config);
		return orderList.size();
	}

}
