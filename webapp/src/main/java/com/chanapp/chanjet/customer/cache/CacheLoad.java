package com.chanapp.chanjet.customer.cache;

public abstract class CacheLoad<K, V> {

    public abstract Cache<K, V> loadCache();

}
