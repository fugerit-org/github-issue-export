package test.org.fugerit.java.github.issue.export;

import org.fugerit.java.github.issue.export.helper.PoiHelper;
import org.junit.Assert;
import org.junit.Test;

public class TestPoiHelper {

	private static final String PAD = "ABCDEFGHABCDEFGHABCDEFGHABCDEFGHABCDEFGHABCDEFGHABCDEFGHABCDEFGHABCDEFGHABCDEFGHABCDEFGH ";
	
	@Test
	public void testPrepareCell() {
		StringBuffer buffer = new StringBuffer();
		int length = 0;
		while ( length < PoiHelper.MAX_XLS_CELL_LENGTH ) {
			buffer.append( PAD );
			length+= PAD.length();
		}
		String currentCell = PoiHelper.prepareCell( buffer.toString() );
		Assert.assertTrue( currentCell.indexOf( "[...]" ) > 0 );
		Assert.assertEquals( "" ,  PoiHelper.prepareCell( "null" ) );
	}
	
}
