package com.file.search.indexing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class SetFromMap<E> extends AbstractSet<E> implements Set<E>, Serializable {

    private static final long serialVersionUID = 4536506202852597860L;

    private final Map<E, Boolean> m;
    private transient Set<E> s;

    public SetFromMap() {
        this(new ConcurrentHashMap<>());
    }

    public SetFromMap(Map<E, Boolean> map) {
        if (!map.isEmpty()) {
            throw new IllegalArgumentException("Map is non-empty");
        }
        m = map;
        s = map.keySet();
    }

    @Override
    public void clear() {
        m.clear();
    }

    @Override
    public int size() {
        return m.size();
    }

    @Override
    public boolean isEmpty() {
        return m.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return m.containsKey(o);
    }

    @Override
    public boolean remove(Object o) {
        return m.remove(o) != null;
    }

    @Override
    public boolean add(E e) {
        return m.put(e, Boolean.TRUE) == null;
    }

    @Override
    public Iterator<E> iterator() {
        return s.iterator();
    }

    @Override
    public Object[] toArray() {
        return s.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return s.toArray(a);
    }

    @Override
    public String toString() {
        return s.toString();
    }

    @Override
    public int hashCode() {
        return s.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o == this || s.equals(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return s.containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return s.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return s.retainAll(c);
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        s.forEach(action);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return s.removeIf(filter);
    }

    @Override
    public Spliterator<E> spliterator() {
        return s.spliterator();
    }

    @Override
    public Stream<E> stream() {
        return s.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return s.parallelStream();
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        s = m.keySet();
    }

}