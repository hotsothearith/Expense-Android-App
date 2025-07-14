package com.example.week2;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.week2.dao.AppDatabase;
import com.example.week2.model.Category;

public class NewCategoryActivity extends BaseActivity {

    private EditText TextCategoryName;
    private Button buttonAddCategory;
    private AppDatabase db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_category);

        TextCategoryName = findViewById(R.id.TextCategoryName);
        buttonAddCategory = findViewById(R.id.buttonAddCategory);
        db = AppDatabase.getInstance(getApplicationContext());

        buttonAddCategory.setOnClickListener(v -> {
            String categoryName = TextCategoryName.getText().toString().trim();
            if (!categoryName.isEmpty()) {
                insertCategory(new Category(categoryName));
            } else {
                Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // NewCategoryActivity.java
    private void insertCategory(Category category) {
        new Thread(() -> {
            long id = db.categoryDao().insert(category);
            runOnUiThread(() -> {
                if (id > 0) {
                    Toast.makeText(this, "Category added successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Set the result code here
                    finish();
                } else {
                    Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}