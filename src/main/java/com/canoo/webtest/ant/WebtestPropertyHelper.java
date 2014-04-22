package com.canoo.webtest.ant;

import java.text.ParsePosition;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.property.ParseNextProperty;
import org.apache.tools.ant.property.PropertyExpander;

import com.canoo.webtest.engine.Context;
import com.canoo.webtest.extension.ScriptStep;

/**
 * Helper class for working with Ant and WebTest dynamic properties.<p>
 *
 * This property helper is registered at the start of a WebTest and used by ant to evaluate
 * properties when configuring tasks.<br/>
 * It is able to evaluate traditional Ant properties like ${my.property} as well as 
 * WebTest dynamic properties like #{my.dynamic.property}.<br/>
 * It will notify build listeners implementing {@link IPropertyExpansionListener} of the property expansion.
 * @author Marc Guillemot
 */
public class WebtestPropertyHelper
{
    private static final Logger LOG = Logger.getLogger(WebtestPropertyHelper.class);
	
    private static final PropertyExpander WEBTEST_PROPERTY_EXPANDER = new PropertyExpander() {
        /**
         * Parse the next property name.
         * @param value the String to parse.
         * @param pos the ParsePosition in use.
         * @param parseNextProperty parse next property
         * @return parsed String if any, else <code>null</code>.
         */
        public String parsePropertyName(final String value, final ParsePosition pos,
        		final ParseNextProperty parseNextProperty) {
        	final int start = pos.getIndex();
            if (value.length() - start >= 3
                && '#' == value.charAt(start) && '{' == value.charAt(start + 1)) {
                pos.setIndex(start + 2);

                final StringBuilder sb = new StringBuilder();
                for (int c = pos.getIndex(); c < value.length(); c = pos.getIndex()) {
                    if (value.charAt(c) == '}') {
                        pos.setIndex(c + 1);
                        return "wt:" + sb.toString();
                    }
                    Object o = parseNextProperty.parseNextProperty(value, pos);
                    if (o != null) {
                        sb.append(o);
                    } else {
                        // be aware that the parse position may now have changed;
                        // update:
                        c = pos.getIndex();
                        sb.append(value.charAt(c));
                        pos.setIndex(c + 1);
                    }
                }
            }
            pos.setIndex(start);
            return null;
        }
	};

    private static final PropertyExpander NESTED_PROPERTY_EXPANDER = new PropertyExpander() {
        /**
         * Parse the next property name.
         * @param value the String to parse.
         * @param pos the ParsePosition in use.
         * @param parseNextProperty parse next property
         * @return parsed String if any, else <code>null</code>.
         */
        public String parsePropertyName(final String value, final ParsePosition pos,
        		final ParseNextProperty parseNextProperty) {
        	final int start = pos.getIndex();
            if (value.length() - start >= 3
                && '$' == value.charAt(start) && '{' == value.charAt(start + 1)) {
                pos.setIndex(start + 2);

                final StringBuilder sb = new StringBuilder();
                for (int c = pos.getIndex(); c < value.length(); c = pos.getIndex()) {
                    if (value.charAt(c) == '}') {
                        pos.setIndex(c + 1);
                        return sb.toString();
                    }
                    Object o = parseNextProperty.parseNextProperty(value, pos);
                    if (o != null) {
                        sb.append(o);
                    } else {
                        // be aware that the parse position may now have changed;
                        // update:
                        c = pos.getIndex();
                        sb.append(value.charAt(c));
                        pos.setIndex(c + 1);
                    }
                }
            }
            pos.setIndex(start);
            return null;
        }
	};


	/**
	 * Configures the special WebTest property helper for the current project
	 * @param project the project which property helper should be wrapped
	 */
	public static void configureWebtestPropertyHelper(final Project project) {
		final  PropertyHelper.PropertyEvaluator getProperty = new  PropertyHelper.PropertyEvaluator() {
			@Override
			public Object evaluate(final String property, final PropertyHelper propertyHelper) {
				if (!property.startsWith("wt:"))
				{
					return null;
				}
				return getDynamicPropertyValue(property.substring(3));
			}
		};
		final PropertyHelper propertyHelper = PropertyHelper.getPropertyHelper(project);
		propertyHelper.add(WEBTEST_PROPERTY_EXPANDER);
		propertyHelper.add(NESTED_PROPERTY_EXPANDER);
		propertyHelper.add(getProperty);
	}

	private static String getDynamicPropertyValue(final String propName) {
		if (propName.startsWith("script:"))
		{
	    	final Context context = WebtestTask.getThreadContext();
	        if (context == null || context.getRunner() == null) {
	            return null;
	        }
			final String expr = StringUtils.substringAfter(propName, "script:");
			return ScriptStep.evalScriptExpression(context, expr, null);
		}
		else
			return (String) getDynamicProperties().get(propName);
	}

	/**
	 * Gets the dynamic properties map for the current webtest
	 * @return the map of properties
	 */
	static Map getDynamicProperties() {
		return WebtestTask.getThreadContext().getWebtest().getDynamicProperties();
	}

	public static void cleanWebtestPropertyHelper(Project project) {
		final PropertyHelper propertyHelper = (PropertyHelper) project.getReferences().get(MagicNames.REFID_PROPERTY_HELPER);
		propertyHelper.add(WEBTEST_PROPERTY_EXPANDER);
		
	}
}