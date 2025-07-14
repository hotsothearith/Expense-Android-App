package com.example.week2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.week2.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends BaseActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.createBtn.setOnClickListener(v -> {
            onAlreadyHaveAccountClicked();
        });
        binding.signBtn.setOnClickListener(v -> {
            onSignUpClicked();
        });

    }

    private void onSignUpClicked() {
        String email = binding.inputEmail.getText().toString();
        String password = binding.inputPassword.getText().toString();
        String confirmPassword = binding.confrimPassword.getText().toString(); // Corrected line

        if(!password.equals(confirmPassword)){
            Toast.makeText(this, "Confirm password doesn't match!", Toast.LENGTH_SHORT).show();
        }else {
            binding.pbLoginProgress.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                binding.pbLoginProgress.setVisibility(View.INVISIBLE);
                                Intent loginIntent = new Intent(Register.this, Login.class);
                                loginIntent.putExtra("email", email);
                                loginIntent.putExtra("password", password);
                                setResult(RESULT_OK, loginIntent);
                                finish();
                            } else {
                                Toast.makeText(Register.this, task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                binding.pbLoginProgress.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
        }

    }

    private void onAlreadyHaveAccountClicked() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
}