<html>
<head>
<title>Refresh test first page</title>
<meta http-equiv="refresh" content="<%=request.getParameter("delay") != null ? request.getParameter("delay"): "0"%>; URL=autoRefresh2.jsp">
</head>

<body>

Browser should be redirected to autoRefresh2.jsp

</body>
</html>
 
