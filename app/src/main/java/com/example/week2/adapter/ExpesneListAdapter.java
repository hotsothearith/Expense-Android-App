// ExpesneListAdapter.java
package com.example.week2.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.week2.DetailExpenseActivity;
import com.example.week2.databinding.ItemExpenseBinding;
import com.example.week2.model.Expense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;

public class ExpesneListAdapter extends RecyclerView.Adapter<ExpesneListAdapter.ExpenseViewHolder> {

    private List<Expense> expenses;
    private Context context;
    private SimpleDateFormat dateFormat;

    public ExpesneListAdapter(List<Expense> expenses) {
        this.expenses = expenses;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemExpenseBinding binding = ItemExpenseBinding.inflate(inflater, parent, false);
        context = parent.getContext();
        return new ExpenseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.binding.tvRemark.setText(expense.getRemark());
        holder.binding.tvAmount.setText(String.valueOf(expense.getAmount()));
        holder.binding.tvCurrency.setText(expense.getCurrency());

        Date createdDate = expense.getCreatedDate();
        Log.d("ListAdapterDate", "Expense Date (Raw): " + createdDate);

        if (createdDate != null) {
            String formattedDate = dateFormat.format(createdDate);
            Log.d("ListAdapterDate", "Expense Date (Formatted): " + formattedDate);
            holder.binding.tvCreateDate.setText(formattedDate);
        } else {
            holder.binding.tvCreateDate.setText("N/A");
        }

        holder.binding.tvCategory.setText(expense.getCategory());
        holder.binding.tvId.setText(expense.getId());
        holder.binding.tvCreatedBy.setText(expense.getCreatedBy());
        Log.d("ListAdapter", "Binding expense: " + expense.getId());

        holder.itemView.setOnClickListener(v -> {
            String expenseId = expense.getId();
            Log.d("ListAdapter", "Clicked item with ID: " + expenseId);
            Intent intent = new Intent(holder.itemView.getContext(), DetailExpenseActivity.class);
            intent.putExtra("expenseId", expenseId);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void addExpenses(List<Expense> newExpenses) {
        expenses.addAll(newExpenses);
        notifyDataSetChanged();
    }

    public void clearExpenses() {
        expenses.clear();
        notifyDataSetChanged();
    }

    public class ExpenseViewHolder extends RecyclerView.ViewHolder {
        public final ItemExpenseBinding binding;

        public ExpenseViewHolder(ItemExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}