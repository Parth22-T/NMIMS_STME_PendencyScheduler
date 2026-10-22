# Setup & Execution Guide
## NMIMS STME Dynamic Academic Pendency Scheduler

This guide provides end-to-end instructions for running the application, setting up the multi-device real-time sync server, and connecting multiple client devices (Emulators and Physical Android Phones) simultaneously.

---

## 📋 Prerequisites

| Component | Minimum Version | Recommended |
|-----------|----------------|-------------|
| **Operating System** | Windows 10/11, macOS, Linux | Windows 11 / macOS Sonoma |
| **Java Development Kit (JDK)** | OpenJDK 17 | OpenJDK 17 (Temurin / Zulu) |
| **Android Studio** | Hedgehog (2023.1.1)+ | Ladybug (2024.2)+ |
| **Android SDK** | API Level 26 (Android 8.0) | API Level 34/35 (Android 14/15) |
| **Node.js** (for Central Sync Server) | Node.js v18.0.0+ | Node.js v20+ / v22+ (LTS) |

---

## 🚀 Quick Start (Local Setup)

### Step 1: Start the Central Synchronization Server

The central REST server enables real-time synchronization between Class Representatives, Faculty, and the Administration Department.

1. Open a terminal or PowerShell prompt.
2. Navigate to the `server/` directory:
   ```bash
   cd server
   ```
3. Start the server (pure Node.js with zero external `npm` dependencies):
   ```bash
   node server.js
   ```
4. Output will display:
   ```text
   ================================================================
     NMIMS INDORE • STME ACADEMIC PENDENCY SCHEDULER
     Multi-Device Real-Time Synchronization Server
   ================================================================
     ✓ Server listening on PORT : 8080
     ✓ Android Emulator Address : http://10.0.2.2:8080
     ✓ Local Wi-Fi Addresses for Physical Devices:
         -> http://192.168.1.15:8080 (Example)
   ================================================================
   ```
> Keep this terminal window open in the background.

---

### Step 2: Open and Run the Android App in Android Studio

1. Launch **Android Studio**.
2. Select **File &rarr; Open...** and select the root directory:
   ```text
   NMIMS_STME_PendencyScheduler/
   ```
3. Wait for Gradle sync to complete successfully.
4. Select your target device from the device dropdown:
   - **Android Emulator** (e.g. `Medium_Phone` running API 34/35)
   - OR **Physical Android Device** connected via USB / Wi-Fi Debugging.
5. Click **Run** (`Shift + F10` or the green play button).

---

### Step 3: Run via Command Line (Alternative to Android Studio)

If you prefer building and deploying directly from PowerShell / Terminal:

```powershell
# Assemble and run unit test suite
.\gradlew.bat testDebugUnitTest

# Build and install onto connected emulator or device
.\gradlew.bat installDebug

# Launch the app activity
adb shell am start -n com.nmims.stme.pendency/.ui.MainActivity
```

---

## 📱 Multi-Device Simultaneous Setup

The application supports simultaneous multi-user collaboration across phones on the same Wi-Fi network.

```
                    ┌───────────────────────────────────────────────┐
                    │      Central Synchronization Server           │
                    │         http://192.168.1.15:8080              │
                    └───────────────────────┬───────────────────────┘
                                            │
               ┌────────────────────────────┼────────────────────────────┐
               │                            │                            │
               ▼                            ▼                            ▼
   ┌───────────────────────┐    ┌───────────────────────┐    ┌───────────────────────┐
   │    Device 1 (CR)      │    │  Device 2 (Faculty)   │    │   Device 3 (Admin)    │
   │ Physical Phone / AVD  │    │  Physical Phone / AVD │    │ Physical Phone / AVD  │
   │ Submits Reschedule Req│    │ Endorses / intimates  │    │ Clears Room & Locks   │
   └───────────────────────┘    └───────────────────────┘    └───────────────────────┘
```

### Configuring Physical Devices to Connect to the Server:

1. Connect your computer and your Android phones to the **same Wi-Fi network** (or a phone's mobile hotspot).
2. Note your computer's local Wi-Fi IP address printed by `server.js` (e.g. `http://192.168.1.15:8080`).
3. On the physical Android phone:
   - Open the **NMIMS STME Pendency** app.
   - In the top header bar, tap on the **`[ 🟢 LINKED ]`** or **`[ ⚠️ OFFLINE ]`** badge.
   - The **Server Synchronization Settings** dialog will appear.
   - In the **Server URL** field, enter your computer's IP:
     ```text
     http://192.168.1.15:8080
     ```
   - Tap **TEST PING**: The indicator will display `✓ Connected: AY 2026-27 (Latency: 12ms)`.
   - Tap **SAVE & SYNC NOW**.
4. Both devices are now fully linked in real time! Any action taken by a CR immediately updates the Faculty and Admin dashboards across all devices.

---

## 🧪 Testing the Complete Workflow

To verify the three-tier workflow with authentication on your device:

1. **Sign In as Class Representative (CR)**:
   - On the Login Screen, tap **`CR (CE S1 A)`** under *1-Tap Quick Demo Credentials* (or type `aarav.sharma@nmims.edu` / `CR@CES1A`).
   - Tap **Sign In to Academic Scheduler**.
   - Notice the header shows: `👤 Aarav Sharma (CR) (Class Representative)`.
   - Tap `Reschedule Lecture` (+).
   - Select subject `Calculus Theory`.
   - Tap `⚡ Auto-Suggest Best Available Free Slot` &rarr; picks clash-free time and room.
   - Tap `Submit Reschedule Request to Faculty`.
   - Tap **Sign Out** in the top header.

2. **Sign In as Faculty (Dr. Vikas Khare)**:
   - On the Login Screen, tap **`DR. VIKAS KHARE`** (or type `vikas.khare@nmims.edu` / `Faculty@VK2026`).
   - Tap **Sign In to Academic Scheduler**.
   - Notice the header shows: `👤 Dr. Vikas Khare (Faculty Member)`.
   - View your courses, weekly teaching timetable, or tap `📅 Intimate Planned Absence / Leave`.
   - Tap **Sign Out**.

3. **Sign In as Administration Department (Admin)**:
   - On the Login Screen, tap **`ADMIN OFFICE`** (or type `admin.stme@nmims.edu` / `Admin@STME2026`).
   - Tap **Sign In to Academic Scheduler**.
   - Notice the header shows: `👤 STME Academic Administration (Admin Dept)`.
   - View `Final Admin Approval Queue`, approve/reject requests with institutional remarks, or monitor syllabus compliance in the **Analytics Tab**.

4. **Faculty Leave Conflict Check**:
   - Under Faculty role, tap `📅 Intimate Planned Absence / Leave`.
   - File leave for a future date (e.g. `2026-10-27`).
   - Switch back to CR role & try to reschedule a lecture on that date &rarr; `ConflictChecker` blocks it automatically:
     `⚠️ Faculty Unavailable: Faculty has registered leave on 2026-10-27`.

---

## ❓ Troubleshooting

| Issue | Cause | Resolution |
|-------|-------|------------|
| Status shows `OFFLINE` on Android Emulator | Default URL not set | Tap the status badge and ensure URL is `http://10.0.2.2:8080`. |
| Status shows `OFFLINE` on physical phone | Firewall blocking port 8080 or different Wi-Fi | Ensure computer and phone are on the same Wi-Fi. In Windows Defender Firewall, allow port `8080` inbound. |
| Gradle build fails with OutOfMemory | Insufficient heap | In `gradle.properties`, set `org.gradle.jvmargs=-Xmx4096m`. |
| `adb: command not found` | Android SDK platform-tools not in PATH | Add `$env:LOCALAPPDATA\Android\Sdk\platform-tools` to your system PATH. |
