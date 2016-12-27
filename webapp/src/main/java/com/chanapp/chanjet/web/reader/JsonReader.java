package com.chanapp.chanjet.web.reader;

import java.io.InputStream;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.common.base.json.JSONArray;
import com.chanjet.csp.common.base.json.JSONObject;

/**
 * resources/目录下的json文件读取工具
 * @author tds
 */
public final class JsonReader extends BaseReader<JSONObject> {

  private JsonReader(String configPath) {
    super(configPath);
  }

  public static JsonReader getInstance(String configPath) {
    return new JsonReader(configPath);
  }

  public JSONArray getJSONArray(String key, boolean reload) {
    JSONObject json = get(reload);
    if (json == null) {
      return null;
    }
    return json.optJSONArray(key);
  }

  public JSONArray getJSONArray(String key) {
    return getJSONArray(key, false);
  }

  @Override
  protected JSONObject load(InputStream is) throws Exception {
    JSONObject json = AppWorkManager.getDataManager().createJSONObject(is);
    return json;
  }
}
