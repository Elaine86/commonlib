package com.example.zhanyu.commonlib.image.bean;


public class UploadTaskBean {
    // 原始文件
    String filePath;
    // 上传后的文件
    String url;
    // 上传进度
    int progress;
    // 状态 准备中，上传中、出错、完成
    UploadState state;

    public enum UploadState {
        UPLOAD_PREPARE, UPLOADING, UPLOAD_FAIL, UPLOAD_SUCCESS
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public UploadState getState() {
        return state;
    }

    public void setStatue(UploadState statue) {
        this.state = statue;
    }
}
