# Asynchronous Voice Report Pipeline

This post-project extension moves Voice Report conversion and analysis out of the HTTP request path. Submission persists a job, stores the source media in S3, publishes an SQS message, and returns `202 Accepted`. Android polls the persisted status until the job reaches a terminal state.

## Architecture

```text
Android multipart POST
  -> Spring persists PENDING VoiceReport
  -> S3 stores voice-reports/{reportId}/input.<ext>
  -> SQS receives {reportId, mediaObjectKey}
  -> HTTP 202 {reportId, PENDING}

SQS worker
  -> claims PENDING or stale PROCESSING job
  -> downloads source media from S3
  -> ffmpeg converts it to 16 kHz mono WAV
  -> existing Python API performs analysis
  -> Spring persists the result as COMPLETED or FAILED
  -> source media is deleted from S3
```

Android polls `GET /api/voice-reports/{id}/status` every two seconds while the state is `PENDING` or `PROCESSING`. Polling stops on `COMPLETED`, `FAILED`, after 900 attempts (about 30 minutes), or when the ViewModel is cleared.

The submission response and queue message contain identifiers only:

```json
{ "reportId": 42, "status": "PENDING" }
```

```json
{ "reportId": 42, "mediaObjectKey": "voice-reports/42/input.mp4" }
```

## Job lifecycle and failure handling

```text
PENDING -> PROCESSING -> COMPLETED
                      -> FAILED
PENDING ----------------> FAILED  (submission failure)
```

The `voice_report` row stores the processing status, media key, failure reason, processing start time, completion time, and optimistic-lock version. Because Hibernate DDL is disabled, apply [voice_report_async_migration.sql](../backend/src/main/resources/db/voice_report_async_migration.sql) before enabling submission or the consumer.

SQS uses at-least-once delivery. A pessimistic row lock serializes claims:

- `PENDING` can be claimed.
- Active `PROCESSING` duplicates remain unacknowledged.
- `PROCESSING` older than `VOICE_REPORT_PROCESSING_LEASE_SECONDS` can be reclaimed.
- `COMPLETED` and `FAILED` are terminal; duplicates are acknowledged without rerunning analysis.
- Completion side effects execute only on the first transition to `COMPLETED`.

Storage or publish failures mark a pending row `FAILED` and attempt media cleanup. A process crash between MySQL, S3, and SQS can still leave a pending row or orphaned object; an outbox or reconciliation process would be the next production-hardening step. The processing lease must exceed normal job duration because it is a recovery timeout, not a distributed execution lock.

## Configuration

The AWS SDK default credential provider chain is used; credentials are not stored in the repository.

| Environment variable | Purpose | Default |
|---|---|---:|
| `VOICE_REPORT_AWS_REGION` | S3/SQS region | `ap-northeast-2` |
| `VOICE_REPORT_S3_BUCKET` | Private media bucket | none |
| `VOICE_REPORT_SQS_QUEUE_URL` | Standard queue URL | none |
| `VOICE_REPORT_CONSUMER_ENABLED` | Enables queue polling | `false` |
| `VOICE_REPORT_SQS_WAIT_TIME_SECONDS` | Long-poll duration | `10` |
| `VOICE_REPORT_SQS_VISIBILITY_TIMEOUT_SECONDS` | Message visibility | `900` |
| `VOICE_REPORT_PROCESSING_LEASE_SECONDS` | Stale-worker recovery threshold | `900` |

## Benchmark structure

Benchmark-only code lives under
`backend/src/test/java/capstone/voicereport/benchmark/`.

- `VoiceReportMixedWorkloadExperimentTest` — orchestrates scenarios, assertions, and result output.
- `MixedWorkloadHttpDriver` — sends loopback HTTP traffic and records latency and throughput.
- `MixedVoiceLoadHarness` — reproduces the pre-extension synchronous Voice behavior and provides in-memory dependencies for the async worker path.

The async case exercises the current production `VoiceReportController -> VoiceReportService`
submission path and worker processing path. The synchronous case exists only inside the
benchmark.

For the mixed-workload test, `GET /api/workbooks/chapter` uses the real
`WorkbookController -> WorkbookService.getChapter` path; only `UserRepository` is mocked to remove database variability.

## Evaluation methodology

All three stages use a fixed 2,000 ms downstream delay and local test doubles. They measure request-path behavior and eventual completion, not external-service or AI inference performance.

### 1. Request decoupling

The Spring MockMvc comparison used one warm-up batch and three measured batches.

| Concurrency | Sync response p50/p95 | Async acceptance p50/p95 | Completed |
|---:|---:|---:|---:|
| 1 | 2.01 / 2.01 s | 5.7 / 6.2 ms | 3/3 |
| 10 | 2.01 / 2.01 s | 7.9 / 9.6 ms | 30/30 |
| 50 | 2.01 / 2.01 s | 4.0 / 8.3 ms | 150/150 |

Submission acceptance was decoupled from the downstream delay; downstream completion time did not become faster.

### 2. Bounded-load behavior

The embedded Tomcat experiment used 10 request threads, an accept count and connection limit of 200, 10 async workers, loopback HTTP, identical multipart payloads, one concurrency-10 warm-up, and one measured batch per level.

| Voice requests | Sync response p50/p95 | Async `202` p50/p95 | Sync completed | Async completed | Async queue peak |
|---:|---:|---:|---:|---:|---:|
| 10 | 2.02 / 2.02 s | 10.5 / 11.4 ms | 10/10 | 10/10 | 2 |
| 50 | 6.04 / 10.05 s | 17.9 / 21.5 ms | 50/50 | 50/50 | 40 |
| 100 | 10.05 / 20.10 s | 11.3 / 16.2 ms | 100/100 | 100/100 | 90 |

Synchronous waiting accumulated in Tomcat's request backlog; asynchronous waiting accumulated in the worker queue. Total completion time remained similar because both variants retained the same downstream capacity.

### 3. Mixed-workload isolation

This experiment measured the real `GET /api/workbooks/chapter` controller/service path with a deterministic mock `UserRepository`. The server limits matched the bounded-load test. Each condition used one warm-up, then 100 probes released every 50 ms (20 offered requests/second). Voice requests and the first probe shared a start latch; later probes were released at fixed offsets independent of earlier responses.

Probe latency is measured from HTTP send to response. Probe throughput is successful responses divided by the interval from the common release until the final probe response.

| Voice background | Workbook p50 | Workbook p95 | Probe success | Probe throughput | Voice completed/failed |
|---|---:|---:|---:|---:|---:|
| None | 5.9 ms | 8.1 ms | 100/100 | 20.2 req/s | n/a |
| Sync, 50 | 7.53 s | 9.76 s | 100/100 | 9.9 req/s | 50/0 |
| Async, 50 | 4.2 ms | 7.1 ms | 100/100 | 20.2 req/s | 50/0 |
| Sync, 100 | 17.55 s | 19.79 s | 100/100 | 5.0 req/s | 100/0 |
| Async, 100 | 4.1 ms | 5.7 ms | 100/100 | 20.2 req/s | 100/0 |

The bounded-load test shows where backlog moves; the mixed-workload test shows why request-layer isolation matters. Synchronous Voice work occupied the shared request pool and delayed Workbook requests, while the async path kept Workbook latency near its probe-only baseline and completed every job.

## Limitations

- These are bounded, single-JVM local experiments, not production load tests.
- MySQL, S3, SQS, ffmpeg, FastAPI, OpenAI, network transfer, and AI inference are replaced by deterministic doubles.
- Only Voice Report is asynchronous. Workbook, simulation feedback, and Chatbot AI calls still block Spring request threads.
- The results do not establish AWS capacity, production QoS, or faster inference.

Raw results: [baseline.json](async-voice-results/baseline.json), [async.json](async-voice-results/async.json), [bounded-load.json](async-voice-results/bounded-load.json), and [mixed-workload.json](async-voice-results/mixed-workload.json).
