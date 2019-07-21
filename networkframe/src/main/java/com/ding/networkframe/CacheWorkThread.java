package com.ding.networkframe;

import android.os.Process;
import android.util.Log;

import java.util.concurrent.BlockingQueue;

/**
 * Created by jindingwei on 2019/7/21.
 */

public class CacheWorkThread extends Thread {
    private BlockingQueue<Request> mCacheQueue;
    private BlockingQueue<Request> mNetQueue;
    private boolean mQuit;
    private Cache mCache;
    private ResponseDelivery mDelivery;

    public CacheWorkThread(Cache cache, BlockingQueue cacheQueue, BlockingQueue netQueue, ResponseDelivery delivery) {
        mCache = cache;
        mCacheQueue = cacheQueue;
        mNetQueue = netQueue;
        mDelivery = delivery;
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
                request = mCacheQueue.take();
            } catch (InterruptedException e) {
                if (mQuit) {
                    Log.e("ding", "CacheWorkThread---quit");
                    return;
                }
                continue;
            }
            if (request.isCancled()) {
                request.finished();
                continue;
            }
            Cache.Entry entry = mCache.get(request.getCacheKey());
            if (entry == null) {
                addToNetQueue(request, null);
                continue;
            }
            if (entry.isExpired()) {
                addToNetQueue(request, entry);
                continue;
            }
            Response response = request.parserNetworkResponse(new NetworkResponse(entry.data, entry.responseHeaders));
            mDelivery.postResponse(request, response);
        }
    }

    private void addToNetQueue(Request request, Cache.Entry entry) {
        if (entry != null) {
            request.setCacheEntry(entry);
        }
        try {
            mNetQueue.put(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
