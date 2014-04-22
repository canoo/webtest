package com.canoo.ant.table;

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

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

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

    private HSSFWorkbook getWorkbook() throws IOException {
        final File file = getContainer();
        if (!file.exists()) {
        	throw new FileNotFoundException("File not found >" + file.getAbsolutePath() + "< " + getContainer());
        }
        else if (!file.isFile() ||!file.canRead()) {
        	throw new IllegalArgumentException("No a regular readable file: >" + file.getAbsolutePath() + "<");
        }
        final POIFSFileSystem excelFile = new POIFSFileSystem(new FileInputStream(file));
        return new HSSFWorkbook(excelFile);
	}

	protected List read(final String sheetName) throws IOException {
        final HSSFWorkbook workbook = getWorkbook();
        final HSSFSheet sheet = getSheet(workbook, sheetName);

        final int lastRowNum = sheet.getLastRowNum();
        final List header = new ArrayList();
        final HSSFRow headerRow = sheet.getRow(0);
        for (short i = 0; i < headerRow.getLastCellNum(); i++) {
        	final HSSFCell cell = headerRow.getCell(i);
        	if (cell != null)
        		header.add(stringValueOf(workbook, sheet, headerRow, cell));
        	else
        		header.add(null);
        }
        final List result = new LinkedList();
        for (int rowNo = 1; rowNo <= lastRowNum; rowNo++) { // last Row is included
            final HSSFRow row = sheet.getRow(rowNo);
            if (row != null) // surprising, but row can be null
            {
	            final Properties props = new Properties();
	            for (short i = 0; i < header.size(); i++) {
	            	final String headerName = (String) header.get(i);
	            	if (headerName != null) // handle empty cols
	            	{
		            	final HSSFCell cell = row.getCell(i);
		            	final String value = stringValueOf(workbook, sheet, row, cell);
		                putValue(value, headerName, props);
	            	}
	            }
	            result.add(props);
            }
        }

        return result;
    }

	private HSSFSheet getSheet(final HSSFWorkbook workbook, final String sheetName)
	{
		final HSSFSheet sheet;
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
    
    private String stringValueOf(final HSSFWorkbook workbook, final HSSFSheet sheet, final HSSFRow row, final HSSFCell cell) {
        if (null == cell) {
            return EMPTY;
        }
        final int cellValueType;
        if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
           	final HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(sheet, workbook);
           	evaluator.setCurrentRow(row);
           	cellValueType = evaluator.evaluateFormulaCell(cell);
        }
        else {
        	cellValueType = cell.getCellType();
        }
        
        switch (cellValueType) {
            case (HSSFCell.CELL_TYPE_STRING):
                return cell.getRichStringCellValue().getString();
            case (HSSFCell.CELL_TYPE_NUMERIC):
                final HSSFDataFormat dataFormat = workbook.createDataFormat();
            	if (HSSFDateUtil.isCellDateFormatted(cell))
            		return excelDateToString(dataFormat, cell);
            	else
            		return excelNumberToString(dataFormat, cell);
            case (HSSFCell.CELL_TYPE_BLANK):
                return "";
            case (HSSFCell.CELL_TYPE_BOOLEAN):
                return "" + cell.getBooleanCellValue();
            default:
                LOG.warn("Cell Type not supported: " + cell.getCellType());
                return EMPTY;
        }
    }
    
    private String excelNumberToString(HSSFDataFormat dataFormat, HSSFCell _cell)
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

	private String excelDateToString(final HSSFDataFormat dataFormat, final HSSFCell _cell)
	{
    	final String excelFormat = dataFormat.getFormat(_cell.getCellStyle().getDataFormat());
    	final String javaFormat = excelDateFormat2Java(excelFormat);
    	LOG.debug("Excel date format >" + excelFormat + "< converted to >" + javaFormat + "<");
    	
    	final Date date = HSSFDateUtil.getJavaDate(_cell.getNumericCellValue());
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
