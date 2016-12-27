package com.chanapp.chanjet.customer.cache;

import java.util.HashMap;

public class BaseCache<K, V> extends AbstractCacheMap<K, V> {

    public BaseCache() {
        super();
        this.cacheMap = new HashMap<K, CacheObject<K, V>>();
    }

    @Override
    public int eliminate() {
        return 0;
    }

}
