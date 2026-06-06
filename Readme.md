# PROJECT_CONTEXT.md

# 1. Project Overview

## Name

HR AI Portal / Resume Analyzer API

## Purpose

A backend system that helps HR teams:

* Create hiring sessions
* Store job descriptions
* Upload resumes
* Extract resume information using AI
* Score candidates against job requirements
* Rank candidates within a hiring session

## Primary Users

* HR personnel
* Recruiters

## Tech Stack

Backend:

* Java 21
* Spring Boot
* Spring WebFlux (Reactive)

Databases:

* Supabase (relational workflow data)
* MongoDB (resume content and analysis)

AI:

* Groq LLM

Deployment:

* Docker
* Render

---

# 2. High-Level Architecture

HR/Recruiter
↓
REST API (Spring Boot)
↓
Controllers
↓
Services
↓
┌──────────────┬──────────────┐
│              │              │
Supabase     MongoDB        Groq
Workflow     Resume Data    LLM
Data

Workflow:

* USERS creates session
* Job description stored
* Resume uploaded
* PDF text extracted
* Groq analyzes resume
* Resume stored in MongoDB
* Metadata stored in Supabase
* Ranking generated

---

# 3. Folder Structure

common/
├── client/
│   ├── GroqClient
│   └── SupabaseClient
│
├── config/
│   ├── SecurityConfig
│   ├── MongoConfig
│   ├── GroqConfig
│   └── SupabaseConfig
│
├── properties/
│   ├── GroqProperties
│   └── SupabaseProperties
│
├── exception/
│   └── GlobalExceptionHandler
│
└── util/
└── PdfExtractorUtil

modules/

├── users/
├── session/
├── document/
├── resume/
└── jobdescription/

Pattern per module:

module/
├── controller/
├── service/
├── repository/
├── entity/
└── dto/

---

# 4. Database Schema

## Supabase

### HR

Purpose:
Stores users.

Fields:

* userId
* name
* email
* password
* company
* createdAt
* updatedAt
* is_deleted

---

### Session

Purpose:
Hiring campaign.

Fields:

* sessionId
* userId
* title
* createdAt
* is_deleted

Relationship:

users
└── many Sessions

---

### JobDescription

Purpose:
Stores JD associated with session.

Fields:

* jdId
* sessionId
* title
* content
* is_deleted

Relationship:

Session
└── one/many JobDescriptions

---

### DocumentRecord

Purpose:
Tracks uploaded resume files.

Fields:

* documentId
* sessionId
* userId
* filename
* fileHash
* mongoId
* uploadedAt
* jobTitle
* score
* recommendation
* is_deleted

Relationship:

Session
└── many Documents

Document
└── ResumeDocument via mongoId

---

## MongoDB

Collection:
resumes

Document:
ResumeDocument

Fields:

* id
* filename
* contact
* summary
* skills
* experience
* projects
* education
* score
* matchingSkills
* gaps
* recommendation
* uploadedAt

Purpose:
Stores extracted and analyzed resume content.

---

# 5. API Documentation

## USER APIs

POST /api/register

* Register USER

POST /api/login

* Login USER

GET /api/me/{id}

* Get USER details

---

## Session APIs

POST /api/sessions

* Create session

GET /api/sessions/hr/{hrId}

* Sessions for HR

GET /api/sessions/my-sessions

* Get session

PATCH /api/sessions/{sessionId}

* Update title

DELETE /api/sessions/{sessionId}

* Delete session

GET /api/sessions/{sessionId}/rankings

* Get ranked resumes

---

## Document APIs

POST /api/documents/upload
Multipart request:

* file
* session_id
* user_id
* job_title

Uploads and analyzes resume.

GET /api/documents/session/{sessionId}

* Documents in session

GET /api/documents/{mongoId}/analysis

* Full MongoDB resume analysis

---

## Job Description APIs

Controller exists.

Purpose:
Create and manage JDs tied to sessions.

---

# 6. Request Flows

## Resume Upload Flow

1. USER uploads PDF
2. DocumentController receives request
3. DocumentService processes upload
4. PdfExtractorUtil extracts text
5. Groq analyzes resume
6. Structured ResumeDocument created
7. ResumeDocument stored in MongoDB
8. DocumentRecord stored in Supabase
9. Response returned

---

## Ranking Flow

1. Request:
   GET /api/sessions/{sessionId}/rankings

2. SessionService queries DocumentRepository

3. Documents retrieved

4. Sort by score descending

5. Build rankingResponse DTO

6. Return ranked candidates

---

# 7. Service Responsibilities

## UserService

Responsibilities:

* Register user
* Login user
* Retrieve user profile

---

## SessionService

Responsibilities:

* Create session
* Retrieve sessions
* Update session
* Delete session
* Generate rankings

---

## DocumentService

Responsibilities:

* Resume upload
* PDF extraction
* AI analysis
* Save metadata
* Retrieve analysis

---

## JobDescriptionService

Responsibilities:

* Store JDs
* Retrieve JDs
* Associate JD with session

---

# 8. Environment Variables

Required:

MONGO_URI

SUPABASE_URL

SUPABASE_ANON_KEY

SUPABASE_SERVICE_ROLE_KEY

GROQ_API_KEY

PORT

Optional / Legacy:

PINECONE_API_KEY

PINECONE_INDEX_NAME

PINECONE_NAMESPACE

HUGGINGFACE_API_KEY

HF_EMBEDDING_MODEL

---

# 9. Design Decisions

## Why MongoDB?

Resume structure varies.

Different candidates have:

* different projects
* different skills
* different experiences

Document model is flexible.

---

## Why Supabase?

Workflow entities are relational:

* Users
* Sessions
* Documents
* Job Descriptions

Relational DB fits better.

---

## Why Separate Document and Resume?

DocumentRecord:

Tracks workflow metadata.

ResumeDocument:

Stores actual analyzed resume content.

Linked via:

mongoId

---

## Why Reactive?

Project uses:

Mono

Allows non-blocking operations and easier scalability.

---

# 10. Business Rules

1. Every Session belongs to one USER.

2. Every Document belongs to one Session.

3. Resume content is stored in MongoDB.

4. DocumentRecord acts as tracking metadata.

5. Ranking is based on score descending.

6. Document and Resume are connected through mongoId.

7. Uploaded resume receives:

    * score
    * recommendation
    * skill analysis

8. Session rankings are generated from DocumentRecords, not directly from MongoDB.

9. Job descriptions are session-specific.

10. Users only manage their own sessions and documents.

---

# LLM QUICK START

Key Concept:

DocumentRecord ≠ ResumeDocument

DocumentRecord:
Workflow metadata stored in Supabase.

ResumeDocument:
AI-extracted resume content stored in MongoDB.

Connection:

DocumentRecord.mongoId
↓
ResumeDocument.id

Rankings are generated from DocumentRecords sorted by score.
