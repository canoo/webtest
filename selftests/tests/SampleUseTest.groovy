import com.canoo.webtest.WebtestCase
import org.junit.Test

/**
 * Example of WebTest expressed as unit test in Groovy
 */
public class SampleUseTest extends WebtestCase
{
	void testSample()
	{
		webtest("sampleUse as Groovy code: Show how to use Canoo WebTest plainly as unit test in Groovy")
		{
			invoke url: '${start.page}', description: "main page", 
				username: "some user", password: "some password"
			verifyTitle description: "main page reached",
				text: ".*WebT.*", regex: "true"

			group description: "sample with clickLink",
			{
				invoke url: '${start.page}', description: "get main page"
				clickLink "MyLink"
				verifyInputField description: "test the referrer", name: "field1", value: "MyLink"
			}

			group description: "example with form POST",
			{
				invoke description: "main page with POST form", url: '${start.page}?mode=postTest'
				verifyInputField description: "input field must be present and empty", name: "field1", value: ""
				setInputField description: "fill in a value", name: "field1", value: "X"
				clickButton "doIt"
				verifyInputField description: "submitted value is shown", name: "field1", value: "X"
			}

			sleep milliseconds: "50", description: "normal Ant sleep can be used"
		}
	}
}