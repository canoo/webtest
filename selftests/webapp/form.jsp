<html>
<head>
<title>For setXXX direct steps tests</title>
</head>

<script type="text/javascript">
function updateSelect(_oSelect)
{
	var oSelect2 = _oSelect.form.mySelect2;
	// clear all entry except the first one
	for (var i=oSelect2.options.length-1; i>0; --i)
		oSelect2.options[i] = null;

	var oSelectedOption1 = _oSelect.options[_oSelect.selectedIndex];
	// adds to select 2 an option with
	var oOption = new Option("Sub " + oSelectedOption1.value, oSelectedOption1.value + "b");
	oSelect2.options[oSelect2.options.length] = oOption;
}
function toggleChocolateFlavor(enabled) {
    document.myForm.chocolateFlavor.disabled = !enabled;
}
function toggleBeerDegree() {
    document.myForm.beerDegree.disabled = !document.myForm.beerDegree.disabled;
}
</script>
<body>
<h1>A small form</h1>

<form name="myForm" action="form.jsp">

myTextField: <input type="text" id="textfieldId" name="myTextField" value="<%= (request.getParameter("myTextField")!=null ? request.getParameter("myTextField") : "") %>" onchange="this.form.myTextFieldEcho.value=this.value"><br>
<label for="myRefId">text field referenced by label</label>: <input type="text" id="myRefId" name="myTextFieldRefByLabel" value="<%= (request.getParameter("myTextFieldRefByLabel")!=null ? request.getParameter("myTextFieldRefByLabel") : "") %>"><br>

<input type="hidden" name="myTextField" value="decoy should not be found as text field">
myPasswordField: <input type="text" name="myPasswordField" value="" onchange="this.form.myPasswordFieldEcho.value=this.value"><br>
myTextArea: <textarea name="myTextArea" onchange="this.form.myTextAreaEcho.value=this.value"></textarea><br>
myRadioButton:
    <input type="radio" name="myRadioButton" value="mint" onclick="toggleChocolateFlavor(false)" <%= "mint".equals(request.getParameter("myRadioButton"))? "checked": "" %> > with mint,
    <input type="radio" name="myRadioButton" value="chocolate" onclick="toggleChocolateFlavor(true)" <%= "chocolate".equals(request.getParameter("myRadioButton"))? "checked": "" %> >with chocolate
    <input type="text" name="chocolateFlavor" <%= "chocolate".equals(request.getParameter("myRadioButton"))? "": "disabled" %> /><br>
cream: <input type="checkbox" id="checkboxId" name="myCheckbox" value="yes, please">with cream<br/>
beer:
    <input type="checkbox" name="beer" value="ice" <%= "ice".equals(request.getParameter("beer"))? "checked": "" %> >with ice,
    <input type="checkbox" name="beer" value="lemon" <%= "lemon".equals(request.getParameter("beer"))? "checked": "" %> >with lemon,
    <input type="checkbox" name="beer" onclick="toggleBeerDegree()" value="alcohol" <%= "alcohol".equals(request.getParameter("beer"))? "checked": "" %> >with alcohol
    (<input type="text" name="beerDegree" <%= "alcohol".equals(request.getParameter("beer"))? "": "disabled" %> > degree).<br/>
mySelect1: <select name="mySelect1" onchange="updateSelect(this)">
<option value="0">option 0</option>
<option value="1">option 1</option>
<option value="2">option 2</option>
</select><br/>
mySelect2: <select name="mySelect2">
<option value="00">option 00</option>
</select>
<input type='hidden' name='beer' value="zzz - decoy should not be found as checkbox"/>
<input type='hidden' name='fieldWithNoValue'/>
<br>

<br/><br/>
<hr>
JavaScript filled echos: <br/><br/>
myTextField: <input type="text" name="myTextFieldEcho"><br>
myPasswordField: <input type="text" name="myPasswordFieldEcho"><br>
myTextArea: <textarea name="myTextAreaEcho"></textarea><br>

<input type="submit">
</form>

<br/><br/>
<hr>
<h3>Received parameters:</h3>
<ul>
<%-- with scriptlet as long as it's not decided that we use JSP 2 --%>
<%
for (java.util.Enumeration en=request.getParameterNames(); en.hasMoreElements(); ) {
	String strParam = (String) en.nextElement();
	out.print("<li>" + strParam + ": ");
	String[] tabValues = request.getParameterValues(strParam);
	java.util.Arrays.sort(tabValues);
	for (int i=0; i<tabValues.length; ++i) {
		out.print(tabValues[i]);
		if (i != tabValues.length - 1) { out.print(", "); }
	}
	out.println("</li>");
}
%>
</ul>
</body>
</html>
