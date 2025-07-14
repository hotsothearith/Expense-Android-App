package com.example.week2.dao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.week2.dao.CategoryDao;
import com.example.week2.model.Category;

@Database(entities = {Category.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract CategoryDao categoryDao(); // Correct method name

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "expense_tracker_db")
                    .fallbackToDestructiveMigration() // Handle schema changes
                    .build();
        }
        return instance;
    }
}