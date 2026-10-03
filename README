AGRI DETECT - SMART AGRICULTURE ENVIRONMENT MONITOR

An end-to-end IoT project for monitoring the environment around plants in
agricultural greenhouses (serres agricoles). The system has two parts: an
ESP32-based embedded device that senses temperature, humidity, and gas/air
quality, and an Android companion app that displays live data and historical
trends. It helps farmers know when to adjust temperature, humidity, or
ventilation to keep plants healthy. Data flows from sensors -> ESP32 ->
Firebase Realtime Database -> Android app.

THE WHOLE STORY

This project started as a simple gas leak detector, but it quickly grew into
something bigger: a full environment monitor for agriculture. The idea is to
place the device inside a greenhouse or near a plant, where it continuously
watches temperature, humidity, and air quality. Plants are sensitive to their
environment. If it gets too hot, too humid, or the air is not fresh enough,
the plant suffers. Agri Detect solves this by alerting the farmer when
something needs to change, whether that means opening a vent, watering more,
or checking for gas or smoke nearby.

The system has two parts. On the hardware side, an ESP32 reads an MQ5 gas
sensor and a DHT11 temperature/humidity sensor, cycles readings on a 16x2 LCD,
and pushes state changes to Firebase. On the software side, a Java-based
Android app (Agri Detect) reads that data and shows it through a clean
dashboard with live gauges, historical charts, and event logs.

The result is a working prototype of a remote greenhouse monitoring system:
the hardware shows the current status locally on the LCD, and the app lets you
monitor the same conditions from your phone, anywhere, with authentication
and history.

HOW IT WORKS

ESP32 reads the MQ5 (air quality/gas) and DHT11 (temp/humidity) every
2 seconds.

LCD cycles between two screens: air quality status and temp/humidity,
so anyone standing near the plant can see the current state.

On a state change (gas detected/cleared, or significant temp/humidity
shift), the ESP32 pushes a new entry to "air_quality_log" in Firebase
Realtime Database.

The Android app subscribes to the database, reads the latest values,
and updates the dashboard in real time.

The farmer opens the app to check the plant's environment, sees if
temperature, humidity, or air quality is out of range, and knows what
to adjust (open vents, water more, check for gas leaks, etc.).

Diagram:

ESP32 + Sensors --> Firebase Realtime DB --> Android App
(MQ5, DHT11, LCD) (air_quality_log) (Agri Detect)
in the greenhouse on the farmer's phone

PART 1 - ESP32 FIRMWARE

Hardware

ESP32 (NodeMCU-32S)

MQ5 gas sensor (analog output) - detects gas/smoke/air quality

DHT11 temperature and humidity sensor - monitors plant environment

16x2 I2C LCD (address 0x27) - local display for anyone near the plant

Wiring

MQ5 analog out -> GPIO 34

DHT11 data -> GPIO 4

LCD SDA -> GPIO 21

LCD SCL -> GPIO 22

How It Works

Every 2 seconds, the ESP32 samples the MQ5 (analog) and DHT11 (digital).

Every 3 seconds, the LCD cycles between two screens:
Air Quality: "Normal air quality" / "Gaz detected"
(threshold-based on MQ5 reading)
Temp/Humidity: live DHT11 readings

All readings are also printed to Serial (115200 baud) for debugging.

Firebase integration: the ESP32 connects to WiFi and pushes state changes
only (not raw continuous values) to Firebase Realtime Database under
"air_quality_log".

Libraries

marcoschwartz/LiquidCrystal_I2C

adafruit/DHT sensor library

adafruit/Adafruit Unified Sensor

Build & Flash
Project built and managed with PlatformIO. Open the /ESP32_Code folder in
PlatformIO, configure your WiFi/Firebase credentials (see Security Notes),
and upload to the ESP32.

PART 2 - ANDROID APP (AGRI DETECT)

An IoT companion Android application for real-time greenhouse environment
monitoring. Connects to Firebase Realtime Database to display live sensor
data sent from the ESP32.

Features

Real-Time Monitoring: live temperature, humidity, and air quality updates
from Firebase Realtime Database.

Air Quality Status: instant alert card when gas is detected
(haptic feedback + visual warning).

Custom Gauges: arc gauge for Temperature, wave orb for Humidity.

Historical Data: temperature and humidity trends with line charts, so you
can see how the plant's environment changed over time.

Time Range Filters: last 30 minutes, 6 hours, or 24 hours.

Gas Event Log: count of gas events in the selected time period.

Firebase Authentication: Email/Password, Google Sign-In, Phone (OTP).

Clean UI: modern, minimalistic "Cozy Greenery" color palette.

Tech Stack

Language: Java

IDE: Android Studio

Backend: Firebase Realtime Database

Auth: Firebase Auth (Email, Google, Phone)

UI: Material Design + Custom Views
(ArcGauge, WaveOrb, LineChart, StatusOrb, GasStrip)

Min SDK: 24 (Android 7.0)

Target SDK: 34 (Android 14)

Project Structure
Android_App/
app/
src/main/
java/com/example/agri_detect/
MainActivity.java - Main Menu (Login / Signup)
LoginActivity.java - Login logic (Email, Google, Phone)
SignupActivity.java - Signup logic
DashboardActivity.java - Live data + History
HistoryStore.java - Local storage for history
ArcGaugeView.java - Custom temperature gauge
WaveOrbView.java - Custom humidity orb
LineChartView.java - Custom line chart
StatusOrbView.java - Gas status indicator
GasStripView.java - Gas event timeline
res/ - layouts, drawables, values
google-services.json - Firebase config (DO NOT SHARE PUBLICLY)
build.gradle.kts
build.gradle.kts

Firebase Database Structure
The app expects the following structure in your Realtime Database:

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

The app automatically reads the latest values and detects keys by name
(e.g., temperature, temp, or t) and displays them on the dashboard.

WHY THIS MATTERS FOR AGRICULTURE

Plants in greenhouses (serres agricoles) need a stable environment. If the
temperature rises too high, plants wilt or stop growing. If humidity is too
low, they dry out. If humidity is too high, mold and fungus appear. If the
air is not fresh, or if gas/smoke is present, the plants (and workers) are
at risk.

Agri Detect gives the farmer a simple answer to three questions:

Is it too hot or too cold near my plants? -> Temperature gauge

Is it too dry or too humid? -> Humidity orb

Is the air fresh, or is something wrong? -> Air quality alert

With the history charts, the farmer can also see trends: did the temperature
spike last night? Did humidity drop after the vents were closed? This helps
make better decisions about ventilation, watering, and heating.

SETUP & INSTALLATION

Prerequisites

Android Studio (latest version)

A Firebase Project

The ESP32 hardware already pushing data to Firebase (see /ESP32_Code)

Clone the Repository
git clone https://github.com/MrPhrogger/Agri-detect

Set Up the ESP32

Open /ESP32_Code in PlatformIO.

Configure WiFi and Firebase credentials (see Security Notes).

Flash to your ESP32.

Verify Serial output at 115200 baud and confirm the LCD cycles correctly.

Set Up the Android App

Open the project in Android Studio:
File -> Open -> Select the Android_App folder.

Firebase Console:
Create a new project (or use an existing one).
Add an Android app with package name com.example.agri_detect.
Download google-services.json and place it in app/.

Enable Firebase Services:
Realtime Database: create in test mode (or set rules).
Authentication: enable Email/Password, Google, and Phone sign-in.

Add SHA-1 Fingerprint (required for Google Sign-In):
Android Studio -> Gradle tab -> app -> Tasks -> android -> signingReport.
Copy the SHA-1 hash.
Firebase Console -> Project Settings -> Your Android App -> Add fingerprint.
Re-download google-services.json and replace the old one.

Build and Run in Android Studio.

SECURITY NOTES

Do NOT commit google-services.json to a public repository. It contains
Firebase API keys.

Move WiFi/Firebase credentials out of hardcoded #defines in the ESP32
firmware into a gitignored config file before making the repo public.

Set proper Firebase Realtime Database Rules to prevent unauthorized access:

{
"rules": {
".read": "auth != null",
".write": "auth != null"
}
}

SCREENSHOTS

Login Screen | Dashboard (Live) | Dashboard (History)
(Add screenshot) | (Add screenshot) | (Add screenshot)

ROADMAP / NEXT STEPS

[DONE] ESP32 reads MQ5 + DHT11, displays on LCD, prints to Serial.
[DONE] Firebase integration: push state changes to Realtime Database.
[DONE] Android app: live gauges, history charts, gas event log, auth.
[TODO] Add configurable thresholds for temperature and humidity alerts
(e.g., "warn me if temp > 30C or humidity < 40%").
[TODO] Push notifications to the phone when the environment goes out of
range for the plants.
[TODO] Move WiFi/Firebase credentials to a gitignored config file.
[TODO] Harden Firebase rules and rotate any exposed keys.

RELATED PROJECTS

ESP32 Firmware: see the /ESP32_Code folder in this same repository.

Android App: see the Android_App folder.

LICENSE

This project is licensed under the MIT License.

AUTHOR

Phroggy
GitHub: @MrPhrogger
Email: youssef.gasmi0605@gmail.com


