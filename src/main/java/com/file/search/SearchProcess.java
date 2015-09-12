package com.file.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;

/**
 * @author ahmad
 */
public final class SearchProcess {

    public static final int DEFAULT_PARALLELISM = Runtime.getRuntime().availableProcessors();

    private final File[] dirs;
    private final Consumer<File> action;
    private final ForkJoinPool pool;

    public SearchProcess(File[] dirs, Consumer<File> action) {
        this(dirs, action, DEFAULT_PARALLELISM);
    }

    public SearchProcess(File[] dirs, Consumer<File> action, int parallelism) {
        this.dirs = dirs;
        this.action = action;
        pool = new ForkJoinPool(parallelism);
    }

    public void doProcess() {
        for (File dir : dirs) {
            pool.invoke(new FolderProcessor(dir));
        }
        pool.shutdown();
    }

    private final class FolderProcessor extends RecursiveAction {

        private final File dir;

        private FolderProcessor(File dir) {
            this.dir = dir;
        }

        @Override
        protected void compute() {
            action.accept(dir);
            final File[] files = dir.listFiles();
            if (files != null) {
                final List<RecursiveAction> actions = new ArrayList<>();
                for (File file : files) {
                    if (file.isDirectory()) {
                        actions.add(new FolderProcessor(file));
                    } else {
                        action.accept(file);
                    }
                }
                invokeAll(actions);
                actions.clear();
            }
        }

    }

}