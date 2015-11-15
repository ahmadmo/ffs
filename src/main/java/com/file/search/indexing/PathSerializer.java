package com.file.search.indexing;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author ahmad
 */
public final class PathSerializer extends Serializer<Path> {

    @Override
    public void write(Kryo kryo, Output output, Path path) {
        kryo.writeObject(output, path.toString());
    }

    @Override
    public Path read(Kryo kryo, Input input, Class<Path> type) {
        return Paths.get(kryo.readObject(input, String.class));
    }

}
