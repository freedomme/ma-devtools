/**
 * Copyright (C) 2017 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.junit.Test;

import com.infiniteautomation.store.BulkModuleUploader;

/**
 * Upload the core and all modules to the store using a properties file that may have been defined.
 * 
 * @author Terry Packer
 */
public class TestUploadModules {

	@Test
	public void testUploadAllModules(){
		Properties props = new Properties();
		try(InputStream input = getClass().getClassLoader().getResourceAsStream("moduleUpload.properties")){
			
			props.load(input);
			//Abort if we don't have a configuration to use
			if(props.getProperty("enabled", "false").equals("false"))
				return;
			
			String url = props.getProperty("store.url");
			String username = props.getProperty("store.username");
			String password = props.getProperty("store.password");
			String moduleDir = props.getProperty("modules.dir");
			String coreZip = props.getProperty("core.zip");
			
			BulkModuleUploader uploader = new BulkModuleUploader(url);
			
			try {
				HttpClient httpclient = uploader.login(username, password);
				System.out.println("Logged in as: " + username);
				
				//Upload Core if there is one
				File core = new File(coreZip);
				if(core.exists() && core.isFile()) {
				    System.out.println("**** Uploading " + core.getAbsolutePath() + "... ****");
                    uploader.startUploadMonitor(httpclient, "core");
                    uploader.postFile(httpclient, core, "core");
                    System.out.println("**** " + core.getAbsolutePath() + " Uploaded ****");
				}
				
				//Upload Modules
				File dir = new File(moduleDir);
				if(!dir.exists())
					fail("Directory " + moduleDir + " doesn't exist.");
				
				if(dir.isDirectory()){
					File[] modules = dir.listFiles(new FilenameFilter(){
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".zip");
						}
					});
					
					for(File module : modules){
						System.out.println("**** Uploading " + module.getAbsolutePath() + "... ****");
						uploader.startUploadMonitor(httpclient, "modules");
						uploader.postFile(httpclient, module, "modules");
						System.out.println("**** Module " + module.getAbsolutePath() + " Uploaded ****");
					}
					
				}
				
			} catch (ClientProtocolException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			}
			
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
}
