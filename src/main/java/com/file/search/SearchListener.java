package com.file.search;

/**
 * @author ahmad
 */
public interface SearchListener {

    void onStart(long when, String pattern);

    void onResult(String path);

    void onChangeDirectory(String path);

    void onComplete(long when);

}
