<%
final String counterStr = request.getParameter("counter");
final int counter = (counterStr == null) ? 0 : Integer.parseInt(counterStr);

String buffer = "";
if (request.getParameter("append") != null)
{
	buffer = (String) request.getSession().getAttribute("buffer");
	if (buffer == null)
		buffer = "";
	buffer += request.getParameter("append") + " ";
	request.getSession().setAttribute("buffer", buffer);
}
%>
<html>
<head>
<title>Test page for the retry step</title>
</head>

<body>

<a id="testLink" href="?counter=<%=counter+1%>">to next page</a>
<div>foo <%=counter/2%>, <%=counter%2%></div>

<hr>
Some links to test repeat on XPath
<ul id="testRepeatXPath">
  <li><a href="?append=1stLink">a first link</a></li>
  <li><a href="?append=2ndLink">a second link</a></li>
  <li><a href="?append=3rdLink">a third link</a></li>
</ul>

Link from this section clicked until now: <%=buffer%>
</body>
</html>
 
