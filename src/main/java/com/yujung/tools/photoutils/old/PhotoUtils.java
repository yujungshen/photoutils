package com.yujung.tools.photoutils.old;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.StringTokenizer;


public class PhotoUtils {
	public static void fixPhotoPath(String dirPath) {
		File directory = new File(dirPath);
		if (!directory.isDirectory()) {
			System.out.println("Path " + dirPath + " is not a directory, nothing to do.");
			return;
		}
		
		// get a list of folders and sort by date, then number each folder
		File[] fileList = directory.listFiles();
//		Arrays.sort(fileList, new FilePathComparator());
		
		// now go through the file list and update path
		int count = 1;
		for (File file : fileList) {
			String fileName = file.getName();
			int dashIndex = fileName.indexOf("-");
			if (dashIndex >= 0) {
				String prefix = fileName.substring(0, dashIndex).trim();
				try {
					Integer.parseInt(prefix);
					fileName = fileName.substring(dashIndex + 1).trim();
				} catch (NumberFormatException nfe) {
					// do nothing
				}
			}
			fileName = getNumberedString(count) + "-" + fileName;
			System.out.println("Final file name: " + fileName);
			count++;
			// rename the folder
			File renameFile = new File(dirPath + System.getProperty("file.separator") + fileName);
			boolean success = file.renameTo(renameFile);
			if (!success) {
				System.out.println("Rename unsuccessful from " + file.getName() + " to " + fileName);
			}
		}
	}
	
	// this is just to fix year 2012 folder
	public static void fixPhotoPath2(String dirPath) {
		File directory = new File(dirPath);
		if (!directory.isDirectory()) {
			System.out.println("Path " + dirPath + " is not a directory, nothing to do.");
			return;
		}
		
		// get a list of folders and sort by date, then number each folder
		File[] fileList = directory.listFiles();
		
		// now go through the file list and update path
		for (File file : fileList) {
			String fileName = file.getName();
			String prefix = fileName.substring(0, 8);
			if (prefix.equals("Year2012")) {
			//try {
				//Integer.parseInt(prefix);
				fileName = fileName.substring(8).trim();
				System.out.println("fix file name: " + fileName);
				// rename the folder
				File renameFile = new File(dirPath + System.getProperty("file.separator") + fileName);
				boolean success = file.renameTo(renameFile);
				if (!success) {
					System.out.println("Rename unsuccessful from " + file.getName() + " to " + fileName);
				}
			//} catch (NumberFormatException nfe) {
				// do nothing
			}
			
		}
	}

	private static String getNumberedString(int num) {
		if (num < 10) {
			return "0" + num;
		}
		return String.valueOf(num);
	}
	private static String getFilePathDate(String name) {
		// strip out the old numbering in the from or the description in the back delimited by "-"
		// the file name can be in one the following format
		//     04 - January 20, 2012
		//	   June 23, 2012
		//	   05 - January, 5, 2012 - playing
		
		StringTokenizer tokenizer = new StringTokenizer(name, "-");
		String firstToken = tokenizer.nextToken().trim();
		// if first Token is a digit, then second token is what we want
		// if first token is not a digit, then first token is what we want
		try {
			Integer.parseInt(firstToken);
			return tokenizer.nextToken().trim();
		} catch (NumberFormatException nfe) {
			return firstToken;
		}
		
	}
	private final static SimpleDateFormat fileNameDateFormat = new SimpleDateFormat("MMMMM dd, yyyy");
//	public static class FilePathComparator implements Comparator<File> {
//
//		@Override
//		public int compare(File file1, File file2) {
//			String name1 = getFilePathDate(file1.getName());
//			String name2 = getFilePathDate(file2.getName());
//			
//			// strip out the old numbering in the from or the description in the back delimited by "-"
//			try {
//				Date fileDate1 = fileNameDateFormat.parse(name1);
//				Date fileDate2 = fileNameDateFormat.parse(name2);
//				return fileDate1.compareTo(fileDate2);
//			} catch (ParseException pe) {
//				System.out.println("Cannot parse file path, name1 = " + name1 + ", name2 = " + name2);
// 				throw new RuntimeException(pe);
//			}
//		}
//		
//	}
	
	public static void main(String[] args) {
		PhotoUtils.fixPhotoPath(args[0]);
	}
}
