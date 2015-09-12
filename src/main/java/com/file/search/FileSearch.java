package com.file.search;

import com.file.search.indexing.FileIndexer;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import static com.file.search.util.RegexUtil.escape;

/**
 * @author ahmad
 */
public final class FileSearch {

    private static final String PATTERN_FORMAT = "\\b%s.*";

    private FileSearch() {
    }

    public static void search(final FileIndexer indexer, final FileMatcher matcher, final SearchListener listener, final File... baseDirs) {
        listener.onStart(System.currentTimeMillis(), matcher.getPattern());
        final List<String> result = indexer.find(matcher, baseDirs);
        String path;
        for (File dir : baseDirs) {
            path = dir.getAbsolutePath();
            listener.onChangeDirectory(path);
            final Pattern p = Pattern.compile(String.format(PATTERN_FORMAT, escape(path)), Pattern.CASE_INSENSITIVE);
            result.stream().filter(s -> p.matcher(s).matches()).forEach(listener::onResult);
        }
        listener.onComplete(System.currentTimeMillis());
        result.clear();
    }

}
