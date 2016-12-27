package com.chanapp.chanjet.customer.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.alibaba.fastjson.JSON;
import com.chanjet.csp.platform.test.HttpMethodEnum;
import com.chanjet.csp.platform.test.HttpResponse;

/**
 * restlet 接口 test基类
 * 
 * @author tds
 *
 */
public class RestletBaseTest extends BaseTest {
    final String ENTERPRISE_MANAGER = "enterpriseManager";
    final String APP_MANAGER = "appManager";
    final String REGULAR_USER = "regularUser";
    final String REGULAR_USER_2 = "regularUser2";
    private static final String URL_PREFIX = "";
    public static String UPLOAD_FILE_FORM_NAME = "file1";

    @BeforeClass
    public static void setUpBeforeClass_RestletBaseTest() throws Exception {
        setIsCspServletURL(false);
    }

    @AfterClass
    public static void tearDownAfterClass_RestletBaseTest() throws Exception {
        setIsCspServletURL(true);
    }

    private HttpResponse postWithUser(String userTag, Url url, Object data) throws Exception {
    	System.out.println("url:"+getPath(url));
        HttpResponse response = this.doPost(userTag, getPath(url), data);
        return response;
    }

    private HttpResponse putWithUser(String userTag, Url url, Object data) throws Exception {
        HttpResponse response = this.doPut(userTag, getPath(url), data);
        return response;
    }

    private HttpResponse getWithUser(String userTag, Url url) throws Exception {
        HttpResponse response = this.doGet(userTag, getPath(url), null);
        return response;
    }

    private HttpResponse deleteWithUser(String userTag, Url url) throws Exception {
        HttpResponse response = this.doDelete(userTag, getPath(url), null);
        return response;
    }

    private HttpResponse uploadWithUser(String userTag, Url url, String filePath) throws Exception {
        String filename = getClass().getClassLoader().getResource(filePath).getFile();
        Map<String, String> header = new HashMap<String, String>();
        byte[] data = getUploadFileContent(filename, header);
        HttpResponse response = this.doAppHttpByWebSession(userTag, getPath(url), HttpMethodEnum.POST, header, null,
                data, false);
        return response;
    }

    private <T> T postWithUser(String userTag, Url url, Object data, Class<T> clazz) throws Exception {
        HttpResponse response = postWithUser(userTag, url, data);
        assertEquals(Status.OK.getStatusCode(), response.getStatusCode());
        return JSON.parseObject(response.getString(), clazz);
    }

    private <T> T putWithUser(String userTag, Url url, Object data, Class<T> clazz) throws Exception {
        HttpResponse response = putWithUser(userTag, url, data);
        assertEquals(Status.OK.getStatusCode(), response.getStatusCode());
        return JSON.parseObject(response.getString(), clazz);
    }

    private <T> T getWithUser(String userTag, Url url, Class<T> clazz) throws Exception {
        HttpResponse response = getWithUser(userTag, url);
        assertEquals(Status.OK.getStatusCode(), response.getStatusCode());
        return JSON.parseObject(response.getString(), clazz);
    }

    private <T> T deleteWithUser(String userTag, Url url, Class<T> clazz) throws Exception {
        HttpResponse response = deleteWithUser(userTag, url);
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        return JSON.parseObject(response.getString(), clazz);
    }

    private <T> T uploadWithUser(String userTag, Url url, String filePath, Class<T> clazz) throws Exception {
        HttpResponse response = uploadWithUser(userTag, url, filePath);
        assertEquals(Status.OK.getStatusCode(), response.getStatusCode());
        return JSON.parseObject(response.getString(), clazz);
    }

    protected HttpResponse post(Url url, Object data) throws Exception {
        return postWithUser(APP_MANAGER, url, data);
    }

    protected HttpResponse put(Url url, Object data) throws Exception {
        return putWithUser(APP_MANAGER, url, data);
    }

    protected HttpResponse get(Url url) throws Exception {
        return getWithUser(APP_MANAGER, url);
    }

    protected HttpResponse delete(Url url) throws Exception {
        return deleteWithUser(APP_MANAGER, url);
    }

    protected HttpResponse upload(Url url, String filePath) throws Exception {
        return uploadWithUser(APP_MANAGER, url, filePath);
    }

    protected <T> T post(Url url, Object data, Class<T> clazz) throws Exception {
        return postWithUser(APP_MANAGER, url, data, clazz);
    }

    protected <T> T put(Url url, Object data, Class<T> clazz) throws Exception {
        return putWithUser(APP_MANAGER, url, data, clazz);
    }

    protected <T> T get(Url url, Class<T> clazz) throws Exception {
        return getWithUser(APP_MANAGER, url, clazz);
    }

    protected <T> T delete(Url url, Class<T> clazz) throws Exception {
        return deleteWithUser(APP_MANAGER, url, clazz);
    }

    protected <T> T upload(Url url, String filePath, Class<T> clazz) throws Exception {
        return uploadWithUser(APP_MANAGER, url, filePath, clazz);
    }

    protected HttpResponse postWithRegular(Url url, Object data) throws Exception {
        return postWithUser(REGULAR_USER, url, data);
    }

    protected HttpResponse putWithRegular(Url url, Object data) throws Exception {
        return putWithUser(REGULAR_USER, url, data);
    }

    protected HttpResponse getWithRegular(Url url) throws Exception {
        return getWithUser(REGULAR_USER, url);
    }

    protected HttpResponse deleteWithRegular(Url url) throws Exception {
        return deleteWithUser(REGULAR_USER, url);
    }

    protected HttpResponse uploadWithRegular(Url url, String filePath) throws Exception {
        return uploadWithUser(REGULAR_USER, url, filePath);
    }

    protected <T> T postWithRegular(Url url, Object data, Class<T> clazz) throws Exception {
        return postWithUser(REGULAR_USER, url, data, clazz);
    }

    protected <T> T putWithRegular(Url url, Object data, Class<T> clazz) throws Exception {
        return putWithUser(REGULAR_USER, url, data, clazz);
    }

    protected <T> T getWithRegular(Url url, Class<T> clazz) throws Exception {
        return getWithUser(REGULAR_USER, url, clazz);
    }

    protected <T> T deleteWithRegular(Url url, Class<T> clazz) throws Exception {
        return deleteWithUser(REGULAR_USER, url, clazz);
    }

    protected <T> T uploadWithRegular(Url url, String filePath, Class<T> clazz) throws Exception {
        return uploadWithUser(REGULAR_USER, url, filePath, clazz);
    }

    protected HttpResponse postWithRegular2(Url url, Object data) throws Exception {
        return postWithUser(REGULAR_USER_2, url, data);
    }

    protected HttpResponse putWithRegular2(Url url, Object data) throws Exception {
        return putWithUser(REGULAR_USER_2, url, data);
    }

    protected HttpResponse getWithRegular2(Url url) throws Exception {
        return getWithUser(REGULAR_USER_2, url);
    }

    protected HttpResponse deleteWithRegular2(Url url) throws Exception {
        return deleteWithUser(REGULAR_USER_2, url);
    }

    protected HttpResponse uploadWithRegular2(Url url, String filePath) throws Exception {
        return uploadWithUser(REGULAR_USER_2, url, filePath);
    }

    protected <T> T postWithRegular2(Url url, Object data, Class<T> clazz) throws Exception {
        return postWithUser(REGULAR_USER_2, url, data, clazz);
    }

    protected <T> T putWithRegular2(Url url, Object data, Class<T> clazz) throws Exception {
        return putWithUser(REGULAR_USER_2, url, data, clazz);
    }

    protected <T> T getWithRegular2(Url url, Class<T> clazz) throws Exception {
        return getWithUser(REGULAR_USER_2, url, clazz);
    }

    protected <T> T deleteWithRegular2(Url url, Class<T> clazz) throws Exception {
        return deleteWithUser(REGULAR_USER_2, url, clazz);
    }

    protected <T> T uploadWithRegular2(Url url, String filePath, Class<T> clazz) throws Exception {
        return uploadWithUser(REGULAR_USER_2, url, filePath, clazz);
    }

    protected HttpResponse postWithEntManager(Url url, Object data) throws Exception {
        return postWithUser(ENTERPRISE_MANAGER, url, data);
    }

    protected HttpResponse putWithEntManager(Url url, Object data) throws Exception {
        return putWithUser(ENTERPRISE_MANAGER, url, data);
    }

    protected HttpResponse getWithEntManager(Url url) throws Exception {
        return getWithUser(ENTERPRISE_MANAGER, url);
    }

    protected HttpResponse deleteWithEntManager(Url url) throws Exception {
        return deleteWithUser(ENTERPRISE_MANAGER, url);
    }

    protected HttpResponse uploadWithEntManager(Url url, String filePath) throws Exception {
        return uploadWithUser(ENTERPRISE_MANAGER, url, filePath);
    }

    protected <T> T postWithEntManager(Url url, Object data, Class<T> clazz) throws Exception {
        return postWithUser(ENTERPRISE_MANAGER, url, data, clazz);
    }

    protected <T> T putWithEntManager(Url url, Object data, Class<T> clazz) throws Exception {
        return putWithUser(ENTERPRISE_MANAGER, url, data, clazz);
    }

    protected <T> T getWithEntManager(Url url, Class<T> clazz) throws Exception {
        return getWithUser(ENTERPRISE_MANAGER, url, clazz);
    }

    protected <T> T deleteWithEntManager(Url url, Class<T> clazz) throws Exception {
        return deleteWithUser(ENTERPRISE_MANAGER, url, clazz);
    }

    protected <T> T uploadWithEntManager(Url url, String filePath, Class<T> clazz) throws Exception {
        return uploadWithUser(ENTERPRISE_MANAGER, url, filePath, clazz);
    }

    private String getPath(Url url) {
        assertNotNull(url);

        String path = this.getRealUrl(url);

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path.startsWith("/services/1.0/") ? path : URL_PREFIX + path;
    }

    /**
     * 对get参数进行urlencoder
     * 
     * @param params
     * @return
     */
    public static String urlEncode(String params) {
        try {
            return URLEncoder.encode(params, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return params;
    }

    /**
     * 获取文件字节码
     * 
     * @param filename
     * @param header
     * @return
     * @throws IOException
     */
    protected byte[] getUploadFileContent(String filename, Map<String, String> header) throws IOException {
        String boundary = "------------upload_file_" + System.currentTimeMillis();
        header.put("Content-Type", "multipart/form-data; boundary=" + boundary);

        StringBuffer strBuf = new StringBuffer();
        File file = new File(filename);
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        strBuf.append("\r\n").append("--").append(boundary).append("\r\n");
        strBuf.append("Content-Disposition: form-data; name=\"" + UPLOAD_FILE_FORM_NAME + "\"; filename=\""
                + file.getName() + "\"\r\n");
        strBuf.append("Content-Type:" + contentType + "\r\n\r\n");

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bout.write(strBuf.toString().getBytes());

        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(file));
            int bytes = 0;
            byte[] bufferOut = new byte[1024];
            while ((bytes = in.read(bufferOut)) != -1) {
                bout.write(bufferOut, 0, bytes);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }

        bout.write(("\r\n--" + boundary + "--\r\n").getBytes());
        return bout.toByteArray();
    }

    protected void changeUploadFileFormNameTo(String formName) {
        UPLOAD_FILE_FORM_NAME = formName;
    }

    protected Map<String, Object> parseObject(String str) {
        return JSON.parseObject(str);
    }

    protected <T> T parseObject(String str, Class<T> clazz) {
        return JSON.parseObject(str, clazz);
    }

    protected String toJSONString(Object obj) {
        return JSON.toJSONString(obj);
    }

    protected String getAppManagerId() {
        return this.getAppUser(APP_MANAGER).getUserId();
    }

    protected String getEnterseManagerId() {
        return this.getAppUser(ENTERPRISE_MANAGER).getUserId();
    }

    protected String getRegularUserId() {
        return this.getAppUser(REGULAR_USER).getUserId();
    }

    protected String getRegularUser2Id() {
        return this.getAppUser(REGULAR_USER_2).getUserId();
    }
}
