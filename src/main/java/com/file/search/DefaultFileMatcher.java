package com.file.search;

import com.file.search.util.RegexUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Pattern;

/**
 * @author ahmad
 */
public final class DefaultFileMatcher implements FileMatcher {

    private static final String PATTERN_FORMAT = "^(%s).*";
    private static final String DIR_DELIMITER = "|";

    private String name;
    private Pattern namePattern;
    private List<Path> baseDirs;
    private Pattern baseDirsPattern;
    private boolean hiddenFilesIncluded;
    private boolean caseInsensitive;

    private static boolean isHidden(Path path) {
        try {
            return Files.isHidden(path);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        Objects.requireNonNull(name);
        this.name = name;
        namePattern = Pattern.compile(RegexUtils.escape(name), Pattern.CASE_INSENSITIVE);
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
    public List<Path> getBaseDirectories() {
        return baseDirs;
    }

    @Override
    public void setBaseDirectories(List<Path> baseDirectories) {
        baseDirs = baseDirectories;
        final StringJoiner joiner = new StringJoiner(DIR_DELIMITER);
        for (Path dir : baseDirs) {
            joiner.add(RegexUtils.escape(dir.toString()));
        }
        baseDirsPattern = Pattern.compile(String.format(PATTERN_FORMAT, joiner), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean matchFileName(String fileName) {
        return caseInsensitive ? namePattern.matcher(fileName).find() : fileName.contains(name);
    }

    @Override
    public boolean matchDirectory(Path path) {
        return (hiddenFilesIncluded || !isHidden(path))
                && baseDirsPattern.matcher(path.toString()).matches();
    }

}
