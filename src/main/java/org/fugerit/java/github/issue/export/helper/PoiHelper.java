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
	
	public static void addRow( String[] values, int index, Sheet sheet, CellStyle style) {
		Row row = sheet.createRow( index );
		for ( int k=0; k<values.length; k++ ) {
			Cell cell = row.createCell( k );
			String currentCell = values[k];
			if ( currentCell.length() > 32767 ) {
				currentCell = currentCell.substring( 0, 32000 )+" [...]";
			}
			if ( currentCell == null || "null".equalsIgnoreCase( currentCell ) ) {
				currentCell = "";
			}
			cell.setCellValue( currentCell );
			if ( style != null ) {
				cell.setCellStyle( style );	
			}
		}
	}
	
	public static void addRow( String[] values, int index, Sheet sheet ) {
		addRow(values, index, sheet, null);
	}
	
}
