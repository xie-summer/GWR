<%@page import="com.gewara.util.HttpUtils"%>
<%
String[] servers=new String[]{"43","44","45","46","47","48","49","50","51","39","49","37"};
String base = "http://172.22.1.";
out.println("<table>");
for(String server:servers){
	String result = HttpUtils.getUrlAsString(base + server + ":82/server.jsp").getRetval();
	out.println("<tr><td>" + server + "</td><td>" + result + "</td></tr>");
}
out.println("</table>");
%>
