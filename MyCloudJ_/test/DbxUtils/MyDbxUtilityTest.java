package DbxUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

import com.dropbox.core.DbxException;

public class MyDbxUtilityTest {
	
	@Test
	public void testDbxUploadFile() {
		// Object of class DbxUtility
		DbxUtility obj = new DbxUtility();
		String authorizeUrl="", code="";
		
		// Generate dropbox app url
		try {
			authorizeUrl = obj.DbxLogin();
		} catch (IOException | DbxException e) {
			e.printStackTrace();
		}
		
		// To open the url in the default browser. If return value is "done", it is successful. Otherwise, some error
		obj.openDefaultBrowser(authorizeUrl);
		
		// Enter the access Code
		System.out.println("Enter the access code:");
		try {
			code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
			
			// connect user to dropbox	
			obj.DbxLinkUser(code);

			// Upload a file
			// obj.DbxUploadFile("/Users/mathuratin/Desktop/travel_0013.jpg", "/");
			// obj.DbxUploadFile("<ABSOLUTE_FILE_PATH>", "<TARGET_DROPBOX_PATH>");
		} catch (IOException | DbxException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDbxUploadFolder() {
		// Object of class DbxUtility
		DbxUtility obj = new DbxUtility();
		String authorizeUrl="", code="";
		
		// Generate dropbox app url
		try {
			authorizeUrl = obj.DbxLogin();
		} catch (IOException | DbxException e) {
			e.printStackTrace();
		}
		
		// To open the url in the default browser. If return value is "done", it is successful. Otherwise, some error
		obj.openDefaultBrowser(authorizeUrl);
		
		// Enter the access Code
		System.out.println("Enter the access code:");
		try {
			code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
			
			// connect user to dropbox	
			obj.DbxLinkUser(code);

			// Upload a folder
			// obj.DbxUploadFolder("/Users/mathuratin/Desktop/test", "/");
			// obj.DbxUploadFolder("<ABSOLUTE_FOLDER_PATH>", "<TARGET_DROPBOX_PATH>");
		} catch (IOException | DbxException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDbxDownloadFile() {
		// Object of class DbxUtility
		DbxUtility obj = new DbxUtility();
		String authorizeUrl="", code="";
		
		// Generate dropbox app url
		try {
			authorizeUrl = obj.DbxLogin();
		} catch (IOException | DbxException e) {
			e.printStackTrace();
		}
		
		// To open the url in the default browser. If return value is "done", it is successful. Otherwise, some error
		obj.openDefaultBrowser(authorizeUrl);
		
		// Enter the access Code
		System.out.println("Enter the access code:");
		try {
			code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
			
			// connect user to dropbox	
			obj.DbxLinkUser(code);

			// Download a file
			// obj.DbxDownloadFile("/Getting Started.pdf", "/Users/mathuratin/Desktop/test");
			// obj.DbxDownloadFile("<DROPBOX_FILE_PATH>", "<TARGET_LOCAL_PATH>");
		} catch (IOException | DbxException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDbxDownloadFolder() {
		// Object of class DbxUtility
		DbxUtility obj = new DbxUtility();
		String authorizeUrl="", code="";
		
		// Generate dropbox app url
		try {
			authorizeUrl = obj.DbxLogin();
		} catch (IOException | DbxException e) {
			e.printStackTrace();
		}
		
		// To open the url in the default browser. If return value is "done", it is successful. Otherwise, some error
		obj.openDefaultBrowser(authorizeUrl);
		
		// Enter the access Code
		System.out.println("Enter the access code:");
		try {
			code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
			
			// connect user to dropbox	
			obj.DbxLinkUser(code);

			// Download a folder
			// obj.DbxDownloadFolder("/jASSAR_", "/Users/mathuratin/Desktop/test");
			// obj.DbxDownloadFolder("<DROPBOX_FOLDER_PATH>", "<TARGET_LOCAL_PATH>");
		} catch (IOException | DbxException e) {
			e.printStackTrace();
		}
	}
}