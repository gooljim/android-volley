package com.android.volley.toolbox;

import com.android.volley.AuthFailureError;
import com.android.volley.CanceledError;
import com.android.volley.ExecutorDelivery;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;

/**
 */

public class DownloadRequest extends Request<File> {

    private final Response.Listener<File> mListener;
    private String mSavePath;
    private boolean mAutoResume;
    private ExecutorDelivery.ProgressDeliveryRunnable mDeliverRunnable;

    public DownloadRequest(String url, String fileSavePath, Response.Listener<File> listener, Response.ErrorListener errorListener) {
        this(Method.GET, url, fileSavePath, false, listener, errorListener);
    }

    public DownloadRequest(int method, String url, String fileSavePath, boolean autoResume, Response.Listener<File> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        mSavePath = fileSavePath;
        mAutoResume = autoResume;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        if (mAutoResume) {
            File downloadFile = new File(mSavePath);
            long fileLen = 0;
            if (downloadFile.isFile() && downloadFile.exists()) {
                fileLen = downloadFile.length();
            }
            if (fileLen > 0) {
                headers.put("RANGE", "bytes=" + fileLen + "-");
                return headers;
            }
        }
        return headers;
    }

    public boolean isSupportRange(final okhttp3.Response response) {
        if (response == null) return false;
        String header = response.header("Accept-Ranges");
        if (header != null) {
            return "bytes".equals(header);
        }
        header = response.header("Content-Range");
        if (header != null) {
            return header != null && header.startsWith("bytes");
        }
        return false;
    }

    @Override
    public byte[] handleRawResponse(okhttp3.Response httpResponse) throws IOException, ServerError, CanceledError {
        int statusCode = httpResponse.code();

        if (httpResponse.isSuccessful()) {
            mAutoResume = mAutoResume && isSupportRange(httpResponse);

            ResponseBody entity = httpResponse.body();

            File targetFile = new File(mSavePath);

            if (!targetFile.exists()) {
                File dir = targetFile.getParentFile();
                if (dir.exists() || dir.mkdirs()) {
                    targetFile.createNewFile();
                }
            }

            long current = 0;
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                FileOutputStream fileOutputStream = null;
                if (mAutoResume) {
                    current = targetFile.length();
                    fileOutputStream = new FileOutputStream(mSavePath, true);
                } else {
                    fileOutputStream = new FileOutputStream(mSavePath);
                }
                long total = entity.contentLength() + current;
                bis = new BufferedInputStream(entity.byteStream());
                bos = new BufferedOutputStream(fileOutputStream);

                if (isCanceled()) {
                    throw new CanceledError();
                }
                postProgress(false, current, total);

                byte[] tmp = new byte[4096];
                int len;
                long oldCurrent = current;
                while ((len = bis.read(tmp)) != -1) {
                    bos.write(tmp, 0, len);
                    current += len;
                    if (isCanceled()) {
                        throw new CanceledError();
                    }
                    if ((current - oldCurrent) * 100 / total >= 1) {
                        postProgress(false, current, total);
                        oldCurrent = current;
                    }
                }
                bos.flush();
                postProgress(true, total, total);
                return mSavePath.getBytes();
            } finally {
                // Close the InputStream and release the resources by "consuming the content".
                entity.close();
                closeQuietly(bos);
                closeQuietly(bis);
            }
        } else if (statusCode == 416) {
            throw new IOException("may be the file have been download finished.");
        } else {
            throw new IOException();
        }
    }

    public void postProgress(boolean isUpload, long current, long total) {
        if (mDeliverRunnable == null) {
            mDeliverRunnable = new ExecutorDelivery.ProgressDeliveryRunnable(this);
        }
        mDeliverRunnable.setCurrent(current);
        mDeliverRunnable.setIsUpload(isUpload);
        mDeliverRunnable.setTotal(total);
        if (mLoadingListener != null && mResponseDelivery != null) {
            mResponseDelivery.postProgress(mDeliverRunnable);
        }
    }

    public void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
            }
        }
    }

    @Override
    protected Response<File> parseNetworkResponse(NetworkResponse response) {
        String filename = new String(response.data);
        if (response.data.length > 0) {
            return Response.success(new File(filename), null);
        } else {
            return Response.error(new ParseError(response));
        }
    }

    @Override
    protected void deliverResponse(File response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }


}
