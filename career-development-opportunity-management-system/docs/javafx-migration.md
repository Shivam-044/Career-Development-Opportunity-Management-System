# Swing to JavaFX migration

The interface moved from Swing to JavaFX (FXML + CSS). Everything below the interface is unchanged.

## What did not change

Models, enums, DAO interfaces, JDBC implementations, services, validation rules, status workflow, CSV export, exceptions,
the database and the 71 existing tests. `Services.createDefault()` still builds the single service graph, and the JavaFX
controllers call those services. No SQL exists in any JavaFX class.

The only edits outside the new `fx` package are additive: `util/ErrorMessages` (the friendly error text, shared logic with
no UI types), a per-user settings file lookup in `AppConfig` for installed copies, and the `pom.xml`.

## Layers

```
FXML + CSS  ->  fx.controller  ->  service  ->  dao  ->  dao.jdbc  ->  MySQL
```

| Package | Role |
| --- | --- |
| `fx` | `CareerForgeApplication` (entry point), `FxLauncher` (main class for packaged builds) |
| `fx.controller` | One controller per page, plus `MainShellController`. `BaseFxController` holds the shared context. |
| `fx.controller.dialog` | Controllers for the form dialogs (skill, company, opportunity, apply, status, notes, interview) |
| `fx.components` | `StatCard`, `StatusBadge`, `PageHeader` |
| `fx.navigation` | `NavigationManager`, `Route`, `PageController`, `Searchable` |
| `fx.support` | `FxContext`, `FxControllerFactory`, `FxTaskRunner`, `FxDialogService`, `ThemeManager`, `FxAnimations`, `ToastManager`, `NotificationCenter` and table/combobox helpers |

Dependencies are passed in, not reached for: `FxControllerFactory` gives every controller the same `FxContext` (services,
task runner, dialogs, toasts, theme).

## Threading

`FxTaskRunner` runs every service call on a three-thread daemon pool using JavaFX `Task`, and delivers results on the
JavaFX thread. It disables the buttons you pass it while the call runs, shows a thin progress bar after 150 ms, routes
errors to `FxDialogService.error` (friendly text from `ErrorMessages`) and shuts the pool down when the application exits.
The Swing `BackgroundTask` is not used by JavaFX code.

## Themes

`theme-dark.css` and `theme-light.css` define the same named colours on `.root`; `components.css` and `animations.css`
only use those names, so no screen is duplicated per theme. `ThemeManager` keeps every scene (the window and each dialog)
registered, swaps the stylesheet live, and stores the choice in Java Preferences.

JavaFX CSS has no backdrop blur. The glass look is a translucent gradient fill, a light gradient edge and a soft shadow
over a gradient background with three soft radial "orbs". Icons are CSS vector shapes, so they follow the theme colours.

## Swing to JavaFX map

| Swing | JavaFX |
| --- | --- |
| `MainFrame`, `NavButton` | `main-shell.fxml`, `MainShellController` |
| `DashboardPanel` | `dashboard-view.fxml`, `DashboardFxController` |
| `ProfilePanel` | `profile-view.fxml`, `ProfileFxController` |
| `SkillsPanel` | `skills-view.fxml`, `SkillsFxController` |
| `CompaniesPanel` | `companies-view.fxml`, `CompaniesFxController` |
| `OpportunitiesPanel` | `opportunities-view.fxml`, `OpportunitiesFxController` |
| `ApplicationsPanel` | `applications-view.fxml`, `ApplicationsFxController` |
| `InterviewsPanel` | `interviews-view.fxml`, `InterviewsFxController` |
| `ReportsPanel` | `reports-view.fxml`, `ReportsFxController` |
| `ui.dialogs.*` | `view/dialogs/*.fxml` and `fx.controller.dialog.*` |
| `BackgroundTask` (SwingWorker) | `FxTaskRunner` (Task) |
| `Notifier` (JOptionPane) | `FxDialogService` and `ToastManager` |

The Swing code (`ui`, `controller`, `Main`) is kept as a fallback and still compiles. Launch it with
`mvn exec:java -Dexec.mainClass=com.studenttracker.Main`. FlatLaf stays in the `pom.xml` for it. Once the Swing
fallback is retired, delete those packages and the FlatLaf dependency together.

## Behaviour notes

- Header search is shared. Pages that implement `Searchable` (Skills, Companies, Opportunities, Applications) use it;
  on other pages the search box is hidden.
- The Applications page has a details panel for the selected row, including its interview rounds.
- Interviews have a round name, not a round number, because that is what the data model stores.
- Reminders come from real data (interviews and deadlines in the next 7 days). Dismissals are remembered between
  launches. They appear only while the application is running.
- "Reduce motion" in Settings turns off transitions, hover scaling and animated counters.

## How the JavaFX layer was checked

The JavaFX libraries were not available where this layer was written, so it could not be launched or compiled against the
real API there. What was done instead:

- Compiled the whole project against a hand-written stand-in for the JavaFX classes the code uses, to catch typos, type
  errors and wrong member names in the project's own code.
- Cross-checked every FXML file against its controller (fx:id to field, handler to method, imports, style classes to CSS
  rules, palette names defined in both themes). `FxmlConsistencyTest` keeps that check in the build.
- Ran the 71 existing tests plus the new ones for `ErrorMessages`, `NotificationBuilder` and `FxFormats`.

Not verified: that the project builds with the real JavaFX 21.0.13 artifacts, that the CSS renders as intended, and
anything at runtime. Run `mvn clean test` and `mvn javafx:run` first and report any error.
