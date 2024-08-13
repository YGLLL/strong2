package com.ygl.strong.utils.videocache.file;

import com.ygl.strong.utils.videocache.file.FileCache;

import java.io.File;
import java.io.IOException;

/**
 * Declares how {@link FileCache} will use disc space.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public interface DiskUsage {

    void touch(File file) throws IOException;

}
