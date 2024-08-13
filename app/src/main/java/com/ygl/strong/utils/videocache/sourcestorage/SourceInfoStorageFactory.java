package com.ygl.strong.utils.videocache.sourcestorage;

import android.content.Context;

import com.ygl.strong.utils.videocache.sourcestorage.DatabaseSourceInfoStorage;
import com.ygl.strong.utils.videocache.sourcestorage.NoSourceInfoStorage;
import com.ygl.strong.utils.videocache.sourcestorage.SourceInfoStorage;

/**
 * Simple factory for {@link com.ygl.strong.utils.videocache.sourcestorage.SourceInfoStorage}.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public class SourceInfoStorageFactory {

    public static SourceInfoStorage newSourceInfoStorage(Context context) {
        return new DatabaseSourceInfoStorage(context);
    }

    public static SourceInfoStorage newEmptySourceInfoStorage() {
        return new NoSourceInfoStorage();
    }
}
