# I:ON AI Services

The `ai/` directory contains Python services that generate personalized workbook content, produce workbook feedback, answer parenting questions, and analyze parent-child conversations. The Spring Boot backend calls these services over HTTP and remains the application-facing API boundary.

## Services

| Directory | Responsibility |
|---|---|
| `chatbot/` | Builds profile-aware chatbot prompts, retrieves relevant reference material, and generates a response. |
| `workbook/` | Generates workbook activities and simulated conversation scenarios from user and chapter context. |
| `workbook_feedback/` | Generates feedback for workbook answers and conversation simulations. |
| `voiceReport/` | Accepts conversation audio, processes the transcript, and returns a structured feedback report. |

Each directory is an independently runnable FastAPI application with its own dependency list.

## Request Flow

```text
Android client
      |
      v
Spring Boot backend
      |
      v
FastAPI service
   |        |
   v        v
MySQL   OpenAI API
```

The backend forwards user context in request headers and bodies through configured WebClient instances. The AI services load profile or curriculum/reference data from MySQL, construct task-specific prompts, call the configured model API, validate structured responses where required, and return them to the backend.

The chatbot adds a retrieval step before generation. It encodes questions with sentence-transformers, computes cosine similarity against stored vectors with NumPy, and reads application data through PyMySQL.

## API Boundaries

The implemented FastAPI routes include:

- `POST /api/chatbot`
- `POST /api/workbooks`
- `POST /api/workbook_simulation`
- `POST /api/workbook_feedback`
- `POST /api/workbook_feedback_simulation`
- `POST /api/voice-report`

The services also expose lightweight root or health endpoints where implemented.

## Personalization Inputs

Prompt construction can incorporate repository-backed user information such as child age, parenting profile, goals, child characteristics, and communication preferences. Individual services use only the inputs needed for their task.

## Technology

- Python 3
- FastAPI, Uvicorn, and Pydantic
- OpenAI Python SDK
- PyMySQL
- sentence-transformers and NumPy for chatbot retrieval
- NumPy, pandas, and PyTorch in the voice-report service

## Configuration and Local Use

The applications load environment variables with `python-dotenv` and expect an environment-backed local `config` module for model and database settings. That configuration is not committed. Do not place API keys or database passwords in source files.

Install dependencies inside the service you want to run, for example:

```bash
cd ai/chatbot
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

Before starting a service, provide its required OpenAI and database configuration through local environment/configuration files. A complete request also depends on reachable MySQL data and the backend contract; the original hosted demonstration environment is not assumed to be online.

## Related Documentation

- [Project overview](../README.md)
- [Backend](../backend/README.md)
- [Android client](../android/README.md)
