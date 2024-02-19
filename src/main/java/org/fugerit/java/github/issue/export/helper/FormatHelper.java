package org.fugerit.java.github.issue.export.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FormatHelper {
	
	private FormatHelper() {}

	private static final String DF = "dd/MM/yyyy HH:mm";
	
	private static final String STRING_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	public static String formatDate( Object date, String lang ) throws ParseException {
		log.trace( "date : {}, lang : {}", date, lang );
		String res = "";
		if ( date != null && date.toString().length() > 0 ) {
			if ( date instanceof Date ) {
				SimpleDateFormat sdf = new SimpleDateFormat( DF );
				res = sdf.format( (Date)date );
			} else {
				SimpleDateFormat parser = new SimpleDateFormat( STRING_FORMAT );
				SimpleDateFormat sdf = new SimpleDateFormat( DF );
				Date curr = parser.parse( String.valueOf( date ) );
				res = sdf.format( curr );
			}
		}
		return res;
	}
	
}
