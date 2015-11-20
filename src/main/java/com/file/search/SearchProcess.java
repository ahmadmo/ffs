package com.file.search;

import com.file.search.util.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;

/**
 * @author ahmad
 */
public final class SearchProcess {

    public static final int DEFAULT_PARALLELISM = 2;

    private final Iterable<Path> dirs;
    private final Consumer<Path> action;
    private final ForkJoinPool pool;

    public SearchProcess(Iterable<Path> dirs, Consumer<Path> action) {
        this(dirs, action, DEFAULT_PARALLELISM);
    }

    public SearchProcess(Iterable<Path> dirs, Consumer<Path> action, int parallelism) {
        this.dirs = dirs;
        this.action = action;
        pool = new ForkJoinPool(parallelism);
    }

    public void doProcess() {
        for (Path dir : dirs) {
            if (Files.isReadable(dir)) {
                pool.invoke(new FolderProcessor(dir));
            }
        }
        pool.shutdown();
    }

    private final class FolderProcessor extends RecursiveAction {

        private static final long serialVersionUID = -6084708633762147774L;

        private final Path dir;

        private FolderProcessor(Path dir) {
            this.dir = dir;
        }

        @Override
        protected void compute() {
            action.accept(dir);
            final List<RecursiveAction> actions = new ArrayList<>();
            FileUtils.forEachEntry(dir, path -> {
                if (Files.isDirectory(path)) {
                    actions.add(new FolderProcessor(path));
                } else {
                    action.accept(path);
                }
            });
            invokeAll(actions);
            actions.clear();
        }

    }

}