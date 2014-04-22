package com.canoo.ant.table;

import com.canoo.ant.filter.AllEqualsFilter;
import com.canoo.ant.filter.AllFilter;
import com.canoo.ant.filter.FirstEqualsFilter;
import com.canoo.ant.table.test.InMemoryPropertyTable;
import junit.framework.TestCase;

import java.io.File;
import java.util.List;
import java.util.Properties;

public class PropertyTableTest extends TestCase {
    private InMemoryPropertyTable fTable;
    private static final String TABLE = "mytable";
    private static final String FOREIGN_TABLE = "myForeignTable";

    public PropertyTableTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        fTable = new InMemoryPropertyTable();
        InMemoryPropertyTable.reset();
        fTable.setTable(TABLE);
        fTable.setContainer(new File("mycontainer"));
    }

    public void testEmptyNoExpansion() {
        assertEquals(0, fTable.getRawTable().size());
        assertTrue("All Filter is default", fTable.getFilter() instanceof AllFilter);
        assertEquals(0, fTable.getPropertiesList(null, null).size());
    }

    public void testOneEntryNoExpansion() {
        InMemoryPropertyTable.addTo(TABLE, new String[]{"myname"});
        InMemoryPropertyTable.addTo(TABLE, new String[]{"myvalue"});

        List rawTable = fTable.getRawTable();
        List propertiesList = fTable.getPropertiesList(null, null);
        // list is new, but entries are equal, nothing is said about identity of entries
        assertCopy(rawTable, propertiesList);
    }

    public void testSimplePrefixNoExpansion() {
        InMemoryPropertyTable.addTo(TABLE, new String[]{"myname"});
        InMemoryPropertyTable.addTo(TABLE, new String[]{"myvalue"});
        List propertiesList = fTable.getPropertiesList(null, "prefix");
        assertEquals("myvalue", ((Properties) propertiesList.get(0)).getProperty("prefix.myname"));
    }

    public void testSimpleFilter() {
        InMemoryPropertyTable.addTo(TABLE, new String[]{"myname"});
        InMemoryPropertyTable.addTo(TABLE, new String[]{"myvalue"});
        InMemoryPropertyTable.addTo(TABLE, new String[]{"othervalue"});
        fTable.setFilter(new FirstEqualsFilter("myname"));
        List rawTable = fTable.getRawTable();
        List propertiesList = fTable.getPropertiesList("othervalue", null);
        assertEquals(2, rawTable.size());
        assertEquals(1, propertiesList.size());
        assertEquals(rawTable.get(1), propertiesList.get(0));
    }

    public void testColumnExpansion() {
        InMemoryPropertyTable.addTo(TABLE, new String[]{"myname"});
        InMemoryPropertyTable.addTo(TABLE, new String[]{"myvalue"});

        InMemoryPropertyTable.addTo(FOREIGN_TABLE, new String[]{"myForeignName","othername"});
        InMemoryPropertyTable.addTo(FOREIGN_TABLE, new String[]{"myvalue",      "othervalue"});

        InMemoryPropertyTable.addTo(APropertyTable.KEY_JOIN, new String[]{
            TableFactory.KEY_TABLE, TableFactory.KEY_NAME, TableFactory.KEY_FOREIGN_TABLE, TableFactory.KEY_FOREIGN_NAME
        });
        InMemoryPropertyTable.addTo(APropertyTable.KEY_JOIN, new String[]{
            TABLE,                  "myname",               FOREIGN_TABLE,              "myForeignName"
        });

        List rawTable = fTable.getRawTable();
        List propertiesList = fTable.getPropertiesList(null, null);
        assertEquals(1, rawTable.size());
        assertEquals(1, propertiesList.size());
        Properties prop = (Properties) propertiesList.get(0);
        assertEquals("1 :"+prop,"myvalue", prop.getProperty("myname"));
        assertEquals("2 :"+prop,"myvalue", prop.getProperty("myForeignName"));
        assertEquals("3 :"+prop,"othervalue", prop.getProperty("othername"));
    }
    public void testEndlessRecursion() {
        InMemoryPropertyTable.addTo(TABLE, new String[]{"myname"});
        InMemoryPropertyTable.addTo(TABLE, new String[]{"myvalue"});

        InMemoryPropertyTable.addTo(APropertyTable.KEY_JOIN, new String[]{
            TableFactory.KEY_TABLE, TableFactory.KEY_NAME, TableFactory.KEY_FOREIGN_TABLE, TableFactory.KEY_FOREIGN_NAME
        });
        InMemoryPropertyTable.addTo(APropertyTable.KEY_JOIN, new String[]{
            TABLE,                  "myname",               TABLE,                         "myname"
        });

        List rawTable = fTable.getRawTable();
        List propertiesList = fTable.getPropertiesList(null, null);
        assertEquals(1, rawTable.size());
        assertEquals(1, propertiesList.size());
        Properties prop = (Properties) propertiesList.get(0);
        assertEquals("1 :"+prop,"myvalue", prop.getProperty("myname"));
    }

    public void testTwoDimensionalExpansion() {
        InMemoryPropertyTable.addTo(TABLE, new String[]{"myname"});
        InMemoryPropertyTable.addTo(TABLE, new String[]{"myvalue"});

        InMemoryPropertyTable.addTo(FOREIGN_TABLE, new String[]{"myForeignName","othername"});
        InMemoryPropertyTable.addTo(FOREIGN_TABLE, new String[]{"myvalue",      "othervalue"});
        InMemoryPropertyTable.addTo(FOREIGN_TABLE, new String[]{"myvalue",      "thirdvalue"});

        InMemoryPropertyTable.addTo(APropertyTable.KEY_JOIN, new String[]{
            TableFactory.KEY_TABLE, TableFactory.KEY_NAME, TableFactory.KEY_FOREIGN_TABLE, TableFactory.KEY_FOREIGN_NAME, TableFactory.KEY_FILTER_CLASS,
        });
        InMemoryPropertyTable.addTo(APropertyTable.KEY_JOIN, new String[]{
            TABLE,                  "myname",               FOREIGN_TABLE,                 "myForeignName",               AllEqualsFilter.class.getName()
        });

        List rawTable = fTable.getRawTable();
        List propertiesList = fTable.getPropertiesList(null, null);
        assertEquals(1, rawTable.size());
        assertEquals(2, propertiesList.size());

        Properties prop = (Properties) propertiesList.get(0);
        assertEquals("myvalue", prop.getProperty("myname"));
        assertEquals("myvalue", prop.getProperty("myForeignName"));
        assertEquals("othervalue", prop.getProperty("othername"));

        prop = (Properties) propertiesList.get(1);
        assertEquals("myvalue", prop.getProperty("myname"));
        assertEquals("myvalue", prop.getProperty("myForeignName"));
        assertEquals("thirdvalue", prop.getProperty("othername"));

    }

    private void assertCopy(List rawTable, List propertiesList) {
        assertNotSame(rawTable, propertiesList);
        assertEquals(rawTable, propertiesList);
    }

}