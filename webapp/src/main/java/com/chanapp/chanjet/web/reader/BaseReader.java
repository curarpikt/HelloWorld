package com.chanapp.chanjet.web.reader;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * resources/目录下的文件读取工具
 * 
 * @author tds
 *
 */
public abstract class BaseReader<T> {
    private static Logger logger = LoggerFactory.getLogger(BaseReader.class);

    private static ConcurrentHashMap<String, Object> cacheMap = new ConcurrentHashMap<String, Object>();

    String configPath = "";

    protected BaseReader(String configPath) {
        this.configPath = configPath;
    }

    public T get() {
        return get(false);
    }

    public T get(boolean reload) {
        return loadFile(reload);
    }

    protected abstract T load(InputStream is) throws Exception;

    @SuppressWarnings("unchecked")
    protected T loadFile(boolean reload) {
        synchronized (configPath) {
            T val = null;
            if (reload) {
                cacheMap.remove(configPath);
            } else {
                val = (T) cacheMap.get(configPath);
            }
            if (val == null) {
                InputStream is = BaseReader.class.getClassLoader().getResourceAsStream(configPath);
                if (is == null) {
                    is = Thread.currentThread().getContextClassLoader().getResourceAsStream(configPath);
                }
                try {
                    val = load(is);
                    cacheMap.put(configPath, val);
                } catch (Exception e) {
                    logger.error("configPath:{} read error:{}", configPath, e.getMessage());
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return val;
        }
    }
}
