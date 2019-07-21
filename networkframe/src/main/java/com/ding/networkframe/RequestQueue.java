package com.ding.networkframe;


import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by jindingwei on 2019/7/20.
 */

public class RequestQueue {
    private static final int NET_WORK_THREAD_NUM = 4;

    private LinkedBlockingQueue<Request> mNetworkQueue;
    private LinkedBlockingQueue<Request> mCacheQueue;
    private Map<String, LinkedList<Request>> mWaitQueue;
    private NetworkDispatcher[] mNetDispatcher;
    private Network mNetwork;
    private ResponseDelivery mResponseDelivery;
    private Cache mCache;
    private CacheWorkThread mCacheThread;

    public RequestQueue() {
        mNetworkQueue = new LinkedBlockingQueue<>();
        mCacheQueue = new LinkedBlockingQueue<>();
        mWaitQueue = new LinkedHashMap<>();
        mNetDispatcher = new NetworkDispatcher[NET_WORK_THREAD_NUM];
        mNetwork = new NetworkImpl();
        mResponseDelivery = new ResponseDelivery();
        File dir = new File(NetRequestManager.getInstance().getConfig().sCacheDirPath);
        mCache = new DiskBasedCache(dir);
        mCache.initialize();

        start();
    }

    public void start() {
        stop();
        for (int i = 0;i < NET_WORK_THREAD_NUM;i++) {
            mNetDispatcher[i] = new NetworkDispatcher(mNetworkQueue, mNetwork, mResponseDelivery, mCache);
            mNetDispatcher[i].start();
        }
        mCacheThread = new CacheWorkThread(mCache, mCacheQueue, mNetworkQueue, mResponseDelivery);
        mCacheThread.start();
    }

    public void stop() {
        for (int i = 0;i < NET_WORK_THREAD_NUM;i++) {
            if (mNetDispatcher[i] != null) {
                mNetDispatcher[i].quit();
            }
        }
        if (mCacheThread != null) {
            mCacheThread.quit();
        }
    }

    public void add(Request request) {
        if (request == null) {
            return;
        }
        request.setRequestQueue(this);
        if (!request.isShouldCache()) {
            mNetworkQueue.add(request);
        } else {
            synchronized (mWaitQueue) {
                LinkedList waitList = mWaitQueue.get(request.getCacheKey());
                if (waitList == null) {
                    waitList = new LinkedList();
                    mWaitQueue.put(request.getCacheKey(), waitList);
                    mCacheQueue.add(request);
                } else {
                    waitList.add(request);
                }
            }
        }
    }

    public void requestFinished(Request request) {
        synchronized (mWaitQueue) {
            LinkedList<Request> waitList = mWaitQueue.get(request.getCacheKey());
            if (waitList != null) {
                for (Request req : waitList) {
                    mCacheQueue.add(req);
                }
                waitList.clear();
                mWaitQueue.remove(request.getCacheKey());
            }
        }
    }
}
