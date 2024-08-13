package com.ygl.strong.utils.videocache.file;

import com.ygl.strong.utils.videocache.file.DiskUsage;

import java.io.File;
import java.io.IOException;

/**
 * Unlimited version of {@link com.ygl.strong.utils.videocache.file.DiskUsage}.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
public class UnlimitedDiskUsage implements DiskUsage {

    @Override
    public void touch(File file) throws IOException {
        // do nothing
    }
}
