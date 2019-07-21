package com.ding.networkframe;

import java.io.InterruptedIOException;

/**
 * Created by jindingwei on 2019/7/20.
 */

public interface Network {

    NetworkResponse performRequest(Request request) throws NetRequetsError, InterruptedIOException;
}
