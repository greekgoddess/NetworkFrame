package com.ding.networkframe;

import android.text.TextUtils;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

import java.util.Map;

/**
 * Created by jindingwei on 2019/7/20.
 */

public class HttpHeaderParser {
    private static final String CONTENT_TYPE = "Content-Type";


    private static final String DEFAULT_HTTP_ENCODING = "ISO-8859-1";

    public static String parserHttpEncoding(Map<String, String> headers) {
        if (headers == null) {
            return DEFAULT_HTTP_ENCODING;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (CONTENT_TYPE.equals(entry.getKey())) {
                String type = entry.getValue();
                int index = type.indexOf(";");
                if (index > 0 && type.length() > index + 1) {
                    String encodeStr = type.substring(index + 1);
                    String[] params = encodeStr.split("=");
                    if (params != null && params.length == 2 && "charset".equals(params[0])) {
                        return params[1];
                    }
                }
            }
        }
        return DEFAULT_HTTP_ENCODING;
    }

    public static Cache.Entry parserCacheHeader(NetworkResponse response) {
        if (response == null) {
            return null;
        }
        Map<String, String> headers = response.headers;
        long now = System.currentTimeMillis();
        String etag = headers.get("ETag");
        long cacheTime = 0;
        String cacheControl = headers.get("Cache-Control");
        long max_age = -1;
        if (!TextUtils.isEmpty(cacheControl)) {
            String[] params = cacheControl.split(",");
            for (int i = 0;i < params.length;i++) {
                if (params[i].contains("no-cache") || params[i].contains("no-store")) {
                    return null;
                } else if (params[i].startsWith("max-age=")) {
                    max_age = Long.parseLong(params[i].substring(8));
                }
            }
        }
        String expires = headers.get("Expires");
        String serverDate = headers.get("Date");
        long expiresTime = 0;
        long serverTime = 0;
        try {
            if (!TextUtils.isEmpty(expires)) {
                expiresTime = DateUtils.parseDate(expires).getTime();
            }
            if (!TextUtils.isEmpty(serverDate)) {
                serverTime = DateUtils.parseDate(serverDate).getTime();
            }
        } catch (DateParseException e) {
            e.printStackTrace();
        }
        if (max_age >= 0) {
            cacheTime = now + max_age * 1000;
        } else {
            if (expiresTime > serverTime) {
                cacheTime = expiresTime - serverTime + now;
            }
        }
        String headerVaule = headers.get("Last-Modified");
        long lastModefied = 0;
        if (!TextUtils.isEmpty(headerVaule)) {
            try {
                lastModefied = DateUtils.parseDate(headerVaule).getTime();
            } catch (DateParseException e) {
                e.printStackTrace();
            }
        }

        Cache.Entry entry = new Cache.Entry();
        entry.lastModified = lastModefied;
        entry.ttl = cacheTime;
        entry.softTtl = cacheTime;
        entry.etag = etag;
        entry.responseHeaders = headers;
        entry.serverDate = serverTime;
        entry.data = response.datas;
        return entry;
    }
}
