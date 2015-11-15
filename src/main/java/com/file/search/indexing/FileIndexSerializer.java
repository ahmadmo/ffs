package com.file.search.indexing;

import com.file.search.util.serialization.ObjectSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;

import static com.file.search.util.serialization.Checksum.createChecksum;

/**
 * @author ahmad
 */
public final class FileIndexSerializer {

    private static final String INDEX_LOCATION = System.getProperty("user.home") + File.separator + ".ffs" + File.separator;
    private static final String G_INDEX = INDEX_LOCATION + "g_index.ser";
    private static final String D_INDEX = INDEX_LOCATION + "d_index.ser";

    static {
        try {
            Files.createDirectories(Paths.get(INDEX_LOCATION));
        } catch (IOException e) {
            System.exit(1);
        }
    }

    private FileIndexSerializer() {
    }

    public static FileIndexWrapper deserializeIndex() {
        final Preferences preferences = Preferences.userNodeForPackage(FileIndexSerializer.class);
        final String g_hash = preferences.get("g_hash", null);
        final String d_hash = preferences.get("d_hash", null);
        if (g_hash != null && d_hash != null) {
            if (Objects.equals(createChecksum(G_INDEX), g_hash) && Objects.equals(createChecksum(D_INDEX), d_hash)) {
                try (ObjectSerializer<ConcurrentHashMap> g_serializer = new ObjectSerializer<>(G_INDEX, ConcurrentHashMap.class, false);
                     ObjectSerializer<ConcurrentHashMap> d_serializer = new ObjectSerializer<>(D_INDEX, ConcurrentHashMap.class, false)) {
                    g_serializer.register(Path.class, new PathSerializer());
                    d_serializer.register(IndexedFile.class, new IndexedFileSerializer());
                    System.out.print("\nloading index files ... ");
                    @SuppressWarnings("unchecked")
                    final FileIndexWrapper index = new FileIndexWrapper(g_serializer.loadFromDisk(), d_serializer.loadFromDisk());
                    System.out.println("done.\n");
                    return index;
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void serializeIndex(final FileIndexWrapper index) {
        try (ObjectSerializer<ConcurrentHashMap> g_serializer = new ObjectSerializer<>(G_INDEX, ConcurrentHashMap.class, index.getFileGroups(), false);
             ObjectSerializer<ConcurrentHashMap> d_serializer = new ObjectSerializer<>(D_INDEX, ConcurrentHashMap.class, index.getDirs(), false)) {
            g_serializer.register(Path.class, new PathSerializer());
            d_serializer.register(IndexedFile.class, new IndexedFileSerializer());
            System.out.print("\nsaving index files ... ");
            g_serializer.flushToDisk();
            d_serializer.flushToDisk();
            final Preferences preferences = Preferences.userNodeForPackage(FileIndexSerializer.class);
            preferences.put("g_hash", createChecksum(G_INDEX));
            preferences.put("d_hash", createChecksum(D_INDEX));
            System.out.println("done.\n");
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }

}
