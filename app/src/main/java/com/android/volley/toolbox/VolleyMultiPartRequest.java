package com.android.volley.toolbox;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * A canned request for retrieving the response body at a given URL as a String.
 */
public class VolleyMultiPartRequest extends Request<String> {
    private final Response.Listener<String> mListener;
    private Map<String, Object> mParams = null;
    private Map<String, String> mFileUploads = null;
    public static final int TIMEOUT_MS = 30000;
    private final String mBoundary = "Volley-" + System.currentTimeMillis();
    private final String lineEnd = "\r\n";
    private final String twoHyphens = "--";
    ByteArrayOutputStream mOutputStream = new ByteArrayOutputStream();

    public VolleyMultiPartRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        this.mListener = listener;
        this.mParams = new HashMap<>();
        this.mFileUploads = new HashMap<>();
    }

    /**
     * 参数开头的分隔符
     *
     * @throws IOException
     */
    private void writeFirstBoundary() throws IOException {
        mOutputStream.write((twoHyphens + mBoundary + lineEnd).getBytes());
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + mBoundary;
    }

    @Override
    public byte[] getBody() {
        try {
            // 输出流
            DataOutputStream dos = new DataOutputStream(mOutputStream);

            // 上传参数
            for (String key : mParams.keySet()) {
                writeFirstBoundary();

                final String paramType = "Content-Type: text/plain; charset=UTF-8" + lineEnd;
                dos.writeBytes(paramType);
                dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                dos.writeBytes("Content-Transfer-Encoding: 8bit\r\n\r\n");
                String value = (String)mParams.get(key);
                dos.writeBytes(URLEncoder.encode(value, "UTF-8"));

                dos.writeBytes(lineEnd);
            }

            // 上传文件
            for (String key : mFileUploads.keySet()) {
                writeFirstBoundary();

                File file = new File(mFileUploads.get(key));
                final String type = "Content-Type: application/octet-stream" + lineEnd;
                dos.writeBytes(type);
                String fileName = file.getName();
                dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"" + lineEnd);
                dos.writeBytes("Content-Transfer-Encoding: binary\r\n\r\n");

                FileInputStream fin = new FileInputStream(file);
                final byte[] tmp = new byte[4096];
                int len = 0;
                while ((len = fin.read(tmp)) != -1) {
                    mOutputStream.write(tmp, 0, len);
                }
                fin.close();

                dos.writeBytes(lineEnd);
            }

            // send multipart form data necessary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + mBoundary + twoHyphens + lineEnd);

            return mOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addParam(String name, String value) {
        this.mParams.put(name, value);
    }

    public void addParams(Map<String, Object> params) {
        this.mParams.putAll(params);
    }

    public void addFile(String name, String filePath) {
        this.mFileUploads.put(name, filePath);
    }

    public void addFiles(Map<String, String> filePaths) {
        this.mFileUploads.putAll(filePaths);
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
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> result = new HashMap<String, String>();
        result.putAll(RequestManager.getHeader());
        return result;
    }
}

