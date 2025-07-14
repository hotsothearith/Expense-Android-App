package com.example.week2;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.week2.model.Expense;
import com.example.week2.repository.ExpenseRepository;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetailExpenseActivity extends AppCompatActivity {

    private TextView tvRemark, tvAmount, tvCurrency, tvCategory, tvCreateDate, tvId, tvCreatedBy, tvReceiptImageLabel;
    private ImageView receiptImageView;
    private ExpenseRepository expenseRepository;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_expense);

        tvRemark = findViewById(R.id.tv_remark);
        tvAmount = findViewById(R.id.tv_amount);
        tvCurrency = findViewById(R.id.tv_currency);
        tvCategory = findViewById(R.id.tv_category);
        tvCreateDate = findViewById(R.id.tv_create_date);
        tvId = findViewById(R.id.tv_id);
        tvCreatedBy = findViewById(R.id.tv_created_by);
        receiptImageView = findViewById(R.id.receiptImageView);
        tvReceiptImageLabel = findViewById(R.id.tv_receipt_Image);

        expenseRepository = new ExpenseRepository();

        String expenseId = getIntent().getStringExtra("expenseId");
        if (expenseId != null) {
            fetchExpenseDetails(expenseId);
        } else {
            Toast.makeText(this, "Expense ID is missing", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchExpenseDetails(String expenseId) {
        expenseRepository.getExpense(expenseId, new ExpenseRepository.IApiCallback<Expense>() {
            @Override
            public void onSuccess(Expense expense) {
                runOnUiThread(() -> {
                    if (expense != null) {
                        tvRemark.setText(expense.getRemark());
                        tvAmount.setText(String.valueOf(expense.getAmount()));
                        tvCurrency.setText(expense.getCurrency());
                        if (expense.getCreatedDate() != null) {
                            tvCreateDate.setText(dateFormat.format(expense.getCreatedDate()));
                        } else {
                            tvCreateDate.setText("N/A");
                        }
                        tvCategory.setText(expense.getCategory());
                        tvId.setText(expense.getId());
                        tvCreatedBy.setText(expense.getCreatedBy());

                        // Load image from network using Glide
                        if (expense.getReceiptImageUrl() != null && !expense.getReceiptImageUrl().isEmpty()) {
                            Log.d("DetailActivity", "Loading image from URL: " + expense.getReceiptImageUrl());
                            Glide.with(DetailExpenseActivity.this)
                                    .load(expense.getReceiptImageUrl())
                                    .addListener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            receiptImageView.setVisibility(View.VISIBLE);
                                            tvReceiptImageLabel.setVisibility(View.VISIBLE);
                                            return false; // Let Glide handle the view
                                        }

                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            Log.e("Glide", "Image load failed", e);
                                            receiptImageView.setVisibility(View.GONE);
                                            tvReceiptImageLabel.setVisibility(View.GONE);
                                            Toast.makeText(DetailExpenseActivity.this, "Failed to load receipt image", Toast.LENGTH_SHORT).show();
                                            return false; // Let Glide handle the view
                                        }
                                    })
                                    .into(receiptImageView);
                        } else {
                            receiptImageView.setVisibility(View.GONE);
                            tvReceiptImageLabel.setVisibility(View.GONE);
                        }

                    } else {
                        Toast.makeText(DetailExpenseActivity.this, "Expense not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(DetailExpenseActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("DetailExpenseActivity", "Error fetching expense: " + errorMessage);
                    finish();
                });
            }
        });
    }
}