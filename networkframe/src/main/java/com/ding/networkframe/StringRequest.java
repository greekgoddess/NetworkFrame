package com.ding.networkframe;

import java.io.UnsupportedEncodingException;

/**
 * Created by jindingwei on 2019/7/20.
 */

public class StringRequest extends Request<String> {
    private Response.Listener<String> mListener;

    public StringRequest(int method, String url, Response.Listener<String> listener,
                         Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
    }

    @Override
    protected void deliveryResponse(String result) {
        if (mListener != null) {
            mListener.onResponse(result);
        }
    }

    @Override
    protected void deliveryError(NetRequetsError error) {
        if (mErrorListener != null) {
            mErrorListener.onError(error);
        }
    }

    @Override
    protected Response<String> parserNetworkResponse(NetworkResponse response) {
        String result = "";
        try {
            result = new String(response.datas, HttpHeaderParser.parserHttpEncoding(response.headers));
        } catch (UnsupportedEncodingException e) {
            result = new String(response.datas);
        }
        Response<String> rps = new Response<>(result, HttpHeaderParser.parserCacheHeader(response));
        return rps;
    }
}
