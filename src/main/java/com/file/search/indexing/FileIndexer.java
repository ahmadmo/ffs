package com.file.search.indexing;

import com.file.search.FileMatcher;
import com.file.search.SearchProcess;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.file.search.indexing.IndexedFile.index;
import static com.file.search.util.RegexUtil.escape;

/**
 * @author ahmad
 */
public final class FileIndexer {

    private static final String PATTERN_FORMAT = "\\b(%s).*";
    private static final String DIR_DELIMITER = "|";

    private final FileCrawler crawler;

    private ConcurrentHashMap<String, Set<File>> fileGroups;
    private ConcurrentHashMap<IndexedFile, Set<IndexedFile>> dirs;

    public FileIndexer() {
        crawler = new FileCrawler(this);
        init();
    }

    private void init() {
        try {
            final File[] roots = File.listRoots();
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

    public void forEachGroup(BiConsumer<String, Set<File>> action) {
        fileGroups.forEach(action);
    }

    public void forEachDir(BiConsumer<IndexedFile, Set<IndexedFile>> action) {
        dirs.forEach(action);
    }

    public Set<File> getGroup(File file) {
        return fileGroups.get(file.getName());
    }

    public Set<IndexedFile> getChildren(IndexedFile dir) {
        return dirs.get(dir);
    }

    public Long getLastModified(File file) {
        return index(file).getLastModified();
    }

    public void setLastModified(File file) {
        index(file, System.currentTimeMillis());
    }

    public void group(File file) {
        groupName(file);
        groupChildren(file);
    }

    private void groupName(File file) {
        final String fileName = file.getName();
        Set<File> group = fileGroups.get(fileName);
        if (group == null) {
            final Set<File> g = fileGroups.putIfAbsent(fileName, group = new SetFromMap<>());
            if (g != null) {
                group = g;
            }
        }
        group.add(file);
    }

    private void groupChildren(File file) {
        final IndexedFile indexedFile = index(file, System.currentTimeMillis());
        if (file.isDirectory() && !dirs.containsKey(indexedFile)) {
            dirs.put(indexedFile, new SetFromMap<>());
        }
        final File parent = file.getParentFile();
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

    public void removeFile(File file) {
        removeGroup(file);
        removeChildren(file);
    }

    private void removeGroup(File file) {
        final Set<File> group = fileGroups.get(file.getName());
        if (group != null) {
            group.remove(file);
        }
    }

    private void removeChildren(File file) {
        final File parent = file.getParentFile();
        if (parent != null) {
            final Set<IndexedFile> children = dirs.get(index(parent));
            if (children != null) {
                children.remove(index(file));
            }
        }
    }

    public void removeDir(IndexedFile dir) {
        final Set<IndexedFile> children = dirs.remove(dir);
        if (children != null) {
            for (IndexedFile child : children) {
                if (child.getFile().isDirectory()) {
                    removeDir(child);
                } else {
                    removeFile(child.getFile());
                }
            }
        }
        removeFile(dir.getFile());
    }

    public List<String> find(FileMatcher matcher, File... baseDirs) {
        if (baseDirs == null || baseDirs.length == 0) {
            return Collections.emptyList();
        }
        final StringJoiner j = new StringJoiner(DIR_DELIMITER);
        for (File dir : baseDirs) {
            j.add(escape(dir.getAbsolutePath()));
        }
        final Pattern p = Pattern.compile(String.format(PATTERN_FORMAT, j), Pattern.CASE_INSENSITIVE);
        return fileGroups.keySet().parallelStream()
                .filter(matcher::matchFileName)
                .flatMap(s -> fileGroups.get(s).parallelStream())
                .filter(matcher::matchFileAttribute)
                .map(File::getAbsolutePath)
                .filter(s -> p.matcher(s).matches())
                .sorted()
                .collect(Collectors.toList());
    }

}
