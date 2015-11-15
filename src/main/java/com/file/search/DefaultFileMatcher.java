package com.file.search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.file.search.util.RegexUtils.escape;

/**
 * @author ahmad
 */
public final class DefaultFileMatcher implements FileMatcher {

    private String pattern;
    private boolean hiddenFilesIncluded;
    private boolean caseInsensitive;
    private Pattern p;

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public void setPattern(String pattern) {
        Objects.requireNonNull(pattern);
        this.pattern = pattern;
        p = Pattern.compile(escape(pattern), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean isHiddenFilesIncluded() {
        return hiddenFilesIncluded;
    }

    @Override
    public void setHiddenFilesIncluded(boolean hiddenFilesIncluded) {
        this.hiddenFilesIncluded = hiddenFilesIncluded;
    }

    @Override
    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    @Override
    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    @Override
    public boolean matchFileName(String fileName) {
        return caseInsensitive ? p.matcher(fileName).find() : fileName.contains(pattern);
    }

    @Override
    public boolean matchFileAttribute(Path path) {
        return Files.isReadable(path) && (hiddenFilesIncluded || !isHidden(path));
    }

    private static boolean isHidden(Path path) {
        try {
            return Files.isHidden(path);
        } catch (IOException e) {
            return false;
        }
    }

}
