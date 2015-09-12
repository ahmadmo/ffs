package com.file.search;

import java.io.File;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.file.search.util.RegexUtil.escape;

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
    public boolean matchFileAttribute(File file) {
        return file.canRead() && (hiddenFilesIncluded || !file.isHidden());
    }

}
