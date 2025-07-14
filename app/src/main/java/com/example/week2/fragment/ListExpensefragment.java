// ListExpensefragment.java
package com.example.week2.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week2.R;
import com.example.week2.adapter.ExpesneListAdapter;
import com.example.week2.model.Expense;
import com.example.week2.repository.ExpenseRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date; // Explicitly import Date

public class ListExpensefragment extends Fragment {

    private RecyclerView rcvExpenses;
    private ProgressBar progressBar;
    private ExpenseRepository expenseRepository;
    private FirebaseAuth mAuth;

    private static final int PRE_LOAD_ITEMS = 2;
    private ExpesneListAdapter expesneListAdapter;
    private int pageNumber = 1;
    private int itemsPerPage = 10;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    private LinearLayoutManager layoutManager;
    private SimpleDateFormat dateFormat;

    public ListExpensefragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_list, container, false);
        rcvExpenses = view.findViewById(R.id.rcvExpenses);
        progressBar = view.findViewById(R.id.progressBar);
        expenseRepository = new ExpenseRepository();
        mAuth = FirebaseAuth.getInstance();

        layoutManager = new LinearLayoutManager(requireContext());
        rcvExpenses.setLayoutManager(layoutManager);
        expesneListAdapter = new ExpesneListAdapter(new ArrayList<>());
        rcvExpenses.setAdapter(expesneListAdapter);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        rcvExpenses.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage && (firstVisibleItemPosition + visibleItemCount >= totalItemCount)
                        && firstVisibleItemPosition >= 0 && totalItemCount >= itemsPerPage) {
                    loadExpenses();
                }
            }
        });

        loadExpenses();
        Log.d("ListExpenseFragment", "onCreateView() finished");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("ListExpenseFragment", "onResume() called - Refreshing data");
        loadExpenses();
    }

    private void loadExpenses() {
        Log.d("ListExpenseFragment", "loadExpenses() called. Page: " + pageNumber + ", Limit: " + itemsPerPage);
        showProgressBar(true);
        isLoading = true;

        expenseRepository.getExpenses(
                null,
                null, null, null, null, null,
                pageNumber, itemsPerPage,
                new ExpenseRepository.IApiCallback<List<Expense>>() {
                    @Override
                    public void onSuccess(List<Expense> expenses) {
                        if (isAdded()) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showProgressBar(false);
                                    isLoading = false;

                                    if (expenses == null || expenses.isEmpty()) {
                                        isLastPage = true;
                                        Toast.makeText(requireContext(), "No expenses found.", Toast.LENGTH_SHORT).show();
                                        expesneListAdapter.clearExpenses();
                                        return;
                                    }

                                    expesneListAdapter.clearExpenses();
                                    expesneListAdapter.addExpenses(expenses);
                                    Log.d("ListExpenseFragment", "Expenses loaded. Total count: " + expesneListAdapter.getItemCount());
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (isAdded()) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showProgressBar(false);
                                    isLoading = false;
                                    Log.e("ListExpenseFragment", "Error loading expenses: " + errorMessage);
                                    Toast.makeText(requireContext(), "Error loading expenses: " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }
        );
    }

    private void showProgressBar(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rcvExpenses.setVisibility(show ? View.GONE : View.VISIBLE);
        Log.d("ListExpenseFragment", "showProgressBar(" + show + ") called");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("ListExpenseFragment", "onDestroyView() called");
        // Clear adapter or other resources if needed
    }
}