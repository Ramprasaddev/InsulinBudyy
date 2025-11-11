# 🩺 **InsulinBuddy: Smart Insulin Dosage & Glucose Monitoring App**

> A comprehensive health management app designed to help diabetic patients predict insulin dosage, monitor glucose levels, and track overall health patterns — powered by **AI**, **Kotlin**, **FastAPI**, and **PHP-MySQL backend**.

---

## 🌟 **Overview**

**InsulinBuddy** is an Android-based mobile application that leverages **machine learning** and **real-time data tracking** to provide insulin dosage recommendations and glucose monitoring insights for diabetic patients.  
It aims to improve daily diabetes management through automation, reminders, and personalized dosage prediction based on user-specific health data.

The app communicates with a **FastAPI-based backend** (Python) for AI dose prediction and a **PHP-MySQL backend** for user management, profile setup, and data storage.

---

## 💡 **Key Features**

| Category | Features |
|-----------|-----------|
| 🧠 **AI Prediction** | Predicts recommended insulin dosage based on glucose level, carbohydrate intake, activity level, and time of day. |
| 📊 **Glucose & Insulin Graphs** | Displays real-time data visualization over daily, weekly, and monthly trends. |
| 📅 **Date Range Filter** | Allows users to select custom date ranges to view insulin or glucose trends. |
| ⏰ **Smart Reminders** | Automated notifications for insulin intake, glucose check, and carb logging. |
| 🧾 **PDF Report Export** | Users can export their glucose or carb tracking history as PDF reports. |
| 🧍‍♂️ **Profile Management** | Secure profile setup and update for gender, age, diabetes type, weight, ICR, ISR, and target glucose. |
| 💬 **Feedback & Support** | Built-in feedback feature for app improvement suggestions. |
| 🔔 **Notifications System** | Daily summaries, missed entry alerts, and profile completion prompts. |


---

## ⚙️ **Technology Stack**

| Layer | Technology | Purpose |
|-------|-------------|----------|
| **Frontend** | Kotlin (Android Studio) | App UI, user interaction, API calls |
| **Backend 1** | PHP + MySQL | User management, data storage |
| **Backend 2** | FastAPI (Python) | Machine learning-based insulin dosage prediction |
| **Model** | Random Forest Regressor (Joblib) | AI model trained on diabetic patient data |
| **Libraries** | OkHttp, Gson, MPAndroidChart | Networking, JSON handling, Graph plotting |
| **Database** | MySQL | Secure structured data storage |
| **Notifications** | Android AlarmManager & WorkManager | Reminders and alerts |




---

## 🔧 **Setup & Installation**

### **Step 1 — Clone the Repository**
```bash
git clone https://github.com/Ramprasaddev/InsulinBudyy.git
cd InsulinBudyy


Step 2 — Setup FastAPI Backend

Ensure Python 3.9+ is installed.

Create a virtual environment:

python -m venv venv
venv\Scripts\activate


Install dependencies:

pip install fastapi uvicorn pandas scikit-learn joblib


Run the backend:

uvicorn app:app --host 0.0.0.0 --port 8001 --reload


Backend runs on http://10.0.2.2:8001 (for Android Emulator access).

Step 3 — Setup PHP Backend

Copy all PHP files (fetch_user_profile.php, etc.) to your web server folder (e.g., XAMPP htdocs/INSULIN/).

Create a MySQL database and import your tables (e.g., users, insulin_data).

Update database credentials inside the PHP files.

Access PHP APIs using your local or tunnel URL (e.g., https://your-tunnel-url/INSULIN/fetch_user_profile.php).

Step 4 — Run the Android App

Open project in Android Studio.

Ensure dependencies are synced and Gradle builds successfully.

Run the app on an emulator or physical device.

Login → Enter glucose and carb details → Submit → Get AI dose result.

🧠 Machine Learning Model Logic

The FastAPI backend uses a Random Forest Regressor model trained on patient datasets with these input parameters:

Gender

Age

Type of diabetes (Type1 / Type2)

Insulin Sensitivity Ratio (ISR)

Insulin-to-Carb Ratio (ICR)

Target Glucose

Current Glucose

Carbs intake

Physical Activity level (Low, Moderate, High)

Time of day (Morning, Lunch, Evening, Night)

Outputs:

AI Predicted Insulin Dose

Correction Dose = (Current - Target) / ISR

Carb Dose = Carbs / ICR

The final suggested insulin = AI Prediction + Correction + Carb Dose
