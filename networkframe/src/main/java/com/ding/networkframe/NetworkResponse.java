package com.ding.networkframe;

import java.util.Map;

/**
 * Created by jindingwei on 2019/7/20.
 */

public class NetworkResponse {

    public int statusCode;

    public byte[] datas;

    public Map<String, String> headers;

    public boolean notModified;

    public NetworkResponse(int statusCode, byte[] datas, Map<String, String> headers, boolean notModified) {
        this.statusCode = statusCode;
        this.datas = datas;
        this.headers = headers;
        this.notModified = notModified;
    }

    public NetworkResponse(byte[] datas, Map<String, String> headers) {
        this.datas = datas;
        this.headers = headers;
    }
}
