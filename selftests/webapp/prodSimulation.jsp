<html>
<head>
<title>Production Simulator</title>
</head>
<body>
<h1>Production Simulator</h1>
<%
    String action = request.getParameter("action");
    String mode = request.getParameter("mode");
    if ("createUser".equals(action) && "prod".equals(mode)) {
%>
        <p id="result">DISASTER! You created a test user in production!</p>
<%  } else { %>
        <p id="result">Operation Successful. (Operation: <%= action %>, Mode: <%= mode %></p>
<%  } %>
</body>
</html>
