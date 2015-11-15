package com.file.search.indexing;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author ahmad
 */
public final class IndexedFile implements Cloneable, Serializable {

    private static final long serialVersionUID = 7725700949544758581L;

    private static final Map<Integer, IndexedFile> CACHE = new ConcurrentHashMap<>();

    private final Path path;
    private volatile Long lastModified;

    private final AtomicReferenceFieldUpdater<IndexedFile, Long> lastModifiedUpdater
            = AtomicReferenceFieldUpdater.newUpdater(IndexedFile.class, Long.class, "lastModified");

    private IndexedFile(Path path, Long lastModified) {
        this.path = path;
        this.lastModified = lastModified;
    }

    public Path getPath() {
        return path;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        lastModifiedUpdater.set(this, lastModified);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && obj instanceof IndexedFile && path.equals(((IndexedFile) obj).path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return path.toString();
    }

    public static IndexedFile index(Path path) {
        return index(path, null);
    }

    public static IndexedFile index(Path path, Long lastModified) {
        final int hash = path.hashCode();
        IndexedFile indexedFile = CACHE.get(hash);
        if (indexedFile == null) {
            final IndexedFile i = CACHE.putIfAbsent(hash, indexedFile = new IndexedFile(path, lastModified));
            if (i != null) {
                indexedFile = i;
            }
        } else if (lastModified != null) {
            indexedFile.setLastModified(lastModified);
        }
        return indexedFile;
    }

}
