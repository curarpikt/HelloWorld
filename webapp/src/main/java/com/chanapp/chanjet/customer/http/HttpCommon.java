package com.chanapp.chanjet.customer.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpCommon {
    private static final int HTTP_TIMEOUT = 30 * 1000;
    private static final int SO_TIMEOUT = 5 * 1000;
    private static final boolean useCache = false;
    private static final int BUFFER_SIZE = 4096;

    public static HttpResponse doHttp(String urlString, HttpMethodEnum httpMethod, Map<String, String> header,
            Map<String, String> getParams, byte[] body) throws Exception {
        return doHttp(urlString, httpMethod, header, getParams, body, false);
    }

    public static HttpResponse doHttp(String urlString, HttpMethodEnum httpMethod, Map<String, String> header,
            Map<String, String> getParams, byte[] body, int timeout) throws Exception {
        return doHttp(urlString, httpMethod, header, getParams, body, timeout, false);
    }

    public static HttpResponse doHttp(String urlString, HttpMethodEnum httpMethod, Map<String, String> header,
            Map<String, String> getParams, byte[] body, boolean redirect) throws Exception {
        return doHttp(urlString, httpMethod, header, getParams, body, HTTP_TIMEOUT, redirect);
    }

    public static HttpResponse doHttp(String urlString, HttpMethodEnum httpMethod, Map<String, String> header,
            Map<String, String> getParams, byte[] body, int timeout, boolean redirect) throws Exception {
        return doHttp(urlString, httpMethod, header, getParams, body, timeout, SO_TIMEOUT, redirect);
    }

    public static HttpResponse doHttp(String urlString, HttpMethodEnum httpMethod, Map<String, String> header,
            Map<String, String> getParams, byte[] body, int timeout, int stimeout, boolean redirect) throws Exception {
        httpCheck(urlString, httpMethod);

        //System.out.println("==== URL: " + urlString + " Method: " + httpMethod);        
        URL url;
        if ( getParams != null && !getParams.isEmpty() ) {
        	String getParamsString = generatorHttpParams(getParams);
            url = new URL(urlString + "?" + getParamsString);
        } else {
            url = new URL(urlString);
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (header != null) {
            Iterator<Entry<String, String>> iterator = header.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, String> entry = (Entry<String, String>) iterator.next();
                String key = entry.getKey();
                String value = entry.getValue();
                conn.setRequestProperty(key, value);
            }
        }
        conn.setRequestMethod(httpMethod.toString());
        conn.setInstanceFollowRedirects(redirect);
        conn.setDoOutput(true);
        conn.setConnectTimeout(timeout < 0 ? HTTP_TIMEOUT : timeout);
        conn.setReadTimeout(stimeout < 0 ? SO_TIMEOUT : stimeout);
        conn.setUseCaches(useCache);
        if (body != null) {
            OutputStream outStream = conn.getOutputStream();
            outStream.write(body);
            outStream.flush();
            outStream.close();
        } else {
            conn.connect();
        }

        HttpResponse httpResponse = new HttpResponse();

        int resCode = conn.getResponseCode();
        httpResponse.setStatusCode(resCode);

        Map<String, List<String>> headerFields = conn.getHeaderFields();
        httpResponse.setHeaderFields(headerFields);

        InputStream resInputStream;
        if (resCode == 200) {
            resInputStream = conn.getInputStream();
        } else {
            resInputStream = conn.getErrorStream();
        }
        // httpResponse.setInputStream(resInputStream);

        byte[] resByte = null;
        String resString = null;
        if (resInputStream != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] data = new byte[BUFFER_SIZE];
            int count = -1;
            while ((count = resInputStream.read(data, 0, BUFFER_SIZE)) != -1) {
                baos.write(data, 0, count);
            }
            data = null;
            resByte = baos.toByteArray();
            resString = new String(resByte);
        }
        httpResponse.setBytes(resByte);
        httpResponse.setString(resString);

        conn.disconnect();

        return httpResponse;
    }

    public static void httpCheck(String urlString, HttpMethodEnum httpMethod) throws Exception {
        if (urlString == null || urlString.equals("")) {
            throw new Exception("URL is null");
        }

        if (httpMethod == null) {
            throw new Exception("Invalid Http method");
        }
    }

    public static String generatorHttpParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return null;            
        }
        StringBuffer paramsBuf = new StringBuffer();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, String> entry = (Entry<String, String>) iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            paramsBuf.append(key + "=" + urlEncode(value));
            if (iterator.hasNext()) {
                paramsBuf.append("&");
            }
        }
        return paramsBuf.toString();
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

}
