<html>
<head>
<title>Button Test</title>
</head>

<body>

<h2>Form 1</h2>
<form name="form1" method="POST" action="clickButton.jsp?lastForm=form1">
<input type="hidden" name="mode" value="buttonTest" />
<input type="text" name="someInput1"/>
<Input type="image" src="seibertec.jpg" value="imageButtonValue" name="imageButton0" alt="value=imageButtonValue name=imageButton0 form=form1" />
<Input type="image" src="seibertec.jpg" value="imageButtonValue" name="imageButton1" alt="bla bla" />
</form>

<h2>Form 2</h2>
<form name="form2" method="POST" action="clickButton.jsp?lastForm=form2">

<input type="hidden" name="mode" value="buttonTest" />
<input type="text" name="someInput2"/>
<Input type="image" src="seibertec.jpg" value="imageButtonValue" name="imageButton1" alt="value=imageButtonValue name=imageButton1 form=form2" />
<Input type="image" src="seibertec.jpg" value="imageButtonValue" name="imageButton2" alt="value=imageButtonValue name=imageButton2 form=form2" />
<Input type="image" src="seibertec.jpg" value="imageButtonValue" name="imageButton2" alt="value=imageButtonValue name=imageButton2 form=form2" />
<Input type="image" src="seibertec.jpg" value="myImageButtonValue" name="imageButton2" alt="some image button text" />
</form>


<h2>Form 3</h2>
<form name="form3" method="POST" action="clickButton.jsp?lastForm=form3">
<input type="hidden" name="mode" value="buttonTest" />
<input type="text" name="someInput3"/>
<Input type="button" value="valueTypeButton" name="typeButton1" alt="value=valueTypeButton name=typeButton1 form=form3" />
</form>


<h2>Form 4</h2>
<form name="form4" method="POST" action="clickButton.jsp?lastForm=form4">

<input type="hidden" name="mode" value="buttonTest" />
<input type="text" name="someInput4"/>
<input type="submit" value="valueSubmitButton" name="submitButton1" alt="value=valueSubmitButton name=submitButton1 form=form4" />
<input type="submit" value="ok" id="buttonWithId" name="buttonWithIdName"/>
</form>
<br/>

<h2>Button with onClick</h2>
<form name="form4OnClick" method="POST" action="clickButton.jsp">
<Input type="button" name="onClick" value="onClickTest" onclick="window.open('formTest')"/>
</form>
<br/>

<h2>Button which needs form locator for clicking</h2>
<form name="form5" method="POST" action="clickButton.jsp">
<input type="hidden" name="x" value="1"/>
<Input type="submit" value="Details"/>
</form>
<form name="form6" method="POST" action="clickButton.jsp">
<input type="hidden" name="x" value="2"/>
<input type="submit" value="Details"/>
</form>
<br/>

<h2>Button which needs index locator for clicking</h2>
<form method="POST" action="clickButton.jsp?lastForm=form7">
<input type="hidden" name="x" value="3"/>
<input type="hidden" name="2ButtonsWithSameName" value="dummy to make sure we are not fooled"/>
<input type="submit" name="2ButtonsWithSameName" value="First button"/>
<input type="submit" name="2ButtonsWithSameName" value="2nd button"/>
</form>
<form method="POST" action="clickButton.jsp">
<input type="hidden" name="x" value="4"/>
<input type="submit" value="Details"/>
</form>
<br/>

<h2>Button which needs index locator for clicking</h2>
<form method="POST" action="clickButton.jsp?lastForm=form8">
<button name="2HtmlButtonsWithSameName" type="submit" value="First button">HtmlButton1</button>
<button name="2HtmlButtonsWithSameName" type="submit" value="2nd button">HtmlButton2</button>
<button id="HtmlButtonWithId" name="3rdButton" type="submit" value="3rd button">HtmlButton3</button>
</form>
<!--<form method="POST" action="clickButton.jsp">-->
<!--<input type="hidden" name="x" value="4"/> -->
<!--<input type="submit" value="Details"/>-->
<!--</form>-->
<br/>

<h2>Result Monitor</h2>
<form name="monitor" method="POST" action="clickButton.jsp">
Last submitted form: <Input type="text" name="lastForm" value="<%=request.getParameter("lastForm")%>" readonly />
</form>
:

<br/><br/>
<hr>
<h3>Received parameters:</h3>
<ul>
<%-- with scriptlet as long as it's not decided that we use JSP 2 --%>
<%
for (java.util.Enumeration en=request.getParameterNames(); en.hasMoreElements(); )
{
	String strParam = (String) en.nextElement();
	out.print("<li>" + strParam + ": ");
	String[] tabValues = request.getParameterValues(strParam);
	java.util.Arrays.sort(tabValues);
	for (int i=0; i<tabValues.length; ++i)
	{
		out.print(tabValues[i]);
		if (i != tabValues.length - 1)
			out.print(", ");
	}
	out.println("</li>");
}
%>
</ul>
</body>
</html>
