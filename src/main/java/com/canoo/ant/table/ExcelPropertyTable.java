package com.canoo.ant.table;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class ExcelPropertyTable extends APropertyTable {

    private static final Logger LOG = Logger.getLogger(ExcelPropertyTable.class);

    public ExcelPropertyTable() {
    }

    protected boolean hasJoinTable() {
    	final Object sheet;
		try {
			sheet = getWorkbook().getSheet(KEY_JOIN);
		} 
		catch (final IOException e) {
			throw new RuntimeException("Failed to read container: >" + getContainer() + "<", e);
		}
    	return sheet != null;
    }

    private Workbook getWorkbook() throws IOException {
        final File file = getContainer();
        if (!file.exists()) {
        	throw new FileNotFoundException("File not found >" + file.getAbsolutePath() + "< " + getContainer());
        }
        else if (!file.isFile() ||!file.canRead()) {
        	throw new IllegalArgumentException("No a regular readable file: >" + file.getAbsolutePath() + "<");
        }

        try {
            final POIFSFileSystem excelFile = new POIFSFileSystem(new FileInputStream(file));
            return new HSSFWorkbook(excelFile);
        } catch (Exception e) {
            return new XSSFWorkbook(new FileInputStream(file));
        }
	}

	protected List read(final String sheetName) throws IOException {
        final Workbook workbook = getWorkbook();
        final Sheet sheet = getSheet(workbook, sheetName);

        final int lastRowNum = sheet.getLastRowNum();
        final List header = new ArrayList();
        final Row headerRow = sheet.getRow(0);
        for (short i = 0; i < headerRow.getLastCellNum(); i++) {
        	final Cell cell = headerRow.getCell(i);
        	if (cell != null)
        		header.add(stringValueOf(workbook, sheet, headerRow, cell));
        	else
        		header.add(null);
        }
        final List result = new LinkedList();
        for (int rowNo = 1; rowNo <= lastRowNum; rowNo++) { // last Row is included
            final Row row = sheet.getRow(rowNo);
            if (row != null) // surprising, but row can be null
            {
	            final Properties props = new Properties();
	            for (short i = 0; i < header.size(); i++) {
	            	final String headerName = (String) header.get(i);
	            	if (headerName != null) // handle empty cols
	            	{
		            	final Cell cell = row.getCell(i);
		            	final String value = stringValueOf(workbook, sheet, row, cell);
		                putValue(value, headerName, props);
	            	}
	            }
	            result.add(props);
            }
        }

        return result;
    }

	private Sheet getSheet(final Workbook workbook, final String sheetName)
	{
		final Sheet sheet;
		if (sheetName == null) {
        	sheet = workbook.getSheetAt(0); // no name specified, take the first sheet
        }
        else {
        	sheet = workbook.getSheet(sheetName);
        }
        if (null == sheet) {
        	String msg = "No sheet \"" + sheetName + "\" found in file " + getContainer() + ". Available sheets: ";
        	for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
        		if (i != 0)
        			msg += ", ";
				msg += workbook.getSheetName(i);
			}
        	throw new IllegalArgumentException(msg);
        }
		return sheet;
	}
    
    protected void putValue(String value, Object key, Properties props) {
        props.put(key, value);
    }
    
    private String stringValueOf(final Workbook workbook, final Sheet sheet, final Row row, final Cell cell) {
        if (null == cell) {
            return EMPTY;
        }
        final int cellValueType;
        if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            FormulaEvaluator evaluator;
            if (workbook instanceof HSSFWorkbook) {
                evaluator = new HSSFFormulaEvaluator((HSSFWorkbook) workbook);
            } else {
                evaluator = new XSSFFormulaEvaluator((XSSFWorkbook) workbook);
            }
           	cellValueType = evaluator.evaluateFormulaCell(cell);
        }
        else {
        	cellValueType = cell.getCellType();
        }
        
        switch (cellValueType) {
            case (Cell.CELL_TYPE_STRING):
                return cell.getRichStringCellValue().getString();
            case (Cell.CELL_TYPE_NUMERIC):
                final DataFormat dataFormat = workbook.createDataFormat();
            	if (DateUtil.isCellDateFormatted(cell))
            		return excelDateToString(dataFormat, cell);
            	else
            		return excelNumberToString(dataFormat, cell);
            case (Cell.CELL_TYPE_BLANK):
                return "";
            case (Cell.CELL_TYPE_BOOLEAN):
                return "" + cell.getBooleanCellValue();
            default:
                LOG.warn("Cell Type not supported: " + cell.getCellType());
                return EMPTY;
        }
    }
    
    private String excelNumberToString(DataFormat dataFormat, Cell _cell)
	{
    	final String excelFormat = dataFormat.getFormat(_cell.getCellStyle().getDataFormat());
    	final String javaFormat = excelNumberFormat2Java(excelFormat);
    	
    	LOG.debug("Excel date format >" + excelFormat + "< converted to >" + javaFormat + "< for " + _cell.getNumericCellValue());
    	String response = new DecimalFormat(javaFormat).format(_cell.getNumericCellValue());
    	
    	return response;
	}

	private String excelNumberFormat2Java(final String _excelFormat)
	{
		if ("general".equalsIgnoreCase(_excelFormat))
			return "#.##"; // default seems to be 2 decimals (if any)
		else
			return _excelFormat;
	}

	private String excelDateToString(final DataFormat dataFormat, final Cell _cell)
	{
    	final String excelFormat = dataFormat.getFormat(_cell.getCellStyle().getDataFormat());
    	final String javaFormat = excelDateFormat2Java(excelFormat);
    	LOG.debug("Excel date format >" + excelFormat + "< converted to >" + javaFormat + "<");
    	
    	final Date date = DateUtil.getJavaDate(_cell.getNumericCellValue());
    	return new SimpleDateFormat(javaFormat).format(date);
	}

	static String excelDateFormat2Java(String format)
    {
    	// Y -> y
    	format = format.replaceAll("Y", "y");
    	// DD -> dd
    	format = format.replaceAll("DD", "dd");
    	// remove \
    	format = format.replaceAll("\\\\", "");
    	// MM for minutes -> mm
    	format = format.replaceAll("HH:MM", "HH:mm");
    	// SS -> ss
    	format = format.replaceAll("SS", "ss");
    	// WW -> w
    	format = format.replaceAll("WW", "w");

    	return format;
    }
}
