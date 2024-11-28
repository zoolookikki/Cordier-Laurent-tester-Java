package com.parkit.parkingsystem.util;

import java.util.Date;

public class DateUtil {
    
    // prevent instantiation
    private DateUtil () {
    }
    
    public static Date getDateNow() {
        return new Date();
    }
    
    public static Date getPastDate(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("invalid argument in getPastDate");
        }
        // L to avoid overflow int => see with spotbugs.
        long time = System.currentTimeMillis() - (minutes * 60L * 1000);
        return new Date(time) ;
    }
    
    public static Date getFutureDate(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("invalid argument in getFutureDate");
        }
        long time = System.currentTimeMillis() + (minutes * 60L * 1000);
        return new Date(time) ;
    }
}
