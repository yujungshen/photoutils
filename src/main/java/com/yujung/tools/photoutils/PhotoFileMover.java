package com.yujung.tools.photoutils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhotoFileMover {

    private String destPath;
    private String sortPath;
    
    public PhotoFileMover(String sortPath, String destPath) {
        this.sortPath = sortPath;
        this.destPath = destPath;
    }
    
    public void sortByDate(boolean testOnly) throws IOException, ImageProcessingException, ParseException {
        Map<String, File> dirMap = prepareForSort();
        
        // recursively find all files in sortPath and put it into the destPath
        System.out.println("--> Sort files based on creation time, testOnlyMode=" + testOnly);
        sortFilesByDate(new File(sortPath), new File(destPath), FileSystems.getDefault().getSeparator(), dirMap, testOnly);
    }
    
    private void sortFilesByDate(File file, File destRoot, String fileSeparator, Map<String, File> dirMap, boolean testOnly) throws IOException, ImageProcessingException, ParseException {
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            for (File f : fileList) {
                sortFilesByDate(f, destRoot, fileSeparator, dirMap, testOnly);
            }
        } else {
            // first see if we can get the date of the pictures taken
            //if (file.getName().toUpperCase().endsWith(".MOV") || file.getName().toUpperCase().endsWith(".AVI") || file.getName().toUpperCase().endsWith(".MP4")) {
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
                System.out.println("WARNING: Probably not an image, skipping " + file.getName());
            } catch (IOException io) {
                System.out.println("ERROR: Cannot read file " + file.getName() + ": " + io.getLocalizedMessage());
            }
        }
    }
    
    private HashMap<String, File> prepareForSort() throws IOException {
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

        Pattern photoDirPattern = Pattern.compile(MediaFileFormat.DIR_PATTERN_SHORT.getPattern());

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
    
    void sortFile(Date fileDate, File file, File destRoot, String fileSeparator, Map<String, File> dirMap, boolean testOnly) throws IOException {
        String dateStr = DateFormatPattern.SHORT.getDateFormat().format(fileDate);
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
}
