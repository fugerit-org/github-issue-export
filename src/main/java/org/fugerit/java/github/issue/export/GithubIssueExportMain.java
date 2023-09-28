/**
 * 
 */
package org.fugerit.java.github.issue.export;

import java.util.Locale;
import java.util.Properties;

import org.fugerit.java.core.cli.ArgUtils;
import org.fugerit.java.core.function.SafeFunction;
import org.fugerit.java.core.io.SafeIO;
import org.fugerit.java.core.lang.helpers.BooleanUtils;
import org.fugerit.java.core.lang.helpers.ClassHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GithubIssueExportMain {
	
	private GithubIssueExportMain() {}

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
	
	public static final String ARG_HELP = "help";
	
	private static void printHelp() {
		log.info( "help : \n\n{}", SafeIO.readStringStream( () -> ClassHelper.loadFromDefaultClassLoader( "tool-config/help.txt" ) ) );
	}
	
	public static void handle( Properties params ) {
		String help = params.getProperty( ARG_HELP );
		if ( help != null ) {
			printHelp();
		} else {
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
	
	public static void main( String[] args ) {
		handle( ArgUtils.getArgs( args ) );
	}
	
}
