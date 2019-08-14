package com.example.zhanyu.commonlib.network.impl;


public interface IResponse<T> {

    T getData();

    void setData(T data);

    int getResult();

    void setResult(int result);

    String getMessage();

    void setMessage(String message);
}
