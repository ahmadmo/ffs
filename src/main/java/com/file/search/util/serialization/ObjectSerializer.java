package com.file.search.util.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author ahmad
 */
public final class ObjectSerializer<T> implements AutoCloseable {

    private volatile T value;
    private final String source;
    private final Class<T> type;
    private final AtomicReferenceFieldUpdater<ObjectSerializer, Object> valueUpdater;
    private final boolean deleteOnClose;
    private final Kryo kryo = new Kryo();

    private Input input;

    public ObjectSerializer(String source, Class<T> type) {
        this(source, type, null, true);
    }

    public ObjectSerializer(String source, Class<T> type, T value) {
        this(source, type, value, true);
    }

    public ObjectSerializer(String source, Class<T> type, boolean deleteOnClose) {
        this(source, type, null, deleteOnClose);
    }

    public ObjectSerializer(String source, Class<T> type, T value, boolean deleteOnClose) {
        this.source = source;
        this.type = type;
        this.value = value;
        this.deleteOnClose = deleteOnClose;
        valueUpdater = AtomicReferenceFieldUpdater.newUpdater(ObjectSerializer.class, Object.class, "value");
        kryo.register(type);
    }

    public String getSource() {
        return source;
    }

    public Class<T> getType() {
        return type;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        valueUpdater.set(this, value);
    }

    public <S> void register(Class<S> type) {
        kryo.register(type);
    }

    public <S> void register(Class<S> type, int id) {
        kryo.register(type, id);
    }

    public <S> void register(Class<S> type, Serializer<S> serializer) {
        kryo.register(type, serializer);
    }

    public <S> void register(Class<S> type, Serializer<S> serializer, int id) {
        kryo.register(type, serializer, id);
    }

    public void flushToDisk() throws FileNotFoundException {
        final T t = value;
        if (t != null) {
            serialize(t);
            set(null);
        }
    }

    public T loadFromDisk() throws FileNotFoundException {
        final T newValue = deserialize();
        set(newValue);
        return newValue;
    }

    private synchronized void serialize(T t) throws FileNotFoundException {
        closeInput();
        try (Output output = new Output(new BufferedOutputStream(new FileOutputStream(source)))) {
            kryo.writeObject(output, t);
        }
        openInput();
    }

    private synchronized T deserialize() throws FileNotFoundException {
        openInput();
        return kryo.readObject(input, type);
    }

    private void openInput() throws FileNotFoundException {
        if (input == null) {
            input = new Input(new BufferedInputStream(new FileInputStream(source)));
        }
    }

    private void closeInput() {
        if (input != null) {
            input.close();
            input = null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && obj instanceof ObjectSerializer && source.equals(((ObjectSerializer) obj).source);
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }

    @Override
    public void close() throws IOException {
        set(null);
        closeInput();
        if (deleteOnClose) {
            Files.delete(Paths.get(source));
        }
    }

}