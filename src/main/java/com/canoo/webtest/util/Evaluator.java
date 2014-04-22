// Copyright © 2004-2005 ASERT. Released under the Canoo Webtest license.
package com.canoo.webtest.util;

/**
 * Evaluator is a callback interface for the Expression class.
 * Objects wanting to define special values to be parsed in expressions
 * should supply an Evaluator instance that converts a string value
 * into it's double value.
 * <p/>
 * For example, to parse "2*(3-PI)", evaluate will be called with the value
 * "PI", which should return the value Math.PI.  The whole expression will then
 * evaluate to 0.283
 *
 * @author Paul King
 * @author Rob Nielsen
 */
public interface Evaluator
{
    /**
     * Evaluates a special string value.
     *
     * @param s the string to evaluate
     * @return the double value of the string
     * @throws IllegalArgumentException if there is a problem parsing the parameter
     */
    double evaluate(String s);
}
