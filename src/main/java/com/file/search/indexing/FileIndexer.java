package com.file.search.indexing;

import com.file.search.FileMatcher;
import com.file.search.SearchProcess;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.file.search.indexing.IndexedFile.index;
import static com.file.search.util.RegexUtils.escape;

/**
 * @author ahmad
 */
public final class FileIndexer {

    private static final String PATTERN_FORMAT = "\\b(%s).*";
    private static final String DIR_DELIMITER = "|";

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
            final FileIndex index = FileIndexSerializer.deserializeIndex();
            if (index == null) {
                fileGroups = new ConcurrentHashMap<>();
                dirs = new ConcurrentHashMap<>();
                System.out.print("\nwaiting for index job ... ");
                new SearchProcess(roots, this::group).doProcess();
                System.out.println("done.\n");
            } else {
                fileGroups = index.getFileGroups();
                dirs = index.getDirs();
            }
            crawler.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                crawler.stop();
                FileIndexSerializer.serializeIndex(new FileIndex(fileGroups, dirs));
            }));
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

    public List<String> find(FileMatcher matcher, List<Path> baseDirs) {
        if (baseDirs == null || baseDirs.isEmpty()) {
            return Collections.emptyList();
        }
        final StringJoiner j = new StringJoiner(DIR_DELIMITER);
        for (Path dir : baseDirs) {
            j.add(escape(dir.toString()));
        }
        final Pattern p = Pattern.compile(String.format(PATTERN_FORMAT, j), Pattern.CASE_INSENSITIVE);
        return fileGroups.keySet().parallelStream()
                .filter(matcher::matchFileName)
                .flatMap(s -> fileGroups.get(s).parallelStream())
                .filter(matcher::matchFileAttribute)
                .map(path -> path.toAbsolutePath().toString())
                .filter(s -> p.matcher(s).matches())
                .sorted()
                .collect(Collectors.toList());
    }

}
