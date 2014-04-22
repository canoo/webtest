package com.canoo.webtest.groovy;

import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.UnknownElement;

import com.canoo.webtest.extension.groovy.GroovyStep;

import groovy.lang.Closure;
import groovy.util.AntBuilder;

/**
 * Extends {@link AntBuilder} to add support for some goodies that are not fully Ant-conform
 * but more natural when writing Groovy code.
 * @author Marc Guillemot
 *
 */
public class WebTestBuilder extends AntBuilder {
	
	public WebTestBuilder(final Project antProject) {
		super(antProject);
	}

	/**
	 * In XML, WebTest &lt;groovy...&gt; step accepts the Groovy code as string.
	 * When the test is written in Groovy this is ugly to have to write Groovy code within a string
	 * and better to be able to pass a closure... what is not possible with the standard AntBuilder.
	 */
	@Override
	protected Object doInvokeMethod(String methodName, Object name, final Object _args) {
		final Object[] args = (Object[]) _args;
		if ("groovy".equals(methodName) && (args[args.length-1] instanceof Closure)) {
			final Closure closure = (Closure) args[args.length-1];

			final Integer key = GroovyStep.registerBodyClosure(closure);
			
			// pass all arguments except the closure to the AntBuilder
			final Map map;
			if (args.length == 1)
			{
				map = new HashMap();
			}
			else
			{
				map = (Map) args[0];
			}
			map.put("closureKey", key);
			final Object[] newArgs = { map };
			return super.doInvokeMethod(methodName, name, newArgs);
		}
		else
			return super.doInvokeMethod(methodName, name, args);
	}
}
