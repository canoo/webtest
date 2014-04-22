import com.canoo.webtest.WebtestCase

/**
 * WebTest tests can be written as plain Groovy code as well
 */
class GroovyExampleTest extends WebtestCase
{
	/**
	 * a webtest should throw an exeption if it fails when executed from Groovy
	 */
	void testWebtestThrowsWhenFailing()
	{
		shouldFail {
			webtest("[should fail] verify that a webtest written in Groovy throws an exception when failing (1)")
			{
				invoke 'numbers.html'
				verifyTitle "This is surely not the title"
			}
		}

		shouldFail {
			webtest("[should fail] verify that a webtest written in Groovy throws an exception when failing (2)")
			{
				notExistingStep notExistingAttribute: "something"
			}
		}
	}

	void testGroovyStepWithClosure()
	{
		webtest("test as Groovy code: Show how to use closure for the Groovy step")
		{
			invoke 'numbers.html'
			verifyTitle "Special Numbers"
			group(description: "groovy step with body as a closure")
			{
				not {
					verifyProperty name: "foo", text: "Hello from Groovy"
				}
				groovy(description: "set property 'foo' with step body specified as closure") {
					println "in the closure"
					step.webtestProperties["foo"] = "Hello from Groovy"
				}
				verifyProperty name: "foo", text: "Hello from Groovy"
			}
			group(description: "groovy step with body as a closure declared in the owning class")
			{
				not {
					verifyProperty name: "foo2", text: "Hello from a closure in the class"
				}
				groovy description: "set property 'foo2' with step body specified as closure which is a class property", aClosureOnTheClass
				verifyProperty name: "foo2", text: "Hello from a closure in the class"
			}

		}
	}
	
	def aClosureOnTheClass = {
		step.webtestProperties["foo2"] = "Hello from a closure in the class"
	}

	void testFoo()
	{
		webtest("Example of WebTest as GroovyTestCase")
		{
			invoke "numbers.html"
			verifyTitle "Special Numbers"
			verifyText "The prime number of the day is"
		}
	}
}