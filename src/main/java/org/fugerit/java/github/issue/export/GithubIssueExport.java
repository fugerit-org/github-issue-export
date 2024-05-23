package org.fugerit.java.github.issue.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.fugerit.java.core.cfg.ConfigException;
import org.fugerit.java.core.cli.ArgUtils;
import org.fugerit.java.core.function.SafeFunction;
import org.fugerit.java.core.lang.helpers.StringUtils;
import org.fugerit.java.github.issue.export.helper.FormatHelper;
import org.fugerit.java.github.issue.export.helper.PoiHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
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

	private GithubIssueExport() {}
	
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
	public static final String ARG_GITHUB_TOKEN = "github-token";
	
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
	private static List<Map> parseJsonData( String data ) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonFactory factory = mapper.getFactory();
		try ( JsonParser jp = factory.createParser( data ) ) {
			JsonNode node = jp.readValueAsTree();
			// data mapping
			ArrayList<Map> issueList = new ArrayList<>();
			issueList = ( ArrayList<Map> )buildModel( String.valueOf( node ), issueList.getClass() );
			return issueList;
		}
	}
	
	protected static void handle( Properties params ) {
		SafeFunction.apply( () -> {
			GithubIssueInfo info = new GithubIssueInfo(params);
			try {
				logger.debug( "params {}", params );
				doHandle( info );
			} finally {
				GithubIssueConfig.getInstance().saveCachePropForRepo( info.getCache() , info.getOwner(), info.getRepo() );
			}	
		} );
	}
		
	private static boolean activeCache( String cacheMode ) {
		return  ARG_ASSIGNEE_DATE_MODE_ALL.equals( cacheMode ) || ARG_ASSIGNEE_DATE_MODE_SKIP_CLOSED.equals( cacheMode );
	}
	
	@SuppressWarnings("rawtypes")
	private static String handleAssignedIssue( Map issue, String issueId, boolean activeCache, GithubIssueInfo info ) throws IOException {
		String assignDate = null;
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
		return assignDate;
	}
	
	@SuppressWarnings("rawtypes")
	private static void handleAssigned( List<String> currentLine, Map issue, String cacheMode, String issueId, String state, GithubIssueInfo info, String lang ) throws IOException, ParseException {
		// assigned
		Map assignee = (Map)issue.get( "assignee" );
		if ( assignee != null ) {
			currentLine.add( String.valueOf( assignee.get( "login" ) ) );
			String assignDate = null;
			boolean activeCache = activeCache( cacheMode);
			if ( activeCache ) {
				assignDate = info.getCacheEntry( issueId , GithubIssueConfig.FIELD_ASSIGN_DATE );
			}
			logger.info( "activeCache : {} - issueId:{} , assign date {}", activeCache, issueId, assignDate );
			if ( assignDate == null ) {
				if ( ARG_STATE_CLOSED.equalsIgnoreCase( state ) && ARG_ASSIGNEE_DATE_MODE_SKIP_CLOSED.equals( cacheMode ) ) {
					// just skip
				} else {
					assignDate = handleAssignedIssue(issue, issueId, activeCache, info);
				}				
			}
			currentLine.add( FormatHelper.formatDate( assignDate, lang ) );
		} else {
			currentLine.add( "-" );
			currentLine.add( "-" );
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static void doHandle( GithubIssueInfo info ) {
		SafeFunction.apply( () -> {
			List<List<String>> lines = new ArrayList<>();
			
			String cacheMode = info.getProperty( GithubIssueExport.ARG_ASSIGNEE_DATE_MODE, GithubIssueExport.ARG_ASSIGNEE_DATE_MODE_SKIP );
			logger.info( "cache-mode : {}", cacheMode );
			
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
				List<String> currentLine = new ArrayList<>();
				String issueId = String.valueOf( issue.get( "number" ) );
				String state = String.valueOf( issue.get( ARG_STATE ) );
				currentLine.add( issueId );
				currentLine.add( String.valueOf( issue.get( "title" ) ) );
				currentLine.add( state );
				// labels
				List labels = (List)issue.get( "labels" );
				if ( labels != null && !labels.isEmpty() ) {
					Iterator itLables = labels.iterator();
					StringBuilder labelList = new StringBuilder();
					while ( itLables.hasNext() ) {
						Map currentLabel = (Map)itLables.next();
						labelList.append( currentLabel.get( "name" ) );
						labelList.append( ", " );
					}
					currentLine.add( labelList.toString() );
				} else {
					currentLine.add( "-" );
				}
				handleAssigned(currentLine, issue, cacheMode, issueId, state, info, lang);
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
		} );
	}

	private static void readUrlDataEnd(StringBuilder buffer, HttpURLConnection conn) throws ConfigException, IOException {
		if ( conn.getResponseCode() != 200 ) {
			throw new ConfigException( "HTTP exit code : "+conn.getResponseCode() );
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
	}
	
	private static String readUrlData( String url, GithubIssueInfo info ) {
		return SafeFunction.get( () -> {
			final String proxyHost = info.getProperty( ARG_PROXY_HOST );
			final String proxyPort = info.getProperty( ARG_PROXY_PORT );
			final String proxyUser = info.getProperty( ARG_PROXY_USER );
			final String proxyPass = info.getProperty( ARG_PROXY_PASS );
			String githubUser = info.getProperty( ARG_GITHUB_USER );
			String githubPass = info.getProperty( ARG_GITHUB_TOKEN, info.getProperty( ARG_GITHUB_PASS ) );	// github_pass is checked for backward compatibility
			logger.info( "connecting to url : {}(user:{})", url, githubUser );
			HttpURLConnection conn;
			if ( !StringUtils.isEmpty( proxyHost ) && !StringUtils.isEmpty( proxyPort ) ) {
				String proxyData = proxyHost+":"+proxyPort;
				logger.debug( "using proxy : {} (user:{})", proxyData, proxyUser );
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt( proxyPort )));
				if ( !StringUtils.isEmpty( proxyUser ) && !StringUtils.isEmpty( proxyPass ) ) {
					Authenticator authenticator = new Authenticator() {
						@Override
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
				// https://github.com/fugerit-org/github-issue-export/issues/22 :
				// the githubPass is used as Bearer token
				// the githuUser is ignored
				if ( StringUtils.isNotEmpty( githubPass ) ) {
					logger.info( "Set bearer token size : {}", githubPass.length() );
					conn.setRequestProperty("Authorization", "Bearer "+githubPass );
				}
			}
			StringBuilder buffer = new StringBuilder();
			readUrlDataEnd(buffer, conn);
			return buffer.toString();
		} );
		
	}
	
	private static String readData( GithubIssueInfo info, int currentPage, int perPage ) {
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
	public static Object buildModel( String data, Class c ) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		// jackson 1.9 and before
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return objectMapper.readValue( data , c );
	}

	private static Workbook createReport( String fileName ) {
		if ( fileName.toLowerCase().endsWith( "xlsx" ) ) {
			return new XSSFWorkbook();
		} else {
			return new HSSFWorkbook();
		}
	}

	private static void handleExcel( GithubIssueInfo info, List<List<String>> lines ) {
		SafeFunction.apply( () -> {
			String xlsFile = info.getProperty( ARG_XLSFILE );
			try ( FileOutputStream fos = new FileOutputStream( new File( xlsFile ) );
					Workbook workbook = createReport( xlsFile ) ) {
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
				logger.info( "Writing xls to file : '{}'", xlsFile );
				workbook.write( fos );
				fos.flush();
			}
		} );

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
