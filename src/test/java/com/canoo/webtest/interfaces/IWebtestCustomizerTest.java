package com.canoo.webtest.interfaces;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.reporting.StepExecutionListener;
import com.gargoylesoftware.htmlunit.WebClient;

/**
 * This TestCase guarantees the availability of the {@link IWebtestCustomizer}
 * interface: the tests won't be automatically refactored by an IDE and will detect erroneous refactoring.
 * 
 * @author ardeshir.arfaian
 * @author Marc Guillemot
 */
public class IWebtestCustomizerTest extends TestCase {

	private static final String interfaceName = "com.canoo.webtest.interfaces.IWebtestCustomizer";

	/**
	 * Key value shouldn't be changed as the string value can be used by... users
	 */
	public void testKey()
	{
		assertEquals("wt.webtestCustomizer", IWebtestCustomizer.KEY);
	}

	/**
	 */
	public void testInterfaceAvailable() throws Exception {
		Class.forName(interfaceName);
	}

	/**
	 * test StepExecutionListener createExecutionListener(WebtestTask wt);
	 * @throws Exception
	 */
	public void testCreateExecutionListener() throws Exception {
		testMethod("createExecutionListener", new Class[] { WebtestTask.class }, StepExecutionListener.class);
	}

	/**
	 * test WebClient customizeWebClient(WebClient wc);
	 * 
	 * @throws Exception
	 */
	public void testCustomizeWebClient() throws Exception {
		testMethod("customizeWebClient", new Class[] { WebClient.class }, WebClient.class);
	}

	private static void testMethod(final String methodName, final Class<?>[] args, final Class<?> returnType) throws Exception {

		final Class<?> connectionClass = Class.forName(interfaceName);
		Method method = connectionClass.getDeclaredMethod(methodName, args);

		assertEquals(returnType, method.getReturnType());
	}
}
