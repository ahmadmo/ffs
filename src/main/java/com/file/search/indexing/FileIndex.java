package com.file.search.indexing;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ahmad
 */
public final class FileIndex {

    private final ConcurrentHashMap<String, Set<File>> fileGroups;
    private final ConcurrentHashMap<IndexedFile, Set<IndexedFile>> dirs;

    public FileIndex(ConcurrentHashMap<String, Set<File>> fileGroups, ConcurrentHashMap<IndexedFile, Set<IndexedFile>> dirs) {
        this.fileGroups = fileGroups;
        this.dirs = dirs;
    }

    public ConcurrentHashMap<String, Set<File>> getFileGroups() {
        return fileGroups;
    }

    public ConcurrentHashMap<IndexedFile, Set<IndexedFile>> getDirs() {
        return dirs;
    }

}
