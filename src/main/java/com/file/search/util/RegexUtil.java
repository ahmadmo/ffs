package com.file.search.util;

import java.util.regex.Pattern;

/**
 * @author ahmad
 */
public final class RegexUtil {

    private static final Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[\\{\\}\\(\\)\\[\\]\\.\\+\\*\\?\\^\\$\\\\\\|]");

    private RegexUtil(){}

    public static String escape(String s) {
        return SPECIAL_REGEX_CHARS.matcher(s).replaceAll("\\\\$0");
    }

}
