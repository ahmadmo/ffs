package com.file.search.indexing;

import com.file.search.FileMatcher;
import com.file.search.SearchProcess;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.file.search.indexing.IndexedFile.index;

/**
 * @author ahmad
 */
public final class FileIndexer {

    private final FileCrawler crawler;

    private ConcurrentHashMap<String, Set<Path>> fileGroups;
    private ConcurrentHashMap<IndexedFile, Set<IndexedFile>> dirs;

    public FileIndexer() {
        crawler = new FileCrawler(this);
        init();
    }

    private static String name(Path path) {
        Path name = path.getFileName();
        return name == null ? "" : name.toString();
    }

    private void init() {
        try {
            final Iterable<Path> roots = FileSystems.getDefault().getRootDirectories();
            Objects.requireNonNull(roots);
            if (!loadFromDisk()) {
                fileGroups = new ConcurrentHashMap<>();
                dirs = new ConcurrentHashMap<>();
                System.out.print("\nwaiting for index job ... ");
                new SearchProcess(roots, this::group).doProcess();
                System.out.println("done.\n");
            }
            crawler.start();
        } catch (Throwable e) {
            System.err.println("index job failed. (due to : " + e.getCause() + ")\n");
            System.console().readLine();
            System.exit(1);
        }
    }

    public void forEachGroup(BiConsumer<String, Set<Path>> action) {
        fileGroups.forEach(action);
    }

    public void forEachDir(BiConsumer<IndexedFile, Set<IndexedFile>> action) {
        dirs.forEach(action);
    }

    public Set<Path> getGroup(Path path) {
        return fileGroups.get(name(path));
    }

    public Set<IndexedFile> getChildren(IndexedFile dir) {
        return dirs.get(dir);
    }

    public Long getLastModified(Path path) {
        return index(path).getLastModified();
    }

    public void setLastModified(Path path) {
        index(path, System.currentTimeMillis());
    }

    public void group(Path file) {
        groupName(file);
        groupChildren(file);
    }

    private void groupName(Path path) {
        final String fileName = name(path);
        Set<Path> group = fileGroups.get(fileName);
        if (group == null) {
            final Set<Path> g = fileGroups.putIfAbsent(fileName, group = new SetFromMap<>());
            if (g != null) {
                group = g;
            }
        }
        group.add(path);
    }

    private void groupChildren(Path path) {
        final IndexedFile indexedFile = index(path, System.currentTimeMillis());
        if (Files.isDirectory(path) && !dirs.containsKey(indexedFile)) {
            dirs.put(indexedFile, new SetFromMap<>());
        }
        final Path parent = path.getParent();
        if (parent != null) {
            final IndexedFile p = index(parent);
            Set<IndexedFile> children = dirs.get(p);
            if (children == null) {
                final Set<IndexedFile> ch = dirs.putIfAbsent(p, children = new SetFromMap<>());
                if (ch != null) {
                    children = ch;
                }
            }
            children.add(indexedFile);
        }
    }

    public void removeFile(Path path) {
        removeGroup(path);
        removeChildren(path);
    }

    private void removeGroup(Path path) {
        final Set<Path> group = fileGroups.get(name(path));
        if (group != null) {
            group.remove(path);
        }
    }

    private void removeChildren(Path path) {
        final Path parent = path.getParent();
        if (parent != null) {
            final Set<IndexedFile> children = dirs.get(index(parent));
            if (children != null) {
                children.remove(index(path));
            }
        }
    }

    public void removeDir(IndexedFile dir) {
        final Set<IndexedFile> children = dirs.remove(dir);
        if (children != null) {
            for (IndexedFile child : children) {
                if (Files.isDirectory(child.getPath())) {
                    removeDir(child);
                } else {
                    removeFile(child.getPath());
                }
            }
        }
        removeFile(dir.getPath());
    }

    public List<Path> find(FileMatcher matcher) {
        return fileGroups.keySet().parallelStream()
                .filter(matcher::matchFileName)
                .flatMap(s -> fileGroups.get(s).stream())
                .filter(matcher::matchDirectory)
                .sorted()
                .collect(Collectors.toList());
    }

    public boolean loadFromDisk() {
        final FileIndex index = FileIndexSerializer.deserializeIndex();
        if (index != null) {
            fileGroups = index.getFileGroups();
            dirs = index.getDirs();
            return true;
        }
        return false;
    }

    public void saveToDisk() {
        FileIndexSerializer.serializeIndex(new FileIndex(fileGroups, dirs));
    }

}
