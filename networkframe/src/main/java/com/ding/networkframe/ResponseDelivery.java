package com.ding.networkframe;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * Created by jindingwei on 2019/7/20.
 */

public class ResponseDelivery {
    private static final int CMD_DELIVERY_RESPONSE = 101;

    private HandlerThread mDeliveryThread;
    private Handler mDeliveryHandler;

    public ResponseDelivery() {
        mDeliveryThread = new HandlerThread("ResponseDelivery");
        mDeliveryThread.start();
        mDeliveryHandler = new Handler(mDeliveryThread.getLooper(), mDeliveryCallback);
    }

    public void postResponse(Request request, Response response) {
        Message msg = Message.obtain();
        msg.what = CMD_DELIVERY_RESPONSE;
        msg.obj = new DeliveryRunnable(request, response);
        mDeliveryHandler.sendMessage(msg);
    }

    public void postError(Request request, NetRequetsError error) {
        Response response = Response.error(error);
        Message msg = Message.obtain();
        msg.what = CMD_DELIVERY_RESPONSE;
        msg.obj = new DeliveryRunnable(request, response);
        mDeliveryHandler.sendMessage(msg);
    }

    private Handler.Callback mDeliveryCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == CMD_DELIVERY_RESPONSE) {
                if (msg.obj instanceof DeliveryRunnable) {
                    mMainHandler.post((Runnable) msg.obj);
                }
            }
            return true;
        }
    };

    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    private static class DeliveryRunnable implements Runnable {
        private Request request;
        private Response response;

        public DeliveryRunnable(Request request, Response response) {
            this.request = request;
            this.response = response;
        }

        @Override
        public void run() {
            if (request == null || response == null) {
                return;
            }
            request.finished();
            if (request.isCancled()) {
                return;
            }
            if (response.isSuccess) {
                request.deliveryResponse(response.result);
            } else {
                request.deliveryError(response.netError);
            }
        }
    }
}
