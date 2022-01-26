package com.demo.fde.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZipException;

public class ExtractCallback implements IArchiveExtractCallback {
	
	private IInArchive inArchive;
	private int index;
	private OutputStream outputStream;
	private File file;
	private ExtractAskMode extractAskMode;
	private boolean isFolder;
	
	private File outputDirectoryFile;

	ExtractCallback(IInArchive inArchive, File outputDirectoryFile) {
		this.inArchive = inArchive;
		this.outputDirectoryFile = outputDirectoryFile;
	}

	@Override
	public void setTotal(long total) throws SevenZipException {

	}

	@Override
	public void setCompleted(long completeValue) throws SevenZipException {

	}

	@Override
	public ISequentialOutStream getStream(int index,
			ExtractAskMode extractAskMode) throws SevenZipException {
		closeOutputStream();

		this.index = index;
		this.extractAskMode = extractAskMode;
		this.isFolder = (Boolean) inArchive.getProperty(index,
				PropID.IS_FOLDER);

		if (extractAskMode != ExtractAskMode.EXTRACT) {
			// Skipped files or files being tested
			return null;
		}

		String path = (String) inArchive.getProperty(index, PropID.PATH);
		path = path.replaceAll("\\s*\\\\", "\\\\").trim();
		file = new File(outputDirectoryFile, path);

		if (isFolder) {
			createDirectory(file);
			return null;
		}

		createDirectory(file.getParentFile());

		try {
			outputStream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw new SevenZipException("Error opening file: "
					+ file.getAbsolutePath(), e);
		}

		return new ISequentialOutStream() {
			public int write(byte[] data) throws SevenZipException {
				try {
					outputStream.write(data);
				} catch (IOException e) {
					throw new SevenZipException("Error writing to file: "
							+ file.getAbsolutePath());
				}
				return data.length; // Return amount of consumed data
			}
		};
	}

	private void createDirectory(File parentFile) throws SevenZipException {
		if (!parentFile.exists()) {
			if (!parentFile.mkdirs()) {
				throw new SevenZipException("Error creating directory: "
						+ parentFile.getAbsolutePath());
			}
		}
	}

	private void closeOutputStream() throws SevenZipException {
		if (outputStream != null) {
			try {
				outputStream.close();
				outputStream = null;
			} catch (IOException e) {
				throw new SevenZipException("Error closing file: "
						+ file.getAbsolutePath());
			}
		}
	}

	@Override
	public void prepareOperation(ExtractAskMode extractAskMode)
			throws SevenZipException {

	}

	@Override
	public void setOperationResult(
			ExtractOperationResult extractOperationResult)
			throws SevenZipException {
		closeOutputStream();
		String path = (String) inArchive.getProperty(index, PropID.PATH);
		if (extractOperationResult != ExtractOperationResult.OK) {
			throw new SevenZipException("Invalid file: " + path);
		}

		if (!isFolder) {
			switch (extractAskMode) {
			case EXTRACT:
				System.out.println("Extracted " + path);
				break;
			case TEST:
				System.out.println("Tested " + path);

			default:
			}
		}
	}

}
