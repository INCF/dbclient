package DbxUtils;

/*
 * Description		:		Google Summer of Code 2014 Project
 * Organization		:		International Neuroinformatics Coordinating Facility (INCF), Belgian Node
 * Author			:		Atin Mathur (mathuratin007@gmail.com)
 * Mentor			: 		Dimiter Prodanov
 * Project Title	:		Dropbox Client for ImageJ (Image Processing Software in java)
 * FileName			:		DbxUtility.java (package DbxUtils)
 * 							Contains all the functions to access the User's Dropbox Accounts 
 * 
 * Users			:		Image Processing Researchers (Neuroscientists etc.)
 * Motivation		:		To facilitate the sharing of datasets on among ImageJ users
 * Technologies		:		Java, Dropbox Core APIs, Restful Web Services, Swing GUI
 * Installation		:		Put the plugin/MyCloudJ_.jar to the plugins/ folder of the ImageJ. It will show 
 * 							up in the plugins when you run ImageJ.
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

public class DbxUtility {
		/*
	 	 * Got this APP Key and Secret from the Developer's APP console. These will remain fixed and depends on the APP.
	 	 */
		final private String APP_KEY = "5jysg1bzg0ulli3";
		final private String APP_SECRET = "t0ln07k26pctonw";
		
		
		/*
		 * Class variables used during the complete process of the plugin.
		 */
		public DbxClient client;
		private DbxWebAuthNoRedirect webAuth;
		private DbxRequestConfig config;
		private DbxAppInfo appInfo;
		private String authorizeUrl;
		private DbxAuthFinish authFinish;
		private String accessToken;
		public String OS = System.getProperty("os.name").toLowerCase();
		
		   
		/*
		 * Function to open Dropbox App URL in the default browser for user authentication 
		*/
		public void openDefaultBrowser(String url) {
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
		
				
		/*
		 * Function for User Sign-in and Allow the Dropbox App MyCloudJ 
		 */
		public String DbxLogin() throws IOException, DbxException {
			appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);

	        config = new DbxRequestConfig("JavaTutorial/1.0", Locale.getDefault().toString());
	        webAuth = new DbxWebAuthNoRedirect(config, appInfo);

	        // Generate the URL
	        authorizeUrl = webAuth.start();
	        return authorizeUrl;
		}

		
		/*
		 * Function to accept the Access code and link the account of the user concerned.
		 */
		public void DbxLinkUser(String code) throws IOException, DbxException {
			/*
			 *  This will fail if the user enters an invalid authorization code.
			 */
	        authFinish = webAuth.finish(code);
	        accessToken = authFinish.accessToken;
	        client = new DbxClient(config, accessToken);
		}

		
		/* 
		 * Function to upload a "File" to Dropbox given the complete path of the file in local machine and the Dropbox folder's path
		 * where "File" has to be saved.
		 */
		public void DbxUploadFile(String FileLocalPath, String TargetDbxPath) throws IOException, DbxException {
	        File inputFile = new File(FileLocalPath);
	        InputStream inputStream = new FileInputStream(inputFile);
	        try {
		        if(!inputFile.isHidden()) {
		        	@SuppressWarnings("unused")
					DbxEntry.File uploadedFile = client.uploadFile(TargetDbxPath, DbxWriteMode.add(), inputFile.length(), inputStream);
		        }
		        } finally {
		            inputStream.close();
		        }
		}
		
		
		/* 
		 * Function to upload a "Folder" to Dropbox given the complete path of the file in local machine and the Dropbox folder's path
		 * where "Folder" has to be saved.
		 */
		public void DbxUploadFolder(String FolderLocalPath, String TargetDbxPath) throws IOException, DbxException {
			String newFolderLocalPath = FolderLocalPath.replace('\\', '/');
			String folderName;
        	folderName = newFolderLocalPath.substring(newFolderLocalPath.lastIndexOf("/"));
			File inputFolder = new File(FolderLocalPath);
	        if(inputFolder.isDirectory()) {
	        	@SuppressWarnings("unused")
				DbxEntry folder = client.createFolder(TargetDbxPath+folderName);
	        	String[] files = inputFolder.list();
	        	for(int i=0;i<files.length;i++) {
	        		if(OS.contains("windows"))
	        			DbxUploadFolder(FolderLocalPath+'\\'+files[i], TargetDbxPath+folderName);
	        		else
	        			DbxUploadFolder(FolderLocalPath+'/'+files[i], TargetDbxPath+folderName);
	        	}
	        }
	        else if(inputFolder.isFile()) {
	        	DbxUploadFile(FolderLocalPath, TargetDbxPath+folderName);
	        }
		}
		
		
		/* 
		 * Function to download a "File" from Dropbox given the complete path of the file in Dropbox and also local machine path 
		 * where the "File" has to be saved.
		 */
		public void DbxDownloadFile(String FileDbxPath, String TargetLocalPath) throws IOException, DbxException {
			String fileName = FileDbxPath.substring(FileDbxPath.lastIndexOf("/"));
			TargetLocalPath += fileName;
			File SaveAsFile = new File(TargetLocalPath);
	        OutputStream outputStream = new FileOutputStream(SaveAsFile);
	        try {
	        @SuppressWarnings("unused")
			DbxEntry.File downloadedFile = client.getFile(FileDbxPath, null, outputStream);
	        } finally {
	            outputStream.close();
	        }
		}
		
		
		/* 
		 * Function to download a "Folder" from Dropbox given the complete path of the Folder in Dropbox and also the local machine path
		 * where the "Folder" has to be saved.
		 */
		public void DbxDownloadFolder(String FolderDbxPath, String TargetLocalPath) throws DbxException {
			/*
			 * Create the folder in the local machine
			 */
			String folderName = FolderDbxPath.substring(FolderDbxPath.lastIndexOf("/"));
			TargetLocalPath += folderName;
			boolean newFolder = new File(TargetLocalPath).mkdirs();
			if(!newFolder) {
			}
			/*
			 * Function to get the metadata of the folder you wish to download
			 */
			DbxEntry.WithChildren folderInfo = client.getMetadataWithChildren(FolderDbxPath);
			Iterator<DbxEntry> iterChildren;
			 if (folderInfo == null) {
			    // IJ.error("No file or folder at that path.");
			 } else {				 
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
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				 }
			 }
		}
}