package com.ding.networkframe;

/**
 * Created by jindingwei on 2019/7/20.
 */

public class Response<T> {

    public interface Listener<T> {
        void onResponse(T data);
    }

    public interface ErrorListener {
        void onError(NetRequetsError error);
    }

    public T result;

    public boolean isSuccess;

    public NetRequetsError netError;

    public Cache.Entry entry;

    public Response(T result, Cache.Entry entry) {
        this.result = result;
        this.isSuccess = true;
        this.entry = entry;
    }

    private Response(NetRequetsError error) {
        isSuccess = false;
        netError = error;
    }

    public static <T> Response<T> success(T result, Cache.Entry entry) {
        return new Response(result, entry);
    }

    public static Response error(NetRequetsError error) {
        return new Response(error);
    }
}
