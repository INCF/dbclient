package DbxUtils;

/*
 * Description		:		Google Summer of Code 2014 Project
 * Project Title	:		Dropbox Client for ImageJ (Image Processing Software in Java)
 * Organization		:		International Neuroinformatics Coordinating Facility (INCF), Belgian Node
 * Author			:		Atin Mathur (mathuratin007@gmail.com)
 * Mentor			: 		Dimiter Prodanov (dimiterpp@gmail.com)
 * FileName			:		DbxUtility.java (package DbxUtils)
 * 							Contains all the functions to access the User's Dropbox Accounts via Dropbox Core APIs
 * 
 * Users			:		Image Processing Researchers (Neuroscientists etc.)
 * Motivation		:		To facilitate the sharing of datasets on among ImageJ users
 * Technologies		:		Java, Dropbox Core APIs, Restful Web Services, Swing GUI
 * Installation		:		Put the plugin/MyCloudJ_.jar to the plugins/ folder of the ImageJ. It will show up in the plugins when you run ImageJ.
 * Requirements		:		ImageJ alongwith JRE 1.7 or later. 
 * Date				:		19-May-2014
 */

import com.dropbox.core.*;

import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

// Dropbox APIs calls for the MyCloudJ_ plugin are made from this class
public class DbxUtility {
		/*
		 * Class variables
		 */
	
		/*
	 	 * Got this APP Key and Secret from the Developer's APP console after registering an app
	 	 * APP_KEY			:	MycloudJ APP Key (5jysg1bzg0ulli3)
	 	 * APP_SECRET		:	MycloudJ APP Secret (t0ln07k26pctonw) 
	 	 * 
	 	 * Note				:	These will remain fixed and depends on the APP.
	 	 */
		final private String APP_KEY = "5jysg1bzg0ulli3";
		final private String APP_SECRET = "t0ln07k26pctonw";
		
		
		/*
		 * Dropbox API objects:
		 * 
		 * client			:	obj of class DbxClient. Use this class to make remote calls to the Dropbox API. You'll need an access token first. Note the public access specifier
		 * 	
		 * webAuth			:	obj of class DbxWebAuthNoRedirect. This class does the OAuth web-based authorization flow for apps that can't provide 
		 * 						a redirect URI.
		 * 
		 * config			:	obj of class DbxRequestConfig. This class manages the grouping of a few configuration parameters for how we should make 
		 * 						requests to the Dropbox servers.
		 * 
		 * appInfo			:	obj of class DbxAppInfo. This class Identifies the information of the Dropbox App (In our case, it is the MyCloudJ_ Dropbox App).
		 * 
		 * authFinish		:	obj of class DbxAuthFinish. When you successfully complete the authorization process, the Dropbox server returns this information to you.
		 * 
		 * authorizeUrl		:	String that stores the MyCloudJ App url.
		 * 
		 * accessToken		:	String that stores the access token that allows to access a particular user's account. It is temporary and has to be generated everytime.  
		 */
		public DbxClient client;
		private DbxWebAuthNoRedirect webAuth;
		private DbxRequestConfig config;
		private DbxAppInfo appInfo;
		private DbxAuthFinish authFinish;
		private String authorizeUrl;
		private String accessToken;
		
		/*
		 * User's Dropbox information	: 	Displayed in the text area after the plugin is connected to user's dropbox account.
		 * 
		 * userName						:	Dropbox user name
		 * country						:	Country
		 * userQuota					:	Total size(in GBs) of user's dropbox account 
		 * 
		 */
		public String userName="", country="", userQuota="";
		
		// Stores the OS-type: Linux/Windows/Mac. It is used to solve the problem of path separator(/ or \\). It makes plugin platform independent.
		public String OS = System.getProperty("os.name").toLowerCase();
		
		
		/*
		 * Function to open Dropbox App URL in the default browser for user authentication
		 * 
		 * Parameters:
		 * 
		 * String url	:	MyCloudJ App url to be opened in the default browser
		 * 
		 * This function is called from the MyCloudJ_ class
		*/
		public String openDefaultBrowser(String url) {
			if(Desktop.isDesktopSupported()){
	            Desktop desktop = Desktop.getDesktop();
	            try {
	            	// opens the url in the browser
	                desktop.browse(new URI(url));
	            } catch (IOException | URISyntaxException e) {
	            	e.printStackTrace();
	            	return e.getMessage();
	            }
	        }
			else{
	            Runtime runtime = Runtime.getRuntime();
	            try {
	                runtime.exec("xdg-open " + url);
	            } catch (IOException e) {
	            	e.printStackTrace();
	            	return e.getMessage();
	            }
	        }
			return "done";
		}
		
		
		/*
		 * Function for User Sign-in and Allow the Dropbox App MyCloudJ 
		 */
		public String DbxLogin() throws IOException, DbxException {
			// Identifying the information of MyCLoudJ App  	
			appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
			
			// Manages the configuration 
	        config = new DbxRequestConfig("JavaTutorial/1.0", Locale.getDefault().toString());
	        
	        /*
	         * Generate the App url and start authorization
	         */
	        webAuth = new DbxWebAuthNoRedirect(config, appInfo);
	        authorizeUrl = webAuth.start();
	        
	        // Return the App Url to the MyCloudJ plugin
	        return authorizeUrl;
		}
		
		
		/*
		 * Function to accept the Access code and link the account of the user concerned.
		 */
		public void DbxLinkUser(String code) throws IOException, DbxException {
			/*
			 *  This will fail if the user enters an invalid authorization code.
			 *  authFinish		:	When you successfully complete the authorization process, the Dropbox server returns this information to you.
			 *  accessToken		:	An access token that can be used to make Dropbox API calls.
			 */
	        authFinish = webAuth.finish(code);
	        accessToken = authFinish.accessToken;
	        
	        // Passed accessToken in to the DbxClient constructor.
	        client = new DbxClient(config, accessToken);
	        
	        /*
			 * Retrieve username, country and quota from dropbox account info API and
			 * print it in the text area for the user
			 */
	        userName = client.getAccountInfo().displayName;
			country = client.getAccountInfo().country;
			userQuota += (double)client.getAccountInfo().quota.total/(1024*1024*1024);
		}
		
		
		/* 
		 * Function to upload a "File" to Dropbox given the complete path of the file in local machine and the Dropbox folder's path
		 * where "File" has to be saved.
		 * 
		 * Parameters:
		 * 
		 * FileLocalPath		:		Absolute Local Path of the File to be uploaded to Dropbox
		 * TargetDbxPath		:		Absolute Dropox Path of the folder where the file is to be added
		 *  
		 */
		public void DbxUploadFile(String FileLocalPath, String TargetDbxPath) throws IOException, DbxException {
	        File inputFile = new File(FileLocalPath);
	        InputStream inputStream = new FileInputStream(inputFile);
	        try {
		        if(!inputFile.isHidden()) {
		        	@SuppressWarnings("unused")
		        	// Upload the file to dropbox
					DbxEntry.File uploadedFile = client.uploadFile(TargetDbxPath, DbxWriteMode.add(), inputFile.length(), inputStream);
		        }
		        } finally {
		            inputStream.close();
		        }
		}
		
		
		/* 
		 * Function to upload a "Folder" to Dropbox given the complete path of the folder in local machine and the Dropbox folder's path
		 * where "Folder" has to be saved.
		 * 
		 * Parameters:
		 * 
		 * FolderLocalPath		:		Absolute Path of the Folder to be uploaded to Dropbox
		 * TargetDbxPath		:		Absolute Dropox Path of the folder where the Folder has to be uploaded
		 * 
		 */
		public void DbxUploadFolder(String FolderLocalPath, String TargetDbxPath) throws IOException, DbxException {
			// Replace Path separator '\\' (for windows) to  Dropbox Path Separator '/'
			String newFolderLocalPath = FolderLocalPath.replace('\\', '/');
			
			// Extract the foldername i.e. Last part of the Path 
			String folderName;
        	folderName = newFolderLocalPath.substring(newFolderLocalPath.lastIndexOf("/"));
        	
        	// File pointer to the folder 
			File inputFolder = new File(FolderLocalPath);
	        
			/*
			 *  If Folder is a directory, Execute this part
			 *  We iteratively call DbxUploadFile for each children (sub-folders and files) inside the folder.
			 */
			if(inputFolder.isDirectory()) {
	        	@SuppressWarnings("unused")
	        	// Create a new folder of name "string folderName" inside Dropbox folder TargetDbxPath
				DbxEntry folder = client.createFolder(TargetDbxPath+folderName);
	        	
	        	// List of files inside the folder 
	        	String[] files = inputFolder.list();
	        	
	        	/*
	        	 * Iterate for each child in the list and call,  
	        	 * 
	        	 * 1. DbxUploadFile() function if the child is a file, otherwise
	        	 * 
	        	 * 2. DbxUploadFolder() function if the child is a sub-folder.
	        	 * 
	        	 */
	        	for(int i=0;i<files.length;i++) {
	        		if(OS.contains("windows"))
	        			DbxUploadFolder(FolderLocalPath+'\\'+files[i], TargetDbxPath+folderName);
	        		else
	        			DbxUploadFolder(FolderLocalPath+'/'+files[i], TargetDbxPath+folderName);
	        	}
	        }
			// If the folder is not a directory but a file. This will never execute if the we check the File Type before calling this method. 
	        else if(inputFolder.isFile()) {
	        	DbxUploadFile(FolderLocalPath, TargetDbxPath+folderName);
	        }
		}
		
		
		/* 
		 * Function to download a "File" from Dropbox given the complete path of the file in Dropbox and also local machine path 
		 * where the "File" has to be saved.
		 * 
		 * Parameters:
		 * 
		 * FileDbxPath			:		Complete Dropbox Path of the File to be downloaded from Dropbox
		 * TargetLocalPath		:		Absolute Local Path of the folder where the file has to be downloaded
		 * 
		 */
		public void DbxDownloadFile(String FileDbxPath, String TargetLocalPath) throws IOException, DbxException {
			// Extract the filename from the absolute path i.e. the last part
			String fileName = FileDbxPath.substring(FileDbxPath.lastIndexOf("/"));
			
			// Add the filename to the end of the local machine path where the file has to be added
			TargetLocalPath += fileName;
			
			// File Pointer to the file
			File SaveAsFile = new File(TargetLocalPath);
	        OutputStream outputStream = new FileOutputStream(SaveAsFile);
	        try {
	        @SuppressWarnings("unused")
	        // download the file from Dropbox
			DbxEntry.File downloadedFile = client.getFile(FileDbxPath, null, outputStream);
	        } finally {
	            outputStream.close();
	        }
		}
		
		
		/* 
		 * Function to download a "Folder" from Dropbox given the complete path of the Folder in Dropbox and also the local machine path
		 * where the "Folder" has to be saved.
		 * 
		 * Parameters:
		 * 
		 * FolderDbxPath			:		Complete Dropbox Path of the Folder to be downloaded from Dropbox
		 * TargetLocalPath			:		Absolute Local Path of the folder where the Folder has to be downloaded
		 * 
		 */
		public void DbxDownloadFolder(String FolderDbxPath, String TargetLocalPath) throws IOException, DbxException {
			/*
			 * Create the folder in the local machine with last part of the of the FolderDbxPath i.e Folder's name
			 */
			String folderName = FolderDbxPath.substring(FolderDbxPath.lastIndexOf("/"));
			TargetLocalPath += folderName;
			boolean newFolder = new File(TargetLocalPath).mkdirs();
			
			if(!newFolder) {
				return;
			}
			
			/*
			 * Function to get the metadata of the folder you wish to download
			 */
			DbxEntry.WithChildren folderInfo = client.getMetadataWithChildren(FolderDbxPath);
			Iterator<DbxEntry> iterChildren;
			 if (folderInfo == null) {
			    // IJ.error("No file or folder at that path.");
			 } else {				 
				 /*
	        	 * Iterate for each child of the Folder and call,  
	        	 * 
	        	 * 1. DbxDownloadFile() function if the child is a file, otherwise
	        	 * 
	        	 * 2. DbxDownloadFolder() function if the child is a sub-folder.
	        	 * 
	        	 */
				 iterChildren = folderInfo.children.iterator();
				 @SuppressWarnings("unused")
				 boolean tillEndOfDirectory = true;
				 DbxEntry child;
				 while(tillEndOfDirectory=iterChildren.hasNext()) {
					child = iterChildren.next();
					if(child.isFolder())
						DbxDownloadFolder(child.path, TargetLocalPath);
					else if(child.isFile()) {
						try {
							DbxDownloadFile(child.path, TargetLocalPath);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				 }
			 }
		}

	    
	    /*
	     * Function to add nodes to the JTree
	     * 
	     * This function is called when user selects a parent node and clicks Expand button
	     * 
	     * Parameters:
	     * 
	     * node			:	Parent node to which we have to add the child nodes.
	     * Treemodel	:	DefaultTreeModel of the JTree where these nodes are to be added
	     * name			:	name of the parent node 
	     *
	     */
	    public void addChildren(DefaultMutableTreeNode node, DefaultTreeModel Treemodel, String name) {
	    	/*
			 * Function to get the metadata of the folder you wish to download
			 */
			DbxEntry.WithChildren folderInfo=null;
			try {
				folderInfo = client.getMetadataWithChildren(name);
			} catch (DbxException e) {
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
					// New child node to be added to the parent node
					if(child.isFile()) {
						String lastPart = DbxPath.getName(child.path);
						DefaultMutableTreeNode nodeChild = new DefaultMutableTreeNode(lastPart);
						// Add only unique nodes. In case, this function is called for the same parent node more than once
						addUniqueNode(node, nodeChild, Treemodel);
					}
					else if(child.isFolder()){
						DefaultMutableTreeNode nodeChild = new DefaultMutableTreeNode(child.path);
						// Add only unique nodes. In case, this function is called for the same parent node more than once
						addUniqueNode(node, nodeChild, Treemodel);
					}
				 }
			 }
	    }
	    
	    /*
	     * Function to add nodes to the JTree
	     * 
	     * This function is called when user selects a parent node and clicks Expand button
	     * 
	     * Parameters:
	     * 
	     * node			:	Parent node to which we have to add the child nodes.
	     * TreeModel	:	DefaultTreeModel of the JTree where this node has to be added.
	     * name			:	name of the parent node
	     * 
	     * Note		:	The difference between this function is that it is used to add only those nodes which are "folder" type.	
	     * 
	     */
	    public void addChildrenFolder(DefaultMutableTreeNode node, DefaultTreeModel Treemodel, String name) {
	    	/*
			 * Function to get the metadata of the folder you wish to download
			 */
			DbxEntry.WithChildren folderInfo=null;
			try {
				folderInfo = client.getMetadataWithChildren(name);
			} catch (DbxException e) {
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
					// Only add child node if it is a folder
					if(child.isFolder()) {
						// New child node to be added to the parent node
						DefaultMutableTreeNode nodeChild = new DefaultMutableTreeNode(child.path);
						// Add only unique nodes. In case, this function is called for the same parent node more than once
						addUniqueNode(node, nodeChild, Treemodel);
					}	
				 }
			 }
	    }
	    
	    
	    /*
	     * This function only adds unique children nodes to the parent node.
	     * 
	     * Parameters:
	     * 
	     * parentNode		:		Node to which children has to be added
	     * childNode		:		Node to be added to the parent Node
	     * model			:		JTree model
	     * 
	     */
	    private void addUniqueNode(DefaultMutableTreeNode parentNode, DefaultMutableTreeNode childNode, DefaultTreeModel model) {
	        // Check each node
	        boolean isUnique = true;
	        for (int i = 0; i < model.getChildCount(parentNode); i++)
	        {
	            Object compUserObj = ((DefaultMutableTreeNode) model.getChild(parentNode, i)).getUserObject();
	            if (compUserObj.equals(childNode.getUserObject()))
	            {
	                isUnique = false;
	                break;
	            }
	        }

	        // If Unique, insert
	        if(isUnique)
	            model.insertNodeInto(childNode, parentNode, parentNode.getChildCount());
	    }
}