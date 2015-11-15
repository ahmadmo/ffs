package com.file.search.indexing;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ahmad
 */
public final class FileIndex {

    private final ConcurrentHashMap<String, Set<Path>> fileGroups;
    private final ConcurrentHashMap<IndexedFile, Set<IndexedFile>> dirs;

    public FileIndex(ConcurrentHashMap<String, Set<Path>> fileGroups, ConcurrentHashMap<IndexedFile, Set<IndexedFile>> dirs) {
        this.fileGroups = fileGroups;
        this.dirs = dirs;
    }

    public ConcurrentHashMap<String, Set<Path>> getFileGroups() {
        return fileGroups;
    }

    public ConcurrentHashMap<IndexedFile, Set<IndexedFile>> getDirs() {
        return dirs;
    }

}
