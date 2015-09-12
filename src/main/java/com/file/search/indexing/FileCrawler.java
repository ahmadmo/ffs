package com.file.search.indexing;

import java.io.File;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * @author ahmad
 */
public final class FileCrawler {

    public static final long DEFAULT_UPDATE_INTERVAL_MILLIS = 2000L;

    private final FileIndexer indexer;
    private final long updateIntervalMillis;
    private final BiConsumer<IndexedFile, Set<IndexedFile>> updater = this::processDir;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public FileCrawler(FileIndexer indexer) {
        this(indexer, DEFAULT_UPDATE_INTERVAL_MILLIS);
    }

    public FileCrawler(FileIndexer indexer, long updateIntervalMillis) {
        this.indexer = indexer;
        this.updateIntervalMillis = updateIntervalMillis;
    }

    public void start() {
        if (running.compareAndSet(false, true)) {
            final Thread t = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted() && running.get()) {
                    indexer.forEachDir(updater);
                    sleep();
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    public void stop() {
        running.set(false);
    }

    private void sleep() {
        try {
            Thread.sleep(updateIntervalMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void processDir(final IndexedFile dir, final Set<IndexedFile> children) {
        final File d = dir.getFile();
        if (d.exists()) {
            if (d.lastModified() > dir.getLastModified()) {
                dir.setLastModified(System.currentTimeMillis());
                checkUpdates(dir, children);
            }
        } else {
            indexer.removeDir(dir);
        }
    }

    private void checkUpdates(final IndexedFile dir, final Set<IndexedFile> children) {
        for (IndexedFile child : children) {
            final File ch = child.getFile();
            if (!ch.exists() && !ch.isDirectory()) {
                indexer.removeFile(ch);
            }
        }
        final File[] files = dir.getFile().listFiles();
        if (files != null) {
            for (File file : files) {
                Long lastModified = indexer.getLastModified(file);
                if (file.isDirectory()) {
                    if (lastModified == null) {
                        newDir(file);
                    }
                } else if (lastModified == null) {
                    indexer.group(file);
                } else if (file.lastModified() > lastModified) {
                    indexer.setLastModified(file);
                }
            }
        }
    }

    private void newDir(final File dir) {
        indexer.group(dir);
        final File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    newDir(child);
                } else {
                    indexer.group(child);
                }
            }
        }
    }

}
