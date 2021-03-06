<html>
<head>
    <title>Favourite preferences page</title>
</head>
<body>
<%
    String greeting = "Hello " + request.getParameter("name");
    String fontStyle = "font-family:" + request.getParameter("font");
    String colorStyle = "color:" + request.getParameter("colour");
    String levelStr = request.getParameter("level");
    int level = 1;
    try {
        level = Integer.parseInt(levelStr);
        if (level < 1) level = 1;
        if (level > 6) level = 6;
    } catch (Exception e) {
        // ignore gives default level of one
    }
    String headingTag = "h" + level;
%>
<%= "<" + headingTag + " id='heading' style='" + colorStyle + "; " + fontStyle + "'>" + greeting + "</" + headingTag + ">" %>
<form method="post" action="favourite.jsp">
    <input type="hidden" name="hiddenLevel" value="<%= level %>">
    <label for="name">Enter your name</label>
    <input id="name" name="name" type="text" size="10"><br/>
    <label for="colour">Enter your favourite colour</label>
    <select id="colour" name="colour">
        <option>&lt;Select Colour&gt;</option>
        <option value="red"/>Red
        <option value="green"/>Green
        <option value="blue"/>Blue
        <option value="black"/>Black
    </select><br/>
    <label for="font">Enter your favourite font</label>
    <select id="font" name="font">
        <option>&lt;Select Font&gt;</option>
        <option value="arial"/>Arial
        <option value="times roman"/>Times Roman
        <option value="courier"/>Courier
        <option value="helvetica"/>Helvetica
    </select><br/>
    <label for="level">Enter a number between 1 and 6</label>
    <input id="level" name="level" type="text" size="3"><br/>
    <br/>
    <input type="submit" name="fav_submit" value="Submit">
</form>
</body>
</html>