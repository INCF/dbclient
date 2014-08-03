package CloudConnect;

/*
 * Description		:		Google Summer of Code 2014 Project 
 * Organization		:		International Neuroinformatics Coordinating Facility, Belgian Node
 * Author			:		Atin Mathur (mathuratin007@gmail.com), (atin.mathur@lnmiit.ac.in)
 * Mentor			: 		Dimiter Prodanov
 * Project Title	:		Dropbox Client for ImageJ (Image Processing Software in java)
 * FileName			:		MyCloudJ_.java (package CloudConnect)
 * 							ImageJ Plugin for Dropbox Client. Uses the DbxUtility.java of package DbxUtils to access 
 * 							the User's Dropbox Accounts and perform download and upload actions.
 * 
 * Users			:		Image Processing Researchers (Neuroscientists etc.)
 * Motivation		:		To facilitate the sharing of datasets among ImageJ users
 * Technologies		:		Java, Dropbox Core APIs, Restful Web Services, Swing GUI
 * Installation		:		Put the plugin/MyCloudJ_.jar to the plugins/ folder of the ImageJ. It will show 
 * 							up in the plugins when you run ImageJ.
 * Requirements		:		ImageJ alongwith JRE 1.7 or later.
 * Date				:		19-May-2014
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.dropbox.core.*;

import ij.plugin.*;
import DbxUtils.*;

public class MyCloudJ_ implements PlugIn {
	/*
	 *  Class variables
	 */
	
	/*  
	 *  obj				:	Object of the DbxUtility class. Used to access all the functions of this class. Dropbox APIs are called from this DbxUtility class.
	 *  code			:	Stores the value of the code.
	 *  authorizeUrl	:	Stores the APP Url.
	 *  userStatus		:	Stores if user is connected(1) or not(0). Initialized to 0. 
	 */
	private DbxUtility obj = new DbxUtility();
	private String code;
	private String authorizeUrl;
	private int userStatus = 0;
	 
	
	/*
	 * JTree DbxTree1	:	Stores the complete metadata(path of folders) of Dropbox account. Used to display when user browses to 
	 * 						select the files/folders to Download from.
	 */
	private JTree DbxTree1;
	private DefaultTreeModel treeModel1;
	private DefaultMutableTreeNode root1;
	
	
	/*
	 * JTree DbxTree2	:	Stores the complete metadata(path of folders) of Dropbox account. Used to display when user browses to 
	 * 						select the folders to Upload into.
	 */
	private JTree DbxTree2;
	private DefaultTreeModel treeModel2;
	private DefaultMutableTreeNode root2;
	
	
	/*
	 * User's Dropbox information	: 	Displayed in the text area, if connected.
	 * 
	 * userName						:	Dropbox user name
	 * country						:	Country
	 * userQuota					:	Total size(in GBs) of user's dropbox account 
	 * 
	 */
	private String userName="", country="", userQuota="";
	
	
	/*
	 * String displayInstructions	:	Displayed for new user in the text area.
	 */
	private String displayInstructions="";
	
	
	/*
	 * Variables used during Download/Upload process:
	 * 
	 * Download:
	 * TargetLocalPath		:	Stores the "Target Local Machine Path" where users wants to download data from Dropbox.
	 * FileDbxPath			: 	Stores the "Source Dropbox File Path" which has to be downloaded.
	 * FolderDbxPath		:	Stores the "Source Dropbox Folder Path" which has to be downloaded. Initialized to "/"
	 * 
	 * Note: During a download process, either FileDbxPath or FolderDbxPath is used (Depending on what has to be downloaded). This is just
	 * for easy understanding.
	 * 
	 * Upload:
	 * TargetDbxPath		:	Stores the "Target Dropbox Path" where users wants to upload data from local machine.
	 * FileLocalPath		:	Stores the "Source Local Machine File Path" which has to be uploaded.
	 * FolderLocalPath		:	Stores the "Source Local Machine Folder Path" which has to be uploaded.
	 */
	private String TargetLocalPath = ".", FileDbxPath = "", FolderDbxPath="/";
	private String TargetDbxPath = "/", FileLocalPath = "", FolderLocalPath=".";
	
	
	/*
	 * File		:	Used to Determine whether a path is File or Folder 
	 */
	private int File=0;
	
	
	/*
	 * String variables		:	To store instructions displayed for the user's help.
	 */
	private String heading = "  Instructions : \n \n";
	private String step1 = "  1. Click the \"Access Dropbox !\" button below. It will open the Dropbox app URL in the default browser.\n \n";
	private String step2 = "  2. Next, Sign in to the Dropbox account and allow the MyCloudJ App.\n \n";
	private String step3 = "  3. On clicking the \"Allow\" button, Dropbox will generate an access code.\n \n";
	private String step4 = "  4. Copy the \"access code\" and paste it in the text field below.\n \n";
	private String step5 = "  5. Click the \"Connect !\" button. You can now access Dropbox.\n \n";
	private String note1 = "  Note: Enter the correct access code!";
	
	
	/*
	 * Execution of the plugin begins here.
	 * Function contains the code to generate Graphical User Interface (GUI) for the plugin.
 	 */
    @SuppressWarnings("deprecation")
	public void run(String arg) {
    	/*
		 *  A JFrame that contains the whole GUI for the MyCloudJ_ plugin
		 *  mainFrame	:	contains topPanel1(Left side of the mainFrame) and topPanel2(Right side of the mainFrame)
		 */
		final JFrame mainFrame = new JFrame();
		mainFrame.setLayout(new FlowLayout());
        mainFrame.setTitle("CloudConnect - MyCloudJ");
        mainFrame.setSize(1200,500);
        mainFrame.setResizable(false);
        // This will position the JFrame in the center of the screen
        mainFrame.setLocationRelativeTo(null);
        
        
        /*
         *  JPanels for better alignment of Components in JFrame
         *  topPanel1	: 	Left side of the mainFrame. It will contain: Instructions, Acess Dropbox button, Access Code, Connect button etc.
         *  				In short, it will contains the components used to connect to the dropbox account.
         *  
         *  topPanel2	: 	Right side of the mainFrame. It will contain components used to download/upload from the dropbox account.
         *  
         *  Also set the titled border to topPanel1 and topPanel2
         */
        Border blackline = BorderFactory.createLineBorder(Color.black);
        TitledBorder title1 = BorderFactory.createTitledBorder(blackline, "Dropbox Connect");
        TitledBorder title2 = BorderFactory.createTitledBorder(blackline, "Tasks");
        
        // topPanel1
        final JPanel topPanel1 = new JPanel();
        topPanel1.setLayout(new BoxLayout(topPanel1, BoxLayout.PAGE_AXIS));
        topPanel1.setBorder(title1);
        
        // topPanel2
        final JPanel topPanel2 = new JPanel();
        topPanel2.setLayout(new BoxLayout(topPanel2, BoxLayout.PAGE_AXIS));
        topPanel2.setBorder(title2);
        // will be used for displaying task related information to user (will be added to topPanel2)
        final JTextArea msgs = new JTextArea();
        
        // First we'll add components to topPanel1. Then, we'll start with topPanel2 
        
        /*
         * These panels will add into topPanel1 (Left side of the frame)
         * lPanel1		:	To display Instructions
         * lPanel2		:	Access Dropbox Button
         * lPanel3		:	Access code label, Access Code JTextField and Connect button
         * lPanel4		:	User Status Label: Connected as <username> or Not Connected
         * lPanel5		: 	To display dropbox related information: <username>, <country>  and <user quota(in GBs)>
         * 
         * Note			:	All these panels will be added into topPanel1(left side of the mainFrame)
         */
        JPanel lPanel1 = new JPanel(new FlowLayout());
        JPanel lPanel2 = new JPanel(new FlowLayout());
        JPanel lPanel3 = new JPanel(new FlowLayout());
        JPanel lPanel4 = new JPanel(new FlowLayout());
        JPanel lPanel5 = new JPanel(new FlowLayout());
        
        
        /*
         *  Add JTextArea		:	 This text area is used to display instructions for the new users.
         *  
         *  Added onto panel1
         */
        displayInstructions = heading+step1+step2+step3+step4+step5+note1;
        JTextArea instructions = new JTextArea(displayInstructions);
        instructions.setEditable(false);
        lPanel1.add(instructions);
        
        
        /*
         * Add JButton		:	 "Access the Dropbox button". This is used to open the APP Url in the default browser.
         * 
         * Added onto panel2
         */
        final JButton accessDbxButton = new JButton("Access Dropbox  !");
        lPanel2.add(accessDbxButton);
        
        
        /*
         * Add JLabel		:	"Access Code".
         * 
         * Added onto panel3
         */
        JLabel lbl1;
        lbl1 = new JLabel("Dropbox Access Code: ");
        lPanel3.add(lbl1);
        
        
        /*
         *  Add JTextField		:	This is where user has to paste the Dropbox Access Code. Plugin can only be
         * 					  		connected if user enters the correct "Access Code" and clicks "Connect" button.
         * 					  	
         * 							Added onto panel3
         * 
         * Note					:	Initially disabled.
         */
        final JTextField accessCode = new JTextField(25);
        accessCode.setText(null);
        accessCode.enable(false);
        lPanel3.add(accessCode);
        
        
        /*
         * Add JButton			:	"Connect to Dropbox button". Users need to click this button, once they paste the access code in the 
         * 							textfield. The plugin will be successfully connected to the user's dropbox account, if it enters the correct code.
         * 							Otherwise, an exception is thrown.
         * 							
         * 							Added onto panel3
         * 
         * Note					:	Intially disabled.
         */
        final JButton btnConnect = new JButton("Connect !");
        btnConnect.disable();
        lPanel3.add(btnConnect);
        
        
        /*
         *  Add JLabel for user status -> connected or not connected.
         *  This label will display the connection status of the plugin along with username (if connected)
         *  Status Format	:	Connected as <username> or Not Connected!
         *  					
         *  					Added onto panel4
         *  
         *  Note			:	Intial status "Not Connected !"
         *  
         */
        final JLabel lblStatus = new JLabel("Not Connected !");
        lPanel4.add(lblStatus);
        
        
        /*
         *  Add JTextArea		:		User Information
         *  This text area will display the user information i.e
         *  Format:
         *  
         *  Username:
         *  Country:
         *  Quota:
         *  
         *  Added onto panel5
         */
        final JTextArea userInfo = new JTextArea("\n\n");
        userInfo.setEditable(false);
        lPanel5.add(userInfo);	
        
        
        /*
         *  Event Handling for btnConnect.
         *  This handles the complete set set of events that has to be executed after user presses the "Connect" button.
         */
        btnConnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// retrieve the access code from textfield
					code = accessCode.getText();
					
					// if user is previously not connected and access code is not empty then connect it
					if(userStatus == 0 && !code.equals("")) {
						// connect user to dropbox	
						obj.DbxLinkUser(code);
						
						// user status changed to 1(i.e., connected)
						userStatus = 1;
						
						/*
						 * Retrieve username, country and quota from dropbox account info API and
						 * print it in the text area for the user
						 */
						userName = obj.client.getAccountInfo().displayName;
						country = obj.client.getAccountInfo().country;
						userQuota += (double)obj.client.getAccountInfo().quota.total/(1024*1024*1024);
						lblStatus.setText("Connected as "+userName); 
						userInfo.setText("Username: "+userName+"\nCountry: "+country+"\nQuota: "+userQuota+" GB");
						
						/*
						 * Create the JTree for browsing(to select path for downloading the file/folder)
						 */
						root1 = new DefaultMutableTreeNode("/");
						obj.addChildren(root1,"/");
		        		DbxTree1 = new JTree(root1);
		        		treeModel1 = new DefaultTreeModel(root1);
		        		DbxTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		        		treeModel1.reload(root1);
						
		        		/*
		        		 * Create the JTree for browsing(to select path for uploading the file/folder)
		        		 */
		        		root2 = new DefaultMutableTreeNode("/");
						obj.addChildrenFolder(root2,"/");
		        		DbxTree2 = new JTree(root2);
		        		treeModel2 = new DefaultTreeModel(root2);
		        		DbxTree2.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		        		treeModel2.reload(root2);
		        		
		        		/*
		        		 * Disable the access code textfield and enable the the right panel(which contains the tasks section)
		        		 * after the user is connected.
		        		 */
		        		accessCode.disable();
		        		
		        		// All the components of topPanel2 are enabled after successful connection with user's dropbox account
		        		setEnabledAll(topPanel2, true);
					}
					// If user is already connected userStatus=1, warning for user
					else if(userStatus == 1)
						JOptionPane.showMessageDialog(mainFrame, "Already connected !", "MyCLoudJ - Already Connected", JOptionPane.WARNING_MESSAGE);
					// If user is not connected but there is no access code
					else if(userStatus == 0 && code.equals(""))
						JOptionPane.showMessageDialog(mainFrame, "Enter Access Code !", "MyCLoudJ - Enter Access code", JOptionPane.WARNING_MESSAGE);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(mainFrame, "Access code error - Re-enter the correct access code !\n"+e1.getMessage(), "MyCLoudJ - Access Code Error", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				} catch (DbxException e1) {
					JOptionPane.showMessageDialog(mainFrame, "Access code error - Re-enter the correct access code !\n"+e1.getMessage(), "MyCLoudJ - Access Code Error", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		});
        
        
        /* 
         * Add action listener for Access Dropbox button.
         * This handles the events associated with the "Access Dropbox" button.
         */
        accessDbxButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// if user if not connected, then
				if(userStatus == 0){
					// Generate the authorize URL
					try {
						// Generate dropbox app url
						authorizeUrl = obj.DbxLogin();
					} catch (IOException | DbxException e4) {
						JOptionPane.showMessageDialog(mainFrame, "URL error !\n"+e4.getMessage(), "MyCLoudJ - URL Error", JOptionPane.ERROR_MESSAGE);
						e4.printStackTrace();
					}
					
					// To open the url in the default browser
					obj.openDefaultBrowser(authorizeUrl);
					
					// access code textfield enabled so that user can paste the access code in it and connect
					accessCode.enable(true);
				}
				// If userStatus=1 (is already connected), no need to use connect connect button, warning for user
				else {
					JOptionPane.showMessageDialog(mainFrame, "Already connected !", "MyCLoudJ - Already Connected", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
        
        
        /*
         * Added all the components related to connection in the topPanel1(Left side of the mainFrame). 
         */
        topPanel1.add(lPanel1);
        topPanel1.add(lPanel2);
        topPanel1.add(lPanel3);
        topPanel1.add(lPanel4);
        topPanel1.add(lPanel5);
        
        
        /*
         * Let's start working on topPanel2(Right side of the mainFrame)
         */
        
        /*
         * JPanels		:		To be added to topPanel2(Right side of mainFrame)
         * 
         * rPanel1		:		Contains label "Tasks:"
         * rPanel2		:		Contains Radio buttons "Upload" and "Download".
         * 
         * Added to topPanel2
         */ 
        JPanel rPanel1 = new JPanel(new FlowLayout());
        JPanel rPanel2 = new JPanel(new FlowLayout());
        JPanel rPanel3 = new JPanel(new FlowLayout());
        JPanel rPanel4 = new JPanel(new FlowLayout());
        JPanel rPanel5 = new JPanel(new FlowLayout());
        
        
        /*
         * Add JLabel	:	"Tasks". Download/Upload
         * 
         * Added to rPanel1 
         */
        JLabel lblTasks;
        lblTasks = new JLabel("Tasks: ");
        rPanel1.add(lblTasks);
        
        
        /*
         * JRadioButtons	:	For selecting from one of the two tasks.
         * rButton1			:	For Upload
         * rButton2			:	For Download
         * 
         * Added to rPanel1
         */
        final JRadioButton rButton1 = new JRadioButton("Upload");
        final JRadioButton rButton2 = new JRadioButton("Download", true);
        ButtonGroup group = new ButtonGroup();
        group.add(rButton1);
        group.add(rButton2);
        rPanel1.add(rButton1);
        rPanel1.add(rButton2);
        
        
        /*
         * Add JLabel	: 	"Source".
         * 
         * Added to rPanel2 
         */
        JLabel lblSrc;
        lblSrc = new JLabel("Source: ");
        rPanel2.add(lblSrc);
        
        
        /*
         * JTextField	:	Source address
         * 
         * Added onto rPanel2
         */
        final JTextField srcTxt = new JTextField("",25);
        srcTxt.setEditable(false);
        rPanel2.add(srcTxt);
        
        
        /*
         * JButton		:	Button to open file chooser for the source file
         * 
         * Added onto rPanel2
         */
        JButton btnFileChooser1 = new JButton("Browse");
        rPanel2.add(btnFileChooser1);
        btnFileChooser1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// If user wants to Upload(Upload Radio button is selected), then source is the local machine
				if(rButton1.isSelected()) {
					// JFileChooser opens in current directory(imagej plugins/)
					JFileChooser chooser= new JFileChooser(new File("."));
					
					// Both Files and Directories are allowed
					chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					int choice = chooser.showOpenDialog(chooser);
					if (choice != JFileChooser.APPROVE_OPTION) return;
					// File selected
					File chosenFile = chooser.getSelectedFile();
					
					// The path of the selected file is set in the source textfield
					srcTxt.setText(chosenFile.getAbsolutePath());
				}
				// If user wants to Download(Download Radio button is selected), then source is the Dropbox
				else if(rButton2.isSelected()) {
					/*
	        		 * This JFrame contains Dropbox Jtree for selecting the files/folder
	        		 */
					final JFrame treeFrame = new JFrame();
					BoxLayout boxLayout = new BoxLayout(treeFrame.getContentPane(), BoxLayout.Y_AXIS);
					treeFrame.setLayout(boxLayout);
					
					/*
					 * JPanel for browsing frame
					 * 
					 * Scroll bar added
					 */
					JPanel treePanel = new JPanel();
					JScrollPane scroll = new JScrollPane(treePanel);
					
					/*
					 * JButton
					 * 
					 * Expand	:	expand the folders into subfolders and files.
					 * 
					 * Added onto panel2
					 */
					JPanel panel2 = new JPanel(new FlowLayout());
					JButton Expand = new JButton("Expand");
					panel2.add(Expand);
			        Expand.addActionListener(new ActionListener() {
			        	@Override
			        	public void actionPerformed(ActionEvent e) {
			        		// Parent node is initially null
							DefaultMutableTreeNode parentNode = null;
							
							// Parent path of the currently selected node
						    TreePath parentPath = DbxTree1.getSelectionPath();
						    
						    // get t a the parent node(one in which we have to add children)
						    parentNode = (DefaultMutableTreeNode)(parentPath.getLastPathComponent());
						    
						    // extract the name of the node
						    String parentName = parentNode.toString();
						    
						    // Add child nodes to this node(files and subfolders)
						    obj.addChildren(parentNode, parentName);
						}
					});
			        
			        /*
					 * JButton
					 * 
					 * Select	:	Select the folder/file and set the source textfield with its dropbox path
					 *
					 * Added onto panel2
					 */
			        JButton Select = new JButton("Select");
			        panel2.add(Select);
			        Select.addActionListener(new ActionListener() {
			        	@Override
			        	public void actionPerformed(ActionEvent e) {
			        		// Get the latest node selected
							Object node = DbxTree1.getLastSelectedPathComponent();
							
							// Extract the name from the node and set the source address with its path
						    String name;  
						    name = (node == null) ? "NONE" : node.toString();
						    srcTxt.setText(name);
						    
						    // Extract the metadata of this file/folder
						    DbxEntry metaData=null;
							try {
								// Get the metadata of the name(node selected)
								metaData = obj.client.getMetadata(name);
							} catch (DbxException e1) {
								msgs.append("\n"+e1.getMessage()+"\n");
								e1.printStackTrace();
							}
							
							// If this file, then set File=1(used while calling download function for file)
						    if(metaData.isFile())
								File=1;
						    // If this file, then set File=0(used while calling download function for folder)
							else if(metaData.isFolder())
								File=0;
						    
						    // close the treeFrame
						    treeFrame.dispose();
						}
					});

			        /*
			         * JButton
			         * 
			         * Cancel	:	No action, just close the treeFrame
			         * 
			         * Added onto panel2
			         */
					JButton Cancel = new JButton("Cancel");
			        panel2.add(Cancel);
			        Cancel.addActionListener(new ActionListener() {
			        	@Override
						public void actionPerformed(ActionEvent e) {
			        		// Close the treeFrame
			        		treeFrame.dispose();
						}
					});

			        
			        // This will position the JFrame in the center of the screen
			        treeFrame.setLocationRelativeTo(null);
			        treeFrame.setTitle("Dropbox - Browse!");
			        treeFrame.setSize(350,350);
					
			        // Add DbxTree1(JTree) to this panel and in turn in treeFrame
			        treePanel.add(DbxTree1);
			        treeFrame.add(scroll);
			        treeFrame.add(panel2);
			        treeFrame.setVisible(true);
				}
			}
		});
        
        
        /*
         * Jlabel	 : 	"Target"
         * 
         * Added onto rPanel3
         */
        JLabel targetLbl = new JLabel("Target: ");
        rPanel3.add(targetLbl);
        
        
        /*
         * JTextField	:	Target address
         * 
         * Added onto rPanel3
         */
        final JTextField targetTxt = new JTextField("",25);
        targetTxt.setEditable(false);
        rPanel3.add(targetTxt);
        	
        
        /*
         * JButton	:	Button to open the file chooser for the target file
         * 
         * Added onto rPanel3
         */
        JButton btnFileChooser2 = new JButton("Browse");
        rPanel3.add(btnFileChooser2);
        btnFileChooser2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// If user wants to Upload(Upload Radio button is selected), then target is the Dropbox
				if(rButton1.isSelected()) {
					/*
	        		 * This JFrame contains Dropbox Jtree for selecting the folder for upload
	        		 */
					final JFrame treeFrame = new JFrame();
					BoxLayout boxLayout = new BoxLayout(treeFrame.getContentPane(), BoxLayout.Y_AXIS);
					treeFrame.setLayout(boxLayout);
					
					/*
					 * JPanel for browsing frame
					 * 
					 * Scroll bar added
					 */
					JPanel treePanel = new JPanel();
					JScrollPane scroll = new JScrollPane(treePanel);
					
					/*
					 * JButton
					 * 
					 * Expand	:	expand the folders into subfolders and files.
					 * 
					 * Added onto panel2
					 */
					JPanel panel2 = new JPanel(new FlowLayout());
					JButton Expand = new JButton("Expand");
					panel2.add(Expand);
			        Expand.addActionListener(new ActionListener() {
			        	@Override
			        	public void actionPerformed(ActionEvent e) {
			        		// Parent node is initially null
			        		DefaultMutableTreeNode parentNode = null;
			        		
			        		// Parent path of the currently selected node
						    TreePath parentPath = DbxTree2.getSelectionPath();
						    
						    // get t a the parent node(one in which we have to add children)
						    parentNode = (DefaultMutableTreeNode)(parentPath.getLastPathComponent());
						    
						    // extract the name of the node
						    String parentName = parentNode.toString();
						    
						    // Add child nodes to this node(only subfolders)
						    obj.addChildrenFolder(parentNode, parentName);
						}
					});
			        
			        /*
					 * JButton
					 * 
					 * Select	:	Select the folder and set the source textfield with its dropbox path
					 *
					 * Added onto panel2
					 */
			        JButton Select = new JButton("Select");
			        panel2.add(Select);
			        Select.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							Object node = DbxTree2.getLastSelectedPathComponent();  
						    String name;  
						    name = (node == null) ? "NONE" : node.toString();
						    targetTxt.setText(name);
						    treeFrame.dispose();
						}
					});

			        /*
			         * JButton
			         * 
			         * Cancel	:	No action, just close the treeFrame
			         * 
			         * Added onto panel2
			         */
					JButton Cancel = new JButton("Cancel");
			        panel2.add(Cancel);
			        Cancel.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							// Close the treeFrame
							treeFrame.dispose();
						}
					});

			        
			        // This will position the JFrame in the center of the screen
			        treeFrame.setLocationRelativeTo(null);
			        treeFrame.setTitle("Dropbox - Browse!");
			        treeFrame.setSize(350,350);
					
			        // Add DbxTree2(JTree) to this panel and in turn in treeFrame
			        treePanel.add(DbxTree2);
			        treeFrame.add(scroll);
			        treeFrame.add(panel2);
			        treeFrame.setVisible(true);
				}
				// If user wants to Download(Download Radio button is selected), then target is the local machine
				else if(rButton2.isSelected()) {
					// JFileChooser opens in current directory(imagej plugins/)	
					JFileChooser chooser= new JFileChooser(new File("."));
					
					// Only Directories are allowed
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int choice = chooser.showOpenDialog(chooser);
					if (choice != JFileChooser.APPROVE_OPTION) return;
					// File selected
					File chosenFile = chooser.getSelectedFile();
					
					// The path of the selected file is set in the target textfield and set to non-editable
					targetTxt.setText(chosenFile.getAbsolutePath());
					targetTxt.setEditable(false);
				}
			}
		});
        
        
        /*
         * Set source/target address to "".
         */
        rButton1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// intialize source address
				srcTxt.setText("");
				
				// initialize target address
				targetTxt.setText("");
			}
		});
        
        rButton2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// intialize source address
				srcTxt.setText("");
				
				// initialize target address
				targetTxt.setText("");
			}
		});
        
        
        /*
         * JButton	:	Button to start download/upload
         * 
         * Added onto rPanel4
         */
        JButton btnStart = new JButton("Start !");
        rPanel4.add(btnStart);
        btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// If source or target is empty, take no action and inform the user about the same
				if(srcTxt.getText().equals("") || targetTxt.getText().equals("")) {
					msgs.append("\nSelect the files/folder to upload/download");
					return;
				}
				
				// If user wants to upload, this will be executed othewise else block will execute
				if(rButton1.isSelected()) {
					// open the file selected by the user
					File file = new File(srcTxt.getText());
					
					// Print the uploading information for the user in the text area
					SwingUtilities.invokeLater(new Runnable() {
					    public void run() {
					      // Your Code Here
					    	msgs.append("\nUploading "+srcTxt.getText()+" to Dropbox"+"\n");
					    }
					});
					
					// Checks if selected path is of a file/folder, if file, then execute IF block
					if(file.isFile()) {
						try {
							// Store the file path 
							FileLocalPath = srcTxt.getText();
							
							// if windows OS, then change the separator from \\ to /
							String newFileLocalPath = FileLocalPath.replace('\\', '/');
							
							// Retrieve the filename from the path
							String fileName = newFileLocalPath.substring(newFileLocalPath.lastIndexOf("/"));
							
							// Target path in Dropbox folder
							TargetDbxPath = targetTxt.getText();
							
							// Append the filename at the end of the target path
							TargetDbxPath += fileName;
							
							// Call the upload function of DbxUtility class which in turn calls Dropbox API for upload function
							obj.DbxUploadFile(FileLocalPath, TargetDbxPath);
														
							// Message once the upload is complete
							msgs.append("Upload Complete !\n");
							//JOptionPane.showMessageDialog(mainFrame, "Upload Complete !", "MyCLoudJ - Upload Complete", JOptionPane.INFORMATION_MESSAGE);
							} catch (IOException | DbxException e1) {
								msgs.append("\n"+e1.getMessage()+"\n");
								//JOptionPane.showMessageDialog(mainFrame, "Upload Aborted !\n"+e1.getMessage(), "MyCLoudJ - Upload Aborted", JOptionPane.ERROR_MESSAGE);
								e1.printStackTrace();
							}
					}
					// If selected path is a directory, execute ELSE-IF block
					else if (file.isDirectory()) {
							try {
								// Store the Local folder's path
								FolderLocalPath = srcTxt.getText();
								
								// Store the Target Dropbox's path
								TargetDbxPath = targetTxt.getText();
								
								// Call the upload function
								obj.DbxUploadFolder(FolderLocalPath, TargetDbxPath);
								
								// Message, once the Upload is complete
								msgs.append("Upload Complete !\n");
								// JOptionPane.showMessageDialog(mainFrame, "Upload Complete !", "MyCLoudJ - Upload Complete", JOptionPane.INFORMATION_MESSAGE);
							} catch (IOException | DbxException e1) {
								msgs.append("\n"+e1.getMessage()+"\n");
								//JOptionPane.showMessageDialog(mainFrame, "Upload Aborted !\n"+e1.getMessage(), "MyCLoudJ - Upload Aborted", JOptionPane.ERROR_MESSAGE);
								e1.printStackTrace();
							}
					}

					// Code for Opening the file/folder after upload in default application
					try {
						Desktop.getDesktop().open(file);
					} catch (IOException e1) {
						msgs.append("\n"+e1.getMessage()+"\n");
						e1.printStackTrace();
					}
				}
				// If user wants to upload, ELSE block will be executed
				else if(rButton2.isSelected()) {
					// Stores the target path on Local machine
					TargetLocalPath = targetTxt.getText();

					// Print the downloading information for the user in the text area
					SwingUtilities.invokeLater(new Runnable() {
					    public void run() {
					      // Your Code Here
					    	msgs.append("\nDownloading "+srcTxt.getText()+" from Dropbox"+"\n");
					    }
					});
					
					// If the path is file, then execute this
					if(File==1) {
						try {
							// Store the Dropbox file path
							FileDbxPath = srcTxt.getText();
							
							// Call the download function of the DbxUtility class
							obj.DbxDownloadFile(FileDbxPath, TargetLocalPath);
							
							// Message for Download complete
							msgs.append("Download Complete !\n");
							// JOptionPane.showMessageDialog(mainFrame, "Download Complete !", "MyCLoudJ - Download Complete", JOptionPane.INFORMATION_MESSAGE);
							} catch (IOException | DbxException e1) {
								msgs.append("\n"+e1.getMessage()+"\n");
								//JOptionPane.showMessageDialog(mainFrame, "Download Aborted !\n"+e1.getMessage(), "MyCLoudJ - Download Aborted", JOptionPane.ERROR_MESSAGE);
								e1.printStackTrace();
							}
					}
					// If the path is a directory. execute this
					else if (File==0) {
						try {
							// Store the Dropbox folder path
							FolderDbxPath = srcTxt.getText();
							
							// Call the download folder function of the DbXUtility class
							obj.DbxDownloadFolder(FolderDbxPath, TargetLocalPath);
							
							// Message for Download Complete
							msgs.append("Download Complete !\n");
							// JOptionPane.showMessageDialog(mainFrame, "Download Complete !", "MyCLoudJ - Download Complete", JOptionPane.INFORMATION_MESSAGE);
						} catch (DbxException e1) {
							msgs.append("\n"+e1.getMessage()+"\n");
							//JOptionPane.showMessageDialog(mainFrame, "Download Aborted !\n"+e1.getMessage(), "MyCLoudJ - Download Aborted", JOptionPane.ERROR_MESSAGE);
							e1.printStackTrace();
						} 
					}

					/*
					 *  To open the file/folder which is downloaded from Dropbox
					 *  
					 *  DbxPath.getName(path)	:	Returns just the last component of the path.
					 *  							For Ex:
					 *  							getName("/") returns "/"
					 *  							getName("/Photos") returns "Photos"
					 *  							getName("/Photos/Home.jpeg") returns "Home.jpeg"	
					 */
					String openFile = srcTxt.getText();
					String lastPart = DbxPath.getName(openFile);
					
					// If OS is windows, the path separator is '\' else '/'
					if(obj.OS.contains("windows")) {
						lastPart = "\\"+lastPart;
					}
					else {
						lastPart = "/"+lastPart;
					}
					
					// Append the filename to Target local path
					openFile = TargetLocalPath+lastPart;
					File openFiles = new File(openFile);
					try {
						// open the file in the default application
						Desktop.getDesktop().open(openFiles);
					} catch (IOException e1) {
						msgs.append("\n"+e1.getMessage()+"\n");
						e1.printStackTrace();
					}
				}
			}
		});
        
        
        /*
         * JLabel	:	"Messages"
         * 
         * Added onto rPanel5
         */
        JLabel lblMsg = new JLabel("Messages: ");
        rPanel5.add(lblMsg);
        
        /*
         * JTextArea	:	For user's information (task related)
         * 
         * Added onto rPanel5
         */
        msgs.setLineWrap(true);
        msgs.setWrapStyleWord(true);
        
        // Added the scrollpane to text area
        JScrollPane msgsScrollPane = new JScrollPane(msgs);
        msgsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        msgsScrollPane.setPreferredSize(new Dimension(340, 220));
        rPanel5.add(msgsScrollPane);
        
        
        /*
         * Add rPanels to topPanel2(right side)
         */
        topPanel2.add(rPanel1);
        topPanel2.add(rPanel2);
        topPanel2.add(rPanel3);
        topPanel2.add(rPanel4);
        topPanel2.add(rPanel5);
      
        // Initially all the components of topPanel2 are disabled. It is enabled after successful connection with user's dropbox account
        setEnabledAll(topPanel2, false);
        
        /*
         * Add the topPanel1(Left side) and topPanel2(Right side) to mainFrame.
         * Also set mainFrame  visible
         */
        mainFrame.add(topPanel1);
        mainFrame.add(topPanel2);
        mainFrame.setVisible(true);
        
    // End of the run() method
    }
    
    
    /*
     * Function to enable/disable components inside a container(works for Nested containers)
     * 
     * Parameters:
     * Container container		:	container which you have to disable/enable
     * enabled					:	boolean value, true(enable) or false(disable)
     */
    public void setEnabledAll(Container container, boolean enabled) {
    	   Component[] components = container.getComponents();
    	   if (components.length > 0) {
    	      for (Component component : components) {
    	         component.setEnabled(enabled);
    	         if (component instanceof Container) { // has to be a container to contain components
    	            setEnabledAll((Container)component, enabled); // the recursive call
    	         }
    	      }
    	   }
    // End of setEnabledAll() method
    }
    
// End of the MyCloudJ_ class
}