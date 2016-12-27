package com.chanapp.chanjet.customer.service.sync;

import com.chanapp.chanjet.customer.cache.Cache;
import com.chanapp.chanjet.customer.cache.LRUCache;

public class SyncCache {
    private static Cache<String, SyncUserInfo> syncCache = new LRUCache<String, SyncUserInfo>(5000, 300000);

    public static Cache<String, SyncUserInfo> getSyncCache() {
        return syncCache;
    }

}
