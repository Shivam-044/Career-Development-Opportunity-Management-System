# Database design

Database: `student_placement_tracker` (MySQL). The full DDL is in [`database/schema.sql`](../database/schema.sql).

```mermaid
erDiagram
    student_profile ||--o{ student_skills : tracks
    skills ||--o{ student_skills : "is tracked in"
    skills ||--o{ opportunity_required_skills : "is required by"
    companies ||--o{ opportunities : lists
    opportunities ||--o{ opportunity_required_skills : requires
    student_profile ||--o{ applications : submits
    opportunities ||--o{ applications : "is applied to in"
    applications ||--o{ interviews : has

    student_profile {
        bigint student_id PK
        varchar full_name
        varchar email
        varchar roll_number
        varchar degree
        varchar department
        int graduation_year
        decimal cgpa
    }
    skills {
        bigint skill_id PK
        varchar skill_name UK
    }
    student_skills {
        bigint student_id PK, FK
        bigint skill_id PK, FK
        varchar proficiency
        varchar learning_status
        varchar notes
    }
    companies {
        bigint company_id PK
        varchar company_name UK
        varchar industry
        varchar website
        varchar location
        tinyint is_archived
    }
    opportunities {
        bigint opportunity_id PK
        bigint company_id FK
        varchar role_title
        varchar opportunity_type
        varchar work_mode
        decimal minimum_cgpa
        date application_deadline
        varchar listing_status
    }
    opportunity_required_skills {
        bigint opportunity_id PK, FK
        bigint skill_id PK, FK
    }
    applications {
        bigint application_id PK
        bigint student_id FK
        bigint opportunity_id FK
        varchar status
        date applied_on
    }
    interviews {
        bigint interview_id PK
        bigint application_id FK
        varchar round_name
        datetime scheduled_at
        varchar interview_mode
        varchar result
    }
```

## Integrity rules

- `applications` has a unique key on `(student_id, opportunity_id)`, so the same opportunity cannot be applied to twice. The service checks first and the database enforces it.
- `student_skills` has a composite primary key on `(student_id, skill_id)`.
- `companies.company_name` is unique.
- Status and type columns are guarded by `CHECK` constraints that mirror the Java enums.
- Deleting an interview-bearing application cascades to its interviews. Companies and opportunities are archived rather than deleted, and foreign keys without a cascade stop accidental removal.
