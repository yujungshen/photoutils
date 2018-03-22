package com.yujung.tools.photoutils;

public enum MediaFileFormat {
	ANDRIOD_SCREEN_SHOT("xxxTODO"),
	ANDRIOD_GALAXY_S4_PHOTO1("([0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9])_(.*)"),
	ANDRIOD_GALAXY_S4_PHOTO2("([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]) (.*)"),
	ANDRIOD_INFUSE_4G_PHOTO("([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9])(.*)"),
	ANDRIOD_INFUSE_4G_VIDEO("(video-)([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9])(.*)"),
	
	//  November 30, 2012
	//      01 - december 30, 2012 - place
	//      January 1, 2013 - hello
	DIR_PATTERN_LONG("([0-9]*)([ ]?[-]?[ ]?)([A-Za-z]+[^-]*)([-]?.*)"),
	
	// 01-31-2011
    // 02-20-2012 - place
	DIR_PATTERN_SHORT("([0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9])([ ]?[-]?.*)");
	
	private String pattern;
	
	private MediaFileFormat(String pattern) {
		this.pattern = pattern;
	}
	
	public String getPattern() {
		return pattern;
	}
}
