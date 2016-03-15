package com.android.volley.toolbox;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ByteArrayRequest extends Request<String> {
    private Map<String, Object> mFileUploads = null;
    private final Response.Listener<String> mListener;

    /**
     * Creates a new request with the given method.
     *
     * @param method        the request {@link Method} to use
     * @param url           URL to fetch the string at
     * @param listener      Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public ByteArrayRequest(int method, String url, Response.Listener<String> listener,
                            Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        mFileUploads = new HashMap<>();
    }

    /**
     * Creates a new GET request.
     *
     * @param url           URL to fetch the string at
     * @param listener      Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public ByteArrayRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    public Map<String, Object> getParams() {
        return mFileUploads;
    }

    @Override
    public int getBodyCreateType() {
        return BodyType.MULTIPART;
    }

    public void addParams(Map<String, Object> value) {
        mFileUploads.putAll(value);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> result = new HashMap<String, String>();
        result.putAll(RequestManager.getHeader());
        return result;
    }
}
