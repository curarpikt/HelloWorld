package com.chanapp.chanjet.customer.cache;

public class CacheObject<K, V> {

    private K key;

    private V value;

    private long lastAccess;

    private long accessCount;

    private long expire;

    CacheObject(K key, V value, long expire) {
        this.key = key;
        this.value = value;
        this.expire = expire;
        this.lastAccess = System.currentTimeMillis();
    }

    K getKey() {
        return this.key;
    }

    V getObject() {
        lastAccess = System.currentTimeMillis();
        accessCount++;
        return this.value;
    }

    boolean isExpired() {
        if (expire == 0) {
            return false;
        }
        return lastAccess + expire < System.currentTimeMillis();
    }

}
