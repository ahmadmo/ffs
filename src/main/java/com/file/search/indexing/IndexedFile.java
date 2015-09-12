package com.file.search.indexing;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author ahmad
 */
public final class IndexedFile implements Cloneable, Serializable {

    private static final long serialVersionUID = 2366750191119019642L;

    private static final Map<Integer, IndexedFile> CACHE = new ConcurrentHashMap<>();

    private final File file;
    private volatile Long lastModified;

    private final AtomicReferenceFieldUpdater<IndexedFile, Long> lastModifiedUpdater
            = AtomicReferenceFieldUpdater.newUpdater(IndexedFile.class, Long.class, "lastModified");

    private IndexedFile(File file, Long lastModified) {
        this.file = file;
        this.lastModified = lastModified;
    }

    public File getFile() {
        return file;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        lastModifiedUpdater.set(this, lastModified);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && obj instanceof IndexedFile && file.equals(((IndexedFile) obj).file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    protected IndexedFile clone() {
        IndexedFile clone = null;
        try {
            clone = (IndexedFile) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clone;
    }

    public static IndexedFile index(File file) {
        return index(file, null);
    }

    public static IndexedFile index(File file, Long lastModified) {
        final int hash = file.hashCode();
        IndexedFile indexedFile = CACHE.get(hash);
        if (indexedFile == null) {
            final IndexedFile i = CACHE.putIfAbsent(hash, indexedFile = new IndexedFile(file, lastModified));
            if (i != null) {
                indexedFile = i;
            }
        } else if (lastModified != null) {
            indexedFile.setLastModified(lastModified);
        }
        return indexedFile;
    }

}
