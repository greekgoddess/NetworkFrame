package com.ding.networkframe;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by jindingwei on 2019/8/4.
 */

public class ImageRequest extends Request<Bitmap> {
    private Response.Listener<Bitmap> mListener;

    public ImageRequest(int method, String url, Response.Listener<Bitmap> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    @Override
    protected Response parserNetworkResponse(NetworkResponse response) {
        if (response == null || response.datas == null || response.datas.length <= 0) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(response.datas, 0, response.datas.length);
        Response result = null;
        if (bitmap != null) {
            result = Response.success(bitmap, HttpHeaderParser.parserCacheHeader(response));
        } else {
            result = Response.error(new NetRequetsError("图片解码失败"));
        }
        return result;
    }

    @Override
    protected void deliveryResponse(Bitmap data) {
        if (mListener != null) {
            mListener.onResponse(data);
        }
    }

    @Override
    protected void deliveryError(NetRequetsError error) {
        if (mErrorListener != null) {
            mErrorListener.onError(error);
        }
    }
}
