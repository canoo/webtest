package com.canoo.webtest.engine.xpath;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

public class SimpleXPathVariableResolver implements XPathVariableResolver
{
	private final Map<QName, Object> variables_ = new HashMap<QName, Object>(); 
	
	public Object resolveVariable(final QName _variableName)
	{
		return variables_.get(_variableName);
	}

	public void setVariableValue(final QName _name, final Object _value)
	{
		variables_.put(_name, _value);
		
	}
}
