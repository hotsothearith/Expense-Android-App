package com.example.week2.service;

import com.example.week2.model.User;

import java.util.List;

public interface IUserService {
    void Register(String email, String password, String confirmPassword);
    User login(String email, String password);
    List<User> getAllUsers();
}
