package com.yujung.tools.photoutils;

import java.text.SimpleDateFormat;

public enum DateFormatPattern {
    SHORT("MM-dd-yyyy"),
    LONG("MMMMM dd, yyyy"),
    YEAR_FIRST("yyyy-MM-dd"),
    GALAXY_S4_1("yyyyMMdd");

    private String pattern;

    private DateFormatPattern(String pattern) {
        this.pattern = pattern;
    }

    public SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat(pattern);
    }
}
