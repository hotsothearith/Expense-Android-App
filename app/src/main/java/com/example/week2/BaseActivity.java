package com.example.week2;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.week2.util.LocaleHelper;
import com.example.week2.util.ThemeHelper;

public class BaseActivity extends AppCompatActivity  {
    @Override
    protected void attachBaseContext(Context newBase) {
//       apply the locale using localeHelper
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguageFromPreferences(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.setTheme(this, ThemeHelper.getThemeFromPreferences(this));
    }
}
