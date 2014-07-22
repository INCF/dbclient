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
 * Motivation		:		To facilitate the sharing of datasets on among ImageJ users
 * Technologies		:		Java, Dropbox Core APIs, Restful Web Services, Swing GUI
 * Installation		:		Put the plugin/MyCloudJ_.jar to the plugins/ folder of the ImageJ. It will show 
 * 							up in the plugins when you run ImageJ.
 * Requirements		:		ImageJ alongwith JRE 1.7 or later.
 * Date				:		19-May-2014
 */


import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.dropbox.core.*;

import ij.plugin.*;
import DbxUtils.*;

public class MyCloudJ_ implements PlugIn {
	
	/*
	 *  class variables :  For easy access throughout the plugin source code.
	 */
	private DbxUtility obj = new DbxUtility();
	private String code;
	private String authorizeUrl;
	private int userStatus = 0;
	
	/*
	 * JTree for Downloading from Dropbox
	 */
	private JTree DbxTree1;
	private DefaultTreeModel treeModel1;
	private DefaultMutableTreeNode root1;

	/*
	 * JTree for Uploading to Dropbox
	 */
	private JTree DbxTree2;
	private DefaultTreeModel treeModel2;
	private DefaultMutableTreeNode root2;

	
	private String userName="", country="", userQuota="";
	private String displayInstructions="";
	private String TargetLocalPath = ".", TargetDbxPath = "/";
	private String FileDbxPath = "", FolderDbxPath="/";
	private String FileLocalPath = "", FolderLocalPath=".";
	private int File=0;

	
	/*
	 * String variables	:	To store instructions displayed for the user's help.
	 */
	private String heading = "  Instructions : \n \n";
	private String step1 = "  1. Click the \"Access Dropbox !\" button below. It will open the Dropbox app URL in the default browser.\n \n";
	private String step2 = "  2. Next, Sign in to the Dropbox account and allow the MyCloudJ App.\n \n";
	private String step3 = "  3. On clicking the \"Allow\" button, Dropbox will generate an access code.\n \n";
	private String step4 = "  4. Copy the \"access code\" and paste it in the text field below.\n \n";
	private String step5 = "  5. Click the \"Connect !\" button. You can now access Dropbox.\n \n";
	private String note1 = "  Note: Enter the correct access code!";
	
	
	/*
	 * (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 * Execution of the plugin begins here.
	 * Function contains the code to generate Graphical User Interface (GUI) for the plugin.
 	 */
    @SuppressWarnings("deprecation")
	public void run(String arg) {
		/*
		 *  A JFrame that contains the whole GUI for the MyCloudJ_ plugin
		 */
		final JFrame mainFrame = new JFrame();
		BoxLayout boxLayout = new BoxLayout(mainFrame.getContentPane(), BoxLayout.Y_AXIS);
		mainFrame.setLayout(boxLayout);
		mainFrame.setMaximumSize(new Dimension(700,500));
        mainFrame.setTitle("CloudConnect - MyCloudJ");
        mainFrame.setSize(700,500);
        
        
        /*
         * This will position the JFrame in the center of the screen
         */
        mainFrame.setLocationRelativeTo(null);
        
        
        /* 
         * To Create the Menu bar : Actions	:	a. Download, b. Upload
         */
        JMenuBar menubar = new JMenuBar();
        
        final JMenu actionsMenu = new JMenu("Actions");
        actionsMenu.add(new JSeparator());
        actionsMenu.disable();
        
        JMenuItem actionItem1 = new JMenuItem("Download");
        JMenuItem actionItem2 = new JMenuItem("Upload");
        
        actionsMenu.add(actionItem1);
        actionsMenu.add(actionItem2);
        
        menubar.add(actionsMenu);
        mainFrame.setJMenuBar(menubar);
  
        
        /*
         *  JPanels for better alignment of Components in JFrame
         */
        JPanel panel1 = new JPanel(new FlowLayout());
        JPanel panel2 = new JPanel(new FlowLayout());
        JPanel panel3 = new JPanel(new FlowLayout());
        JPanel panel4 = new JPanel(new FlowLayout());
        JPanel panel5 = new JPanel(new FlowLayout());
        
        
        /*
         *  Add JTextArea -> Instructions for the user
         */
        displayInstructions = heading+step1+step2+step3+step4+step5+note1;
        JTextArea instructions = new JTextArea(displayInstructions);
        instructions.setEditable(false);
        panel1.add(instructions);

        
        /*
         * Add JButton -> Access the Dropbox! button
         */
        final JButton accessDbxButton = new JButton("Access Dropbox  !");
        panel2.add(accessDbxButton);
        
        
        /*
         * Add JLabel -> Access Code
         */
        JLabel lbl1;
        lbl1 = new JLabel("Dropbox Access Code: ");
        panel3.add(lbl1);
        
        
        /*
         *  Add JTextField -> Users will have to paste the copied Access code here
         */
        final JTextField accessCode = new JTextField(25);
        accessCode.setText(null);
        accessCode.enable(false);
        panel3.add(accessCode);
        
        
        /*
         * Add JButton -> Connect to Dropbox button
         */
        final JButton btnConnect = new JButton("Connect !");
        btnConnect.disable();
        panel3.add(btnConnect);
        
        
        /*
         *  Add JLabel for user status -> connected or not connected
         */
        final JLabel lblStatus = new JLabel("Not Connected !");
        panel4.add(lblStatus);

        
        /*
         *  Add JTextArea -> User Information
         */
        final JTextArea userInfo = new JTextArea("");
        userInfo.setEditable(false);
        panel5.add(userInfo);	
        

        /*
         *  Event Handling for btnConnect
         */
        btnConnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					code = accessCode.getText();
					if(userStatus == 0 && !code.equals("")) {
					obj.DbxLinkUser(code);
					userStatus = 1;
					userName = obj.client.getAccountInfo().displayName;
					country = obj.client.getAccountInfo().country;
					userQuota += (double)obj.client.getAccountInfo().quota.total/(1024*1024*1024);
					lblStatus.setText("Connected as "+userName);
					userInfo.setText("Username: "+userName+"\nCountry: "+country+"\nQuota: "+userQuota+" GB");
					
					/*
					 * Create the JTree for Download purpose
					 */
					root1 = new DefaultMutableTreeNode("/");
					addChildren(root1,"/");
	        		DbxTree1 = new JTree(root1);
	        		treeModel1 = new DefaultTreeModel(root1);
	        		DbxTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	        		treeModel1.reload(root1);
					
	        		/*
	        		 * Create the JTree for Upload purpose
	        		 */
	        		root2 = new DefaultMutableTreeNode("/");
					addChildrenFolder(root2,"/");
	        		DbxTree2 = new JTree(root2);
	        		treeModel2 = new DefaultTreeModel(root2);
	        		DbxTree2.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	        		treeModel2.reload(root2);

	        		
	        		accessCode.disable();
					actionsMenu.enable();
					}
					else if(userStatus == 1)
						JOptionPane.showMessageDialog(mainFrame, "Already connected !", "MyCLoudJ - Already Connected", JOptionPane.WARNING_MESSAGE);
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
         */
        accessDbxButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(userStatus == 0){
					/*
					 *  Generate the authorize URL
					 */
					try {
						authorizeUrl = obj.DbxLogin();
					} catch (IOException | DbxException e4) {
						// TODO Auto-generated catch block
						e4.printStackTrace();
					}
					// Instructions for the User are in stepsToAuthorize.
					obj.openDefaultBrowser(authorizeUrl);
					accessCode.enable(true);
				}
				else {
					JOptionPane.showMessageDialog(mainFrame, "Already connected !", "MyCLoudJ - Already Connected", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
      
        
        /*
         * Add action listener for downloading the File/Folder from the user's Dropbox Account. (Actions -> Download)
         */
        actionItem1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				 * Inner JFrame : Download Frame
				 */
				final JFrame downloadFrame = new JFrame();
				BoxLayout boxLayout = new BoxLayout(downloadFrame.getContentPane(), BoxLayout.Y_AXIS);
				downloadFrame.setLayout(boxLayout);
				
				
				 /*
		         * This will position the JFrame in the center of the screen
		         */
		        downloadFrame.setLocationRelativeTo(null);
		        
				downloadFrame.setMaximumSize(new Dimension(500,250));
		        downloadFrame.setTitle("CloudConnect - MyCloudJ: Download");
		        downloadFrame.setSize(500,250);
		        
		        
		        /*
		         * Inner JPanels for frame2
		         */
		        JPanel innerPanel1 = new JPanel(new FlowLayout());
		        JPanel innerPanel2 = new JPanel(new FlowLayout());
		        JPanel innerPanel3 = new JPanel(new FlowLayout());
		        JPanel innerPanel4 = new JPanel(new FlowLayout());
		        
		        
		        /*
		         * 
		         */
		        /*JLabel type = new JLabel("Type: ");
		        innerPanel1.add(type);*/
		        
		        
		        /*
		         * JRadioButtons -> For selecting the action on File or Folder.
		         */
		        /*final JRadioButton rButton1 = new JRadioButton("File");
		        final JRadioButton rButton2 = new JRadioButton("Folder", true);
		        ButtonGroup group = new ButtonGroup();
		        group.add(rButton1);
		        group.add(rButton2);
		        innerPanel1.add(rButton1);
		        innerPanel1.add(rButton2);*/
		        
		        
		        /*
		         * Jlabel -> Source
		         */
		        JLabel srcLbl = new JLabel("Source: ");
		        innerPanel2.add(srcLbl);
		        
		        
		        /*
		         * JTextField -> source address : Dropbox address -> currently user will have to enter it manually
		         */
		        final JTextField srcTxt = new JTextField(FolderDbxPath,25);
		        innerPanel2.add(srcTxt);
		        
		        
		        /*
		         * JButton -> Button to open file chooser for the source file
		         */
		        JButton btnFileChooser1 = new JButton("Browse");
		        innerPanel2.add(btnFileChooser1);
		        btnFileChooser1.addActionListener(new ActionListener() {
		        	@Override
					public void actionPerformed(ActionEvent e) {
		        		/*
		        		 * This JFrame contains value of the Jtree
		        		 */
						final JFrame treeFrame = new JFrame();
						BoxLayout boxLayout = new BoxLayout(treeFrame.getContentPane(), BoxLayout.Y_AXIS);
						treeFrame.setLayout(boxLayout);
						
						/*
						 * This panel contains the value of the JPanel
						 */
						JPanel treePanel = new JPanel();
						JScrollPane scroll = new JScrollPane(treePanel);
						
						/*
						 * JButtons
						 */
						JPanel panel2 = new JPanel(new FlowLayout());
						JButton Expand = new JButton("Expand");
						panel2.add(Expand);
				        Expand.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								DefaultMutableTreeNode parentNode = null;
							    TreePath parentPath = DbxTree1.getSelectionPath();
							    parentNode = (DefaultMutableTreeNode)(parentPath.getLastPathComponent());
							    String parentName = parentNode.toString();
							    addChildren(parentNode, parentName);
							}
						});
				        
				        
				        JButton Select = new JButton("Select");
				        panel2.add(Select);
				        Select.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								Object node = DbxTree1.getLastSelectedPathComponent();  
							    String name;  
							    name = (node == null) ? "NONE" : node.toString();
							    srcTxt.setText(name);
							    
							    DbxEntry metaData=null;
								try {
									metaData = obj.client.getMetadata(name);
								} catch (DbxException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								
							    if(metaData.isFile())
									File=1;
								else if(metaData.isFolder())
									File=0;
							    treeFrame.dispose();
							}
						});

				        
						JButton Cancel = new JButton("Cancel");
				        panel2.add(Cancel);
				        Cancel.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								treeFrame.dispose();
							}
						});

				        
						/*
				         * This will position the JFrame in the center of the screen
				         */
				        treeFrame.setLocationRelativeTo(null);
				        treeFrame.setTitle("Dropbox - Browse!");
				        treeFrame.setSize(400,400);
						
				        treePanel.add(DbxTree1);
				        treeFrame.add(scroll);
				        treeFrame.add(panel2);
				        treeFrame.setVisible(true);
					}
				});
		        
		        
		        /*
		         * Jlabel -> Target
		         */
		        JLabel targetLbl = new JLabel("Target: ");
		        innerPanel3.add(targetLbl);
		        
		        
		        /*
		         * JTextField -> source address : Dropbox address -> currently user will have to enter it manually
		         */
		        final JTextField targetTxt = new JTextField(TargetLocalPath,25);
		        innerPanel3.add(targetTxt);
		        	
		        
		        /*
		         * JButton -> Button to open the file chooser for the target file
		         */
		        JButton btnFileChooser2 = new JButton("Browse");
		        innerPanel3.add(btnFileChooser2);
		        btnFileChooser2.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						JFileChooser chooser= new JFileChooser(new File("."));
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						int choice = chooser.showOpenDialog(chooser);
						if (choice != JFileChooser.APPROVE_OPTION) return;
						File chosenFile = chooser.getSelectedFile();
						targetTxt.setText(chosenFile.getAbsolutePath());
						targetTxt.setEditable(false);
					}
				});
		        
		        
		        /*
		         * JButton -> To Download the File/Folder from the Source to Target Destination
		         */
		        JButton btnDownload = new JButton("Download");
		        innerPanel4.add(btnDownload);
		        btnDownload.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						TargetLocalPath = targetTxt.getText();
						if(File==1) {
							try {
								FileDbxPath = srcTxt.getText();
								obj.DbxDownloadFile(FileDbxPath, TargetLocalPath);
								JOptionPane.showMessageDialog(downloadFrame, "Download Complete !", "MyCLoudJ - Download Complete", JOptionPane.INFORMATION_MESSAGE);
								} catch (IOException | DbxException e1) {
									// TODO Auto-generated catch block
									JOptionPane.showMessageDialog(downloadFrame, "Download Aborted !\n"+e1.getMessage(), "MyCLoudJ - Download Aborted", JOptionPane.ERROR_MESSAGE);
									e1.printStackTrace();
								}
						}
						else if (File==0) {
							try {
								FolderDbxPath = srcTxt.getText();
								obj.DbxDownloadFolder(FolderDbxPath, TargetLocalPath);
								JOptionPane.showMessageDialog(downloadFrame, "Download Complete !", "MyCLoudJ - Download Complete", JOptionPane.INFORMATION_MESSAGE);
							} catch (DbxException e1) {
								// TODO Auto-generated catch block
								JOptionPane.showMessageDialog(downloadFrame, "Download Aborted !\n"+e1.getMessage(), "MyCLoudJ - Download Aborted", JOptionPane.ERROR_MESSAGE);
								e1.printStackTrace();
							} 
						}
					}
				});
		        
		        /*
		         * This will position the JFrame in the center of the screen
		         */
		        downloadFrame.setLocationRelativeTo(null);
		        
		        downloadFrame.add(innerPanel1);
		        downloadFrame.add(innerPanel2);
		        downloadFrame.add(innerPanel3);
		        downloadFrame.add(innerPanel4);
		        
		        downloadFrame.setVisible(true);
			}
		});
        
        
        /*
         * Add action listener for downloading the File from the user's Dropbox Account. (Actions -> Download)
         */
        actionItem2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				 * Inner JFrame	:	Upload Frame
				 */
				final JFrame uploadFrame = new JFrame();
				BoxLayout boxLayout = new BoxLayout(uploadFrame.getContentPane(), BoxLayout.Y_AXIS);
				uploadFrame.setLayout(boxLayout);
				
				
				 /*
		         * This will position the JFrame in the center of the screen
		         */
		        uploadFrame.setLocationRelativeTo(null);
		        
		        uploadFrame.setMaximumSize(new Dimension(500,250));
		        uploadFrame.setTitle("CloudConnect - MyCloudJ: Upload");
		        uploadFrame.setSize(500,250);
		        
		        
		        /*
		         * Inner JPanels for frame2
		         */
		        JPanel innerPanel1 = new JPanel(new FlowLayout());
		        JPanel innerPanel2 = new JPanel(new FlowLayout());
		        JPanel innerPanel3 = new JPanel(new FlowLayout());
		        JPanel innerPanel4 = new JPanel(new FlowLayout());
		        
		        
		        /*
		         * 
		         
		        JLabel type = new JLabel("Type: ");
		        innerPanel1.add(type);
		        
		        
		        
		         * JRadioButtons -> For selecting the action on File or Folder.
		         
		        final JRadioButton rButton1 = new JRadioButton("File");
		        final JRadioButton rButton2 = new JRadioButton("Folder", true);
		        ButtonGroup group = new ButtonGroup();
		        group.add(rButton1);
		        group.add(rButton2);
		        innerPanel1.add(rButton1);
		        innerPanel1.add(rButton2);*/
		        
		        
		        /*
		         * Jlabel -> Source
		         */
		        JLabel srcLbl = new JLabel("Source: ");
		        innerPanel2.add(srcLbl);
		        
		        
		        /*
		         * JTextField -> source address : Local Machine Address-> currently user will have to enter it manually
		         */
		        final JTextField srcTxt = new JTextField(FolderLocalPath, 25);
		        innerPanel2.add(srcTxt);
		        
		        
		        /*
		         * JButton -> Button to open file chooser for the source file
		         */
		        JButton btnFileChooser1 = new JButton("Browse");
		        innerPanel2.add(btnFileChooser1);
		        
		        
		        /*
		         * Jlabel -> Target
		         */
		        JLabel targetLbl = new JLabel("Target: ");
		        innerPanel3.add(targetLbl);
		        
		        
		        /*
		         * JTextField -> source address : Dropbox address -> currently user will have to enter it manually
		         */
		        final JTextField targetTxt = new JTextField(TargetDbxPath,25);
		        innerPanel3.add(targetTxt);
		        
		        
		        /*
		         * JButton -> Button to open the file chooser for the target file
		         */
		        JButton btnFileChooser2 = new JButton("Browse");
		        innerPanel3.add(btnFileChooser2);
		        btnFileChooser2.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						/*
		        		 * This JFrame contains value of the Jtree
		        		 */
						final JFrame treeFrame = new JFrame();
						BoxLayout boxLayout = new BoxLayout(treeFrame.getContentPane(), BoxLayout.Y_AXIS);
						treeFrame.setLayout(boxLayout);
						
						/*
						 * This panel contains the value of the JPanel
						 */
						JPanel treePanel = new JPanel();
						JScrollPane scroll = new JScrollPane(treePanel);
						
						/*
						 * JButtons
						 */
						JPanel panel2 = new JPanel(new FlowLayout());
						JButton Expand = new JButton("Expand");
						panel2.add(Expand);
				        Expand.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								DefaultMutableTreeNode parentNode = null;
							    TreePath parentPath = DbxTree2.getSelectionPath();
							    parentNode = (DefaultMutableTreeNode)(parentPath.getLastPathComponent());
							    String parentName = parentNode.toString();
							    addChildrenFolder(parentNode, parentName);
							}
						});
				        
				        
				        JButton Select = new JButton("Select");
				        panel2.add(Select);
				        Select.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								Object node = DbxTree2.getLastSelectedPathComponent();  
							    String name;  
							    name = (node == null) ? "NONE" : node.toString();
							    targetTxt.setText(name);
							    treeFrame.dispose();
							}
						});

				        
						JButton Cancel = new JButton("Cancel");
				        panel2.add(Cancel);
				        Cancel.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								treeFrame.dispose();
							}
						});

				        
						/*
				         * This will position the JFrame in the center of the screen
				         */
				        treeFrame.setLocationRelativeTo(null);
				        treeFrame.setTitle("Dropbox - Browse!");
				        treeFrame.setSize(400,400);
						
				        treePanel.add(DbxTree2);
				        treeFrame.add(scroll);
				        treeFrame.add(panel2);
				        treeFrame.setVisible(true);
					}
				});
		        
		        btnFileChooser1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						JFileChooser chooser= new JFileChooser(new File("."));
						/*if(rButton2.isSelected())
							chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);*/
						chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						int choice = chooser.showOpenDialog(chooser);
						if (choice != JFileChooser.APPROVE_OPTION) return;
						File chosenFile = chooser.getSelectedFile();
						srcTxt.setText(chosenFile.getAbsolutePath());
					}
				});
		        
		        
		        /*
		         * JButton -> To Download the File/Folder from the Source to Target Destination
		         */
		        JButton btnUpload = new JButton("Upload");
		        innerPanel4.add(btnUpload);
		        btnUpload.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						File file = new File(srcTxt.getText());
						if(file.isFile()) {
							try {
								FileLocalPath = srcTxt.getText();
								String newFileLocalPath = FileLocalPath.replace('\\', '/');
								String fileName = newFileLocalPath.substring(newFileLocalPath.lastIndexOf("/"));
								TargetDbxPath = targetTxt.getText();
								TargetDbxPath += fileName;
								obj.DbxUploadFile(FileLocalPath, TargetDbxPath);
								JOptionPane.showMessageDialog(uploadFrame, "Upload Complete !", "MyCLoudJ - Upload Complete", JOptionPane.INFORMATION_MESSAGE);
								} catch (IOException | DbxException e1) {
									// TODO Auto-generated catch block
									JOptionPane.showMessageDialog(uploadFrame, "Upload Aborted !\n"+e1.getMessage(), "MyCLoudJ - Upload Aborted", JOptionPane.ERROR_MESSAGE);
									e1.printStackTrace();
								}
						}
						else if (file.isDirectory()) {
								try {
									FolderLocalPath = srcTxt.getText();
									TargetDbxPath = targetTxt.getText();
									obj.DbxUploadFolder(FolderLocalPath, TargetDbxPath);
									JOptionPane.showMessageDialog(uploadFrame, "Upload Complete !", "MyCLoudJ - Upload Complete", JOptionPane.INFORMATION_MESSAGE);
								} catch (IOException | DbxException e1) {
									// TODO Auto-generated catch block
									JOptionPane.showMessageDialog(uploadFrame, "Upload Aborted !\n"+e1.getMessage(), "MyCLoudJ - Upload Aborted", JOptionPane.ERROR_MESSAGE);
									e1.printStackTrace();
								}
						}
					}
				});
		        
		        /*
		         * This will position the JFrame in the center of the screen
		         */
		        uploadFrame.setLocationRelativeTo(null);
		        
		        uploadFrame.add(innerPanel1);
		        uploadFrame.add(innerPanel2);
		        uploadFrame.add(innerPanel3);
		        uploadFrame.add(innerPanel4);
		        
		        uploadFrame.setVisible(true);
			}
		});
        
   
        mainFrame.add(panel1);
        mainFrame.add(panel2);
        mainFrame.add(panel3);
        mainFrame.add(panel4);
        mainFrame.add(panel5);
        
        mainFrame.setVisible(true);
	}
    
    
    /*
     * Function to add nodes to the JTree
     */
    public void addChildren(DefaultMutableTreeNode node, String name) {
    	/*
		 * Function to get the metadata of the folder you wish to download
		 */
		DbxEntry.WithChildren folderInfo=null;
		try {
			folderInfo = obj.client.getMetadataWithChildren(name);
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		 * Iterate over children and add nodes into the JTree
		 */
		Iterator<DbxEntry> iterChildren;
		 if (folderInfo == null) {
		 } 
		 else {				 
			 iterChildren = folderInfo.children.iterator();
			 @SuppressWarnings("unused")
			 boolean tillEndOfDirectory = true;
			 DbxEntry child;
			 while(tillEndOfDirectory=iterChildren.hasNext()) {
				child = iterChildren.next();
				DefaultMutableTreeNode nodeChild = new DefaultMutableTreeNode(child.path);
				node.add(nodeChild);
			 }
		 }
    }
    
    /*
     * Function to add nodes to the JTree
     */
    public void addChildrenFolder(DefaultMutableTreeNode node, String name) {
    	/*
		 * Function to get the metadata of the folder you wish to download
		 */
		DbxEntry.WithChildren folderInfo=null;
		try {
			folderInfo = obj.client.getMetadataWithChildren(name);
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		 * Iterate over children and add nodes into the JTree
		 */
		Iterator<DbxEntry> iterChildren;
		 if (folderInfo == null) {
		 } 
		 else {				 
			 iterChildren = folderInfo.children.iterator();
			 @SuppressWarnings("unused")
			 boolean tillEndOfDirectory = true;
			 DbxEntry child;
			 while(tillEndOfDirectory=iterChildren.hasNext()) {
				child = iterChildren.next();
				if(child.isFolder()) {
					DefaultMutableTreeNode nodeChild = new DefaultMutableTreeNode(child.path);
					node.add(nodeChild);
				}	
			 }
		 }
    }
}