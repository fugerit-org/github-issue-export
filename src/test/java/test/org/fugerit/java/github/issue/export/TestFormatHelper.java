package test.org.fugerit.java.github.issue.export;

import java.sql.Date;
import java.text.ParseException;
import java.util.Locale;

import org.fugerit.java.github.issue.export.helper.FormatHelper;
import org.junit.Assert;
import org.junit.Test;

public class TestFormatHelper {

	@Test
	public void testDate() throws ParseException {
		String checkDate = "2024-01-01";
		String checkDateRes = "01/01/2024 00:00";
		String fd = FormatHelper.formatDate( Date.valueOf( checkDate ) , Locale.ENGLISH.getLanguage() );
		Assert.assertEquals( checkDateRes , fd);
	}
	
}
