package com.file.search;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.file.search.util.TimeUtil.normalizeFormat;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author ahmad
 */
public final class DefaultSearchListener implements SearchListener {

    private static final BufferedWriter out = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(FileDescriptor.out), StandardCharsets.UTF_8), 512);

    private final BufferedWriter writer;
    private final boolean console;
    private final AtomicInteger total = new AtomicInteger(0);
    private final AtomicLong start = new AtomicLong(0);

    public DefaultSearchListener() {
        this(out, true);
    }

    public DefaultSearchListener(File outFile, boolean append) throws IOException {
        this(new BufferedWriter(new FileWriter(outFile, append)), false);
    }

    private DefaultSearchListener(BufferedWriter writer, boolean console) {
        this.writer = writer;
        this.console = console;
    }

    @Override
    public void onResult(String path) {
        try {
            writer.write(path);
            writer.newLine();
            if (console) {
                writer.flush();
            }
        } catch (IOException ignored) {
        }
        total.incrementAndGet();
    }

    @Override
    public void onChangeDirectory(String path) {
        try {
            writer.write(String.format("%n -> searching directory %s%n%n", path));
            if (console) {
                writer.flush();
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void onStart(long when, String pattern) {
        start.set(when);
        try {
            writer.write(String.format("[%s] search results for '%s' :%n", new Date(when), pattern));
            if (console) {
                writer.flush();
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void onComplete(long when) {
        String s = String.format("%ntotal results = %d, execution time = %s%n", total.get(), normalizeFormat(when - start.get(), MILLISECONDS));
        try {
            writer.newLine();
            writer.write(s);
            writer.flush();
            if (!console) {
                writer.close();
                out.write(s);
                out.flush();
            }
        } catch (IOException ignored) {
        }
    }

}