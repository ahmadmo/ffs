package com.file.search.indexing;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.nio.file.Paths;

import static com.file.search.indexing.IndexedFile.index;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author ahmad
 */
public final class IndexedFileSerializer extends Serializer<IndexedFile> {

    private static byte[] toByteArray(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xffL);
            value >>= 8;
        }
        return result;
    }

    private static long fromByteArray(byte[] bytes) {
        return (bytes[0] & 0xFFL) << 56
                | (bytes[1] & 0xFFL) << 48
                | (bytes[2] & 0xFFL) << 40
                | (bytes[3] & 0xFFL) << 32
                | (bytes[4] & 0xFFL) << 24
                | (bytes[5] & 0xFFL) << 16
                | (bytes[6] & 0xFFL) << 8
                | (bytes[7] & 0xFFL);
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    @Override
    public void write(Kryo kryo, Output output, IndexedFile indexedFile) {
        kryo.writeObject(output, concat(toByteArray(indexedFile.getLastModified()), indexedFile.toString().getBytes(UTF_8)));
    }

    @Override
    public IndexedFile read(Kryo kryo, Input input, Class<IndexedFile> aClass) {
        byte[] bytes = kryo.readObject(input, byte[].class);
        return index(Paths.get(new String(bytes, 8, bytes.length - 8, UTF_8)), fromByteArray(bytes));
    }

}
