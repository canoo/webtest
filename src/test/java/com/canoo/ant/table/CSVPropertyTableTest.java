package com.canoo.ant.table;

import java.util.List;
import java.util.Properties;

public class CSVPropertyTableTest extends BaseTestCase {

    public CSVPropertyTableTest(String name) {
        super(name);
    }

    public void testFile() throws Exception {
    	final APropertyTable table = new CSVPropertyTable();

        table.setContainer(getPackageResource("simple.csv"));

        final List rawTable = table.getPropertiesList(null, null);
        Properties props = (Properties) rawTable.get(0);
        assertEquals(2, rawTable.size());
        assertEquals("value11", props.getProperty("header1"));
        assertEquals("value12", props.getProperty("header2"));
        assertEquals("simple", props.getProperty(".file.name"));

        props = (Properties) rawTable.get(1);
        assertEquals("value21", props.getProperty("header1"));
        assertEquals("", props.getProperty("header2"));
        assertEquals("simple", props.getProperty(".file.name"));
    }

    public void testFolder() throws Exception {
        final APropertyTable table = new CSVPropertyTable();

        table.setContainer(getPackageResource("csv"));

        final List rawTable = table.getPropertiesList(null, null);
        Properties props = (Properties) rawTable.get(0);
        assertEquals(2, rawTable.size());
        assertEquals("value11", props.getProperty("header1"));
        assertEquals("value12", props.getProperty("header2"));
        assertEquals("one", props.getProperty(".file.name"));

        props = (Properties) rawTable.get(1);
        assertEquals("value21", props.getProperty("header1"));
        assertEquals("value22", props.getProperty("header2"));
        assertEquals("two", props.getProperty(".file.name"));
    }

    public void testFolderWithSpecificTable() throws Exception {
        final APropertyTable table = new CSVPropertyTable();

        table.setContainer(getPackageResource("csv"));
        table.setTable("two.csv");

        final List rawTable = table.getPropertiesList(null, null);
        Properties props = (Properties) rawTable.get(0);
        assertEquals(1, rawTable.size());
        assertEquals("value21", props.getProperty("header1"));
        assertEquals("value22", props.getProperty("header2"));
        assertEquals("two", props.getProperty(".file.name"));
    }

}