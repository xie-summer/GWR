<%
   //String[] indexpages=new String[]{
   //   "/movie/index.xhtml","/shanghai/ktv/index.xhtml","/shanghai/bar/index.xhtml","/shanghai/gym/index.xhtml", "/shanghai/sport/index.xhtml"
   //};
   //int time = Math.abs((int)System.currentTimeMillis());
   //request.getRequestDispatcher(indexpages[time % 5]).forward(request, response);
   response.sendRedirect("/shanghai/movie/index.xhtml");
   //response.sendRedirect(indexpages[time % 5]);
%>