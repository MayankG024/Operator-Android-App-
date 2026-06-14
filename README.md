# Operator — AI-Powered Execution Coach

> **Plan. Focus. Execute. Reflect. Progress.**

Operator is an AI-powered Android productivity app that acts as your personal execution coach. It transforms vague goals into structured daily missions, guides you through deep focus sessions with a live AI coach, and helps you reflect and improve through evening debriefs — all gamified with XP, streaks, skill trees, and achievements.

---

## ✨ Features

### 🏠 Home — Command Center
- AI-generated **Daily Brief**: a tactical < 150-word coaching message summarizing your day's primary mission, focus order, and predicted blockers
- Live XP bar, current level, and all three streak counters (Planning / Execution / Reflection)
- Quick overview of today's missions and their statuses

### 📝 Plan — Night Planning
- Enter raw, unstructured goals in plain text
- Gemini AI converts them into **structured missions** with:
  - Refined outcome-based titles
  - Success criteria
  - Actionable subtasks
  - Time estimates and difficulty ratings (Easy / Medium / Hard / Epic)
  - Predicted blockers and recommended resources
  - Skill category classification (Development, Learning, Career, Business, Fitness, Writing)
- Supports **Boss Missions** — multi-day epic projects broken into milestones

### ⏱️ Focus — Deep Work Mode
- Select a mission and start a **focus timer**
- Tick off subtasks as you go
- Track interruptions
- Real-time **AI Execution Coach** chat — ask for help, get instantly unblocked with punchy, action-oriented advice
- Report progress status: On Track / Delayed / Blocked / Complete
- Blocker reporting triggers automatic AI recovery advice

### 📋 Debrief — Evening Reflection
- Log your energy level (1–10) and blocker reasons
- Gemini AI generates a structured debrief:
  - Day summary
  - Lessons learned
  - Recommended adjustments for tomorrow
- Updates your Reflection streak

### 🏆 Progress — Skill Tree & Achievements
- **Skill Tree**: 6 independent skill categories, each with their own XP and level
- **Achievements**: Unlock milestones like "First Mission Complete", "7-Day Streak", "Deep Work Specialist", and "Giant Slayer"
- **XP History**: Full log of every XP event
- **AI-generated Insights**: Personalized productivity patterns based on your historical data (e.g. "Development tasks are completed 90% more often before noon")
- Boss Mission progress tracker

---

## 🤖 AI Capabilities (Gemini API)

All AI features call the **Gemini REST API** directly via OkHttp, returning structured JSON parsed with Moshi:

| Feature | Description |
|---------|-------------|
| Goal Enhancement | Converts raw text goals → structured missions with subtasks |
| Daily Brief | Generates a tactical morning briefing for your plan |
| Live Coach | Context-aware coaching during Focus Mode (knows your mission, subtask, and past blockers) |
| Evening Debrief | Analyses the day and provides lessons + tomorrow's adjustments |
| Productivity Insights | Mines historical mission/session data for personalized patterns |

---

## 🎮 Gamification System

| Element | Details |
|---------|---------|
| **Mission XP** | Easy: 25 XP · Medium: 50 XP · Hard: 100 XP · Epic: 250 XP |
| **Focus XP** | 1 XP per minute of deep focus (capped at 50 XP per session) |
| **Leveling** | Progressive: each level requires `level × 100` XP |
| **Skill Levels** | Independent XP per category: `level = 1 + (xp / 150)` |
| **Streaks** | Planning, Execution, Reflection — all with 1-day grace protection |
| **Achievements** | First Mission, 7-Day Streak, 10 Hours Focus, First Boss Mission |
| **Boss Bonus** | Clearing all daily missions awards a +75 XP "Swarm Cleared" bonus |

---

## 🛠️ Tech Stack

| Technology | Usage |
|-----------|-------|
| **Kotlin** | Primary language |
| **Jetpack Compose** | Declarative UI (Material 3) |
| **Room** | Local SQLite persistence (9 tables) |
| **OkHttp** | HTTP client for Gemini REST API |
| **Moshi** | JSON serialization/deserialization |
| **Kotlin Coroutines + Flow** | Async state management |
| **AndroidViewModel** | State ownership (MVVM) |
| **Secrets Gradle Plugin** | Injects `GEMINI_API_KEY` from `.env` at build time |
| **Roborazzi** | Screenshot/regression tests |

---

## 🗄️ Database Schema (Room)

| Table | Description |
|-------|-------------|
| `users` | Single user profile: level, XP, streaks |
| `missions` | Daily task records with status, difficulty, category |
| `subtasks` | Checklist items per mission |
| `focus_sessions` | Timer logs per mission: duration, interruptions |
| `reviews` | Evening debrief records |
| `xp_events` | Full XP ledger |
| `skills` | Per-category XP and level |
| `achievements` | Achievement definitions and unlock status |
| `boss_missions` | Multi-day epic projects with progress tracking |
| `insights` | AI-generated productivity insight records |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────┐
│              UI Layer (Compose)              │
│  HomeScreen · PlanScreen · FocusScreen      │
│  DebriefScreen · ProgressScreen             │
└─────────────────┬───────────────────────────┘
                  │ StateFlow / collectAsState
┌─────────────────▼───────────────────────────┐
│          OperatorViewModel (MVVM)            │
│  Navigation · Loading · UI state            │
└──────────┬──────────────────────────────────┘
           │
┌──────────▼──────────────────────────────────┐
│           OperatorRepository                 │
│  Business logic · Gamification · Streaks    │
└──────────┬──────────────────┬───────────────┘
           │                  │
┌──────────▼──────┐  ┌────────▼──────────────┐
│   Room (DAO)    │  │    GeminiService       │
│  Local SQLite   │  │  Gemini REST API       │
└─────────────────┘  └───────────────────────┘
```

---

## 🚀 Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (latest stable)
- A [Gemini API Key](https://aistudio.google.com/app/apikey)
- Android device or emulator (API 24+)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/MayankG024/Operator-Android-App-.git
   cd Operator-Android-App-
   ```

2. **Open in Android Studio**
   Select **File → Open** and choose the project directory.

3. **Configure your API key**
   Create a `.env` file in the project root (see `.env.example`):
   ```env
   GEMINI_API_KEY=your_api_key_here
   ```

4. **Fix the signing config** *(local debug builds only)*
   Remove this line from `app/build.gradle.kts`:
   ```kotlin
   signingConfig = signingConfigs.getByName("debugConfig")
   ```

5. **Run the app**
   Select your target device and click **Run ▶**.

---

## 📂 Project Structure

```
operator/
├── app/
│   └── src/main/java/com/example/
│       ├── MainActivity.kt              # Entry point, bottom nav
│       ├── data/
│       │   ├── database/
│       │   │   ├── AppDatabase.kt       # Room database
│       │   │   ├── Entities.kt          # 9 Room entity data classes
│       │   │   └── OperatorDao.kt       # All DAO queries
│       │   ├── network/
│       │   │   └── GeminiService.kt     # Gemini REST API client
│       │   └── repository/
│       │       └── OperatorRepository.kt # Business logic & gamification
│       └── ui/
│           ├── screens/
│           │   ├── HomeScreen.kt
│           │   ├── PlanScreen.kt
│           │   ├── FocusScreen.kt
│           │   ├── DebriefScreen.kt
│           │   └── ProgressScreen.kt
│           ├── theme/                   # Material 3 theme, colors
│           └── viewmodel/
│               └── OperatorViewModel.kt
├── .env.example                         # API key template
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 📄 License

This project is open-source. Feel free to fork, extend, and build upon it.
