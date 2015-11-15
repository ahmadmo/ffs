package com.file.search;

import java.nio.file.Path;
import java.util.List;

/**
 * @author ahmad
 */
public interface FileMatcher {

    String getName();

    void setName(String name);

    boolean isHiddenFilesIncluded();

    void setHiddenFilesIncluded(boolean hiddenFilesIncluded);

    boolean isCaseInsensitive();

    void setCaseInsensitive(boolean caseSensitive);

    List<Path> getBaseDirectories();

    void setBaseDirectories(List<Path> baseDirectories);

    boolean matchFileName(String fileName);

    boolean matchDirectory(Path path);

}
