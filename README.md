<div align="center">

<!-- Architectural Banner -->
<img src="https://capsule-render.vercel.app/api?type=rect&height=170&color=0:0F172A,50:1E293B,100:0284C7&text=CAREERFORGE&fontColor=F8FAFC&fontSize=46&fontAlignY=42&desc=PLACEMENT%20PREPARATION%20%26%20OPPORTUNITY%20MANAGEMENT%20SYSTEM&descAlignY=66&descSize=12&animation=fadeIn" width="100%" alt="CareerForge Banner"/>

<br/>

<!-- Tech & Build Badges -->
<p align="center">
  <img src="https://img.shields.io/badge/Java-21-0F172A?style=for-the-badge&logo=openjdk&logoColor=38BDF8" alt="Java 21"/>
  <img src="https://img.shields.io/badge/JavaFX-21-0F172A?style=for-the-badge&logo=java&logoColor=38BDF8" alt="JavaFX 21"/>
  <img src="https://img.shields.io/badge/MySQL-8.0+-0F172A?style=for-the-badge&logo=mysql&logoColor=38BDF8" alt="MySQL 8.0+"/>
  <img src="https://img.shields.io/badge/Maven-3.9+-0F172A?style=for-the-badge&logo=apachemaven&logoColor=38BDF8" alt="Maven"/>
  <img src="https://img.shields.io/badge/JUnit-5-0F172A?style=for-the-badge&logo=junit5&logoColor=38BDF8" alt="JUnit 5"/>
</p>

<p align="center">
  <b>A local-first JavaFX desktop suite for orchestrating placement preparation—tracking student profiles, technical skills, company pipelines, applications, and interview rounds in one structured workspace.</b>
</p>

<p align="center">
  <a href="#-getting-started"><code>Quick Start</code></a> •
  <a href="#-system-architecture"><code>Architecture</code></a> •
  <a href="#-application-workflow--domain-rules"><code>State Rules</code></a> •
  <a href="#-packaging--testing"><code>Build & Test</code></a>
</p>

</div>

---

## `01 // OVERVIEW`

**CareerForge** (`career-development-opportunity-management-system`) replaces scattered spreadsheets and bookmarks with a strongly typed, relational desktop application backed by **MySQL** via **JDBC**.

> **Scope & Design Philosophy:** CareerForge is an intentional, offline-first personal system of record. It does **not** scrape job portals, auto-submit applications, or contact recruiters—keeping your data private, deterministic, and under local database control.

<!-- Replace the placeholder below once you capture UI screenshots -->
> **UI Preview:** *Add light/dark dashboard screenshots to `docs/screenshots/` and link them here.*

---

## `02 // CORE CAPABILITIES`

<table>
  <tr>
    <td width="50%" valign="top">
      <h4>Pipeline & Opportunity Tracking</h4>
      <ul>
        <li><b>Company & Role Watchlist:</b> Track openings with deadlines, source URLs, minimum CGPA criteria, and required skills.</li>
        <li><b>Controlled Application Lifecycle:</b> Enforced state transitions, duplicate-application prevention, and a dedicated inspection drawer.</li>
        <li><b>Interview Round Logs:</b> Record round names, timestamps, interview modes, outcomes, and interviewer feedback.</li>
      </ul>
    </td>
    <td width="50%" valign="top">
      <h4>Analytics, UX & Reporting</h4>
      <ul>
        <li><b>Live Telemetry Dashboard:</b> Real-time KPI counters, status distribution charts, skill progress, and a 7-day urgency feed.</li>
        <li><b>Smart Bell Reminders:</b> In-app notifications for upcoming interviews and closing deadlines with one-click navigation.</li>
        <li><b>Theme & Accessibility Engine:</b> Instant Light/Dark mode persistence, live list filtering, Reduce Motion mode, and Excel-ready CSV exports.</li>
      </ul>
    </td>
  </tr>
</table>

---

## `03 // SYSTEM ARCHITECTURE`

CareerForge enforces a strict layered architecture separating UI rendering, business invariants, and SQL persistence. Every database query executes asynchronously via **`FxTaskRunner`**, marshaling results back to the JavaFX Application Thread so the UI never freezes.

```mermaid
flowchart LR
    UI["FXML + CSS Views"] --> CTRL["fx.controller<br/>(UI State Only)"]
    CTRL --> SVC["service<br/>(Rules & Validation)"]
    SVC --> DAO["dao<br/>(Interfaces)"]
    JDBC["dao.jdbc<br/>(MySQL / JDBC)"] -.->|Implements| DAO
