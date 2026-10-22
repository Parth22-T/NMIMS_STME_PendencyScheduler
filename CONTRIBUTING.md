# Contributing to NMIMS STME Dynamic Academic Pendency Scheduler

Thank you for your interest in contributing to the **NMIMS STME Dynamic Academic Pendency Scheduler**! This project was engineered for NMIMS Indore STME to automate academic pendency management, lecture quota compliance, and timetable conflict detection.

---

## Code of Conduct
We expect contributors to maintain professional, respectful, and collaborative behavior. Maintain documentation integrity and preserve institutional standards for NMIMS Indore STME.

---

## Development Workflow

### 1. Fork & Branch
- Fork the repository on GitHub.
- Clone your fork locally:
  ```bash
  git clone https://github.com/<your-username>/NMIMS_STME_PendencyScheduler.git
  cd NMIMS_STME_PendencyScheduler
  ```
- Create a feature branch:
  ```bash
  git checkout -b feature/your-feature-name
  ```

### 2. Code Standards
- **Language**: Java 17 for Android, modern JavaScript (ES6+) for the backend sync server.
- **Architecture**: MVVM (Model-View-ViewModel) with Android Jetpack Architecture Components (`Room`, `LiveData`, `ViewModel`, `Navigation`).
- **Conflict Prevention**: Any new scheduling feature must invoke `ConflictChecker.validateSlot()` before mutating room or timetable entities.
- **Quota Logic**: Strict adherence to NMIMS credit rules:
  - 1 Credit = 15 Lectures
  - 2 Credits = 30 Lectures
  - 3 Credits = 45 Lectures
  - 4 Credits = 60 Lectures
  - 5 Credits = 75 Lectures

### 3. Testing
Before submitting any changes, verify that the unit tests and lint checks pass:
```bash
./gradlew testDebugUnitTest
```

### 4. Commit Guidelines
Write clear, conventional commit messages:
- `feat(scheduler): add recurring compensatory slot selector`
- `fix(sync): resolve edge-case in leave ID parsing`
- `docs(readme): update multi-device IP setup steps`

### 5. Pull Requests
- Push your feature branch to your fork.
- Open a Pull Request targeting the `main` branch.
- Provide a clear summary of your changes, screenshots (if UI was altered), and testing evidence.
