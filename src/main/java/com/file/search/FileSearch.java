package com.file.search;

import com.file.search.indexing.FileIndexer;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static com.file.search.util.RegexUtils.escape;

/**
 * @author ahmad
 */
public final class FileSearch {

    private static final String PATTERN_FORMAT = "\\b%s.*";

    private FileSearch() {
    }

    public static void search(final FileIndexer indexer, final FileMatcher matcher, final SearchListener listener, final List<Path> baseDirs) {
        listener.onStart(System.currentTimeMillis(), matcher.getPattern());
        final List<String> result = indexer.find(matcher, baseDirs);
        String path;
        for (Path dir : baseDirs) {
            path = dir.toString();
            listener.onChangeDirectory(path);
            final Pattern p = Pattern.compile(String.format(PATTERN_FORMAT, escape(path)), Pattern.CASE_INSENSITIVE);
            result.stream().filter(s -> p.matcher(s).matches()).forEach(listener::onResult);
        }
        listener.onComplete(System.currentTimeMillis());
        result.clear();
    }

}
