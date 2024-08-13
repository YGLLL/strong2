package com.ygl.strong.utils.videocache;

import com.ygl.strong.utils.videocache.ProxyCache;

/**
 * Indicates any error in work of {@link com.ygl.strong.utils.videocache.ProxyCache}.
 *
 * @author Alexey Danilov
 */
public class ProxyCacheException extends Exception {

    public ProxyCacheException(String message) {
        super(message);
    }

    public ProxyCacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProxyCacheException(Throwable cause) {
        super("No explanation error", cause);
    }
}
