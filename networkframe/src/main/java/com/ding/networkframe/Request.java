package com.ding.networkframe;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jindingwei on 2019/7/20.
 */

public abstract class Request<T> {

    private static final String DEFAULT_PARAMS_ENCODE_TYPE = "utf-8";

    public interface Method {
        int GET = 1;
        int POST = 2;
        int PUT = 3;
        int DELETE = 4;
        int HEAD = 5;
    }

    private int mMethod;

    private String mUrl;

    private String mRedirectUrl;

    private boolean mShouldCache = false;

    private int mTimeOut;

    private boolean isCancled;

    private int mRetryCount;

    protected Response.ErrorListener mErrorListener;

    private String mCacheKey;

    private Cache.Entry cacheEntry;

    private RequestQueue mRequestQueue;

    private Map<String, String> mHttpParams;

    public Request(int method, String url, Response.ErrorListener errorListener) {
        mMethod = method;
        if (mMethod == Method.GET) {
            mShouldCache = true;
        }
        mUrl = url;
        mCacheKey = mMethod + mUrl;
        mErrorListener = errorListener;

        mHttpParams = new HashMap<>();
    }

    protected abstract Response parserNetworkResponse(NetworkResponse response);

    protected abstract void deliveryResponse(T data);

    protected abstract void deliveryError(NetRequetsError error);

    protected String getParamsEncodeType() {
        return DEFAULT_PARAMS_ENCODE_TYPE;
    }

    public String getContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncodeType();
    }

    public void finished() {
        if (mRequestQueue != null) {
            mRequestQueue.requestFinished(this);
        }
    }

    public byte[] getBody() {
        StringBuilder builder = new StringBuilder();
        for (String key : mHttpParams.keySet()) {
            try {
                builder.append(URLEncoder.encode(key, getParamsEncodeType()));
                builder.append("=");
                builder.append(URLEncoder.encode(mHttpParams.get(key), getParamsEncodeType()));
                builder.append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        try {
            if (builder.length() > 0) {
                return builder.toString().getBytes(getParamsEncodeType());
            }
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }

    public void addHttpParam(String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        mHttpParams.put(key, value);
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public void setRequestQueue(RequestQueue requestQueue) {
        this.mRequestQueue = requestQueue;
    }

    public Cache.Entry getCacheEntry() {
        return cacheEntry;
    }

    public void setCacheEntry(Cache.Entry cacheEntry) {
        this.cacheEntry = cacheEntry;
    }

    public String getCacheKey() {
        return mCacheKey;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.mRedirectUrl = redirectUrl;
    }

    public int getRetryCount() {
        return mRetryCount;
    }

    public void setRetryCount(int retryCount) {
        this.mRetryCount = retryCount;
    }

    public boolean isCancled() {
        return isCancled;
    }

    public void setCancled(boolean cancled) {
        isCancled = cancled;
    }

    public int getTimeOut() {
        return mTimeOut;
    }

    public void setTimeOut(int timeOut) {
        this.mTimeOut = timeOut;
    }

    public int getMethod() {
        return mMethod;
    }

    public void setMethod(int method) {
        this.mMethod = method;
    }

    public String getUrl() {
        if (!TextUtils.isEmpty(mRedirectUrl)) {
            return mRedirectUrl;
        }
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public boolean isShouldCache() {
        return mShouldCache;
    }

    public void setShouldCache(boolean shouldCache) {
        this.mShouldCache = shouldCache;
    }
}
