package com.ding.networkframe;

import android.text.TextUtils;

import org.apache.http.impl.cookie.DateUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jindingwei on 2019/7/20.
 */

public class NetworkImpl implements Network {
    private static final int MAX_RETRY_NUM = 3;

    public NetworkImpl() {

    }

    @Override
    public NetworkResponse performRequest(Request request) throws NetRequetsError, InterruptedIOException {
        while (true) {
            String url = request.getUrl();
            int responseCode = 0;
            try {
                URL parserUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) parserUrl.openConnection();
                connection.setConnectTimeout(request.getTimeOut());
                connection.setReadTimeout(request.getTimeOut());
                connection.setUseCaches(false);
                connection.setDoInput(true);

                setRequestMethod(request, connection);
                addCacheHeader(request, connection);

                responseCode = connection.getResponseCode();
                Map<String, String> headerMap = parserResponseHeader(connection);

                if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                    if (request.getCacheEntry() == null) {
                        return new NetworkResponse(responseCode, null, headerMap, true);
                    }
                    request.getCacheEntry().responseHeaders.putAll(headerMap);
                    return new NetworkResponse(responseCode, request.getCacheEntry().data,
                            request.getCacheEntry().responseHeaders, true);
                }

                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM
                        || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    String directUrl = headerMap.get("Location");
                    request.setRedirectUrl(directUrl);
                    throw new Exception("url direct");
                }

                if (responseCode < 200 || responseCode > 299) {
                    throw new Exception();
                }

                byte[] datas = null;
                if (hasResponseBoby(request.getMethod(), responseCode)) {
                    HttpEntity entity = parserHttpBody(connection);
                    datas = httpEntityToBytes(entity);
                }

                return new NetworkResponse(responseCode, datas, headerMap, false);
            } catch (MalformedURLException e) {
                throw new NetRequetsError("MalformedURLException");
            } catch (InterruptedIOException e) {
                throw e;
            } catch (IOException e) {
                if (request.getRetryCount() <= MAX_RETRY_NUM) {
                    request.setRetryCount(request.getRetryCount() + 1);
                } else {
                    throw new NetRequetsError("net request error");
                }
            } catch (Exception e) {
                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM
                        || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    if (request.getRetryCount() <= MAX_RETRY_NUM) {
                        request.setRetryCount(request.getRetryCount() + 1);
                    } else {
                        throw new NetRequetsError("net request error");
                    }
                } else {
                    throw new NetRequetsError("未知错误");
                }
            }
        }
    }

    private void addCacheHeader(Request request, HttpURLConnection connection) {
        Cache.Entry entry = request.getCacheEntry();
        if (entry != null) {
            if (!TextUtils.isEmpty(entry.etag)) {
                connection.setRequestProperty("If-None-Match", entry.etag);
            }
            if (entry.lastModified > 0) {
                Date date = new Date(entry.lastModified);
                connection.setRequestProperty("If-Modified-Since", DateUtils.formatDate(date));
            }
        }
    }

    private HttpEntity parserHttpBody(HttpURLConnection connection) throws IOException {
        HttpEntity entity = new HttpEntity();
        entity.ins = connection.getInputStream();
        entity.contentLength = connection.getContentLength();
        entity.contentType = connection.getContentType();
        entity.contentEncode = connection.getContentEncoding();
        return entity;
    }

    private byte[] httpEntityToBytes(HttpEntity entity) throws IOException {
        if (entity == null) {
            return null;
        }
        int size = 0;
        if (entity.contentLength > 0) {
            size = (int) entity.contentLength;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
        byte[] buffer = new byte[1024];
        int len = 0;
        try {
            while ((len = entity.ins.read(buffer)) >= 0) {
                bos.write(buffer, 0, len);
            }
        } finally {
            entity.ins.close();
            bos.close();
        }
        return bos.toByteArray();
    }

    private Map<String, String> parserResponseHeader(HttpURLConnection connection) {
        Map<String, String> headerMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
            if (entry.getKey() != null && entry.getValue().get(0) != null) {
                headerMap.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        return headerMap;
    }

    private boolean hasResponseBoby(int requestMethod, int responseCode) {
        return requestMethod != Request.Method.HEAD
                && responseCode != HttpURLConnection.HTTP_NO_CONTENT
                && responseCode != HttpURLConnection.HTTP_NOT_MODIFIED;
    }

    private void setRequestMethod(Request request, HttpURLConnection connection) throws IOException {
        switch (request.getMethod()) {
            case Request.Method.GET:
                connection.setRequestMethod("GET");
                break;
            case Request.Method.POST:
                connection.setRequestMethod("POST");
                addBody(request, connection);
                break;
            case Request.Method.PUT:
                connection.setRequestMethod("PUT");
                addBody(request, connection);
                break;
            case Request.Method.DELETE:
                connection.setRequestMethod("DELETE");
                break;
            case Request.Method.HEAD:
                connection.setRequestMethod("HEAD");
                break;
        }
    }

    private void addBody(Request request, HttpURLConnection connection) throws IOException {
        byte[] body = request.getBody();
        if (body != null) {
            connection.setRequestProperty("Content-Type", request.getContentType());
            connection.setDoOutput(true);
            OutputStream ops = null;
            try {
                ops = connection.getOutputStream();
                ops.write(body);
            } finally {
                if (ops != null) {
                    ops.close();
                }
            }
        }
    }
}
