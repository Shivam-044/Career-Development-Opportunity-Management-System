# CareerForge

Project name: `career-development-opportunity-management-system`

CareerForge is a JavaFX desktop application for keeping placement preparation in one place: your profile, skills, the companies and roles you are watching, applications, and interview rounds. Data is stored in a local MySQL database through JDBC.

It records and organises information. It does not scrape job portals, submit applications, or contact recruiters.

## Features

- One student profile with academic details, validated on save
- Skill tracker with proficiency and learning status
- Companies and opportunities, with deadlines, source links, minimum CGPA and required skills
- Application tracker with a controlled status workflow, duplicate protection and a details panel
- Interview rounds with date and time, mode, result and feedback
- Dashboard with counts, status chart, skill progress, upcoming interviews and deadlines closing within seven days
- Reminders behind the bell icon for interviews and deadlines in the next week
- Light and dark themes that switch instantly and are remembered
- Search and filters on the list pages
- Reports with charts and CSV export of applications and interviews, readable in Excel

## Technology

| Area | Choice |
| --- | --- |
| Language | Java 21 |
| Interface | JavaFX 21 (FXML and CSS) |
| Database | MySQL 8.0.16 or newer |
| Data access | JDBC with MySQL Connector/J |
| Build | Maven |
| Tests | JUnit 5 |

The earlier Swing interface is still in the project as a fallback (see below).

## Architecture

```
FXML + CSS  ->  fx.controller  ->  service  ->  dao (interfaces)  <-  dao.jdbc (MySQL)
```

| Package | Responsibility |
| --- | --- |
| `fx` | Application entry point and the launcher used by packaged builds |
| `fx.controller` | One controller per page, plus the dialog controllers. No SQL, no business rules. |
| `fx.components`, `fx.support`, `fx.navigation` | Reusable components, themes, animations, dialogs, toasts, background tasks and navigation |
| `service` | Validation, duplicate checks, status transitions, dashboard metrics. No UI types. |
| `dao`, `dao.jdbc` | DAO interfaces and their JDBC implementations. All SQL lives here. |
| `model`, `enums` | Domain classes with private fields and validating setters |
| `export` | `DataExporter` interface and the CSV implementations |
| `config` | Database settings, connection factory and wiring of the object graph |
| `ui`, `controller`, `Main` | The previous Swing interface, kept as a fallback |

Every database call runs on a background thread (`FxTaskRunner`), and results are applied on the JavaFX thread, so the window stays responsive. How the migration was done, and how it was checked, is in [`docs/javafx-migration.md`](docs/javafx-migration.md). Diagrams for the class model and database are in [`docs`](docs).

## Application status rules

```
Applied -> Assessment -> Interview -> Offer
   \            \            \
    +------------+------------+---> Rejected / Withdrawn
```

Applied can also jump straight to Interview, Offer, Rejected or Withdrawn, and Assessment can skip to Offer. Offer, Rejected and Withdrawn are final. If a status was entered by mistake, use **Correct status** on the Applications page. It is a deliberate override and skips these rules.

Other rules worth knowing:

- You can only apply to an open listing whose deadline has not passed.
- Saving an open listing with a past deadline asks whether to keep it open or mark it closed.
- Interviews can only be added to applications that are still in progress.
- Archived opportunities and companies stay in the database, so application history is never lost.

## Getting started

### Prerequisites

- JDK 21 (`java -version` and `javac -version` should both report 21)
- Maven 3.9 or newer (`mvn -v`)
- MySQL Server 8.0.16 or newer, running on your machine
- MySQL Workbench is optional but convenient for running the scripts

JavaFX itself is downloaded by Maven. Nothing to install or copy by hand.

### 1. Create the database

Run these scripts in order as a MySQL administrator (for example the `root` account in MySQL Workbench):

1. `database/schema.sql` creates the `student_placement_tracker` database and its tables. It is safe to run again.
2. `database/setup-user.sql` creates the `tracker_app` account the application uses. Replace `CHANGE_ME` with a password of your own first.
3. `database/sample-data.sql` is optional. It adds synthetic demo records, including a demo profile. Skip it if you want to start empty.

The application connects as `tracker_app`, which can read and write this database only. It never uses `root`.

### 2. Configure the connection

Give the application the account details in either of these ways. Environment variables take precedence.

**Option A: settings file.** Copy `config/application.properties.example` to `config/application.properties` and fill in the password. The copy is ignored by Git. For an installed copy of the app, put the same file at `%USERPROFILE%\.careerforge\application.properties`.

**Option B: environment variables.** In PowerShell:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/student_placement_tracker"
$env:DB_USER = "tracker_app"
$env:DB_PASSWORD = "your-password"
```

`DB_URL` is optional and defaults to the local server on port 3306. The settings file path can be changed with `TRACKER_CONFIG`. **Settings > Test connection** inside the app checks that everything is wired up.

### 3. Run

From the project folder:

```powershell
mvn clean compile
mvn javafx:run
```

To build a single runnable JAR instead:

```powershell
mvn clean package
java -jar target/career-development-opportunity-management-system-1.0.0.jar
```

The JAR contains JavaFX for the operating system it was built on, so build it on the machine (or OS) you will run it on. Run it from the project folder so it finds `config/application.properties`, or use environment variables. It does not contain the database.

### Windows app and installer

```powershell
.\scripts\package-windows.ps1              # a folder with CareerForge.exe, no extra tools
.\scripts\package-windows.ps1 -Type exe    # an installer; needs WiX Toolkit 3.x on PATH
```

The result is written to `dist/`.

### Swing fallback

```powershell
mvn exec:java "-Dexec.mainClass=com.studenttracker.Main"
```

### Tests

```powershell
mvn test
```

The tests cover validation, status transitions, the service rules and dashboard counts (using in-memory DAOs), CSV output, configuration loading, user-facing error text, reminder building, and the contract between each FXML file and its controller. They do not need MySQL and do not start the JavaFX toolkit.

## Using the app

- **Theme:** the sun or moon button in the header, or Settings. The choice is remembered.
- **Search:** the header search box filters the list on the current page. It is hidden on pages with nothing to search.
- **Reminders:** the bell lists interviews and deadlines in the next 7 days. Click one to jump to it, or dismiss it. They only appear while the app is open.
- **Reduce motion:** Settings > Accessibility turns off page transitions and animated numbers.

## Project layout

```
career-development-opportunity-management-system/
  pom.xml
  config/            settings template (the real file is git-ignored)
  database/          schema.sql, setup-user.sql, sample-data.sql
  docs/              diagrams, JavaFX migration notes, planned features
  scripts/           package-windows.ps1 and the app icon
  src/main/java/com/studenttracker/
    fx/ config/ dao/ dao/jdbc/ enums/ exception/ export/ model/ service/ util/
    ui/ controller/ Main.java            (Swing fallback)
  src/main/resources/com/studenttracker/fx/
    view/ view/dialogs/ css/ assets/
  src/test/java/com/studenttracker/
```

## Known limitations

- Single user and a single profile, by design. There is no login; see [`docs/future-features.md`](docs/future-features.md) for how one would be added.
- Anyone with access to the computer and the database account can open the data.
- Reminders are derived from interviews and deadlines and only appear while the app is running. Custom reminders are designed but not built.
- Connections are opened per operation, which is fine at this scale. A pool would be the next step for heavier use.
- Skills on an opportunity are entered as a comma-separated list. A match score against your own skills is not implemented.
- The stored interview round is a name (such as "Technical round 1"), not a number.

<!-- Screenshots: add images to docs/screenshots and link them here. -->

## Author

Shivam Kumar, course and college go here.
