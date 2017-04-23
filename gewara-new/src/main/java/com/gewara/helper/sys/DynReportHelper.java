package com.gewara.helper.sys;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;

import com.gewara.model.report.Report;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;

public class DynReportHelper {
	private Report report;
	public static final String[] fieldtypeList = new String[]{"integer", "long", "double", "date", "timestamp", "string"};
	private static final Map<String, Class> fieldtypeMap = new HashMap<String, Class>();
	private Map<String, String> displayMap;
	private Map<String, Field> fieldMap;
	private List<Field> paramList = new ArrayList<Field>();
	private HttpServletRequest request;
	static {
		fieldtypeMap.put("integer", Integer.class);
		fieldtypeMap.put("long", Long.class);
		fieldtypeMap.put("double", Double.class);
		fieldtypeMap.put("date", Date.class);
		fieldtypeMap.put("timestamp", Timestamp.class);
		fieldtypeMap.put("string", String.class);
	}
	
	public DynReportHelper(Report report){
		this.report = report;
		this.displayMap = new CaseInsensitiveMap(JsonUtils.readJsonToMap(report.getDisplayname()));
		if(StringUtils.isNotBlank(report.getParams())) {
			this.paramList = JsonUtils.readJsonToObjectList(Field.class, report.getParams());
			Collections.sort(paramList);
		}
		fieldMap = new HashMap<String, Field>();
		List<Field> fieldList = JsonUtils.readJsonToObjectList(Field.class, report.getFields());
		if(fieldList!=null) fieldMap = BeanUtil.beanListToMap(fieldList, "fieldname");
	}
	public DynReportHelper(Report report, HttpServletRequest request) {
		this(report);
		this.request = request;
	}
	public List getParameterList(){
		List result = new ArrayList();
		for(Field field: paramList){
			result.add(getValueFromString(request.getParameter(field.fieldname), field.fieldtype));
		}
		return result;
		
	}
	public String getLabel(String fieldname){
		if(displayMap.containsKey(fieldname)) return displayMap.get(fieldname);
		return fieldname;
	}
	public String value2String(Map row, String fieldname){
		Object value = row.get(fieldname);
		if(value==null) return "";
		Field field = fieldMap.get(fieldname);
		if(field==null) return value.toString();
		String type = field.fieldtype;
		if(StringUtils.equals(type, "string")){
			return value.toString();
		}else if(StringUtils.equals(type, "date")){
			return DateUtil.formatDate((Date)value);
		}else if(StringUtils.equals(type, "timestamp")){
			return DateUtil.formatTimestamp((Timestamp)value);
		}else if(StringUtils.equals(type, "integer") || StringUtils.equals(type, "long") || StringUtils.equals(type, "double")){
			return ""+value;
		}else{
			throw new IllegalArgumentException("invalid data type!");
		}
		
	}
	public Object getValueFromString(String value, String type){
		if(StringUtils.equals(type, "string")){
			return value;
		}else if(StringUtils.equals(type, "date")){
			return DateUtil.parseDate(value);
		}else if(StringUtils.equals(type, "timestamp")){
			return DateUtil.parseTimestamp(value);
		}else if(StringUtils.equals(type, "integer")){
			return Integer.parseInt(value);
		}else if(StringUtils.equals(type, "long")){
			return Long.parseLong(value);
		}else if(StringUtils.equals(type, "double")){
			return Double.parseDouble(value);
		}else{
			throw new IllegalArgumentException("invalid data type!");
		}
	}
	public static class Field implements Comparable<Field>{
		private String fieldname;
		private Integer fieldorder;
		private String fieldtype;
		public String getFieldname() {
			return fieldname;
		}
		public void setFieldname(String fieldname) {
			this.fieldname = fieldname;
		}
		public String getFieldtype() {
			return fieldtype;
		}
		public void setFieldtype(String fieldtype) {
			this.fieldtype = fieldtype;
		}
		public Integer getFieldorder() {
			return fieldorder;
		}
		public void setFieldorder(Integer fieldorder) {
			this.fieldorder = fieldorder;
		}
		@Override
		public int compareTo(Field another) {
			if(fieldorder==null && another.fieldorder==null) return 0;
			if(fieldorder!=null && another.fieldorder!=null) return fieldorder.compareTo(another.fieldorder);
			return fieldorder==null?-1:1;
		}
	}
	public Report getReport() {
		return report;
	}
	public List<Field> getParamList() {
		return paramList;
	}
}
