package com.ygl.strong.utils.videocache;

import com.ygl.strong.utils.videocache.ProxyCache;
import com.ygl.strong.utils.videocache.ProxyCacheException;

/**
 * Indicates interruption error in work of {@link com.ygl.strong.utils.videocache.ProxyCache} fired by user.
 *
 * @author Alexey Danilov
 */
public class InterruptedProxyCacheException extends ProxyCacheException {

    public InterruptedProxyCacheException(String message) {
        super(message);
    }

    public InterruptedProxyCacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterruptedProxyCacheException(Throwable cause) {
        super(cause);
    }
}
