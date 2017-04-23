<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%
   String contextPath = request.getContextPath();
   if(!contextPath.endsWith("/") && !contextPath.endsWith("\\"))
      contextPath+="/";
   if(!contextPath.startsWith("/") && !contextPath.startsWith("\\"))
      contextPath="/"+contextPath;
   String basePath = contextPath;
   contextPath = basePath;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <title>HTML编辑</title>
   <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
   <script type="text/javascript">
      function FCKeditor_OnComplete(editorInstance){
         window.status = editorInstance.Description ;
      }
   </script>
</head>
<body style="width: 650px; margin: auto">
   <form method="post" action="<%=basePath%>common/contentEditor.jsp" target="_self" id="editForm">
   	<input type="hidden" name="content" id="content" />
		<iframe id="content___Frame" width="98%" height="250px" frameborder="0" scrolling="no" src="<%=basePath%>fckeditor/editor/fckeditor.html?InstanceName=content&amp;Toolbar=Full"></iframe>
   </form>
   <input type="button" value="确定" onclick="saveForm();"/>
   <input type="button" value="取消" onclick="window.close();"/>
   <script type="text/javascript">
   </script>
</body>
</html>