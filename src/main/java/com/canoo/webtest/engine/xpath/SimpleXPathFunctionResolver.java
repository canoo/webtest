package com.canoo.webtest.engine.xpath;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xpath.compiler.FunctionTable;
import org.apache.xpath.functions.Function;

public class SimpleXPathFunctionResolver
{
	private final Map<QName, Function> functions_ = new HashMap<QName, Function>(); 
    private final FunctionTable funcTable_ = new FunctionTable();

	public Function resolveFunction(final QName _functionName, int _arity)
	{
		return functions_.get(_functionName);
	}

	public void registerFunction(final QName _functionName, final Class<? extends Function> _class)
	{
		try
		{
			functions_.put(_functionName, _class.newInstance());
		}
		catch (final Exception e)
		{
			throw new RuntimeException(e);
		}
        funcTable_.installFunction(_functionName.getLocalPart(), _class);
	}
	
	FunctionTable getFunctionTable()
	{
		return funcTable_;
	}
}
