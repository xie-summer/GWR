<%@ page import="com.gewara.util.DateUtil"%>
<%
java.util.Date date = new java.util.Date();
Long cur = System.currentTimeMillis();
int d = Integer.valueOf(DateUtil.format(date, "dd"));
int h = Integer.valueOf(DateUtil.format(date, "HH"));
int m = Integer.valueOf(DateUtil.format(date, "mm"));
int s = Integer.valueOf(DateUtil.format(date, "ss"));
long remain = (h*60*60 + m*60 + s)*1000;
out.println(cur+"-"+remain+"-"+d);
%>
