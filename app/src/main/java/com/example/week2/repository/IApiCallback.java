package com.example.week2.repository;

public interface IApiCallback<T> {
    void onSuccess(T result);

    void onError(String errorMessage);
}