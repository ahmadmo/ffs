package com.file.search;

import com.file.search.indexing.FileIndexer;
import com.file.search.util.RegexUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author ahmad
 */
public final class FileSearch {

    private static final String PATTERN_FORMAT = "^%s.*";

    private FileSearch() {
    }

    public static void search(final FileIndexer indexer, final FileMatcher matcher, final SearchListener listener, final List<Path> baseDirs) {
        listener.onStart(System.currentTimeMillis(), matcher.getName());
        final List<Path> results = indexer.find(matcher);
        baseDirs.stream().map(Path::toString).forEach(dir -> {
            listener.onChangeDirectory(dir);
            final Pattern dirPattern = getDirPattern(dir);
            results.stream()
                    .map(Path::toString)
                    .filter(path -> dirPattern.matcher(path).matches())
                    .forEach(listener::onResult);
        });
        listener.onComplete(System.currentTimeMillis());
        results.clear();
    }

    private static Pattern getDirPattern(String dir) {
        return Pattern.compile(String.format(PATTERN_FORMAT, RegexUtils.escape(dir)), Pattern.CASE_INSENSITIVE);
    }

}
