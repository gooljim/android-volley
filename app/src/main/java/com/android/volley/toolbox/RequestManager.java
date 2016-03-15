/*
 * Created by Storm Zhang, Feb 11, 2014.
 */

package com.android.volley.toolbox;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import java.util.HashMap;
import java.util.Map;

public class RequestManager {
    private static RequestQueue sRequestQueue;
    private static Map<String, String> sHeader = new HashMap<>();
    public static Context sContext;

    private RequestManager() {
        // no instances
    }

    public static void init(Context context) {
        sRequestQueue = Volley.newRequestQueue(context);
        sContext = context;
    }

    public static RequestQueue getRequestQueue() {
        if (sRequestQueue != null) {
            return sRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    /**
     * 返回额外头部信息
     *
     * @return
     */
    public static Map<String, String> getHeader() {
        return sHeader;
    }

    /**
     * 添加请求的额外的头部信息
     *
     * @param key
     * @param value
     */
    public static void addHeader(String key, String value) {
        if (value == null) {
            value = "";
        }
        sHeader.put(key, value);
    }

    public static void addRequest(Request<?> request, Object tag) {
        if (tag != null) {
            request.setTag(tag);
        }
        sRequestQueue.add(request);
    }

    public static void cancelAll(Object tag) {
        sRequestQueue.cancelAll(tag);
    }


}
