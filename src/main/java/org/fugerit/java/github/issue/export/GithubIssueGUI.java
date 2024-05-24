/**
 * 
 */
package org.fugerit.java.github.issue.export;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import org.fugerit.java.core.function.SafeFunction;
import org.fugerit.java.core.lang.helpers.StringUtils;
import org.fugerit.java.core.util.i18n.XMLResourceBundleControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mttfranci
 *
 */
public class GithubIssueGUI extends JFrame implements WindowListener, ActionListener {

	protected static final Logger logger = LoggerFactory.getLogger(GithubIssueGUI.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7199253866506495111L;
	
	private Properties config;
	
	private JTextField inputProxyHost;
	private JTextField inputProxyPort;
	private JTextField inputProxyUser;
	
	private JPasswordField inputProxyPass;
	private JPasswordField inputRepoPass;
	
	private JTextField inputRepoName;
	private JTextField inputRepoOwner;
	private JTextField inputRepoUser;
	private JTextField inputXlsPath;
	private JTextField inputLocale;
	
	private JComboBox<String> inputStateCombo, inputAssignDateCombo;
	
	private String labelStateOpen;
	private String labelStateClosed;
	private String labelStateAll;

	private String labelAssignDateDefault;
	private String labelAssignDateSkip;
	
	private transient ResourceBundle lagelBundle;
	
	private JButton buttonSaveConfiguration;
	private JButton buttonGenerateReport;
	
	private JTextArea outputArea;
	
	private JMenuItem actionSaveConfigurationMI;
	private JMenuItem helpQuickstartMI;
	private JMenuItem helpInfoMI;
	
	private File configSavePath;
	
	private static JLabel newJLabel( String text, int hAlign ) {
		JLabel label = new JLabel( text );
		configureLayout( label );
		label.setHorizontalAlignment( hAlign );
		Font f = label.getFont();
		f = new Font( f.getFontName(), Font.BOLD, f.getSize() );
		label.setFont( f );
		return label;
	}
	
	private static JLabel newJLabel( String text ) {
		return newJLabel( text, SwingConstants.RIGHT );
	}
	
	private static JTextField newJTextField( String text ) {
		JTextField field = new JTextField( text );
		configureLayout( field );
		return field;
	}
	
	private static void configureLayout( Component c) {
		c.setBackground( new Color( 220, 220, 255 ) );
		c.setForeground( new Color( 0, 0, 80 ) );
	}
	
	private static void addRow( Component c1 , JPanel parent ) {
		configureLayout( c1 );
		parent.add( c1 );
	}
	
	private static JPanel newRowPanel( Component c1, Component c2, JPanel parent ) {
		JPanel panel = new JPanel( new GridLayout( 1, 2 ) );
		configureLayout( c1 );
		configureLayout( c2 );
		configureLayout( panel );
		panel.add( c1 );
		panel.add( c2 );
		parent.add( panel );
		return panel;
	}
	
	private static JMenuItem createMenuItem( String text, JMenu parent, ActionListener il ) {
		JMenuItem mi = new JMenuItem( text );
		parent.add( mi );
		if ( il != null ) {
			mi.addActionListener( il );
		}
		return mi;
	}
	
	/*
	 * Configuration init method
	 */
	private void initConf() {
		this.config = new Properties();
		// handle config file
		this.configSavePath = GithubIssueConfig.getInstance().getMainConfigFile();
		if ( this.configSavePath.exists() ) {
			try ( FileInputStream fis = new FileInputStream( this.configSavePath ) ) {
				this.config.load( fis );
				logger.info( "Config loaded : {}", this.configSavePath );
			} catch (Exception e) {
				logger.warn( "Failed to load configuration "+this.configSavePath, e );
			} 
		} else {
			logger.info( "Config file does not exist : {}" , this.configSavePath );
		}
		// i18n
		String defaultLocale = this.config.getProperty( GithubIssueExport.ARG_LANG, Locale.getDefault().toString() );
		Locale loc = Locale.getDefault();
		if ( !StringUtils.isEmpty( defaultLocale ) ) {
			try {
				loc = Locale.forLanguageTag( defaultLocale );	
			} catch (Exception e) {
				logger.warn( "Errore overriding locale : "+defaultLocale+", using default : "+loc, e );
			}
		}
		this.lagelBundle = ResourceBundle.getBundle( "org.fugerit.java.github.issue.export.config.gui.gui-label_xml", loc, new XMLResourceBundleControl() );
		// create components
		this.outputArea = new JTextArea( this.lagelBundle.getString( "label.output.area.init" ) );
		this.outputArea.setEditable( false );
		String defaultInputText = "";
		// proxy configuration
		this.inputProxyHost = newJTextField( this.config.getProperty( GithubIssueExport.ARG_PROXY_HOST, defaultInputText ) );
		this.inputProxyPort = newJTextField( this.config.getProperty( GithubIssueExport.ARG_PROXY_PORT, defaultInputText ) );
		this.inputProxyUser = newJTextField( this.config.getProperty( GithubIssueExport.ARG_PROXY_USER, defaultInputText ) );
		this.inputProxyPass = new JPasswordField();
		// repository configuration
		this.inputRepoOwner = newJTextField( this.config.getProperty( GithubIssueExport.ARG_OWNER, defaultInputText ) );
		this.inputRepoName = newJTextField( this.config.getProperty( GithubIssueExport.ARG_REPO, defaultInputText ) );
		this.inputRepoUser = newJTextField( this.config.getProperty( GithubIssueExport.ARG_GITHUB_USER, defaultInputText ) );
		String savedPass = this.config.getProperty( GithubIssueExport.ARG_GITHUB_PASS, defaultInputText );
		this.inputRepoPass = new JPasswordField( savedPass );
		this.inputXlsPath = newJTextField( this.config.getProperty( GithubIssueExport.ARG_XLSFILE, defaultInputText ) );
		this.inputLocale = newJTextField( defaultLocale );
		// input state combo
		this.inputStateCombo = new JComboBox<>();
		this.labelStateOpen = this.lagelBundle.getString( "label.input.state.open" );
		this.labelStateClosed = this.lagelBundle.getString( "label.input.state.closed" );
		this.labelStateAll = this.lagelBundle.getString( "label.input.state.all" );
		this.inputStateCombo.addItem( this.labelStateOpen );
		this.inputStateCombo.addItem( this.labelStateClosed );
		this.inputStateCombo.addItem( this.labelStateAll );
		String selectedState = this.config.getProperty( GithubIssueExport.ARG_STATE );
		if ( selectedState != null ) {
			if ( selectedState.equalsIgnoreCase( GithubIssueExport.ARG_STATE_OPEN ) ) {
				this.inputStateCombo.setSelectedItem( this.labelStateOpen );	
			} else if ( selectedState.equalsIgnoreCase( GithubIssueExport.ARG_STATE_CLOSED ) ) {
				this.inputStateCombo.setSelectedItem( this.labelStateClosed );
			} else if ( selectedState.equalsIgnoreCase( GithubIssueExport.ARG_STATE_ALL ) ) {
				this.inputStateCombo.setSelectedItem( this.labelStateAll );
			}
		}
		// input assign date handling combo
		this.inputAssignDateCombo = new JComboBox<>();
		this.labelAssignDateDefault = this.lagelBundle.getString( "label.input.assignDate.default" );
		this.labelAssignDateSkip = this.lagelBundle.getString( "label.input.assignDate.skip" );
		this.inputAssignDateCombo.addItem( this.labelAssignDateDefault );
		this.inputAssignDateCombo.addItem( this.labelAssignDateSkip );
		String selectedStateAssignDate = this.config.getProperty( GithubIssueExport.ARG_ASSIGNEE_DATE_MODE );
		if ( GithubIssueExport.ARG_ASSIGNEE_DATE_MODE_SKIP.equalsIgnoreCase( selectedStateAssignDate ) ) {
			this.inputAssignDateCombo.setSelectedItem( this.labelAssignDateSkip );
		} else {
			this.inputAssignDateCombo.setSelectedItem( this.labelAssignDateDefault );
		}
		// buttons
		this.buttonSaveConfiguration = new JButton( this.lagelBundle.getString( "button.input.configuration.save" ) );
		this.buttonSaveConfiguration.addActionListener( this );
		this.buttonGenerateReport = new JButton( this.lagelBundle.getString( "button.input.generate.report" ) );
		this.buttonGenerateReport.addActionListener( this );
		
	}
	
	/*
	 * Layout
	 */
	private void initLayout() {
		this.setTitle( this.lagelBundle.getString( "frame.title" ) );
		
		// configure layout
		JPanel mainPanel = new JPanel();
		GridLayout ml = new GridLayout( 13, 1 );
		mainPanel.setLayout( ml );
		configureLayout( mainPanel );
		
		// repository config
		// add row
		addRow( newJLabel( this.lagelBundle.getString( "label.input.repo.title" ), SwingConstants.CENTER ), mainPanel );
		// add row
		JPanel repoPanel1 = new JPanel( new GridLayout( 1 , 2 ) );
		newRowPanel( newJLabel( this.lagelBundle.getString( "label.input.repo.owner" ) ), this.inputRepoOwner, repoPanel1 );
		newRowPanel( newJLabel( this.lagelBundle.getString( "label.input.repo.name" ) ), this.inputRepoName, repoPanel1 );
		addRow( repoPanel1 , mainPanel );
		// add row (inputRepoUser now ignored, see issue #22)
		JPanel repoPanel2 = new JPanel( new GridLayout( 1 , 1 ) );
		newRowPanel( newJLabel( this.lagelBundle.getString( "label.input.repo.pass" ) ), this.inputRepoPass, repoPanel2 );
		addRow( repoPanel2 , mainPanel );
		// add row
		addRow( new JLabel( "" ), mainPanel );
		
		// report config
		// add row
		addRow( newJLabel( this.lagelBundle.getString( "label.input.report.title" ), SwingConstants.CENTER ), mainPanel );
		// add row
		JPanel reportPanel1 = new JPanel( new GridLayout( 1 , 2 ) );
		newRowPanel( newJLabel( this.lagelBundle.getString( "label.input.input.language" ) ), this.inputLocale, reportPanel1 );
		newRowPanel( newJLabel( this.lagelBundle.getString( "label.input.state.label" ) ), this.inputStateCombo, reportPanel1 );
		addRow( reportPanel1 , mainPanel );
		// add row
		JPanel reportPanel2 = new JPanel( new GridLayout( 1 , 2 ) );
		newRowPanel( newJLabel( this.lagelBundle.getString( "label.input.output.xls" ) ), this.inputXlsPath, reportPanel2 );
		newRowPanel( newJLabel( this.lagelBundle.getString( "label.input.assignDate.label" ) ), this.inputAssignDateCombo, reportPanel2 );
		addRow( reportPanel2 , mainPanel );
		// add row
		addRow( new JLabel( "" ), mainPanel );

		// proxy field config
		// add row		
		addRow( newJLabel( this.lagelBundle.getString( "label.input.proxy.title" ), SwingConstants.CENTER ), mainPanel );				
		// add row
		JPanel proxyPanel1 = new JPanel( new GridLayout( 1 , 2 ) );
		newRowPanel( newJLabel( this.lagelBundle.getString( "label.input.proxy.host" ) ), this.inputProxyHost, proxyPanel1 );	
		newRowPanel( newJLabel( this.lagelBundle.getString( "label.input.proxy.port" ) ), this.inputProxyPort, proxyPanel1 );
		addRow( proxyPanel1 , mainPanel );
		// add row
		JPanel proxyPanel2 = new JPanel( new GridLayout( 1 , 2 ) );
		newRowPanel( newJLabel( this.lagelBundle.getString( "label.input.proxy.user" ) ), this.inputProxyUser, proxyPanel2 );		
		newRowPanel( newJLabel( this.lagelBundle.getString( "label.input.proxy.pass" ) ), this.inputProxyPass, proxyPanel2 );
		addRow( proxyPanel2 , mainPanel );
		// add row
		addRow( new JLabel( "" ), mainPanel );
		
		// buttons
		// add row
		newRowPanel( this.buttonSaveConfiguration, this.buttonGenerateReport, mainPanel );									
		
		// menu bar
		JMenuBar menubar = new JMenuBar();
		JMenu actionMenu = new JMenu( this.lagelBundle.getString( "label.menu.action.title" ) );
		JMenu helpMenu = new JMenu( this.lagelBundle.getString( "label.menu.help.title" ) );
		this.actionSaveConfigurationMI = createMenuItem( "Salva configurazione" , actionMenu, this );
		this.helpInfoMI = createMenuItem( this.lagelBundle.getString( "label.menu.help.info" ) , helpMenu, this );
		this.helpQuickstartMI = createMenuItem( this.lagelBundle.getString( "label.menu.help.quickstart" ) , helpMenu, this );
		menubar.add( actionMenu );
		menubar.add( helpMenu );
		this.setJMenuBar( menubar );
		
		this.addWindowListener( this );
		
		this.setLayout( new GridLayout( 2 , 1 ) );
		this.add( mainPanel );
		this.add( this.outputArea );
		
		this.setSize( 640 , 480 );
		this.setVisible( true );
	}
	
	private void checkPreset( Properties params, String key, JTextField field ) {
		String value = params.getProperty( key );
		if ( this.config.isEmpty()  && value != null ) {
			field.setText( value );
		}
		logger.info( "key : {} - > {}", key, value );
	}
	
	public GithubIssueGUI( Properties params ) {
		super( "GITHUB ISSUE EXPORT GUI" );
		this.initConf();
		this.checkPreset( params , GithubIssueExportMain.ARG_GUI_PRESET_OWNER,  this.inputRepoOwner );
		this.checkPreset( params , GithubIssueExportMain.ARG_GUI_PRESET_REPO,  this.inputRepoName );
		this.checkPreset( params , GithubIssueExportMain.ARG_GUI_PRESET_PROXY_HOST,  this.inputProxyHost );
		this.checkPreset( params , GithubIssueExportMain.ARG_GUI_PRESET_PROXY_PORT,  this.inputProxyPort );
		String presetCacheMode = params.getProperty( GithubIssueExportMain.ARG_GUI_PRESET_ARG_ASSIGNEE_DATE_MODE );
		if ( presetCacheMode != null ) {
			this.config.setProperty( GithubIssueExport.ARG_ASSIGNEE_DATE_MODE , presetCacheMode );
		}
		this.initLayout();
	}
	
	private JDialog openInfoDialog( String title, String content ) {
		final JDialog frame = new JDialog( this, title, true );
		Thread t = new Thread( () -> {
			Long timeOut = Long.valueOf( System.getProperty( "gui-default-dialog-timeout", "10000" ) );
			logger.info( "dialog timeout : {}", timeOut );
			SafeFunction.apply( () -> Thread.sleep( timeOut ) );
			frame.dispose();
		});
		t.start();
		frame.setSize( 400, 300 );
		frame.setLayout( new GridLayout( 1 , 1 ) );
		JTextPane area = new JTextPane();
		area.setContentType( "text/html" );
		area.setText( content );
		frame.add( area );
		frame.pack();
		frame.setVisible(true);
		return frame;
	}

	protected void pressGenerateButton() {
		this.performMainAction( this.buttonGenerateReport );
	}
	
	protected void pressSaveConfigurationButton() {
		this.performMainAction( this.buttonSaveConfiguration );
	}
	
	protected void pressHelpInfoMI() {
		this.openInfoDialog( this.helpInfoMI.getText() , this.lagelBundle.getString( "label.menu.help.dialog.info" ) );
	}
	
	private void performMainAction( Object source ) {
		this.config.setProperty( GithubIssueExport.ARG_PROXY_HOST , this.inputProxyHost.getText() );
		this.config.setProperty( GithubIssueExport.ARG_PROXY_PORT , this.inputProxyPort.getText() );
		this.config.setProperty( GithubIssueExport.ARG_PROXY_USER , this.inputProxyUser.getText() );
		this.config.setProperty( GithubIssueExport.ARG_OWNER , this.inputRepoOwner.getText() );
		this.config.setProperty( GithubIssueExport.ARG_REPO , this.inputRepoName.getText() );
		this.config.setProperty( GithubIssueExport.ARG_XLSFILE , this.inputXlsPath.getText() );
		this.config.setProperty( GithubIssueExport.ARG_LANG , this.inputLocale.getText() );
		this.config.setProperty( GithubIssueExport.ARG_PROXY_PASS , new String( this.inputProxyPass.getPassword() ) );
		this.config.setProperty( GithubIssueExport.ARG_GITHUB_USER , this.inputRepoUser.getText() );
		this.config.setProperty( GithubIssueExport.ARG_GITHUB_PASS , new String( this.inputRepoPass.getPassword() ) );
		if ( this.inputAssignDateCombo.getSelectedItem() == this.labelAssignDateSkip ) {
			this.config.setProperty( GithubIssueExport.ARG_ASSIGNEE_DATE_MODE , GithubIssueExport.ARG_ASSIGNEE_DATE_MODE_SKIP );
		}
		String selectedState = this.inputStateCombo.getSelectedItem().toString();
		if ( selectedState.equalsIgnoreCase( this.labelStateOpen ) ) {
			this.config.setProperty( GithubIssueExport.ARG_STATE , GithubIssueExport.ARG_STATE_OPEN );	
		} else if ( selectedState.equalsIgnoreCase( this.labelStateClosed ) ) {
			this.config.setProperty( GithubIssueExport.ARG_STATE , GithubIssueExport.ARG_STATE_CLOSED );
		} else if ( selectedState.equalsIgnoreCase( this.labelStateAll ) ) {
			this.config.setProperty( GithubIssueExport.ARG_STATE , GithubIssueExport.ARG_STATE_ALL );
		}
		// skip assign date
		String selectedStateAssignDateMode = this.inputAssignDateCombo.getSelectedItem().toString();
		if ( selectedStateAssignDateMode.equalsIgnoreCase( this.labelAssignDateSkip ) ) {
			this.config.setProperty( GithubIssueExport.ARG_ASSIGNEE_DATE_MODE , GithubIssueExport.ARG_ASSIGNEE_DATE_MODE_SKIP );
		} else {
			this.config.remove( GithubIssueExport.ARG_ASSIGNEE_DATE_MODE );
		}
		if ( source == this.buttonGenerateReport ) {
			if ( StringUtils.isEmpty( this.inputXlsPath.getText() ) ) {
				this.outputArea.setText( this.lagelBundle.getString( "label.output.area.generate.validate.noOutputFile" ) );
			} else {
					String baseText1 = this.lagelBundle.getString( "label.output.area.generate.start" );
					this.outputArea.setText( baseText1 );
					Runnable runExport = newReportExportRun( this );
					Thread t = new Thread( runExport );
					t.start();
			}
		} else if ( source == this.buttonSaveConfiguration || source == this.actionSaveConfigurationMI ) {
			String tempPass1 = this.config.getProperty( GithubIssueExport.ARG_GITHUB_PASS );
			String tempPass2 = this.config.getProperty( GithubIssueExport.ARG_PROXY_PASS );
			this.config.remove( GithubIssueExport.ARG_GITHUB_PASS );
			this.config.remove( GithubIssueExport.ARG_PROXY_PASS );
			try {
				if ( !this.configSavePath.getParentFile().exists() )  {
					this.configSavePath.getParentFile().mkdirs();
				}
				try (FileOutputStream fos = new FileOutputStream( this.configSavePath )) {
					this.config.store( fos , "Config saved on "+new Date() );
				}
				String baseText = this.lagelBundle.getString( "label.output.area.configuration.saved" );
				this.outputArea.setText( baseText+" "+this.configSavePath.getAbsolutePath() );
			} catch (Exception ex) {
				logger.warn( String.format( "Failed to save configuration %s", this.configSavePath ), ex );
			}
			this.config.setProperty( GithubIssueExport.ARG_GITHUB_PASS , tempPass1 );
			this.config.setProperty( GithubIssueExport.ARG_PROXY_PASS , tempPass2 );	
		} else if ( source == this.helpInfoMI ) {
			this.openInfoDialog( this.helpInfoMI.getText() , this.lagelBundle.getString( "label.menu.help.dialog.info" ) );
		} else if ( source == this.helpQuickstartMI ) {
			this.openInfoDialog( this.helpInfoMI.getText() , this.lagelBundle.getString( "label.menu.help.dialog.quickstart" ) );
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		this.performMainAction( e.getSource() );
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent e) {
		// no need to do anything for this event
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent e) {
		if ( e.getSource() == this ) {
			this.setVisible( false );
			this.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent e) {
		// no need to do anything for this event
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent e) {
		// no need to do anything for this event
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	public void windowDeiconified(WindowEvent e) {
		// no need to do anything for this event
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent e) {
		// no need to do anything for this event
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	public void windowDeactivated(WindowEvent e) {
		// no need to do anything for this event
	}
	
	private Runnable newReportExportRun( final GithubIssueGUI gui ) {
		return () -> {
			try {
				GithubIssueExport.handle( gui.config );
				String baseText2 = gui.lagelBundle.getString( "label.output.area.generate.ok" );
				gui.outputArea.setText( baseText2 + new File( gui.inputXlsPath.getText() ).getAbsolutePath() );
			} catch (Exception ex) {
				logger.warn( "Report generation failed "+ex, ex );
				String baseText = gui.lagelBundle.getString( "label.output.area.generate.ko" );
				Throwable t = ex;
				while ( t.getCause() != null ) {
					t = t.getCause();
				}
				gui.outputArea.setText( baseText+t.getMessage() );
			}
		};
	}
	
}

