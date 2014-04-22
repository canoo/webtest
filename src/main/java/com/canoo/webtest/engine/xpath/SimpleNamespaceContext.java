package com.canoo.webtest.engine.xpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class SimpleNamespaceContext implements NamespaceContext
{
	final Map<String, String> prefix2uri_ = new HashMap<String, String>();
	final Map<String, List<String>> uri2prefixes_ = new HashMap<String, List<String>>();

	public String getNamespaceURI(final String _prefix)
	{
		return prefix2uri_.get(_prefix);
	}

	public String getPrefix(final String _namespaceURI)
	{
		final List<String> prefixes = uri2prefixes_.get(_namespaceURI);
		if (prefixes == null)
			return null;
		else
			return prefixes.get(0);
	}

	public Iterator<String> getPrefixes(final String _namespaceURI)
	{
		final List<String> prefixes = uri2prefixes_.get(_namespaceURI);
		if (prefixes == null)
			return null;
		else
			return prefixes.iterator();
	}

	public void addNamespace(final String _prefix, final String _uri)
	{
		prefix2uri_.put(_prefix, _uri);
		List<String> prefixes = uri2prefixes_.get(_uri);
		if (prefixes == null)
		{
			prefixes = new ArrayList<String>();
			uri2prefixes_.put(_uri, prefixes);
		}
		prefixes.add(_prefix);
	}

}

/**
 * A wrapper for a {@link NamespaceContext} allowing to locally add namespaces
 */
/*
 * private static class SimpleLocalNamespaceContext extends
 * SimpleNamespaceContext { private final NamespaceContext fWrappedContext;
 * SimpleLocalNamespaceContext(final NamespaceContext _wrapped) {
 * fWrappedContext = _wrapped; } public String
 * translateNamespacePrefixToUri(final String prefix) { String resp =
 * super.translateNamespacePrefixToUri(prefix); if (resp == null) return
 * fWrappedContext.translateNamespacePrefixToUri(prefix); else return resp; } }
 */
