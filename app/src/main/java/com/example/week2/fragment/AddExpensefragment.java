package com.example.week2.fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.week2.NewCategoryActivity;
import com.example.week2.R;
import com.example.week2.dao.AppDatabase;
import com.example.week2.model.Category;
import com.example.week2.model.Expense;
import com.example.week2.repository.ExpenseRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.JsonToken;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class AddExpensefragment extends Fragment {

    private EditText amountEditText;
    private RadioGroup currencyRadioGroup;
    private Spinner categorySpinner;
    private ImageButton addCategoryButton;
    private EditText remarkEditText;
    private EditText dateEditText;
    private Button btnAddExpense;
    private ProgressBar progressBar;
    private ExpenseRepository expenseRepository;
    private FirebaseAuth mAuth;
    private AppDatabase db;
    private List<String> categoryList;
    private ArrayAdapter<String> categoryAdapter;

    private static final int NEW_CATEGORY_REQUEST_CODE = 100;
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final String CHANNEL_ID = "budget_warning_channel"; // Notification channel ID
    private static final String FCM_CHANNEL_ID = "fcm_channel";  //  FCM

    private Button btnTakePhoto;
    private Button btnPickImage;
    private ImageView receiptImageView;
    private Uri currentPhotoUri;
    private String receiptImageUrl;

    private NotificationManager notificationManager; // Moved to class level

    public AddExpensefragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("AddExpenseFragment", "onCreateView() called");
        View view = inflater.inflate(R.layout.fragment_add_expense, container, false);

        amountEditText = view.findViewById(R.id.AmountInput);
        currencyRadioGroup = view.findViewById(R.id.currencyRadioGroup);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        addCategoryButton = view.findViewById(R.id.addCategoryButton);
        remarkEditText = view.findViewById(R.id.remark);
        dateEditText = view.findViewById(R.id.editTextDate);
        btnAddExpense = view.findViewById(R.id.btn_add_expense);
        progressBar = view.findViewById(R.id.tasks_progress_bar);
        btnTakePhoto = view.findViewById(R.id.btn_take_photo);
        btnPickImage = view.findViewById(R.id.btn_pick_image);
        receiptImageView = view.findViewById(R.id.receiptImageView);
        receiptImageView.setVisibility(View.GONE); // Initially hide the image view

        expenseRepository = new ExpenseRepository();
        mAuth = FirebaseAuth.getInstance();
        db = AppDatabase.getInstance(requireContext());

        categoryList = new ArrayList<>();
        categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryList);
        categorySpinner.setAdapter(categoryAdapter);

        loadCategories();

        btnAddExpense.setOnClickListener(v -> addExpense());
        addCategoryButton.setOnClickListener(v -> navigateToNewCategory());

        btnTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
            } else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        });

        btnPickImage.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
        });

        // Initialize NotificationManager
        notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannels(); // Create both channels

        // Get FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("AddExpenseFragment", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    // Log and toast
                    String msg = "Token: " + token;
                    Log.d("AddExpenseFragment", msg);
                    //Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show(); //Don't show token in toast.
                });


        Log.d("AddExpenseFragment", "onCreateView() finished");

        return view;
    }

    private void createNotificationChannels() {
        // Create the NotificationChannel, but only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Budget Warning Channel
            CharSequence name = "Budget Warning Channel";
            String description = "Channel for budget limit exceeded warnings";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);

            // FCM Channel
            CharSequence fcmName = "FCM Channel";
            String fcmDescription = "Channel for general notifications from FCM";
            int fcmImportance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel fcmChannel = new NotificationChannel(FCM_CHANNEL_ID, fcmName, fcmImportance);
            fcmChannel.setDescription(fcmDescription);
            notificationManager.createNotificationChannel(fcmChannel);
        }
    }


    private void navigateToNewCategory() {
        Intent intent = new Intent(requireContext(), NewCategoryActivity.class);
        startActivityForResult(intent, NEW_CATEGORY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_IMAGE_CAPTURE) { // Changed to REQUEST_IMAGE_CAPTURE
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("AddExpenseFragment", "Error creating image file", ex);
                Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
                return;
            }
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(requireContext(),
                        "com.example.week2.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(requireContext(), "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_CATEGORY_REQUEST_CODE && resultCode == RESULT_OK) {
            loadCategories();
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK) {
                if (data != null) {
                    currentPhotoUri = data.getData();
                    receiptImageView.setImageURI(currentPhotoUri);
                    receiptImageView.setVisibility(View.VISIBLE);
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (currentPhotoUri != null) {
                    receiptImageView.setImageURI(currentPhotoUri);
                    receiptImageView.setVisibility(View.VISIBLE);
                } else if (data != null && data.getExtras() != null) {
                    android.graphics.Bitmap imageBitmap = (android.graphics.Bitmap) data.getExtras().get("data");
                    receiptImageView.setImageBitmap(imageBitmap);
                    receiptImageView.setVisibility(View.VISIBLE);
                    // Consider saving this Bitmap to a file to get a Uri for Firebase upload
                    // For simplicity in this full code, we'll proceed with the Bitmap (though saving to a file is better for large images)
                    // If you choose to save to a file here, update currentPhotoUri accordingly.
                } else {
                    Log.w("AddExpenseFragment", "No image data received from camera");
                    Toast.makeText(requireContext(), "Error capturing image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void loadCategories() {
        new Thread(() -> {
            List<Category> categories = db.categoryDao().getAll();
            List<String> categoryNames = new ArrayList<>();
            for (Category category : categories) {
                categoryNames.add(category.getName());
            }
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    categoryList.clear();
                    categoryList.addAll(categoryNames);
                    categoryAdapter.notifyDataSetChanged();
                });
            }
        }).start();
    }

    private void addExpense() {
        Log.d("AddExpenseFragment", "addExpense() called");
        String amountString = amountEditText.getText().toString().trim();
        String remark = remarkEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String currency = getSelectedCurrency();
        String dateString = dateEditText.getText().toString().trim();

        if (amountString.isEmpty() || remark.isEmpty() || category.isEmpty() || currency.isEmpty() || dateString.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int amount = Integer.parseInt(amountString);
            showProgressBar(true);
            saveImageAndThenCreateExpense(); // Handle image upload before creating expense
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            showProgressBar(false);
            Log.e("AddExpenseFragment", "NumberFormatException: " + e.getMessage());
        }
    }

    private void showBudgetWarningNotification(String remark) {
        // Create the NotificationChannel, but only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning) // Replace with your warning icon (create in drawable)
                .setContentTitle("Budget Warning!")
                .setContentText("You have exceeded your budget for \"" + remark + "\"")
                .setPriority(NotificationCompat.PRIORITY_MIN);

        notificationManager.notify(1, builder.build());
    }


    private void saveImageAndThenCreateExpense() {
        if (currentPhotoUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            String userId = mAuth.getCurrentUser().getUid();
            StorageReference storageRef = storage.getReference().child("receipts/" + userId + "/" + UUID.randomUUID().toString());

            storageRef.putFile(currentPhotoUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> downloadUrlTask = storageRef.getDownloadUrl();
                        downloadUrlTask.addOnSuccessListener(downloadUri -> {
                            receiptImageUrl = downloadUri.toString();
                            Log.d("AddExpenseFragment", "Firebase Image URL: " + receiptImageUrl);
                            createExpense(); // Now create the expense with the URL
                        });
                        downloadUrlTask.addOnFailureListener(e -> {
                            showProgressBar(false);
                            Toast.makeText(requireContext(), "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("AddExpenseFragment", "Firebase get download URL error", e);
                        });
                    })
                    .addOnFailureListener(e -> {
                        showProgressBar(false);
                        Toast.makeText(requireContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("AddExpenseFragment", "Firebase upload error", e);
                    });
        } else if (receiptImageView.getDrawable() != null) {
            // If a Bitmap is in the ImageView (from direct camera capture without EXTRA_OUTPUT)
            receiptImageView.buildDrawingCache();
            android.graphics.Bitmap bitmap = receiptImageView.getDrawingCache();
            if (bitmap != null) {
                // Convert Bitmap to a byte array for Firebase upload
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                FirebaseStorage storage = FirebaseStorage.getInstance();
                String userId = mAuth.getCurrentUser().getUid();
                StorageReference storageRef = storage.getReference().child("receipts/" + userId + "/" + UUID.randomUUID().toString());

                storageRef.putBytes(data)
                        .addOnSuccessListener(taskSnapshot -> {
                            Task<Uri> downloadUrlTask = storageRef.getDownloadUrl();
                            downloadUrlTask.addOnSuccessListener(downloadUri -> {
                                receiptImageUrl = downloadUri.toString();
                                Log.d("AddExpenseFragment", "Firebase Image URL (from Bitmap): " + receiptImageUrl);
                                createExpense();
                            });
                            downloadUrlTask.addOnFailureListener(e -> {
                                showProgressBar(false);
                                Toast.makeText(requireContext(), "Failed to get download URL (Bitmap): " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("AddExpenseFragment", "Firebase get download URL error (Bitmap)", e);
                            });
                        })
                        .addOnFailureListener(e -> {
                            showProgressBar(false);
                            Toast.makeText(requireContext(), "Image upload failed (Bitmap): " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("AddExpenseFragment", "Firebase upload error (Bitmap)", e);
                        });
            } else {
                receiptImageUrl = null;
                createExpense();
            }
            receiptImageView.destroyDrawingCache();
        } else {
            receiptImageUrl = null;
            createExpense(); // No image selected
        }
    }

    private void createExpense() {
        Log.d("AddExpenseFragment", "createExpense() called");
        String amountString = amountEditText.getText().toString().trim();
        String remark = remarkEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String currency = getSelectedCurrency();
        String dateString = dateEditText.getText().toString().trim();
        int amount = Integer.parseInt(amountString); // Amount is already parsed in addExpense

        Expense expense = new Expense();
        expense.setId(UUID.randomUUID().toString());
        expense.setAmount(amount);
        expense.setCurrency(currency);
        expense.setCategory(category);
        expense.setRemark(remark);
        expense.setCreatedBy(mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "");
        expense.setCreatedDate(parseAndFormatDate(dateString));
        expense.setReceiptImageUrl(receiptImageUrl); // Set the image URL

        Log.d("AddExpenseFragment", "Expense data before sending: " + expense);

        expenseRepository.createExpense(expense, new ExpenseRepository.IApiCallback<Expense>() {
            @Override
            public void onSuccess(Expense result) {
                if (isAdded()) {
                    showProgressBar(false);
                    Toast.makeText(getContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                    loadListFragment();
                    Log.d("AddExpenseFragment", "Expense added successfully. Result: " + result);

                    // Check budget limit and trigger notification
                    if ((currency.equals("USD") && amount > 100) || (currency.equals("KHR") && amount > 400000)) {
                        showBudgetWarningNotification(remark);
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {
                    showProgressBar(false);
                    Toast.makeText(getContext(), "Failed to add expense: " + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("AddExpenseFragment", "Error adding expense: " + errorMessage);
                }
            }
        });
    }

    private String getSelectedCurrency() {
        int selectedId = currencyRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.USDButton) {
            return "USD";
        } else if (selectedId == R.id.KHRButton) {
            return "KHR";
        }
        return "";
    }

    private void clearFields() {
        amountEditText.setText("");
        currencyRadioGroup.clearCheck();
        categorySpinner.setSelection(0);
        remarkEditText.setText("");
        dateEditText.setText("");
        receiptImageView.setImageDrawable(null);
        receiptImageView.setVisibility(View.GONE);
        currentPhotoUri = null;
        receiptImageUrl = null;
    }

    private void showProgressBar(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        amountEditText.setEnabled(!show);
        currencyRadioGroup.setEnabled(!show);
        categorySpinner.setEnabled(!show);
        addCategoryButton.setEnabled(!show);
        remarkEditText.setEnabled(!show);
        dateEditText.setEnabled(!show);
        btnAddExpense.setEnabled(!show);
        btnTakePhoto.setEnabled(!show);
        btnPickImage.setEnabled(!show);
    }

    private void loadListFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, new ListExpensefragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public static class ISO8601DateAdapter extends TypeAdapter<Date> {
        private final SimpleDateFormat formatter;

        public ISO8601DateAdapter() {
            formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        @Override
        public void write(JsonWriter out, Date date) throws IOException {
            if (date == null) {
                out.nullValue();
            } else {
                out.value(formatter.format(date));
            }
        }

        @Override
        public Date read(JsonReader in) throws IOException {
            try {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }
                return formatter.parse(in.nextString());
            } catch (ParseException e) {
                Log.e("ISO8601DateAdapter", "Error parsing date: " + in.nextString(), e);
                in.skipValue();
                return null;
            }
        }
    }

    private Date parseAndFormatDate(String dateString) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // Adjust to your input format
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = inputFormat.parse(dateString);
            return date; // Return the parsed Date object
        } catch (ParseException e) {
            Log.e("AddExpenseFragment", "Error parsing date: " + dateString, e);
            return null; // Or handle the error as appropriate
        }
    }

    @Override
    public String toString() {
        return "AddExpensefragment{}";
    }
}

