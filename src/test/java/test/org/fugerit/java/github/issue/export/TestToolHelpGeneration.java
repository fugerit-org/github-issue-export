package test.org.fugerit.java.github.issue.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.fugerit.java.doc.base.config.DocConfig;
import org.fugerit.java.doc.base.config.DocException;
import org.fugerit.java.tool.helper.config.ToolHelpConfig;
import org.fugerit.java.tool.helper.facade.ToolHelpFacade;
import org.fugerit.java.tool.helper.handlers.TxtDocTypeHandler;
import org.junit.Assert;
import org.junit.Test;

public class TestToolHelpGeneration {

	private static final List<String> TYPES = Arrays.asList( DocConfig.TYPE_HTML, DocConfig.TYPE_MD, DocConfig.TYPE_PDF, TxtDocTypeHandler.TYPE );
	
	private boolean testWorker( String baseName, ToolHelpConfig config ) throws IOException, DocException {
		boolean ok = true;
		for ( String type : TYPES ) {
			String fileName = baseName+"."+type;
			File outputFile = new File( "target", fileName );
			try ( FileOutputStream fos = new FileOutputStream( outputFile ) ) {
				ToolHelpFacade.generate(type, config, fos);
			}
			ok = ok && outputFile.exists();
		}
		return ok;
	}
	
	@Test
	public void testGneration() throws IOException, DocException {
		boolean ok = this.testWorker( "tool_help_default" , ToolHelpConfig.loadSafe( "cl://tool-config/tool-help-config.xml" ) );
		Assert.assertTrue( ok );
	}
	
}
