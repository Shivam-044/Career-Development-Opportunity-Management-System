# CareerForge
### Career Development & Opportunity Management System

**Take control of your career journey. Track your skills, explore opportunities, manage applications, and stay prepared for every interview — all from one desktop application.**

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge" alt="JavaFX 21">
  <img src="https://img.shields.io/badge/MySQL-8.0+-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL 8">
  <img src="https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven" alt="Maven">
  <img src="https://img.shields.io/badge/JUnit-5-green?style=for-the-badge&logo=junit5" alt="JUnit 5">
</p>

<p align="center">
  <a href="https://github.com/Shivam-044/Career-Development-Opportunity-Management-System">
    <strong>View Repository</strong>
  </a>
</p>

---

## 📌 Overview

**CareerForge** is a JavaFX-based desktop application designed to simplify and organise the placement preparation journey.

Instead of managing career information across spreadsheets, notes, and separate applications, CareerForge brings the essentials together in one centralised workspace.

From maintaining your student profile and tracking technical skills to monitoring company opportunities, applications, and interview rounds, the application helps you stay organised throughout your job preparation journey.

The application uses **Java 21, JavaFX, JDBC, and MySQL** with a layered architecture that separates the user interface, business logic, and database operations.

> **Important:** CareerForge is an information management tool. It does not scrape job portals, automatically submit applications, or contact recruiters.

---

## ✨ Key Features

| Feature | Description |
|---|---|
| 👤 Student Profile | Maintain academic information and personal details with validation. |
| 🧠 Skill Tracker | Record technical skills, proficiency levels, and learning status. |
| 🏢 Company Management | Maintain a structured list of companies you are targeting. |
| 🎯 Opportunity Management | Track job roles, eligibility criteria, required skills, source links, and application deadlines. |
| 📋 Application Tracker | Manage applications through a controlled status workflow, with duplicate protection and detailed application views. |
| 🎤 Interview Management | Record interview rounds, schedules, modes, results, and feedback. |
| 📊 Interactive Dashboard | View application statistics, status distribution, skill progress, upcoming interviews, and approaching deadlines. |
| 🔔 Smart Reminders | Get reminders for interviews and deadlines occurring within the next seven days. |
| 🌗 Dual Themes | Switch instantly between light and dark themes, with preferences remembered. |
| 🔎 Search & Filters | Find and organise records using search and filtering options. |
| 📈 Reports & Analytics | Visualise application and interview information through charts. |
| 📤 CSV Export | Export application and interview records into CSV files compatible with Microsoft Excel. |
| ♿ Accessibility | Enable reduced motion to disable page transitions and animated numbers. |

---

## 🛠️ Technology Stack

CareerForge is built using a Java-based desktop architecture.

| Layer | Technology |
|---|---|
| Programming Language | Java 21 |
| Desktop Interface | JavaFX 21 |
| UI Design | FXML and CSS |
| Database | MySQL 8.0.16+ |
| Database Connectivity | JDBC |
| JDBC Driver | MySQL Connector/J |
| Build & Dependency Management | Maven |
| Unit Testing | JUnit 5 |
| Data Export | CSV |

---

## 🏗️ Application Architecture

CareerForge follows a layered architecture to maintain separation of concerns, improve maintainability, and simplify future development.

```text
              JavaFX Interface
               (FXML + CSS)
                    |
                    ▼
             FX Controllers
                    |
                    ▼
               Services
          (Business Logic)
                    |
                    ▼
              DAO Interfaces
                    |
                    ▼
             JDBC DAO Layer
                    |
                    ▼
                 MySQL
```

### Package Responsibilities

| Package | Responsibility |
|---|---|
| `fx` | Application entry point and launcher for packaged builds. |
| `fx.controller` | Page and dialog controllers. Handles UI interactions without SQL or business rules. |
| `fx.components` | Reusable JavaFX UI components. |
| `fx.support` | Themes, animations, dialogs, toasts, background tasks, and supporting utilities. |
| `fx.navigation` | Page navigation and application flow. |
| `service` | Validation, duplicate checks, application status transitions, and dashboard metrics. |
| `dao` | DAO interfaces defining database operations. |
| `dao.jdbc` | JDBC implementations containing database queries and SQL operations. |
| `model` | Domain entities with encapsulated fields and validating setters. |
| `enums` | Application-specific enumerations. |
| `export` | Data export interfaces and CSV implementations. |
| `config` | Database configuration, connection factory, and dependency wiring. |
| `ui`, `controller`, `Main` | Previous Swing implementation retained as a fallback. |

### Non-Blocking Database Operations

Database operations are executed through a background task mechanism (`FxTaskRunner`).

Their results are then applied on the JavaFX Application Thread, keeping the interface responsive during database operations.

For details about the JavaFX migration and its verification, see [`docs/javafx-migration.md`](docs/javafx-migration.md).

Architecture diagrams and database documentation are available in the [`docs`](docs) directory.

---

## 🔄 Application Status Workflow

CareerForge uses a controlled application status workflow to maintain consistency when tracking placement applications.

```text
                    ┌─────────────┐
                    │   Applied   │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ Assessment  │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │  Interview  │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │    Offer    │
                    └─────────────┘

       Applications can also move to:
             Rejected / Withdrawn
```

### Supported Transitions

- Applied → Assessment, Interview, Offer, Rejected, or Withdrawn
- Assessment → Interview, Offer, Rejected, or Withdrawn
- Interview → Offer, Rejected, or Withdrawn

**Offer, Rejected, and Withdrawn are final states.**

For accidental status changes, the Applications page provides a **Correct Status** option. This is an intentional override that bypasses the normal transition rules.

### Additional Business Rules

- Applications can only be created for open opportunities whose deadlines have not passed.
- Saving an open opportunity with an expired deadline prompts the user to keep it open or mark it closed.
- Interviews can only be added to applications that are still in progress.
- Archived companies and opportunities remain in the database, preserving historical application records.

---

## 🚀 Getting Started

Follow these instructions to run CareerForge locally.

### Prerequisites

Make sure the following software is installed:

| Requirement | Version |
|---|---|
| JDK | 21 |
| Maven | 3.9+ |
| MySQL Server | 8.0.16+ |
| MySQL Workbench | Optional |

Verify your installations:

```powershell
java -version
javac -version
mvn -v
```

Both Java commands should report version 21.

> JavaFX dependencies are downloaded automatically through Maven. No manual JavaFX installation is required.

### 1. Clone the Repository

```bash
git clone https://github.com/Shivam-044/Career-Development-Opportunity-Management-System.git
```

Navigate into the project:

```bash
cd Career-Development-Opportunity-Management-System
```

### 2. Create the Database

Open MySQL Workbench or another MySQL client and execute the following scripts in order:

| Script | Purpose |
|---|---|
| `database/schema.sql` | Creates the `student_placement_tracker` database and its tables. Safe to execute again. |
| `database/setup-user.sql` | Creates the restricted `tracker_app` database user. |
| `database/sample-data.sql` | Optional script that inserts synthetic demo records, including a demo student profile. |

**Important:** Before running `setup-user.sql`, replace `CHANGE_ME` with a password of your choice.

CareerForge connects using the `tracker_app` account, which has database access limited to reading and writing the application database. The application does not use the MySQL `root` account.

### 3. Configure Database Connectivity

You can configure the database connection using either a properties file or environment variables.

**Option A — Properties File**

1. Copy `config/application.properties.example`.
2. Rename the copied file to `application.properties`.
3. Enter your database credentials.

For an installed application, the configuration can also be placed at:

```text
%USERPROFILE%\.careerforge\application.properties
```

The actual configuration file is ignored by Git.

**Option B — Environment Variables**

In PowerShell:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/student_placement_tracker"
$env:DB_USER = "tracker_app"
$env:DB_PASSWORD = "your-password"
```

Additional configuration details:

- `DB_URL` is optional and defaults to the local MySQL server on port `3306`.
- Environment variables take precedence over the properties file.
- `TRACKER_CONFIG` can be used to change the settings file location.
- Use **Settings → Test Connection** inside the application to verify connectivity.

### 4. Run the Application

From the root project directory:

```powershell
mvn clean compile
mvn javafx:run
```

### 5. Build a Runnable JAR

```powershell
mvn clean package
```

Run the generated JAR:

```powershell
java -jar target/career-development-opportunity-management-system-1.0.0.jar
```

> **Platform note:** The JAR contains JavaFX dependencies for the operating system it was built on. Build it on the same operating system where you intend to run it. The MySQL database is not bundled with the JAR.
>
> Run it from the project directory so it can locate `config/application.properties`, or configure the required environment variables.

### 6. Create a Windows Application

CareerForge includes a Windows packaging script.

**Generate a portable application folder:**

```powershell
.\scripts\package-windows.ps1
```

**Generate a Windows installer:**

```powershell
.\scripts\package-windows.ps1 -Type exe
```

The installer option requires **WiX Toolkit 3.x** to be available on your system PATH.

Generated packages are saved in:

```text
dist/
```

### 7. Run the Legacy Swing Version

The previous Swing interface is retained as a fallback.

```powershell
mvn exec:java "-Dexec.mainClass=com.studenttracker.Main"
```

---

## 🧪 Running Tests

CareerForge uses **JUnit 5** for automated testing.

Run the complete test suite:

```powershell
mvn test
```

### Test Coverage

The tests cover:

- Input validation and business rules.
- Application status transitions.
- Service-layer functionality.
- Dashboard calculations and counts.
- CSV export functionality.
- Database configuration loading.
- User-facing error messages.
- Reminder generation.
- FXML and controller contract validation.

Tests use in-memory DAOs where applicable. They do not require a running MySQL database or initialise the JavaFX toolkit.

---

## 💻 Using CareerForge

### Themes
Switch between light and dark themes using the sun/moon button in the application header or through Settings. Your selected theme is remembered.

### Search
Use the header search field to filter records on the current page. It is automatically hidden on pages where search is not applicable.

### Reminders
The notification bell displays upcoming interviews and opportunity deadlines within the next seven days. Selecting a reminder navigates to the relevant record. Reminders can also be dismissed.

Reminders are displayed while the application is running; they are not operating-system notifications.

### Accessibility
Navigate to **Settings → Accessibility** and enable Reduce Motion to disable page transitions and animated numbers.

---

## 📁 Project Structure

```text
career-development-opportunity-management-system/
│
├── config/
│   └── application.properties.example
│
├── database/
│   ├── schema.sql
│   ├── setup-user.sql
│   └── sample-data.sql
│
├── docs/
│   ├── diagrams
│   ├── javafx-migration.md
│   └── future-features.md
│
├── scripts/
│   └── package-windows.ps1
│
├── src/
│   ├── main/
│   │   ├── java/com/studenttracker/
│   │   │   ├── fx/
│   │   │   ├── config/
│   │   │   ├── dao/
│   │   │   ├── dao/jdbc/
│   │   │   ├── enums/
│   │   │   ├── exception/
│   │   │   ├── export/
│   │   │   ├── model/
│   │   │   ├── service/
│   │   │   ├── util/
│   │   │   ├── ui/
│   │   │   ├── controller/
│   │   │   └── Main.java
│   │   │
│   │   └── resources/com/studenttracker/fx/
│   │       ├── view/
│   │       ├── view/dialogs/
│   │       ├── css/
│   │       └── assets/
│   │
│   └── test/
│       └── java/com/studenttracker/
│
├── pom.xml
└── README.md
```

---

## ⚠️ Known Limitations

CareerForge focuses on individual placement preparation and currently has the following limitations:

1. **Single-user application:** Designed for one student profile. No login or multi-user authentication is implemented.
2. **Local data access:** Anyone with access to the computer and database account can access the stored information.
3. **Reminder system:** Reminders are derived from interview schedules and opportunity deadlines. They appear only while the application is running. Custom reminders are planned but not implemented.
4. **Database connections:** Connections are opened per operation. This is sufficient for the current application scale; a connection pool would be a future improvement.
5. **Skill matching:** Required skills are entered as a comma-separated list. Automated matching and scoring against the student's skills are not implemented.
6. **Interview rounds:** Interview rounds are stored as names, such as `Technical Round 1`, rather than numeric round identifiers.

For planned enhancements and future development considerations, see [`docs/future-features.md`](docs/future-features.md).

---

## 🔮 Future Scope

The project documentation includes ideas for future development. Potential areas for expansion include:

- Multi-user authentication and role-based access.
- Automated skill-to-opportunity matching.
- Customisable reminders and notifications.
- Connection pooling for database operations.
- More advanced placement analytics.

These are possible enhancements, not features currently implemented in CareerForge.

---

## 👨‍💻 Author

**Shivam Kumar**

Java | JavaFX | MySQL | JDBC | Object-Oriented Programming

<p align="left">
  <a href="https://github.com/Shivam-044">
    <img src="https://img.shields.io/badge/GitHub-Shivam--044-181717?style=for-the-badge&logo=github" alt="GitHub Profile">
  </a>
</p>

**Repository:** [CareerForge — Career Development & Opportunity Management System](https://github.com/Shivam-044/Career-Development-Opportunity-Management-System)

---

<p align="center">
  <strong>CareerForge — Organise your preparation. Track your progress. Own your career journey.</strong>
</p>

<p align="center">
  Built with Java 21 and JavaFX.
</p>
