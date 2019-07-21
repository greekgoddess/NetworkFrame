package com.ding.networkframe;

/**
 * Created by jindingwei on 2019/7/20.
 */

public class NetRequestManager {

    private static volatile NetRequestManager mInstance;
    private RequestQueue mQueue;
    private NetFrameConfig mConfig;

    private NetRequestManager() {
        mConfig = new NetFrameConfig();
    }

    public static NetRequestManager getInstance() {
        if (mInstance == null) {
            synchronized (NetRequestManager.class) {
                if (mInstance == null) {
                    mInstance = new NetRequestManager();
                }
            }
        }
        return mInstance;
    }

    public static void init(NetFrameConfig config) {
        NetRequestManager.getInstance().initCofig(config);
    }

    public void initCofig(NetFrameConfig config) {
        if (config == null) {
            mConfig = config;
        }
        mQueue = new RequestQueue();
    }

    public NetFrameConfig getConfig() {
        return mConfig;
    }

    public void request(Request request) {
        mQueue.add(request);
    }

    public void stop() {
        mQueue.stop();
    }
}
