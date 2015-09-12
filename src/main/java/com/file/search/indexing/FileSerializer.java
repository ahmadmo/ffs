package com.file.search.indexing;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.File;

/**
 * @author ahmad
 */
public final class FileSerializer extends Serializer<File> {

    @Override
    public void write(Kryo kryo, Output output, File object) {
        kryo.writeObject(output, object.getAbsolutePath());
    }

    @Override
    public File read(Kryo kryo, Input input, Class<File> type) {
        return new File(kryo.readObject(input, String.class));
    }

}
