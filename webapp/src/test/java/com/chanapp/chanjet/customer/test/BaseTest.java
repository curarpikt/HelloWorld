package com.chanapp.chanjet.customer.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.Rule;

import com.alibaba.fastjson.JSON;
import com.chanjet.csp.platform.test.AppUser;
import com.chanjet.csp.platform.test.RestfulIT;
import com.chanjet.csp.platform.test.utils.ConfigInfo;

public class BaseTest extends RestfulIT {

    private static final int VERSION_NUM = 1;
    public static final String[] VERSIONS = { "v4" };
    public static final String DEFAULT_VERSION = "v4";
    private static final String[] VERSION_SUFFIX = { "", "_3.3" };
    private static int CURRENT_VERSION_INDEX = 0;
    private static ConfigInfo[] configs = new ConfigInfo[VERSION_NUM];
    private static Object[] appUserMaps = { null, null };

    static {
        for (int i = 0; i < VERSION_NUM; i++) {
            InputStream is = null;
            try {
                is = ClassLoader.getSystemResourceAsStream("config" + VERSION_SUFFIX[i] + ".json");
                StringBuffer sb = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    line = br.readLine();
                }
                System.out.println("INFO: " + "config" + VERSION_SUFFIX[i] + ".json: " + sb.toString());
                ConfigInfo config = (ConfigInfo) JSON.parseObject(sb.toString(), ConfigInfo.class);
                configs[i] = config;
            } catch (Exception e) {
                System.out.println("ERROR: load config.json failed:" + e.getMessage());
                e.printStackTrace();
                System.exit(-1);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    @AfterClass
    public static void tearDownAfterClass_BaseTest() throws Exception {
        switchToV4();
    }

    public static String getCurrentVersion() {
        return VERSIONS[CURRENT_VERSION_INDEX];
    }

    public static boolean switchToDefault() throws Exception {
        return switchTo(DEFAULT_VERSION);
    }

    public static boolean switchToV4() throws Exception {
        return switchTo("v4");
    }

    public static boolean switchToV3() throws Exception {
        return switchTo("v3");
    }

    public static boolean isV3() {
        return getCurrentVersion().equals("v3");
    }

    public static boolean switchTo(String version) throws Exception {
        for (int i = 0; i < VERSION_NUM; i++) {
            if (VERSIONS[i].equals(version)) {
                return switchTo(i);
            }
        }
        return false;
    }

    private static boolean switchTo(int index) throws Exception {
        if (index == CURRENT_VERSION_INDEX) {
            return true;
        }

        Field field = RestfulIT.class.getDeclaredField("appUserMap");
        field.setAccessible(true);
        appUserMaps[CURRENT_VERSION_INDEX] = field.get(null);
        if (appUserMaps[index] == null) {
            field.set(null, new HashMap<String, AppUser>());
        } else {
            field.set(null, appUserMaps[index]);
        }

        // switch confg
        field = RestfulIT.class.getDeclaredField("config");
        field.setAccessible(true);
        field.set(null, configs[index]);

        CURRENT_VERSION_INDEX = index;

        return true;
    }

    /**
     * 清除全部APP的全部已登陆用户信息
     * 
     * @throws Exception
     */
    protected void clearAllSession() throws Exception {
        Field field = RestfulIT.class.getDeclaredField("appUserMap");
        field.setAccessible(true);
        field.set(null, new HashMap<String, AppUser>());

        appUserMaps = new Object[] { null, null, null };
    }

    @Rule
    public VersionRule TESTV3 = new VersionRule();

    protected String getRealUrl(Url url) {
        return isV3() ? url.origUrl : url.newUrl;
    }
}
