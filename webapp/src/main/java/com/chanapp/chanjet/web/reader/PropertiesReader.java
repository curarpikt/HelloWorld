package com.chanapp.chanjet.web.reader;

import java.io.InputStream;
import java.util.Properties;

import com.chanapp.chanjet.web.util.ConvertUtil;

/**
 * resources/目录下的properties文件读取工具
 * 
 * @author tds
 *
 */
public final class PropertiesReader extends BaseReader<Properties> {

    private PropertiesReader(String configPath) {
        super(configPath);
    }

    public static PropertiesReader getInstance(String configPath) {
        return new PropertiesReader(configPath);
    }

    public String getString(String key, String defaultValue, boolean reload) {
        Properties prop = get(reload);
        return prop.getProperty(key);
    }

    public String getString(String key, String defaultValue) {
        return getString(key, defaultValue, false);
    }

    public String getString(String key, boolean reload) {
        return getString(key, null, reload);
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public Boolean getBoolean(String key, Boolean defaultValue, boolean reload) {
        String value = getString(key, null, reload);
        return ConvertUtil.toBoolean(value, defaultValue);
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        return getBoolean(key, defaultValue, false);
    }

    public Boolean getBoolean(String key, boolean reload) {
        return getBoolean(key, null, reload);
    }

    public Boolean getBoolean(String key) {
        return getBoolean(key, null);
    }

    @Override
    protected Properties load(InputStream is) throws Exception {
        Properties prop = new Properties();
        prop.load(is);
        return prop;
    }
}
