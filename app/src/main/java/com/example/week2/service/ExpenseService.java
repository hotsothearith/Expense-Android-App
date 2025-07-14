// ExpenseService.java
package com.example.week2.service;

import com.example.week2.model.Expense;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ExpenseService {

    @GET("expenses")
    Call<List<Expense>> getExpenses(
            @Query("createdBy") String createdBy,
            @Query("_sort") String sort,
            @Query("_order") String order,
            @Query("category") String category,
            @Query("currency") String currency,
            @Query("remark_like") String remarkLike
    );

    @GET("expenses/{id}")
    Call<Expense> getExpense(@Path("id") String id);

    @POST("expenses")
    Call<Expense> createExpense(@Body Expense expense);

    @DELETE("expenses/{id}")
    Call<Void> deleteExpense(@Path("id") String id);
    @Multipart
    @POST("uploadImage") // Replace with your actual upload endpoint
    Call<String> uploadImage(@Part MultipartBody.Part image);
}