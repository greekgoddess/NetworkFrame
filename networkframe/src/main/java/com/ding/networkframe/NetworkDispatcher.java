package com.ding.networkframe;

import android.os.Process;

import java.io.InterruptedIOException;
import java.util.concurrent.BlockingQueue;

/**
 * Created by jindingwei on 2019/7/20.
 */

public class NetworkDispatcher extends Thread {
    private BlockingQueue<Request> mNetworkQueue;
    private boolean mQuit = false;
    private Network mNetwork;
    private ResponseDelivery mDelivery;
    private Cache mCache;

    public NetworkDispatcher(BlockingQueue blockingQueue, Network network,
                             ResponseDelivery delivery, Cache cache) {
        mNetworkQueue = blockingQueue;
        mNetwork = network;
        mDelivery = delivery;
        mCache = cache;
    }

    public void quit() {
        mQuit = true;
        interrupt();
    }

    @Override
    public void run() {
        super.run();
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            Request request = null;
            try {
                request = mNetworkQueue.take();
            } catch (InterruptedException e) {
                if (mQuit) {
                    return;
                }
                continue;
            }

            if (request.isCancled()) {
                request.finished();
                continue;
            }

            NetworkResponse networkResponse = null;
            try {
                networkResponse = mNetwork.performRequest(request);
                Response response = request.parserNetworkResponse(networkResponse);

                if (request.isShouldCache() && response.entry != null) {
                    mCache.put(request.getCacheKey(), response.entry);
                }
                mDelivery.postResponse(request, response);
            } catch (NetRequetsError netRequetsError) {
                mDelivery.postError(request, netRequetsError);
            } catch (InterruptedIOException e) {
                if (mQuit) {
                    mDelivery.postError(request, new NetRequetsError("请求失败"));
                    return;
                }
            }
        }
    }
}
