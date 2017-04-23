<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="com.gewara.util.WebUtils"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<body>
	<div id="myInfo">[<%=WebUtils.getRemoteIp(request) %>]</div>
	<div><%=WebUtils.getHeaderStr(request)%></div>
	<div>REDIRECT_PATH:<%=System.getenv().get("REDIRECT_PATH") %></div>
	<form action="/testCaptcha.xhtml">
		zt:<input name="zt"/>
		captchaId:<input name="captchaId"/>
		captcha:<input name="captcha"/><br />
		<input type="submit" value="submit"/>
	</form>
	<img src="/captcha.xhtml?captchaId=<%=request.getParameter("captchaId")%>&zt=<%=request.getParameter("zt")%>" />
</body>
</html>

