package com.demo.fde.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class Extract {

	private String archive;
	private String outputDirectory;
	private File outputDirectoryFile;
	private boolean test;
	private String filterRegex;
	private String[] filterArray;

	public Extract(String archive, String outputDirectory, boolean test, String filter) {
		this.archive = archive;
		this.outputDirectory = outputDirectory;
		this.test = test;
		this.filterRegex = filterToRegex(filter);
	}

	public void extract() throws ExtractionException {
		checkArchiveFile();
		prepareOutputDirectory();
		extractArchive();
	}

	private void prepareOutputDirectory() throws ExtractionException {
		outputDirectoryFile = new File(outputDirectory);
		if (!outputDirectoryFile.exists()) {
			outputDirectoryFile.mkdirs();
		} /*else {
			if (outputDirectoryFile.list().length != 0) {
				throw new ExtractionException("Output directory not empty: "
						+ outputDirectory);
			}
		}*/
	}

	private void checkArchiveFile() throws ExtractionException {
		if (!new File(archive).exists()) {
			throw new ExtractionException("Archive file not found: " + archive);
		}
		if (!new File(archive).canRead()) {
			System.out.println("Can't read archive file: " + archive);
		}
	}

	public void extractArchive() throws ExtractionException {
		RandomAccessFile randomAccessFile;
		boolean ok = false;
		try {
			randomAccessFile = new RandomAccessFile(archive, "r");
		} catch (FileNotFoundException e) {
			throw new ExtractionException("File not found", e);
		}
		try {
			extractArchive(randomAccessFile);
			ok = true;
		} finally {
			try {
				randomAccessFile.close();
			} catch (Exception e) {
				if (ok) {
					throw new ExtractionException("Error closing archive file",	e);
				}
			}
		}
	}

	private static String filterToRegex(String filter) {
		if (filter == null) {
			return null;
		}
		return "\\Q" + filter.replace("*", "\\E.*\\Q") + "\\E";
	}

	private void extractArchive(RandomAccessFile file) throws ExtractionException {
		IInArchive inArchive;
		boolean ok = false;
		try {
			inArchive = SevenZip.openInArchive(null,
					new RandomAccessFileInStream(file));
		} catch (SevenZipException e) {
			throw new ExtractionException("Error opening archive", e);
		}
		try {
			
			int[] ids = null; // All items
			if (filterRegex != null) {
				ids = filterIds(inArchive, filterRegex);
			}
			inArchive.extract(ids, test, new ExtractCallback(inArchive, outputDirectoryFile));
			ok = true;
		} catch (SevenZipException e) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Error extracting archive '");
			stringBuilder.append(archive);
			stringBuilder.append("': ");
			stringBuilder.append(e.getMessage());
			if (e.getCause() != null) {
				stringBuilder.append(" (");
				stringBuilder.append(e.getCause().getMessage());
				stringBuilder.append(')');
			}
			String message = stringBuilder.toString();

			throw new ExtractionException(message, e);
		} finally {
			try {
				inArchive.close();
			} catch (SevenZipException e) {
				if (ok) {
					throw new ExtractionException("Error closing archive", e);
				}
			}
		}
	}

	private static int[] filterIds(IInArchive inArchive, String regex) throws SevenZipException {
		List<Integer> idList = new ArrayList<Integer>();
		
		int numberOfItems = inArchive.getNumberOfItems();
		
		Pattern pattern = Pattern.compile(regex);
		for (int i = 0; i < numberOfItems; i++) {
			String path = (String) inArchive.getProperty(i, PropID.PATH);
			String fileName = new File(path).getName();
			if (pattern.matcher(fileName).matches()) {
				idList.add(i);
			}
		}
		
		int[] result = new int[idList.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = idList.get(i);
		}
		return result ;
	}
	
}
