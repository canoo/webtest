// Released under the Canoo Webtest license.
package com.canoo.webtest.util;

import java.util.Map;

public class MapUtil
{
    /**
     * Utility for adding element to a map if not null: puts the value in the map if is not null,
     * does nothing if it is null.
     * 
     * @param map the map to put in
     * @param name the key to use
     * @param value the value to add to the map
     */
    public static void putIfNotNull(final Map map, final String name, final String value) {
        if (value != null) {
            map.put(name, value);
        }
    }
}
