package org.fugerit.java.github.issue.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.fugerit.java.core.lang.helpers.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GithubIssueConfig {

	public static final String ENV_OVERRIDE_MAIN_CONFIG = "override-main-config";
	
	public static final String CONFIG_FOLDER = ".github-issue-export";
	
	public static final String MAIN_CONFIG_FILE = "saved-config.properties";
		
	private static final GithubIssueConfig INSTANCE = new GithubIssueConfig();
	
	public static final String FIELD_ASSIGN_DATE = "assign_date"; 
	
	public static GithubIssueConfig getInstance() {
		return INSTANCE;
	}
	
	public File getBaseConfigPath() {
		return new File( System.getProperty( "user.home" ), CONFIG_FOLDER );
	}
	
	public File getMainConfigFile() {
		String overrideMainConfig = System.getProperty( ENV_OVERRIDE_MAIN_CONFIG );
		File configFile = null;
		if ( StringUtils.isEmpty( overrideMainConfig ) ) {
			configFile = new File( getBaseConfigPath(), MAIN_CONFIG_FILE );
		} else {
			log.info( "override-main-config : {}", overrideMainConfig );
			configFile = new File( overrideMainConfig );
		}
		return configFile;
	}
	
	public File getCacheFileForRepo( String owner, String repo ) {
		String baseName = "cache-"+owner+"-"+repo+".properties";
		File file = new File( getBaseConfigPath(), baseName );
		if ( !file.getParentFile().exists() ) {
			log.info( "create config dir : {} -> {}", file.getAbsolutePath(), file.getParentFile().mkdirs() );
		}
		return file;
	}
	
	public Properties loadCachePropForRepo( String owner, String repo ) throws IOException {
		Properties cache = new Properties();
		File file = this.getCacheFileForRepo(owner, repo);
		if ( file.exists() ) {
			try ( FileInputStream fis = new FileInputStream( file ) ) {
				cache.load( fis );
			}
		}
		return cache;
	}
	
	public void saveCachePropForRepo( Properties cache, String owner, String repo ) throws IOException {
		try ( FileOutputStream fos = new FileOutputStream( this.getCacheFileForRepo(owner, repo) ) ) {
			cache.store( fos , "cache file for repo : "+owner+"/"+repo );
		}
	}
	
}
