package CloudConnect;
/*
 * Description		:		Google Summer of Code 2014 Project
 * Organization		:		International Neuroinformatics Coordinating Facility, Belgian Node
 * Author			:		Atin Mathur (mathuratin007@gmail.com), (atin.mathur@lnmiit.ac.in)
 * Mentor			: 		Dimiter Prodanov
 * Project Title	:		Dropbox Client for ImageJ (Image Processing Software in java)
 * FileName			:		MyCloudJ_.java (package CloudConnect)
 * 							ImageJ Plugin for Dropbox Client. Uses the DbxUtility.java of package DbxUtils to access 
 * 							the User's Dropbox Accounts  
 * 
 * Users			:		Image Processing Researchers (Neuroscientists etc.)
 * Motivation		:		To facilitate the sharing of datasets on among ImageJ users
 * Technologies		:		Java, Dropbox Core APIs, Restful Web Services, Swing GUI
 * Installation		:		Put the plugin/MyCloudJ_.jar to the plugins/ folder of the ImageJ. It will show 
 * 							up in the plugins when you run ImageJ.
 * Requirements		:		ImageJ alongwith JRE 1.7 or later.
 * Date				:		19-May-2014
 */
import java.awt.Desktop; 
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.dropbox.core.*;

import ij.plugin.*;
import DbxUtils.*;

public class MyCloudJ_ implements PlugIn {
	/*
	 *  Class variables
	 */
	private String code;
	private String authorizeUrl;
	private String downloadFileDetails, uploadFileDetails;
	private DbxUtility obj = new DbxUtility();
	private int enableBtn = 0;
	private int userStatus = 0;
	private String usrInfo;
	private String displayInstructions;
	
	/*
	 * Instructions for the user to Connect the Plugin to their Dropbox Accounts
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
		 *  Create a JFrame that contains the whole GUI for the MyCloudJ_ plugin
		 */
		final JFrame mainFrame = new JFrame();
		BoxLayout boxLayout = new BoxLayout(mainFrame.getContentPane(), BoxLayout.Y_AXIS);
		mainFrame.setLayout(boxLayout);
		mainFrame.setMaximumSize(new Dimension(800,800));
        mainFrame.setTitle("CloudConnect - MyCloudJ");
        mainFrame.setSize(800,800);
      
        /*
         * This will position the JFrame in the center of the screen
         */
        mainFrame.setLocationRelativeTo(null);
        
        /* Create the Menu bar.
         * 
         * 2 Menus will be required:
         * 
         * 1. Download -> 2 Menu items: a. File, b. Folder
         * 
         * 2. Upload -> 2 Menu items: a. File, b. Folder
         */
        JMenuBar menubar = new JMenuBar();
        
        final JMenu downloadMenu = new JMenu("Download");
        downloadMenu.add(new JSeparator());
        downloadMenu.disable();
        
        final JMenu uploadMenu = new JMenu("Upload");
        uploadMenu.add(new JSeparator());
        uploadMenu.disable();
        
        JMenuItem downloadItem1 = new JMenuItem("File");
        JMenuItem downloadItem2 = new JMenuItem("Folder");
        
        JMenuItem uploadItem1 = new JMenuItem("File");
        JMenuItem uploadItem2 = new JMenuItem("Folder");
        
        downloadMenu.add(downloadItem1);
        downloadMenu.add(downloadItem2);
        
        uploadMenu.add(uploadItem1);
        uploadMenu.add(uploadItem2);
        
        menubar.add(downloadMenu);
        menubar.add(uploadMenu);
        
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
		usrInfo ="hello";
        final JTextArea userInfo = new JTextArea(usrInfo);
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
					if(enableBtn == 1 && userStatus == 0 && !code.equals(null)) {
					obj.DbxLinkUser(code);
					//IJ.error("Linked account: " + obj.client.getAccountInfo().displayName);
					enableBtn = 0;
					lblStatus.setText("Connected as "+obj.client.getAccountInfo().displayName);
					usrInfo = obj.client.getAccountInfo().toStringMultiline();
					userInfo.setText(usrInfo);
					accessCode.disable();
					downloadMenu.enable();
					uploadMenu.enable();
					userStatus = 1;
					}
					else if(userStatus == 1)
						JOptionPane.showMessageDialog(mainFrame, "Already connected !", "MyCLoudJ - Already Connected", JOptionPane.WARNING_MESSAGE);
					else
						JOptionPane.showMessageDialog(mainFrame, "Enter Access Code !", "MyCLoudJ - Enter Access code", JOptionPane.WARNING_MESSAGE);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (DbxException e1) {
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
				openDefaultBrowser(authorizeUrl);
				accessCode.enable(true);
				enableBtn = 1;
			}
		});
        panel2.add(accessDbxButton);
        
        /*
         * Add action listener for downloading the File from the user's Dropbox Account. (Download -> File)
         */
        downloadItem1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String FileDbxPath = "/ICPR2012/ATIN/activeContour.cpp";
				String TargetLocalPath = "/Users/mathuratin/Desktop/activeContours.cpp";
				try {
				obj.DbxDownloadFile(FileDbxPath, TargetLocalPath);
				System.out.println(downloadFileDetails);
				} catch (IOException | DbxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
        
        
        /*
         * Add action listener for downloading the Folder from the user's Dropbox Account. (Download -> Folder)
         */
        downloadItem2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					obj.DbxDownloadFolder("/a", "/Users/mathuratin/Desktop");
				} catch (DbxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
        
        
        /*
         * Add action listener for uploading the File to the user's Dropbox Account. (Upload -> File)
         */
        uploadItem1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String filename = "/Users/mathuratin/Desktop/Byte of VIM.pdf";
				String DbxTargetPath = "/test/a.pdf";
				try {
					obj.DbxUploadFile(filename, DbxTargetPath);
					System.out.println(uploadFileDetails);
				} catch (IOException | DbxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
        
        
        /*
         * Add action listener for uploading the Folder to the user's Dropbox Account. (Upload -> Folder)
         */
        uploadItem2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String FolderLocalPath = "/Users/mathuratin/Desktop/DropBoxClient";
				String TargetDbxPath = "/a";
				try {
					obj.DbxUploadFolder(FolderLocalPath, TargetDbxPath);
				} catch (IOException | DbxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
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
	 * Function to open Dropbox App URL in the default browser for user authentication 
	*/
	private void openDefaultBrowser(String url) {
		if(Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else{
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("xdg-open " + url);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
	}
		
}

