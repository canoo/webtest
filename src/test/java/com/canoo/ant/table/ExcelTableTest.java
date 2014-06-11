package com.canoo.ant.table;

import com.canoo.ant.filter.AllEqualsFilter;
import com.canoo.ant.filter.GroupFilter;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;

/**
 * Unit tests for {@link ExcelPropertyTable}.
 * @author unknown
 * @author Marc Guillemot
 */
public class ExcelTableTest extends BaseTestCase {

    public ExcelTableTest(String name) {
        super(name);
    }

    public void testFullXls() throws Exception {
        assertFile("Test.xls");
    }

    public void testFullXlsx() throws Exception {
        assertFile("Test.xlsx");
    }

    public void testDefaultsXls() throws Exception {
        assertFile("ByDefaults.xls");
    }

    public void testDefaultsXlsx() throws Exception {
        assertFile("ByDefaults.xlsx");
    }

    public void testMixedXls() throws Exception {
        assertFile("Mixed.xls");
    }

    public void testMixedXlsx() throws Exception {
        assertFile("Mixed.xlsx");
    }

    private void assertFile(final String file) throws Exception {
        ExcelPropertyTable table = new ExcelPropertyTable();
        table.setContainer(getPackageResource(file));
        table.setTable("roles");
        table.setFilter(new AllEqualsFilter("role.name"));
        // find lastname of the first user with role "guest"
        Properties firstProp = (Properties) table.getPropertiesList("guest", null).get(0);
        assertEquals(firstProp.toString(), "Koenig", firstProp.getProperty("user.last"));
    }

    public void testDependsXls() throws Exception {
        assertDepends("Depends.xls");
    }

    public void testDependsXlsx() throws Exception {
        assertDepends("Depends.xlsx");
    }

    // pb with the second expansion on the same table when
    // having multiple lines to expand
    private void assertDepends(final String file) throws Exception {
        ExcelPropertyTable table = new ExcelPropertyTable();
        table.setContainer(getPackageResource(file));
        table.setTable("suite");
        table.setFilter(new AllEqualsFilter("app"));
        List propertiesList = table.getPropertiesList("a", null);
        assertEquals(2, propertiesList.size());
        Properties firstProp = (Properties) propertiesList.get(0);
        assertEquals("0 "+firstProp.toString(), "PPP", firstProp.getProperty("partner.name"));
        Properties secondProp = (Properties) propertiesList.get(1);
        assertEquals("1 "+secondProp.toString(), "PPP", secondProp.getProperty("partner.name"));
    }

    public void testGroupXls() throws Exception {
        assertGroup("Depends.xls");
    }

    public void testGroupXlsx() throws Exception {
        assertGroup("Depends.xlsx");
    }

    private void assertGroup(final String file) throws Exception {
        ExcelPropertyTable table = new ExcelPropertyTable();
        table.setContainer(getPackageResource(file));
        table.setTable("suite");
        table.setFilter(new GroupFilter("app"));
        List propertiesList = table.getPropertiesList("a", null);
        assertEquals(3, propertiesList.size());
    }
    
    public void testDatePatternConversion()
    {
    	assertEquals("dd/MM/yyyy HH:mm:ss", ExcelPropertyTable.excelDateFormat2Java("DD/MM/YYYY\\ HH:MM:SS"));
    	assertEquals("dd/MM/yy", ExcelPropertyTable.excelDateFormat2Java("DD/MM/YY"));
    	assertEquals("dd/MM/yy HH:mm", ExcelPropertyTable.excelDateFormat2Java("DD/MM/YY\\ HH:MM"));
    	assertEquals("MMMM", ExcelPropertyTable.excelDateFormat2Java("MMMM"));
    	assertEquals("w", ExcelPropertyTable.excelDateFormat2Java("WW"));
    }

    public void testFormulaXls() throws Exception {
        assertFormula("format.xls");
    }

    public void testFormulaXlsx() throws Exception {
        assertFormula("format.xlsx");
    }

    private void assertFormula(final String file) throws Exception {
    	final ExcelPropertyTable table = new ExcelPropertyTable();
        table.setContainer(getPackageResource(file));
        table.setTable("formulaExamples");

        final List rawTable = table.getRawTable();
        Properties props = (Properties) rawTable.get(0);
        assertEquals("a value", props.getProperty("MyFormula"));

        props = (Properties) rawTable.get(1);
        assertEquals("149", props.getProperty("MyFormula"));

        props = (Properties) rawTable.get(2);
        // use a DecimalFormat here because depending on the default locale, it may be for instance "1.71" or "1,71"
        assertEquals(new DecimalFormat("#.##").format(1.71), props.getProperty("MyFormula"));

        props = (Properties) rawTable.get(3);
        assertEquals("135", props.getProperty("MyFormula"));

        props = (Properties) rawTable.get(4);
        assertEquals("7", props.getProperty("MyFormula"));
    }

    public void testEmptyCellsXls() throws Exception{
        assertEmptyCells("simple.xls");
    }

    public void testEmptyCellsXlsx() throws Exception{
        assertEmptyCells("simple.xlsx");
    }

    private void assertEmptyCells(final String file) throws Exception {
    	final ExcelPropertyTable table = new ExcelPropertyTable();
        table.setContainer(getPackageResource(file));
        table.setTable("CornerCase_blankCell");

        final List rawTable = table.getPropertiesList(null, null);
        Properties props = (Properties) rawTable.get(0);
        assertEquals("the associated value", props.getProperty("Header2"));

        props = (Properties) rawTable.get(1);
        assertEquals("", props.getProperty("Header2"));
   }
}