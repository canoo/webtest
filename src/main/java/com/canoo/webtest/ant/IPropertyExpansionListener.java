package com.canoo.webtest.ant;

/**
 * Interface for listeners which are interested when properties are expanded.<p>
 *
 * WebTest reports contain the expanded form of properties (ie "foo" instead of "${myProp}")
 * but Ant doesn't offer any mean to know how properties have been expanded.<br/>
 * WebTest custom PropertyHelper {@link WebtestPropertyHelper} will notify build listeners
 * implementing {@link IPropertyExpansionListener} of property expansion.
 * @author Marc Guillemot
 */
public interface IPropertyExpansionListener
{
	/**
	 * Notifies a property expansion
	 * @param originalValue the original value (like "${myProp}")
	 * @param expanded the expanded value (like "blabla")
	 */
	void propertiesExpanded(final String originalValue, final String expanded);
}
