package com.file.search.util.serialization;

import java.io.File;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * @author ahmad
 */
public final class Checksum {

    private static final char[] HEX = "0123456789abcdef".toCharArray();
    private static final int BUFFER_SIZE = 8192;

    private Checksum() {
    }

    public static String createChecksum(String source) {
        return createChecksum(Paths.get(source));
    }

    public static String createChecksum(File source) {
        return createChecksum(Paths.get(source.toURI()));
    }

    public static String createChecksum(Path source) {
        try (FileChannel inChannel = FileChannel.open(source, StandardOpenOption.READ)) {
            MappedByteBuffer mbb = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
            int count;
            byte[] bytes = new byte[BUFFER_SIZE];
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            while ((count = Math.min(mbb.remaining(), BUFFER_SIZE)) > 0) {
                mbb.get(bytes, 0, count);
                digest.update(bytes, 0, count);
            }
            return toHex(digest.digest());
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String toHex(byte[] bytes) {
        Objects.requireNonNull(bytes);
        final StringBuilder hex = new StringBuilder(2 * bytes.length);
        for (final byte b : bytes) {
            hex.append(HEX[(b & 0xF0) >> 4]).append(HEX[b & 0x0F]);
        }
        return hex.toString();
    }

}
