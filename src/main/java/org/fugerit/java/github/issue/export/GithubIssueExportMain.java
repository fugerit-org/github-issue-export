/**
 * 
 */
package org.fugerit.java.github.issue.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import org.fugerit.java.core.cli.ArgUtils;
import org.fugerit.java.core.function.SafeFunction;
import org.fugerit.java.core.io.StreamIO;
import org.fugerit.java.core.lang.helpers.BooleanUtils;
import org.fugerit.java.core.lang.helpers.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GithubIssueExportMain {

	protected static final Logger logger = LoggerFactory.getLogger(GithubIssueExportMain.class);
	
	public static final String ARG_GUI = "gui";

	public static final String ARG_GUI_LOCALE = "gui_locale";
	
	private static final String PREFIX_GUI_PRESET = "gui_preset-";
	
	public static final String ARG_GUI_PRESET_ARG_ASSIGNEE_DATE_MODE = PREFIX_GUI_PRESET+GithubIssueExport.ARG_ASSIGNEE_DATE_MODE;
	public static final String ARG_GUI_PRESET_OWNER = PREFIX_GUI_PRESET+GithubIssueExport.ARG_OWNER;
	public static final String ARG_GUI_PRESET_REPO = PREFIX_GUI_PRESET+GithubIssueExport.ARG_REPO;
	public static final String ARG_GUI_PRESET_PROXY_HOST = PREFIX_GUI_PRESET+GithubIssueExport.ARG_PROXY_HOST;
	public static final String ARG_GUI_PRESET_PROXY_PORT = PREFIX_GUI_PRESET+GithubIssueExport.ARG_PROXY_PORT;
	
	public static final String ARG_COPY_RES = "copy-res";
	
	public static void main( String[] args ) {
		Properties params = ArgUtils.getArgs( args );
		// copy res start
		SafeFunction.applySilent( () -> {
			String copyRes = params.getProperty( ARG_COPY_RES );
			File basePath = GithubIssueConfig.getInstance().getBaseConfigPath();
			if ( StringUtils.isNotEmpty(copyRes) ) {
				File dest = new File( basePath, copyRes );
				if ( !dest.exists() ) {
					InputStream is = GithubIssueExportMain.class.getResourceAsStream( "/"+copyRes );
					FileOutputStream fos = new FileOutputStream( dest );
					StreamIO.pipeStream( is , fos , StreamIO.MODE_CLOSE_OUT_ONLY );
				}	
			}
		} );
		// copy res end
		SafeFunction.applySilent( () -> {
			String gui = params.getProperty( ARG_GUI, BooleanUtils.BOOLEAN_1 );
			if ( BooleanUtils.isTrue( gui ) ) {
				String guiLocale = params.getProperty( ARG_GUI_LOCALE );
				if (guiLocale != null) {
					logger.info( "gui locale : {}", guiLocale );
					Locale.setDefault( Locale.forLanguageTag( guiLocale ) );
				}
				logger.info( "gui mode : {} (default if gui mode, if no gui add --gui 0", gui );
				new GithubIssueGUI( params ); 
			} else {
				logger.info( "no gui mode : {}", gui );
				GithubIssueExport.handle( params );	
			}
		} );
	}
	
}
