package test.org.fugerit.java.github.issue.export;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.fugerit.java.core.cli.ArgUtils;
import org.fugerit.java.core.lang.helpers.BooleanUtils;
import org.fugerit.java.core.lang.helpers.StringUtils;
import org.fugerit.java.github.issue.export.GithubIssueExport;
import org.fugerit.java.github.issue.export.GithubIssueExportMain;
import org.junit.Assert;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestGithubIssueExportMain {

	private void checkUser( List<String> params ) {
		String githubParam1 = System.getProperty( "githubParam1" );
		String githubParam2 = System.getProperty( "githubParam2" );
		if ( StringUtils.isNotEmpty( githubParam1 ) && StringUtils.isNotEmpty( githubParam2 ) ) {
			params.add( ArgUtils.getArgString( GithubIssueExport.ARG_GITHUB_USER ) );
			params.add( githubParam1 );
			params.add( ArgUtils.getArgString( GithubIssueExport.ARG_GITHUB_PASS ) );
			params.add( githubParam2 );	
			log.info( "set user and pass , user : {}", githubParam1 );
		}
	}
	
	@Test
	public void testCommandLine() {
		File outputFile = new File( "target", "report.xls" );
		List<String> params = new ArrayList<>( 
				Arrays.asList( ArgUtils.getArgString( GithubIssueExportMain.ARG_GUI ), BooleanUtils.BOOLEAN_FALSE,
				ArgUtils.getArgString( GithubIssueExport.ARG_OWNER ), "fugerit-org",
				ArgUtils.getArgString( GithubIssueExport.ARG_REPO ), "github-issue-export",
				ArgUtils.getArgString( GithubIssueExport.ARG_XLSFILE ), outputFile.getAbsolutePath() )
		);
		this.checkUser(params);
		String[] args = params.toArray( new String[0] );
		GithubIssueExportMain.main(args);
		Assert.assertTrue( outputFile.exists() );
	}

	@Test
	public void testCommandLineAlt() {
		File outputFile = new File( "target", "report.xls" );
		List<String> params = new ArrayList<>( 
				Arrays.asList( ArgUtils.getArgString( GithubIssueExportMain.ARG_GUI ), BooleanUtils.BOOLEAN_FALSE,
				ArgUtils.getArgString( GithubIssueExport.ARG_OWNER ), "fugerit-org",
				ArgUtils.getArgString( GithubIssueExport.ARG_REPO ), "github-issue-export",
				ArgUtils.getArgString( GithubIssueExport.ARG_XLSFILE ), outputFile.getAbsolutePath(),
				ArgUtils.getArgString( GithubIssueExportMain.ARG_COPY_RES ), "target" )
		);
		this.checkUser(params);
		String[] args = params.toArray( new String[0] );
		GithubIssueExportMain.main(args);
		Assert.assertTrue( outputFile.exists() );
	}

	@Test
	public void testCommandLineAltXlsx() {
		File outputFile = new File( "target", "report.xlsx" );
		List<String> params = new ArrayList<>(
				Arrays.asList( ArgUtils.getArgString( GithubIssueExportMain.ARG_GUI ), BooleanUtils.BOOLEAN_FALSE,
						ArgUtils.getArgString( GithubIssueExport.ARG_OWNER ), "fugerit-org",
						ArgUtils.getArgString( GithubIssueExport.ARG_REPO ), "github-issue-export",
						ArgUtils.getArgString( GithubIssueExport.ARG_XLSFILE ), outputFile.getAbsolutePath(),
						ArgUtils.getArgString( GithubIssueExportMain.ARG_COPY_RES ), "target" )
		);
		this.checkUser(params);
		String[] args = params.toArray( new String[0] );
		GithubIssueExportMain.main(args);
		Assert.assertTrue( outputFile.exists() );
	}
	
	@Test
	public void testHel() {
		String[] args = { ArgUtils.getArgString( GithubIssueExport.ARG_HELP ) };
		GithubIssueExportMain.main(args);
		Assert.assertTrue( true );
	}
	
	@Test
	public void testGui() {
		GithubIssueExportMain.main(new String[0]);
		Assert.assertTrue( true );
	}
	
	@Test
	public void testGuiLocale() {
		String[] args = { ArgUtils.getArgString( GithubIssueExportMain.ARG_GUI_LOCALE ), Locale.ENGLISH.toString() };
		GithubIssueExportMain.main(args);
		Assert.assertTrue( true );
	}
	
}
