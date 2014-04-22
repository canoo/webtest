package com.canoo.ant.table;

import java.io.File;
import java.util.Properties;

import com.canoo.ant.filter.AllEqualsFilter;

public class DirectoryTableTest extends BaseTestCase {

    public DirectoryTableTest(String name) {
        super(name);
    }

    public void testFull() throws Exception {
    	final APropertyTable table = new DirectoryPropertyTable();

        final File tableContainer = getPackageResource("container");
        assertTrue(tableContainer.exists());
        table.setContainer(tableContainer);
        table.setTable("roles");
        table.setFilter(new AllEqualsFilter("role.name"));
        // find lastname of the first user with role "guest"
        Properties firstProp = (Properties) table.getPropertiesList("guest", null).get(0);
        assertEquals("Koenig", firstProp.getProperty("user.last"));
        assertEquals("Mittie", firstProp.getProperty("user.file.name"));
        assertEquals("guest", firstProp.getProperty("roles.file.name"));
    }

}