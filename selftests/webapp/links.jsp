<html>
<head>
<title>For clicklink tests</title>
</head>
<body>

<h1>Some links to test the &lt;clicklink...&gt; step...</h1>

<a href="links.jsp?linkClicked=MyLink">MyLink</a><br/>
<a href="links.jsp?linkClicked=image"><img alt="imagelink" src="seibertec.jpg"></a><br/>
<a href="links.jsp?linkClicked=linkWithId" id="myLinkId">MyLink with id</a><br/>

<br/>
<br/>
Request param 'linkClicked': <%=request.getParameter("linkClicked")%><br/>
<br/>
<br/>

Link to submit the form using javascript: <a href="javascript:document.forms[0].submit()" id="myJsLink">submit form</a>
<form action="links.jsp">
<input type="text" name="linkClicked" value="fromForm">
<input type="submit">
</form>

Link to call javascript while clicking on the form:
<form action="formTest">
<input type="text" name="linkClicked" value="fromForm">
    <script type="text/javascript">
        function setActionAndSubmit(inputField) {
            inputField.form.action.value = inputField.value;
            inputField.form.submit();
            return true;
        }
    </script>
<input type="submit" name="onClickSubmit" onClick="setActionAndSubmit(this)">
</form>

</body>
</html>
