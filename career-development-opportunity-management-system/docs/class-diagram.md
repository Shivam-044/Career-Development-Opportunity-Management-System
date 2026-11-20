# Class design

```mermaid
classDiagram
    class Student {
        -Long studentId
        -String fullName
        -BigDecimal cgpa
        +setCgpa(BigDecimal)
    }
    class StudentSkill {
        -String skillName
        -Proficiency proficiency
        -SkillStatus learningStatus
    }
    class Company {
        -String companyName
        -boolean archived
    }
    class Opportunity {
        -String roleTitle
        -OpportunityType type
        -LocalDate deadline
        -OpportunityStatus status
        +isDeadlinePassed(LocalDate) boolean
    }
    class JobApplication {
        -ApplicationStatus status
        -LocalDate appliedOn
    }
    class Interview {
        -String roundName
        -LocalDateTime scheduledAt
        -InterviewResult result
    }
    class ApplicationStatus {
        <<enumeration>>
        +nextStatuses() Set
        +canMoveTo(ApplicationStatus) boolean
    }

    Student "1" --> "*" StudentSkill
    Company "1" --> "*" Opportunity
    Student "1" --> "*" JobApplication
    Opportunity "1" --> "*" JobApplication
    JobApplication "1" --> "*" Interview
    JobApplication --> ApplicationStatus
```

## Layers and collaborators

```mermaid
classDiagram
    class ApplicationService
    class ApplicationDAO {
        <<interface>>
    }
    class JdbcApplicationDAO
    class DataExporter~T~ {
        <<interface>>
        +export(Path, List~T~)
    }
    class CsvApplicationExporter
    class CsvInterviewExporter
    class ExportService

    ApplicationService --> ApplicationDAO
    JdbcApplicationDAO ..|> ApplicationDAO
    CsvApplicationExporter ..|> DataExporter
    CsvInterviewExporter ..|> DataExporter
    ExportService --> DataExporter
```

The JavaFX controllers in `fx.controller` call the services and know nothing about DAOs or SQL.

Services depend on DAO interfaces, never on JDBC classes, which is what lets the tests run against in-memory implementations. `ExportService` is written against `DataExporter`, so another format needs only a new implementation.
