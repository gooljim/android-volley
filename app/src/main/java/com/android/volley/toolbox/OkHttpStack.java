package com.android.volley.toolbox;

import com.android.volley.AuthFailureError;
import com.android.volley.ByteParam;
import com.android.volley.Request;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 */
public class OkHttpStack implements HttpStack {
    private OkHttpClient mClient;

    public OkHttpStack() {
        this.mClient = new OkHttpClient();
    }


    @SuppressWarnings("deprecation")
    private static void setConnectionParametersForRequest
            (okhttp3.Request.Builder builder, Request<?> request)
            throws IOException, AuthFailureError {
        switch (request.getMethod()) {
            case Request.Method.DEPRECATED_GET_OR_POST:
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    builder.post(RequestBody.create
                            (MediaType.parse(request.getPostBodyContentType()), postBody));
                }
                break;

            case Request.Method.GET:
                builder.get();
                break;

            case Request.Method.DELETE:
                builder.delete();
                break;

            case Request.Method.POST:
                builder.post(createRequestBody(request));
                break;

            case Request.Method.PUT:
                builder.put(createRequestBody(request));
                break;

            case Request.Method.HEAD:
                builder.head();
                break;

            case Request.Method.OPTIONS:
                builder.method("OPTIONS", null);
                break;

            case Request.Method.TRACE:
                builder.method("TRACE", null);
                break;

            case Request.Method.PATCH:
                builder.patch(createRequestBody(request));
                break;

            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static RequestBody createRequestBody(Request<?> request) throws AuthFailureError {
        final int type = request.getBodyCreateType();
        if (type == Request.BodyType.MULTIPART) {
            return createMultiPartBody(request);
        } else {
            final byte[] body = request.getBody();
            if (body == null) return null;
            return RequestBody.create(MediaType.parse(request.getBodyContentType()), body);
        }

    }

    private static RequestBody createMultiPartBody(Request<?> request) throws AuthFailureError {
        Map<String, Object> content = request.getParams();
        if (content == null) return null;
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        for (Map.Entry<String, Object> entry : content.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof ByteParam) {
                requestBodyBuilder.addFormDataPart(key, key, RequestBody.create(MediaType.parse(((ByteParam) value).getFileType()), ((ByteParam) value).getContent()));
            } else {
                requestBodyBuilder.addFormDataPart(key, String.valueOf(entry.getValue()));
            }

        }
        RequestBody requestBody = requestBodyBuilder.build();
        return requestBody;
    }


    @Override
    public okhttp3.Response performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {
        if (request.getBodyCreateType() == Request.BodyType.MULTIPART) {
            mClient = new OkHttpClient();
        }
        int timeoutMs = request.getTimeoutMs();
        OkHttpClient client = mClient.newBuilder()
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .build();

        okhttp3.Request.Builder okHttpRequestBuilder = new okhttp3.Request.Builder();
        Map<String, String> headers = request.getHeaders();
        String url = request.getUrl();
        //add cookie
        headers.put(request.getCookieKey(), CookieManager.getInstance().getCookie(url));
        for (final String name : headers.keySet()) {
            okHttpRequestBuilder.addHeader(name, headers.get(name));
        }

        for (final String name : additionalHeaders.keySet()) {
            okHttpRequestBuilder.addHeader(name, additionalHeaders.get(name));
        }
        setConnectionParametersForRequest(okHttpRequestBuilder, request);

        okhttp3.Request okhttp3Request = okHttpRequestBuilder.url(request.getUrl()).build();
        Response okHttpResponse = client.newCall(okhttp3Request).execute();

        Headers responseHeaders = okHttpResponse.headers();
        for (int i = 0, len = responseHeaders.size(); i < len; i++) {
            final String name = responseHeaders.name(i), value = responseHeaders.value(i);
            if (name != null) {
                if (name.toLowerCase(Locale.getDefault()).equals("set-cookie")) {
                    CookieManager.getInstance().setCookie(request.getUrl(), value);
                }
            }
        }
        return okHttpResponse;
    }
}