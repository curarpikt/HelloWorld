package com.chanapp.chanjet.customer.cache;

public interface Cache<K, V> {

    /**
     * 获取默认失效时间
     */
    long getDefaultExpire();

    /**
     * put value to cache with no expire
     */
    void put(K key, V value);

    /**
     * put value to cache with expireTime
     */
    void put(K key, V value, long expire);

    /**
     * 获取缓存值
     */
    V get(K key);

    /**
     * 如果缓存满了，按照淘汰算法清除数据
     */
    int eliminate();

    /**
     * 判断缓存是否已装满
     */
    boolean isFull();

    /**
     * 当前缓存数据的数量
     */
    long size();

    /**
     * 删除
     */
    void remove(K key);

    /**
     * 清空缓存
     */
    void clear();

    /**
     * 判断是否为空
     */
    boolean isEmpty();

}
