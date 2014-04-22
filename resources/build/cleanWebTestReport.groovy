/*
Small script removing the variable parts in a WebTest report (like date, duration, ...) to allow comparison with a reference report
Call from WebTest root dir with:
groovy resources/build/cleanWebTestReport.groovy build/selftests/reports/WebTestReport.xml > build/selftests/reports/WebTestReport.cleaned.xml
*/

def currentReport = new File(args[0])

def WebTestHome = ""

def catchWebTestHome = ~/.*location="(.*?).selftests.tests.*/

def toDelete = ~/.*name="method" value="GET".*/
def toFix = ~/(duration|starttime|endtime)="[^"]*"/
def paramToFix = ~/parameter name="resultFilename" value="[^"]*"/                                                              

def failureResultPageFix = ~/(<failure message=.*resultFilename=&quot;)[^&]*(\.\w+&quot;.*)/
def i = 0

currentReport.eachLine 
{
line ->
	if (toDelete.matcher(line))
	{
		// ignore
	}
	else
	{
		def newLine = line
		if (!WebTestHome && catchWebTestHome.isCase(line))
		{
			def matcher = catchWebTestHome.matcher(line)
			matcher.find()
			WebTestHome = matcher.group(1)
//			println WebTestHome
		}
			
		newLine = newLine - WebTestHome
		newLine = toFix.matcher(newLine).replaceFirst(/$1="fixed for report comparison"/)
		newLine = paramToFix.matcher(newLine).replaceFirst(/parameter name="resultFilename" value="fixed"/)
		newLine = failureResultPageFix.matcher(newLine).replaceFirst('$1fixedForReportComparison$2')
		if (newLine)
			println newLine
	}
}
