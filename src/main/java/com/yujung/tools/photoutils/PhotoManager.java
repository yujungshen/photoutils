package com.yujung.tools.photoutils;

public class PhotoManager {
    public static void main(String[] args) throws Exception {
        
        // pass in destPath and sortPath
        // PhotoUtils7.sortFilesByCreationTime(args[0], args[1]);
        
        // rename android files for infuse 4g
        // PhotoUtils7.sortFileAndroidInfuse4g(args[0], args[1]);
        
        // move files for galaxy s4
//        if (args[0].equals("galaxys4_1")) {
//            PhotoUtils7.sortFileAndroidGalaxyS4(args[1], args[2], Boolean.parseBoolean(args[3]), FILE_NAME_PATTERN_ANDRIOD_GALAXY_S4_1, DATE_FORMAT_ANDROID_GALAXY_S4_1);
//        } else if (args[0].equals("galaxys4_2")) {
//            PhotoUtils7.sortFileAndroidGalaxyS4(args[1], args[2], Boolean.parseBoolean(args[3]), FILE_NAME_PATTERN_ANDRIOD_GALAXY_S4_2, DATE_FORMAT_ANDROID_GALAXY_S4_2);
//        } else if (args[0].equals("date")) {
//            PhotoUtils7.sortFilesByDate(args[1], args[2], Boolean.parseBoolean(args[3]));
//        } else if (args[0].equals("copyDescription")) {
//            PhotoUtils7.copyFileName(args[1], args[2], Boolean.parseBoolean(args[3]));
//        }
    
        PhotoFileMover mover = new PhotoFileMover(args[0], args[1]);
        mover.sortByDate(Boolean.parseBoolean(args[2]));
    }
}
