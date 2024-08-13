package com.ygl.strong.utils.videocache.sourcestorage;

import com.ygl.strong.utils.videocache.SourceInfo;
import com.ygl.strong.utils.videocache.sourcestorage.SourceInfoStorage;

/**
 * {@link com.ygl.strong.utils.videocache.sourcestorage.SourceInfoStorage} that does nothing.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public class NoSourceInfoStorage implements SourceInfoStorage {

    @Override
    public SourceInfo get(String url) {
        return null;
    }

    @Override
    public void put(String url, SourceInfo sourceInfo) {
    }

    @Override
    public void release() {
    }
}
