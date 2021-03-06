package org.fugerit.java.github.issue.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.fugerit.java.core.cli.ArgUtils;
import org.fugerit.java.github.issue.export.helper.FormatHelper;
import org.fugerit.java.github.issue.export.helper.PoiHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tool for generating a report of github issues on a Repo
 * 
 * parameters :
 *  
 * --owner		github repo owner (required)
 * --repo		github repo name  (required)
 * --xls-file	issue report file (required)
 * --lang		language (optional)
 * 
 * --state		filter state of issues [open|closed|all] (optional, default:open)
 * --limit		max number of issue loaded per page (optional, default:100)
 * 
 * --proxy_host proxy address (optional)
 * --proxy_port proxy port (optional)
 * --proxy_user proxy user (optional)
 * --proxy_pass proxy password (optional)
 * 
 * --help		print help usage
 * 
 * @author Daneel
 *
 */
public class GithubIssueExport {

	protected static final Logger logger = LoggerFactory.getLogger(GithubIssueExport.class);
	
	public static final String ARG_HELP = "help";
	
	public static final String ARG_ASSIGNEE_DATE_MODE = "assignee_date_mode";
	public static final String ARG_ASSIGNEE_DATE_MODE_CACHE = "cache";
	public static final String ARG_ASSIGNEE_DATE_MODE_ALL = "all";
	public static final String ARG_ASSIGNEE_DATE_MODE_SKIP = "skip";
	public static final String ARG_ASSIGNEE_DATE_MODE_SKIP_CLOSED = "skip-closed";
	
	public static final String ARG_REPO = "repo";
	public static final String ARG_OWNER = "owner";
	
	public static final String ARG_GITHUB_USER = "github_user";
	public static final String ARG_GITHUB_PASS = "github_pass";
	
	public static final String ARG_XLSFILE = "xls-file";
	
	public static final String ARG_LANG = "lang";
	
	public static final String ARG_LIMIT = "limit";
	public static final String ARG_LIMIT_DEFAULT = "100";
	
	public static final String ARG_STATE = "state";
	public static final String ARG_STATE_OPEN = "open";
	public static final String ARG_STATE_CLOSED = "closed";
	public static final String ARG_STATE_ALL = "all";
	public static final String ARG_STATE_DEFAULT = ARG_STATE_OPEN;
	
	public static final String ARG_PROXY_HOST = "proxy_host";
	
	public static final String ARG_PROXY_PORT = "proxy_port";
	
	public static final String ARG_PROXY_USER = "proxy_user";
	
	public static final String ARG_PROXY_PASS = "proxy_pass";
	
	public static Locale getLocale( String lang ) {
		Locale loc = Locale.getDefault();
		if ( !StringUtils.isEmpty( lang ) ) {
			try {
				loc = Locale.forLanguageTag( lang );	
			} catch (Exception e) {
				logger.warn( "Errore overriding locale : "+lang+", using default : "+loc, e );
			}
		}
		return loc;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List<Map> parseJsonData( String data ) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		JsonFactory factory = mapper.getFactory();
		JsonParser jp = factory.createParser( data );
		JsonNode node = jp.readValueAsTree();
		// data mapping
		ArrayList<Map> issueList = new ArrayList<Map>();
		issueList = ( ArrayList<Map> )buildModel( String.valueOf( node ), issueList.getClass() );
		return issueList;
	}
	
	@SuppressWarnings({ "rawtypes" })
	protected static void handle( Properties params ) throws Exception {
		GithubIssueInfo info = new GithubIssueInfo(params);
		try {
			logger.debug( "params "+params );
			doHandle( info );
		} catch ( Exception e ) {
			throw e;
		} finally {
			GithubIssueConfig.getInstance().saveCachePropForRepo( info.getCache() , info.getOwner(), info.getRepo() );
		}
	}
		
	private static boolean activeCache( String cacheMode ) {
		return  ARG_ASSIGNEE_DATE_MODE_ALL.equals( cacheMode ) || ARG_ASSIGNEE_DATE_MODE_SKIP_CLOSED.equals( cacheMode );
	}
	
	private static void doHandle( GithubIssueInfo info ) throws Exception {
		List<List<String>> lines = new ArrayList<List<String>>();
		
		String cacheMode = info.getProperty( GithubIssueExport.ARG_ASSIGNEE_DATE_MODE, GithubIssueExport.ARG_ASSIGNEE_DATE_MODE_SKIP );
		logger.info( "cache-mode : "+cacheMode );
		
		String lang = getLocale( info.getProperty( ARG_LANG ) ).toString();
		// data read
		int currentePage = 1;
		String limit = info.getProperty( ARG_LIMIT , ARG_LIMIT_DEFAULT );
		int perPage = Integer.parseInt( limit );
		String data = readData( info, currentePage, perPage );
		List<Map> issueList = parseJsonData( data );
		while ( issueList.size() % perPage == 0 ) {
			currentePage++;
			data = readData( info, currentePage, perPage );
			issueList.addAll( parseJsonData( data ) );
		}
		// finishing touch
		Iterator<Map> issueIt = issueList.iterator();
		while ( issueIt.hasNext() ) {
			Map issue = issueIt.next();
			List<String> currentLine = new ArrayList<String>();
			String issueId = String.valueOf( issue.get( "number" ) );
			String state = String.valueOf( issue.get( "state" ) );
			currentLine.add( issueId );
			currentLine.add( String.valueOf( issue.get( "title" ) ) );
			currentLine.add( state );
			// labels
			List labels = (List)issue.get( "labels" );
			if ( labels != null && labels.size() > 0 ) {
				Iterator itLables = labels.iterator();
				StringBuffer labelList = new StringBuffer();
				while ( itLables.hasNext() ) {
					Map currentLabel = (Map)itLables.next();
					labelList.append( currentLabel.get( "name" ) );
					labelList.append( ", " );
				}
				currentLine.add( labelList.toString() );
			} else {
				currentLine.add( "-" );
			}
			// assigned
			Map assignee = (Map)issue.get( "assignee" );
			if ( assignee != null ) {
				currentLine.add( String.valueOf( assignee.get( "login" ) ) );
				String assignDate = null;
				boolean activeCache = activeCache( cacheMode);
				if ( activeCache ) {
					assignDate = info.getCacheEntry( issueId , GithubIssueConfig.FIELD_ASSIGN_DATE );
				}
				logger.info( "activeCache : "+activeCache+" - issueId:"+issueId+" , assign date "+assignDate );
				if ( assignDate == null ) {
					if ( "closed".equalsIgnoreCase( state ) && ARG_ASSIGNEE_DATE_MODE_SKIP_CLOSED.equals( cacheMode ) ) {
						// just skip
					} else {
						String eventUrl = String.valueOf( issue.get( "events_url" ) );
						String eventsData = readUrlData( eventUrl, info );
						List<Map> eventsList = parseJsonData( eventsData );
						Iterator<Map> eventsIt = eventsList.iterator();
						while ( eventsIt.hasNext() ) {
							Map currentEvent = eventsIt.next();
							String eventType = String.valueOf( currentEvent.get( "event" ) );
							if ( eventType.equalsIgnoreCase( "assigned" ) ) {
								assignDate = String.valueOf( currentEvent.get( "created_at" ) );
								if ( activeCache ) {
									info.addCacheEntry( issueId , GithubIssueConfig.FIELD_ASSIGN_DATE, assignDate );
								}
							}
						}		
					}				
				}
				currentLine.add( FormatHelper.formatDate( assignDate, lang ) );
			} else {
				currentLine.add( "-" );
				currentLine.add( "-" );
			}
			Map user = (Map)issue.get( "user" );
			currentLine.add( String.valueOf( user.get( "login" ) ) );
			currentLine.add( FormatHelper.formatDate( issue.get( "created_at" ), lang ) );
			currentLine.add( FormatHelper.formatDate( issue.get( "updated_at" ), lang ) );
			currentLine.add( FormatHelper.formatDate( issue.get( "closed_at" ), lang ) );
			currentLine.add( String.valueOf( issue.get( "comments" ) ) );
			currentLine.add( String.valueOf( issue.get( "html_url" ) ) );
			currentLine.add( String.valueOf( issue.get( "body" ) ) );
			lines.add( currentLine );
		}
		handleExcel( info, lines);
	}

	private static String readUrlData( String url, GithubIssueInfo info ) throws Exception {
		final String proxyHost = info.getProperty( ARG_PROXY_HOST );
		final String proxyPort = info.getProperty( ARG_PROXY_PORT );
		final String proxyUser = info.getProperty( ARG_PROXY_USER );
		final String proxyPass = info.getProperty( ARG_PROXY_PASS );
		String githubUser = info.getProperty( ARG_GITHUB_USER );
		String githubPass = info.getProperty( ARG_GITHUB_PASS );
		logger.info( "connecting to url : "+url+" (user:"+githubUser+")" );
		HttpURLConnection conn;
		if ( !StringUtils.isEmpty( proxyHost ) && !StringUtils.isEmpty( proxyPort ) ) {
			logger.debug( "using proxy : "+proxyHost+":"+proxyPort+" (user:"+proxyUser+")" );
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt( proxyPort )));
			if ( !StringUtils.isEmpty( proxyUser ) && !StringUtils.isEmpty( proxyPass ) ) {
				Authenticator authenticator = new Authenticator() {
			        public PasswordAuthentication getPasswordAuthentication() {
			            return (new PasswordAuthentication( proxyUser, proxyPass.toCharArray() ) );
			        }
			    };
			    Authenticator.setDefault(authenticator);
			}
			URL u = new URL( url );
			conn = (HttpURLConnection)u.openConnection( proxy );
		} else {
			URL u = new URL( url );
			conn = (HttpURLConnection)u.openConnection();
			if ( StringUtils.isNotEmpty( githubUser ) && StringUtils.isNotEmpty( githubPass ) ) {
				String encoded = Base64.getEncoder().encodeToString((githubUser+":"+githubPass).getBytes(StandardCharsets.UTF_8));
				logger.info( "Set authentication : "+encoded );
				conn.setRequestProperty("Authorization", "Basic "+encoded);
			}
		}
		StringBuffer buffer = new StringBuffer();
		if ( conn.getResponseCode() != 200 ) {
			throw new Exception( "HTTP exit code : "+conn.getResponseCode() );
		} else {
			BufferedReader br = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
			String line = br.readLine();
			while ( line != null ) {
				buffer.append( line );
				line = br.readLine();
			}
			br.close();
		}
		conn.disconnect();
		return buffer.toString();
	}
	
	private static String readData( GithubIssueInfo info, int currentPage, int perPage ) throws Exception {
		String repo = info.getRepo();
		String owner = info.getOwner();
		String state = info.getProperty( ARG_STATE, ARG_STATE_DEFAULT );
		if ( StringUtils.isEmpty( state ) ) {
			state = ARG_STATE_DEFAULT;
		}
		String url = "https://api.github.com/repos/"+owner+"/"+repo+"/issues?page="+currentPage+"&per_page="+perPage+"&state="+state;
		return readUrlData( url, info );
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object buildModel( String data, Class c ) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		// jackson 1.9 and before
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Object items = objectMapper.readValue( data , c );
		return items;
	}
	
	public static String getValue( Object val ) {
		String res = null;
		if ( val != null ) {
			res = String.valueOf( val );
		}
		return res;
	}
	
	private static void handleExcel( GithubIssueInfo info, List<List<String>> lines ) throws Exception {
		String xlsFile = info.getProperty( "xls-file" );
		Workbook workbook = new HSSFWorkbook();
		Sheet sheet = workbook.createSheet( "Report github issue" );
		CellStyle headerStyle = PoiHelper.getHeaderStyle( workbook );
		String lang = info.getProperty( ARG_LANG );
		Locale loc = getLocale( lang );
		ResourceBundle headerBundle = ResourceBundle.getBundle( "org.fugerit.java.github.issue.export.config.header-label", loc );
		String[] header = {
				headerBundle.getString( "header.column.id" ),
				headerBundle.getString( "header.column.title" ),
				headerBundle.getString( "header.column.state" ),
				headerBundle.getString( "header.column.labels" ),
				headerBundle.getString( "header.column.assigned" ),
				headerBundle.getString( "header.column.assigned_on" ),
				headerBundle.getString( "header.column.created_by" ),
				headerBundle.getString( "header.column.creation" ),
				headerBundle.getString( "header.column.update" ),
				headerBundle.getString( "header.column.closed" ),
				headerBundle.getString( "header.column.comments_count" ),
				headerBundle.getString( "header.column.url" ),
				headerBundle.getString( "header.column.body" ),
		};
		PoiHelper.addRow( header , 0, sheet, headerStyle );
		int count = 1;
		Iterator<List<String>> itLines = lines.iterator();
		while ( itLines.hasNext() ) {
			List<String> current = itLines.next();
			String[] currentLine = new String[current.size()];
			currentLine = current.toArray( currentLine );
			PoiHelper.addRow( currentLine , count, sheet);
			count++;
		}
		PoiHelper.resizeSheet( sheet );
		logger.info( "Writing xls to file : '"+xlsFile+"'" );
		workbook.close();
		FileOutputStream fos = new FileOutputStream( new File( xlsFile ) );
		workbook.write( fos );
		fos.flush();
		fos.close();
		
	}
		
	public static void main( String[] args ) {
		Properties params = ArgUtils.getArgs( args );
		try {
			handle( params );
		} catch (Exception e) {
			logger.error( e.getMessage(), e );
		}
	}
	
}
