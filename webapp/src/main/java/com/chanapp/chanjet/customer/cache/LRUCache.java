package com.chanapp.chanjet.customer.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * 
 * <p>
 * LRU 最近最少使用淘汰 缓存
 * </p>
 * 
 * @author rui </br>
 * @Email chenruib@chanjet.com
 * @date 2014年7月7日 下午1:58:40
 *
 * @version V1.0
 *
 */
public class LRUCache<K, V> extends AbstractCacheMap<K, V> {

    public LRUCache(int cacheSize, long defaultExpire) {
        super(cacheSize, defaultExpire);
        this.cacheMap = new LinkedHashMap<K, CacheObject<K, V>>(cacheSize + 1, 1f, true) {

            private static final long serialVersionUID = 4491049635936405910L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CacheObject<K, V>> eldest) {
                return LRUCache.this.removeEldestEntry(eldest);
            }
        };
    }

    protected boolean removeEldestEntry(Map.Entry<K, CacheObject<K, V>> eldest) {
        if (cacheSize == 0)
            return false;
        return size() > cacheSize;
    }

    @Override
    public int eliminate() {
        if (!isNeedClearExpiredObject()) {
            return 0;
        }
        Iterator<CacheObject<K, V>> iterator = cacheMap.values().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            CacheObject<K, V> cacheObject = iterator.next();
            if (cacheObject.isExpired()) {
                iterator.remove();
                count++;
            }
        }
        return count;
    }

    public String toString() {
        String cacheContent = "[";
        Iterator<CacheObject<K, V>> iterator = cacheMap.values().iterator();
        while (iterator.hasNext()) {
            CacheObject<K, V> cacheObject = iterator.next();
            cacheContent += cacheObject.getKey() + ":";
            cacheContent += cacheObject.getObject() + ",";
        }
        cacheContent += "]";
        return cacheContent;
    }

}