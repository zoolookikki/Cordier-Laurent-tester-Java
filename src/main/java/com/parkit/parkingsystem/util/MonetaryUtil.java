package com.parkit.parkingsystem.util;

public class MonetaryUtil {
    
    // prevent instantiation
    private MonetaryUtil () {
    }
    
    public static double round(double value) {
        // rounded to the nearest then move the comma to round to 2 decimal places.
        return Math.round(value * 100.0) / 100.0;  
    }
}
