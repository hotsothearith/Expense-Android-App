# 💸 Expense Android App

A modern, user-friendly Android application built with **Java** for managing personal expenses. This app lets users track spending, attach receipt images, switch languages, customize the background, and store data securely with **Firebase**.

---
## 🌟 Features

### ✅ Expense Management
- ➕ Add a new expense (title, amount, and date)
- 🖊️ Edit or update existing expenses
- 🗑️ Delete expense entries
- 🔍 View detailed info including attached receipt images

### 🖼️ Receipt Upload
- 📷 Capture receipts using the camera
- 🖼️ Select existing images from the gallery
- ☁️ Upload images to **Firebase Storage**
- ⚡ Display images with **Glide**

### 📃 Expense List
- 📜 View expenses in a scrollable list using `RecyclerView`
- 🔄 Real-time sync with **Firebase Firestore**
- ⏱️ Sorted by newest date first

### 🌐 Multi-Language Support
- 🇰🇭 Khmer and 🇺🇸 English switcher
- Instantly changes all UI text
- Localized using `strings.xml` for both languages

### 🎨 UI Customization
- 🖼️ Change background image/theme
- 🎨 Choose light/dark mode or custom colors
- Saved preferences using `SharedPreferences`

### 🔔 Notifications & Feedback
- ⏳ Loading indicators when fetching from Firebase
- ✅ Toast messages and status updates
- ⚠️ Error messages if upload or fetch fails

---

## 🧰 Tech Stack

- **Java**
- **Android Studio**
- **Firebase Firestore** – Real-time database
- **Firebase Storage** – Image storage
- **Glide** – For fast image loading
- **SharedPreferences** – Theme/language settings
- **Material Design** – UI components

## 🚀 Getting Started

### 🔧 Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/hotsothearith/Expense-Android-App.git
Open in Android Studio

Firebase Setup:

Go to Firebase Console

Create a project, enable Firestore & Storage

Add an Android app and download google-services.json

Place it inside app/ folder

Build & Run the app

⚙️ Permissions Used
CAMERA – For taking receipt photos

READ_EXTERNAL_STORAGE – To pick images

INTERNET – For syncing data to Firebase

🔮 Future Plans

📈 Expense charts & insights (e.g., bar/line graphs)

📂 Export data to CSV or PDF

🔍 Filter by category/date range

📆 Calendar-based expense view
