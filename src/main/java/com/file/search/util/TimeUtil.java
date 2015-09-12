package com.file.search.util;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author ahmad
 */
public final class TimeUtil {

    private TimeUtil() {
    }

    public static String normalizeFormat(long time, TimeUnit unit) {
        long millis = MILLISECONDS.convert(time, unit);
        double seconds = Math.round((millis / 1000.0) * 100.0) / 100.0;
        return seconds > 1 ? seconds + " sec" : millis + " ms";
    }

}
