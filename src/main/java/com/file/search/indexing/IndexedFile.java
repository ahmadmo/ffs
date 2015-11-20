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
    private final AtomicReferenceFieldUpdater<IndexedFile, Long> lastModifiedUpdater
            = AtomicReferenceFieldUpdater.newUpdater(IndexedFile.class, Long.class, "lastModified");

    private volatile Long lastModified;

    private IndexedFile(Path path, long lastModified) {
        this.path = path;
        this.lastModified = lastModified;
    }

    public static IndexedFile index(Path path) {
        return index(path, 0L);
    }

    public static IndexedFile index(Path path, long lastModified) {
        final int hash = path.hashCode();
        IndexedFile indexedFile = CACHE.get(hash);
        if (indexedFile == null) {
            final IndexedFile i = CACHE.putIfAbsent(hash, indexedFile = new IndexedFile(path, lastModified));
            if (i != null) {
                indexedFile = i;
            }
        } else if (lastModified != 0L) {
            indexedFile.setLastModified(lastModified);
        }
        return indexedFile;
    }

    public Path getPath() {
        return path;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
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

}
