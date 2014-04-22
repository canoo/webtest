<html>
<head>
<title>For setFileField tests</title>
</head>
<body>
<h2>A small form with some file input fields</h2>

<form name="myForm" action="formTest?mode=upload" method="POST" enctype="multipart/form-data">
<input type="hidden" id="foundButWrongType" name="myFileFieldName1" value="decoy should not be found as file field">
myFileField1: <input type="file" name="myFileFieldName1" value=""><br>
myFileField2: <input type="file" id="myFileFieldId2" name="myFileFieldName2" value=""><br>
<input type="submit">
</form>

</body>
</html>
