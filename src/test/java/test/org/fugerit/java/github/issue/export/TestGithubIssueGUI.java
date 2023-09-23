package test.org.fugerit.java.github.issue.export;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Duration;
import java.util.Properties;

import org.awaitility.Awaitility;
import org.fugerit.java.core.function.SafeFunction;
import org.fugerit.java.core.lang.helpers.StringUtils;
import org.fugerit.java.core.util.PropsIO;
import org.fugerit.java.github.issue.export.GithubIssueConfig;
import org.fugerit.java.github.issue.export.GithubIssueExport;
import org.fugerit.java.github.issue.export.GithubIssueGUI;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestGithubIssueGUI {

	@Test
	public void testGUI1() {
		SafeFunction.apply( () -> {
			long time = System.currentTimeMillis();
			File file = new File( "target/report_"+time+".xls" );
			Properties configProps = PropsIO.loadFromClassLoader( "gui/test-repo.properties" );
			configProps.setProperty( "xls-file" , file.getAbsolutePath() );
			
			String githubParam1 = System.getProperty( "githubParam1" );
			String githubParam2 = System.getProperty( "githubParam2" );
			if ( StringUtils.isNotEmpty( githubParam1 ) && StringUtils.isNotEmpty( githubParam2 ) ) {
				log.info( "github param set : {} - ********", githubParam1 );
				configProps.setProperty( GithubIssueExport.ARG_GITHUB_USER , githubParam1 );
				configProps.setProperty( GithubIssueExport.ARG_GITHUB_PASS , githubParam2 );
			}
			
			File configFile = new File( "target/override-config.properties" );
			try ( FileOutputStream fos = new FileOutputStream( configFile ) )  {
				configProps.store( fos , "test props" );
			}
			System.setProperty(GithubIssueConfig.ENV_OVERRIDE_MAIN_CONFIG , configFile.getCanonicalPath() );
			Properties params = new Properties();
			TestGUI gui = new TestGUI( params );
			gui.pressGenerateButton();
			Awaitility.await()
		    	.atLeast( Duration.ofMillis( 50 ) )
		    	.atMost( Duration.ofMillis( 5000 ) )
		    	.with()
		    	.pollInterval( Duration.ofMillis( 50 ) )
		    	.until( file::exists );
			gui.dispose();
			log.info( "file : {} -> {}", file.getCanonicalPath(), file.length() );
		} );
		
	}
	
	
}

class TestGUI extends GithubIssueGUI {
	
	private static final long serialVersionUID = 7334639546702278060L;

	public TestGUI(Properties params) {
		super(params);
	}
	
	public void pressGenerateButton() {
		super.pressGenerateButton();
	}
	
}

