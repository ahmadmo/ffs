package com.file.search;

import java.nio.file.Path;

/**
 * @author ahmad
 */
public interface FileMatcher {

    String getPattern();

    void setPattern(String query);

    boolean isHiddenFilesIncluded();

    void setHiddenFilesIncluded(boolean hiddenFilesIncluded);

    boolean isCaseInsensitive();

    void setCaseInsensitive(boolean caseSensitive);

    boolean matchFileName(String fileName);

    boolean matchFileAttribute(Path path);

}
