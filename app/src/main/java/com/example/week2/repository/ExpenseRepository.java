// ExpenseRepository.java
package com.example.week2.repository;

import android.util.Log;

import com.example.week2.model.Expense;
import com.example.week2.service.ExpenseService;
import com.example.week2.util.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpenseRepository {
    private ExpenseService expenseService;

    public ExpenseRepository() {
        expenseService = RetrofitClient.getClient().create(ExpenseService.class);
    }

    public void getExpenses(String createdBy, String sort, String order, String category, String currency, String remarkLike, int pageNumber, int itemsPerPage, final IApiCallback<List<Expense>> callback) {
        Call<List<Expense>> call = expenseService.getExpenses(createdBy, sort, order, category, currency, remarkLike);

        call.enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<Expense>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void createExpense(Expense expense, final IApiCallback<Expense> callback) {
        expenseService.createExpense(expense).enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteExpense(String expenseId, final IApiCallback<String> callback) {
        expenseService.deleteExpense(expenseId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("Expense deleted successfully");
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getExpense(String expenseId, final IApiCallback<Expense> callback) {
        expenseService.getExpense(expenseId).enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private String getErrorMessage(Response<?> response) {
        if (response.errorBody() != null) {
            try {
                String errorBody = response.errorBody().string();
                try {
                    JsonObject jsonObject = new Gson().fromJson(errorBody, JsonObject.class);
                    return "Error: " + response.code() + " - " + jsonObject.toString();
                } catch (com.google.gson.JsonSyntaxException e) {
                    return "Error: " + response.code() + " - " + errorBody;
                }

            } catch (IOException e) {
                Log.e("ExpenseRepository", "Failed to read error body", e);
                return "Error: " + response.code() + " (failed to read error body)";
            }
        }
        return "Unknown error: " + response.code();
    }

    public interface IApiCallback<T> {
        void onSuccess(T result);

        void onError(String errorMessage);
    }
}