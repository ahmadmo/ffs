package com.file.search.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author ahmad
 */
public final class FileUtils {

    private FileUtils() {
    }

    public static void forEachEntry(Path dir, Consumer<Path> action) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, AccessibleFilter.FILTER)) {
            for (Path path : stream) {
                action.accept(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class AccessibleFilter implements DirectoryStream.Filter<Path> {

        static final AccessibleFilter FILTER = new AccessibleFilter();

        private AccessibleFilter() {
        }

        @Override
        public boolean accept(Path entry) {
            return Files.isReadable(entry);
        }

    }

}
