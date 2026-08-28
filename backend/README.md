# I:ON Backend

## Overview

The backend is a Java 21 Spring Boot application that exposes the REST API used by the Android client. It coordinates user and domain workflows, persists application data, accepts media uploads, and calls the Python AI services for generated or analyzed content.

## Architecture and Request Flow

```text
Android client
      |
      v
REST controller -> domain service -> Spring Data repository -> MySQL
                         |
                         v
                  WebClient adapter -> FastAPI service
```

Controllers define the application-facing REST boundary. Services implement workflow logic and use Spring Data repositories for persistence. Workbook, chatbot, and voice-report flows call separately configured Python services through Spring WebClient; connection and response timeouts are configuration properties rather than implicit defaults.

## Modules

- `user` — registration, sign-in, user profiles, and parenting-style assessment data
- `home` — home summaries, profile information, phrases, and rewards
- `workbook` — workbook lessons, theory, answers, simulations, and generated feedback
- `chatbot` — chat sessions, question/answer history, and AI-service delegation
- `voicereport` — media processing, AI analysis requests, report persistence, and report retrieval
- `config`, `web`, `common`, and `support` — service clients, request handling, shared errors, file support, and configuration binding

## Persistence

Spring Data JPA repositories and Hibernate map the application entities to MySQL. The model includes users and assessment results, home/profile data, workbook and simulation records, chatbot history, and structured voice-report results.

`WorkbookRepository` illustrates the measured data-access patterns: a lesson-list projection, an exact lookup by user/chapter/lesson, and an ordered first-lesson lookup. These production query shapes are the subject of the later performance study; the application schema itself was not changed by that benchmark commit.

## Configuration

The committed configuration contains no literal database password. `application.yml` references `${DB_PASSWORD}`, which must be supplied by the runtime environment. Spring configuration can also be overridden externally for the datasource URL, datasource username, AI-service base URLs, service timeouts, upload location, and ffmpeg path.

Do not commit local credentials or environment-specific override files. Relevant local overrides are already covered by the repository ignore rules.

## Running Locally

Requirements:

- Java 21
- MySQL compatible with the configured schema
- `DB_PASSWORD` and any required datasource overrides
- Reachable AI-service endpoints for workbook, chatbot, or voice-report flows
- ffmpeg for voice media conversion

From the repository root:

```bash
cd backend
export DB_PASSWORD='<local value>'
./gradlew bootRun
```

Build and run tests with:

```bash
cd backend
./gradlew build
```

The original hosted database and AI endpoints are not assumed to remain available. Use external Spring configuration to point the application at local or otherwise authorized services.

## Performance Study

The [Workbook query performance study](../perf/README.md) is an independent post-project extension. It evaluates the actual `WorkbookRepository` query shapes against deterministic, reconstructed local MySQL datasets and compares the baseline index with a workload-aligned composite index. It is not a production RDS benchmark.

## Related Documentation

- [Project overview](../README.md)
- [Android client](../android/README.md)
- [AI services](../ai/README.md)
