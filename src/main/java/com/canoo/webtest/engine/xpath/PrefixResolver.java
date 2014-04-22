package com.canoo.webtest.engine.xpath;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.apache.xpath.jaxp.JAXPPrefixResolver;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

class PrefixResolver extends JAXPPrefixResolver
{
	private final Map<String, String> grabbedNamespaces = new HashMap<String, String>();
	private String defaultNamespace = "WebTestDefaultNamespace";

    public PrefixResolver(final NamespaceContext nsContext, Object _contextItem) {
        super(nsContext);
        if (_contextItem instanceof Document)
        {
        	final NamedNodeMap docEltAttributes = ((Document) _contextItem).getDocumentElement().getAttributes();
        	
        	for (int i=0; i<docEltAttributes.getLength(); ++i) { 
        		final Node attrNode = docEltAttributes.item(i); 
        		if (attrNode.getNodeName().startsWith("xmlns:")) { 
        			final String namespace = attrNode.getNodeName().substring(6);
        			grabbedNamespaces.put(namespace, attrNode.getNodeValue());
        		}
        		else if (attrNode.getNodeName().equals("xmlns")) {
        			defaultNamespace = attrNode.getNodeValue();
        		}
        	}
        }
    }

    @Override
    public String getNamespaceForPrefix(String _prefix)
    {
    	if (_prefix.length() == 0) {
    		return defaultNamespace;
    	}
    	final String ns = grabbedNamespaces.get(_prefix);
    	if (ns != null)
    		return ns;
    	else
    		return super.getNamespaceForPrefix(_prefix);
    }
}
