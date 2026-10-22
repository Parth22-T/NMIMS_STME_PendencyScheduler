# NMIMS Indore STME - Dynamic Academic Pendency Scheduler

![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![Language](https://img.shields.io/badge/Language-Java%2017-orange.svg)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Room%20DB-blue.svg)
![Sync Server](https://img.shields.io/badge/Backend-Node.js%20(Zero--Dependency)-brightgreen.svg)
![Institution](https://img.shields.io/badge/Institution-NMIMS%20Indore%20STME-red.svg)
![License](https://img.shields.io/badge/License-MIT-purple.svg)

A production-grade native Android mobile application built with **Java** for the **School of Technology Management and Engineering (STME), NMIMS Indore Campus**. It dynamically schedules, tracks, resolves, and approves academic lecture pendencies arising from national holidays, gazetted breaks, institutional events, and faculty leaves.

---

## 🏛️ Context & Problem Addressed

In NMIMS STME, each course is governed by a mandatory curricular quota directly correlated with credit weighting:
- **1 Credit**: **15 Lectures** (Practical Laboratories, Tutorials)
- **2 Credits**: **30 Lectures** (EVS, Biology, Ethics, Life Skills, IKS)
- **3 Credits**: **45 Lectures** (Standard Core Theory)
- **4 Credits**: **60 Lectures** (Intensive Theory, Core AI, Mathematics, Big Data)
- **5 Credits**: **75 Lectures** (Capstone Projects, Industry Electives)

### The Academic Challenge
When lectures are lost due to gazetted holidays, unplanned faculty illness, or official administrative duties, departments struggle with tracking syllabus deficit and finding clash-free rooms. 

This application provides:
1. **Pre-scheduling**: Proactively reserving makeup lectures ahead of upcoming gazetted holidays (e.g. Gandhi Jayanti, Dussehra, Diwali Break).
2. **Post-scheduling**: Resolving accumulated missed lectures.
3. **Three-Tier Governance Pipeline**:
   ```
   [CR Request] ──► [Faculty Verification] ──► [Admin Clearance] ──► [Timetable & Quota Locked]
   ```
4. **Real-Time Cross-Device Synchronization**: Instant updates across Class Representatives, Faculty, and Admin on different devices via a lightweight central server.

---

## 🏗️ System Architecture

```
                                 ┌─────────────────────────────────┐
                                 │      CENTRAL SYNC SERVER        │
                                 │     (server/server.js :8080)    │
                                 │   State Persistence & Polling   │
                                 └───────────────▲─────────────────┘
                                                 │
                               HTTP REST (Pull: 2.5s Delta / Push)
                                                 │
                        ┌────────────────────────┴────────────────────────┐
                        ▼                                                 ▼
             ┌─────────────────────┐                           ┌─────────────────────┐
             │   ANDROID DEVICE    │                           │   ANDROID DEVICE    │
             │     (Client 1)      │                           │     (Client 2)      │
             └──────────┬──────────┘                           └──────────┬──────────┘
                        │                                                 │
  ┌─────────────────────┴──────────────────────┐        ┌─────────────────┴────────────────────┐
  │         App Architecture (MVVM)            │        │        App Architecture (MVVM)       │
  │                                            │        │                                      │
  │  [UI Layer: Fragments & Dialogs]           │        │  [UI Layer: Fragments & Dialogs]     │
  │     ├── CR Dashboard (Subjects & Quotas)   │        │     ├── CR Dashboard                 │
  │     ├── Master Timetable (Weekly Grid)     │        │     ├── Faculty Inbox & Leaves       │
  │     ├── Holiday Calendar (1-Tap Pre-sched) │        │     └── Admin Roster & Queue         │
  │     └── Analytics & Compliance Export      │        │                                      │
  │                                            │        │                                      │
  │  [ViewModel & Business Logic]              │        │  [ViewModel & Business Logic]        │
  │     ├── MainViewModel                      │        │     ├── MainViewModel                │
  │     └── ConflictChecker (Room/Prof/Class)  │        │     └── ConflictChecker              │
  │                                            │        │                                      │
  │  [Repository & Data Layer]                 │        │  [Repository & Data Layer]           │
  │     ├── NetworkSyncManager (OkHttp Poller) │        │     ├── NetworkSyncManager           │
  │     └── AppDatabase (Room SQLite v5)       │        │     └── AppDatabase (Room SQLite v5) │
  └────────────────────────────────────────────┘        └──────────────────────────────────────┘
```

---

## 👥 Three-Tier Role-Based Features

### 1. Class Representatives (2 Appointed CRs Per Class)
- **Class-Specific Dashboard**: Bound to official section lecture hall (e.g., `CR 302`, `CR 304`).
- **Quota Fulfillment Bars**: Live tally of Conducted vs. Rescheduled vs. Pending vs. Target (15–75).
- **Automated Holiday Pendency Alerts**: Proactive banner alerting CR of missed lectures on upcoming breaks.
- **Enhanced Scheduling Form**:
  - Interactive `DatePickerDialog` for original and proposed dates.
  - **"⚡ Auto-Suggest Best Available Free Slot"**: Scans all 247 institute slots to auto-select the earliest conflict-free period and vacant room.
  - Select submitting CR name (`CR: Aarav Sharma`, `CR: Yash Malhotra`, etc.).
  - Multi-factor conflict validator for immediate feedback.

### 2. Faculty Members
- **Multi-Class Portfolio**: Switch across all subjects taught across departments (CE, AIDS, MBA.Tech).
- **Personal Weekly Timetable**: View unified teaching roster across all classes.
- **"📅 Intimate Planned Absence / Leave"**:
  - Register leave date and reason (e.g. Medical, Academic Council Meeting, Conference).
  - Automatically calculates all lectures affected across all sections and alerts the respective CRs.
- **Approvals Inbox**: Review incoming reschedule requests.
- **Actions**: Approve with remarks or Reject with rationale.

### 3. Administration Department (Attendance & Academic Office)
- **Central Institutional Oversight**: Monitor all 18 classes across STME.
- **Final Approval Queue**: Clear faculty-endorsed requests and lock room allocations.
- **Automatic Timetable Injection**: Creates a newly scheduled slot marked with a green `[ Rescheduled ]` badge in the master class timetable.
- **Compliance & Analytics Dashboard**:
  - Institutional Quota Health (% fulfilled).
  - Departmental filters (CE, AIDS, MBA.Tech).
  - **"📋 Copy / Share Academic Audit Report"**: One-click generation of audit report for campus leadership.

---

## 🔐 Pre-Seeded Login Credentials (Admin, Faculty, CRs)

The app features a secure Login Portal with session management, role enforcement, and **1-Tap Quick Demo Autofill**:

### 🏛️ 1. Administration Department Accounts
| Role | Display Name | Official Email | Password |
|------|--------------|----------------|----------|
| **Admin** | STME Academic Administration | `admin.stme@nmims.edu` | `Admin@STME2026` |
| **Admin** | Central Attendance Monitoring Cell | `attendance.stme@nmims.edu` | `Attendance@2026` |
| **Admin** | Dr. Anshuman Jaswal (Director) | `director.stme@nmims.edu` | `Director@2026` |

### 👨‍🏫 2. Faculty Accounts (Sample Featured Faculty)
| Faculty Code | Faculty Name & Designation | Official Email | Password |
|--------------|----------------------------|----------------|----------|
| **VK** | Dr. Vikas Khare (Associate Dean & Prof) | `vikas.khare@nmims.edu` | `Faculty@VK2026` |
| **NA** | Dr. Nidhhi Asthana (Assoc Prof & TT Coord) | `nidhhi.asthana@nmims.edu` | `Faculty@NA2026` |
| **MJ** | Mr. Lokesh Gupta (Asst Prof, Physics) | `lokesh.gupta@nmims.edu` | `Faculty@MJ2026` |
| **VJ** | Dr. Vikas Jain (Assoc Prof & TT Coord) | `vikas.jain@nmims.edu` | `Faculty@VJ2026` |
| **AR** | Dr. Ankur Ratmele (Assoc Prof & TT Coord) | `ankur.ratmele@nmims.edu` | `Faculty@AR2026` |
*(All 33 faculty codes from the official timetable are seeded with pattern: `Faculty@<CODE>2026`)*

### 🎓 3. Class Representatives (2 Appointed CRs Per Class)
| Class | Appointed CR Name | Official Email | Password |
|-------|-------------------|----------------|----------|
| **B.Tech. CE SEC A SEM I** | Aarav Sharma | `aarav.sharma@nmims.edu` | `CR@CES1A` |
| **B.Tech. CE SEC A SEM I** | Diya Patel | `diya.patel@nmims.edu` | `CR@CES1A` |
| **B.Tech. AIDS SEM I SEC A** | Kabir Singh | `kabir.singh@nmims.edu` | `CR@AIDSS1A` |
| **B.Tech. AIDS SEM I SEC A** | Riya Mehra | `riya.mehra@nmims.edu` | `CR@AIDSS1A` |
| **B.Tech. CE SEC A SEM V** | Yash Malhotra | `yash.malhotra@nmims.edu` | `CR@CES5A` |
| **B.Tech. CE SEC A SEM V** | Kritika Saxena | `kritika.saxena@nmims.edu` | `CR@CES5A` |
| **MBA.Tech. CE SEM I** | Siddharth Jain | `siddharth.jain@nmims.edu` | `CR@CES12026` |
*(All 18 class sections supported with both appointed CR accounts)*

---

## 📅 Pre-Loaded Timetable & Quota Database (AY 2026-27)

Pre-seeded with **247 master slots** and **132 curriculum courses** from the official NMIMS Indore STME timetable effective 31.08.2026:

| # | Class Name | Program | Default Room | Appointed CRs |
|---|------------|---------|--------------|---------------|
| 1 | B.Tech. CE SEC A SEM I | B.Tech. CE | CR 302 | Aarav Sharma & Diya Patel |
| 2 | B.Tech. CE SEC B SEM I | B.Tech. CE | CR 303 | Rohan Gupta & Ishita Verma |
| 3 | B.Tech. AIDS SEC B SEM I | B.Tech. AIDS | CR 303 | Ananya Joshi & Varun Rao |
| 4 | B.Tech. AIDS SEM I SEC A | B.Tech. AIDS | CR 304 | Kabir Singh & Riya Mehra |
| 5 | MBA.Tech. CE SEM I | MBA.Tech | CR 306 | Siddharth Jain & Tanvi Shah |
| 6 | B.Tech. CE SEC A SEM III | B.Tech. CE | CR 307 | Aryan Kulkarni & Sneha Patil |
| 7 | B.Tech. CE SEC B SEM III | B.Tech. CE | CR 308 | Devendra Malviya & Pooja Nair |
| 8 | B.Tech. AIDS SEM III | B.Tech. AIDS | CR 309 | Aditya Chopra & Meera Deshmukh |
| 9 | MBA.Tech. CE SEM III | MBA.Tech | CR 310 | Kunal Singhania & Natasha Roy |
| 10 | B.Tech. CE SEC A SEM V | B.Tech. CE | CR 311 | Yash Malhotra & Kritika Saxena |
| 11 | B.Tech. CE SEC B SEM V | B.Tech. CE | CR 312 | Sameer Khan & Neha Trivedi |
| 12 | MBA.Tech. CE SEM V | MBA.Tech | CR 315 | Harshwardhan Goel & Simran Kaur |
| 13 | B.Tech. AIDS SEC A SEM V | B.Tech. AIDS | CR 313 | Pranav Bansal & Shruti Agarwal |
| 14 | B.Tech. AIDS SEC B SEM V | B.Tech. AIDS | CR 314 | Nikhil Sen & Ritika Sen |
| 15 | B.Tech. CE SEM VII Sec A | B.Tech. CE | CR 316 | Tushar Mehta & Avantika Rao |
| 16 | B.Tech. CE SEM VII Sec B | B.Tech. CE | CR 317 | Gautam Dave & Shraddha Solanki |
| 17 | MBA.Tech. CE SEM VII | MBA.Tech | CR 413 | Aman Kapoor & Shalini Vyas |
| 18 | B.Tech. AIDS SEM VII | B.Tech. AIDS | CR 301 | Chirag Lodha & Payal Choudhary |

**Specialized Laboratories & Halls Supported**: `CR 301`–`CR 413`, `CC 201`, `CC 202`, `CC 401`, `CC 402`, `CL 201`–`CL 206`, `EI lab`, `BEE lab`, `LL 201`, `TUT 202`.

---

## ⚡ Conflict & Error Check Matrix

`ConflictChecker.java` runs a 6-factor deterministic check before submitting or approving any slot:

| Conflict Rule | Description | Error Message |
|---------------|-------------|---------------|
| **1. Sunday Prohibition** | Rescheduling on Sundays is strictly forbidden | `⚠️ Weekend Restriction: Rescheduling on Sunday is not permitted.` |
| **2. National / Gazetted Holiday** | Checks against 15 pre-loaded institutional holidays | `⚠️ Official Holiday Clash: Proposed date is [Holiday Name]. Institute is closed.` |
| **3. Faculty Leave Absence** | Checks against real-time faculty leave intimations | `⚠️ Faculty Unavailable: Faculty [Code] has registered leave on [Date] ([Reason]).` |
| **4. Class Timetable Clash** | Checks if the class already has a slot in that period | `⚠️ Class Schedule Conflict: Class has '[Subject]' already scheduled in slot [Time] on [Day].` |
| **5. Faculty Cross-Teaching Clash** | Ensures faculty isn't teaching another section | `⚠️ Faculty Teaching Conflict: Faculty [Code] is already taking '[Subject]' with [Class] in [Room].` |
| **6. Room Double-Booking** | Ensures no room or lab is occupied by another course | `⚠️ Room Double-Booking: Room [Room] is already occupied by [Class] for '[Subject]'.` |

---

## 🚀 How to Run

For complete step-by-step instructions, see **[SETUP_AND_RUN.md](SETUP_AND_RUN.md)**.

### Quick Start (Terminal):

```powershell
# 1. Start the zero-dependency central sync server
cd server
node server.js

# 2. In another terminal, build & run the Android application
cd ..
.\gradlew.bat installDebug

# 3. Launch on emulator or connected phone
adb shell am start -n com.nmims.stme.pendency/.ui.MainActivity
```

---

## 🐙 Step-by-Step GitHub Repository Creation

To publish this project to GitHub under your account:

### 1. Initialize Local Git Repository
Open PowerShell or Terminal inside the project folder:
```bash
cd C:\Users\parth\.gemini\antigravity\scratch\NMIMS_STME_PendencyScheduler
git init
```

### 2. Stage and Commit Files
```bash
git add .
git commit -m "feat: initial commit of NMIMS STME Dynamic Academic Pendency Scheduler"
```

### 3. Create a New Repository on GitHub
1. Go to [https://github.com/new](https://github.com/new).
2. Repository name: `NMIMS_STME_PendencyScheduler`
3. Description: `Dynamic Academic Pendency Scheduler for NMIMS Indore STME with Real-Time Multi-Device Sync`
4. Set visibility to **Public** (or **Private**).
5. Leave "Add a README file", ".gitignore", and "license" **unchecked** (we already created them).
6. Click **Create repository**.

### 4. Link Remote and Push
```bash
# Rename default branch to main
git branch -M main

# Add your GitHub remote repository (replace <your-username> with your GitHub username)
git remote add origin https://github.com/<your-username>/NMIMS_STME_PendencyScheduler.git

# Push code to GitHub
git push -u origin main
```

---

## 📂 Project Structure

```text
NMIMS_STME_PendencyScheduler/
├── .gitignore                     # Git ignore rules for Android Studio & Node.js
├── LICENSE                        # MIT License
├── README.md                      # Project documentation
├── SETUP_AND_RUN.md               # Detailed setup & multi-device networking guide
├── CONTRIBUTING.md                # Contribution guidelines
├── build.gradle                   # Root Gradle build script
├── settings.gradle                # Gradle module settings
├── gradle.properties              # JVM & Android build properties
├── server/                        # Central Multi-Device Synchronization Server
│   ├── server.js                  # Zero-dependency Node.js REST server
│   └── server_state.json          # Persistent state file (requests, leaves, slots)
└── app/
    ├── build.gradle               # App-level dependencies (Room, Retrofit/OkHttp, Material)
    └── src/
        ├── test/                  # Unit tests (Room, ConflictChecker, Quota logic)
        └── main/
            ├── AndroidManifest.xml
            ├── java/com/nmims/stme/pendency/
            │   ├── data/
            │   │   ├── AppDatabase.java          # Room Database (v5)
            │   │   ├── DatabaseInitializer.java  # 247 timetable slots & 132 subjects
            │   │   ├── dao/                      # Room DAOs (Class, Subject, Request, Slot, Leave)
            │   │   └── entity/                   # Room Entities
            │   ├── sync/
            │   │   └── NetworkSyncManager.java   # Real-time background delta sync engine
            │   ├── util/
            │   │   └── ConflictChecker.java      # 6-factor deterministic conflict validator
            │   └── ui/
            │       ├── MainActivity.java         # Master navigation & role switcher
            │       ├── MainViewModel.java        # LiveData state coordinator
            │       ├── ServerSettingsDialog.java # Real-time latency test & IP configuration
            │       ├── cr/                       # Class Representative Screens & Dialogs
            │       ├── faculty/                  # Faculty Screens & Leave Intimations
            │       ├── admin/                    # Administration Clearance & Approval Queues
            │       ├── timetable/                # Master Timetable Grid
            │       ├── holidays/                 # Academic Holiday Calendar
            │       ├── analytics/                # Quota Health & Compliance Export
            │       └── adapter/                  # Material RecyclerView Adapters
            └── res/
                ├── layout/                       # Clean XML layouts & dialog designs
                ├── values/                       # Colors (NMIMS Crimson, Navy), styles, strings
                └── drawable/                     # Material vector badges & background cards
```

---

## 📜 License
This project is licensed under the [MIT License](LICENSE). Built for NMIMS Indore STME Academic Administration & Attendance Monitoring Cell.
