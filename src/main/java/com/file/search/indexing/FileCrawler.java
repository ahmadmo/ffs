package com.file.search.indexing;

import com.file.search.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * @author ahmad
 */
public final class FileCrawler {

    public static final long DEFAULT_UPDATE_INTERVAL_MILLIS = 5000L;

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
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    private static long getLastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return -1L;
        }
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
        final Path p = dir.getPath();
        if (Files.exists(p)) {
            if (getLastModified(p) > dir.getLastModified()) {
                dir.setLastModified(System.currentTimeMillis());
                checkUpdates(dir, children);
            }
        } else {
            indexer.removeDir(dir);
        }
    }

    private void checkUpdates(final IndexedFile dir, final Set<IndexedFile> children) {
        for (IndexedFile child : children) {
            final Path path = child.getPath();
            if (!Files.exists(path) && !Files.isDirectory(path)) {
                indexer.removeFile(path);
            }
        }
        FileUtils.forEachEntry(dir.getPath(), path -> {
            long lastModified = indexer.getLastModified(path);
            if (Files.isDirectory(path)) {
                if (lastModified == 0L) {
                    newDir(path);
                }
            } else if (lastModified == 0L) {
                indexer.group(path);
            } else if (getLastModified(path) > lastModified) {
                indexer.setLastModified(path);
            }
        });
    }

    private void newDir(final Path dir) {
        indexer.group(dir);
        FileUtils.forEachEntry(dir, path -> {
            if (Files.isDirectory(path)) {
                newDir(path);
            } else {
                indexer.group(path);
            }
        });
    }

}
