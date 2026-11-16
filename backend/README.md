# DecisionHub – Backend

Collaborative Decision-Making & Community Polling Platform Backend Service.

DecisionHub is built with Java 21, Spring Boot 4.1.2, Spring Security, Spring Data JPA, and MySQL. It delivers secure, scalable RESTful APIs for decision management, polling, multi-option voting, community discussions, and notifications.

---

## 1. Database Architecture & Schema Design (Phase 2.1 Refined)

The DecisionHub database (`decisionhub`) follows a normalized relational structure (3NF) designed for high concurrency, relational integrity, multi-poll voting flexibility, and strict deduplication.

### Refined Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    USERS ||--o{ DECISIONS : "creates (1:N)"
    USERS ||--o{ COMMUNITIES : "creates (1:N)"
    USERS ||--o{ COMMUNITY_MEMBERS : "joins (1:N)"
    USERS ||--o{ BALLOTS : "casts (1:N)"
    USERS ||--o{ COMMENTS : "writes (1:N)"
    USERS ||--o{ NOTIFICATIONS : "receives (1:N)"
    USERS ||--o{ REPORTS : "files (1:N)"
    USERS ||--o{ VOTES : "legacy_casts (1:N)"

    COMMUNITIES ||--o{ DECISIONS : "contains (1:N)"
    COMMUNITIES ||--o{ COMMUNITY_MEMBERS : "has (1:N)"

    DECISIONS ||--|{ OPTIONS : "has (1:N)"
    DECISIONS ||--o{ BALLOTS : "receives (1:N)"
    DECISIONS ||--o{ COMMENTS : "contains (1:N)"
    DECISIONS ||--o{ REPORTS : "receives (1:N)"
    DECISIONS ||--o{ VOTES : "legacy_receives (1:N)"

    BALLOTS ||--|{ BALLOT_SELECTIONS : "contains (1:N)"
    OPTIONS ||--o{ BALLOT_SELECTIONS : "selected_in (1:N)"

    COMMENTS ||--o{ COMMENTS : "replies_to (parent 1:N)"

    USERS {
        bigint id PK
        varchar full_name
        varchar email UK
        varchar password
        varchar role
        varchar status
        datetime created_at
        datetime updated_at
    }

    COMMUNITIES {
        bigint id PK
        varchar name UK
        text description
        bigint creator_id FK
        datetime created_at
        datetime updated_at
    }

    COMMUNITY_MEMBERS {
        bigint id PK
        bigint community_id FK
        bigint user_id FK
        varchar role
        varchar status
        datetime joined_at
        datetime created_at
        datetime updated_at
    }

    DECISIONS {
        bigint id PK
        varchar title
        text description
        varchar category
        varchar visibility
        varchar status
        varchar poll_type
        bigint creator_id FK
        bigint community_id FK
        datetime deadline
        datetime created_at
        datetime updated_at
    }

    OPTIONS {
        bigint id PK
        bigint decision_id FK
        varchar title
        text description
        int display_order
        datetime created_at
        datetime updated_at
    }

    BALLOTS {
        bigint id PK
        bigint user_id FK
        bigint decision_id FK
        datetime created_at
        datetime updated_at
    }

    BALLOT_SELECTIONS {
        bigint id PK
        bigint ballot_id FK
        bigint option_id FK
        bigint decision_id FK
        int rating
        datetime created_at
        datetime updated_at
    }

    COMMENTS {
        bigint id PK
        bigint user_id FK
        bigint decision_id FK
        text content
        bigint parent_comment_id FK
        datetime created_at
        datetime updated_at
    }

    NOTIFICATIONS {
        bigint id PK
        bigint user_id FK
        varchar type
        text message
        boolean is_read
        bigint reference_id
        datetime created_at
        datetime updated_at
    }

    REPORTS {
        bigint id PK
        bigint user_id FK
        bigint decision_id FK
        varchar report_type
        text description
        varchar status
        datetime created_at
        datetime updated_at
    }
```

---

## 2. Implemented Database Entities & Repositories

### 1. User (`users`)
- **Entity**: `User.java` | **Repository**: `UserRepository.java`
- **Columns**: `id`, `full_name`, `email` (UK), `password`, `role` (`ROLE_USER`, `ROLE_COMMUNITY_MODERATOR`, `ROLE_ADMIN`), `status` (`ACTIVE`, `PENDING`, `SUSPENDED`, `DEACTIVATED`), timestamps.

### 2. Community (`communities`)
- **Entity**: `Community.java` | **Repository**: `CommunityRepository.java`
- **Columns**: `id`, `name` (UK), `description`, `creator_id` (FK to `users.id`), timestamps.
- **Relationships**: Created by a User. Contains `decisions` and `members`.

### 3. Community Member (`community_members`)
- **Entity**: `CommunityMember.java` | **Repository**: `CommunityMemberRepository.java`
- **Columns**: `id`, `community_id` (FK), `user_id` (FK), `role` (`MEMBER`, `MODERATOR`, `ADMIN`), `status` (`ACTIVE`, `PENDING`, `BANNED`, `LEFT`), `joined_at`, timestamps.
- **Constraints**: `UNIQUE KEY uk_community_members_user_community (user_id, community_id)`. Prevents duplicate memberships at the database level.
- **Cascade Behavior**: Deleting a community cleans up membership associations without deleting user accounts.

### 4. Decision (`decisions`)
- **Entity**: `Decision.java` | **Repository**: `DecisionRepository.java`
- **Columns**: `id`, `title`, `description`, `category`, `visibility` (`PUBLIC`, `PRIVATE`), `status` (`DRAFT`, `ACTIVE`, `CLOSED`, `ARCHIVED`), `poll_type` (`SINGLE_CHOICE`, `MULTIPLE_CHOICE`, `RATING`), `creator_id` (FK), `community_id` (FK, nullable), `deadline`, timestamps.
- **Indexes**: `idx_decisions_creator`, `idx_decisions_community`, `idx_decisions_status`, `idx_decisions_category`.

### 5. Option (`options`)
- **Entity**: `Option.java` | **Repository**: `OptionRepository.java`
- **Columns**: `id`, `decision_id` (FK), `title`, `description`, `display_order`, timestamps.
- **Composite Key**: `UNIQUE KEY uk_options_id_decision (id, decision_id)`. Required for composite foreign key validation.

### 6. Ballot (`ballots`)
- **Entity**: `Ballot.java` | **Repository**: `BallotRepository.java`
- **Columns**: `id`, `user_id` (FK), `decision_id` (FK), timestamps.
- **Constraints**: `UNIQUE KEY uk_ballots_user_decision (user_id, decision_id)` and `UNIQUE KEY uk_ballots_id_decision (id, decision_id)`.
- **Purpose**: Establishes the user's ballot submission for a decision. Guarantees one ballot per user per decision.

### 7. Ballot Selection (`ballot_selections`)
- **Entity**: `BallotSelection.java` | **Repository**: `BallotSelectionRepository.java`
- **Columns**: `id`, `ballot_id` (FK), `option_id` (FK), `decision_id` (FK), `rating` (INT, nullable), timestamps.
- **Constraints**: `UNIQUE KEY uk_ballot_selections_ballot_option (ballot_id, option_id)`.
- **Composite Foreign Keys**:
  - `fk_ballot_selections_opt_dec`: `FOREIGN KEY (option_id, decision_id) REFERENCES options (id, decision_id)`
  - `fk_ballot_selections_bal_dec`: `FOREIGN KEY (ballot_id, decision_id) REFERENCES ballots (id, decision_id)`
- **Voting Mechanism Support**:
  - **Single-Choice**: Exactly 1 `BallotSelection` per `Ballot` (enforced at service level).
  - **Multiple-Choice**: Multiple `BallotSelection` rows per `Ballot` (preventing duplicates via `uk_ballot_selections_ballot_option`).
  - **Rating-Based**: Each selection carries a numeric score in `rating`.

### 8. Comment (`comments`)
- **Entity**: `Comment.java` | **Repository**: `CommentRepository.java`
- **Columns**: `id`, `user_id` (FK), `decision_id` (FK), `content`, `parent_comment_id` (FK, nullable), timestamps.
- **Composite Integrity Constraint**:
  - `UNIQUE KEY uk_comments_id_decision (id, decision_id)`
  - `CONSTRAINT fk_comments_parent_composite FOREIGN KEY (parent_comment_id, decision_id) REFERENCES comments (id, decision_id) ON DELETE CASCADE`
  - Guarantees that threaded replies cannot reference a parent comment from a different decision.

### 9. Notification (`notifications`)
- **Entity**: `Notification.java` | **Repository**: `NotificationRepository.java`
- **Columns**: `id`, `user_id` (FK), `type` (`VOTE_RECEIVED`, `DECISION_CLOSED`, `COMMENT_REPLY`, `SYSTEM`), `message`, `is_read`, `reference_id`, timestamps.

### 10. Report (`reports`)
- **Entity**: `Report.java` | **Repository**: `ReportRepository.java`
- **Columns**: `id`, `user_id` (FK), `decision_id` (FK), `report_type` (`SPAM`, `INAPPROPRIATE`, `MISINFORMATION`, `OTHER`), `description`, `status` (`PENDING`, `REVIEWED`, `DISMISSED`, `RESOLVED`), timestamps.

### 11. Legacy Vote Table (`votes`)
- Preserved for backwards compatibility with legacy phase data.
- Controlled migration plan documented below to transfer historical votes into `ballots` and `ballot_selections`.

---

## 3. Relational Integrity & Cross-Entity Consistency

| Integrity Boundary | Level of Enforcement | How It Is Enforced |
| :--- | :--- | :--- |
| **Duplicate Community Membership** | Database Engine | `UNIQUE (user_id, community_id)` in `community_members`. |
| **One Ballot Per User Per Decision** | Database Engine | `UNIQUE (user_id, decision_id)` in `ballots`. |
| **Duplicate Option on Same Ballot** | Database Engine | `UNIQUE (ballot_id, option_id)` in `ballot_selections`. |
| **Cross-Decision Option Selection** | Database Engine | Composite Foreign Key `(option_id, decision_id) REFERENCES options(id, decision_id)` in `ballot_selections`. |
| **Cross-Decision Comment Reply** | Database Engine | Composite Foreign Key `(parent_comment_id, decision_id) REFERENCES comments(id, decision_id)` in `comments`. |
| **Single-Choice / Max-Choice Quotas** | Service Level (Phase 4) | Validated in `VotingService` against `decision.pollType` and `maxChoicesAllowed`. |
| **Rating Score Boundaries (e.g., 1-5)** | Service Level (Phase 4) | Validated in `VotingService` and Jakarta Validation annotations. |

---

## 4. Database Migration Plan for Legacy Votes

To migrate historical records from the initial `votes` table into normalized `ballots` and `ballot_selections`:

```sql
-- Step 1: Migrate unique ballots from votes into ballots
INSERT INTO ballots (user_id, decision_id, created_at, updated_at)
SELECT v.user_id, v.decision_id, MIN(v.created_at), MIN(v.updated_at)
FROM votes v
GROUP BY v.user_id, v.decision_id;

-- Step 2: Migrate selections into ballot_selections matching the created ballot
INSERT INTO ballot_selections (ballot_id, option_id, decision_id, rating, created_at, updated_at)
SELECT b.id, v.option_id, v.decision_id, NULL, v.created_at, v.updated_at
FROM votes v
JOIN ballots b ON b.user_id = v.user_id AND b.decision_id = v.decision_id;

-- Step 3: Archive or deprecate the legacy votes table after verification
-- (DROP TABLE votes; executed only after verifying migration counts)
```

---

## 5. MySQL Installation & Environment Setup

1. **Verify Connectivity**:
   ```powershell
   Test-NetConnection -ComputerName localhost -Port 3306
   ```
2. **Environment Variables**:
   ```powershell
   $env:DB_USERNAME="root"
   $env:DB_PASSWORD="your_mysql_password"
   $env:DB_URL="jdbc:mysql://localhost:3306/decisionhub?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
   ```

---

## 6. Running Tests & Package Build

To run the entire 24-test suite:
```powershell
cd backend
.\mvnw.cmd test
```

To build the executable production JAR:
```powershell
cd backend
.\mvnw.cmd package
```
Output artifact: `target/backend-0.0.1-SNAPSHOT.jar`


---

## 7. Option Comparison & Evaluation Matrix (Milestone 2 – Phase 2)

### 1. Comparison Architecture & Entity Relationships

The Option Comparison System delivers structured evaluation matrices, qualitative trade-off analyses, and deterministic multi-criteria decision rankings.

```mermaid
erDiagram
    DECISIONS ||--o{ COMPARISON_CRITERIA : "defines (1:N)"
    DECISIONS ||--|{ OPTIONS : "contains (1:N)"
    OPTIONS ||--o{ OPTION_SCORES : "evaluated_in (1:N)"
    COMPARISON_CRITERIA ||--o{ OPTION_SCORES : "rates (1:N)"
    OPTIONS ||--o| OPTION_COMPARISONS : "describes (1:1)"
    OPTIONS ||--o{ OPTION_PROS_CONS : "has (1:N)"

    COMPARISON_CRITERIA {
        bigint id PK
        bigint decision_id FK
        varchar name
        varchar description
        double weight
        double min_score
        double max_score
        varchar polarity
        boolean is_default
        datetime created_at
        datetime updated_at
    }

    OPTION_SCORES {
        bigint id PK
        bigint option_id FK
        bigint criterion_id FK
        double raw_score
        varchar notes
        datetime created_at
        datetime updated_at
    }

    OPTION_COMPARISONS {
        bigint id PK
        bigint option_id FK UK
        text feature_values
        text cost_details
        text benefit_details
        text risk_details
        text time_details
        text convenience_details
        datetime created_at
        datetime updated_at
    }

    OPTION_PROS_CONS {
        bigint id PK
        bigint option_id FK
        varchar type
        varchar description
        int display_order
        datetime created_at
        datetime updated_at
    }
```

### 2. Multi-Criteria Scoring Formula & Normalization

Given a decision with $M$ criteria $C_1, C_2, \dots, C_M$ and $N$ options $O_1, O_2, \dots, O_N$:

1. **Normalized Criterion Weight**:
   $$w_j = \frac{W_j}{\sum_{k=1}^M W_k} \quad \text{where } W_j > 0$$

2. **Polarity-Aware Normalization to 0–10 Scale**:
   For an option with score $S_{i,j} \in [\text{Min}_j, \text{Max}_j]$:
   - **Higher is Better** (e.g. `BENEFITS`, `CONVENIENCE`, `QUALITY`):
     $$\text{NormalizedScore}(S_{i,j}) = 10.0 \times \left( \frac{S_{i,j} - \text{Min}_j}{\text{Max}_j - \text{Min}_j} \right)$$
   - **Lower is Better** (e.g. `COST`, `RISK`, `TIME`):
     $$\text{NormalizedScore}(S_{i,j}) = 10.0 \times \left( \frac{\text{Max}_j - S_{i,j}}{\text{Max}_j - \text{Min}_j} \right)$$
     *(e.g. on a 1–10 scale, an option with low raw cost score $S=1$ yields a top normalized score of $10.0$; high cost $S=10$ yields $0.0$)*

3. **Missing Value Handling**:
   Unscored criteria automatically default to the neutral midpoint fallback:
   $$S_{i,j}^{\text{fallback}} = \frac{\text{Min}_j + \text{Max}_j}{2.0}$$
   (e.g., $5.5$ on a 1–10 scale), flagged with `isFallback: true` in API responses.

4. **Total Weighted Score**:
   $$\text{TotalScore}_i = \sum_{j=1}^M \left( w_j \times \text{NormalizedScore}(S_{i,j}) \right) \in [0.0, 10.0]$$
   $$\text{ScorePercentage}_i = \text{TotalScore}_i \times 10 \in [0.0\%, 100.0\%]$$

5. **Deterministic Multi-Level Tie-Breaking**:
   Options are strictly ordered by:
   1. `totalWeightedScore` descending
   2. Normalized score on the highest-weighted criterion descending
   3. Option `displayOrder` ascending
   4. Option `id` ascending

### 3. Comparison System REST API Endpoints

| Method | Endpoint | Description | Auth Required |
|:---|:---|:---|:---|
| `POST` | `/api/v1/decisions/{id}/criteria` | Create custom comparison criterion | Bearer JWT (Creator/Admin) |
| `POST` | `/api/v1/decisions/{id}/criteria/default` | Initialize standard defaults (`COST`, `BENEFITS`, `RISK`, `TIME`, `CONVENIENCE`) | Bearer JWT (Creator/Admin) |
| `GET` | `/api/v1/decisions/{id}/criteria` | Retrieve criteria definitions for decision | Public / Auth |
| `PUT` | `/api/v1/decisions/{id}/criteria/{criterionId}` | Update criterion weight, scale, or polarity | Bearer JWT (Creator/Admin) |
| `DELETE` | `/api/v1/decisions/{id}/criteria/{criterionId}` | Delete criterion and cascade remove associated scores | Bearer JWT (Creator/Admin) |
| `PUT` | `/api/v1/decisions/{id}/options/{optId}/comparison` | Upsert qualitative feature and trade-off details | Bearer JWT (Creator/Admin) |
| `GET` | `/api/v1/decisions/{id}/options/{optId}/comparison` | Retrieve qualitative comparison details | Public / Auth |
| `POST` | `/api/v1/decisions/{id}/options/{optId}/scores` | Set / update numerical scores for an option | Bearer JWT (Creator/Admin) |
| `GET` | `/api/v1/decisions/{id}/options/{optId}/scores` | Retrieve numerical scores for an option | Public / Auth |
| `POST` | `/api/v1/decisions/{id}/options/{optId}/pros-cons` | Add advantage (`PRO`) or disadvantage (`CON`) | Bearer JWT (Creator/Admin) |
| `GET` | `/api/v1/decisions/{id}/options/{optId}/pros-cons` | Retrieve all pros and cons for an option | Public / Auth |
| `PUT` | `/api/v1/decisions/{id}/options/{optId}/pros-cons/{proConId}` | Update a pro or con entry | Bearer JWT (Creator/Admin) |
| `DELETE` | `/api/v1/decisions/{id}/options/{optId}/pros-cons/{proConId}` | Delete a pro or con entry | Bearer JWT (Creator/Admin) |
| `GET` | `/api/v1/decisions/{id}/comparison-matrix` | Generate full comparison matrix with rankings | Public / Auth |
| `GET` | `/api/v1/decisions/{id}/rankings` | Retrieve ranked options with score breakdowns | Public / Auth |

### 4. Example Request & Response Payloads

#### A. Set Option Scores
`POST /api/v1/decisions/1/options/1/scores`
```json
{
  "scores": [
    { "criterionId": 1, "rawScore": 2.0, "notes": "Very low infrastructure cost" },
    { "criterionId": 2, "rawScore": 8.5, "notes": "High feature flexibility" },
    { "criterionId": 3, "rawScore": 3.0, "notes": "Low operational risk" }
  ]
}
```

**Success Response (HTTP 200 OK)**:
```json
{
  "success": true,
  "message": "Option scores saved successfully",
  "data": [
    {
      "id": 1,
      "optionId": 1,
      "criterionId": 1,
      "criterionName": "COST",
      "rawScore": 2.0,
      "notes": "Very low infrastructure cost",
      "createdAt": "2026-11-16T16:15:00",
      "updatedAt": "2026-11-16T16:15:00"
    }
  ],
  "timestamp": "2026-11-16T16:15:00"
}
```

#### B. Generate Comparison Matrix
`GET /api/v1/decisions/1/comparison-matrix`

**Success Response (HTTP 200 OK)**:
```json
{
  "success": true,
  "message": "Comparison matrix generated successfully",
  "data": {
    "decisionId": 1,
    "decisionTitle": "Select Core Backend Architecture",
    "winnerOptionTitle": "Modular Monolith",
    "criteria": [
      {
        "id": 1,
        "name": "COST",
        "weight": 20.0,
        "minScore": 1.0,
        "maxScore": 10.0,
        "polarity": "LOWER_IS_BETTER"
      }
    ],
    "rankedOptions": [
      {
        "rank": 1,
        "optionId": 1,
        "title": "Modular Monolith",
        "totalWeightedScore": 8.45,
        "scorePercentage": 84.5,
        "criterionScores": [
          {
            "criterionId": 1,
            "criterionName": "COST",
            "weight": 20.0,
            "normalizedWeight": 0.2222,
            "polarity": "LOWER_IS_BETTER",
            "rawScore": 2.0,
            "normalizedScore": 8.89,
            "weightedContribution": 1.975,
            "isFallback": false,
            "notes": "Very low infrastructure cost"
          }
        ],
        "pros": [
          { "id": 1, "type": "PRO", "description": "Fast ACID transactions" }
        ],
        "cons": [
          { "id": 2, "type": "CON", "description": "Coupled deployments" }
        ]
      }
    ]
  }
}
```

### 5. Error Cases & Validation Handling

- **Score Range Exceeded (`400 Bad Request`)**:
  ```json
  {
    "status": 400,
    "error": "Bad Request",
    "message": "Score 15.00 for criterion 'COST' is outside valid range [1.00, 10.00]",
    "path": "/api/v1/decisions/1/options/1/scores"
  }
  ```
- **Cross-Decision Mismatch (`400 Bad Request`)**:
  Thrown when attempting to set scores or pros/cons on an option using a decision ID it does not belong to:
  `Option with id 5 does not belong to decision 1`
- **Duplicate Criterion Name (`400 Bad Request`)**:
  `A criterion with the name 'COST' already exists for this decision`
- **Unauthorized Modification (`403 Forbidden`)**:
  `You do not have permission to modify comparison criteria or data for this decision`
