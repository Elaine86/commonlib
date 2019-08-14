package com.example.zhanyu.commonlib.network.impl;

import java.util.List;

public interface IResponseList<T> {
    List<T> getData();

    void setData(List<T> data);

    int getResult();

    void setResult(int result);

    String getMessage();

    void setMessage(String message);
}
