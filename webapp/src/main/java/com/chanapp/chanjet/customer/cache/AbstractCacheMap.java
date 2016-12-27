package com.chanapp.chanjet.customer.cache;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractCacheMap<K, V> implements Cache<K, V> {

    protected Map<K, CacheObject<K, V>> cacheMap;

    private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
    private final Lock readLock = cacheLock.readLock();
    private final Lock writeLock = cacheLock.writeLock();

    protected long cacheSize;
    protected boolean existCustomExpire;
    protected long defaultExpire;

    public AbstractCacheMap() {
    }

    public AbstractCacheMap(int cacheSize, long defaultExpire) {
        this.cacheSize = cacheSize;
        this.defaultExpire = defaultExpire;
    }

    @Override
    public long getDefaultExpire() {
        return defaultExpire;
    }

    public void put(K key, V value) {
        put(key, value, defaultExpire);
    }

    public void put(K key, V value, long expire) {
        writeLock.lock();
        try {
            CacheObject<K, V> co = new CacheObject<K, V>(key, value, expire);
            if (expire != 0) {
                existCustomExpire = true;
            }
            cacheMap.put(key, co);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V get(K key) {
        readLock.lock();
        try {
            CacheObject<K, V> co = cacheMap.get(key);
            if (co == null) {
                return null;
            }
            if (co.isExpired() == true) {
                cacheMap.remove(key);
                return null;
            }
            return co.getObject();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void remove(K key) {
        writeLock.lock();
        try {
            cacheMap.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            this.cacheMap.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        return this.cacheMap.isEmpty();
    }

    @Override
    public long size() {
        return this.cacheMap.size();
    }

    @Override
    public abstract int eliminate();

    @Override
    public boolean isFull() {
        if (cacheSize == 0) {// 0 -> 无限制
            return false;
        }
        return cacheMap.size() >= cacheSize;
    }

    protected boolean isNeedClearExpiredObject() {
        return defaultExpire > 0 || existCustomExpire;
    }

}
