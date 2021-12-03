package com.demo.fde.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {

	public static void decompressZip(String fileZip, String destDir) throws IOException, FileNotFoundException {
	
	    byte[] buffer = new byte[1024];
	    ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
	    ZipEntry zipEntry = zis.getNextEntry();
	    
		while (zipEntry != null) {
		
		     File newFile = newFile(new File(destDir), zipEntry);
		     if (zipEntry.isDirectory()) {
		         if (!newFile.isDirectory() && !newFile.mkdirs()) {
		             throw new IOException("Error al crear la carpeta " + newFile);
		         }
		     } else {
		         // fix for Windows-created archives
		         File parent = newFile.getParentFile();
		         if (!parent.isDirectory() && !parent.mkdirs()) {
		             throw new IOException("Error al crear la carpeta " + parent);
		         }
		         
		         // write file content
		         FileOutputStream fos = new FileOutputStream(newFile);
		         int len;
		         while ((len = zis.read(buffer)) > 0) {
		             fos.write(buffer, 0, len);
		         }
		         fos.close();
		     }
		     
		     zipEntry = zis.getNextEntry();
		}
        
        zis.closeEntry();
		zis.close();
			
	}
	
	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
	    File destFile = new File(destinationDir, zipEntry.getName());

	    String destDirPath = destinationDir.getCanonicalPath();
	    String destFilePath = destFile.getCanonicalPath();

	    if (!destFilePath.startsWith(destDirPath + File.separator)) {
	        throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
	    }

	    return destFile;
	}

	
	
}
