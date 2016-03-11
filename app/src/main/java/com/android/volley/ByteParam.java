package com.android.volley;

/**
 * Created by gaojun on 2016/3/11.
 */
public class ByteParam {
    byte[] mContent;
    String fileType;

    private ByteParam(byte[] content, String fileType) {
        mContent = content;
        this.fileType = fileType;
    }

    public static ByteParam createByteParam(byte[] content, String fileType) {
        return new ByteParam(content, fileType);
    }

    public byte[] getContent() {
        return mContent;
    }

    public void setContent(byte[] mContent) {
        this.mContent = mContent;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
