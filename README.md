# AI Incident Assistant

Spring Boot service that triages production-style incident text with **Anthropic Claude** via **Spring AI**. The design uses a **multi-stage agent** (not a single monolithic prompt), returns **machine-readable JSON**, and applies **validation plus recovery-oriented retries** when structured output is invalid.

## How to run (minimal steps)

Prerequisites: **JDK 17+**, **Maven**, and Lombok annotation processing in the IDE (if you open the code).

1. Set the API key:

```bash
export ANTHROPIC_API_KEY="YOUR_KEY"
```

2. Start the application:

```bash
mvn spring-boot:run
```

3. Call the analyze endpoint (default port **8080**):

```bash
curl -sS "http://localhost:8080/api/v1/incidents/analyze" \
  -H "Content-Type: application/json" \
  -d '{"description":"Customers cannot pay by card. payment-service logs show PayGate timeouts since 12:05 UTC; other services look normal."}' \
  | jq .
```

Optional: `ANTHROPIC_MODEL`, `ANTHROPIC_TEMPERATURE`, `ANTHROPIC_MAX_TOKENS` (see `src/main/resources/application.yaml`). The system prompt is configured under `incident.assistant.prompt.system`; if empty, the app uses the default in `DefaultIncidentTriagePromptComposer`.

## Agent structure

The triage flow is orchestrated by `DefaultIncidentTriageAgent`, which coordinates parsing, static knowledge injection, prompt composition, the LLM call, JSON extraction, validation, and retry with **recovery hints** when validation fails. The LLM is hidden behind `IncidentLlmClient` so the rest of the stack stays testable and swappable.

**Pipeline (step-by-step):**

1. **Parse input** (`IncidentInputParsingStage`) — reject blank text; enforce `max-input-length`.
2. **Assemble context** (`IncidentKnowledgeAssemblyStage`) — attach topology and examples from `classpath:agent/incident-knowledge.md` through `IncidentKnowledgeProvider`.
3. **Compose prompts** (`IncidentTriagePromptComposer`) — system contract plus user content (incident + knowledge + optional `RecoveryHint`).
4. **Generate** (`IncidentLlmClient` / Spring AI `ChatModel`) — model returns text that should contain one JSON object.
5. **Extract** (`JsonPayloadExtractor`) — tolerate prose or markdown fences around the JSON.
6. **Validate** (`IncidentAnalysisStructuredOutputValidator`, `OutputLanguageConsistencyChecker`) — enforce shape (e.g. 1–3 hypotheses, 2–3 `next_steps` each, `severity` enum) and a lightweight language/script consistency check for Latin-heavy incidents.
7. **Recover** (`IncidentAnalysisRecoveryPlanner`) — on validation failure, plan the next attempt with explicit repair guidance (and a truncated view of the bad output), up to `incident.assistant.llm.max-attempts`.

## How behavior was tested

The following scenarios cover automated tests (no real Anthropic calls) and manual smoke checks against a live key.

| # | Scenario | Expected outcome |
|---|----------|------------------|
| 1 | **JSON extraction** (`JsonPayloadExtractorTest`): model text with only prose + embedded `{"a":1}`, or fenced `` ```json ... ``` `` blocks | Extracted string is the inner JSON object; missing JSON throws `StructuredOutputValidationException`. |
| 2 | **Schema validation** (`IncidentAnalysisStructuredOutputValidatorTest`): valid PayGate-style payload vs payload with **four** hypotheses | Valid JSON maps to `IncidentAnalysisResult` (category, summary, severity, hypotheses with 2+ `next_steps`); too many hypotheses is rejected. |
| 3 | **Agent retry path** (`DefaultIncidentTriageAgentTest`): first `llmClient.complete` returns `{}`, second returns valid analysis JSON for a card/PayGate incident | Second attempt succeeds with `severity` **HIGH** and one hypothesis; **`complete` is invoked exactly twice** (invalid then valid). |
| 4 | **HTTP integration** (`IncidentAnalysisControllerIntegrationTest`): `POST /api/v1/incidents/analyze` with `{"description":"PayGate timeouts in payment-service logs."}` while `ChatModel` is mocked to return valid JSON | **HTTP 200**; response JSON includes `category`, `summary`, `severity: "high"`, and `hypotheses[0].next_steps` with at least two string steps. |
| 5 | **Manual smoke (real API)**: incidents such as **DB saturation** (504 on payments + reporting + DB CPU), **notification SMTP errors**, or **auth-service 401 / bad token signatures** | Responses stay in the **required JSON shape**; content should align with the incident (e.g. DB vs email vs auth), with **operational next steps** grounded in the static knowledge file—not exact wording, but plausible categories and severities. |

## Trade-offs

**What was simplified to fit the time box**

- **Stateless triage** — one-shot analysis per HTTP request; no session history or follow-up turns.
- **Static knowledge** — platform context lives in `agent/incident-knowledge.md`, not live metrics, RAG, external APIs.
- **Recovery scope** — retries and hints focus on **invalid structured JSON**; there is no general policy for Anthropic rate limits, timeouts, or 5xx beyond what Spring AI does by default.
- **Language guardrail** — a small script-consistency heuristic instead of a full locale or content-policy pipeline.

**What we would do differently with more time**

- **Observability** — structured logs, request IDs, latency and token-usage metrics, and optional tracing around each agent stage.
- **Resilience** — dedicated backoff/retry for provider errors, circuit breaking, and configurable timeouts.
- **Grounding** — RAG or internal links to real runbooks, dashboards, and recent incidents instead of a single static markdown file.
- **Product hardening** — auth for the API, rate limiting, persistence of analyses, and optional human feedback to improve prompts and validation rules.
