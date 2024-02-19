package org.fugerit.java.github.issue.export.helper;

import java.util.Iterator;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Helper class for generating XLS spreadsheet
 * 
 * @author Daneel
 *
 */
public class PoiHelper {

	private PoiHelper() {}

	/*
	 * https://support.microsoft.com/en-gb/office/excel-specifications-and-limits-1672b34d-7043-467e-8e27-269d656771c3
	 */
	public static final int MAX_XLS_CELL_LENGTH = 32767;
	
	/*
	 * Use with caution, bad performance
	 */
	public static void resizeSheet( Sheet s ) {
		Row row = s.getRow( 0 );
		Iterator<Cell> cells = row.cellIterator();
		while ( cells.hasNext() ) {
			Cell c = cells.next();
			s.autoSizeColumn( c.getColumnIndex() );
		}
	}
	
	public static CellStyle getHeaderStyle( Workbook workbook ) {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold( true );
		style.setFont( font );
		style.setAlignment( HorizontalAlignment.CENTER );
		return style;
	}
	
	public static String prepareCell( String currentCell ) {
		if ( "null".equalsIgnoreCase( currentCell ) ) {
			currentCell = "";
		} else if ( currentCell.length() > MAX_XLS_CELL_LENGTH ) {
			currentCell = currentCell.substring( 0, 32000 )+"[...]";
		}
		return currentCell;
	}
	
	public static void addRow( String[] values, int index, Sheet sheet, CellStyle style) {
		Row row = sheet.createRow( index );
		for ( int k=0; k<values.length; k++ ) {
			Cell cell = row.createCell( k );
			cell.setCellValue( prepareCell( values[k] ) );
			if ( style != null ) {
				cell.setCellStyle( style );	
			}
		}
	}
	
	public static void addRow( String[] values, int index, Sheet sheet ) {
		addRow(values, index, sheet, null);
	}
	
}
