<%
	String status = request.getParameter("status");
	if(status!=null){
		out.println(status);
		response.sendError(Integer.parseInt(status));
	}else{
		out.println("status=500?");
	}
%>

