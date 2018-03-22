package com.yujung.tools.photoutils.old;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;


public class PhotoUtils7 {
	// the photo file names are in the following pattern:
	//		November 30, 2012
	//		01 - december 30, 2012 - place
	//		January 1, 2013 - hello
	private final static String PHOTO_DIR_NAME_PATTERN_LONG = "([0-9]*)([ ]?[-]?[ ]?)([A-Za-z]+[^-]*)([-]?.*)";
	
	// short format:
	// 01-31-2011
	// 02-20-2012 - place
	private final static String PHOTO_DIR_NAME_PATTERN_SHORT = "([0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9])([ ]?[-]?.*)";
	
	// Andriod Infuse 4g file name format:
	// 2012-01-08 12.06.21
	final static String PHOTO_NAME_PATTERN_ANDRIOD_INFUSE_4G = "([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9])(.*)";
	final static String VIDEO_NAME_PATTERN_ANDRIOD_INFUSE_4G = "(video-)([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9])(.*)";
	
	// Samsung Galaxy S4 file name format
	// 20131001_140520.xxx
	private final static String FILE_NAME_PATTERN_ANDRIOD_GALAXY_S4_1 = "([0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9])_(.*)";
	private final static String FILE_NAME_PATTERN_ANDRIOD_GALAXY_S4_2 = "([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]) (.*)";
	
	private final static SimpleDateFormat FILE_FORMAT_SHORT = new SimpleDateFormat("MM-dd-yyyy");
	private final static SimpleDateFormat FILE_FORMAT_LONG = new SimpleDateFormat("MMMMM dd, yyyy");
	final static SimpleDateFormat DATE_FORMAT_YEAR_FIRST = new SimpleDateFormat("yyyy-MM-dd");
	private final static SimpleDateFormat DATE_FORMAT_ANDROID_GALAXY_S4_1 = new SimpleDateFormat("yyyyMMdd");
	private final static SimpleDateFormat DATE_FORMAT_ANDROID_GALAXY_S4_2 = new SimpleDateFormat("yyyy-MM-dd");
	
	static HashMap<String, File> prepareForSort(String destPath, String sortPath) throws IOException {
		// 1. check that from and to paths are both directories
		FileSystem local = FileSystems.getDefault();
		Path dest = local.getPath(destPath);
		System.out.println("Destination path: " + destPath);
		System.out.println("Path to sort: " + sortPath);
		BasicFileAttributes destAttr = Files.readAttributes(dest, BasicFileAttributes.class);
		if (!destAttr.isDirectory()) {
			System.out.println("Destination " + destPath + " is not a directory, nothing to do.");
			System.exit(1);
		}
		Path sort = local.getPath(sortPath);
		BasicFileAttributes sortAttr = Files.readAttributes(sort, BasicFileAttributes.class);
		if (!sortAttr.isDirectory()) {
			System.out.println("SortPath " + sortPath + " is not a directory, nothing to do.");
			System.exit(2);
		}

		Pattern photoDirPattern = Pattern.compile(PHOTO_DIR_NAME_PATTERN_SHORT);

		// 2. find all first level directories in fromPath and map it, prefix
		// are Yearxxxx
		System.out.println("--> Catalog destination path...");
		HashMap<String, File> dirMap = new HashMap<String, File>();
		File destFile = dest.toFile();
		File[] fileList = destFile.listFiles();
		for (File file : fileList) {
			if (file.getName().startsWith("Year")) {
				File[] yearList = file.listFiles();
				for (File subfile : yearList) {
					Matcher m = photoDirPattern.matcher(subfile.getName());
					if (m.matches()) {
						// find the date and put the File in hashMap
						String dateStr = m.group(1).trim();
						System.out .println("Found dir " + subfile.getAbsolutePath() + " --> " + dateStr);
						dirMap.put(dateStr, subfile);
					} else {
						System.out.println("Skipping " + subfile.getAbsolutePath());
					}
				}
			}
		}

		return dirMap;
	}
	
	public static void sortFilesByDate(String destPath, String sortPath, boolean testOnly) throws IOException, ImageProcessingException, ParseException {
		HashMap<String, File> dirMap = prepareForSort(destPath, sortPath);
		
		// recursively find all files in sortPath and put it into the destPath
		System.out.println("--> Sort files based on creation time, testOnlyMode=" + testOnly);
		sortFilesByDate(new File(sortPath), new File(destPath), FileSystems.getDefault().getSeparator(), dirMap, testOnly);
	}
	
	private static void sortFilesByDate(File file, File destRoot, String fileSeparator, HashMap<String, File> dirMap, boolean testOnly) throws IOException, ImageProcessingException, ParseException {
		if (file.isDirectory()) {
			File[] fileList = file.listFiles();
			for (File f : fileList) {
				sortFilesByDate(f, destRoot, fileSeparator, dirMap, testOnly);
			}
		} else {
			// first see if we can get the date of the pictures taken
			if (file.getName().toUpperCase().endsWith(".MOV") || file.getName().toUpperCase().endsWith(".AVI") || file.getName().toUpperCase().endsWith(".MP4")) {
				Date fileDate = getVideoRecordDate(file);
				sortFile(fileDate, file, destRoot, fileSeparator, dirMap, testOnly);
			} else {
				// rest are images, use date take to sort
				try {
					Metadata metadata = ImageMetadataReader.readMetadata(file);
					// obtain the Exif directory
					ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
					Date date = null;
					if (directory != null) {
						// query the tag's value
						date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
					}
					if (date == null) {
						System.out.println("*** Cannot find Exif date, using file creation date for " + file.getCanonicalPath());
						// some parameic picture does not have Exif info, just use creation date
						BasicFileAttributes attr = Files.readAttributes(Paths.get(file.getCanonicalPath()), BasicFileAttributes.class);
						date = new Date(attr.creationTime().toMillis());
					}
					//System.out.println("creationTime: " + date);
					//System.out.println(file.getName() + " --> " + date);
					sortFile(date, file, destRoot, fileSeparator, dirMap, testOnly);
				} catch (ImageProcessingException e) {
					System.out.println("Probably not an image, skipping " + file.getName());
				}
			}
			

		}
	}
	
	public static void sortFileAndroidGalaxyS4(String destPath, String sortPath, boolean testOnly, String fileNamePattern, SimpleDateFormat dateFormat) throws IOException, ParseException {
		HashMap<String, File> dirMap = prepareForSort(destPath, sortPath);
		
		// recursively find all files in sortPath and put it into the destPath
		System.out.println("--> Sort files based on Android file name");
		File sortDir = new File(sortPath);
		File[] fileList = sortDir.listFiles();
		Pattern fileDirPattern = Pattern.compile(fileNamePattern);
		File destRoot = new File(destPath);
		for (File file : fileList) {
			Matcher m = fileDirPattern.matcher(file.getName());
			String dateStr = "";
			if (m.matches()) {
				dateStr = m.group(1);
			} else {
				System.out.println("Skipping " + file.getName());
				continue;
			}
			Date fileDate = dateFormat.parse(dateStr);
			sortFile(fileDate, file, destRoot, FileSystems.getDefault().getSeparator(), dirMap, testOnly);
		}
	}
	
	static void sortFile(Date fileDate, File file, File destRoot, String fileSeparator, HashMap<String, File> dirMap, boolean testOnly) throws IOException {
		String dateStr = FILE_FORMAT_SHORT.format(fileDate);
		File destDir = dirMap.get(dateStr);
		if (destDir == null) {
			String toDir = destRoot.getAbsolutePath() + fileSeparator + "Year" + dateStr.substring(6, 10) + fileSeparator + dateStr;
			destDir = new File(toDir);
			if (!destDir.exists()) {
				destDir.mkdirs();
				dirMap.put(dateStr, destDir);
			}
		}
		String newPath = destDir.getAbsolutePath() + fileSeparator + file.getName();
		if (file.getCanonicalPath().toUpperCase().equals(newPath.toUpperCase())) {
			System.out.println(file.getCanonicalPath() + " is already at the right place!!!");
			return;
		}
	
		System.out.println("File " + file.getCanonicalPath() + " should go to " + newPath);
		if (!testOnly) {
			File newFile = new File(newPath);
			if (newFile.exists()) {
				System.out.println("\t\t file exists, not changed");
			} else {
				boolean success = file.renameTo(new File(newPath));
				if (!success) {
					System.out.println("Rename unsuccessful from " + file.getName() + " to " + newPath);
				}
			}
		}
	}
	
	// rename from long format to short format
	public static void renameFile(String destPath) throws IOException, ParseException {
		Pattern photoDirPattern = Pattern.compile(PHOTO_DIR_NAME_PATTERN_LONG);
		FileSystem local = FileSystems.getDefault();
		Path dest = local.getPath(destPath);
		System.out.println("Destination path: " + destPath);
		BasicFileAttributes destAttr = Files.readAttributes(dest, BasicFileAttributes.class);
		if (!destAttr.isDirectory()) {
			System.out.println("Destination " + destPath + " is not a directory, nothing to do.");
			return;
		}
		
		File destFile = dest.toFile();
		File[] fileList = destFile.listFiles();
		for (File file : fileList) {
			if (file.getName().startsWith("Year")) {
				File[] yearList = file.listFiles();
				for (File subfile : yearList) {
					if (!subfile.isDirectory()) {
						continue;
					}
					Matcher m = photoDirPattern.matcher(subfile.getName());
					if (m.matches()) {
						// find the date and put the File in hashMap
						String dateStr = m.group(3).trim();
						String suffix = m.group(4).trim();
						//System.out.println("Found dir " + subfile.getAbsolutePath() + " --> " + dateStr);
						Date date = FILE_FORMAT_LONG.parse(dateStr);
						String newName = subfile.getParentFile().getAbsolutePath() + local.getSeparator() + FILE_FORMAT_SHORT.format(date);
						if (suffix.length() > 0) {
							newName += " " + suffix;
						}
						System.out.println("\t\tRename to " + newName);
						if (file.getCanonicalPath().toUpperCase().equals(newName)) {
							System.out.println(file.getCanonicalPath() + "alreay at the right place!!!");
							continue;
						}
						boolean success = subfile.renameTo(new File(newName));
						if (!success) {
							System.out.println("Rename unsuccessful from " + subfile.getName() + " to " + newName);
						}
					} else {
						System.out.println("Skipping " + subfile.getAbsolutePath());
					}
				}
			}
		}
	}
	
	private static Date getVideoRecordDate(File file) throws IOException, ParseException {
		// A Runtime object has methods for dealing with the OS
	    Runtime r = Runtime.getRuntime();
	    Process p;     // Process tracks one external native process
	    BufferedReader is = null;  // reader for output of process
	    String line;
	    
	    // Our argv[0] contains the program to run; remaining elements
	    // of argv contain args for the target program. This is just
	    // what is needed for the String[] form of exec.
	    
	    p = r.exec(new String[] {"c:\\mediainfo\\MediaInfo.exe", file.getCanonicalPath()});
	    
	    Date date = null;
	    // getInputStream gives an Input stream connected to
	    // the process p's standard output. Just use it to make
	    // a BufferedReader to readLine() what the program writes out.
	    try {
		    is = new BufferedReader(new InputStreamReader(p.getInputStream()));
	
		    // find "Recorded Date"
		    while ((line = is.readLine()) != null) {
		    	if (line.startsWith("Encoded date")) {
		    		//System.out.println(line);
		    		String[] segments = line.split(":");
		    		String dateStr = segments[1].substring(4, 15);
		    		//System.out.println("date str: " + dateStr);
		    		date = DATE_FORMAT_YEAR_FIRST.parse(dateStr);
		    		//System.out.println("Encoded date: " + date);
		    		break;
		    	}
		    }
		    
		    if (date == null) {
			    System.out.println("*** Cannot find video encoded" +
			    		" date, using file creation date for " + file.getCanonicalPath());
				// some parameic picture does not have Exif info, just use creation date
				BasicFileAttributes attr = Files.readAttributes(Paths.get(file.getCanonicalPath()), BasicFileAttributes.class);
				date = new Date(attr.creationTime().toMillis());
		    }
	    } finally {
	    	if (is != null) {
	    		is.close();
	    	}
	    }
	
	    System.out.flush();
		try {
			p.waitFor(); // wait for process to complete
		} catch (InterruptedException e) {
			System.err.println(e); // "Can'tHappen"

		}
		
		return date;
	}

	public static void copyFileName(String destPath, String sourcePath, boolean testOnly) throws IOException, ParseException {
		Pattern photoDirPattern = Pattern.compile(PHOTO_DIR_NAME_PATTERN_LONG);
		FileSystem local = FileSystems.getDefault();
		Path source = local.getPath(sourcePath);
		Path dest = local.getPath(destPath);
//		System.out.println("Destination path: " + destPath);
//		BasicFileAttributes destAttr = Files.readAttributes(dest, BasicFileAttributes.class);
//		if (!destAttr.isDirectory()) {
//			System.out.println("Destination " + destPath + " is not a directory, nothing to do.");
//			return;
//		}
		
		File sourceFile = source.toFile();
		for (File file : sourceFile.listFiles()) {
			Matcher m = photoDirPattern.matcher(file.getName());
			if (m.matches()) {
//				for (int i = 0; i <= m.groupCount(); i++) {
//					System.out.println("group " + i + " = " + m.group(i));
//				}
				// find the date and put the File in hashMap
				String dateStr = m.group(3).trim();
				String suffix = m.group(4).trim();
				Date date = FILE_FORMAT_LONG.parse(dateStr);
				if (suffix.length() > 0) {
					String dateDir = FILE_FORMAT_SHORT.format(date);
					String year = dateDir.substring(6);
					String newName = destPath + FileSystems.getDefault().getSeparator() + "Year" + year +  
						FileSystems.getDefault().getSeparator() + dateStr + suffix;
					System.out.println("old name" + file.getAbsolutePath() + "\t\tRename to " + newName);
					
					
//					boolean success = file.renameTo(new File(newName));
//					if (!success) {
//						System.out.println("Rename unsuccessful from " + subfile.getName() + " to " + newName);
//					}
				} else {
					continue;
				}
			} else {
				System.out.println("Skipping " + file.getAbsolutePath());
			}
		}
	}
	
	public static void main(String[] args) throws Exception { 
		// pass in destPath and sortPath
		// PhotoUtils7.sortFilesByCreationTime(args[0], args[1]);
		
		// rename android files for infuse 4g
		// PhotoUtils7.sortFileAndroidInfuse4g(args[0], args[1]);
		
		// move files for galaxy s4
		if (args[0].equals("galaxys4_1")) {
			PhotoUtils7.sortFileAndroidGalaxyS4(args[1], args[2], Boolean.parseBoolean(args[3]), FILE_NAME_PATTERN_ANDRIOD_GALAXY_S4_1, DATE_FORMAT_ANDROID_GALAXY_S4_1);
		} else if (args[0].equals("galaxys4_2")) {
			PhotoUtils7.sortFileAndroidGalaxyS4(args[1], args[2], Boolean.parseBoolean(args[3]), FILE_NAME_PATTERN_ANDRIOD_GALAXY_S4_2, DATE_FORMAT_ANDROID_GALAXY_S4_2);
		} else if (args[0].equals("date")) {
			PhotoUtils7.sortFilesByDate(args[1], args[2], Boolean.parseBoolean(args[3]));
		} else if (args[0].equals("copyDescription")) {
			PhotoUtils7.copyFileName(args[1], args[2], Boolean.parseBoolean(args[3]));
		}
	
		
		// rename files
		//PhotoUtils7.renameFile(args[0]);
	}
}
