package com.gewara.service.drama.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.OdiConstant;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreField;
import com.gewara.service.DaoService;
import com.gewara.service.PlaceService;
import com.gewara.service.drama.DramaPlayItemImporter;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.DramaStarService;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;


@Service("dramaPlayItemImporter")
public class XLSDramaPlayItemImporter implements DramaPlayItemImporter {

	private transient final GewaLogger dgLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);

	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	
	@Autowired@Qualifier("dramaStarService")
	private DramaStarService dramaStarService;
	
	@Autowired@Qualifier("placeService")
	private PlaceService placeService;
	
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}

	@Override
	public void importPlayTime(String fileName, List<String> errorMessages, String tag) {
		importPlayTime(new File(fileName), errorMessages, tag);
	}

	@Override
	public void importPlayTime(File file, List<String> errorMessages, String tag){
		try {
			importHSSFPlayTime(new BufferedInputStream(new FileInputStream(file)), errorMessages, tag);
		} catch (Exception e) {
			try{
				importXSSFPlayTime(new BufferedInputStream(new FileInputStream(file)), errorMessages, tag);
			}catch (Exception e1) {
				throw new IllegalArgumentException(e1);
			}
		}
	}
	
	@Override
	public void importXSSFPlayTime(InputStream inputStream, List<String> errorMessages, String tag) {
		try {
			Workbook workbook = new XSSFWorkbook(inputStream);
			workbookImport(workbook, errorMessages, tag);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}finally{
			try {
				inputStream.close();
			} catch (IOException e) {
				dgLogger.warn("", e);
			}
		}
	}
	
	@Override
	public void importHSSFPlayTime(InputStream inputStream, List<String> errorMessages, String tag) {
		try {
			POIFSFileSystem fs = new POIFSFileSystem(inputStream);
			Workbook workbook = new HSSFWorkbook(fs);
			workbookImport(workbook, errorMessages, tag);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}finally{
			try {
				inputStream.close();
			} catch (IOException e) {
				dgLogger.warn("", e);
			}
		}
	
	}
	
	private void workbookImport(Workbook workbook, List<String> errorMessages, String tag){
		Sheet dataSheet = workbook.getSheetAt(0);
		String cityCode = "310000";// 默认是上海
		Row cityRow = dataSheet.getRow(1);
		if(cityRow != null){
			Cell cityCell = cityRow.getCell(2);
			if(cityCell != null){
				cityCode = cityCell.getRichStringCellValue().getString();
				if(StringUtils.isBlank(cityCode)) cityCode = "310000";
			}
		}
		//dramaname=B,name,playroom,playdate,playtime,price,discountprice,studentprice,language,remark
		int lastRowNum = dataSheet.getLastRowNum();
		List<Integer> rowNumberList = new ArrayList<Integer>();
		errorMessages.add("当前导入的是行政区号为" + cityCode + "的放映信息。");
		List<String> dramanameList = new ArrayList<String>();
		List<String> theatreNameList = new ArrayList<String>();
		List<String> dramaStarNameList = new ArrayList<String>();
		List<DramaPlayItem> playItemList = new ArrayList<DramaPlayItem>();
		Long batch = System.currentTimeMillis(); //批号
		Timestamp createtime = new Timestamp(batch);
		Date cur = DateUtil.getCurDate();
		if(StringUtils.isNotBlank(tag) && StringUtils.isNumeric(tag)) batch = new Long(tag);
		for(int rowIndex = 4; rowIndex <= lastRowNum; rowIndex ++){
			Row row = dataSheet.getRow(rowIndex);
			if(row == null) continue;//跳过空行
			Cell dramanameCell = row.getCell(1);//B列开始 
			Cell theatrenameCell = row.getCell(2); 
			Cell playroomCell = row.getCell(3); 
			Cell playdateCell = row.getCell(4);
			Cell playtimeCell = row.getCell(5); 
			Cell dramaStarCell = row.getCell(6);
			Cell languageCell = row.getCell(7);
			String dramaname = null,theatrename = null,playroom = null, playtime = null, language = null, dramastar = null;
			Date playdate = null;
			boolean error = false;
			if(dramanameCell==null || theatrenameCell==null || playdateCell == null || playtimeCell==null || playroomCell == null){
				if(dramanameCell==null && theatrenameCell==null)   continue; //认为是空行，跳过。
				if(dramanameCell != null)
					dramaname = dramanameCell.getRichStringCellValue().getString();
				if(theatrenameCell != null)
					theatrename = theatrenameCell.getRichStringCellValue().getString();
				if(StringUtils.isBlank(dramaname) && StringUtils.isBlank(theatrename)){//认为是空行，跳过。
					continue;
				}
				errorMessages.add("第"+(rowIndex+1)+"行：dramaname,theatrename,theatreroom,playtime都为必填项！");
				error = true;
			}else{
				try{
					dramaname = dramanameCell.getRichStringCellValue().getString();
				}catch(Exception e){
					errorMessages.add("第"+(rowIndex+1)+"行dramaname演出项目名称格式不正确，必须是Text类型！" + e.getMessage());
					error = true;
				}
				try{
					theatrename = theatrenameCell.getRichStringCellValue().getString();
				}catch(Exception e){
					errorMessages.add("第"+(rowIndex+1)+"行theatrename演出场馆名称格式不正确，必须是Text类型！" + e.getMessage());
					error = true;
				}
				try{
					playdate = playdateCell.getDateCellValue();
					if(playdate.before(cur)){
						errorMessages.add("第"+(rowIndex+1)+"行playdate日期已经过期！");
						error = true;
					}
				}catch(Exception e){
					errorMessages.add("第"+(rowIndex+1)+"行playdate日期格式不正确！" + e.getMessage());
					error = true;
				}
				//playdateCell.getNumericCellValue();
				try{
					playtime = DateUtil.formatTime(playtimeCell.getRichStringCellValue().getString());
				}catch(Exception e){
					errorMessages.add("第"+(rowIndex+1)+"行playtime时间格式不正确！" + e.getMessage());
					error = true;
				}
				if(StringUtils.isBlank(dramaname) || StringUtils.isBlank(theatrename) || playdate==null){
					errorMessages.add("第"+(rowIndex+1)+"行：dramaname,theatrename,playdate,playtime都为必填项！");
					error = true;
				}else{
					dramaname = StringUtils.trim(dramaname);
					theatrename = StringUtils.trim(theatrename);
				}
				try{
					playroom = playroomCell.getRichStringCellValue().getString();
				}catch(Exception e){
					errorMessages.add("第"+(rowIndex+1)+"行theatreroom演出场地格式不正确！" + e.getMessage());
					error = true;
				}
			}
			if(dramaStarCell != null){
				try{
					dramastar = dramaStarCell.getRichStringCellValue().getString();
					dramaStarNameList.add(dramastar);
				}catch(Exception e){
					errorMessages.add("第"+(rowIndex+1)+"行dramaStarname演出社团格式不正确！" + e.getMessage());
					error = true;
				}
			}
			if(!error){
				if(languageCell != null) language = languageCell.getRichStringCellValue().getString().trim();
				DramaPlayItem dpi = new DramaPlayItem(createtime);
				dpi.setRoomname(StringUtils.trim(playroom));
				dpi.setPlaytime(OdiConstant.getFullPlaytime(playdate, playtime));
				dpi.setLanguage(StringUtils.trim(language));
				dpi.setBatch(batch);
				dramanameList.add(StringUtils.trim(dramaname));
				theatreNameList.add(StringUtils.trim(theatrename));
				playItemList.add(dpi);
				rowNumberList.add(rowIndex+1);
			}
		}
		
		//下面两步分开是为了saveRecord提交到数据库后在生成静态页面
		saveRecord(cityCode, dramanameList,theatreNameList, dramaStarNameList, playItemList, rowNumberList, errorMessages);
	}
	private void saveRecord(String citycode, List<String> dramaNameList, List<String> theatreNameList, List<String> dramaStarNameList,
			List<DramaPlayItem> playItemList, List<Integer> rowNumberList, List<String> errorMessages) {
		Set<String> dramaNameSet =  new HashSet<String>(dramaNameList);
		Set<String> theatreNameSet =  new HashSet<String>(theatreNameList);
		Set<String> dramaStarNameSet =  new HashSet<String>(dramaStarNameList);
		Map<String,Drama> dramaMap = new HashMap<String, Drama>();
		Map<String,Theatre> theatreMap = new HashMap<String, Theatre>();
		Map<String,DramaStar> dramaStarMap = new HashMap<String, DramaStar>();
		for(String dramaName:dramaNameSet){
			Drama drama = dramaPlayItemService.getDramaByName(dramaName);
			if(drama != null) dramaMap.put(dramaName, drama); 
		}
		for(String theatreName:theatreNameSet){
			Theatre theatre = placeService.getPlaceByName(citycode, Theatre.class, theatreName);
			if(theatre != null) theatreMap.put(theatreName, theatre);
		}
		for (String dramaStarName : dramaStarNameSet) {
			DramaStar dramaStar = dramaStarService.getDramaStarByName(dramaStarName, DramaStar.TYPE_TROUPE);
			if(dramaStar != null) dramaStarMap.put(dramaStarName, dramaStar);
		}
		for(int i = 0,count=playItemList.size(); i < count; i++){
			Drama drama = dramaMap.get(dramaNameList.get(i));
			Theatre theatre = theatreMap.get(theatreNameList.get(i));
			DramaStar dramaStar =  dramaStarMap.get(dramaStarNameList.get(i));
			if(drama == null){
				errorMessages.add("第" + (rowNumberList.get(i)+1) + "行：演出项目“" + dramaNameList.get(i) + "”在数据库中不存在,请仔细核对名称！");
			}
			if(theatre == null){
				errorMessages.add("第" + (rowNumberList.get(i)+1) + "行：演出场馆“" + theatreNameList.get(i) + "”在数据库中不存在,请仔细核对名称！");
			}
			DramaPlayItem dpi = playItemList.get(i);
			if(drama!=null && theatre!=null){//保存
				if(StringUtils.isNotBlank(dpi.getRoomname())){
					TheatreField field = dramaPlayItemService.getTheatreFieldByName(theatre.getId(), dpi.getRoomname());
					if(field == null){
						errorMessages.add("第" + (rowNumberList.get(i)+1) + "行：" + theatreNameList.get(i) + "的演出场地“”" + dpi.getRoomname() + "不存在,请仔细核对名称！");
						continue;
					}else{
						dpi.setRoomid(field.getId());
					}
				}
				dpi.setTheatreid(theatre.getId());
				dpi.setCitycode(theatre.getCitycode());
				dpi.setDramaid(drama.getId());
				if(dramaStar != null) dpi.setDramaStarId(dramaStar.getId());
				DramaPlayItem olddpi = dramaPlayItemService.getUniqueDpi(theatre.getId(), drama.getId(), dpi.getRoomid(), dpi.getPlaytime());
				if(olddpi == null) olddpi = dpi;
				else{
					errorMessages.add("第" + (rowNumberList.get(i)+1) + "行：修改了原来的排片，请核对！“" + dramaNameList.get(i));
					try {
						Long id = olddpi.getId();
						PropertyUtils.copyProperties(olddpi, dpi);
						olddpi.setId(id);
					} catch (Exception e) {
						dgLogger.warn("", e);
						continue;
					}
				}
				daoService.saveObject(olddpi);
			}
		}
	}

}
