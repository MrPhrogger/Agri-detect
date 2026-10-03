# Agri Detect - Android App

An IoT companion Android application for real-time air quality monitoring. This app connects to a Firebase Realtime Database to display live sensor data (Temperature, Humidity, and Gas Levels) sent from an ESP32 microcontroller.

---

## Features

- **Real-Time Monitoring:** Live data updates from Firebase Realtime Database.
- **Air Quality Status:** Instant alert card when gas is detected (Haptic feedback + visual warning).
- **Custom Gauges:** Beautiful arc gauge for Temperature and wave orb for Humidity.
- **Historical Data:** View temperature and humidity trends with line charts.
- **Time Range Filters:** View history for the last 30 minutes, 6 hours, or 24 hours.
- **Gas Event Log:** See how many gas events occurred in the selected time period.
- **Firebase Authentication:** Secure Login and Signup using:
  - Email & Password
  - Google Sign-In
  - Phone Number (OTP)
- **Clean UI:** Modern, minimalistic design using the "Cozy Greenery" color palette.

---

## Tech Stack

- **Language:** Java
- **IDE:** Android Studio
- **Backend:** Firebase Realtime Database
- **Authentication:** Firebase Auth (Email, Google, Phone)
- **UI Components:** Material Design, Custom Views (ArcGauge, WaveOrb, LineChart)
- **Min SDK:** 24 (Android 7.0 Nougat)
- **Target SDK:** 34 (Android 14)

---

##  Project Structure

Android_App/
├── app/
│ ├── src/main/
│ │ ├── java/com/example/agri_detect/
│ │ │ ├── MainActivity.java # Main Menu (Login / Signup)
│ │ │ ├── LoginActivity.java # Login logic (Email, Google, Phone)
│ │ │ ├── SignupActivity.java # Signup logic
│ │ │ ├── DashboardActivity.java # Main dashboard (Live data + History)
│ │ │ ├── HistoryStore.java # Local storage for history
│ │ │ ├── ArcGaugeView.java # Custom temperature gauge
│ │ │ ├── WaveOrbView.java # Custom humidity orb
│ │ │ ├── LineChartView.java # Custom line chart
│ │ │ ├── StatusOrbView.java # Gas status indicator
│ │ │ └── GasStripView.java # Gas event timeline
│ │ └── res/
│ │ ├── layout/ # UI layout files
│ │ ├── drawable/ # Custom shapes and icons
│ │ └── values/ # Colors, strings, themes
│ ├── google-services.json # Firebase config (DO NOT SHARE PUBLICLY)
│ └── build.gradle.kts
└── build.gradle.kts

---

## Getting Started

### Prerequisites
- Android Studio (Latest version)
- A Firebase Project
- The ESP32 hardware already pushing data to Firebase (see the `/ESP32_Code` folder in the parent repository)

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
Open the project in Android Studio:

File → Open → Select the Android_App folder.

Set up Firebase:

Go to the Firebase Console.

Create a new project or use an existing one.

Add an Android app with the package name: com.example.agri_detect.

Download the google-services.json file.

Place google-services.json inside the app/ directory.

Enable Firebase Services:

Realtime Database: Create a database in test mode (or set your rules).

Authentication: Enable Email/Password, Google, and Phone sign-in methods.

Add SHA-1 Fingerprint (Required for Google Sign-In):

In Android Studio, open the Gradle tab → app → Tasks → android → signingReport.

Copy the SHA-1 hash.

In Firebase Console → Project Settings → Your Android App → Add fingerprint.

Re-download google-services.json and replace the old one.

Build and Run:

Click the Run button in Android Studio.

🗄️ Firebase Database Structure
The app expects the following structure in your Realtime Database:

json
{
  "air_quality_log": {
    "-NxAbc123": {
      "temperature": 24.5,
      "humidity": 60.2,
      "gas_detected": false,
      "gas_status": "normal air quality"
    },
    "-NxDef456": {
      "temperature": 25.1,
      "humidity": 58.7,
      "gas_detected": true,
      "gas_status": "gaz detected"
    }
  }
}
The app automatically reads the latest values, detects keys by name (e.g., temperature, temp, or t), and displays them on the dashboard.

Security Notes
Do NOT commit google-services.json to a public repository. It contains your Firebase API keys.

Set proper Firebase Realtime Database Rules to prevent unauthorized access:

json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
📸 Screenshots
Login Screen	Dashboard (Live)	Dashboard (History)
(Add screenshot)	(Add screenshot)	(Add screenshot)
 Related Projects
ESP32 Firmware: See the /ESP32_Code folder in this same repository.


Author

Phroggy

GitHub: @MrPhrogger

Email: youssef.gasmi0606@gmail.com