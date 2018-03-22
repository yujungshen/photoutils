package com.yujung.tools.photoutils.old;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OldPhotoUtils7 {

	public static void sortFileAndroidInfuse4g(String destPath, String sortPath) throws IOException, ParseException {
		HashMap<String, File> dirMap = PhotoUtils7.prepareForSort(destPath, sortPath);
		
		// recursively find all files in sortPath and put it into the destPath
		System.out.println("--> Sort files based on Android file name");
		File sortDir = new File(sortPath);
		File[] fileList = sortDir.listFiles();
		Pattern photoDirPattern = Pattern.compile(PhotoUtils7.PHOTO_NAME_PATTERN_ANDRIOD_INFUSE_4G);
		Pattern videoDirPattern = Pattern.compile(PhotoUtils7.VIDEO_NAME_PATTERN_ANDRIOD_INFUSE_4G);
		File destRoot = new File(destPath);
		for (File file : fileList) {
			Matcher m = photoDirPattern.matcher(file.getName());
			Matcher v = videoDirPattern.matcher(file.getName());
			String dateStr = "";
			if (m.matches()) {
				dateStr = m.group(1);
			} else if (v.matches()) {
				dateStr = v.group(2);
			} else {
				System.out.println("Skipping " + file.getName());
				continue;
			}
			Date fileDate = PhotoUtils7.DATE_FORMAT_YEAR_FIRST.parse(dateStr);
			PhotoUtils7.sortFile(fileDate, file, destRoot, FileSystems.getDefault().getSeparator(), dirMap, false);
		}
	}

}
